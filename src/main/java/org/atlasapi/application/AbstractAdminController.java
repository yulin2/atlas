package org.atlasapi.application;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.auth.UserFetcher;
import org.atlasapi.output.ErrorResultWriter;
import org.atlasapi.output.ErrorSummary;
import org.atlasapi.output.NotAuthorizedException;
import org.atlasapi.output.ResponseWriter;

import com.google.common.base.Optional;
import com.metabroadcast.common.social.model.UserRef;


public class AbstractAdminController {

    private final UserFetcher userFetcher;
    
    public AbstractAdminController(UserFetcher userFetcher) {
        this.userFetcher = userFetcher;
    }

    public void sendError(HttpServletRequest request,
            HttpServletResponse response,
            ResponseWriter writer,
            Exception e) throws IOException {
        ErrorSummary summary = ErrorSummary.forException(e);
        new ErrorResultWriter().write(summary, writer, request, response);
    }
    
    protected void checkAccess(HttpServletRequest request) throws NotAuthorizedException {
        Optional<UserRef> user = userFetcher.userFor(request);
        if (!user.isPresent()) {
            throw new NotAuthorizedException();
        }   
    }
}
