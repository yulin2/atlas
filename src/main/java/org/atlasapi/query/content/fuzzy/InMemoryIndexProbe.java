package org.atlasapi.query.content.fuzzy;

import org.atlasapi.query.content.fuzzy.InMemoryFuzzySearcher.IndexStats;

import com.metabroadcast.common.health.HealthProbe;
import com.metabroadcast.common.health.ProbeResult;
import com.metabroadcast.common.units.ByteCount;

public class InMemoryIndexProbe implements HealthProbe {

	private static final ByteCount MAX_INDEX_SIZE = ByteCount.gibibytes(1);
	
	private final InMemoryFuzzySearcher index;

	public InMemoryIndexProbe(InMemoryFuzzySearcher index) {
		this.index = index;
	}
	
	@Override
	public ProbeResult probe() {
		ProbeResult result = new ProbeResult(title());
		IndexStats stats = index.stats();
		result.addInfo("brands index size", stats.getBrandsIndexSize().prettyPrint());
		result.addInfo("items index size", stats.getItemsIndexSize().prettyPrint());
		result.add("total index size", stats.getTotalIndexSize().prettyPrint(), stats.getTotalIndexSize().isLessThan(MAX_INDEX_SIZE));
		return result;
	}

	@Override
	public String title() {
		return "Lucene index";
	}
}
