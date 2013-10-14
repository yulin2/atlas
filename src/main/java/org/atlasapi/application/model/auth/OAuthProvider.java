package org.atlasapi.application.model.auth;

import com.google.common.collect.ImmutableSet;

public enum OAuthProvider {
    TWITTER("Twitter", "Sign in with Twitter", "/4.0/auth/twitter/login", "/static/images/sign-in-with-twitter-gray.png");
    
    private final String title;
    private final String prompt;
    private final String authRequestUrl;
    private final String image;
    private static final ImmutableSet<OAuthProvider> ALL = ImmutableSet.copyOf(values());
    
    OAuthProvider(String title, String prompt, String authRequestUrl, String image) {
        this.title = title;
        this.prompt = prompt;
        this.authRequestUrl = authRequestUrl;
        this.image = image;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public String getAuthRequestUrl() {
        return authRequestUrl;
    }
    
    public String getImage() {
        return image;
    }
    
    public static final ImmutableSet<OAuthProvider> all() {
        return ALL;
    }
}
