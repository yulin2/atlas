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

package org.uriplay.equiv.www;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.uriplay.media.entity.Equiv;
import org.uriplay.persistence.equiv.EquivStore;

import com.google.common.collect.Sets;

@Controller
public class EquivController {

	private final EquivStore store;

	public EquivController(EquivStore store) {
		this.store = store;
	}
	
	@ResponseBody
	@RequestMapping("/eqiv/add")
	public String addEquiv(@RequestParam String a, @RequestParam String b) {
		store.store(new Equiv(a, b));
		return "ok";
	}
	
	@ResponseBody
	@RequestMapping("/eqiv")
	public String equivTo(@RequestParam String a) {
		return store.get(Sets.newHashSet(a)).toString();
	}
}
