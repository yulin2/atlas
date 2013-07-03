package org.atlasapi.remotesite.talktalk;

import org.atlasapi.remotesite.talktalk.vod.bindings.ItemTypeType;

import com.google.common.base.Functions;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.metabroadcast.common.collect.ImmutableOptionalMap;
import com.metabroadcast.common.collect.OptionalMap;

/**
 * Provides a way to convert between a groupType for requests to the VOD API and
 * ItemTypeTypes returned in VODEntities, if there is an equivalent.
 * 
 */
public enum GroupType {
    
    BRAND(ItemTypeType.BRAND),
    SERIES(ItemTypeType.SERIES),
    EPISODE(ItemTypeType.EPISODE),
    BROADCAST_ITEM(ItemTypeType.BROADCAST_ITEM),
    CHANNEL(ItemTypeType.CHANNEL),
    LOCATION(ItemTypeType.LOCATION),
    IMGWALL(ItemTypeType.IMGWALL),
    IMAGE(null)
    ;
    
    private final Optional<ItemTypeType> itemType;

    private GroupType(ItemTypeType itemType) {
        this.itemType = Optional.fromNullable(itemType);
    }

    public Optional<ItemTypeType> getItemType() {
        return itemType;
    }
    
    private static final ImmutableSet<GroupType> ALL
        = ImmutableSet.copyOf(values());
    
    public static final ImmutableSet<GroupType> all() {
        return ALL;
    }
    
    private static final Function<GroupType, Optional<ItemTypeType>> toItemType
        = new Function<GroupType, Optional<ItemTypeType>>() {
            @Override
            public Optional<ItemTypeType> apply(GroupType input) {
                return input.getItemType();
            }
    };
    
    private static final Predicate<GroupType> hasItemTypeType 
        = new Predicate<GroupType>() {
            
            @Override
            public boolean apply(GroupType input) {
                return input.getItemType().isPresent();
            }
        };
    
    
    private static final OptionalMap<ItemTypeType, GroupType> itemTypeIndex = 
            ImmutableOptionalMap.fromMap(Maps.uniqueIndex(Iterables.filter(all(), hasItemTypeType),
                Functions.compose(new Function<Optional<ItemTypeType>, ItemTypeType>() {
                    @Override
                    public ItemTypeType apply(Optional<ItemTypeType> input) {
                        return input.get();
                    }
                }, toItemType)));
    
    public static final Optional<GroupType> fromItemType(ItemTypeType type) {
        return itemTypeIndex.get(type);
    }
}
