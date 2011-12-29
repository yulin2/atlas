package org.atlasapi.output;

import java.util.List;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Person;
import org.atlasapi.media.entity.simple.ContentIdentifier;
import org.atlasapi.media.entity.simple.PeopleQueryResult;
import org.atlasapi.persistence.content.ContentResolver;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * {@link AtlasModelWriter} that translates the full URIplay object model
 * into a simplified form and renders that as XML.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class SimplePersonModelWriter extends TransformingModelWriter<Iterable<Person>, PeopleQueryResult> {

    private final ContentResolver contentResolver;

	public SimplePersonModelWriter(AtlasModelWriter<PeopleQueryResult> outputter, ContentResolver contentResolver) {
		super(outputter);
        this.contentResolver = contentResolver;
	}
	
	@Override
	protected PeopleQueryResult transform(Iterable<Person> people) {
        PeopleQueryResult simplePeople = new PeopleQueryResult();
        simplePeople.setPeople(Iterables.transform(people, new Function<Person, org.atlasapi.media.entity.simple.Person>() {
            @Override
            public org.atlasapi.media.entity.simple.Person apply(Person input) {
                return simplify(input);
            }
        }));
        return simplePeople;
    }
	
    public org.atlasapi.media.entity.simple.Person simplify(Person fullPerson) {
        org.atlasapi.media.entity.simple.Person person = new org.atlasapi.media.entity.simple.Person();
        person.setType(Person.class.getSimpleName());
        person.setUri(fullPerson.getCanonicalUri());
        person.setCurie(fullPerson.getCurie());
        person.setName(fullPerson.getTitle());
        person.setProfileLinks(fullPerson.getAliases());
        person.setContent(simpleContentListFrom(fullPerson.getContents()));
        
        return person;
    }

    private List<ContentIdentifier> simpleContentListFrom(Iterable<ChildRef> contents) {
        List<ContentIdentifier> contentList = Lists.newArrayList();
        for (ChildRef ref : contents) {
            contentList.add(ContentIdentifier.identifierFor(ref));
        }
        return contentList;
    }

}
