package org.atlasapi.output;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.atlasapi.query.common.ActiveAnnotations;
import org.atlasapi.query.common.InvalidAnnotationException;
import org.atlasapi.query.common.ReplacementSuggestion;
import org.atlasapi.query.common.Resource;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;


public class AnnotationLookup {

    public static final Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        
        Multimap<String, PathAnnotation> lookup
            = HashMultimap.create();
        Multimap<List<Resource>, String> prefices = HashMultimap.create();
        
        public Builder withImplicitSingleContext(Resource resource, 
                Iterable<Annotation> annotations) {
            ImmutableList<Resource> resources = ImmutableList.of(resource);
            lookup.putAll(buildLookupMap("", resources, annotations));
            lookup.putAll(buildLookupMap(resource.getSingular() + ".", resources, annotations));
            return this;
        }
        
        public Builder withImplicitListContext(Resource resource, 
                Iterable<Annotation> annotations) {
            ImmutableList<Resource> resources = ImmutableList.of(resource);
            lookup.putAll(buildLookupMap("", resources, annotations));
            lookup.putAll(buildLookupMap(resource.getPlural() + ".", resources, annotations));
            return this;
        }
        
        public Builder withExplicitSingleContext(Resource resource,
                Iterable<Annotation> annotations) {
            ImmutableList<Resource> resources = ImmutableList.of(resource);
            lookup.putAll(buildLookupMap(resource.getSingular() + ".", resources, annotations));
            return this;
        }
        
        public Builder withExplicitListContext(Resource resource,
                Iterable<Annotation> annotations) {
            ImmutableList<Resource> resources = ImmutableList.of(resource);
            lookup.putAll(buildLookupMap(resource.getPlural() + ".", resources, annotations));
            return this;
        }

        private Multimap<String, PathAnnotation> buildLookupMap(
                String prefix, List<Resource> resources, Iterable<Annotation> annotations) {
            ImmutableMultimap.Builder<String, PathAnnotation> prefixedAnnotations
                = ImmutableMultimap.builder();
            prefices.put(resources, prefix);
            for (Annotation annotation : annotations) {
                prefixedAnnotations.put(prefix + annotation.toKey(),
                        new PathAnnotation(resources, annotation));
            }
            return prefixedAnnotations.build();
        }
        
        public AnnotationLookup build() {
            return new AnnotationLookup(lookup);
        }

        public Builder attachLookup(Resource context, Annotation attachment,
                AnnotationLookup child) {
            ImmutableMultimap.Builder<String, PathAnnotation> additions = ImmutableMultimap.builder();
            Multimap<String, PathAnnotation> childLookup = child.lookup;
            for (Entry<List<Resource>, String> prefixMapping : prefices.entries()) {
                if (Iterables.getLast(prefixMapping.getKey()).equals(context)) {
                    additions.put(prefixMapping.getValue() + attachment.toKey(),
                        new PathAnnotation(prefixMapping.getKey(), attachment)
                    );
                    for (Entry<String, PathAnnotation> children : childLookup.entries()) {
                        List<Resource> path = ImmutableList.<Resource> builder()
                                .addAll(prefixMapping.getKey())
                                .addAll(children.getValue().getPath())
                                .build();
                        String key = prefixMapping.getValue() + attachment.toKey() + "." + children.getKey();
                        additions.put(key, new PathAnnotation(path, children.getValue().getAnnotation()));
                        additions.put(key, new PathAnnotation(prefixMapping.getKey(), attachment));
                        if (children.getValue().getAnnotation().equals(Annotation.ID)) {
                            additions.put(prefixMapping.getValue() + attachment.toKey(), new PathAnnotation(path, Annotation.ID));
                        }
                    }
                }
            }
            lookup.putAll(additions.build());
            return this;
        }
    }
    
    private static final class PathAnnotation {
        
        private final List<Resource> path;
        private final Annotation annotation;
        
        public PathAnnotation(List<Resource> path, Annotation annotation) {
            this.path = path;
            this.annotation = annotation;
        }

        public List<Resource> getPath() {
            return path;
        }

        public Annotation getAnnotation() {
            return annotation;
        }

    }
    
    private final ImmutableMultimap<String, PathAnnotation> lookup;
    private final ReplacementSuggestion replacmentSuggestion;
    
    public AnnotationLookup(Multimap<String, PathAnnotation> lookup) {
        this.lookup = ImmutableMultimap.copyOf(lookup);
        this.replacmentSuggestion = new ReplacementSuggestion(lookup.keySet(), 
                "Invalid annotations: ", " (did you mean %s?)");
    }

    public ActiveAnnotations lookup(Iterable<String> keys) throws InvalidAnnotationException {
        ImmutableSetMultimap.Builder<List<Resource>, Annotation> annotations
            = ImmutableSetMultimap.builder();
        List<String> invalid = Lists.newArrayList();
        for (String key : keys) {
            Collection<PathAnnotation> paths = lookup.get(key);
            if (paths == null || paths.isEmpty()) {
                invalid.add(key);
            } else { 
                for(PathAnnotation pathAnnotation : paths) {
                    annotations.putAll(pathAnnotation.getPath(), pathAnnotation.getAnnotation());
                }
            }
        }
        if (!invalid.isEmpty()) {
            throw new InvalidAnnotationException(replacmentSuggestion.forInvalid(invalid), invalid);
        }
        return new ActiveAnnotations(annotations.build());
    }
    
}
