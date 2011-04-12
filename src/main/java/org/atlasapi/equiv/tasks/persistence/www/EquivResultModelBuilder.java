package org.atlasapi.equiv.tasks.persistence.www;

import java.util.List;

import org.atlasapi.equiv.tasks.ContainerEquivResult;
import org.atlasapi.equiv.tasks.EquivResult;
import org.atlasapi.equiv.tasks.SuggestedEquivalents;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.model.SimpleModelList;
import com.metabroadcast.common.stats.Count;

public class EquivResultModelBuilder implements ModelBuilder<EquivResult<String>> {

    @Override
    public SimpleModel build(EquivResult<String> target) {
        SimpleModel model = new SimpleModel();
        
        model.put("subject", target.described());
        model.put("fullMatch", target.fullMatch());
        model.put("certainty", target.certainty());
        model.put("suggested", build(target.suggestedEquivalents(), target.certainty()));
        
        if (target instanceof ContainerEquivResult<?, ?>) {
            @SuppressWarnings("unchecked")
            ContainerEquivResult<String, String> containerEquivResult = (ContainerEquivResult<String, String>) target;
            model.put("sub", ImmutableList.copyOf(Iterables.transform(containerEquivResult.getItemResults(), new Function<EquivResult<String>, SimpleModel>() {

                @Override
                public SimpleModel apply(EquivResult<String> input) {
                    return build(input);
                }
            })));
        }
        
        return model;
    }

    private SimpleModelList build(SuggestedEquivalents<String> suggestedEquivalents, double certainty) {
        SimpleModelList model = new SimpleModelList();
        
        List<Count<String>> strong = suggestedEquivalents.allStrongSuggestions(certainty);
        
        for (Count<String> count : suggestedEquivalents.allSuggestions()) {
            model.add(new SimpleModel().put("target", count.getTarget()).put("count", Ints.saturatedCast(count.getCount())).put("strong",strong.contains(count)));
        }
        
        return model;
    }

}
