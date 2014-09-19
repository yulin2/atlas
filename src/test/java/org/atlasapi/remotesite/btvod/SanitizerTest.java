package org.atlasapi.remotesite.btvod;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class SanitizerTest {

    @Test
    public void testSanitization() {
        assertThat(Sanitizer.sanitize("This is a title"), is("this-is-a-title"));
        assertThat(Sanitizer.sanitize("This, is a title"), is("this-is-a-title"));
        assertThat(Sanitizer.sanitize("This is a title: nice!"), is("this-is-a-title-nice"));
        assertThat(Sanitizer.sanitize("This , is a title version 2"),
                is("this-is-a-title-version-2"));
    }
    
}
