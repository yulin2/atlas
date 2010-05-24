/* Copyright 2009 British Broadcasting Corporation
   Copyright 2009 Meta Broadcast Ltd 

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.uriplay.remotesite.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.joda.time.Duration;

/**
 * Simple wrapper for common httpclient, only allowing HEAD and GET requests.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class CommonsHttpClient implements BasicHttpClient {
	
	private String accepts;
	
	private final String USER_AGENT = "Mozilla/5.0 (compatible; uriplay/2.0; +http://uriplay.org)";
	
	private static final DefaultHttpMethodRetryHandler DONT_RETRY = new DefaultHttpMethodRetryHandler(0, false);

	private final static int MAX_CONNECTIONS = 300;
	private long connectionTimeout = Duration.standardSeconds(10).getMillis();
	private  long socketTimeout = Duration.standardSeconds(120).getMillis();
	
	private final MultiThreadedHttpConnectionManager manager;
	private final HttpClient httpClient;

	public CommonsHttpClient() {
		manager = new MultiThreadedHttpConnectionManager();
		manager.getParams().setIntParameter(HttpConnectionManagerParams.SO_TIMEOUT, (int) socketTimeout);
		manager.getParams().setIntParameter(HttpConnectionManagerParams.CONNECTION_TIMEOUT, (int) connectionTimeout);
		manager.getParams().setIntParameter(HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS, MAX_CONNECTIONS);
		manager.getParams().setDefaultMaxConnectionsPerHost(MAX_CONNECTIONS);

		httpClient  = new HttpClient(manager);
		httpClient.getParams().setIntParameter(HttpClientParams.HEAD_BODY_CHECK_TIMEOUT, 3 * 1000);
	    setRetryBehaviour(DONT_RETRY);
	    httpClient.getParams().setIntParameter(HttpClientParams.MAX_REDIRECTS , 5);
	}

	private void setRetryBehaviour(DefaultHttpMethodRetryHandler handler) {
		httpClient.getParams().setParameter(HttpClientParams.RETRY_HANDLER, handler);
	}

	public Reader get(String uri) throws Exception {
		return get(uri, new ResponseFormatter<Reader>() {
			@Override
			public Reader format(HttpMethodBase method) throws IOException {
				String charSet = method.getResponseCharSet();
				if (charSet == null) {
					charSet = "UTF-8";
				}
				return new InputStreamReader(new ByteArrayInputStream(method.getResponseBody()), charSet);
			}
		});
	}
	
	public Reader post(String uri, String content) throws Exception {
		return post(uri, content, new ResponseFormatter<Reader>() {
			@Override
			public Reader format(HttpMethodBase method) throws IOException {
				String charSet = method.getResponseCharSet();
				if (charSet == null) {
					charSet = "UTF-8";
				}
				byte[] response = method.getResponseBody();
				if (response == null) {
					response = new byte[]{};
				}
				return new InputStreamReader(new ByteArrayInputStream(response), charSet);
			}
		});
	}
	
	private <T> T post(String uri, String content, ResponseFormatter<T> formatter) throws Exception {
		
		PostMethod postMethod = new PostMethod(uri);

		postMethod.setRequestHeader("Content-Type", PostMethod.FORM_URL_ENCODED_CONTENT_TYPE);
		setPostMethodBody(content, postMethod);
		
		try {
			httpClient.executeMethod(postMethod);
			closeResponseBody(postMethod);
			
			int statusCode = postMethod.getStatusCode();
			
			if (statusCode == HttpServletResponse.SC_CREATED || statusCode == HttpServletResponse.SC_OK ) {
				return formatter.format(postMethod);
			}
		} catch (Exception e) {
			return formatter.format(postMethod);
		} finally {
			postMethod.releaseConnection();
		}
		
		throw new HttpException(postMethod.getStatusCode(), postMethod.getStatusText());

	}

	@SuppressWarnings("deprecation")
	private void setPostMethodBody(String content, PostMethod postMethod) {
		postMethod.setRequestBody(content);
	}
	
	public TypedData getData(String uri) throws Exception {
		return get(uri, new ResponseFormatter<TypedData>() {
			@Override
			public TypedData format(HttpMethodBase method) throws IOException {
				return new TypedData(responseContentType(method), method.getResponseBody());
			}
		});
	}
	
	private static String responseContentType(HttpMethodBase method) {
		Header header = method.getResponseHeader("Content-type");
		if (header == null) {
			return null;
		}
		return header.getValue();
	}
	
	private <T> T get(String uri, ResponseFormatter<T> formatter) throws Exception {
		
		GetMethod getMethod = new GetMethod(uri);
		
		getMethod.setRequestHeader("User-agent", USER_AGENT);
		if (accepts != null) {
			getMethod.setRequestHeader("Accept", accepts);
		}
		try {
			httpClient.executeMethod(getMethod);
			if (getMethod.getStatusCode() == HttpServletResponse.SC_OK) {
				return formatter.format(getMethod);
			}
		} finally {
			getMethod.releaseConnection();
		}
		throw new HttpException(getMethod.getStatusCode(), getMethod.getStatusText());
	}
	
	public CommonsHttpClient withAcceptHeader(String accepts) {
		this.accepts = accepts;
		return this;
	}

	public HeadMethod head(String uri) {
		
		HeadMethod request = new HeadMethod(uri);
		request.setFollowRedirects(false);
		request.getParams().setParameter(HttpMethodParams.USER_AGENT, USER_AGENT);
		request.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		try {
			httpClient.executeMethod(request);
			closeResponseBody(request);
			return request;
		} catch (Exception e) {
			return null;
		} finally {
			request.releaseConnection();
		}
	}

	private void closeResponseBody(HttpMethodBase request) throws IOException {
		InputStream body = request.getResponseBodyAsStream();
		if (body != null) {
			IOUtils.closeQuietly(body);
		}
	}
	
	public CommonsHttpClient withConnectionTimeout(Duration duration) {
		this.connectionTimeout = duration.getMillis();
		return this;
	}
	
	public CommonsHttpClient withSocketTimeout(Duration duration) {
		this.socketTimeout = duration.getMillis();
		return this;
	}
	
	public CommonsHttpClient withRetries(int retries) {
		setRetryBehaviour(new DefaultHttpMethodRetryHandler(retries, true));
		return this;
	}
	
	private static interface ResponseFormatter<T> {
		
		public T format(HttpMethodBase executedMethod) throws IOException;
		
	}
	
	public CommonsHttpClient withBasicCredentials(String username, String password) {
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
		httpClient.getParams().setAuthenticationPreemptive(true);
		httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY), creds);
		return this;
	}
 }