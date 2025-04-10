package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnFalse extends StandardFunc {
  // will always be pre-evaluated
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) {
    return Bln.FALSE;
  }
}
