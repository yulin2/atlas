package org.atlasapi.equiv.results.www;

import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.equiv.results.persistence.EquivalenceIdentifier;
import org.atlasapi.equiv.results.persistence.RestoredEquivalenceResult;
import org.eclipse.jetty.util.UrlEncoded;

import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.model.SimpleModelList;
import com.metabroadcast.common.time.DateTimeZones;

public class RestoredEquivalenceResultModelBuilder implements ModelBuilder<RestoredEquivalenceResult> {

    @Override
    public SimpleModel build(RestoredEquivalenceResult target) {
        SimpleModel model = new SimpleModel();
        
        model.put("id", target.id());
        model.put("encodedId", UrlEncoded.encodeString(target.id()));
        model.put("title", target.title());
        model.put("time", target.resultTime().toDateTime(DateTimeZones.LONDON).toString("YYYY-MM-dd HH:mm:ss"));
        
        boolean hasStrong = false;
        
        SimpleModelList equivalences = new SimpleModelList();
        for (Entry<EquivalenceIdentifier, Double> equivalence : target.combinedResults().entrySet()) {
            SimpleModel equivModel = new SimpleModel();
            
            EquivalenceIdentifier key = equivalence.getKey();
            
            equivModel.put("id",key.id());
            equivModel.put("encodedId",UrlEncoded.encodeString(key.id()));
            equivModel.put("title", key.title());
            equivModel.put("strong", key.strong());
            equivModel.put("publisher", key.publisher());
            equivModel.put("scores", scores(equivalence.getValue(), target.sourceResults().row(key.id())));
            
            hasStrong |= key.strong();
            
            equivalences.add(equivModel);
        }
        model.put("equivalences", equivalences);
        model.putStrings("sources", target.sourceResults().columnKeySet());
        
        return model;
    }

    private SimpleModel scores(Double value, Map<String, Double> row) {
        SimpleModel scoreModel = new SimpleModel().put("combined", format(value));
        for (Entry<String, Double> sourceScore : row.entrySet()) {
            scoreModel.put(sourceScore.getKey(), format(sourceScore.getValue()));
        }
        return scoreModel;
    }
    
    private String format(Double d) {
        return String.format("%+.5f", d);
    }
    
}
