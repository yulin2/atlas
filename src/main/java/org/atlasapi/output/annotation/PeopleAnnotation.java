package org.atlasapi.output.annotation;


import java.io.IOException;

import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Actor;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.query.v4.schedule.EntityListWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;


public class PeopleAnnotation extends OutputAnnotation<Content> {

    public PeopleAnnotation() {
        super(Content.class);
    }

    @Override
    public void write(Content entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        writer.writeList(new EntityListWriter<CrewMember>() {

            @Override
            public void write(CrewMember entity, FieldWriter writer, OutputContext ctxt) throws IOException {
                writer.writeField("uri", entity.getCanonicalUri());
                writer.writeField("curie", entity.getCurie());
                writer.writeField("type", "person");
                writer.writeList("aliases", "alias", entity.getAliases(), ctxt);
                writer.writeField("name", entity.name());
                if (entity instanceof Actor) {
                    writer.writeField("character", ((Actor) entity).character());
                }
                
                writer.writeField("role", entity.role().key());
                writer.writeField("display_role", entity.role().title());
            }

            @Override
            public String listName() {
                return "people";
            }

            @Override
            public String fieldName() {
                return "person";
            }
        }, entity.people(), ctxt);
    }

}
