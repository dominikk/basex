package org.basex.query.value.item;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * HexBinary item ({@code xs:hexBinary}).
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class Hex extends Bin {
  /**
   * Constructor.
   * @param value bytes
   */
  public Hex(final byte[] value) {
    super(value, AtomType.HEX_BINARY);
  }

  /**
   * Constructor.
   * @param value textual representation
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public Hex(final byte[] value, final InputInfo info) throws QueryException {
    this(parse(Token.trim(value), info));
  }

  /**
   * Constructor.
   * @param bin binary data
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public Hex(final Bin bin, final InputInfo info) throws QueryException {
    this(bin.binary(info));
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeToken(data);
  }

  @Override
  public byte[] string(final InputInfo ii) throws QueryException {
    return Token.hex(binary(ii), true);
  }

  @Override
  public boolean equal(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    final byte[] bin = item instanceof Bin ? ((Bin) item).binary(ii) : parse(item, ii);
    return Token.eq(binary(ii), bin);
  }

  @Override
  public int compare(final Item item, final Collation coll, final InputInfo ii)
      throws QueryException {
    final byte[] bin = item instanceof Bin ? ((Bin) item).binary(ii) : parse(item, ii);
    return Token.compare(binary(ii), bin);
  }

  /**
   * Converts the given item to a byte array.
   * @param item item to be converted
   * @param info input info (can be {@code null})
   * @return byte array
   * @throws QueryException query exception
   */
  public static byte[] parse(final Item item, final InputInfo info) throws QueryException {
    final byte[] bytes = parse(item.string(info));
    if(bytes != null) return bytes;
    throw AtomType.HEX_BINARY.castError(item, info);
  }

  /**
   * Converts the given token into a byte array.
   * @param value value to be converted
   * @param info input info (can be {@code null})
   * @return byte array
   * @throws QueryException query exception
   */
  public static byte[] parse(final byte[] value, final InputInfo info) throws QueryException {
    final byte[] bytes = parse(value);
    if(bytes != null) return bytes;
    throw AtomType.HEX_BINARY.castError(value, info);
  }

  /**
   * Converts the input into a byte array.
   * @param data textual data
   * @return byte array, or {@code null} if input is invalid
   */
  private static byte[] parse(final byte[] data) {
    final int dl = data.length;
    if((dl & 1) != 0) return null;
    final byte[] value = new byte[dl >>> 1];
    for(int d = 0; d < dl; d += 2) {
      final int n = Token.dec(data[d], data[d + 1]);
      if(n < 0) return null;
      value[d >>> 1] = (byte) n;
    }
    return value;
  }

  @Override
  public void toString(final QueryString qs) {
    final TokenBuilder tb = new TokenBuilder().add('"');
    if(data.length > 128) {
      tb.add(Token.hex(Arrays.copyOf(data, 128), true)).add(Text.DOTS);
    } else {
      tb.add(Token.hex(data, true));
    }
    qs.token(tb.add('"').finish());
  }
}
