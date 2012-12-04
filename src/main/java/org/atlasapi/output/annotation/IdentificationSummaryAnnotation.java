package org.atlasapi.output.annotation;

import java.io.IOException;
import java.math.BigInteger;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;

import com.metabroadcast.common.ids.NumberToShortStringCodec;

public class IdentificationSummaryAnnotation extends OutputAnnotation<Identified> {

    private final NumberToShortStringCodec codec;

    public IdentificationSummaryAnnotation(NumberToShortStringCodec codec) {
        super(Identified.class);
        this.codec = codec;
    }

    @Override
    public void write(Identified entity, FieldWriter formatter, OutputContext ctxt) throws IOException {
        formatter.writeField("id", encodedIdOrNull(entity));        
    }

    private String encodedIdOrNull(Identified entity) {
        return entity.getId() != null ? codec.encode(BigInteger.valueOf(entity.getId()))
                                      : null;
    }
}
