package org.atlasapi.remotesite.talktalk;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.ContentMerger;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemTypeType;
import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;

/**
 * Updates {@link org.atlasapi.media.entity.Content Content} for each provided
 * {@link VODEntityType}.
 * 
 * Entities for ItemDetail for Brands, Series and Episodes are fetched and
 * processed. For Brand and Series, their VOD lists are fetched and processed
 * entity by entity using this processor.
 * 
 */
//TODO: this shouldn't extract a list of Content but a ContentHierarchy.
public class ContentUpdatingTalkTalkVodEntityProcessor implements
        TalkTalkVodEntityProcessor<List<Content>> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final TalkTalkClient client;
    private final ContentResolver resolver;
    private final ContentWriter writer;
    
    private final TalkTalkItemDetailItemExtractor itemExtractor = new TalkTalkItemDetailItemExtractor();
    private final TalkTalkItemDetailContainerExtractor containerExtractor = new TalkTalkItemDetailContainerExtractor();

    public ContentUpdatingTalkTalkVodEntityProcessor(TalkTalkClient client, ContentResolver resolver, ContentWriter writer) {
        this.client = checkNotNull(client);
        this.resolver = checkNotNull(resolver);
        this.writer = checkNotNull(writer);
    }

    private void logProcessing(VODEntityType entity) {
        log.debug("processing {} {}", entity.getItemType(), entity.getId());
    }

    @Override
    public List<Content> processEntity(VODEntityType entity) {
        return processEntity(Optional.<Brand>absent(), Optional.<Series>absent(), entity);
    }

    public List<Content> processEntity(Optional<Brand> brand, Optional<Series> series, VODEntityType entity) {
        ImmutableList<Content> result = ImmutableList.of();
        switch (entity.getItemType()) {
        case BRAND:
            result = processBrandEntity(entity);
            break;
        case SERIES:
            result = processSeriesEntity(brand, entity);
            break;
        case EPISODE:
            result = processEpisodeEntity(brand, series, entity);
            break;
        default:
            log.warn("Not processing unexpected entity type {}", entity.getItemType());
            break;
        }
        return result;
    }
    
    public ImmutableList<Content> processBrandEntity(VODEntityType entity) {
        logProcessing(entity);
        ItemDetailType detail = getEntityDetail(entity);
        if (detail == null) {
            return ImmutableList.of();
        }
        
        Brand brand = containerExtractor.extractBrand(detail);
        Optional<Identified> existing = getExisting(brand.getCanonicalUri());
        if (existing.isPresent()) {
            Identified identifiedExisting = existing.get();
            checkState(identifiedExisting instanceof Brand, "%s not Brand", existing);
            brand = (Brand) ContentMerger.merge((Brand) existing.get(), brand);
        }
        
        writer.createOrUpdate(brand);
        return ImmutableList.<Content>builder()
                .add(brand)
                .addAll(processEntityList(Optional.of(brand), Optional.<Series>absent(),entity))
                .build();
    }

    private ImmutableList<Content> processSeriesEntity(final Optional<Brand> brand, VODEntityType entity) {
        logProcessing(entity);
        ItemDetailType detail = getEntityDetail(entity);
        if (detail == null) {
            return ImmutableList.of();
        }
        
        Series series = containerExtractor.extractSeries(detail, brand);
        Optional<Identified> existing = getExisting(series.getCanonicalUri());
        if (existing.isPresent()) {
            Identified identifiedExisting = existing.get();
            checkState(identifiedExisting instanceof Series, "%s not Series", existing);
            Container merged = ContentMerger.merge((Series) existing.get(), series);
            series = (Series) merged;
        }
        writer.createOrUpdate(series);
        return ImmutableList.<Content>builder()
                .add(series)
                .addAll(processEntityList(brand, Optional.of(series), entity))
                .build();
    }


    private ImmutableList<Content> processEpisodeEntity(Optional<Brand> brand, Optional<Series> series,
            VODEntityType entity) {
        logProcessing(entity);
        checkArgument(ItemTypeType.EPISODE.equals(entity.getItemType()));
        ItemDetailType detail = getEntityDetail(entity);
        if (detail == null) {
            return ImmutableList.of();
        }
        
        Item item = itemExtractor.extract(detail, brand, series);
        Optional<Identified> existing = getExisting(item.getCanonicalUri());
        item = mergeWithExisting(item, existing);
        
        writer.createOrUpdate(item);
        
        return ImmutableList.<Content> of(item);
    }
    
    private ImmutableList<Content> processEntityList(final Optional<Brand> brand,
            Optional<Series> series, VODEntityType entity) {
            try {
                return client.processVodList(groupType(entity), entity.getId(), entityProcessor(brand, series));
            } catch (TalkTalkException tte) {
                String message = String.format("List fetch failed: %s %s", entity.getItemType(), entity.getId());
                log.error(message,tte);
                return ImmutableList.of();
            }
    }
    
    private TalkTalkVodListProcessor<ImmutableList<Content>> entityProcessor(
            final Optional<Brand> brand, final Optional<Series> series) {
        return new TalkTalkVodListProcessor<ImmutableList<Content>>() {
            
            ImmutableList.Builder<Content> contents = ImmutableList.builder();
            
            @Override
            public ImmutableList<Content> getResult() {
                return contents.build();
            }
   
            @Override
            public void process(VODEntityType entity) {
                contents.addAll(processEntity(brand, series, entity));
            }
            
        };
    }

    private ItemDetailType getEntityDetail(VODEntityType entity) {
        try {
            return client.getItemDetail(groupType(entity), entity.getId());
        } catch (TalkTalkException tte) {
            String message = String.format("Entity fetch failed: %s %s", entity.getItemType(), entity.getId());
            log.error(message,tte);
            return null;
        }
    }
    
    private GroupType groupType(VODEntityType entity) {
        return GroupType.fromItemType(entity.getItemType()).get();
    }

    private Item mergeWithExisting(Item extracted, Optional<Identified> existing) {
        if (existing.isPresent()) {
            Identified identifiedExisting = existing.get();
            checkState(identifiedExisting instanceof Item, "%s not Item", existing);
            extracted = ContentMerger.merge((Item) existing.get(), extracted);
        }
        return extracted;
    }

    private Optional<Identified> getExisting(String uri) {
        Maybe<Identified> maybe = resolver.findByCanonicalUris(ImmutableSet.of(uri)).get(uri);
        if (maybe.hasValue()) {
            return Optional.of(maybe.requireValue());
        }
        return Optional.absent();
    }
}
