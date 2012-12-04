package org.atlasapi.query.v4.schedule;

import java.io.IOException;

import org.atlasapi.output.ErrorSummary;

final class ErrorSummaryWriter implements EntityWriter<ErrorSummary> {

    @Override
    public void write(ErrorSummary entity, FieldWriter formatter, OutputContext ctxt) throws IOException {
        formatter.writeField("message", entity.message());
        formatter.writeField("error_code", entity.errorCode());
        formatter.writeField("error_id", entity.id());
    }

    @Override
    public String fieldName() {
        return "error";
    }
}