package org.basex.index.value;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Caches values and IDs for update operations.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ValueCache implements Iterable<byte[]> {
  /** Keys. */
  private final TokenSet keys = new TokenSet();
  /** IDs. */
  private final ArrayList<IntList> ids = new ArrayList<>();
  /** Positions. */
  private final ArrayList<IntList> pos;

  /**
   * Caches the text and ID for a node with specified PRE value.
   * @param pre PRE value
   * @param type index type
   * @param data data reference
   */
  public ValueCache(final int pre, final IndexType type, final Data data) {
    this(new IntList(1).add(pre), type, data);
  }

  /**
   * Caches all texts and IDs in the specified database range.
   * @param pre PRE value
   * @param size size value
   * @param type index type
   * @param data data reference
   */
  public ValueCache(final int pre, final int size, final IndexType type, final Data data) {
    this(pres(pre, size), type, data);
  }

  /**
   * Caches texts of the specified PRE values.
   * @param pres PRE values
   * @param type index type
   * @param data data reference
   */
  public ValueCache(final IntList pres, final IndexType type, final Data data) {
    pos = type == IndexType.TOKEN ? new ArrayList<>() : null;

    final IndexNames in = new IndexNames(type, data);
    final boolean text = type == IndexType.TEXT;
    final int pl = pres.size(), kind = text ? Data.TEXT : Data.ATTR;
    for(int p = 0; p < pl; p++) {
      final int pre = pres.get(p);
      if(data.kind(pre) == kind && in.contains(pre, text)) {
        if(type == IndexType.TOKEN) {
          int ps = 0;
          for(final byte[] token : distinctTokens(data.text(pre, false))) {
            addId(token, pre, ps++, data);
          }
        } else if(data.textLen(pre, text) <= data.meta.maxlen) {
          addId(data.text(pre, text), pre, 0, data);
        }
      }
    }
  }

  /**
   * Caches all texts and IDs in the specified database range.
   * @param pre PRE value
   * @param size size value
   * @return list of PRE values
   */
  private static IntList pres(final int pre, final int size) {
    final IntList pres = new IntList(size);
    final int last = pre + size;
    for(int curr = pre; curr < last; curr++) pres.add(curr);
    return pres;
  }

  /**
   * Adds a single node ID and position.
   * @param text text
   * @param pre PRE value
   * @param ps position
   * @param data data reference
   */
  private void addId(final byte[] text, final int pre, final int ps, final Data data) {
    final int i = keys.put(text) - 1;
    final boolean exists = i < ids.size();

    IntList list;
    if(exists) {
      list = ids.get(i);
    } else {
      list = new IntList(1);
      ids.add(list);
    }
    list.add(data.id(pre));

    if(pos != null) {
      if(exists) {
        list = pos.get(i);
      } else {
        list = new IntList(1);
        pos.add(list);
      }
      list.add(ps);
    }
  }

  /**
   * Returns an iterator with all keys in sorted order.
   * @return keys iterator
   */
  @Override
  public Iterator<byte[]> iterator() {
    return new TokenList(keys).sort().iterator();
  }

  /**
   * Returns the ID list for the specified key.
   * @param key key
   * @return ID list
   */
  IntList ids(final byte[] key) {
    return ids.get(keys.index(key) - 1);
  }

  /**
   * Returns the position list for the specified key.
   * @param key key
   * @return ID list or {@code null}
   */
  IntList pos(final byte[] key) {
    return pos != null ? pos.get(keys.index(key) - 1) : null;
  }
}
