package org.basex.query.expr.path;

import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * XPath axes.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public enum Axis {
  // ...order is important here for parsing the Query;
  // axes with longer names are parsed first

  /** Ancestor-or-self axis. */
  ANCESTOR_OR_SELF("ancestor-or-self", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.ancestorIter(true);
    }
  },

  /** Ancestor axis. */
  ANCESTOR("ancestor", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.ancestorIter(false);
    }
  },

  /** Attribute axis. */
  ATTRIBUTE("attribute", true) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.attributeIter();
    }
  },

  /** Child axis. */
  CHILD("child", true) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.childIter();
    }
  },

  /** Descendant-or-self axis. */
  DESCENDANT_OR_SELF("descendant-or-self", true) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.descendantIter(true);
    }
  },

  /** Descendant axis. */
  DESCENDANT("descendant", true) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.descendantIter(false);
    }
  },

  /** Following-sibling-or-self axis. */
  FOLLOWING_SIBLING_OR_SELF("following-sibling-or-self", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.followingSiblingIter(true);
    }
  },

  /** Following-sibling axis. */
  FOLLOWING_SIBLING("following-sibling", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.followingSiblingIter(false);
    }
  },

  /** Following axis. */
  FOLLOWING_OR_SELF("following-or-self", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.followingIter(true);
    }
  },

  /** Following axis. */
  FOLLOWING("following", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.followingIter(false);
    }
  },

  /** Parent axis. */
  PARENT("parent", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.parentIter();
    }
  },

  /** Preceding-sibling axis. */
  PRECEDING_SIBLING_OR_SELF("preceding-sibling-or-self", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.precedingSiblingIter(true);
    }
  },

  /** Preceding-sibling axis. */
  PRECEDING_SIBLING("preceding-sibling", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.precedingSiblingIter(false);
    }
  },

  /** Preceding axis. */
  PRECEDING_OR_SELF("preceding-or-self", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.precedingIter(true);
    }
  },

  /** Preceding axis. */
  PRECEDING("preceding", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.precedingIter(false);
    }
  },

  /** Step axis. */
  SELF("self", true) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.selfIter();
    }
  };

  /** Cached enums (faster). */
  public static final Axis[] VALUES = values();
  /** Name of axis. */
  public final String name;
  /** Downward axis. */
  public final boolean down;

  /**
   * Constructor.
   * @param name axis string
   * @param down downward axis
   */
  Axis(final String name, final boolean down) {
    this.name = name;
    this.down = down;
  }

  /**
   * Returns a node iterator.
   * @param n input node
   * @return node iterator
   */
  abstract BasicNodeIter iter(ANode n);

  @Override
  public String toString() {
    return name;
  }

  /**
   * Inverts the axis.
   * @return inverted axis
   */
  public final Axis invert() {
    switch(this) {
      case ANCESTOR:                   return DESCENDANT;
      case ANCESTOR_OR_SELF:           return DESCENDANT_OR_SELF;
      case ATTRIBUTE:
      case CHILD:                      return PARENT;
      case DESCENDANT:                 return ANCESTOR;
      case DESCENDANT_OR_SELF:         return ANCESTOR_OR_SELF;
      case FOLLOWING_SIBLING:          return PRECEDING_SIBLING;
      case FOLLOWING_SIBLING_OR_SELF:  return PRECEDING_SIBLING_OR_SELF;
      case FOLLOWING:                  return PRECEDING;
      case FOLLOWING_OR_SELF:          return PRECEDING_OR_SELF;
      case PARENT:                     return CHILD;
      case PRECEDING_SIBLING:          return FOLLOWING_SIBLING;
      case PRECEDING_SIBLING_OR_SELF:  return FOLLOWING_SIBLING_OR_SELF;
      case PRECEDING:                  return FOLLOWING;
      case PRECEDING_OR_SELF:          return FOLLOWING_OR_SELF;
      case SELF:                       return SELF;
      default:                         throw Util.notExpected();
    }
  }
}
