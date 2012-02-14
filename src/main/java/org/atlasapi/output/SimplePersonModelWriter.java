package org.atlasapi.output;

import java.util.Set;

import org.atlasapi.media.entity.Person;
import org.atlasapi.media.entity.simple.PeopleQueryResult;
import org.atlasapi.output.simple.PersonModelSimplifier;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * {@link AtlasModelWriter} that translates the full URIplay object model
 * into a simplified form and renders that as XML.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class SimplePersonModelWriter extends TransformingModelWriter<Iterable<Person>, PeopleQueryResult> {

    private final PersonModelSimplifier personSimplifier;

	public SimplePersonModelWriter(AtlasModelWriter<PeopleQueryResult> outputter) {
		super(outputter);
        this.personSimplifier = new PersonModelSimplifier();
	}
	
	@Override
	protected PeopleQueryResult transform(Iterable<Person> people, final Set<Annotation> annotations) {
        PeopleQueryResult simplePeople = new PeopleQueryResult();
        simplePeople.setPeople(Iterables.transform(people, new Function<Person, org.atlasapi.media.entity.simple.Person>() {

            @Override
            public org.atlasapi.media.entity.simple.Person apply(Person input) {
                return personSimplifier.simplify(input, annotations);
            }
        }));
        return simplePeople;
    }
	
}
