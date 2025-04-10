package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbPath extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ANode node, parent = toNode(arg(0), qc);
    do {
      node = parent;
      parent = node.parent();
    } while(parent != null);

    final DBNode dbnode = toDBNode(node, false);
    return dbnode.kind() == Data.DOC ? Str.get(dbnode.data().text(dbnode.pre(), true)) : Str.EMPTY;
  }
}
