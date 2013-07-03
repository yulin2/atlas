package org.atlasapi.remotesite.thesun;

import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.time.DateTimeZones;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;

public class TheSunRssItemElement extends Element { 
    private static final String TOL_TEXT_NS = "http://www.timesonline.co.uk/rss-full-text";
    
    private DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeParser().withZone(DateTimeZones.UTC);

    public TheSunRssItemElement(Element element) {
        super(element);
    }
    
    public TheSunRssItemElement(String namespace, String name) {
        super(namespace, name);
    }
    
    public TheSunRssItemElement(String name) {
        super(name);
    }
    
    @Override
    public Node copy() {
        return new TheSunRssItemElement(this);
    }
    
    private String getElementValue(String elementName) {
        Elements elements = this.getChildElements(elementName);
        if (elements.size() == 0) {
            return null;
        }
        return elements.get(0).getValue();
    }
    
    private String getElementValue(String elementName, String namespace) {
        Elements elements = this.getChildElements(elementName, namespace);
        if (elements.size() == 0) {
            return null;
        }
        return elements.get(0).getValue();
    }
    
    public String getTitle() {
        return getElementValue("title");
    }
    
    public String getShortTitle() {
        return getElementValue("shortTitle");
    }
    
    public String getArticleTypeName() {
        return getElementValue("articleTypeName");
    }
    
    public String getArticleTypeVariant() {
        return getElementValue("articleVariant");
    }
    
    public String getSections() {
        return getElementValue("sections");
    }
    
    public String getSection11() {
        return getElementValue("section-11");
    }
    
    public String getAuthor() {
        return getElementValue("author");
    }
    
    public DateTime getPubDate() {
        return dateTimeFormatter.parseDateTime(getElementValue("pubDate"));
    }
    
    public String getLink() {
        return getElementValue("link");
    }
    
    public String getGuid() {
        return getElementValue("guid");
    }
    
    public String getDescription() {
        return getElementValue("description");
    }
    
    public String getStandFirst() {
        return getElementValue("standFirst");
    }
    
    public String getStory() {
        return getElementValue("story", TOL_TEXT_NS);
    }
    
    public List<TheSunRssEnclosureElement> getEnclosures() {
        ImmutableList.Builder<TheSunRssEnclosureElement> resultsBuilder = ImmutableList.builder();
        Elements elements = this.getChildElements("enclosure");
        for (int i=0; i<elements.size(); i++) {
            resultsBuilder.add((TheSunRssEnclosureElement) elements.get(i));
        }
        return resultsBuilder.build();
    }
    
    public TheSunRssOoyalaVideo getOoyalaVideo() {
        Elements elements = this.getChildElements("ooyalaVideo", TOL_TEXT_NS);
        if (elements.size() == 0) {
            return null;
        }
        return (TheSunRssOoyalaVideo) elements.get(0);
    }
}
