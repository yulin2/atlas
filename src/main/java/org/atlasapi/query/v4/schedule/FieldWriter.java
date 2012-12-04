package org.atlasapi.query.v4.schedule;

import java.io.IOException;

import javax.annotation.Nullable;

public interface FieldWriter {

    void writeField(String field, @Nullable Object obj) throws IOException;

    <T> void writeObject(String field, EntityWriter<? super T> writer, T obj)
        throws IOException;

    void writeList(String plural, String singular, Iterable<?> objs) throws IOException;

    <T> void writeList(EntityListWriter<? super T> listWriter,
                       Iterable<T> list) throws IOException;

}