/* Copyright 2009 British Broadcasting Corporation
   Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.output.rdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.media.content.Encoding;
import org.atlasapi.media.content.Episode;
import org.atlasapi.media.content.Location;
import org.atlasapi.media.content.Version;
import org.atlasapi.output.rdf.NaiveTypeMap;
import org.atlasapi.output.rdf.TypeMap;

/**
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class NaiveOneWayTypeMapTest extends TestCase {

	public void testSelectsRdfTypesForKnownClasses() throws Exception {
		
		TypeMap typeMap = new NaiveTypeMap();
		
		Set<String> encodingTypes = typeMap.rdfTypes(Encoding.class);
		assertThat(encodingTypes, hasItem("http://uriplay.org/elements/Encoding"));

		Set<String> locationTypes = typeMap.rdfTypes(Location.class);
		assertThat(locationTypes, hasItem("http://uriplay.org/elements/Location"));
		
		Set<String> episodeTypes = typeMap.rdfTypes(Episode.class);
		assertThat(episodeTypes, hasItem("http://purl.org/ontology/po/Episode"));
		
		Set<String> versionTypes = typeMap.rdfTypes(Version.class);
		assertThat(versionTypes, hasItem("http://purl.org/ontology/po/Version"));
	}
}
