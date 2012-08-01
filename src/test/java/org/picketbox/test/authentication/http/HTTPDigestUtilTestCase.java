/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketbox.test.authentication.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.picketbox.core.util.HTTPDigestUtil;

/**
 * Unit Test the {@link HTTPDigestUtil} class
 *
 * @author anil saldhana
 * @since July 5, 2012
 */
public class HTTPDigestUtilTestCase {

    String val = "Digest username=\"Mufasa\",realm=\"testrealm@host.com\"," + "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\","
            + "uri=\"/dir/index.html\",qop=auth,nc=00000001,cnonce=\"0a4f113b\","
            + "response=\"6629fae49393a05397450978507c4ef1\",opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";

    @Test
    public void testQuoteTokenize() throws Exception {
        String[] tokens = HTTPDigestUtil.quoteTokenize(val);
        validateTokens(tokens);
    }

    @Test
    public void testExtract() throws Exception {
        String[] tokens = HTTPDigestUtil.quoteTokenize(val);
        assertTrue(tokens.length > 0);
        validateExtractedTokens(tokens);
    }

    private void validateTokens(String[] tokens) {
        assertTrue(tokens.length > 0);
        assertEquals("Digest username=\"Mufasa\"", tokens[0]);
        assertEquals("realm=\"testrealm@host.com\"", tokens[1]);
        assertEquals("nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"", tokens[2]);
        assertEquals("uri=\"/dir/index.html\"", tokens[3]);
        assertEquals("qop=auth", tokens[4]);
        assertEquals("nc=00000001", tokens[5]);
        assertEquals("cnonce=\"0a4f113b\"", tokens[6]);
        assertEquals("response=\"6629fae49393a05397450978507c4ef1\"", tokens[7]);
        assertEquals("opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"", tokens[8]);
    }

    private void validateExtractedTokens(String[] tokens) {
        assertEquals("Mufasa", HTTPDigestUtil.userName(tokens[0]));
        assertEquals("testrealm@host.com", HTTPDigestUtil.extract(tokens[1], "realm="));
        assertEquals("dcd98b7102dd2f0e8b11d0f600bfb0c093", HTTPDigestUtil.extract(tokens[2], "nonce="));
        assertEquals("/dir/index.html", HTTPDigestUtil.extract(tokens[3], "uri="));
        assertEquals("auth", HTTPDigestUtil.extract(tokens[4], "qop="));
        assertEquals("00000001", HTTPDigestUtil.extract(tokens[5], "nc="));
        assertEquals("0a4f113b", HTTPDigestUtil.extract(tokens[6], "cnonce="));
        assertEquals("6629fae49393a05397450978507c4ef1", HTTPDigestUtil.extract(tokens[7], "response="));
        assertEquals("5ccc069c403ebaf9f0171e9517f40e41", HTTPDigestUtil.extract(tokens[8], "opaque="));
    }
}