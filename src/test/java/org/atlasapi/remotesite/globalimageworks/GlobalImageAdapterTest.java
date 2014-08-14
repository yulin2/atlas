package org.atlasapi.remotesite.globalimageworks;

import static org.atlasapi.remotesite.globalimageworks.GlobalImageSpreadsheetColumn.DATE;
import static org.atlasapi.remotesite.globalimageworks.GlobalImageSpreadsheetColumn.DESCRIPTION;
import static org.atlasapi.remotesite.globalimageworks.GlobalImageSpreadsheetColumn.DURATION;
import static org.atlasapi.remotesite.globalimageworks.GlobalImageSpreadsheetColumn.ID;
import static org.atlasapi.remotesite.globalimageworks.GlobalImageSpreadsheetColumn.KEYWORDS;
import static org.atlasapi.remotesite.globalimageworks.GlobalImageSpreadsheetColumn.SOURCE;
import static org.atlasapi.remotesite.globalimageworks.GlobalImageSpreadsheetColumn.TITLE;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class GlobalImageAdapterTest {

private static final GlobalImageAdapter adapter = new GlobalImageAdapter();
    
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
        GlobalImageDataRow globalImageDataRow = adapter.globalImageDataRow(customElements());
        assertEquals("date", globalImageDataRow.getDate());
        assertEquals("description", globalImageDataRow.getDescription());
        assertEquals("duration", globalImageDataRow.getDuration());
        assertEquals("id", globalImageDataRow.getId());
        assertEquals("source", globalImageDataRow.getSource());
        assertEquals("title", globalImageDataRow.getTitle());
        assertEquals(ImmutableList.of("keywords"), globalImageDataRow.getKeywords());
    }
    
}
