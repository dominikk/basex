package org.basex.query.func.convert;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ConvertStringToBase64 extends ConvertFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return B64.get(stringToBinary(qc));
  }
}
