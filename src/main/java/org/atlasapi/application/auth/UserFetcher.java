package org.atlasapi.application.auth;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.social.model.UserRef;

public interface UserFetcher {
	
    Optional<UserRef> userFor(HttpServletRequest request);

    ImmutableSet<String> getParameterNames();

}
