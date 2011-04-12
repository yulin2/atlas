package org.atlasapi.equiv.tasks.persistence;

import org.atlasapi.equiv.tasks.ContainerEquivResult;
import org.atlasapi.equiv.tasks.EquivResult;
import org.atlasapi.media.entity.Described;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class ContainerEquivResultTransformer<I, O extends Comparable<? super O>> {

    private static final Function<Described, String> TRANSFORMER = new Function<Described, String>() {
        @Override
        public String apply(Described input) {
            return String.format("%s/%s", input.getTitle(), input.getCanonicalUri());
        }
    };
    
    public static ContainerEquivResultTransformer<Described, String> defaultTransformer() {
        return new ContainerEquivResultTransformer<Described, String>(TRANSFORMER);
    }

    private final Function<I, O> transformer;

    public ContainerEquivResultTransformer(Function<I, O> transformer) {
        this.transformer = transformer;
    }

    public EquivResult<O> transform(EquivResult<? extends I> input) {
        return new EquivResult<O>(transformer.apply(input.described()), input.fullMatch(), input.suggestedEquivalents().transform(transformer), input.certainty());
    }

    public ContainerEquivResult<O, O> transform(ContainerEquivResult<? extends I, ? extends I> result) {
        EquivResult<O> baseResult = transform((EquivResult<? extends I>)result);
        return new ContainerEquivResult<O, O>(baseResult, Iterables.transform(result.getItemResults(), new Function<EquivResult<? extends I>, EquivResult<O>>() {

            @Override
            public EquivResult<O> apply(EquivResult<? extends I> input) {
                return transform(input);
            }
        }));
    }
}
