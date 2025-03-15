package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Unmodifiable hash map implementation for integers.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQIntMap extends XQHashMap {
  /** Map type. */
  private static final MapType TYPE = MapType.get(AtomType.INTEGER, SeqType.INTEGER_O);
  /** Hash map. */
  private final IntMap map;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQIntMap(final long capacity) {
    super(capacity, TYPE);
    map = new IntMap(capacity);
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  Int getInternal(final Item key) throws QueryException {
    if(key instanceof ANum) {
      final double d = key.dbl(null);
      final int i = (int) d;
      if(d == i) {
        final int id = map.id(i);
        if(id != 0) return valueInternal(id);
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
  Int keyInternal(final int pos) {
    return Int.get(map.key(pos));
  }

  @Override
  Int valueInternal(final int pos) {
    return Int.get(map.value(pos));
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    if(key.type == AtomType.INTEGER) {
      final long kl = key.itr(null);
      final int ki = (int) kl;
      if(ki == kl) {
        if(value.seqType().eq(SeqType.INTEGER_O)) {
          final long vl = ((Item) value).itr(null);
          final int vi = (int) vl;
          if(vi == vl) {
            map.put(ki, vi);
            return this;
          }
        }
        return new XQIntObjMap(capacity).build(this).build(key, value);
      }
    }
    return new XQItemObjMap(capacity).build(this).build(key, value);
  }
}
