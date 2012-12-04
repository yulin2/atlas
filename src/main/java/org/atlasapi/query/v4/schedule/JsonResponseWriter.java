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

    private final Writer writer;
    
    private boolean printMemberSeparator = false;

    public JsonResponseWriter(Writer writer) {
        this.writer = writer;
    }
    
    @Override
    public void startResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //TODO JSONP
        writer.write(START_OBJECT);
    }
    
    @Override
    public void finishResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        writer.write(END_OBJECT);
        //TODO: JSONP
    }

    @Override
    public void writeField(String field, Object obj) throws IOException {
        startField(field);
        if (obj != null) {
            writeString(obj.toString());
        } else {
            writer.write(NULL_VALUE);
        }
        printMemberSeparator = true;
    }

    @Override
    public <T> void writeObject(String field, EntityWriter<? super T> objWriter, T obj) throws IOException {
        startField(field);
        writeObject(objWriter, obj);
    }

    @Override
    public void writeList(String plural, String singular, Iterable<?> objs) throws IOException {
        startField(plural);
        writer.write(START_ARRAY);
        Iterator<?> iter = objs.iterator();
        if (iter.hasNext()) {
            writeString(iter.next().toString());
            while (iter.hasNext()) {
                writer.write(ELEMENT_SEPARTOR);
                writeString(iter.next().toString());
            }
        }
        writer.write(END_ARRAY);
        printMemberSeparator = true;
    }

    @Override
    public <T> void writeList(EntityListWriter<? super T> listWriter, Iterable<T> list) throws IOException {
        startField(listWriter.listName());
        writer.write(START_ARRAY);
        
        EntityWriter<? super T> entWriter = listWriter;
        Iterator<T> iter = list.iterator();
        if (iter.hasNext()) {
            writeObject(entWriter, iter.next());
            while (iter.hasNext()) {
                writer.write(ELEMENT_SEPARTOR);
                writeObject(entWriter, iter.next());
            }
        }
        writer.write(END_ARRAY);
    }

    private <T> void writeObject(EntityWriter<? super T> entWriter, T nextObj) throws IOException {
        printMemberSeparator = false;
        writer.write(START_OBJECT);
        entWriter.write(nextObj, this);
        writer.write(END_OBJECT);
        printMemberSeparator = true;
    }

    private void writeString(String string) throws IOException {
        //writer.write(STRING_DELIMITER);
        writer.write(escape(string));
        //writer.write(STRING_DELIMITER);
    }
    
    private String escape(String string) throws IOException {
        return stringSerializer.writeValueAsString(string);
    }

    private void startField(String fieldName) throws IOException {
        if (printMemberSeparator) {
            writer.write(MEMBER_SEPARATOR);
        }
        writeString(fieldName);
        writer.write(PAIR_SEPARATOR);
    }

}
