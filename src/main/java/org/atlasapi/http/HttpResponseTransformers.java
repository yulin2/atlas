package org.atlasapi.http;

import java.io.InputStreamReader;

import org.atlasapi.remotesite.html.HtmlNavigator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metabroadcast.common.http.HttpResponseTransformer;

public class HttpResponseTransformers {

    private HttpResponseTransformers() {
    }
    
    public static <T> HttpResponseTransformer<T> gsonResponseTransformer(GsonBuilder gsonBuilder, Class<? extends T> cls) {
        return new GsonHttpResponseTransformer<T>(gsonBuilder, cls);
    }

    public static class GsonHttpResponseTransformer<T> extends AbstractHttpResponseTransformer<T> {

        public GsonHttpResponseTransformer(GsonBuilder gsonBuilder, Class<? extends T> cls) {
            this.gson = gsonBuilder.create();
            this.cls = cls;
        }
        
        private final Class<? extends T> cls;
        private final Gson gson;

        @Override
        public T transform(InputStreamReader in) throws Exception {
            return gson.fromJson(in, cls);
        }
    }
    
    public static HttpResponseTransformer<HtmlNavigator> htmlNavigatorTransformer() {
        return htmlNavigatorInstance;
    }
    
    private static final HttpResponseTransformer<HtmlNavigator> htmlNavigatorInstance = new HtmlNavigatorHttpResponseTransformer();

    public static class HtmlNavigatorHttpResponseTransformer extends AbstractHttpResponseTransformer<HtmlNavigator> {

        @Override
        public HtmlNavigator transform(InputStreamReader in) throws Exception {
            return new HtmlNavigator(in);
        }
    }
}