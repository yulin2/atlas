package org.atlasapi.query.v4.schedule;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Joiner.MapJoiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class RequestParameterValidator {
    
    private static final MapJoiner suggestionJoiner = Joiner.on("?), ").withKeyValueSeparator("(");
    private static final Joiner commaJoiner = Joiner.on(", ");

    private final Set<String> requiredParams;
    private final Set<String> allParams;
    private final Set<String> optionalParams;

    public RequestParameterValidator(Set<String> requiredParams, Set<String> validParams) {
        this.requiredParams = ImmutableSet.copyOf(requiredParams);
        this.optionalParams = ImmutableSet.copyOf(validParams);
        this.allParams = Sets.union(this.requiredParams, this.optionalParams);
    }

    public HttpServletRequest validateParameters(HttpServletRequest request) {
        Set<String> requestParams = paramNames(request);

        Set<String> invalidParams = Sets.difference(requestParams, allParams);
        if (!invalidParams.isEmpty()) {
            String message = invalidParamMessage(invalidParams);
            throw new IllegalArgumentException(message);
        }

        Set<String> missingParams = Sets.difference(requiredParams, requestParams);
        if (!missingParams.isEmpty()) {
            String message = missingParamMessage(missingParams);
            throw new IllegalArgumentException(message);
        }

        return request;
    }

    @SuppressWarnings("unchecked")
    private Set<String> paramNames(HttpServletRequest request) {
        return request.getParameterMap().keySet();
    }

    private String missingParamMessage(Set<String> missingParams) {
        return String.format("Missing parameters: %s.", commaJoiner.join(missingParams));
    }

    private String invalidParamMessage(Set<String> invalidParams) {
        Map<String, String> suggestions = Maps.newHashMap();
        for (String invalid : invalidParams) {
            String suggestion = findSuggestion(invalid, allParams);
            if (suggestion != null) {
                suggestions.put(invalid, suggestion);
            }
        }

        int invalidCount = invalidParams.size();
        if (suggestions.size() == invalidCount) {
            return String.format("Invalid parameters: %s.", suggestionJoiner.join(suggestions.entrySet()));
        }

        return String.format("Invalid parameters: %s. Valid params: %s.", commaJoiner.join(invalidParams), commaJoiner.join(allParams));
    }

    private String findSuggestion(String invalid, Set<String> validParams) {
        for (String valid : validParams) {
            int distance = StringUtils.getLevenshteinDistance(valid, invalid);
            int maxDistance = 2;
            if (distance < maxDistance) {
                return valid;
            }
        }
        return null;
    }

}
