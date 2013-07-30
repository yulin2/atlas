package org.atlasapi.output.simple;

import javax.annotation.Nullable;

import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Person;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

/**
 * Represents a {@link CrewMember} from a piece of
 * {@link org.atlasapi.media.entity.Content|Content} its possible related
 * {@link Person}.
 * 
 */
public class CrewMemberAndPerson {
    
    private final CrewMember member;
    private final Optional<Person> person;
    
    public CrewMemberAndPerson(CrewMember member, @Nullable Person person) {
        this(member, Optional.fromNullable(person));
    }

    public CrewMemberAndPerson(CrewMember member, Optional<Person> person) {
        this.member = member;
        this.person = person;
    }
    
    public CrewMember getMember() {
        return this.member;
    }

    public Optional<Person> getPerson() {
        return this.person;
    }
    
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof CrewMemberAndPerson) {
            CrewMemberAndPerson other = (CrewMemberAndPerson) that;
            return this.member.equals(other.member)
                && this.person.equals(other.person);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.member.hashCode() ^ this.person.hashCode();
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(getClass())
                .add("member", member)
                .add("person", person)
                .toString();
    }
    
}
