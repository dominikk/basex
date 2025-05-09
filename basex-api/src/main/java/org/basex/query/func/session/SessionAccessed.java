package org.basex.query.func.session;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SessionAccessed extends SessionFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ASession session = session(qc, true);

    return session.accessed();
  }
}
