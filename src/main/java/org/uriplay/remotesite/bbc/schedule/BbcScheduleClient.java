package org.uriplay.remotesite.bbc.schedule;

import java.io.Reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.jherd.remotesite.http.CommonsHttpClient;
import org.jherd.remotesite.http.RemoteSiteClient;


public class BbcScheduleClient implements RemoteSiteClient<ChannelSchedule> {

	private final RemoteSiteClient<Reader> httpClient;
	
	private final JAXBContext context;

	public BbcScheduleClient() throws JAXBException {
		this(new CommonsHttpClient());
	}
	
	BbcScheduleClient(RemoteSiteClient<Reader> httpClient) throws JAXBException {
		this.httpClient = httpClient;
		context = JAXBContext.newInstance(ChannelSchedule.class);
	}

	public ChannelSchedule get(String uri) throws Exception {
		Reader in = httpClient.get(uri);
		Unmarshaller u = context.createUnmarshaller();
		return (ChannelSchedule) u.unmarshal(in);
	}

}