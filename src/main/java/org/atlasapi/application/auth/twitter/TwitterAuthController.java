package org.atlasapi.application.auth.twitter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.Application;
import org.atlasapi.application.model.auth.OAuthProvider;
import org.atlasapi.application.model.auth.OAuthRequest;
import org.atlasapi.application.model.auth.OAuthResult;
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
import com.metabroadcast.common.social.twitter.TwitterApplication;
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
    private final String host;
    private final TwitterFactory twitterFactory;
    private QueryResultWriter<OAuthRequest> oauthRequestResultWriter;
    private QueryResultWriter<OAuthResult> oauthResultResultWriter;
    private Map<String, String> tokenRequestSecrets;
    private Map<String, String> accessTokenSecrets;
    
    public TwitterAuthController(TwitterApplication twitterApplication, 
            String host,
            QueryResultWriter<OAuthRequest> oauthRequestResultWriter,
            QueryResultWriter<OAuthResult> oauthResultResultWriter) {
        super();
        this.host = host;
        this.twitterFactory = new TwitterFactory(
            new ConfigurationBuilder()
                .setOAuthConsumerKey(twitterApplication.getConsumerKey())
                .setOAuthConsumerSecret(twitterApplication.getConsumerSecret())
            .build()
        );
        this.oauthRequestResultWriter = oauthRequestResultWriter;
        this.oauthResultResultWriter = oauthResultResultWriter;
        this.tokenRequestSecrets = Maps.newHashMap();
        this.accessTokenSecrets = Maps.newHashMap();
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
            accessTokenSecrets.put(token.getToken(), token.getTokenSecret());
            OAuthResult oauthResult = OAuthResult.builder()
                    .withSuccess(true)
                    .withProvider(OAuthProvider.TWITTER)
                    .withToken(token.getToken())
                    .build();
            QueryResult<OAuthResult> queryResult = QueryResult.singleResult(oauthResult, QueryContext.standard());
            oauthResultResultWriter.write(queryResult, responseWriter);
        } catch (TwitterException e) {
            throw new RuntimeException(e);
        } 
    }

}
