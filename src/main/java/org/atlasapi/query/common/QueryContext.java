package org.atlasapi.query.common;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.application.ApplicationConfiguration;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.metabroadcast.common.query.Selection;

public class QueryContext {

    private static final QueryContext STANDARD = new QueryContext(
            ApplicationConfiguration.defaultConfiguration(), 
            ActiveAnnotations.standard()    
    );
    
    public static final QueryContext standard() {
        return STANDARD;
    }
    
    private final ApplicationConfiguration appConfig;
    private final ActiveAnnotations annotations;
    private final Optional<Selection> selection;

    public QueryContext(ApplicationConfiguration appConfig, ActiveAnnotations annotations) {
        this(appConfig, annotations, null);
    }
    
    public QueryContext(ApplicationConfiguration appConfig, ActiveAnnotations annotations,
        Selection selection) {
        this.appConfig = checkNotNull(appConfig);
        this.annotations = checkNotNull(annotations);
        this.selection = Optional.fromNullable(selection);
    }

    public ApplicationConfiguration getApplicationConfiguration() {
        return this.appConfig;
    }

    public ActiveAnnotations getAnnotations() {
        return this.annotations;
    }
    
    public Optional<Selection> getSelection() {
        return this.selection;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof QueryContext) {
            QueryContext other = (QueryContext) that;
            return appConfig.equals(other.appConfig)
                && annotations.equals(other.annotations)
                && selection.equals(other.selection);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return appConfig.hashCode() ^ annotations.hashCode() ^ selection.hashCode();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("config", appConfig)
            .add("annotations", annotations)
            .add("selection", selection)
            .toString();
    }
    
}
