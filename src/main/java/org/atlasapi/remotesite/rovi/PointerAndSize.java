package org.atlasapi.remotesite.rovi;

import com.google.common.base.Objects;

public class PointerAndSize {

    private final long pointer;
    private final int size;

    public PointerAndSize(long pointer, int size) {
        this.pointer = pointer;
        this.size = size;
    }

    public long getPointer() {
        return pointer;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("pointer", pointer).add("size", size).toString();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PointerAndSize)) {
            return false;
        }

        PointerAndSize that = (PointerAndSize) other;

        return this.pointer == that.pointer && this.size == that.size;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(pointer, size);
    }

}
