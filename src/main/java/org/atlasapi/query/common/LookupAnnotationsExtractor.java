package org.atlasapi.query.common;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.output.AnnotationLookup;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

public class LookupAnnotationsExtractor implements AnnotationsExtractor {

    private static final String DEFAULT_PARAMETER_NAME = "annotations";

    private final AnnotationLookup lookup;
    private final String parameterName;

    private final Splitter csvSplitter = Splitter.on(",").omitEmptyStrings().trimResults();

    public LookupAnnotationsExtractor(AnnotationLookup lookup) {
        this(DEFAULT_PARAMETER_NAME, lookup);
    }

    public LookupAnnotationsExtractor(String parameterName, AnnotationLookup lookup) {
        this.parameterName = checkNotNull(parameterName);
        this.lookup = checkNotNull(lookup);
    }

    @Override
    public ActiveAnnotations extractFromRequest(HttpServletRequest request)
            throws InvalidAnnotationException {

        String serialisedAnnotations = request.getParameter(parameterName);

        if (serialisedAnnotations == null) {
            return ActiveAnnotations.standard();
        }

        return lookup.lookup(csvSplitter.split(serialisedAnnotations));
    }

    @Override
    public ImmutableSet<String> getParameterNames() {
        return ImmutableSet.of(parameterName);
    }
}
