package org.atlasapi.application.writers;

import java.io.IOException;

import org.atlasapi.application.ApplicationCredentials;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.net.IpRange;


public class ApplicationCredentialsWriter implements EntityListWriter<ApplicationCredentials> {

    @Override
    public void write(ApplicationCredentials entity, FieldWriter writer, OutputContext ctxt)
            throws IOException {
        writer.writeField("apiKey", entity.getApiKey());
        writer.writeList("ipRanges", "ipRanges", ImmutableList.copyOf(Iterables.transform(entity.getIpAddressRanges(), new Function<IpRange, String>(){

            @Override
            public String apply(IpRange range) {
                return range.toFriendlyString();
            }
            
        })), ctxt);
    }

    @Override
    public String fieldName(ApplicationCredentials entity) {
        return "credentials";
    }

    @Override
    public String listName() {
        return "credentials";
    }

}
