package org.basex.query.up.primitives.node;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.up.atomic.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Rename node primitive.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class RenameNode extends NodeUpdate {
  /** New name. */
  private final QNm name;

  /**
   * Constructor.
   * @param pre target node PRE value
   * @param data target data reference
   * @param info input info (can be {@code null})
   * @param name new QName
   */
  public RenameNode(final int pre, final Data data, final InputInfo info, final QNm name) {
    super(UpdateType.RENAMENODE, pre, data, info);
    this.name = name;
  }

  @Override
  public void prepare(final MemData memData, final QueryContext qc) { }

  @Override
  public void merge(final Update update) throws QueryException {
    throw UPMULTREN_X.get(info, node());
  }

  @Override
  public void update(final NamePool pool) {
    final DBNode node = node();
    pool.add(name, (NodeType) node.type);
    pool.remove(node);
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + node() + ", " + name + ']';
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public void addAtomics(final AtomicUpdateCache auc) {
    auc.addRename(pre, name.string(), name.uri());
  }
}
