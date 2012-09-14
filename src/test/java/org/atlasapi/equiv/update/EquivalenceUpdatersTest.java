package org.atlasapi.equiv.update;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.reflect.TypeToken;

@RunWith(MockitoJUnitRunner.class)
public class EquivalenceUpdatersTest {

    
    @Test
    public void test() {
        
        EquivalenceUpdater<Item> itemUpdater = mockedUpdater("item");
        EquivalenceUpdater<Episode> episodeUpdater = mockedUpdater("episode"); 
        EquivalenceUpdater<Container> containerUpdater = mockedUpdater("container"); 
        
        EquivalenceUpdaters updaters = new EquivalenceUpdaters();
        
        Episode ep = new Episode("uri", "curie", Publisher.BBC);
        
        updaters.register(Publisher.BBC, Item.class, itemUpdater);
        
        updaters.updateEquivalences(ep);
        verify(itemUpdater).updateEquivalences(ep);
        
        updaters.register(Publisher.BBC, Episode.class, episodeUpdater);
        
        updaters.updateEquivalences(ep);
        verify(episodeUpdater).updateEquivalences(ep);
        Mockito.reset(itemUpdater, episodeUpdater, containerUpdater);
        
        updaters.register(Publisher.BBC, Container.class, containerUpdater);
        
        updaters.updateEquivalences(ep);
        verify(episodeUpdater).updateEquivalences(ep);
        
    }

    @SuppressWarnings("unchecked")
    private <T> EquivalenceUpdater<T> mockedUpdater(String name) {
        return (EquivalenceUpdater<T>) mock(EquivalenceUpdater.class, name);
    }

}
