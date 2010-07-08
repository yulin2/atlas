package org.atlasapi.remotesite.bbc.schedule;

import java.io.Reader;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.HttpClients;

import com.metabroadcast.common.http.SimpleHttpClient;


public class BbcScheduleClient implements RemoteSiteClient<ChannelSchedule> {

	private final SimpleHttpClient httpClient;
	
	private final JAXBContext context;

	public BbcScheduleClient() throws JAXBException {
		this(HttpClients.webserviceClient());
	}
	
	BbcScheduleClient(SimpleHttpClient httpClient) throws JAXBException {
		this.httpClient = httpClient;
		context = JAXBContext.newInstance(ChannelSchedule.class);
	}

	public ChannelSchedule get(String uri) throws Exception {
		Reader in = new StringReader(httpClient.getContentsOf(uri));
		Unmarshaller u = context.createUnmarshaller();
		return (ChannelSchedule) u.unmarshal(in);
	}
}