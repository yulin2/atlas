package org.atlasapi.remotesite.bloomberg;

import static org.atlasapi.remotesite.bloomberg.BloombergSpreadsheetColumn.DATE;
import static org.atlasapi.remotesite.bloomberg.BloombergSpreadsheetColumn.DESCRIPTION;
import static org.atlasapi.remotesite.bloomberg.BloombergSpreadsheetColumn.DURATION;
import static org.atlasapi.remotesite.bloomberg.BloombergSpreadsheetColumn.ID;
import static org.atlasapi.remotesite.bloomberg.BloombergSpreadsheetColumn.KEYWORDS;
import static org.atlasapi.remotesite.bloomberg.BloombergSpreadsheetColumn.SOURCE;
import static org.atlasapi.remotesite.bloomberg.BloombergSpreadsheetColumn.TITLE;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class BloombergAdapterTest {

    private static final BloombergAdapter adapter = new BloombergAdapter();
    
    private CustomElementCollection customElements() {
        CustomElementCollection customElements = new CustomElementCollection();
        customElements.setValueLocal(DATE.getValue(), "date");
        customElements.setValueLocal(DESCRIPTION.getValue(), "description");
        customElements.setValueLocal(DURATION.getValue(), "duration");
        customElements.setValueLocal(ID.getValue(), "id");
        customElements.setValueLocal(KEYWORDS.getValue(), "keywords");
        customElements.setValueLocal(SOURCE.getValue(), "source");
        customElements.setValueLocal(TITLE.getValue(), "title");
        return customElements;
    }

    @Test
    public void testRowConversionFromCustomElements() {
        BloombergDataRow bloombergDataRow = adapter.bloombergDataRow(customElements());
        assertEquals("date", bloombergDataRow.getDate());
        assertEquals("description", bloombergDataRow.getDescription());
        assertEquals("duration", bloombergDataRow.getDuration());
        assertEquals("id", bloombergDataRow.getId());
        assertEquals("source", bloombergDataRow.getSource());
        assertEquals("title", bloombergDataRow.getTitle());
        assertEquals(ImmutableList.of("keywords"), bloombergDataRow.getKeywords());
    }
    
}
