/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.uriplay.beans;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.uriplay.media.entity.simple.Broadcast;
import org.uriplay.media.entity.simple.Item;
import org.uriplay.media.entity.simple.Location;
import org.uriplay.media.entity.simple.Playlist;
import org.uriplay.media.entity.simple.UriplayQueryResult;
import org.uriplay.media.vocabulary.PLAY;
import org.uriplay.media.vocabulary.PO;
import org.xml.sax.SAXException;

import com.google.common.collect.Iterables;
import com.hp.hpl.jena.vocabulary.DC;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Outputs simple URIplay model in plain XML format using JAXB.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class JaxbXmlTranslator implements BeanGraphWriter {

	private JAXBContext context;

	public JaxbXmlTranslator() throws JAXBException {
		context = JAXBContext.newInstance(UriplayQueryResult.class, Playlist.class, Item.class, Location.class, Broadcast.class);
	}
	
	public void writeTo(Collection<Object> graph, OutputStream stream) {
		
		try {
			Marshaller m = context.createMarshaller();
			m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new UriplayNamespacePrefixMapper());

			XMLSerializer serializer = getXMLSerializer(stream);
			m.marshal(Iterables.getOnlyElement(graph), serializer.asContentHandler());
			
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	private static XMLSerializer getXMLSerializer(OutputStream oStream) throws SAXException {
     
        OutputFormat of = new OutputFormat();

        of.setCDataElements(new String[] { "^embedCode" });  
        
        XMLSerializer serializer = new XMLSerializer(of);
        serializer.setOutputByteStream(oStream);

        return serializer;
    }
	
	private static final class UriplayNamespacePrefixMapper extends NamespacePrefixMapper {
		@Override
		public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
			if (PLAY.NS.equals(namespaceUri)) {
				return PLAY.PREFIX;
			} else if (PO.NS.equals(namespaceUri)) {
				return PO.PREFIX;
			} else if (DC.NS.equals(namespaceUri)) {
				return "dc";
			}
			return null;
		}

		@Override
		public String[] getPreDeclaredNamespaceUris() {
		    return new String[] { PLAY.NS , PO.NS, "dc" };
		}
	}


}
