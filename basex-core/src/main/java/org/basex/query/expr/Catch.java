package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Catch clause.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Catch extends Single {
  /** Error Strings. */
  private static final String[] NAMES = {
    "code", "description", "value", "module",
    "line-number", "column-number", "additional", "stack-trace",
    "map"
  };
  /** Error types. */
  private static final SeqType[] TYPES = {
    SeqType.QNAME_O, SeqType.STRING_ZO, SeqType.ITEM_ZM, SeqType.STRING_ZO,
    SeqType.INTEGER_ZO, SeqType.INTEGER_ZO, SeqType.STRING_O, SeqType.STRING_O,
    SeqType.MAP_O
  };
  /** Error QNames. */
  private static final QNm[] QNAMES = new QNm[NAMES.length];
  /** Error string items. */
  private static final Str[] STRINGS = new Str[NAMES.length];

  static {
    for(int n = NAMES.length - 1; n >= 0; n--) {
      final String name = NAMES[n];
      QNAMES[n] = qname(name);
      STRINGS[n] = Str.get(name);
    }
  }

  /** Error tests. */
  private final ArrayList<Test> tests;
  /** Error variables. */
  private final Var[] vars;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr expression
   * @param vars variables to be bound
   * @param tests error tests
   */
  public Catch(final InputInfo info, final Expr expr, final Var[] vars,
      final ArrayList<Test> tests) {
    super(info, expr, SeqType.ITEM_ZM);
    this.tests = tests;
    this.vars = vars;
    this.expr = expr;
  }

  @Override
  public Catch compile(final CompileContext cc) {
    try {
      expr = expr.compile(cc);
    } catch(final QueryException ex) {
      expr = cc.error(ex, expr);
    }
    return optimize(cc);
  }

  @Override
  public Catch optimize(final CompileContext cc) {
    return (Catch) adoptType(expr);
  }

  /**
   * Returns the value of the caught expression.
   * @param qc query context
   * @param ex caught exception
   * @return resulting item
   * @throws QueryException query exception
   */
  Value value(final QueryContext qc, final QueryException ex) throws QueryException {
    int i = 0;
    for(final Value value : values(ex)) qc.set(vars[i++], value);
    return expr.value(qc);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final int vl = QNAMES.length;
    final Var[] vrs = new Var[vl];
    for(int v = 0; v < vl; v++) vrs[v] = cc.vs().addNew(QNAMES[v], TYPES[v], cc.qc, info);
    final int val = vars.length;
    for(int v = 0; v < val; v++) vm.put(vars[v].id, vrs[v]);
    return copyType(new Catch(info, expr.copy(cc, vm), vrs, new ArrayList<>(tests)));
  }

  @Override
  public Catch inline(final InlineContext ic) {
    try {
      final Expr inlined = expr.inline(ic);
      if(inlined == null) return null;
      expr = inlined;
    } catch(final QueryException ex) {
      expr = ic.cc.error(ex, expr);
    }
    return this;
  }

  /**
   * Returns the catch expression with inlined exception values.
   * @param ex caught exception
   * @param cc compilation context
   * @return expression
   * @throws QueryException query exception
   */
  Expr inline(final QueryException ex, final CompileContext cc) throws QueryException {
    if(expr instanceof Value) return expr;

    Expr inlined = expr;
    int v = 0;
    for(final Value value : values(ex)) {
      inlined = new InlineContext(vars[v++], value, cc).inline(inlined);
    }
    return inlined;
  }

  /**
   * Returns variables for a new catch expression.
   * @param qc query context
   * @param info input info
   * @return variables
   */
  public static Var[] vars(final QueryContext qc, final InputInfo info) {
    final int vl = QNAMES.length;
    final Var[] vs = new Var[vl];
    for(int v = 0; v < vl; v++) {
      vs[v] = new Var(QNAMES[v], TYPES[v], qc, info);
    }
    return vs;
  }

  /**
   * Returns all error values.
   * @param ex caught exception
   * @return values
   * @throws QueryException query exception
   */
  private static Value[] values(final QueryException ex) throws QueryException {
    final Str stack = ex.stackTrace();
    final Value[] values = {
      ex.qname(),
      Str.get(ex.getLocalizedMessage()),
      ex.value() != null ? ex.value() : Empty.VALUE,
      ex.file() != null ? Str.get(ex.file()) : Empty.VALUE,
      ex.line() != 0 ? Int.get(ex.line()) : Empty.VALUE,
      ex.column() != 0 ? Int.get(ex.column()) : Empty.VALUE,
      stack,
      stack,
      null
    };

    // add all values to map
    final MapBuilder mb = new MapBuilder();
    final int nl = NAMES.length - 1;
    for(int n = 0; n < nl; n++) {
      if(!values[n].isEmpty()) mb.put(STRINGS[n], values[n]);
    }
    values[nl] = mb.map();
    return values;
  }

  /**
   * Returns a map with all catch values.
   * @param ex caught exception
   * @return values
   * @throws QueryException query exception
   */
  public static XQMap map(final QueryException ex) throws QueryException {
    final Value[] values = values(ex);
    final int vl = values.length;
    final MapBuilder map = new MapBuilder(vl);
    for(int v = 0; v < vl; v++) map.put(STRINGS[v], values[v]);
    return map.map();
  }

  /**
   * Removes redundant tests.
   * @param list current tests
   * @param cc compilation context
   * @return if catch clause contains relevant tests
   */
  boolean simplify(final ArrayList<Test> list, final CompileContext cc) {
    // check if all errors are already caught
    if(list.contains(KindTest.ELEMENT)) {
      cc.info(OPTSIMPLE_X_X, (Supplier<?>) this::description, "*");
      return false;
    }

    // drop remaining tests in favor or wildcard test
    if(tests.contains(KindTest.ELEMENT) && tests.size() != 1) {
      tests.clear();
      tests.add(KindTest.ELEMENT);
      cc.info(OPTSIMPLE_X_X, (Supplier<?>) this::description, "*");
    }

    // remove redundant tests
    final Iterator<Test> iter = tests.iterator();
    while(iter.hasNext()) {
      final Test test = iter.next();
      if(list.contains(test)) {
        cc.info(OPTREMOVE_X_X, test, (Supplier<?>) this::description);
        iter.remove();
      } else {
        list.add(test);
      }
    }
    return !tests.isEmpty();
  }

  /**
   * Checks if all errors are caught by this cause.
   * @return result of check
   */
  boolean global() {
    return tests.size() == 1 && tests.get(0) instanceof KindTest;
  }

  /**
   * Checks if one of the specified errors match the thrown error.
   * @param ex caught exception
   * @return result of check
   */
  boolean matches(final QueryException ex) {
    final QNm name = ex.qname();
    for(final Test test : tests) {
      if(test instanceof KindTest || ((NameTest) test).matches(name)) return true;
    }
    return false;
  }

  /**
   * Creates a QName with the specified name.
   * @param name name
   * @return QName
   */
  private static QNm qname(final String name) {
    return new QNm(ERR_PREFIX, name, ERROR_URI);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    for(final Var var : vars) {
      if(!visitor.declared(var)) return false;
    }
    return visitAll(visitor, expr);
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Catch)) return false;
    final Catch ctch = (Catch) obj;
    return Array.equals(vars, ctch.vars) && tests.equals(ctch.tests) && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(CATCH);
    int c = 0;
    for(final Test test : tests) {
      if(c++ > 0) qs.token('|');
      qs.token(test.toString(false));
    }
    qs.brace(expr);
  }
}
