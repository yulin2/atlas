package org.atlasapi.input;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.SameAs;
import org.joda.time.DateTime;

import com.metabroadcast.common.time.Clock;

public abstract class IdentifiedModelTransformer<F extends Description, T extends Identified>
        implements ModelTransformer<F, T> {

    private final Clock clock;

    public IdentifiedModelTransformer(Clock clock) {
        this.clock = checkNotNull(clock);
    }
    
    @Override
    public final T transform(F simple) {
        DateTime now = clock.now();
        T output = createIdentifiedOutput(simple, now);
        output.setLastUpdated(now);
        return setIdentifiedFields(output, simple);
    }

    private T setIdentifiedFields(T output, F simple) {
        output.setCanonicalUri(simple.getUri());
        output.setCurie(simple.getCurie());
        setEquivalents(output, simple);
        return output;
    }

    private void setEquivalents(T output, F simple) {
        if (simple.getEquivalents().isEmpty() && !simple.getSameAs().isEmpty()) {
            output.setEquivalentTo(resolveEquivalents(simple.getSameAs()));
        } else if (!simple.getEquivalents().isEmpty()){
            output.setEquivalentTo(resolveSameAs(simple.getEquivalents()));
        }
    }

    protected abstract Set<LookupRef> resolveSameAs(Set<SameAs> equivalents);

    protected abstract Set<LookupRef> resolveEquivalents(Set<String> sameAs);

    protected abstract T createIdentifiedOutput(F simple, DateTime now);

}
