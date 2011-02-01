package org.atlasapi.query.content.fuzzy;

import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.search.model.SearchResults;
import org.atlasapi.search.model.SearchResultsError;

import com.google.gson.Gson;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponse;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.url.UrlEncoding;

public class RemoteFuzzySearcher implements FuzzySearcher {

	private final SimpleHttpClient client = HttpClients.webserviceClient();
	private final Gson gson = new Gson();
	private final String remoteHost;
	
	public RemoteFuzzySearcher(String remoteHost) {
		this.remoteHost = remoteHost;
	}
	
	@Override
	public SearchResults contentSearch(String title, Selection selection) {
		try {
			HttpResponse response = client.get(remoteHost + "/titles?title=" + UrlEncoding.encode(title) + "&" + selection.asQueryParameters());
			if (HttpStatusCode.OK.is(response.statusCode())) {
				return gson.fromJson(response.body(), SearchResults.class);
			}
			throw new RemoteFuzzySearcherException(gson.fromJson(response.body(), SearchResultsError.class));
		} catch (HttpException e) {
			throw new RuntimeException(e);
		}
	}
	
	static class RemoteFuzzySearcherException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		
		public RemoteFuzzySearcherException(SearchResultsError error) {
			super(error.getMessage());
		}
	}
}
