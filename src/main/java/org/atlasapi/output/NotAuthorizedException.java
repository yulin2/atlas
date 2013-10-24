package org.atlasapi.output;


public class NotAuthorizedException extends Exception {
    private static final long serialVersionUID = 4567624251087993406L;

    @Override
    public String getMessage() {
        return "Access denied";
    }

}
