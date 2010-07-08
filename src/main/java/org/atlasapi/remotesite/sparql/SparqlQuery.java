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

package org.atlasapi.remotesite.sparql;

import java.util.Map;

import com.google.common.collect.Maps;

public class SparqlQuery {

	private String selectId;
	private String whereClause;
	private boolean selectDistinct = false;
	private Map<String, String> prefixes = Maps.newHashMap();
	
	private String custom;

	public static SparqlQuery select(String id) {
		SparqlQuery query = new SparqlQuery();
		query.selectId = id;
		return query;
	}
	
	public static SparqlQuery selectDistinct(String id) {
		SparqlQuery query = select(id);
		query.selectDistinct = true;
		return query;
	}

	public SparqlQuery whereSubjectOf(String property, String value) {
		this.whereClause = "?" + selectId + " " + property + " " + value;
		return this;
	}
	
	@Override
	public String toString() {
		
		if (custom != null) {
			return custom;
		}
		
		StringBuffer query = new StringBuffer();
		if (!prefixes.isEmpty()) {
			for (Map.Entry<String, String> prefix : prefixes.entrySet()) {
				query.append("PREFIX ")
				     .append(prefix.getKey())
				     .append(":")
				     .append(" <")
				     .append(prefix.getValue())
				     .append("> ");
			}
		}
		query.append("SELECT ");
		if (selectDistinct) {
			query.append("DISTINCT ");
		}
		query.append(String.format("?%s WHERE { %s. }", selectId, whereClause));
		
		return query.toString();
	}

	public SparqlQuery withPrefix(String prefix, String expanded) {
		prefixes.put(prefix, expanded);
		return this;
	}

	public String getSelectId() {
		return selectId;
	}

	@Override
	public boolean equals(Object obj) {
		return this.toString().equals(obj.toString());
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public SparqlQuery whereObjectOf(String subject, String predicate) {
		whereClause = "<" + subject + ">" + " " + predicate + " " + "?" + selectId;
		return this;
	}

	public static SparqlQuery fromString(String custom) {
		SparqlQuery query = new SparqlQuery();
		query.custom = custom;
		return query;
	}
	
}
