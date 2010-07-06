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

package org.uriplay.equiv;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.uriplay.equiv.www.EquivController;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Item;
import org.uriplay.persistence.equiv.EquivalentUrlStore;
import org.uriplay.persistence.equiv.MongoEquivStore;
import org.uriplay.remotesite.EquivGenerator;
import org.uriplay.remotesite.freebase.FreebaseBrandEquivGenerator;

import com.google.common.collect.ImmutableList;
import com.mongodb.Mongo;

@Configuration
public class EquivModule {

	private @Autowired Mongo mongo;
	
	@Bean EquivController manualEquivAssignmentController() {
		return new EquivController(store());
	}
	
	public @Bean EquivalentUrlStore store() {
		return new MongoEquivStore(mongo, "uriplay");
	}
	
	@Bean EquivContentListener contentListener() {
	    List<EquivGenerator<Brand>> brandEquivGenerators = ImmutableList.<EquivGenerator<Brand>>of(new FreebaseBrandEquivGenerator());
	    
	    BrandEquivUpdater brandUpdater = new BrandEquivUpdater(brandEquivGenerators, store());
	    ItemEquivUpdater itemUpdater = new ItemEquivUpdater(ImmutableList.<EquivGenerator<Item>>of(), store());
	    
	    return new EquivContentListener(brandUpdater, itemUpdater);
	}
}
