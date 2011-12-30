package org.atlasapi.output;

import java.util.Set;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.simple.ContentQueryResult;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.servlet.StubHttpServletRequest;
import com.metabroadcast.common.servlet.StubHttpServletResponse;

@RunWith(JMock.class)
public class FullToSimpleModelTranslatorTest {
    
    private final Mockery context = new Mockery();
    private final ContentResolver contentResolver = context.mock(ContentResolver.class);
    @SuppressWarnings("unchecked")
    private final AtlasModelWriter<ContentQueryResult> xmlOutputter = context.mock(AtlasModelWriter.class);
    private final SimpleContentModelWriter translator = new SimpleContentModelWriter(xmlOutputter, contentResolver);
    
	private StubHttpServletRequest request;
	private StubHttpServletResponse response;

	@Before
	public void setUp() throws Exception {
		this.request = new StubHttpServletRequest();
		this.response = new StubHttpServletResponse();
	}
	
	@Test
	public void testTranslatesItemsInFullModel() throws Exception {
		
		Set<Content> graph = Sets.newHashSet();
		graph.add(new Episode());
		
		context.checking(new Expectations() {{ 
			one(xmlOutputter).writeTo(with(request), with(response), with(simpleGraph()), ImmutableSet.<Annotation>of());
			ignoring(contentResolver);
		}});
		
        translator.writeTo(request, response, graph, ImmutableSet.<Annotation>of());
	}

	protected Matcher<ContentQueryResult> simpleGraph() {
		return new TypeSafeMatcher<ContentQueryResult> () {

			@Override
			public boolean matchesSafely(ContentQueryResult result) {
				if (result.getContents().size() != 1) { return false; }
				Object bean = Iterables.getOnlyElement(result.getContents());
				if (!(bean instanceof Item)) { return false; }
				return true;
			}

			public void describeTo(Description description) {
				// TODO Auto-generated method stub
			}};
	}
	
}
