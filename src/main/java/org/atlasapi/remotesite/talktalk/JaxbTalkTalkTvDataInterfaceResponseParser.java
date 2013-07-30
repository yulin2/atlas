package org.atlasapi.remotesite.talktalk;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;

import org.atlasapi.remotesite.talktalk.vod.bindings.TVDataInterfaceResponse;

import com.google.common.base.Optional;

/**
 * Parses XML from TalkTalk using a JAXBContext Unmarshaller.
 * 
 * Validation is performed if a schema is provided.
 *
 */
public class JaxbTalkTalkTvDataInterfaceResponseParser implements TalkTalkTvDataInterfaceResponseParser {
    
    private final Optional<Schema> schema;
    private final JAXBContext jaxbContext;

    public JaxbTalkTalkTvDataInterfaceResponseParser(JAXBContext context, Optional<Schema> schema) {
        this.jaxbContext = checkNotNull(context);
        this.schema = checkNotNull(schema);
    }
    
    @Override
    public TVDataInterfaceResponse parse(Reader reader, Unmarshaller.Listener listener) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        if (schema.isPresent()) {
            unmarshaller.setSchema(schema.get());
        }
        unmarshaller.setListener(listener);
        return (TVDataInterfaceResponse) unmarshaller.unmarshal(reader);
    }
    
}
