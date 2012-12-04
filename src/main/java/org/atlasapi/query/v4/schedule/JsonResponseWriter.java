package org.atlasapi.query.v4.schedule;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * This class is not thread-safe.
 */
public final class JsonResponseWriter implements ResponseWriter {
    
    //temporary.
    private static final ObjectMapper stringSerializer = new ObjectMapper(); 

    //private static final char STRING_DELIMITER = '"';
    private static final String NULL_VALUE = "null";
    private static final char PAIR_SEPARATOR = ':';
    private static final char ELEMENT_SEPARTOR = ',';
    private static final char MEMBER_SEPARATOR = ',';
    private static final char START_ARRAY = '[';
    private static final char END_ARRAY = ']';
    private static final char START_OBJECT = '{';
    private static final char END_OBJECT = '}';

    private boolean printMemberSeparator = false;

    private final HttpServletResponse response;
    private final HttpServletRequest request;

    public JsonResponseWriter(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }
    
    private Writer writer() throws IOException {
        return response.getWriter();
    }
    
    @Override
    public void startResponse() throws IOException {
        //TODO JSONP
        writer().write(START_OBJECT);
    }
    
    @Override
    public void finishResponse() throws IOException {
        writer().write(END_OBJECT);
        //TODO: JSONP
    }

    @Override
    public void writeField(String field, Object obj) throws IOException {
        startField(field);
        if (obj != null) {
            writeString(obj.toString());
        } else {
            writeNullValue();
        }
        printMemberSeparator = true;
    }

    private void writeNullValue() throws IOException {
        writer().write(NULL_VALUE);
    }

    @Override
    public <T> void writeObject(EntityWriter<? super T> objWriter, T obj, OutputContext ctxt) throws IOException {
        startField(objWriter.fieldName());
        if (obj != null) {
            writeObj(objWriter, obj, ctxt);
        } else {
            writeNullValue();
        }
    }
    
    @Override
    public void writeList(String field, String elem, Iterable<?> list, OutputContext ctxt)
        throws IOException {
        startField(field);
        writer().write(START_ARRAY);
        
        Iterator<?> iter = list.iterator();
        if (iter.hasNext()) {
            writeString(iter.next().toString());
            while (iter.hasNext()) {
                writer().write(ELEMENT_SEPARTOR);
                writeString(iter.next().toString());
            }
        }
        writer().write(END_ARRAY);
    }

    @Override
    public <T> void writeList(EntityListWriter<? super T> listWriter, Iterable<T> list, OutputContext ctxt) throws IOException {
        startField(listWriter.listName());
        writer().write(START_ARRAY);
        
        EntityWriter<? super T> entWriter = listWriter;
        Iterator<T> iter = list.iterator();
        if (iter.hasNext()) {
            writeObj(entWriter, iter.next(), ctxt);
            while (iter.hasNext()) {
                writer().write(ELEMENT_SEPARTOR);
                writeObj(entWriter, iter.next(), ctxt);
            }
        }
        writer().write(END_ARRAY);
    }

    private <T> void writeObj(EntityWriter<? super T> entWriter, T nextObj, OutputContext ctxt) throws IOException {
        printMemberSeparator = false;
        writer().write(START_OBJECT);
        entWriter.write(nextObj, this, ctxt);
        writer().write(END_OBJECT);
        printMemberSeparator = true;
    }

    private void writeString(String string) throws IOException {
        //writer().write(STRING_DELIMITER);
        writer().write(escape(string));
        //writer().write(STRING_DELIMITER);
    }
    
    private String escape(String string) throws IOException {
        return stringSerializer.writeValueAsString(string);
    }

    private void startField(String fieldName) throws IOException {
        if (printMemberSeparator) {
            writer().write(MEMBER_SEPARATOR);
        }
        writeString(fieldName);
        writer().write(PAIR_SEPARATOR);
    }

}
