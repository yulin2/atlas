package org.atlasapi.remotesite.pa;

import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Person;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.mongo.GroupContentNotExistException;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.joda.time.DateTime;

import com.metabroadcast.common.time.DateTimeZones;

public class PaPersonWriter {
    private final ContentWriter contentWriter;
    private final ContentResolver contentResolver;
    private final AdapterLog log;

    public PaPersonWriter(ContentWriter contentWriter, ContentResolver contentResolver, AdapterLog log) {
        this.contentWriter = contentWriter;
        this.contentResolver = contentResolver;
        this.log = log;
    }
    
    public void createOrUpdatePeople(Item item) {
        for (CrewMember crewMember: item.people()) {
            Identified resolvedContent = contentResolver.findByCanonicalUri(crewMember.getCanonicalUri());
            
            Person person = null;
            if (resolvedContent instanceof Person) {
                person = (Person) resolvedContent;
            } else {
                person = crewMember.toPerson();
            }
            person.addContents(item);
            person.setLastUpdated(new DateTime(DateTimeZones.UTC));
            person.setMediaType(null);
            
            try {
                contentWriter.createOrUpdateSkeleton(person);
            } catch (GroupContentNotExistException e) {
                log.record(new AdapterLogEntry(Severity.WARN).withCause(e).withSource(getClass()).withDescription(e.getMessage()+" for episode: "+item.getCanonicalUri()));
            }
        }
    }
}
