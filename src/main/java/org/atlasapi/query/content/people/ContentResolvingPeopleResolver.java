package org.atlasapi.query.content.people;

import java.util.List;

import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Person;
import org.atlasapi.persistence.content.PeopleResolver;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class ContentResolvingPeopleResolver {
    
    private final PeopleResolver delegate;
    private final KnownTypeQueryExecutor contentResolver;

    public ContentResolvingPeopleResolver(PeopleResolver delegate, KnownTypeQueryExecutor contentResolver) {
        this.delegate = delegate;
        this.contentResolver = contentResolver;
    }

    public Person person(String uri, ContentQuery filter) {
        Person person = delegate.person(uri);
        
        if (person != null) {
            List<Content> content = ImmutableList.copyOf(Iterables.filter(Iterables.concat(contentResolver.executeUriQuery(person.getContentUris(), filter).values()), Content.class));
            person.setContents(content);
        }
        
        return person;
    }
}
