package org.atlasapi.remotesite.rovi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;


public class ActionTypeTest {

    @Test
    public void testActionType() {
        ActionType actionType = ActionType.fromRoviType("Ins");
        assertThat(actionType, equalTo(ActionType.INSERT));
        
        actionType = ActionType.fromRoviType("UPD");
        assertThat(actionType, equalTo(ActionType.UPDATE));
    }

    @Test(expected = RuntimeException.class)
    public void testNotExistentActionType() {
        ActionType.fromRoviType("Xxx");
    }
    
}
