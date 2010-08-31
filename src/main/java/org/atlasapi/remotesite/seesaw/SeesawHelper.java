package org.atlasapi.remotesite.seesaw;

import java.util.List;

import org.jdom.Element;

import com.google.common.collect.Lists;

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
    
    static String getAllTextContent(Element element) {
        String text = element.getText().trim();
        
        List<Element> children = element.getChildren();
        for (Element child : children) {
            text += getAllTextContent(child);
        }
        
        return text;
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
    
    static List<String> getAllLinkUris(Element element) {
        List<String> uris = Lists.newArrayList();
        if (element.getName().equals("a")) {
            uris.add(element.getAttributeValue("href"));
        }
        
        List<Element> children = element.getChildren();
        for (Element child : children) {
            uris.addAll(getAllLinkUris(child));
        }
        
        return uris;
    }
}
