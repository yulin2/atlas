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

package org.uriplay.beans.view;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;
import org.uriplay.beans.BeanGraphWriter;
import org.uriplay.media.reference.entity.MimeType;
import org.uriplay.persistence.servlet.RequestNs;

public class BeanGraphWriterView implements View {

	private final BeanGraphWriter writer;

	private final MimeType contentType;
	
	public String getContentType() {
		return contentType.toString();
	}

	public BeanGraphWriterView(MimeType contentType, BeanGraphWriter writer) {
		this.contentType = contentType;
		this.writer = writer;
	}

	@SuppressWarnings("unchecked")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		response.setContentType(getContentType());
		response.setCharacterEncoding("utf-8");
		
		if (model != null) {
			
			Collection<Object> beans = beanGraph(model);
			writer.writeTo(beans, response.getOutputStream());

		} else {
			response.sendError(HttpServletResponse.SC_NO_CONTENT);
		}
	}

	protected Collection<Object> beanGraph(Map<String, Object> model) {
		return (Collection<Object>) model.get(RequestNs.GRAPH);
	}
}
