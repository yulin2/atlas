package org.atlasapi.output;

import org.atlasapi.query.common.QueryExecutionException;


public class InsufficientPrivilegeException extends QueryExecutionException {
    private static final long serialVersionUID = 4190336690606433734L;
    
    public static final InsufficientPrivilegeException CANNOT_CHANGE_USER_ROLE 
              = new InsufficientPrivilegeException("You do not have permission to change the user role");
    
    private InsufficientPrivilegeException(String message) {
        super(message);
    }

}
