package org.atlasapi.query.content.people;

import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.QueuingPersonWriter;
import org.atlasapi.persistence.logging.AdapterLog;

public class QueuingItemsPeopleWriter implements ItemsPeopleWriter {
    @SuppressWarnings("unused")
    private final AdapterLog log;
    private final QueuingPersonWriter personWriter;

    public QueuingItemsPeopleWriter(QueuingPersonWriter personWriter, AdapterLog log) {
        this.personWriter = personWriter;
        this.log = log;
    }
    
    public void createOrUpdatePeople(Item item) {
        for (CrewMember crewMember: item.people()) {
            personWriter.addItemToPerson(crewMember.toPerson(), item);
        }
    }
}
