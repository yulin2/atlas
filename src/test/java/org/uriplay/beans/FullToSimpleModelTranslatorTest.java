package org.uriplay.beans;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jherd.beans.BeanGraphWriter;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Playlist;
import org.uriplay.media.entity.Version;
import org.uriplay.media.entity.simple.Item;
import org.uriplay.media.entity.simple.UriplayXmlOutput;
import org.uriplay.media.reference.entity.MimeType;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class FullToSimpleModelTranslatorTest extends MockObjectTestCase {

	public void testTranslatesItemsInFullModel() throws Exception {
		
		final BeanGraphWriter xmlOutputter = mock(BeanGraphWriter.class);
		final OutputStream stream = new ByteArrayOutputStream();
		
		Set<Object> graph = Sets.newHashSet();
		graph.add(new Episode());
		
		checking(new Expectations() {{ 
			one(xmlOutputter).writeTo(with(simpleGraph()), with(same(stream)));
		}});
		
		new FullToSimpleModelTranslator(xmlOutputter).writeTo(graph, stream);
	}

	protected Matcher<Set<Object>> simpleGraph() {
		return new TypeSafeMatcher<Set<Object>> () {

			@Override
			public boolean matchesSafely(Set<Object> beans) {
				if (beans.size() != 1) { return false; }
				Object bean = Iterables.getOnlyElement(beans);
				if (!(bean instanceof UriplayXmlOutput)) { return false; }
				UriplayXmlOutput output = (UriplayXmlOutput) bean;
				if (output.getItems().size() != 1) { return false; }
				return true;
			}

			public void describeTo(Description description) {
				// TODO Auto-generated method stub
			}};
	}
	
	public void testCanCreateSimpleItemFromFullItem() throws Exception {
		
		org.uriplay.media.entity.Item fullItem = new org.uriplay.media.entity.Item();
		Version version = new Version();
		version.setRatingText("adults only");
		Encoding encoding = new Encoding();
		encoding.setDataContainerFormat(MimeType.VIDEO_3GPP);
		version.addManifestedAs(encoding);
		Location location = new Location();
		location.setUri("http://example.com");
		encoding.addAvailableAt(location);
		fullItem.addVersion(version);
		fullItem.setTitle("Collings and Herrin");
		
		Item simpleItem = FullToSimpleModelTranslator.simpleItemFrom(fullItem);
		
		Set<org.uriplay.media.entity.simple.Location> simpleLocations = simpleItem.getLocations();
		assertThat(simpleLocations.size(), is(1));
		org.uriplay.media.entity.simple.Location simpleLocation = Iterables.getOnlyElement(simpleLocations);
		
		assertThat(simpleLocation.getUri(), is("http://example.com"));
		assertThat(simpleLocation.getDataContainerFormat(), is(MimeType.VIDEO_3GPP.toString()));
		assertThat(simpleLocation.getRatingText(), is("adults only"));
		assertThat(simpleItem.getTitle(), is("Collings and Herrin"));
	}
	
	public void testCanCreateSimplePlaylistFromNestedLists() throws Exception {
		
		
		Playlist showsBeginningWithA = new Playlist();

		Playlist animalHospital = new Playlist();
		Playlist antiquesRoadshow = new Playlist();
		animalHospital.addItem(new org.uriplay.media.entity.Item());
		antiquesRoadshow.addItem(new org.uriplay.media.entity.Item());
		showsBeginningWithA.addPlaylist(animalHospital);
		showsBeginningWithA.addPlaylist(antiquesRoadshow);
		
		Set<Object> processed = Sets.newHashSet();
		org.uriplay.media.entity.simple.Playlist simplePlaylist = FullToSimpleModelTranslator.simplePlaylistFrom(showsBeginningWithA, processed);

		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		new JaxbXmlTranslator().writeTo(Sets.newHashSet((Object) showsBeginningWithA, simplePlaylist), stream);
		
	}
}
