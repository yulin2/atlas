package org.atlasapi.application.auth.twitter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.atlasapi.application.model.auth.OAuthProvider;
import org.atlasapi.application.model.auth.OAuthRequest;
import org.atlasapi.application.model.auth.OAuthResult;
import org.atlasapi.application.users.NewUserSupplier;
import org.atlasapi.application.users.User;
import org.atlasapi.application.users.UserStore;
import org.atlasapi.output.NotAcceptableException;
import org.atlasapi.output.QueryResultWriter;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.output.ResponseWriterFactory;
import org.atlasapi.output.UnsupportedFormatException;
import org.atlasapi.query.common.QueryContext;
import org.atlasapi.query.common.QueryResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.social.auth.credentials.AuthToken;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.UserRef.UserNamespace;
import com.metabroadcast.common.social.twitter.TwitterApplication;
import com.metabroadcast.common.social.user.AccessTokenProcessor;
import com.metabroadcast.common.url.UrlEncoding;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

@Controller
public class TwitterAuthController {
    private final ResponseWriterFactory writerResolver = new ResponseWriterFactory();
    private final TwitterFactory twitterFactory;
    private UserStore userStore; 
    private NewUserSupplier userSupplier;
    private QueryResultWriter<OAuthRequest> oauthRequestResultWriter;
    private QueryResultWriter<OAuthResult> oauthResultResultWriter;
    private AccessTokenProcessor accessTokenProcessor;
    
    private Map<String, String> tokenRequestSecrets;
    
    public TwitterAuthController(TwitterApplication twitterApplication, 
            AccessTokenProcessor accessTokenProcessor,
            UserStore userStore, 
            NewUserSupplier userSupplier,
            QueryResultWriter<OAuthRequest> oauthRequestResultWriter,
            QueryResultWriter<OAuthResult> oauthResultResultWriter) {
        super();
        this.accessTokenProcessor = accessTokenProcessor;
        this.twitterFactory = new TwitterFactory(
            new ConfigurationBuilder()
                .setOAuthConsumerKey(twitterApplication.getConsumerKey())
                .setOAuthConsumerSecret(twitterApplication.getConsumerSecret())
            .build()
        );
        this.userStore = userStore;
        this.userSupplier = userSupplier;
        this.oauthRequestResultWriter = oauthRequestResultWriter;
        this.oauthResultResultWriter = oauthResultResultWriter;
        this.tokenRequestSecrets = Maps.newHashMap();
    }

    // TODO restrict callback URLs?
    @RequestMapping(value = { "/4.0/auth/twitter/login.*" }, method = RequestMethod.GET)
    public void getTwitterLogin(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(required = true) String callbackUrl,
            @RequestParam(required = false) String targetUri) throws UnsupportedFormatException, NotAcceptableException, IOException {
        ResponseWriter writer = writerResolver.writerFor(request, response);
        try {
            Twitter twitter = twitterFactory.getInstance();
            if (!Strings.isNullOrEmpty(targetUri)) {
                callbackUrl += "?targetUri=" + UrlEncoding.encode(targetUri);
            }
            RequestToken requestToken = twitter.getOAuthRequestToken(callbackUrl);
            OAuthRequest oauthRequest = OAuthRequest.builder()
                    .withAuthUrl(requestToken.getAuthenticationURL())
                    .withToken(requestToken.getToken())
                    .build();
            tokenRequestSecrets.put(requestToken.getToken(), requestToken.getTokenSecret());
            QueryResult<OAuthRequest> queryResult = QueryResult.singleResult(oauthRequest, QueryContext.standard());

            oauthRequestResultWriter.write(queryResult, writer);
        } catch (TwitterException e) {
            throw new RuntimeException(e);
        }
    }
    
    @RequestMapping(value = { "/4.0/auth/twitter/token.*" }, method = RequestMethod.GET)
    public void getAccessToken(HttpServletResponse response, HttpServletRequest request, 
            @RequestParam String oauthToken,
            @RequestParam String oauthVerifier,
            @RequestParam(required = false) String targetUri) throws UnsupportedFormatException, NotAcceptableException, IOException {
        Twitter twitter = twitterFactory.getInstance();
        if (!tokenRequestSecrets.containsKey(oauthToken)) {
            response.setStatus(401);
            return;
        }
        
        RequestToken requestToken = new RequestToken(oauthToken, tokenRequestSecrets.get(oauthToken));
        ResponseWriter responseWriter = writerResolver.writerFor(request, response);

        tokenRequestSecrets.remove(requestToken.getToken());
        try {
            AccessToken token = twitter.getOAuthAccessToken(requestToken, oauthVerifier);
            Maybe<UserRef> userRef = accessTokenProcessor.process(new AuthToken(token.getToken(), token.getTokenSecret(), UserNamespace.TWITTER, null));
            OAuthResult oauthResult;
            if (userRef.hasValue()) {
                // TODO pass some user info back?
                User user = getUser(userRef.requireValue());
                oauthResult = OAuthResult.builder()
                        .withSuccess(true)
                        .withProvider(OAuthProvider.TWITTER)
                        .withToken(token.getToken())
                        .build();
            } else {
                oauthResult = OAuthResult.builder()
                        .withSuccess(false)
                        .withProvider(OAuthProvider.TWITTER)
                        .withToken("")
                        .build();
            }
            
            QueryResult<OAuthResult> queryResult = QueryResult.singleResult(oauthResult, QueryContext.standard());
            oauthResultResultWriter.write(queryResult, responseWriter);
        } catch (TwitterException e) {
            throw new RuntimeException(e);
        } 
    }
    
    private User getUser(UserRef userRef) {
        User user = userStore.userForRef(userRef).or(userSupplier);
        if (user.getUserRef() == null) {
            user.setUserRef(userRef);
            userStore.store(user);
        }
        return user;
    }

}
