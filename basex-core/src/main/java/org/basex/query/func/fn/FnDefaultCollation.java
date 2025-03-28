package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnDefaultCollation extends StandardFunc {
  @Override
  public Uri item(final QueryContext qc, final InputInfo ii) {
    final Collation coll = sc().collation;
    return Uri.get(coll == null ? QueryText.COLLATION_URI : coll.uri());
  }
}
