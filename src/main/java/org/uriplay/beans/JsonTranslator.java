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
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import org.jherd.beans.BeanGraphWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.uriplay.media.entity.simple.Item;
import org.uriplay.media.entity.simple.Playlist;

import com.google.common.collect.Sets;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Outputs simple URIplay model in plain XML format using JAXB.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class JsonTranslator implements BeanGraphWriter {
    public static final String CALLBACK = "callback";
    @Autowired
    private HttpServletRequest request;

    private Gson gson;

    public JsonTranslator() throws JAXBException {
        gson = new GsonBuilder().setDateFormat(DateFormat.LONG).setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    }

    public void writeTo(Collection<Object> graph, OutputStream stream) {

        Set<Object> processed = Sets.newHashSet();
        String callback = callback(request);

        OutputStreamWriter writer;
        try {
            writer = new OutputStreamWriter(stream, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }

        try {
            if (callback != null) {
                writer.write(callback+"(");
            }
            outputLists(graph, writer, processed);
            outputItems(graph, writer, processed);

            if (callback != null) {
                writer.write(");");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void outputItems(Collection<Object> graph, Writer writer, Set<Object> processed) throws IOException {

        for (Object bean : graph) {

            if (processed.contains(bean)) {
                continue;
            }

            if (bean instanceof Item) {
                writer.write(gson.toJson(bean));
                processed.add(bean);
            }
        }
    }

    private void outputLists(Collection<Object> graph, Writer writer, Set<Object> processed) throws IOException {

        for (Object bean : graph) {

            if (processed.contains(bean)) {
                continue;
            }

            if (bean instanceof Playlist) {
                Playlist playlist = (Playlist) bean;
                writer.write(gson.toJson(bean));
                for (Item item : playlist.getItems()) {
                    processed.add(item);
                }
                processed.add(playlist);
            }
        }
    }

    private String callback(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String callback = request.getParameter(CALLBACK);
        if (callback == null || callback.length() < 1) {
            return null;
        }
        
        try {
            return URLEncoder.encode(callback, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
