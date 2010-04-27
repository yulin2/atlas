package org.uriplay.beans;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.jherd.hamcrest.Matchers.hasPropertyValue;

import org.jherd.beans.Representation;
import org.jherd.beans.UriPropertySource;
import org.jherd.naming.ResourceMapping;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.entity.Description;
import org.uriplay.persistence.content.MutableContentStore;

public class DescriptionResourceMappingTest extends MockObjectTestCase {
	
	private MutableContentStore beanStore = mock(MutableContentStore.class);
	
	public void testGetsPreferredUriForNamedResources() throws Exception {
		
		Description bean = new Description();
		bean.setCanonicalUri("http://example.com/bean");
		ResourceMapping mapping = new DescriptionResourceMapping(beanStore);
		
		String preferredUri = mapping.getUri(bean);
		assertThat(preferredUri, is("http://example.com/bean"));

	}
	
	public void testReturnsNullPreferredUriForNonNamedResources() throws Exception {
		
		ResourceMapping mapping = new DescriptionResourceMapping(beanStore);
		
		String preferredUri = mapping.getUri(new Object());
		assertThat(preferredUri, is(nullValue()));
		
	}
	
	public void testSetsUriPropertyWhenUsedAsUriSource() throws Exception {
		
		UriPropertySource mapping = new DescriptionResourceMapping(beanStore);
		
		Representation representation = new Representation();
		String docId = "http://bbc.co.uk";
		representation.addType(docId, Description.class);
		
		mapping.merge(representation , docId);
		
		assertThat(representation, hasPropertyValue(docId, "canonicalUri", "http://bbc.co.uk"));
		
	}

}
