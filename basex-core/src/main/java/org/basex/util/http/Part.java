package org.basex.util.http;

import java.util.*;

import org.basex.query.util.list.*;

/**
 * Container for parsed data from "part" element.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
public final class Part {
  /** Part headers. */
  public final Map<String, String> headers = new HashMap<>();
  /** Attributes of part body. */
  public final Map<String, String> attributes = new HashMap<>();
  /** Content of part body. */
  public final ItemList contents = new ItemList();
}
