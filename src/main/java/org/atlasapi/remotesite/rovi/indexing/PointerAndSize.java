package org.atlasapi.remotesite.rovi.indexing;

import com.google.common.base.Objects;
import com.google.common.primitives.Longs;


/**
 * Represent a pair of pointer and size of a line into a file. 
 * The pointer is the offset where the line starts, measured in bytes, from the beginning of this file. 
 * The size is the number of bytes that form the line.
 *
 */
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
        if (this == other) {
            return true;
        }
        
        if (!(other instanceof PointerAndSize)) {
            return false;
        }

        PointerAndSize that = (PointerAndSize) other;

        return this.pointer == that.pointer && this.size == that.size;
    }
    
    @Override
    public int hashCode() {
        return Longs.hashCode(pointer);
    }

}
