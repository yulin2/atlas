package org.uriplay.remotesite.vimeo;

import junit.framework.TestCase;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class VimeoOembedGraphExtractorTest extends TestCase {

	public void testGeneratesCurieFromVimeoUri() throws Exception {
		
		assertEquals("vim:8211405", new VimeoOembedGraphExtractor().curieFor("http://vimeo.com/8211405"));
		
	}
}
