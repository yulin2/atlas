package org.atlasapi.equiv.update;

public class NullEquivalenceUpdater<T> implements EquivalenceUpdater<T> {

    private enum NullUpdater implements EquivalenceUpdater<Object> {
        INSTANCE {
            @Override
            public void updateEquivalences(Object content) {
            }
        };

        @SuppressWarnings("unchecked")
        <T> EquivalenceUpdater<T> withNarrowedType() {
            return (EquivalenceUpdater<T>) this;
        }
    }

    public static final <T> EquivalenceUpdater<T> get() {
        return NullUpdater.INSTANCE.withNarrowedType();
    }

    private NullEquivalenceUpdater() {
    }

    @Override
    public void updateEquivalences(T content) {

    }

}
