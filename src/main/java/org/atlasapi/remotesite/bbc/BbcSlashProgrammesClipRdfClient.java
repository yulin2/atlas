package org.atlasapi.remotesite.bbc;

import java.io.Reader;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.atlasapi.remotesite.HttpClients;

import com.metabroadcast.common.http.SimpleHttpClient;

public class BbcSlashProgrammesClipRdfClient {

    private final SimpleHttpClient httpClient;
    private final JAXBContext context;

    public BbcSlashProgrammesClipRdfClient() {
        this(HttpClients.webserviceClient());
    }
    
    public BbcSlashProgrammesClipRdfClient(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
        try {
            context = JAXBContext.newInstance(SlashProgrammesRdf.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public SlashProgrammesRdf get(String uri) throws Exception {
        Reader in = new StringReader(httpClient.getContentsOf(uri));
        Unmarshaller u = context.createUnmarshaller();
        SlashProgrammesRdf clipDescription = (SlashProgrammesRdf) u.unmarshal(in);
        return clipDescription;
    }
}
