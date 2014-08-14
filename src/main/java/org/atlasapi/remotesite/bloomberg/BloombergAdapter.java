package org.atlasapi.remotesite.bloomberg;

import static org.atlasapi.remotesite.bloomberg.BloombergSpreadsheetColumn.DATE;
import static org.atlasapi.remotesite.bloomberg.BloombergSpreadsheetColumn.DESCRIPTION;
import static org.atlasapi.remotesite.bloomberg.BloombergSpreadsheetColumn.DURATION;
import static org.atlasapi.remotesite.bloomberg.BloombergSpreadsheetColumn.ID;
import static org.atlasapi.remotesite.bloomberg.BloombergSpreadsheetColumn.KEYWORDS;
import static org.atlasapi.remotesite.bloomberg.BloombergSpreadsheetColumn.SOURCE;
import static org.atlasapi.remotesite.bloomberg.BloombergSpreadsheetColumn.TITLE;

import org.atlasapi.remotesite.bloomberg.BloombergDataRow.Builder;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class BloombergAdapter {

    private final Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();
    
    public BloombergDataRow bloombergDataRow(CustomElementCollection customElements) {
        Builder bloombergData = BloombergDataRow.builder();
        
        bloombergData.withDate(customElements.getValue(DATE.getValue()));
        bloombergData.withDescription(customElements.getValue(DESCRIPTION.getValue()));
        bloombergData.withDuration(customElements.getValue(DURATION.getValue()));
        bloombergData.withId(customElements.getValue(ID.getValue()));
        bloombergData.withKeywords(keywords(customElements.getValue(KEYWORDS.getValue())));
        bloombergData.withSource(customElements.getValue(SOURCE.getValue()));
        bloombergData.withTitle(customElements.getValue(TITLE.getValue()));
        
        return bloombergData.build();
    }

    private Iterable<String> keywords(String allKeywords) {
        return allKeywords == null ? ImmutableList.<String>of() : splitter.split(allKeywords);
    }
    
}
