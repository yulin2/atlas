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
import org.elasticsearch.common.Preconditions;
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
    private final Set<String> exceptions;
    private final Set<String> urlsToProtect;
    private final Set<String> urlsNotNeedingCompleteProfile;
    private static final Logger log = LoggerFactory.getLogger(OAuthInterceptor.class);
    
    private OAuthInterceptor(UserFetcher userFetcher, 
            NumberToShortStringCodec idCodec,
            Set<String> urlsToProtect,
            Set<String> urlsNotNeedingCompleteProfile,
            Set<String> exceptions) {
        this.userFetcher = userFetcher;
        this.idCodec = idCodec;
        this.urlsToProtect = ImmutableSet.copyOf(urlsToProtect);
        this.urlsNotNeedingCompleteProfile = ImmutableSet.copyOf(urlsNotNeedingCompleteProfile);
        this.exceptions = ImmutableSet.copyOf(exceptions);
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        try {
            return authorized(request, response);
        } catch (Exception exception) {
            ResponseWriter writer = writerResolver.writerFor(request, response);
            ErrorSummary summary = ErrorSummary.forException(exception);
            new ErrorResultWriter().write(summary, writer, request, response);
        }
        return false;
    }
    
    public boolean authorized(HttpServletRequest request, HttpServletResponse response) throws NotAuthorizedException, UserProfileIncompleteException {
        if (urlsToProtect.isEmpty()) {
            return true; // given we are aren't protecting things that are really private.
        }
        Optional<User> user = userFetcher.userFor(request);
        
        if (authenticationIsRequired(request) && !user.isPresent()) {
            throw new NotAuthorizedException();
        }
        if (user.isPresent() && !user.get().isProfileComplete() && requiresCompleteProfile(request, user.get())) {
            throw new UserProfileIncompleteException();
        }
        return true;
    }
    
    private boolean authenticationIsRequired(final HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        for (String uri : urlsToProtect) {
            if (requestUri.startsWith(uri)) {
                return !exceptions.contains(requestUri);
            }
        }
        return false;
    }
    
    private boolean requiresCompleteProfile(HttpServletRequest request, User user) {
        String uid = idCodec.encode(user.getId().toBigInteger());
        String requestUri = uriWithoutExtension(request.getRequestURI()).replace(uid, ":uid");
        
        // Make a subset by taking urls to protect and removing urls not needing auth
        for (String uri : urlsToProtect) {
            if (requestUri.startsWith(uri)) {
                return !exceptions.contains(requestUri) && !urlsNotNeedingCompleteProfile.contains(requestUri);
            }
        }
        return false;
    }
    
    private String uriWithoutExtension(String requestUri) {
        int suffixStart = requestUri.lastIndexOf(".");
        if (suffixStart >= 0) {
            return requestUri.substring(0, suffixStart);
        }
        return requestUri;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private UserFetcher userFetcher;
        private NumberToShortStringCodec idCodec;
        private Set<String> exceptions = ImmutableSet.of();
        private Set<String> urlsToProtect = ImmutableSet.of();
        private Set<String> urlsNotNeedingCompleteProfile = ImmutableSet.of();
        
        public Builder withUserFetcher(UserFetcher userFetcher) {
            this.userFetcher = userFetcher;
            return this;
        }
        
        public Builder withIdCodec(NumberToShortStringCodec idCodec) {
            this.idCodec = idCodec;
            return this;
        }
        
        public Builder withExceptions(Set<String> exceptions) {
            this.exceptions = exceptions;
            return this;
        }
        
        public Builder withUrlsToProtect(Set<String> urlsToProtect) {
            this.urlsToProtect = urlsToProtect;
            return this;
        }
        
        public Builder withUrlsNotNeedingCompleteProfile(Set<String> urlsNotNeedingCompleteProfile) {
            this.urlsNotNeedingCompleteProfile = urlsNotNeedingCompleteProfile;
            return this;
        }
        
        public OAuthInterceptor build() {
            Preconditions.checkNotNull(userFetcher);
            Preconditions.checkNotNull(idCodec);
            if (urlsToProtect.isEmpty()) {
                log.info("No protected URLs have been set");
            }
            return new OAuthInterceptor(this.userFetcher, 
                    this.idCodec,
                    this.urlsToProtect,
                    this.urlsNotNeedingCompleteProfile,
                    this.exceptions);
        }
    }
}
