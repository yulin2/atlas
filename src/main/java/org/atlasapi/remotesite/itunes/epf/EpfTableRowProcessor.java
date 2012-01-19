package org.atlasapi.remotesite.itunes.epf;

import org.atlasapi.remotesite.itunes.epf.model.EpfTableRow;

public interface EpfTableRowProcessor<ROW extends EpfTableRow,RESULT> {

    boolean process(ROW row);
    
    RESULT getResult();
    
}
