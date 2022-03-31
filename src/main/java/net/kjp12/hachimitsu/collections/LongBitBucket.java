package net.kjp12.hachimitsu.collections;// Created 2022-11-03T01:46:31

/**
 * @author KJP12
 * @since ${version}
 **/
abstract class LongBitBucket {
    protected final long relative;
    protected int size;

    protected LongBitBucket(long relative, int size) {
        this.relative = relative;
        this.size = size;
    }

    /**
     * The relative long of the bucket. Is always the most significant 56 bits.
     */
    final long relative() {
        return relative;
    }

    /**
     * Tests if the long is compatible with the bucket.
     *
     * @param l The long to test.
     * @return true if l is compatible/relative to the bucket, false otherwise.
     */
    final boolean isRelative(long l) {
        return relative() == (l & ~255);
    }

    /**
     * The current size of the bucket.
     */
    final int size() {
        return size;
    }

    /**
     * Whether the bucket contains a long or not.
     */
    abstract boolean contains(long l);

    /**
     * Removes a long from the bucket. True if removed.
     */
    abstract boolean remove(long l);

    /**
     * Adds a long to the bucket. True if added.
     */
    abstract boolean add(long l);

    /**
     * Clears the bucket of any longs.
     */
    abstract void clear();

    /**
     * Gets the long at a given index. Invalid index is undefined behaviour.
     */
    abstract long rawGet(int i);

    /**
     * Gets the next index relative to the given index, -1 if there is no more.
     */
    abstract int nextIndex(int i);

    /**
     * Gets the next index relative to the given index, skipping a given count.
     */
    abstract int nextIndex(int i, int s);

    /** Converts the bucket into an NBT element. Implementation-defined. */
    //abstract NbtElement toNbt();
}
