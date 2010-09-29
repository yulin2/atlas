package org.atlasapi.remotesite.seesaw;

import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.atlasapi.persistence.system.RemoteSiteClient;

import com.google.inject.internal.Lists;

public class SeesawSitemapClient implements RemoteSiteClient<List<String>>{
    private static final String NS = "http://www.sitemaps.org/schemas/sitemap/0.9";

    @Override
    public List<String> get(String url) throws Exception {
        List<String> urls = Lists.newArrayList();
        
        Builder parser = new Builder();
        Document doc = parser.build(url);
        
        Elements elements = doc.getRootElement().getChildElements("url", NS);
        for (int i=0; i<elements.size(); i++) {
            Element element = elements.get(i);
            
            if (element != null) {
                Element location = element.getFirstChildElement("loc", NS);
                if (location != null && location.getValue() != null) {
                    urls.add(location.getValue());
                }
            }
        }
        
        return urls;
    }
}
