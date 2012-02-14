package org.atlasapi.remotesite.hulu;

import org.atlasapi.remotesite.html.HtmlNavigator;

import com.metabroadcast.common.base.Maybe;

public interface HuluClient {

    Maybe<HtmlNavigator> get(String resource);

}