package org.atlasapi.application.auth;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.users.User;
import org.atlasapi.output.ErrorResultWriter;
import org.atlasapi.output.ErrorSummary;
import org.atlasapi.output.NotAuthorizedException;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.output.ResponseWriterFactory;
import org.atlasapi.output.UserProfileIncompleteException;
import org.elasticsearch.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.ids.NumberToShortStringCodec;

public class OAuthInterceptor extends HandlerInterceptorAdapter {
    private final ResponseWriterFactory writerResolver = new ResponseWriterFactory();
    private final UserFetcher userFetcher;
    private final NumberToShortStringCodec idCodec;
    private Set<String> exceptions = ImmutableSet.of();
    private Set<String> urlsToProtect = ImmutableSet.of();
    private Set<String> urlsNotNeedingCompleteProfile = ImmutableSet.of();
    private static final Logger log = LoggerFactory.getLogger(OAuthInterceptor.class);
    
    
    public OAuthInterceptor(UserFetcher userFetcher, NumberToShortStringCodec idCodec) {
        this.userFetcher = userFetcher;
        this.idCodec = idCodec;
    }
    
    public void setUrlsToProtect(Set<String> urlsToProtect) {
        this.urlsToProtect = urlsToProtect;
    }
    
    public void setUrlsNotRequiringCompleteProfile(Set<String> urls) {
        this.urlsNotNeedingCompleteProfile = urls;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        Optional<User> user = userFetcher.userFor(request);
        if (authenticationIsRequired(request) && !user.isPresent()) {
            ResponseWriter writer = writerResolver.writerFor(request, response);
            ErrorSummary summary = ErrorSummary.forException(new NotAuthorizedException());
            new ErrorResultWriter().write(summary, writer, request, response);
            return false;
        }
        if (user.isPresent() && !user.get().isProfileComplete() && requiresCompleteProfile(request, user.get())) {
            ResponseWriter writer = writerResolver.writerFor(request, response);
            ErrorSummary summary = ErrorSummary.forException(new UserProfileIncompleteException());
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
    
    private boolean requiresCompleteProfile(HttpServletRequest request, User user) {
        String requestUri = uriWithoutExtension(request.getRequestURI());
        String uid = idCodec.encode(user.getId().toBigInteger());

        if (urlsToProtect.isEmpty()) {
            log.info("No matching auth-required map for request method: " + request.getMethod());
            return false; // given we are aren't protecting things that are really private.
        }
        
        // Do substitutions
        Set<String> completeProfileNotRequired = Sets.newHashSet();
        for (String url : urlsNotNeedingCompleteProfile) {
            completeProfileNotRequired.add(url.replace(":uid", uid));
        }
        // Make a subset by taking urls to protect and removing urls not needing auth
        for (String uri : urlsToProtect) {
            if (requestUri.startsWith(uri)) {
                return !exceptions.contains(requestUri) && !completeProfileNotRequired.contains(requestUri);
            }
        }
        return false;
    }
    
    private String uriWithoutExtension(String requestUri) {
        int suffixStart = requestUri.lastIndexOf(".");
        if (suffixStart >= 0) {
            return requestUri.substring(0, suffixStart);
        }
        return null;
    }
}
