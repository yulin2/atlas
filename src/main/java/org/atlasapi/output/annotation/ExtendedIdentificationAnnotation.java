package org.atlasapi.output.annotation;


import java.io.IOException;

import org.atlasapi.equiv.EquivalenceRef;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;

import com.metabroadcast.common.ids.NumberToShortStringCodec;


public class ExtendedIdentificationAnnotation extends OutputAnnotation<Identified> {

    private final EntityListWriter<EquivalenceRef> equivalentWriter = new EntityListWriter<EquivalenceRef>() {

        @Override
        public void write(EquivalenceRef entity, FieldWriter formatter, OutputContext ctxt) throws IOException {
            formatter.writeField("id", idCodec.encode(entity.getId().toBigInteger()));
            formatter.writeField("source", entity.getPublisher().key());
        }

        @Override
        public String listName() {
            return "same_as";
        }

        @Override
        public String fieldName() {
            return "equivalent";
        }
    };
    
    private final NumberToShortStringCodec idCodec;

    public ExtendedIdentificationAnnotation(NumberToShortStringCodec idCodec) {
        super(Identified.class);
        this.idCodec = idCodec;
    }

    @Override
    public void write(Identified entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        writer.writeList("aliases", "alias", entity.getAliases(), ctxt);
        writer.writeList(equivalentWriter, entity.getEquivalentTo(), ctxt);
    }
    
}
