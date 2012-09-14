package org.atlasapi.equiv.update;

import static org.atlasapi.media.entity.ChildRef.TO_URI;

import java.util.List;

import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class RootEquivalenceUpdater implements EquivalenceUpdater<Content> {
    
    private static final Logger log = LoggerFactory.getLogger(RootEquivalenceUpdater.class);

    private ContentResolver contentResolver;
    private EquivalenceUpdater<Content> updater;

    public RootEquivalenceUpdater(ContentResolver contentResolver, EquivalenceUpdater<Content> updater) {
        this.contentResolver = contentResolver;
        this.updater = updater;
    }

    @Override
    public void updateEquivalences(Content content) {
        if (content instanceof Container) {
            updateContainer((Container) content);
        } else if (content instanceof Item){
            updateContentEquivalence(content);
        }
    }

    private void updateContentEquivalence(Content content) {
        log.trace("equiv update {}", content);
        updater.updateEquivalences(content);
    }

    private void updateContainer(Container container) {
        updateContentEquivalence(container);
        for (Item child : childrenOf(container)) {
            updateContentEquivalence(child);
        }
        updateContentEquivalence(container);
    }

    protected Iterable<Item> childrenOf(Container content) {
        List<String> childUris = Lists.transform(content.getChildRefs(), TO_URI);
        ResolvedContent children = contentResolver.findByCanonicalUris(childUris);
        return Iterables.filter(children.getAllResolvedResults(), Item.class);
    }
}
