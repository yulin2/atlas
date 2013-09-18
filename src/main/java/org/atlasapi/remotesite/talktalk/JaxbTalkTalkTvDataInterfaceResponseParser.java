package org.atlasapi.remotesite.talktalk;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;

import org.atlasapi.remotesite.talktalk.vod.bindings.TVDataInterfaceResponse;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

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
    private final SAXParserFactory factory;

    public JaxbTalkTalkTvDataInterfaceResponseParser(JAXBContext context, Optional<Schema> schema) {
        this.jaxbContext = checkNotNull(context);
        this.schema = checkNotNull(schema);
        this.factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
    }
    
    @Override
    public void parse(Reader reader, Unmarshaller.Listener listener) throws Exception {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        if (schema.isPresent()) {
            unmarshaller.setSchema(schema.get());
        }
        XMLReader xmlReader = factory.newSAXParser().getXMLReader();
        xmlReader.setContentHandler(unmarshaller.getUnmarshallerHandler());
        unmarshaller.setListener(new TVDataInterfaceListenerAdapter(listener));
        try {
            xmlReader.parse(new InputSource(reader));
        } catch(RuntimeTalkTalkException rtte) {
            throw new TalkTalkException(rtte.getMessage());
        }
    }
    
    private final class TVDataInterfaceListenerAdapter extends Unmarshaller.Listener {
        
        private Unmarshaller.Listener delegate;

        public TVDataInterfaceListenerAdapter(Unmarshaller.Listener delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public void beforeUnmarshal(Object target, Object parent) {
            delegate.beforeUnmarshal(target, parent);
        }
        
        @Override
        public void afterUnmarshal(Object target, Object parent) {
            if (target instanceof TVDataInterfaceResponse) {
                TVDataInterfaceResponse resp = (TVDataInterfaceResponse) target;
                if (resp.getResult() != 0) {
                    throw new RuntimeTalkTalkException(resp.getErrorMessage());
                }
            }
            delegate.afterUnmarshal(target, parent);
        }
        
    }
    
    public class RuntimeTalkTalkException extends RuntimeException {

        public RuntimeTalkTalkException(String errorMessage) {
            super(errorMessage);
        }

    }
    
}
