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

package org.uriplay.system;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.uriplay.media.entity.Description;
import org.uriplay.persistence.content.MutableContentStore;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Controller
public class AliasController {

	private final MutableContentStore store;

	public AliasController(MutableContentStore store) {
		this.store = store;
	}
	
	@RequestMapping("/system/aliases")
	public String showAliasForm() {
		return "system/aliases";
	}
	
	@RequestMapping(value="/system/aliases", method=RequestMethod.POST)
	public String addAlias(@RequestParam String csvAliases, Map<String, Object> model) {
		
		List<String> info = Lists.newArrayList();
		List<String> errors = Lists.newArrayList();
		
		ImmutableList<AliasAndTarget> aliases = aliasesFrom(csvAliases);
		for (AliasAndTarget aliasAndTarget : aliases) {
			
			Description content = store.findByUri(aliasAndTarget.alias);
			if (content != null) {
				info.add("Not adding alias " + aliasAndTarget.alias + "  because it already exists");
				continue;
			}
			
			Description canonicalContent = store.findByUri(aliasAndTarget.canonicalUri);
			if (canonicalContent == null) {
				errors.add("Not adding alias " + aliasAndTarget.alias + "  because the canonicalUri (" + aliasAndTarget.canonicalUri + ") can't be found");
				continue;
			}
			
			store.addAliases(canonicalContent.getCanonicalUri(), Sets.newHashSet(aliasAndTarget.alias));
		}
		
		model.put("info", info);
		model.put("errors", errors);
		return "/system/aliases";
	}
	
	private static ImmutableList<AliasAndTarget> aliasesFrom(String csv) {
		List<AliasAndTarget> aliases = Lists.newArrayList();
		for (String line : csv.split("\n")) {
			if (org.apache.commons.lang.StringUtils.isBlank(line)) {
				continue;
			}
			List<String> parts = Lists.newArrayList(Splitter.on(',').trimResults().split(line));
			if (parts.size() != 2) {
				throw new IllegalStateException("Malformed alias file");
			}
			aliases.add(new AliasAndTarget(parts.get(1).trim(), parts.get(0).trim()));
		}
		return ImmutableList.copyOf(aliases);
	}
	
	private static class AliasAndTarget {
		
		private final String canonicalUri;
		private final String alias;
		
		public AliasAndTarget(String canonicalUri, String alias) {
			if (!isUri(canonicalUri) || !isUri(alias)) {
				throw new IllegalArgumentException("Malformed uri");
			}
			this.canonicalUri = canonicalUri;
			this.alias = alias;
		}

		private boolean isUri(String uri) {
			return uri.startsWith("http://");
		}
	}
}
