package org.basex.query.func.string;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringSoundex extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final AStr value = toStr(arg(0), qc);

    final int[] encoded = Soundex.encode(value.codepoints(info));
    final TokenBuilder tb = new TokenBuilder(encoded.length);
    for(final int cp : encoded) tb.add(cp);
    return Str.get(tb.finish());
  }
}
