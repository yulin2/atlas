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

package org.atlasapi.remotesite.bbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.util.Collections;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Policy;
import org.joda.time.Duration;

import com.metabroadcast.common.intl.Countries;
import org.joda.time.ReadableDuration;


public class BbcProgrammesPolicyClientTest extends TestCase {

	private String programmeThatWillBeAroundForALongWhileAsIsAvailableEverywhere = "http://feeds.bbc.co.uk/iplayer/episode/b00ss2rk";
	
	@SuppressWarnings("unchecked")
	public void testname() throws Exception {
	    BbcLocationPolicyIds locationPolicyIds = BbcLocationPolicyIds.builder().build();
	    
		Policy policy = new BbcProgrammesPolicyClient(locationPolicyIds).policyForUri(programmeThatWillBeAroundForALongWhileAsIsAvailableEverywhere).requireValue();
		
		assertThat(policy.getAvailableCountries(), is(Collections.singleton(Countries.ALL)));
		assertThat(new Duration(policy.getAvailabilityStart(), policy.getAvailabilityEnd()), is(greaterThan((ReadableDuration) Duration.standardDays(100))));
	}
	

}
