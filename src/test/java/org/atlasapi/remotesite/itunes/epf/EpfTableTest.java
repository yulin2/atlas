package org.atlasapi.remotesite.itunes.epf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.atlasapi.remotesite.itunes.epf.model.EpfArtist;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;

public class EpfTableTest extends MockObjectTestCase {

    @SuppressWarnings("unchecked")
    private final EpfTableRowProcessor<EpfArtist,Integer> processor = mock(EpfTableRowProcessor.class);
    
    public void testProcessingRows() throws IOException {
        
        final String name = "Zoltán Kocsis";
        final String rowString = Joiner.on((char)1).join(ImmutableList.of(
                "1320832802897","341126",name,"1","http://itunes.apple.com/artist/zoltan-kocsis/id341126?uo=5","1"+((char)2)+"\n"
        ));
        
        EpfTable<EpfArtist> table = new EpfTable<EpfArtist>(CharStreams.newReaderSupplier(rowString), EpfArtist.FROM_ROW_PARTS);
        
        checking(new Expectations(){{
            one(processor).process(with(rowWithName(name))); will(returnValue(Boolean.TRUE));
            one(processor).getResult(); will(returnValue(5));
        }});
        
        int processed = table.processRows(processor);
        
        assertThat(processed, is(5));
        
    }

    private TypeSafeMatcher<EpfArtist> rowWithName(final String name) {
        return new TypeSafeMatcher<EpfArtist>() {

            @Override
            public void describeTo(Description desc) {
                desc.appendValue(String.format("Row with %s", name));
            }

            @Override
            public boolean matchesSafely(EpfArtist row) {
                return row.get(EpfArtist.NAME).equals(name);
            }
        };
    }
}
