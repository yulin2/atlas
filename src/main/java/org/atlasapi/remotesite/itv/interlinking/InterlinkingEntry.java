package org.atlasapi.remotesite.itv.interlinking;

import org.atlasapi.media.content.Identified;

import com.metabroadcast.common.base.Maybe;

public class InterlinkingEntry<T extends Identified> {

    private final String id;
    private final Maybe<String> parentId;
    private final Maybe<Integer> index;
    private final T content;
    private Maybe<String> link = Maybe.nothing();
    
    
    public InterlinkingEntry(T content, String id) {
        this.content = content;
        this.id = id;
        this.parentId = Maybe.nothing();
        this.index = Maybe.nothing();
    }
    
    public InterlinkingEntry(T content, String id, String parentId) {
        this.content = content;
        this.id = id;
        this.parentId = Maybe.just(parentId);
        this.index = Maybe.nothing();
    }
    
    public InterlinkingEntry(T content, String id, String parentId, int index) {
        this.content = content;
        this.id = id;
        this.parentId = Maybe.just(parentId);
        this.index = Maybe.just(index);
    }

    public String getId() {
        return id;
    }
    
    public Maybe<String> getParentId() {
        return parentId;
    }

    public T getContent() {
        return content;
    }

    public Maybe<Integer> getIndex() {
        return index;
    }
    
    public InterlinkingEntry<T> withLink(String link) {
        this.link = Maybe.just(link);
        return this;
    }

    public Maybe<String> getLink() {
        return link;
    }
}