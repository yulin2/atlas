package org.atlasapi.query.v4.schedule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.metabroadcast.common.servlet.StubHttpServletRequest;
import com.metabroadcast.common.servlet.StubHttpServletResponse;


public class JsonResponseWriterTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpServletRequest request = new StubHttpServletRequest();
    private final HttpServletResponse response = new StubHttpServletResponse();
    
    private final Writer writer = new StringWriter();
    private final JsonResponseWriter formatter = new JsonResponseWriter(writer);
    
    @Before
    public void setup() throws IOException {
        formatter.startResponse(request, response);
    }
    
    @Test
    public void testWritingSingleField() throws Exception {
        
        formatter.writeField("hello", "world");
        formatter.finishResponse(request, response);
       
        assertEquals("world", asMap(writer).get("hello"));
    }

    @Test
    public void testWritingManyFields() throws Exception {
        
        formatter.writeField("hello", "world");
        formatter.writeField("bonjour", "monde");
        formatter.writeField("halt", "hammerzeit");
        formatter.finishResponse(request, response);
        
        Map<String, Object> deser = asMap(writer);
        
        assertEquals("world", deser.get("hello"));
        assertEquals("monde", deser.get("bonjour"));
        assertEquals("hammerzeit", deser.get("halt"));
    }

    @Test
    public void testWritingNullFields() throws Exception {
        
        formatter.writeField("null", null);
        formatter.finishResponse(request, response);
        
        assertNull(null, asMap(writer).get("null"));
    }

    @Test
    public void testWritingNestedObjects() throws Exception {
        
        formatter.writeObject("nested", new EntityWriter<String>() {
            @Override
            public void write(String entity, FieldWriter formatter) throws IOException {
                formatter.writeField("nested_field", entity);
                formatter.writeField("nested_again", entity);
            }
        }, "value");
        formatter.finishResponse(request, response);
        
        ImmutableMap<String, String> expectedMap = ImmutableMap.of(
            "nested_field","value",
            "nested_again","value"
        );
        assertEquals(expectedMap, asMap(writer).get("nested"));
    }

    @Test
    public void testWritingEmptyArray() throws Exception {
        
        formatter.writeList("elems", "elem", ImmutableList.of());
        formatter.finishResponse(request, response);
        
        assertEquals(ImmutableList.of(), asMap(writer).get("elems"));
    }
    
    @Test
    public void testWritingSingletonArray() throws Exception {
        
        formatter.writeList("elems", "elem", ImmutableList.of("elem"));
        formatter.finishResponse(request, response);
        
        assertEquals(ImmutableList.of("elem"), asMap(writer).get("elems"));
    }
    
    @Test
    public void testWritingRegularArray() throws Exception {
        
        formatter.writeList("elems", "elem", ImmutableList.of("elem", "elen", "eleo"));
        formatter.finishResponse(request, response);
        
        assertEquals(ImmutableList.of("elem", "elen", "eleo"), asMap(writer).get("elems"));
    }

    @Test
    public void testWritingObjectArray() throws Exception {
        
        formatter.writeList(new EntityListWriter<String>() {

            @Override
            public void write(String entity, FieldWriter formatter) throws IOException {
                formatter.writeField("prop", entity);
            }

            @Override
            public String listName() {
                return "elems";
            }

            @Override
            public String elementName() {
                return "elem";
            }
            
        }, ImmutableList.of("elem", "elen", "eleo"));
        formatter.finishResponse(request, response);
        
        ImmutableList<ImmutableMap<String, String>> expectedList = ImmutableList.of(
            ImmutableMap.of("prop","elem"), 
            ImmutableMap.of("prop","elen"), 
            ImmutableMap.of("prop","eleo")
        );
        
        assertEquals(expectedList, asMap(writer).get("elems"));
    }

    @Test
    public void testEscapesCharacters() throws Exception {
        String one = "asdf\"zxcv";
        String two = "asdf\\zxcv";
        formatter.writeField("one", one);
        formatter.writeField("two", two);
        formatter.finishResponse(request, response);
        Map<String, Object> deser = asMap(writer);
        assertEquals(one, deser.get("one"));
        assertEquals(two, deser.get("two"));
    }

    @Test
    public void testEscapesSpecialsCharacters() throws Exception {
        String testString = "\t\r\n\b\f\\/";
        formatter.writeField("hello", testString);
        formatter.finishResponse(request, response);
        
        assertEquals(testString, asMap(writer).get("hello"));
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Writer writer) throws Exception {
        return (Map<String,Object>)mapper.readValue(writer.toString(), Map.class);
    }

}
