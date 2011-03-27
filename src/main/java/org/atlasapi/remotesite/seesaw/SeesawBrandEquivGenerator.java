package org.atlasapi.remotesite.seesaw;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.PreDestroy;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Equiv;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.atlasapi.remotesite.EquivGenerator;
import org.joda.time.Duration;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.caching.BackgroundComputingValue;
import com.metabroadcast.common.persistence.mongo.MongoQueryBuilder;

public class SeesawBrandEquivGenerator implements EquivGenerator<Container<?>> {
    
    private final BackgroundComputingValue<Map<BrandEquivKey, String>> brandKeys;

    public SeesawBrandEquivGenerator(MongoDbBackedContentStore retrospectiveContentLister) {
        this.brandKeys = new BackgroundComputingValue<Map<BrandEquivKey, String>>(Duration.standardHours(1), new BrandEquivKeyGenerator(retrospectiveContentLister));
        this.brandKeys.start();
    }
    
    @PreDestroy
    public void shutDown() {
        this.brandKeys.shutdown();
    }

    @Override
    public List<Equiv> equivalent(Container<?> content) {
        if (Publisher.SEESAW != content.getPublisher()) {
            Maybe<BrandEquivKey> fromBrand = BrandEquivKey.fromBrand(content);
            if (fromBrand.hasValue()) {
                
                String equivUri = brandKeys.get().get(fromBrand.requireValue());
                if (equivUri != null) {
                    return ImmutableList.of(new Equiv(content.getCanonicalUri(), equivUri));
                }
            }
        }
        
        return ImmutableList.of();
    }

    class BrandEquivKeyGenerator implements Callable<Map<BrandEquivKey, String>> {
        
        private final MongoDbBackedContentStore retroLister;
        private final MongoQueryBuilder query = where().fieldEquals("publisher", Publisher.SEESAW.key());
        private static final int BATCH_SIZE = 10;

        public BrandEquivKeyGenerator(MongoDbBackedContentStore retroLister) {
            this.retroLister = retroLister;
        }
        
        @Override
        public Map<BrandEquivKey, String> call() throws Exception {
            Map<BrandEquivKey, String> map = Maps.newHashMap();
            
            String lastId = null;
            List<Content> contents;
            do {
                contents = retroLister.iterateOverContent(query, lastId, -BATCH_SIZE);
                for (Brand brand : Iterables.filter(contents, Brand.class)) {
                    Maybe<BrandEquivKey> equivKey = BrandEquivKey.fromBrand(brand);
                    if (equivKey.hasValue()) {
                        map.put(equivKey.requireValue(), brand.getCanonicalUri());
                    }
                }
                lastId = contents.isEmpty() ? lastId : Iterables.getLast(contents).getCanonicalUri();
            } while (!contents.isEmpty());
            
            return ImmutableMap.copyOf(map);
        }
    }
    
    static class BrandEquivKey {
        
        private final String flattenedTitle;
        private final Set<Integer> seriesNumbers;
        
        public static Maybe<BrandEquivKey> fromBrand(Container<?> container) {
            if (container instanceof Brand) {
                Brand brand = (Brand) container;
                ImmutableSet.Builder<Integer> seriesNumbersBuilder = ImmutableSet.builder();
                for (Episode episode: brand.getContents()) {
                    if (episode.getSeriesNumber() != null) {
                        seriesNumbersBuilder.add(episode.getSeriesNumber());
                    }
                }
                
                Set<Integer> seriesNumbers = seriesNumbersBuilder.build();
                if (! Strings.isNullOrEmpty(brand.getTitle()) && ! seriesNumbers.isEmpty()) {
                    String flattened = brand.getTitle().replaceAll("\\W", "").toLowerCase();
                    if (! Strings.isNullOrEmpty(flattened)) {
                        return Maybe.just(new BrandEquivKey(flattened, seriesNumbers));
                    }
                }
            }
            
            return Maybe.nothing();
        }
        
        protected BrandEquivKey(String title, Set<Integer> seriesNumbers) {
            this.flattenedTitle = title;
            this.seriesNumbers = seriesNumbers;
        }
        
        @Override
        public int hashCode() {
            return Objects.hashCode(flattenedTitle);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BrandEquivKey) {
                BrandEquivKey target = (BrandEquivKey) obj;
                return flattenedTitle.equals(target.flattenedTitle);
            }
            return false;
        }
    }
}
