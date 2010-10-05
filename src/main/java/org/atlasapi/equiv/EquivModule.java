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

package org.atlasapi.equiv;

import java.util.List;

import org.atlasapi.equiv.www.EquivController;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.AggregateContentListener;
import org.atlasapi.persistence.equiv.EquivalentUrlStore;
import org.atlasapi.persistence.equiv.MongoEquivStore;
import org.atlasapi.remotesite.EquivGenerator;
import org.atlasapi.remotesite.freebase.FreebaseBrandEquivGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;

@Configuration
public class EquivModule {

	private @Autowired DatabasedMongo db;
	private @Autowired AggregateContentListener aggregateContentListener;
	
	@Bean EquivController manualEquivAssignmentController() {
		return new EquivController(store());
	}
	
	public @Bean EquivalentUrlStore store() {
		return new MongoEquivStore(db);
	}
	
	@Bean EquivContentListener equivContentListener() {
	    List<EquivGenerator<Brand>> brandEquivGenerators = ImmutableList.<EquivGenerator<Brand>>of(new FreebaseBrandEquivGenerator());
	    
	    BrandEquivUpdater brandUpdater = new BrandEquivUpdater(brandEquivGenerators, store());
	    ItemEquivUpdater itemUpdater = new ItemEquivUpdater(ImmutableList.<EquivGenerator<Item>>of(), store());
	    
	    EquivContentListener equivContentListener = new EquivContentListener(brandUpdater, itemUpdater);
	    aggregateContentListener.addListener(equivContentListener);
	    return equivContentListener;
	}
}
