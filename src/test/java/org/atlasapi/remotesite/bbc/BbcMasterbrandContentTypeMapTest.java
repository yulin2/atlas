package org.atlasapi.remotesite.bbc;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.atlasapi.media.entity.ContentType;

public class BbcMasterbrandContentTypeMapTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testLookup() {
		assertThat(BbcMasterbrandContentTypeMap.lookup("/bbcone#service").valueOrNull(), is(equalTo(ContentType.VIDEO)));
		assertThat(BbcMasterbrandContentTypeMap.lookup("/radio4#service").valueOrNull(), is(equalTo(ContentType.AUDIO)));
		assertThat(BbcMasterbrandContentTypeMap.lookup("/notareal#service").valueOrNull(), is(nullValue()));
	}

}
