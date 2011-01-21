package org.atlasapi.remotesite.bbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import junit.framework.TestCase;

import org.atlasapi.media.entity.MediaType;

public class BbcMasterbrandContentTypeMapTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testLookup() {
		assertThat(BbcMasterbrandContentTypeMap.lookup("/bbcone#service").valueOrNull(), is(equalTo(MediaType.VIDEO)));
		assertThat(BbcMasterbrandContentTypeMap.lookup("/radio4#service").valueOrNull(), is(equalTo(MediaType.AUDIO)));
		assertThat(BbcMasterbrandContentTypeMap.lookup("/notareal#service").valueOrNull(), is(nullValue()));
	}

}
