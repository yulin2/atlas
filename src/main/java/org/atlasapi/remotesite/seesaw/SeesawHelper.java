package org.atlasapi.remotesite.seesaw;

import java.util.List;

import org.jdom.Element;

public class SeesawHelper {
    static String getFirstTextContent(Element element) {
        if (!element.getText().equals("")) {
            return element.getText();
        }
            
        List<Element> children = element.getChildren();
        for (Element child : children) {
            String childText = getFirstTextContent(child);
            
            if (!childText.equals("")) {
                return childText;
            }
        }
        
        return "";
    }
    
    static String getFirstLinkUri(Element element) {
        if (element.getName().equals("a")) {
            return element.getAttributeValue("href");
        }

        List<Element> children = element.getChildren();
        for (Element child : children) {
            String link = getFirstLinkUri(child);
            if (!link.equals("")) {
                return link;
            }
        }
        
        return "";
    }
}
