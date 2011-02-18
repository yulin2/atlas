package org.atlasapi.beans;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Currency;
import java.util.Set;

import org.atlasapi.media.entity.Actor;
import org.atlasapi.media.entity.Countries;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Restriction;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.entity.Policy.RevenueContract;
import org.atlasapi.media.entity.simple.ContentQueryResult;
import org.atlasapi.media.entity.simple.Item;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.currency.Price;
import com.metabroadcast.common.media.MimeType;

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
				if (!(bean instanceof ContentQueryResult)) { return false; }
				ContentQueryResult output = (ContentQueryResult) bean;
				if (output.getItems().size() != 1) { return false; }
				return true;
			}

			public void describeTo(Description description) {
				// TODO Auto-generated method stub
			}};
	}
	
	public void testCanCreateSimpleItemFromFullItem() throws Exception {
		
		org.atlasapi.media.entity.Item fullItem = new org.atlasapi.media.entity.Item();
		Version version = new Version();
		
		Restriction restriction = new Restriction();
		restriction.setRestricted(true);
		restriction.setMessage("adults only");
		version.setRestriction(restriction);
		
		Encoding encoding = new Encoding();
		encoding.setDataContainerFormat(MimeType.VIDEO_3GPP);
		version.addManifestedAs(encoding);
		Location location = new Location();
		location.setUri("http://example.com");
		location.setPolicy(new Policy().withRevenueContract(RevenueContract.PAY_TO_BUY).withPrice(new Price(Currency.getInstance("GBP"), 99)).withAvailableCountries(Countries.GB));
		encoding.addAvailableAt(location);
		fullItem.addVersion(version);
		fullItem.setTitle("Collings and Herrin");
		
		CrewMember person = Actor.actor("Andrew Collings", "Dirt-bag Humperdink", Publisher.BBC);
		fullItem.addPerson(person);
		
		Item simpleItem = FullToSimpleModelTranslator.simpleItemFrom(fullItem);
		Set<org.atlasapi.media.entity.simple.Person> people = simpleItem.getPeople();
		org.atlasapi.media.entity.simple.Actor simpleActor = (org.atlasapi.media.entity.simple.Actor) Iterables.getOnlyElement(people);
		assertThat(simpleActor.character(), is("Dirt-bag Humperdink"));
		assertThat(simpleActor.getName(), is("Andrew Collings"));
		
		Set<org.atlasapi.media.entity.simple.Location> simpleLocations = simpleItem.getLocations();
		assertThat(simpleLocations.size(), is(1));
		org.atlasapi.media.entity.simple.Location simpleLocation = Iterables.getOnlyElement(simpleLocations);
		
		assertThat(simpleLocation.getUri(), is("http://example.com"));
		assertThat(simpleLocation.getDataContainerFormat(), is(MimeType.VIDEO_3GPP.toString()));
		assertThat(simpleLocation.getRestriction().getMessage(), is("adults only"));
		assertThat(simpleLocation.getRevenueContract(), is("pay_to_buy"));
		assertThat(simpleLocation.getCurrency(), is("GBP"));
		assertThat(simpleLocation.getPrice(), is(99));
		assertThat(simpleLocation.getAvailableCountries().size(), is(1));
		assertThat(simpleLocation.getAvailableCountries().iterator().next(), is("GB"));
		assertThat(simpleItem.getTitle(), is("Collings and Herrin"));
	}
}
