package org.atlasapi.output.annotation;

import java.io.IOException;

import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;


public class LicenseWriter extends OutputAnnotation<Void> {

    public LicenseWriter() {
        super(Void.class);
    }

    @Override
    public void write(Void entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        // TODO Auto-generated method stub
    }

}
