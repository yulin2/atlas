package org.atlasapi.remotesite.globalimageworks;

import static org.atlasapi.remotesite.globalimageworks.GlobalImageSpreadsheetColumn.DATE;
import static org.atlasapi.remotesite.globalimageworks.GlobalImageSpreadsheetColumn.DESCRIPTION;
import static org.atlasapi.remotesite.globalimageworks.GlobalImageSpreadsheetColumn.DURATION;
import static org.atlasapi.remotesite.globalimageworks.GlobalImageSpreadsheetColumn.ID;
import static org.atlasapi.remotesite.globalimageworks.GlobalImageSpreadsheetColumn.KEYWORDS;
import static org.atlasapi.remotesite.globalimageworks.GlobalImageSpreadsheetColumn.SOURCE;
import static org.atlasapi.remotesite.globalimageworks.GlobalImageSpreadsheetColumn.TITLE;

import org.atlasapi.remotesite.globalimageworks.GlobalImageDataRow.Builder;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class GlobalImageAdapter {

private final Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();
    
    public GlobalImageDataRow globalImageDataRow(CustomElementCollection customElements) {
        Builder globalImageData = GlobalImageDataRow.builder();
        
        globalImageData.withDate(customElements.getValue(DATE.getValue()));
        globalImageData.withDescription(customElements.getValue(DESCRIPTION.getValue()));
        globalImageData.withDuration(customElements.getValue(DURATION.getValue()));
        globalImageData.withId(customElements.getValue(ID.getValue()));
        globalImageData.withKeywords(keywords(customElements.getValue(KEYWORDS.getValue())));
        globalImageData.withSource(customElements.getValue(SOURCE.getValue()));
        globalImageData.withTitle(customElements.getValue(TITLE.getValue()));
        
        return globalImageData.build();
    }

    private Iterable<String> keywords(String allKeywords) {
        return allKeywords == null ? ImmutableList.<String>of() : splitter.split(allKeywords);
    }
    
}
