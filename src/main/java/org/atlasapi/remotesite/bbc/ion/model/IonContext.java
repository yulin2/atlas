package org.atlasapi.remotesite.bbc.ion.model;

import java.util.List;

public class IonContext {

    public static class IonBikiniState {
        private String area;
        private String state;

        public String getArea() {
            return area;
        }

        public String getState() {
            return state;
        }

    }

    private Boolean inHd;
    private String domain;
    private Boolean isSigned;
    private String locale;
    private Boolean isDubbedAudio;
    private List<IonBikiniState> bikiniStates;
    private String recipe;

    public Boolean isInHd() {
        return inHd;
    }

    public String getDomain() {
        return domain;
    }

    public Boolean isSigned() {
        return isSigned;
    }

    public String getLocale() {
        return locale;
    }

    public Boolean isDubbedAudio() {
        return isDubbedAudio;
    }

    public List<IonBikiniState> getBikiniStates() {
        return bikiniStates;
    }

    public String getRecipe() {
        return recipe;
    }

}
