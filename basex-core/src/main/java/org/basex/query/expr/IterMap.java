package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple map expression: iterative evaluation, no positional access.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IterMap extends SimpleMap {
  /** Item-based or iterative processing. */
  private final boolean[] items;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs expressions
   */
  IterMap(final InputInfo info, final Expr... exprs) {
    super(info, exprs);

    final int el = exprs.length;
    items = new boolean[el];
    for(int e = 0; e < el; e++) items[e] = exprs[e].seqType().zeroOrOne();
  }

  @Override
  public Iter iter(final QueryContext qc) {
    final int el = exprs.length - 1;
    final Iter[] iters = new Iter[el + 1];
    final Value[] values = new Value[el + 1];
    values[0] = qc.focus.value;

    return new Iter() {
      int pos;

      @Override
      public Item next() throws QueryException {
        qc.checkStop();

        final QueryFocus qf = qc.focus;
        Item item = null;
        try {
          do {
            qf.value = values[pos];
            Iter iter = iters[pos];
            if(items[pos]) {
              // item-based processing
              if(iter == Empty.ITER) {
                iters[pos--] = null;
              } else {
                item = exprs[pos].item(qc, info);
                if(item.isEmpty()) {
                  item = null;
                  pos--;
                } else {
                  iters[pos] = Empty.ITER;
                  if(pos < el) {
                    values[++pos] = item;
                    item = null;
                  }
                }
              }
            } else {
              // iterative processing
              if(iter == null) {
                iter = exprs[pos].iter(qc);
                iters[pos] = iter;
              }
              item = iter.next();
              if(item == null) {
                iters[pos--] = null;
              } else if(pos < el) {
                values[++pos] = item;
                item = null;
              }
            }
          } while(item == null && pos != -1);
          return item;
        } finally {
          qf.value = values[0];
        }
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final QueryFocus qf = qc.focus;
    final Value qv = qf.value;
    try {
      Value value = exprs[0].value(qc);
      final int el = exprs.length;
      for(int e = 1; e < el; e++) {
        final Expr expr = exprs[e];
        final ValueBuilder vb = new ValueBuilder(qc, e < el - 1 ? -1 : size());
        final Iter iter = value.iter();
        for(Item item; (item = qc.next(iter)) != null;) {
          qf.value = item;
          vb.add(expr.value(qc));
        }
        value = vb.value(expr);
      }
      return value;
    } finally {
      qf.value = qv;
    }
  }

  @Override
  public IterMap copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new IterMap(info, Arr.copyAll(cc, vm, exprs)));
  }

  @Override
  public String description() {
    return "iterative " + super.description();
  }
}
