package org.basex.query.func.db;

import org.basex.data.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbGetPre extends DbGetId {
  @Override
  protected int pre(final int id, final Data data) {
    return id;
  }
}
