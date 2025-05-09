package org.basex.query.func.crypto;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class CryptoValidateSignature extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toNode(arg(0), qc);
    return new DigitalSignature(info).validateSignature(node);
  }
}
