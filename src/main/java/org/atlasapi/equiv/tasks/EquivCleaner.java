package org.atlasapi.equiv.tasks;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;

import com.google.common.collect.Sets;

public class EquivCleaner {
    
    private final ContentResolver resolver;
    private final ContentWriter writer;

    public EquivCleaner(ContentResolver resolver, ContentWriter writer) {
        this.resolver = resolver;
        this.writer = writer;
    }

    public void cleanEquivalences(Brand brand) {
        for (String equivUri : brand.getEquivalentTo()) {
            Identified equivalent = resolver.findByCanonicalUri(equivUri);
            equivalent.getEquivalentTo().remove(brand.getCanonicalUri());
            if(equivalent instanceof Item) {
                writer.createOrUpdate((Item)equivalent);
            } else if (equivalent instanceof Container<?>) {
                writer.createOrUpdate((Container<?>)equivalent, false);
            }
        }
        brand.setEquivalentTo(Sets.<String>newHashSet());
    }

}
