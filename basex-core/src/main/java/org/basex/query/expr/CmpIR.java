package org.basex.query.expr;

import static java.lang.Long.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Integer range expression.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class CmpIR extends Single {
  /** Minimum. */
  public final long min;
  /** Maximum. */
  public final long max;

  /** Evaluation flag: atomic evaluation. */
  private boolean single;

  /**
   * Constructor.
   * @param expr (compiled) expression
   * @param min minimum value
   * @param max maximum value
   * @param info input info
   */
  private CmpIR(final Expr expr, final long min, final long max, final InputInfo info) {
    super(info, expr, SeqType.BOOLEAN_O);
    this.min = min;
    this.max = max;
  }

  /**
   * Tries to convert the specified expression into a range expression.
   * @param cc compilation context
   * @param info input info
   * @param expr expression to be compared
   * @param min minimum position
   * @param max minimum position (inclusive)
   * @return expression
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final InputInfo info, final Expr expr,
      final long min, final long max) throws QueryException {
    return min > max ? Bln.FALSE : min == MIN_VALUE && max == MAX_VALUE ? Bln.TRUE :
      new CmpIR(expr, min, max, info).optimize(cc);
  }

  /**
   * Tries to convert the specified expression into a range expression.
   * @param cc compilation context
   * @param cmp expression to be converted
   * @param eq also rewrite equality comparisons of single integers
   * @return new or original expression
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final CmpG cmp, final boolean eq)
      throws QueryException {

    // only rewrite deterministic expressions
    final Expr expr1 = cmp.exprs[0], expr2 = cmp.exprs[1];
    if(cmp.has(Flag.NDT)) return cmp;

    // only rewrite integer or equality comparisons
    // allowed: $integer > 2, $value = 10 to 20; rejected: $double  > 2
    final Type type1 = expr1.seqType().type;
    final boolean cmpEq = cmp.op == OpG.EQ;
    if(!(type1.instanceOf(AtomType.INTEGER) || cmpEq && type1.isUntyped())) return cmp;

    long mn = MAX_VALUE, mx = MIN_VALUE;
    if(expr2 instanceof RangeSeq) {
      final long[] range = ((RangeSeq) expr2).range(false);
      mn = range[0];
      mx = range[1];
    } else if(expr2 instanceof Int && (eq || !cmpEq)) {
      mn = ((Int) expr2).itr();
      mx = mn;
    } else {
      return cmp;
    }

    switch(cmp.op) {
      case GE: mx = MAX_VALUE; break;
      case GT: mn++; mx = MAX_VALUE; break;
      case LE: mn = MIN_VALUE; break;
      case LT: mn = MIN_VALUE; mx--; break;
      case EQ: break;
      default: return cmp;
    }
    return get(cc, cmp.info, expr1, mn, mx);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.NUMBER, cc);

    final SeqType st = expr.seqType();
    single = st.zeroOrOne() && !st.mayBeArray();

    if(expr instanceof Value) return cc.preEval(this);

    Expr ex = this;
    if(Function.POSITION.is(expr)) {
      final long mn = Math.max(min, 1), mx = max - mn + 1;
      ex = ItrPos.get(RangeSeq.get(mn, mx, true), OpV.EQ, info);
    }
    return cc.replaceWith(this, ex);
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // atomic evaluation of arguments (faster)
    if(single) {
      final Item item = expr.item(qc, info);
      return Bln.get(item != Empty.VALUE && inRange(item));
    }

    // pre-evaluate ranges
    if(expr instanceof Range || expr instanceof RangeSeq) {
      final Value value = expr.value(qc);
      final long size = value.size();
      if(size == 0) return Bln.FALSE;
      if(size == 1) return Bln.get(inRange((Item) value));
      final long[] range = ((RangeSeq) value).range(false);
      return Bln.get(range[1] >= min && range[0] <= max);
    }

    // iterative evaluation
    final Iter iter = expr.atomIter(qc, info);
    for(Item item; (item = qc.next(iter)) != null;) {
      if(inRange(item)) return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  /**
   * Checks if the specified value is within the allowed range.
   * @param item value to check
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean inRange(final Item item) throws QueryException {
    final double value = item.dbl(info);
    return value >= min && value <= max && value == (long) value;
  }

  @Override
  public Expr mergeEbv(final Expr ex, final boolean or, final CompileContext cc)
      throws QueryException {
    if(ex instanceof CmpIR) {
      final CmpIR cmp = (CmpIR) ex;
      if(expr.equals(cmp.expr) && (!or || max >= cmp.min && min <= cmp.max)) {
        final long mn = or ? Math.min(min, cmp.min) : Math.max(min, cmp.min);
        final long mx = or ? Math.max(max, cmp.max) : Math.min(max, cmp.max);
        return get(cc, info, expr, mn, mx);
      }
    }
    return null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final CmpIR cmp = new CmpIR(expr.copy(cc, vm), min, max, info);
    cmp.single = single;
    return copyType(cmp);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof CmpIR)) return false;
    final CmpIR c = (CmpIR) obj;
    return min == c.min && max == c.max && super.equals(obj);
  }

  @Override
  public String description() {
    return "integer range comparison";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, MIN, min, MAX, max, SINGLE, single), expr);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(expr);
    if(min == max) {
      qs.token("=").token(min);
    } else if(min != MIN_VALUE && max != MAX_VALUE) {
      qs.token("=").token(min).token(TO).token(max);
    } else if(min != MIN_VALUE) {
      qs.token(">=").token(min);
    } else {
      qs.token("<=").token(max);
    }
  }
}
