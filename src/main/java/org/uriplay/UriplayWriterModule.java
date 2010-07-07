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

package org.uriplay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.uriplay.equiv.EquivModule;
import org.uriplay.persistence.UriplayPersistenceModule;
import org.uriplay.persistence.content.ContentWriter;
import org.uriplay.persistence.equiv.EquivalentContentFinder;
import org.uriplay.persistence.equiv.EquivalentContentMerger;
import org.uriplay.persistence.equiv.EquivalentContentMergingContentWriter;
import org.uriplay.persistence.equiv.EquivalentUrlFinder;
import org.uriplay.remotesite.RemoteSiteModule;

@Import({EquivModule.class, UriplayPersistenceModule.class})
public class UriplayWriterModule {
	
	private @Autowired EquivalentUrlFinder finder;
	private @Autowired UriplayPersistenceModule persistence;
	private @Autowired RemoteSiteModule remote;

	public @Bean ContentWriter contentWriter() {		
		EquivalentContentMergingContentWriter writer = new EquivalentContentMergingContentWriter(persistence.persistentWriter(), new EquivalentContentMerger(new EquivalentContentFinder(finder, persistence.mongoContentStore())));
		remote.contentWriters().add(writer);
		return writer;
	}
}
