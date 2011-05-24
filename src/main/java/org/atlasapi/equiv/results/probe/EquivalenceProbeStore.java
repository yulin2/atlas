package org.atlasapi.equiv.results.probe;

import java.util.List;

public interface EquivalenceProbeStore {

    void store(EquivalenceResultProbe probe);
    
    EquivalenceResultProbe probeFor(String canonicalUri);
    
    List<EquivalenceResultProbe> probes();
    
}
