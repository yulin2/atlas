package org.atlasapi.remotesite.seesaw;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Countries;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.html.HtmlNavigator;
import org.jaxen.JaxenException;
import org.jdom.Element;

import com.google.common.collect.Sets;

public class SeesawItemContentExtractor implements ContentExtractor<HtmlNavigator, Episode> {
    static final Log LOG = LogFactory.getLog(SeesawItemContentExtractor.class);
    
    @Override
    public Episode extract(HtmlNavigator source) {
        try {
            System.out.println("extracting episode");
            Episode episode = new Episode();
            episode.setPublisher(Publisher.SEESAW);
            
            Version version = new Version();
            version.setProvider(Publisher.SEESAW);
            Encoding encoding = new Encoding();
            Location linkLocation = new Location();
            linkLocation.setTransportType(TransportType.LINK);
            linkLocation.setAvailable(true);
            linkLocation.setPolicy(ukPolicy());
            encoding.addAvailableAt(linkLocation);
            version.addManifestedAs(encoding);
            
            episode.addVersion(version);
            
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
                        LOG.warn("Unable to parse int", e);
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
                        LOG.warn("Unable to parse int", e);
                    }
                }
                
                if (episodeText.contains(": ")) {
                    String episodeTitle = episodeText.substring(episodeText.indexOf(": ") + 2, episodeText.length());
                    episode.setTitle(episodeTitle);
                    System.out.println("title: " + episodeTitle);
                } else if (episode.getEpisodeNumber() != null){
                    episode.setTitle("Episode "+episode.getEpisodeNumber());
                }
            }
            
            if (episode.getTitle() == null) {
                episode.setTitle(title);
            }
            
            Element playerInfoElem = source.firstElementOrNull("//*[@class='programInfo']");
            if (playerInfoElem != null) {
                String info = SeesawHelper.getAllTextContent(playerInfoElem);
                Pattern pattern = Pattern.compile(".*\\((\\d+) mins\\).*", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(info);
                if (matcher.matches()) {
                    try {
                        Integer duration = Integer.valueOf(matcher.group(1)) * 60;
                        System.out.println("Duration " + duration);
                        version.setPublishedDuration(duration);
                    }
                    catch (NumberFormatException e) {
                        LOG.debug("Exception when trying to parse duration: ", e);
                    }
                }
                
            }
            
            Element programmeInfoElem = source.firstElementOrNull("//*[text()='About this programme:']/following-sibling::*", infoElem);
            if (programmeInfoElem != null) {
                String progDesc = SeesawHelper.getFirstTextContent(programmeInfoElem).trim();
                System.out.println("desc: " + progDesc);
                episode.setDescription(progDesc);
            }
            
            Element dateElem = source.firstElementOrNull("//*[text()='Date: ']/following-sibling::*", infoElem);
            if (dateElem != null) {
                String date = SeesawHelper.getFirstTextContent(dateElem).trim();
                System.out.println("date: " + date);
            }
            
            Element categoryElem = source.firstElementOrNull("//*[text()='Categories: ']/following-sibling::*", infoElem);
            if (categoryElem != null) {
                String category = SeesawHelper.getFirstTextContent(categoryElem).trim();
                String categoryLink = SeesawHelper.getFirstLinkUri(categoryElem);
                
                episode.setGenres(Sets.newHashSet(categoryLink));
                System.out.println("category: " + category);
            }
            
            Element externalLinksElem = source.firstElementOrNull("//*[text()='External Links']/following-sibling::*", infoElem);
            if (externalLinksElem != null) {
                List<String> links = SeesawHelper.getAllLinkUris(externalLinksElem);
                System.out.println("external links:");
                for (String link : links) {
                    System.out.println(link);
                }
            }
            
            return episode;
        } catch (JaxenException e) {
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
