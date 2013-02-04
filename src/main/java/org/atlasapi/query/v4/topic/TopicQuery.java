package org.atlasapi.query.v4.topic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.Nullable;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.content.criteria.AtomicQuery;
import org.atlasapi.content.criteria.BooleanAttributeQuery;
import org.atlasapi.content.criteria.DateTimeAttributeQuery;
import org.atlasapi.content.criteria.EnumAttributeQuery;
import org.atlasapi.content.criteria.IdAttributeQuery;
import org.atlasapi.content.criteria.IntegerAttributeQuery;
import org.atlasapi.content.criteria.MatchesNothing;
import org.atlasapi.content.criteria.QueryVisitor;
import org.atlasapi.content.criteria.StringAttributeQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.output.Annotation;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.query.Selection;

public class TopicQuery {

    private static final String ID_ATTRIBUTE_NAME = "id";
    private final ImmutableSet<AtomicQuery> operands;
    private final Selection selection;
    private final ApplicationConfiguration appConfig;
    private final ImmutableSet<Annotation> annotations;
    private final boolean list;

    public TopicQuery(Iterable<? extends AtomicQuery> operands, Selection selection,
        ApplicationConfiguration appConfig, Iterable<Annotation> annotations, boolean list) {
        this.operands = ImmutableSet.copyOf(operands);
        this.selection = checkNotNull(selection);
        this.appConfig = checkNotNull(appConfig);
        this.annotations = ImmutableSet.copyOf(annotations);
        this.list = list;
    }

    public ImmutableSet<AtomicQuery> getOperands() {
        return this.operands;
    }

    public Selection getSelection() {
        return this.selection;
    }
    
    public ApplicationConfiguration getApplicationConfiguration() {
        return this.appConfig;
    }
    
    public ImmutableSet<Annotation> getAnnotations() {
        return this.annotations;
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("query", Joiner.on(", ").join(operands))
            .add("selection", selection)
            .add("annotations", annotations)
            .toString();
    }
    
    public <V> List<V> accept(QueryVisitor<V> v) {
        ImmutableList.Builder<V> result = ImmutableList.builder();
        for (AtomicQuery operand : operands) {
            result.add(operand.accept(v));
        }
        return result.build();
    }

    public Predicate<? super Topic> asSourceFilter() {
        return new Predicate<Topic>() {

            @Override
            public boolean apply(@Nullable Topic input) {
                return appConfig.getEnabledSources().contains(input.getPublisher());
            }
        };
    }

    public Optional<List<Id>> getIdsIfOnly() {
        return operands.size() == 1 ? getIdsIfOnly(Iterables.getOnlyElement(operands))
                                    : Optional.<List<Id>>absent();
    }
    

    private Optional<List<Id>> getIdsIfOnly(AtomicQuery onlyOperand) {
        return Optional.fromNullable(onlyOperand.accept(new QueryVisitor<List<Id>>(){

            @Override
            public List<Id> visit(IntegerAttributeQuery query) {
                return null;
            }

            @Override
            public List<Id> visit(StringAttributeQuery query) {
                return null;
            }

            @Override
            public List<Id> visit(BooleanAttributeQuery query) {
                return null;
            }

            @Override
            public List<Id> visit(EnumAttributeQuery<?> query) {
                return null;
            }

            @Override
            public List<Id> visit(DateTimeAttributeQuery dateTimeAttributeQuery) {
                return null;
            }

            @Override
            public List<Id> visit(MatchesNothing noOp) {
                return null;
            }

            @Override
            public List<Id> visit(IdAttributeQuery query) {
                if (ID_ATTRIBUTE_NAME.equals(query.getAttributeName())) {
                    return query.getValue();
                }
                return null;
            }
        }));
    }

    public boolean isListQuery() {
        return list;
    }
    
}
