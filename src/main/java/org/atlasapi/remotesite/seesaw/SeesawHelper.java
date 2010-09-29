package org.atlasapi.remotesite.seesaw;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Element;

import com.google.common.collect.Lists;

public class SeesawHelper {
    private final static String curiePrefix = "seesaw:";
    final static Pattern seesawLinkPattern = Pattern.compile("http://www.seesaw.com/.*/[bsp]-([0-9]+)-(.*)");
    
    @SuppressWarnings("unchecked")
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
    
    @SuppressWarnings("unchecked")
	static String getAllTextContent(Element element) {
        String text = element.getText().trim();
        List<Element> children = element.getChildren();
        for (Element child : children) {
            text += getAllTextContent(child);
        }
        return text;
    }
    
    @SuppressWarnings("unchecked")
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
    
    @SuppressWarnings("unchecked")
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
    
    static String getBrandCurieFromLink(String contentLink) {
        return curiePrefix + getBrandId(contentLink);
    }
    
    static String getCurieFromTitle(String title) {
        return curiePrefix + title.toLowerCase();
    }
    
    static String getCurieFromLink(String contentLink) {
        return curiePrefix + getId(contentLink);
    }
    
    private static String getId(String contentLink) {
        Matcher matcher = seesawLinkPattern.matcher(contentLink);
        if (matcher.matches()) {
            return matcher.group(1) + "-" + matcher.group(2);
        }
        return null;
    }
    
    private static String getBrandId(String contentLink) {
        Matcher matcher = seesawLinkPattern.matcher(contentLink);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        return null;
    }
}
