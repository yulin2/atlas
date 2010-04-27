/* Copyright 2010 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.uriplay.query.content.fuzzy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;

import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Description;
import org.uriplay.media.entity.Item;
import org.uriplay.persistence.content.ContentListener;

import com.google.common.collect.Lists;

public class InMemoryFuzzyTitleSearcherTest extends MockObjectTestCase {
	
	Brand dragonsDen = brand("/den", "Dragon's den");
	Brand theCityGardener = brand("/garden", "The City Gardener");
	Brand eastenders = brand("/eastenders", "Eastenders");
	Brand meetTheMagoons = brand("/magoons", "Meet the Magoons");
	Brand theJackDeeShow = brand("/dee", "The Jack Dee Show");
	Brand peepShow = brand("/peep-show", "Peep Show");
	Brand euromillionsDraw = brand("/draw", "EuroMillions Draw");
	Brand haveIGotNewsForYou = brand("/news", "Have I Got News For You");
	Brand brasseye = brand("/eye", "Brass Eye");

	Item englishForCats = item("/items/cats", "English for cats");
	
	Item jamieOliversCookingProgramme = item("/items/oliver/1", "Jamie Oliver's cooking programme", "lots of words that are the same alpha beta");
	Item gordonRamsaysCookingProgramme = item("/items/ramsay/2", "Gordon Ramsay's cooking show", "lots of words that are the same alpha beta");
	
	List<Brand> brands = Arrays.asList(dragonsDen, theCityGardener, eastenders, meetTheMagoons, theJackDeeShow, peepShow, haveIGotNewsForYou, euromillionsDraw, brasseye);
	List<Item> items = Arrays.asList(englishForCats, jamieOliversCookingProgramme, gordonRamsaysCookingProgramme);
	
	InMemoryFuzzySearcher searcher;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		searcher = new InMemoryFuzzySearcher();
		searcher.brandChanged(brands, ContentListener.changeType.BOOTSTRAP);
		searcher.itemChanged(items, ContentListener.changeType.BOOTSTRAP);
	}
	
	public void testFindingBrandsByTitle() throws Exception {
		check(searcher.brandTitleSearch("den"), dragonsDen);
		check(searcher.brandTitleSearch("dragon"), dragonsDen);
		check(searcher.brandTitleSearch("dragons"), dragonsDen);
		check(searcher.brandTitleSearch("drag den"), dragonsDen);
		check(searcher.brandTitleSearch("drag"), dragonsDen);
		check(searcher.brandTitleSearch("dragon's den"), dragonsDen);
		check(searcher.brandTitleSearch("eastend"), eastenders);
		check(searcher.brandTitleSearch("easteners"), eastenders);
		check(searcher.brandTitleSearch("eastedners"), eastenders);
		check(searcher.brandTitleSearch("east"), eastenders);
		check(searcher.brandTitleSearch("eas"));
		check(searcher.brandTitleSearch("end"));
		check(searcher.brandTitleSearch("peep show"), peepShow);
		check(searcher.brandTitleSearch("dee"), theJackDeeShow);
		check(searcher.brandTitleSearch("show"), peepShow, theJackDeeShow);
		check(searcher.brandTitleSearch("jack show"), theJackDeeShow);
		check(searcher.brandTitleSearch("dee show"), theJackDeeShow);
		check(searcher.brandTitleSearch("hav i got news"), haveIGotNewsForYou);
		check(searcher.brandTitleSearch("the"));
		check(searcher.brandTitleSearch("brasseye"), brasseye);
		check(searcher.brandTitleSearch("braseye"), brasseye);
		check(searcher.brandTitleSearch("brassey"), brasseye);
	}
	
	public void testUsesPrefixSearchForShortSearches() throws Exception {
		check(searcher.brandTitleSearch("D"), dragonsDen);
		check(searcher.brandTitleSearch("Dr"), dragonsDen);
		check(searcher.brandTitleSearch("a"));
	}
	
	public void testFindingItemsByTitle() throws Exception {
		check(searcher.itemTitleSearch("cats"), englishForCats);
	}
	
	public void testUpdateByType() throws Exception {
		Brand dragonsDenV2 = brand("/den", "Dragon's den Version 2");
		
		check(searcher.brandTitleSearch("dragon"), dragonsDen);
		searcher.brandChanged(Lists.newArrayList(dragonsDenV2), ContentListener.changeType.CONTENT_UPDATE);
		check(searcher.brandTitleSearch("dragon"), dragonsDen);
	}

	private void check(List<String> found, Description... content) {
		assertThat(found, is(toUris(Arrays.asList(content))));
	}

	private List<String> toUris(List<? extends Description> content) {
		List<String> uris = Lists.newArrayList();
		for (Description description : content) {
			uris.add(description.getCanonicalUri());
		}
		return uris;
	}

	private Brand brand(String uri, String title) {
		Brand b = new Brand();
		b.setTitle(title);
		b.setCanonicalUri(uri);
		return b;
	}
	
	private Item item(String uri, String title) {
		return item(uri, title, null);
	}
	
	private Item item(String uri, String title, String description) {
		Item i = new Item();
		i.setTitle(title);
		i.setCanonicalUri(uri);
		i.setDescription(description);
		return i;
	}
}
