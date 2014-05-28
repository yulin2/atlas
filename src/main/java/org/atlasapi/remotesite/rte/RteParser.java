package org.atlasapi.remotesite.rte;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import joptsimple.internal.Strings;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;


public class RteParser {
    
    private static final Predicate<NameValuePair> IS_ID_PARAM = new Predicate<NameValuePair>() {
        @Override
        public boolean apply(NameValuePair parameter) {
            return parameter.getName().equals("id");
        }
    };

    public static String canonicalUriFrom(String idUri) {
        checkArgument(!Strings.isNullOrEmpty(idUri), "Cannot build canonical uri from empty uri");
        NameValuePair id = getIdParam(idUri);
        
        return buildCanonicalUriFromId(id.getValue());
    }

    private static NameValuePair getIdParam(String idUri) {
        try {
            return Iterables.getOnlyElement(Iterables.filter(getParamsFrom(idUri), IS_ID_PARAM));
        } catch (Exception e) {
            throw new IllegalArgumentException("Uri must have one, and only one, 'id' query parameter");
        }
    }

    private static List<NameValuePair> getParamsFrom(String uri) {
        try {
            return URLEncodedUtils.parse(new URI(uri), "UTF-8");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid uri: " + uri, e);
        }
    }
    
    private static String buildCanonicalUriFromId(String id) {
        checkArgument(!Strings.isNullOrEmpty(id), "Cannot build canonical uri from empty id");
        return "http://rte.ie/shows/"+id;
    }
    
}
