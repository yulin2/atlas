package org.atlasapi.remotesite.talktalk;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Container;
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
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.scheduling.UpdateProgress;

/**
 * Updates {@link org.atlasapi.media.entity.Content Content} for each provided
 * {@link VODEntityType}.
 * 
 * Entities for ItemDetail for Brands, Series and Episodes are fetched and
 * processed. For Brand and Series, their VOD lists are fetched and processed
 * entity by entity using this processor.
 * 
 */
public class ContentUpdatingTalkTalkContentEntityProcessor implements
        TalkTalkContentEntityProcessor<UpdateProgress> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final TalkTalkClient client;
    private final ContentResolver resolver;
    private final ContentWriter writer;
    
    private final TalkTalkItemDetailItemExtractor itemExtractor = new TalkTalkItemDetailItemExtractor();
    private final TalkTalkItemDetailContainerExtractor containerExtractor = new TalkTalkItemDetailContainerExtractor();

    public ContentUpdatingTalkTalkContentEntityProcessor(TalkTalkClient client, ContentResolver resolver, ContentWriter writer) {
        this.client = checkNotNull(client);
        this.resolver = checkNotNull(resolver);
        this.writer = checkNotNull(writer);
    }

    private void logProcessing(VODEntityType entity) {
        log.debug("processing {} {}", entity.getItemType(), entity.getId());
    }
    
    @Override
    public UpdateProgress processBrandEntity(VODEntityType entity) {
        try {
            logProcessing(entity);
            Container brand = containerExtractor.extractBrand(getEntityDetail(entity));
            Optional<Identified> existing = getExisting(brand.getCanonicalUri());
            if (existing.isPresent()) {
                Identified identifiedExisting = existing.get();
                checkState(identifiedExisting instanceof Brand, "%s not Brand", existing);
                brand = ContentMerger.merge((Brand) existing.get(), brand);
            }
            writer.createOrUpdate(brand);
            final Optional<Brand> extractedBrand = Optional.of((Brand)brand);
            return client.processVodList(entity.getItemType(), entity.getId(), new TalkTalkVodEntityProcessor<UpdateProgress>() {
    
                UpdateProgress progress = UpdateProgress.SUCCESS;//include the Brand;
                
                @Override
                public UpdateProgress getResult() {
                    return progress;
                }
    
                @Override
                public void process(VODEntityType entity) {
                    switch (entity.getItemType()) {
                    case EPISODE:
                        progress = progress.reduce(processEpisode(extractedBrand, Optional.<Series>absent(), entity));
                        break;
                    case SERIES:
                        progress = progress.reduce(processSeries(extractedBrand, entity));
                        break;
                    default:
                        log.warn("Not processing unexpected entity type {}", entity.getItemType());
                        break;
                    }
                }
                
            }, 500);
        } catch (TalkTalkException tte) {
            log.error(String.format("Failed to entity list for %s %s", entity.getItemType(), entity.getId()), tte);
            return UpdateProgress.FAILURE;
        }
    }
    
    private ItemDetailType getEntityDetail(VODEntityType entity) throws TalkTalkException {
        return client.getItemDetail(entity.getItemType(), entity.getId());
    }

    @Override
    public UpdateProgress processSeriesEntity(VODEntityType entity) {
        return processSeries(Optional.<Brand>absent(), entity);
    }

    private UpdateProgress processSeries(final Optional<Brand> brand, VODEntityType entity) {
        try {
            logProcessing(entity);
            Series series = containerExtractor.extractSeries(getEntityDetail(entity), brand);
            Optional<Identified> existing = getExisting(series.getCanonicalUri());
            if (existing.isPresent()) {
                Identified identifiedExisting = existing.get();
                checkState(identifiedExisting instanceof Series, "%s not Series", existing);
                Container merged = ContentMerger.merge((Series) existing.get(), series);
                series = (Series) merged;
            }
            writer.createOrUpdate(series);
            return processEntityList(series, brand, entity);
        } catch (TalkTalkException tte) {
            log.error(String.format("Failed to entity list for %s %s", entity.getItemType(), entity.getId()), tte);
            return UpdateProgress.FAILURE;
        }
    }

    private UpdateProgress processEntityList(Series series,
            final Optional<Brand> brand, VODEntityType entity) throws TalkTalkException {
            final Optional<Series> extractedSeries = Optional.of((Series)series);
            return client.processVodList(entity.getItemType(), entity.getId(), new TalkTalkVodEntityProcessor<UpdateProgress>() {
    
                UpdateProgress progress = UpdateProgress.SUCCESS;//include the Brand;
                
                @Override
                public UpdateProgress getResult() {
                    return progress;
                }
    
                @Override
                public void process(VODEntityType entity) {
                    switch (entity.getItemType()) {
                    case EPISODE:
                        progress = progress.reduce(processEpisode(brand, extractedSeries, entity));
                        break;
                    default:
                        log.warn("Not processing unexpected entity type {}", entity.getItemType());
                        break;
                    }
                }
                
            }, 500);
    }

    @Override
    public UpdateProgress processEpisodeEntity(VODEntityType entity) {
        return processEpisode(Optional.<Brand>absent(), Optional.<Series>absent(), entity);
    }

    private UpdateProgress processEpisode(Optional<Brand> brand, Optional<Series> series, VODEntityType entity) {
        try {
            logProcessing(entity);
            checkArgument(ItemTypeType.EPISODE.equals(entity.getItemType()));
            ItemDetailType detail = getEntityDetail(entity);
            
            Item item = itemExtractor.extract(detail, brand, series);
            Optional<Identified> existing = getExisting(item.getCanonicalUri());
            item = mergeWithExisting(item, existing);
            
            writer.createOrUpdate(item);
            
            return UpdateProgress.SUCCESS;
        } catch (TalkTalkException tte) {
            String message = String.format("Failed to process %s %s", entity.getItemType(), entity.getId());
            log.error(message,tte);
            return UpdateProgress.FAILURE;
        }
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
