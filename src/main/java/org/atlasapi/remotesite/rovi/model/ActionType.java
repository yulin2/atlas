package org.atlasapi.remotesite.rovi.model;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;


public enum ActionType {

    INSERT("Ins"),
    UPDATE("Upd"),
    DELETE("Del");
    
    private final String roviType;
    
    private ActionType(String roviType) {
        this.roviType = roviType;
    }
    
    public static ActionType fromRoviType(final String roviType) {
        checkNotNull(roviType);
        
        return Iterables.find(asList(ActionType.values()), new Predicate<ActionType>() {
            @Override
            public boolean apply(ActionType actionType) {
                return actionType.roviType.equalsIgnoreCase(roviType);
            }
        });
    }

    public String getRoviType() {
        return roviType;
    }
    
}
