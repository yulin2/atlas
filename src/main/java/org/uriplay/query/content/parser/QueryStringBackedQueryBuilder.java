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

package org.uriplay.query.content.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.uriplay.beans.JsonTranslator;
import org.uriplay.content.criteria.ConjunctiveQuery;
import org.uriplay.content.criteria.ContentQuery;
import org.uriplay.content.criteria.attribute.Attributes;
import org.uriplay.content.criteria.attribute.QueryFactory;
import org.uriplay.content.criteria.attribute.StringValuedAttribute;
import org.uriplay.content.criteria.operator.Operator;
import org.uriplay.content.criteria.operator.Operators;
import org.uriplay.media.entity.Description;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.soy.common.base.Function;
import com.google.soy.common.collect.Lists;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;

public class QueryStringBackedQueryBuilder {

	private static final Operator DEFAULT_OPERATOR = Operators.EQUALS;
	private static final String ATTRIBUTE_OPERATOR_SEPERATOR = "-";
	private static final String OPERAND_SEPERATOR = ",";
	
	private final Set<String> ignoreParams = Sets.newHashSet(Selection.START_INDEX_REQUEST_PARAM, Selection.LIMIT_REQUEST_PARAM, JsonTranslator.CALLBACK); 

	private static final SelectionBuilder selectionBuilder = Selection.builder();

	@SuppressWarnings("unchecked")
	public ContentQuery build(HttpServletRequest request, Class<? extends Description> context) {
		return build(request.getParameterMap(), context).withSelection(selectionBuilder.build(request));
	}
	
	ContentQuery build(Map<String, String[]> params,  Class<? extends Description> context) {
		return buildFromFilteredMap(filter(params), context);
	}
	
	private Map<String, String[]> filter(Map<String, String[]> parameterMap) {
		Map<String, String[]> filtered = Maps.newHashMap();
		for (Entry<String, String[]> entry : parameterMap.entrySet()) {
			if (!ignoreParams.contains(entry.getKey())) {
				filtered.put(entry.getKey(), entry.getValue());
			}
		}
		return filtered;
	}
	
	private ContentQuery buildFromFilteredMap(Map<String, String[]> params, Class<? extends Description> context) {
		if (params.isEmpty()) {
			throw new IllegalArgumentException("No parameters specified");
		}
		/* Special case: one element - avoids creating a single element conjunction */
		if (params.size() == 1) {
			Entry<String, String[]> entry = Iterables.getOnlyElement(params.entrySet());
			String[] values = entry.getValue();
			if (values.length == 1) {
				return toQuery(entry.getKey(), values[0], context);
			}
		}
		ConjunctiveQuery query = new ConjunctiveQuery();
		for (Entry<String, String[]> param : params.entrySet()) {
			String attributeName = param.getKey();
			for (String value : param.getValue()) {
				query.add(toQuery(attributeName, value, context));
			}
		}
		return query;
	}
	
	private ContentQuery toQuery(String paramKey, String paramValue, Class<? extends Description> queryContext) {
		String[] parts = paramKey.split(ATTRIBUTE_OPERATOR_SEPERATOR);
		if (parts.length > 2) {
			throw new IllegalArgumentException("Malformed attribute and operator combination");
		}
		String attributeName = parts[0];
		QueryFactory<?> attribute = Attributes.lookup(attributeName, queryContext);
        if (attribute == null) {
            throw new IllegalArgumentException(attributeName + " is not a valid attribute");
        }
        
		Operator op = defaultOperator(attribute);
		if (parts.length == 2) {
			op = Operators.lookup(parts[1]);
			if (op == null) {
				throw new IllegalArgumentException("Unknown operator " + parts[1]);
			}
		} 
		
		List<String> values = Arrays.asList(paramValue.split(OPERAND_SEPERATOR));
		values = formatValues(attribute, values);
		
		return attributeQueryFor(values, op, attribute);
	}
	
	private Operator defaultOperator(QueryFactory<?> attribute) {
	    if (attribute instanceof StringValuedAttribute) {
	        String attributeName = ((StringValuedAttribute) attribute).javaAttributeName();
	        if ("title".equals(attributeName)) {
	            return Operators.SEARCH;
	        }
	    }
	    return DEFAULT_OPERATOR;
	}
	
	private List<String> formatValues(QueryFactory<?> attribute, List<String> values) {
	    List<String> formattedValues = Lists.newArrayList();
	    if (! (attribute instanceof StringValuedAttribute)) {
	        return values;
	    }
	    
	    String attributeName = ((StringValuedAttribute) attribute).javaAttributeName();
	    
	    if ("genre".equals(attributeName)) {
	        for (String value: values) {
	            if (! value.startsWith("http://")) {
	                value = "http://uriplay.org/genres/uriplay/"+value;
	            }
	            formattedValues.add(value);
	        }
	    } else if ("tag".equals(attributeName)) {
	        for (String value: values) {
                if (! value.startsWith("http://")) {
                    value = "http://uriplay.org/tags/"+value;
                }
                formattedValues.add(value);
            }
	    } else {
	        formattedValues.addAll(values);
	    }
	    
	    return formattedValues;
	}

	private ContentQuery attributeQueryFor(List<String> paramValue, Operator op, QueryFactory<?> attribute) {
		return attribute.createQuery(op,  coerceListToType(paramValue, attribute.requiresOperandOfType()));
	}

	private List<?> coerceListToType(List<String> paramValues, Class<?> requiresOperandOfType) {
		return Lists.transform(paramValues, coerceToType(requiresOperandOfType));
	}

	private Function<String, Object> coerceToType(final Class<?> type) {
		return new Function<String, Object>() {
			@Override
			public Object apply(String paramValue) {
				if (String.class.equals(type)) {
					return paramValue;
				}
				if (Integer.class.equals(type)) {
					return Integer.parseInt(paramValue);
				}
				if (DateTime.class.equals(type)) {
					return coerceToDate(paramValue);
				}
				if (Enum.class.isAssignableFrom(type)) {
					return coerceToEnumValue(paramValue, type);
				}
				if (Boolean.class.equals(type)) {
					return Boolean.valueOf(paramValue);
				}
				throw new UnsupportedOperationException();
			}
		};
	}

	@SuppressWarnings("unchecked")
	private Enum<?> coerceToEnumValue(String paramValue, Class<?> type) {
		return Enum.valueOf((Class) type, paramValue.toUpperCase());
	}

	private DateTime coerceToDate(String param) {
		if (StringUtils.isNumeric(param)) {
			return new DateTime(Long.valueOf(param));
		}
		throw new IllegalArgumentException("DateTime not in a recognised format");
	}
	
	public QueryStringBackedQueryBuilder withIgnoreParams(String... params) {
		ignoreParams.addAll(Arrays.asList(params));
		return this;
	}
}
