package org.basex.core.parse;

/**
   * Reads a password from a specified source (e.g., command line or GUI).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public interface PasswordReader {
  /**
   * Parses and returns a password.
   * In command line and server mode, read from stdin, on GUI command line
   * prompt using a password box.
   * @return password or empty string
   */
  String password();
}
