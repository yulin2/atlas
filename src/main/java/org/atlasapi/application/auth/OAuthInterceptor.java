package org.atlasapi.application.auth;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.output.ErrorResultWriter;
import org.atlasapi.output.ErrorSummary;
import org.atlasapi.output.NotAuthorizedException;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.output.ResponseWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.social.model.UserRef;


public class OAuthInterceptor extends HandlerInterceptorAdapter {
    private final ResponseWriterFactory writerResolver = new ResponseWriterFactory();
    private final UserFetcher userFetcher;
    private Set<String> exceptions = ImmutableSet.of();
    private Set<String> urlsToProtect = ImmutableSet.of();
    private static final Logger log = LoggerFactory.getLogger(OAuthInterceptor.class);
    
    
    public OAuthInterceptor(UserFetcher userFetcher) {
        this.userFetcher = userFetcher;
    }
    
    public void setUrlsToProtect(Set<String> urlsToProtect) {
        this.urlsToProtect = urlsToProtect;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        Optional<UserRef> user = userFetcher.userFor(request);
        if (authenticationIsRequired(request) && !user.isPresent()) {
            ResponseWriter writer = writerResolver.writerFor(request, response);
            ErrorSummary summary = ErrorSummary.forException(new NotAuthorizedException());
            new ErrorResultWriter().write(summary, writer, request, response);
            return false;
        }
        return true;
    }
    
    private boolean authenticationIsRequired(final HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        if (urlsToProtect.isEmpty()) {
            log.info("No matching auth-required map for request method: " + request.getMethod());
            return false; // given we are aren't protecting things that are really private.
        }

        for (String uri : urlsToProtect) {
            if (requestUri.startsWith(uri)) {
                return !exceptions.contains(requestUri);
            }
        }

        return false;
    }
}
