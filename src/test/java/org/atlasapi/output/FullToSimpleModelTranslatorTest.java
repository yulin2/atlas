package org.atlasapi.output;

import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.simple.ContentQueryResult;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.product.ProductResolver;
import org.atlasapi.media.segment.SegmentResolver;
import org.atlasapi.output.simple.ContainerModelSimplifier;
import org.atlasapi.output.simple.ItemModelSimplifier;
import org.atlasapi.output.simple.ProductModelSimplifier;
import org.atlasapi.output.simple.TopicModelSimplifier;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.output.AvailableChildrenResolver;
import org.atlasapi.persistence.output.ContainerSummaryResolver;
import org.atlasapi.persistence.output.UpcomingChildrenResolver;
import org.atlasapi.persistence.topic.TopicQueryResolver;
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
import org.atlasapi.persistence.content.ContentGroupResolver;

@RunWith(JMock.class)
public class FullToSimpleModelTranslatorTest {
    
    private final Mockery context = new Mockery();
    private final ContentResolver contentResolver = context.mock(ContentResolver.class);
    private final ContentGroupResolver contentGroupResolver = context.mock(ContentGroupResolver.class);
    private final TopicQueryResolver topicResolver = context.mock(TopicQueryResolver.class);
    private final SegmentResolver segmentResolver = context.mock(SegmentResolver.class);
    private final AvailableChildrenResolver availableChildren = context.mock(AvailableChildrenResolver.class);
    private final UpcomingChildrenResolver upcomingChildren = context.mock(UpcomingChildrenResolver.class);
    private @SuppressWarnings("unchecked") final AtlasModelWriter<ContentQueryResult> xmlOutputter = context.mock(AtlasModelWriter.class);
    private final ContainerSummaryResolver containerSummaryResolver = context.mock(ContainerSummaryResolver.class);
    
    private final TopicModelSimplifier topicSimplifier = new TopicModelSimplifier("localhostName");

    private ProductModelSimplifier productSimplifier = new ProductModelSimplifier("localhostName");
    private ProductResolver productResolver = context.mock(ProductResolver.class); 

    private final ItemModelSimplifier itemSimplifier = new ItemModelSimplifier("localhostName", contentGroupResolver, topicResolver, productResolver , segmentResolver, containerSummaryResolver);
    private final SimpleContentModelWriter translator = new SimpleContentModelWriter(xmlOutputter, itemSimplifier, new ContainerModelSimplifier(itemSimplifier, "localhostName", contentGroupResolver, topicResolver, availableChildren, upcomingChildren, productResolver),topicSimplifier, productSimplifier);
    
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
			one(xmlOutputter).writeTo(with(request), with(response), with(simpleGraph()), with(ImmutableSet.<Annotation>of()), with(ApplicationConfiguration.DEFAULT_CONFIGURATION));
			ignoring(contentResolver);
            ignoring(contentGroupResolver);
			ignoring(topicResolver);
			ignoring(segmentResolver);
		}});
		
        translator.writeTo(request, response, QueryResult.of(graph), ImmutableSet.<Annotation>of(), ApplicationConfiguration.DEFAULT_CONFIGURATION);
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
