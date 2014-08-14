package org.atlasapi.remotesite.channel4.pmlsd;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class C4AtomApiTest {

    @Test
    public void testConvertsPcOdToBrandIosUri() {
        assertThat(
                C4AtomApi.iOsUriFromPcUri("http://www.channel4.com/programmes/come-dine-with-me/4od#3584274"),
                is("c4-4od://ios.channel4.com/pmlsd/come-dine-with-me/4od.atom"));
    }
}
