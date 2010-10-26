package org.atlasapi.remotesite.hbo;

import java.util.List;

import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.xml.SimpleXmlNavigator;
import org.jdom.Element;

public class HboAdapterHelper {
    public final static String HBO_HOST = "http://www.hbo.com";
    
    public String getNavigationLink(String linkName, SimpleXmlNavigator navigator) {
        try {
            List<Element> navigationItems = navigator.allElementsMatching("//navigation//item");
            for (Element navigationItem : navigationItems) {
                Element nameElement = navigator.firstElementOrNull("name", navigationItem);
                if (nameElement != null) {
                    if (nameElement.getValue().equalsIgnoreCase(linkName)) {
                        String navigationLink = navigator.firstElementOrNull("nav", navigationItem).getValue();
                        
                        return getAbsoluteUrl(navigationLink);
                    }
                }
            }
        }
        catch (Exception e) {
            throw new FetchException("Exception when fetching navigation link " + linkName, e);
        }
        
        return null;
    }
    
    public String getXmlUri(String htmlUri) {
        return htmlUri.substring(0, htmlUri.length() - ".html".length()) + ".xml";
    }
    
    public String removeFormatting(String formattedText) {
        return formattedText.replaceAll("\\{.+?\\}", "").replaceAll("\\<.*?>", "");
    }
    
    public String getAbsoluteUrl(String relativeUrl) {
        if (!relativeUrl.startsWith("/")) {
            relativeUrl = "/" + relativeUrl;
        }
        
        return HBO_HOST + relativeUrl;
    }
}
