package org.atlasapi.application.writers;

import java.io.IOException;
import java.math.BigInteger;
import org.atlasapi.application.users.User;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.output.EntityWriter;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;
import org.atlasapi.output.writers.SourceWriter;
import org.atlasapi.query.common.Resource;

import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.social.model.UserRef;


public class UsersListWriter implements EntityListWriter<User> {
    private final EntityWriter<UserRef> userRefWriter = UserRefWriter.userRefWriter("userRef");
    private final EntityListWriter<Publisher> sourcesWriter = SourceWriter.sourceListWriter("sources");
    private final NumberToShortStringCodec idCodec;
    

    public UsersListWriter(NumberToShortStringCodec idCodec) {
        this.idCodec = idCodec;
    }

    @Override
    public void write(User entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        ctxt.startResource(Resource.APPLICATION);
        writer.writeField("id", idCodec.encode(BigInteger.valueOf(entity.getId())));
        writer.writeObject(userRefWriter, entity.getUserRef(), ctxt);
        writer.writeField("screen_name", entity.getScreenName());
        writer.writeField("full_name", entity.getFullName());
        writer.writeField("company", entity.getCompany());
        writer.writeField("email", entity.getEmail());
        writer.writeField("website", entity.getWebsite());
        writer.writeField("profile_image", entity.getProfileImage());
        writer.writeList("applications", "application", entity.getApplications(), ctxt);
        writer.writeList(sourcesWriter, entity.getSources(), ctxt);
        writer.writeField("role", entity.getRole().toString().toLowerCase());
        writer.writeField("profile_complete", entity.isProfileComplete());
        ctxt.endResource();
    }

    @Override
    public String fieldName(User entity) {
        return Resource.USER.getSingular();
    }

    @Override
    public String listName() {
        return Resource.USER.getPlural();
    }

}
