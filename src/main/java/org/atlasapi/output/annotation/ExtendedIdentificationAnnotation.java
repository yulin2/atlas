package org.atlasapi.output.annotation;


import java.io.IOException;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.query.v4.schedule.EntityListWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;


public class ExtendedIdentificationAnnotation extends OutputAnnotation<Identified> {

    private static final EntityListWriter<LookupRef> equivalentWriter = new EntityListWriter<LookupRef>() {

        @Override
        public void write(LookupRef entity, FieldWriter formatter, OutputContext ctxt) throws IOException {
            formatter.writeField("uri", entity.id());
            formatter.writeField("source", entity.publisher().key());
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

    public ExtendedIdentificationAnnotation() {
        super(Identified.class);
    }

    @Override
    public void write(Identified entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        writer.writeList("aliases", "alias", entity.getAliases(), ctxt);
        writer.writeList(equivalentWriter, entity.getEquivalentTo(), ctxt);
    }
    
}
