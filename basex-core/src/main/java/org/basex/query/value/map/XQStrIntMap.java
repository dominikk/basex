package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Unmodifiable hash map implementation for strings and integers.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQStrIntMap extends XQHashMap {
  /** Map type. */
  private static final MapType TYPE = MapType.get(AtomType.STRING, SeqType.INTEGER_O);
  /** Hash map. */
  private final TokenIntMap map;
  /** Initial capacity. */
  private final int capacity;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQStrIntMap(final int capacity) {
    super(TYPE);
    map = new TokenIntMap(capacity);
    this.capacity = capacity;
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  public Value getOrNull(final Item key) throws QueryException {
    if(key.type.isStringOrUntyped()) {
      final int i = map.index(key.string(null));
      if(i != 0) return valueAt(i - 1);
    }
    return null;
  }

  @Override
  Value keysInternal() {
    return StrSeq.get(map.keys());
  }

  @Override
  Value itemsInternal() {
    final long is = structSize();
    final LongList list = new LongList(is);
    for(int i = 1; i <= is; i++) list.add(map.value(i));
    return IntSeq.get(list.finish());
  }

  @Override
  public Str keyAt(final int pos) {
    return Str.get(map.key(pos + 1));
  }

  @Override
  public Int valueAt(final int pos) {
    return Int.get(map.value(pos + 1));
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    final byte[] k = toString(key);
    final int v = toInt(value);
    if(k != null) {
      if(v != Integer.MIN_VALUE) {
        map.put(k, v);
        return this;
      }
      return new XQStrValueMap(capacity).build(this).build(key, value);
    }
    return new XQItemValueMap(capacity).build(this).build(key, value);
  }
}
