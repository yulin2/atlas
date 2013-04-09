package org.atlasapi.remotesite.bbc.ion;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.HttpStatusCode;

@Controller
public class BbcIonContentUpdateController {

    private final SiteSpecificAdapter<Item> itemAdapter;
    private final ContentResolver resolver;
    private final ContentWriter writer;
    private final BbcIonItemMerger merger = new BbcIonItemMerger();

    public BbcIonContentUpdateController(ContentWriter writer, ContentResolver resolver, SiteSpecificAdapter<Item> itemAdapter) {
        this.writer = writer;
        this.resolver = resolver;
        this.itemAdapter = itemAdapter;
    }

    @RequestMapping(value="/system/update/bbc/item/{pid}", method=RequestMethod.POST)
    public void updateItem(HttpServletResponse response, @PathVariable("pid") String pid) {
        String uri = BbcFeeds.slashProgrammesUriForPid(pid);
        Item fetchedItem = itemAdapter.fetch(uri);
        Maybe<Identified> existingItem = resolver.findByCanonicalUris(ImmutableList.of(uri)).getFirstValue();

        if (existingItem.isNothing()) {
            writer.createOrUpdate(fetchedItem);
        } else if (existingItem.requireValue() instanceof Item) {
            Item merged = merger.merge(fetchedItem, (Item)existingItem.requireValue());
            writer.createOrUpdate(merged);
        } else {
            throw new FetchException(String.format("Existing content for %s with type %s", uri, existingItem.requireValue().getClass().getSimpleName()));
        }
        
        response.setStatus(HttpStatusCode.OK.code());
        response.setContentLength(0);
    }


    @RequestMapping("/system/update/bbc/container/{pid}")
    public void updateContainer(@PathVariable("pid") String pid) {

    }

}
