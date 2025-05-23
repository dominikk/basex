package org.basex.gui.listener;

import java.awt.event.*;

/**
 * Listener interface for released keys.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public interface KeyPressedListener extends KeyListener {
  @Override
  default void keyTyped(final KeyEvent e) { }

  @Override
  default void keyReleased(final KeyEvent e) { }
}
