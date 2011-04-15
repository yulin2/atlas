package org.atlasapi.remotesite.preview;

import java.util.List;

import org.atlasapi.media.entity.Actor;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Person;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.metabroadcast.common.text.MoreStrings;

import nu.xom.Element;
import nu.xom.Elements;

public class PreviewFilmProcessor {
    
    private final ContentWriter contentWriter;
    private final ContentResolver contentResolver;
    private final AdapterLog log;

    public PreviewFilmProcessor(ContentResolver contentResolver, ContentWriter contentWriter, AdapterLog log) {
        this.contentResolver = contentResolver;
        this.contentWriter = contentWriter;
        this.log = log;
    }
    
    public void process(Element movieElement) {
        String id = movieElement.getAttributeValue("movie_id");
        String title = get(movieElement, "original_title");
        Integer duration = getInt(movieElement, "movie_duration");
        String website = get(movieElement, "official_website");
        String imdbLink = "http://imdb.com/title/" + movieElement.getAttributeValue("imdb_id");

        List<CrewMember> crewMembers = getCrewMembers(movieElement);
    }
    
    private List<CrewMember> getCrewMembers(Element movieElement) {
        List<CrewMember> people = Lists.newArrayList();
        
        Elements actorElements = movieElement.getFirstChildElement("actors").getChildElements("actor");
        for (int i = 0; i < actorElements.size(); i++) {
            people.add(Actor.actor(actorElements.get(i).getValue(), null, Publisher.PREVIEW_NETWORKS));
        }
        
        Elements directorElements = movieElement.getFirstChildElement("directors").getChildElements("director");
        for (int i = 0; i < directorElements.size(); i++) {
            people.add(CrewMember.crewMember(directorElements.get(i).getValue(), "director", Publisher.PREVIEW_NETWORKS));
        }
        
        return people;
    }

    private String get(Element element, String childName) {
        Element childElement = element.getFirstChildElement(childName);
        if (childElement != null) {
            if (!Strings.isNullOrEmpty(childElement.getValue())) {
                return childElement.getValue();
            }
        }
        
        return null;
    }
    
    private Integer getInt(Element element, String childName) {
        String stringValue = get(element, childName);
        
        if (stringValue != null && MoreStrings.containsOnlyAsciiDigits(stringValue)) {
            return Integer.parseInt(stringValue);
        }
        
        return null;
    }
}
