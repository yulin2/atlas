package org.atlasapi.query.v4.schedule;

import java.io.IOException;

public interface ResponseWriter extends FieldWriter {

    void startResponse() throws IOException;

    void finishResponse() throws IOException;

}
