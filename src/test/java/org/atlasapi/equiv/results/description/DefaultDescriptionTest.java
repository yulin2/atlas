package org.atlasapi.equiv.results.description;

import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

public class DefaultDescriptionTest extends TestCase {

    public void testDefaultDescription() {

        DefaultDescription desc = new DefaultDescription();
        
        desc.appendText("line one");
        desc.finishStage(); //ignored.
        desc.startStage("stage one");
        desc.startStage("stage one a");
        desc.appendText("line two");
        desc.appendText("line three");
        desc.finishStage();
        desc.appendText("line four");
        desc.finishStage();
        desc.appendText("line five");
        
        assertEquals(ImmutableList.<Object>of(
                "line one",
                "stage one", ImmutableList.of(
                        "stage one a", ImmutableList.of( "line two", "line three"),
                        "line four"
                ),
                "line five"
        ), desc.parts());
        
    }

}
