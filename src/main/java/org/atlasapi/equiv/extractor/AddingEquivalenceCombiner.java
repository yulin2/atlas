package org.atlasapi.equiv.extractor;

import java.util.HashMap;
import java.util.Map;

import org.atlasapi.media.entity.Content;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class AddingEquivalenceCombiner<T extends Content> implements EquivalenceCombiner<T> {

    public static <T extends Content> AddingEquivalenceCombiner<T> create() {
        return new AddingEquivalenceCombiner<T>();
    }
    
    @Override
    public Map<T, Double> combine(Map<T, Double> left, Map<T, Double> right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        HashMap<T, Double> combined = Maps.newHashMap();
        for (T equiv : ImmutableSet.copyOf(Iterables.concat(left.keySet(), right.keySet()))) {
            combined.put(equiv, add(left.get(equiv),right.get(equiv)));
        }
        return combined;
    }

    private Double add(Double left, Double right) {
        return left != null ? (right != null ? left + right : left) : right;
    }

}
