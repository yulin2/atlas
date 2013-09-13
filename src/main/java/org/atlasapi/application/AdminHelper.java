package org.atlasapi.application;

import java.util.List;
import java.util.UUID;

import org.atlasapi.application.model.PrecedenceOrdering;
import org.atlasapi.application.sources.SourceIdCodec;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.query.common.QueryExecutionException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.ids.NumberToShortStringCodec;

/**
 * Utility functions for administration
 * @author liam
 *
 */
public class AdminHelper {

    private final NumberToShortStringCodec idCodec;
    private final SourceIdCodec sourceIdCodec;
    
    public AdminHelper(NumberToShortStringCodec idCodec, SourceIdCodec sourceIdCodec) {
        this.idCodec = idCodec;
        this.sourceIdCodec = sourceIdCodec;
    }

    public Id decode(String encoded) {
        return Id.valueOf(idCodec.decode(encoded));
    }
    
    public Optional<Publisher> decodeSourceId(String encoded) {
        Maybe<Publisher> publisher = sourceIdCodec.decode(encoded);
        return Optional.fromNullable(publisher.valueOrNull());
    }
    
    public List<Publisher> getSourcesFrom(PrecedenceOrdering ordering) throws QueryExecutionException {
        ImmutableList.Builder<Publisher> sources =ImmutableList.builder();
        for (String sourceId : ordering.getOrdering()) {
            Maybe<Publisher> source = sourceIdCodec.decode(sourceId);
            if (source.hasValue()) {
                sources.add(source.requireValue());
            } else {
                throw new QueryExecutionException("No publisher by id " + sourceId);
            }
        }
        return sources.build();
    }
    
    // For compatibility with 3.0
    public String generateSlug(Id id) {
        return "app-" + idCodec.encode(id.toBigInteger());
    }
    
    public String generateApiKey() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
