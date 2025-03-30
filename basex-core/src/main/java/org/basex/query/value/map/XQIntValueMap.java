package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Unmodifiable hash map implementation for integers and values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQIntValueMap extends XQHashMap {
  /** Map type. */
  private static final MapType TYPE = MapType.get(AtomType.INTEGER, SeqType.ITEM_ZM);
  /** Hash map. */
  private final IntObjectMap<Value> map;
  /** Initial capacity. */
  private final int capacity;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQIntValueMap(final int capacity) {
    super(TYPE);
    map = new IntObjectMap<>(capacity);
    this.capacity = capacity;
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  public Value getOrNull(final Item key) throws QueryException {
    if(key instanceof ANum) {
      final double d = key.dbl(null);
      final int v = (int) d;
      if(d == v) {
        final int i = map.index(v);
        if(i != 0) return valueAt(i - 1);
      }
    }
    return null;
  }

  @Override
  Value keysInternal() {
    final long is = structSize();
    final LongList list = new LongList(is);
    for(int i = 1; i <= is; i++) list.add(map.key(i));
    return IntSeq.get(list.finish());
  }

  @Override
  public Item keyAt(final int pos) {
    return Int.get(map.key(pos + 1));
  }

  @Override
  public Value valueAt(final int pos) {
    return map.value(pos + 1);
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    final int k = toInt(key);
    if(k != Integer.MIN_VALUE) {
      map.put(k, value);
      return this;
    }
    return new XQItemValueMap(capacity).build(this).build(key, value);
  }
}
