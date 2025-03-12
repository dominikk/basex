package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Unmodifiable hash map implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQHashMap extends XQMap {
  /** Hash map. */
  final ItemObjectMap<Value> map;
  /** Cached immutable variant, for updates. */
  private XQMap trie;

  /**
   * Constructor.
   * @param map hash map
   */
  XQHashMap(final ItemObjectMap<Value> map) {
    this.map = map;
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  Value getInternal(final Item key) throws QueryException {
    return map.get(key);
  }

  @Override
  XQMap putInternal(final Item key, final Value value) throws QueryException {
    return trie().putInternal(key, value);
  }

  @Override
  public XQMap removeInternal(final Item key) throws QueryException {
    return trie().removeInternal(key);
  }

  @Override
  public void forEach(final QueryBiConsumer<Item, Value> func) throws QueryException {
    final long is = structSize();
    for(int i = 1; i <= is; i++) func.accept(keyInternal(i), valueInternal(i));
  }

  @Override
  public boolean test(final QueryBiPredicate<Item, Value> func) throws QueryException {
    final long is = structSize();
    for(int i = 1; i <= is; i++) {
      if(!func.test(keyInternal(i), valueInternal(i))) return false;
    }
    return true;
  }

  @Override
  public BasicIter<Item> keys() {
    return new BasicIter<>(structSize()) {
      @Override
      public Item get(final long i) {
        return keyInternal((int) i + 1);
      }
      @Override
      public Value value(final QueryContext qc, final Expr expr) {
        return ItemSeq.get(keysInternal(), (int) size, ((MapType) type).keyType);
      }
    };
  }

  /**
   * Returns all keys.
   * @return key
   */
  private Item[] keysInternal() {
    return map.keys();
  }

  /**
   * Returns the key at the specified position.
   * @param pos position (starting with {@code 1})
   * @return key
   */
  private Item keyInternal(final int pos) {
    return map.key(pos);
  }

  /**
   * Returns the value at the specified position.
   * @param pos position (starting with {@code 1})
   * @return key
   */
  private Value valueInternal(final int pos) {
    return map.value(pos);
  }

  /**
   * Transforms the map to an immutable representation.
   * @return map
   * @throws QueryException query exception
   */
  private XQMap trie() throws QueryException {
    if(trie == null) {
      XQMap mp = XQMap.empty();
      final long is = structSize();
      for(int i = 1; i <= is; i++) {
        mp = mp.put(keyInternal(i), valueInternal(i));
      }
      trie = mp;
    }
    return trie;
  }
}
