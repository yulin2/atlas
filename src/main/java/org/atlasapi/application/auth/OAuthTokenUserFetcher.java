package org.atlasapi.application.auth;

import javax.servlet.http.HttpServletRequest;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.social.auth.credentials.Credentials;
import com.metabroadcast.common.social.auth.credentials.CredentialsStore;
import com.metabroadcast.common.social.auth.facebook.AccessTokenChecker;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.UserRef.UserNamespace;

public class OAuthTokenUserFetcher implements UserFetcher {

    public static final String OAUTH_PROVIDER_QUERY_PARAMETER = "oauth_provider";
    public static final String OAUTH_TOKEN_QUERY_PARAMETER = "oauth_token";
    
    private final CredentialsStore credentialsStore;
    private final AccessTokenChecker accessTokenChecker;

    public OAuthTokenUserFetcher(CredentialsStore credentialsStore,
            AccessTokenChecker accessTokenChecker) {
        this.credentialsStore = credentialsStore;
        this.accessTokenChecker = accessTokenChecker;
    }
    
    public ImmutableSet<String> getParameterNames() {
        return ImmutableSet.of(OAUTH_PROVIDER_QUERY_PARAMETER,
                OAUTH_TOKEN_QUERY_PARAMETER);
    }

    public Optional<UserRef> userFor(HttpServletRequest request) {
        if (!Strings.isNullOrEmpty(request.getParameter(OAUTH_PROVIDER_QUERY_PARAMETER))) {
            UserNamespace oauthProviderNamespace = UserNamespace.valueOf(request.getParameter(OAUTH_PROVIDER_QUERY_PARAMETER).toUpperCase());
            String oauthToken = request.getParameter(OAUTH_TOKEN_QUERY_PARAMETER);
            Optional<Credentials> credentials = credentialsStore.credentialsForToken(oauthProviderNamespace, oauthToken);
            if (credentials.isPresent() 
                    && accessTokenChecker.check(credentials.get().authToken()).hasValue()) {
                return Optional.of(credentials.get().userRef());
            }
        }
        return Optional.absent();
    }
}
