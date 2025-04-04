package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author Christian Gruen
 * @author BaseX Team, BSD License
 */
public final class FnUnparsedTextLines extends FnUnparsedTextAvailable {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Item text = unparsedText(qc, false, true, arg(1));
    return text.isEmpty() ? Empty.ITER : new LinesIter(text.string(info));
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item text = unparsedText(qc, false, true, arg(1));
    if(text.isEmpty()) return Empty.VALUE;

    try(NewlineInput ni = new NewlineInput(text.string(info))) {
      final TokenList tl = new TokenList();
      final TokenBuilder tb = new TokenBuilder();
      while(ni.readLine(tb)) {
        qc.checkStop();
        tl.add(tb.toArray());
      }
      return StrSeq.get(tl);
    } catch(final IOException ex) {
      throw FILE_IO_ERROR_X.get(info, ex);
    }
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr href = arg(0);
    return href.seqType().zero() ? href : super.opt(cc);
  }

  /**
   * Line iterator.
   * @author Christian Gruen
   * @author BaseX Team, BSD License
   */
  private static final class LinesIter extends Iter {
    /** Token builder. */
    private final TokenBuilder tb = new TokenBuilder();
    /** Input stream. */
    private final NewlineInput nli;

    /**
     * Constructor.
     * @param contents file contents
     */
    private LinesIter(final byte[] contents) {
      try {
        nli = new NewlineInput(contents);
      } catch(final IOException ex) {
        // input has already been converted to the correct encoding
        throw Util.notExpected(ex);
      }
    }

    @Override
    public Str next() {
      try {
        return nli.readLine(tb) ? Str.get(tb.toArray()) : null;
      } catch(final IOException ex) {
        // input has already been converted to the correct encoding
        throw Util.notExpected(ex);
      }
    }
  }
}
