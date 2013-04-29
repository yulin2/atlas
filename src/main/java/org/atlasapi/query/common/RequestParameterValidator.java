package org.atlasapi.query.common;

import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class RequestParameterValidator {
    
    public static final Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        
        private ImmutableSet<String> required;
        private ImmutableSet<String> optional;

        public Builder withRequiredParameters(String...parameters) {
            this.required = ImmutableSet.copyOf(parameters);
            return this;
        }
        
        public Builder withOptionalParameters(String...parameters) {
            this.optional = ImmutableSet.copyOf(parameters);
            return this;
        }
        
        public RequestParameterValidator build() {
            return new RequestParameterValidator(required, optional);
        }
    }
    
    public RequestParameterValidator(ParameterNameProvider... nameProviders) {
        this(ImmutableSet.<String>of(), ImmutableSet.copyOf(Iterables.concat(Iterables.transform(
            ImmutableList.copyOf(nameProviders),
            new Function<ParameterNameProvider, Set<String>>() {
                @Override
                public Set<String> apply(ParameterNameProvider input) {
                    return input.getParameterNames();
                }
            }
        ))));
    }
    
    private static final Joiner commaJoiner = Joiner.on(", ");

    private final Set<String> requiredParams;
    private final Set<String> allParams;
    private final Set<String> optionalParams;
    
    private final String validParamMsg;
    private final ReplacementSuggestion replacementSuggestion;

    private RequestParameterValidator(Set<String> requiredParams, Set<String> optionalParams) {
        this.requiredParams = ImmutableSet.copyOf(requiredParams);
        this.optionalParams = ImmutableSet.copyOf(optionalParams);
        this.allParams = ImmutableSet.copyOf(Sets.union(this.requiredParams, this.optionalParams));
        this.validParamMsg = "Valid params: " + commaJoiner.join(allParams);
        this.replacementSuggestion = new ReplacementSuggestion(allParams, "Invalid parameters: ", " (did you mean %s?)");
    }

    public HttpServletRequest validateParameters(HttpServletRequest request) throws InvalidParameterException {
        Set<String> requestParams = paramNames(request);

        Set<String> invalidParams = Sets.difference(requestParams, allParams);
        if (!invalidParams.isEmpty()) {
            String message = replacementSuggestion.forInvalid(invalidParams);
            throw new InvalidParameterException(message + ". " + validParamMsg + ".");
        }

        Set<String> missingParams = Sets.difference(requiredParams, requestParams);
        if (!missingParams.isEmpty()) {
            String message = missingParamMessage(missingParams);
            throw new InvalidParameterException(message);
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

}
