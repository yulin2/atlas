package org.atlasapi.output.annotation;

import static org.atlasapi.output.Annotation.EXTENDED_IDENTIFICATION;

import java.io.IOException;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.query.v4.schedule.EntityListWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;

public class ExtendedIdentificationAnnotation extends OutputAnnotation<Identified> {

    private static final EntityListWriter<LookupRef> equivalentWriter = new EntityListWriter<LookupRef>() {

        @Override
        public void write(LookupRef entity, FieldWriter formatter) throws IOException {
            formatter.writeField("uri", entity.id());
            formatter.writeField("source", entity.publisher().key());
        }

        @Override
        public String listName() {
            return "same_as";
        }

        @Override
        public String elementName() {
            return "equivalent";
        }
    };

    public ExtendedIdentificationAnnotation(IdentificationAnnotation idAnnotation) {
        super(EXTENDED_IDENTIFICATION, Identified.class, ImmutableSet.of(idAnnotation));
    }

    @Override
    public void write(Identified entity, FieldWriter writer) throws IOException {
        writer.writeField("curie", entity.getCurie());
        writer.writeList("aliases", "alias", entity.getAliases());
        writer.writeList(equivalentWriter, entity.getEquivalentTo());
    }
    
}
