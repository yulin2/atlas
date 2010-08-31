package org.atlasapi.remotesite.seesaw;

import java.util.List;

import org.atlasapi.media.entity.Countries;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.html.HtmlNavigator;
import org.jaxen.JaxenException;
import org.jdom.Element;

import sun.util.logging.resources.logging;

public class SeesawItemContentExtractor implements ContentExtractor<HtmlNavigator, Episode> {
    @Override
    public Episode extract(HtmlNavigator source) {
        try {
            System.out.println("extracting episode");
            Episode episode = new Episode();
            episode.setPublisher(Publisher.SEESAW);
            
            Element infoElem = source.firstElementOrNull("//div[@class='information']");
            List<Element> headers = source.allElementsMatching("h3", infoElem);
            
            String seriesText = null;
            String episodeText;
            String title = headers.get(0).getText();
            if (headers.size() > 1) {
                if (headers.size() > 2) {
                    seriesText = headers.get(1).getText();
                    episodeText = headers.get(2).getText();
                }
                else {
                    episodeText = headers.get(1).getText();
                }
                
                System.out.println("series: " + seriesText);
                System.out.println("episode: " + episodeText);
                
                if (seriesText != null && seriesText.startsWith("Series ")) {
                    try {
                        int seriesNumber = Integer.parseInt(seriesText.substring("Series ".length(), seriesText.length()));
                        System.out.print("Series number: " + seriesNumber + " / ");
                        episode.setSeriesNumber(seriesNumber);
                    }
                    catch (NumberFormatException e) {
                        e.printStackTrace();
                        // something is messed up at their end, let's ignore
                    }
                    
                }
                if (episodeText.startsWith("Episode ")) {
                    try {
                        String numberString = episodeText.substring("Episode ".length());
                        
                        if (numberString.contains(":")) {
                            numberString = numberString.substring(0, numberString.indexOf(""));
                        }
                        
                        if (numberString.contains(" - ")) {
                            numberString = numberString.substring(0, numberString.indexOf(" - "));
                        }
                        
                        int episodeNumber = Integer.parseInt(episodeText.substring("Episode ".length(), 
                            episodeText.contains(":") ? episodeText.indexOf(":") : episodeText.length()));
                        episode.setEpisodeNumber(episodeNumber);
                        System.out.println("Episode number: " + episodeNumber);
                    }
                    catch (NumberFormatException e) {
                        e.printStackTrace();
                        // something is messed up at their end, let's ignore
                    }
                }
                
                if (episodeText.contains(": ")) {
                    String episodeTitle = episodeText.substring(episodeText.indexOf(": ") + 2, episodeText.length());
                    episode.setTitle(episodeTitle);
                    System.out.println("title: " + episodeTitle);
                }
            }
            
            Element programmeInfoElem = source.firstElementOrNull("//h3[text()='About this programme:']/following-sibling::*", infoElem);
            if (programmeInfoElem != null) {
                String progDesc = SeesawHelper.getFirstTextContent(programmeInfoElem).trim();
                System.out.println("desc: " + progDesc);
                episode.setDescription(progDesc);
            }
            Element seriesInfoElem = source.firstElementOrNull("//h3[text()='About this series:']/parent::div/div", infoElem);
            if (seriesInfoElem != null) {
                String seriesInfo = SeesawHelper.getFirstTextContent(seriesInfoElem).trim();
                System.out.println("serDesc: " + seriesInfo);
            }
            
            Element dateElem = source.firstElementOrNull("//h3[text()='Date: ']/following-sibling::*", infoElem);
            if (dateElem != null) {
                String date = SeesawHelper.getFirstTextContent(dateElem).trim();
                System.out.println("date: " + date);
            }
            
            Element categoryElem = source.firstElementOrNull("//h3[text()='Categories: ']/following-sibling::*", infoElem);
            if (categoryElem != null) {
                String category = SeesawHelper.getFirstTextContent(categoryElem).trim();
                System.out.println("category: " + category);
            }
            
            return episode;
        } catch (JaxenException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
    
    private Policy ukPolicy() {
        Policy policy = new Policy();
        policy.addAvailableCountry(Countries.GB);
        return policy;
    }

}
