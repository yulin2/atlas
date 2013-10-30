package org.atlasapi.application.users;

import java.util.concurrent.TimeUnit;

import org.atlasapi.media.common.Id;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.metabroadcast.common.social.model.UserRef;

public class CacheBackedUserStore implements UserStore {

    private final UserStore delegate;
    private LoadingCache<UserRef, Optional<User>> userRefCache;
    private LoadingCache<Id, Optional<User>> idCache;

    public CacheBackedUserStore(final UserStore delegate) {
        this.delegate = delegate;
        this.userRefCache = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new CacheLoader<UserRef, Optional<User>>() {
                    @Override
                    public Optional<User> load(UserRef key) throws Exception {
                        return delegate.userForRef(key);
                    }
                });
        this.idCache = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new CacheLoader<Id, Optional<User>>() {
                    @Override
                    public Optional<User> load(Id key) throws Exception {
                        return delegate.userForId(key);
                    }
                });
    }
    
    @Override
    public Optional<User> userForRef(UserRef ref) {
        return userRefCache.getUnchecked(ref);
    }

    @Override
    public Optional<User> userForId(Id id) {
        return idCache.getUnchecked(id);
    }

    @Override
    public void store(User user) {
        delegate.store(user);
        userRefCache.invalidate(user.getUserRef());
        idCache.invalidate(user.getId());
    }

    @Override
    public Iterable<User> usersFor(Iterable<Id> ids) {
        return delegate.usersFor(ids);
    }

    @Override
    public Iterable<User> allUsers() {
        return delegate.allUsers();
    }

}
