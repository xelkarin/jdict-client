/*
 * Created:  Sun 10 Mar 2013 05:24:43 PM PDT
 * Modified: Fri 25 Nov 2016 04:08:40 PM PST
 * Copyright (C) 2016 Robert Gill <locke@sdf.lonestar.org>
 *
 * This file is part of JDictClient.
 *
 * JDictClient is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JDictClient is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with JDictClient.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.lonestar.sdf.locke.libs.dict;

/**
 * A DictItem is a simple key, value pair.
 *
 * It is used for data returned by the DICT protocol in the format of:<br />
 *
 * <pre>
 * {@code
 * key "value"
 * }
 * </pre>
 *
 * This includes data returned by SHOW DATABASES, SHOW STRATEGIES, and MATCH.
 *
 * @author Robert Gill &lt;locke@sdf.lonestar.org&gt;
 *
 */
public class DictItem extends Object
{
  /** Key used to identify the item */
  private String key;

  /** Value of the item (often an item description) */
  private String value;

  /**
   * Construct a new DictItem.
   *
   * @param key   the key identifying the item.
   * @param value the value of the item (often an item description).
   */
  public DictItem(String key, String value)
  {
    super();
    this.key = key;
    this.value = value;
  }

  /**
   * Get the item key.
   *
   */
  public String getKey()
  {
    return key;
  }

  /**
   * Get the item value.
   *
   */
  public String getValue()
  {
    return value;
  }

  /**
   * Creates and returns a copy of this object.
   *
   */
  @Override
  public DictItem clone()
  {
    DictItem item = new DictItem(
      getKey(),
      getValue()
    );

    return item;
  }

  /**
   * Returns a string representation of the object.
   *
   */
  @Override
  public String toString()
  {
    return key + " \"" + value + '"';
  }
}
