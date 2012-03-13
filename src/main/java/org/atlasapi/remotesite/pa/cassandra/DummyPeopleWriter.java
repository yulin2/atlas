package org.atlasapi.remotesite.pa.cassandra;

import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;

/**
 */
public class DummyPeopleWriter implements ItemsPeopleWriter {

    @Override
    public void createOrUpdatePeople(Item item) {
    }    
}
