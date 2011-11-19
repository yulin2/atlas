package org.atlasapi.remotesite.itunes.epf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.StringReader;

import org.atlasapi.remotesite.itunes.epf.ArtistTable.ArtistTableRow;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

public class ArtistTableTest extends MockObjectTestCase {

    private final ArtistTableRowProcessor processor = mock(ArtistTableRowProcessor.class);
    
    public void testProcessingRows() throws IOException {
        
        final String name = "Zolt‡n Kocsis";
        StringReader reader = new StringReader(Joiner.on((char)1).join(ImmutableList.of(
                "1320832802897","341126",name,"1","http://itunes.apple.com/artist/zoltan-kocsis/id341126?uo=5","1"+((char)2)+"\n"
        )));
        
        ArtistTable table = new ArtistTable(reader);
        
        checking(new Expectations(){{
            one(processor).process(with(rowWithName(name)));
        }});
        
        int processed = table.processRows(processor);
        
        assertThat(processed, is(1));
        
    }

    private TypeSafeMatcher<ArtistTableRow> rowWithName(final String name) {
        return new TypeSafeMatcher<ArtistTableRow>() {

            @Override
            public void describeTo(Description desc) {
                desc.appendValue(String.format("Row with %s", name));
            }

            @Override
            public boolean matchesSafely(ArtistTableRow row) {
                return row.getName().equals(name);
            }
        };
    }
}
