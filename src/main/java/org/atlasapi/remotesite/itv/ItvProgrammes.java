/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.remotesite.itv;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representation of a list of itv programmes, to be bound
 * to itv xml representation using JAXB.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@XmlRootElement(name="ITVCatchUpMenu")
public class ItvProgrammes {

	@XmlElementWrapper(name="ItvCatchUpProgrammes")
	@XmlElement(name="ITVCatchUpProgramme")
	private List<ItvProgramme> programmeList;

	public List<ItvProgramme> programmeList() {
		return programmeList;
	}

}
