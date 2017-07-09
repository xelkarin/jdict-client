/*
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
package org.lonestar.sdf.locke.libs.jdictclient;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Robert Gill &lt;locke@sdf.lonestar.org&gt;
 */
public class DictBannerTest {
    private final String BANNER = "220 dictd 1.12 <auth.mime> <100@dictd.org>";
    private final String INVALID = "220 invalid banner";

    /**
     * Test method for {@link org.lonestar.sdf.locke.libs.jdictclient.DictBanner#DictBanner(java.lang.String)}.
     */
    @Test
    public void testDictBanner() {
        DictBanner banner = new DictBanner(BANNER);
        assertEquals("dictd 1.12", banner.message);
        assertEquals("<100@dictd.org>", banner.connectionId);
        assertEquals(2, banner.capabilities.size());
        assertEquals(true, banner.capabilities.contains("auth"));
        assertEquals(true, banner.capabilities.contains("mime"));
    }

    /**
     * Test method for {@link org.lonestar.sdf.locke.libs.jdictclient.DictBanner#parse(java.lang.String)}.
     */
    @Test
    public void testParse() {
        DictBanner banner = DictBanner.parse(BANNER);
        assertNotNull(banner);
    }

    /**
     * Test parsing an invalid banner string.
     * <p>
     * Test method for {@link org.lonestar.sdf.locke.libs.jdictclient.DictBanner#parse(java.lang.String)}
     * while passing an invalid banner string.
     */
    @Test
    public void testInvalidBanner() {
        DictBanner banner = DictBanner.parse(INVALID);
        assertNull(banner);
    }

    /**
     * Test method for {@link org.lonestar.sdf.locke.libs.jdictclient.DictBanner#toString()}.
     */
    @Test
    public void testBannerToString() {
        DictBanner banner = DictBanner.parse(BANNER);
        assertEquals(BANNER, banner.toString());
    }
}
