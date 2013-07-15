/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.remotesite.youtube;

import junit.framework.TestCase;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class YoutubeUriCanonicaliserTest extends TestCase {

    public class YoutubeUriCanonicaliserTest extends TestCase {
    }

    public void testThatTheAdapterCanExtractVideoIdFromYoutubeUri() throws Exception {
        check("http://www.youtube.com/watch?v=uOH0o2DQDco", "uOH0o2DQDco");
        check("http://www.youtube.com/watch?v=xyCNqsbVPYM", "xyCNqsbVPYM");
    }

    public void testCanGenerateCurieForUri() throws Exception {
        assertEquals("yt:uOH0o2DQDco",
                YoutubeUriCanonicaliser.curieFor("http://www.youtube.com/watch?v=uOH0o2DQDco&feature=channel"));
        assertEquals("yt:uOH0o2DQDco",
                YoutubeUriCanonicaliser.curieFor("http://www.youtube.com/watch?v=uOH0o2DQDco"));
        assertEquals("yt:xViM96yKlnE",
                YoutubeUriCanonicaliser.curieFor("http://www.youtube.com/watch?v=xViM96yKlnE"));
        assertEquals("yt:SiFvV8Ix0OI",
                YoutubeUriCanonicaliser.curieFor("http://www.youtube.com/watch?v=SiFvV8Ix0OI"));
    }

    private void check(String alternate, final String expected) {
        final String canonicalUri = "http://gdata.youtube.com/feeds/api/videos/" + expected;
        assertEquals(canonicalUri, canonicaliser.canonicalise(alternate));
    }
}