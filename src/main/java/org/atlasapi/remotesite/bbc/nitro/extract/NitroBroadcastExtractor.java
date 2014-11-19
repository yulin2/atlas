package org.atlasapi.remotesite.bbc.nitro.extract;

import java.util.List;

import javax.annotation.Nullable;

import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.ion.BbcIonServices;
import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.atlas.glycerin.model.Id;
import com.metabroadcast.atlas.glycerin.model.Ids;

/**
 * Extracts a {@link com.metabroadcast.atlas.glycerin.model.Broadcast Atlas
 * Broadcast} from a {@link Broadcast Nitro Broadcast}.
 */
public class NitroBroadcastExtractor
    implements ContentExtractor<com.metabroadcast.atlas.glycerin.model.Broadcast, Optional<Broadcast>> {

    private static final String TERRESTRIAL_EVENT_LOCATOR_TYPE = "terrestrial_event_locator";
    private static final String TERRESTRIAL_PROGRAM_CRID_TYPE = "terrestrial_programme_crid";

    @Override
    public Optional<Broadcast> extract(com.metabroadcast.atlas.glycerin.model.Broadcast source) {
        String channel = BbcIonServices.get(source.getService().getSid());
        if (channel == null) {
            return Optional.absent();
        }
        DateTime start = NitroUtil.toDateTime(source.getPublishedTime().getStart());
        DateTime end = NitroUtil.toDateTime(source.getPublishedTime().getEnd());
        Broadcast broadcast = new Broadcast(channel, start, end)
            .withId("bbc:"+source.getPid());
        broadcast.setRepeat(source.isIsRepeat());
        broadcast.setAudioDescribed(source.isIsAudioDescribed());
        broadcast.setAliases(extractAliasesFrom(source));
        broadcast.setAudioDescribed(source.isIsAudioDescribed());
        return Optional.of(broadcast);
    }

    private Iterable<Alias> extractAliasesFrom(
            com.metabroadcast.atlas.glycerin.model.Broadcast source) {
        Ids ids = source.getIds();

        if (ids == null) {
            return ImmutableList.of();
        }

        return aliasesForTypes(ids.getId(), TERRESTRIAL_EVENT_LOCATOR_TYPE, TERRESTRIAL_PROGRAM_CRID_TYPE);
    }

    private String namespaceForType(String type) {
        return "bbc:" + type;
    }

    private Predicate<Id> idOfType(final String type) {
        return new Predicate<Id>() {
            @Override
            public boolean apply(Id input) {
                return type.equals(input.getType());
            }
        };
    }

    private Iterable<Alias> aliasesForTypes(List<Id> ids, String... types) {
        ImmutableList.Builder<Alias> aliases = ImmutableList.builder();

        for (String type: types) {
            Optional<Id> id = Iterables.tryFind(ids, idOfType(type));

            if (id.isPresent()) {
                aliases.add(new Alias(namespaceForType(type), id.get().getValue()));
            }
        }

        return aliases.build();
    }

}
