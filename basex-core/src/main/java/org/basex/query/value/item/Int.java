package org.basex.query.value.item;

import java.io.*;
import java.math.*;

import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Integer item ({@code xs:int}, {@code xs:integer}, {@code xs:short}, etc.).
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class Int extends ANum {
  /** Maximum values. */
  public static final Int MAX;
  /** Value 0. */
  public static final Int ZERO;
  /** Value 1. */
  public static final Int ONE;

  /** Constant values. */
  private static final Int[] INTSS;
  /** Integer value. */
  private final long value;

  // caches the first 128 integers
  static {
    final int nl = 128;
    INTSS = new Int[nl];
    for(int n = 0; n < nl; n++) INTSS[n] = new Int(n);
    MAX = get(Long.MAX_VALUE);
    ZERO = INTSS[0];
    ONE = INTSS[1];
  }

  /**
   * Constructor.
   * @param value value
   */
  private Int(final long value) {
    this(value, AtomType.INTEGER);
  }

  /**
   * Constructor.
   * @param value value
   * @param type item type
   */
  public Int(final long value, final Type type) {
    super(type);
    this.value = value;
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @return instance
   */
  public static Int get(final long value) {
    return value >= 0 && value < INTSS.length ? INTSS[(int) value] : new Int(value);
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @param type item type
   * @return instance
   */
  public static Int get(final long value, final Type type) {
    return type == AtomType.INTEGER ? get(value) : new Int(value, type);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeLong(value);
  }

  @Override
  public byte[] string() {
    return value == 0 ? Token.ZERO : Token.token(value);
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return value != 0;
  }

  @Override
  public long itr() {
    return value;
  }

  @Override
  public float flt() {
    return value;
  }

  @Override
  public double dbl() {
    return value;
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final boolean pred) {
    return pred ? value == qc.focus.pos : bool(ii);
  }

  @Override
  public BigDecimal dec(final InputInfo ii) {
    return BigDecimal.valueOf(value);
  }

  @Override
  public Int abs() {
    return value < 0 ? get(-value) : this;
  }

  @Override
  public Int ceiling() {
    return this;
  }

  @Override
  public Int floor() {
    return this;
  }

  @Override
  public Int round(final int scale, final boolean even) {
    final long v = rnd(scale, even);
    return v == value ? this : get(v);
  }

  /**
   * Returns a rounded value.
   * @param s scale
   * @param e half-to-even flag
   * @return result
   */
  private long rnd(final int s, final boolean e) {
    long v = value;
    if(s >= 0 || v == 0) return v;
    if(s < -15) return Dec.get(new BigDecimal(v)).round(s, e).itr();

    long f = 1;
    final int c = -s;
    for(long i = 0; i < c; i++) f = (f << 3) + (f << 1);
    final boolean n = v < 0;
    final long a = n ? -v : v, m = a % f, d = m << 1;
    v = a - m;
    if(e ? d > f || d == f && v % (f << 1) != 0 : n ? d > f : d >= f) v += f;
    return n ? -v : v;
  }

  @Override
  public boolean equal(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return item instanceof Int ? value == ((Int) item).value :
           item instanceof Dec ? item.equal(this, coll, sc, ii) :
           value == item.dbl(ii);
  }

  @Override
  public int compare(final Item item, final Collation coll, final InputInfo ii)
      throws QueryException {
    if(item instanceof Int) return Long.compare(value, ((Int) item).value);
    if(item.type == AtomType.DECIMAL) return -item.compare(this, coll, ii);
    final double d = item.dbl(ii);
    return Double.isNaN(d) ? UNDEF : value < d ? -1 : value > d ? 1 : 0;
  }

  @Override
  public Object toJava() {
    switch((AtomType) type) {
      case BYTE:
        return (byte) value;
      case SHORT:
      case UNSIGNED_BYTE:
        return (short) value;
      case UNSIGNED_SHORT:
        return (char) value;
      case INT:
        return (int) value;
      default:
        return value;
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Int)) return false;
    final Int i = (Int) obj;
    return type == i.type && value == i.value;
  }

  // STATIC METHODS ===============================================================================

  /**
   * Converts the given token to a long value.
   * @param value value to be converted
   * @param info input info (can be {@code null})
   * @return long value
   * @throws QueryException query exception
   */
  public static long parse(final byte[] value, final InputInfo info) throws QueryException {
    final long l = Token.toLong(value);
    if(l != Long.MIN_VALUE || Token.eq(Token.trim(value), Token.MIN_LONG)) return l;
    throw AtomType.INTEGER.castError(value, info);
  }
}
