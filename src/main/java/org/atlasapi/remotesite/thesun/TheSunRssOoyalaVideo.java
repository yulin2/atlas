package org.atlasapi.remotesite.thesun;

import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;


public class TheSunRssOoyalaVideo extends Element {

    private static final String TOL_TEXT_NS = "http://www.timesonline.co.uk/rss-full-text";
    
    public TheSunRssOoyalaVideo(Element element) {
        super(element);
    }

    public TheSunRssOoyalaVideo(String name, String namespace) {
        super(name, namespace);
    }

    public TheSunRssOoyalaVideo(String name) {
        super(name);
    }

    @Override
    public Node copy() {
        return new TheSunRssOoyalaVideo(this);
    }
    
    private String getTolTextElementValue(String elementName) {
        Elements elements = this.getChildElements(elementName, TOL_TEXT_NS);
        if (elements.size() == 0) {
            return null;
        }
        return elements.get(0).getValue();
    }
    
    public String getId() {
        return this.getAttributeValue("id");
    }
    
    public String getHeadline() {
        return getTolTextElementValue("headline");
    }
    
    public String getTeaser() {
        return getTolTextElementValue("teaser");
    }
    
    public TheSunRssEnclosureElement getEnclosure() {
        Elements elements = this.getChildElements("enclosure", TOL_TEXT_NS);
        if (elements.size() == 0) {
            return null;
        }
        return (TheSunRssEnclosureElement) elements.get(0);
    }

}
