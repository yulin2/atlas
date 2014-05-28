package org.atlasapi.remotesite.rte;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import joptsimple.internal.Strings;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
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
        NameValuePair id = Iterables.getOnlyElement(Iterables.filter(getParamsFrom(idUri), IS_ID_PARAM));
        
        return buildCanonicalUriFromId(id.getValue());
    }

    private static List<NameValuePair> getParamsFrom(String url) {
        try {
            return URLEncodedUtils.parse(new URI(url), "UTF-8");
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }
    
    private static String buildCanonicalUriFromId(String id) {
        checkArgument(!Strings.isNullOrEmpty(id), "Cannon build canonical uri from empty id");
        
        return "http://rte.ie/shows/"+id;
    }
    
}
