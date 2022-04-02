package net.kjp12.hachimitsu.collections;// Created 2022-11-03T01:37:29

import it.unimi.dsi.fastutil.longs.AbstractLongSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.TestOnly;

import java.util.NoSuchElementException;

/**
 * A long set based around buckets of hash-keys and bits.
 *
 * @author KJP12
 * @see LongBitBucket
 * @see LongBitHashBucket
 * @see LongBitSetBucket
 * @since ${version}
 **/
public class LongBitHashSet extends AbstractLongSet implements LongSet {
    private int size;
    private LongBitBucket[] buckets = new LongBitBucket[8];

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public LongIterator iterator() {
        return isEmpty() ? LongIterators.EMPTY_ITERATOR : new j$itr(buckets);
    }

    @Override
    public void clear() {
        for (var bucket : buckets) {
            if (bucket != null) bucket.clear();
        }
        size = 0;
    }

    @Override
    public boolean add(long l) {
        if (add0(l)) {
            size++;
            return true;
        }
        return false;
    }

    private boolean add0(long l) {
        return fetch(l).add(l) || grow(l).add(l);
    }

    @Override
    public boolean contains(long l) {
        var bucket = buckets[index(l)];
        return bucket != null && bucket.contains(l);
    }

    @Override
    public long[] toArray(long[] longs) {
        if (longs == null || longs.length < size) longs = new long[size];
        int i = LongIterators.unwrap(longIterator(), longs);
        assert i == size : "Unexpected mismatch: expected " + size + " but got " + i + "!";
        return longs;
    }

    @Override
    public boolean remove(long l) {
        if (remove0(l)) {
            size--;
            return true;
        }
        return false;
    }

    private boolean remove0(long l) {
        var bucket = buckets[index(l)];
        return bucket != null && bucket.remove(l);
    }

    /**
     * Fetches or creates a bucket based on the given long.
     *
     * @param l The long to base the bucket off of.
     * @return A memory-compressed bucket of longs.
     */
    private LongBitBucket fetch(long l) {
        LongBitBucket bucket;
        if ((bucket = fetch0(l)) == null) {
            return create1(buckets, l);
        }
        if (bucket.isRelative(l)) {
            return bucket;
        }
        return grow0(l);
    }

    private LongBitBucket fetch0(long l) {
        return buckets[index(l)];
    }

    /**
     * Converts to a {@link LongBitSetBucket} when the bucket exists.
     *
     * @param l The long to base the bucket off of.
     * @return A memory-compressed bucket of longs.
     */
    private LongBitBucket grow(long l) {
        LongBitBucket bucket;
        if ((bucket = fetch0(l)) instanceof LongBitHashBucket && !bucket.contains(l)) {
            return create2(buckets, bucket);
        }
        return bucket;
    }

    /**
     * Grows the underlying array, creating a new {@link LongBitHashBucket} in the process.
     *
     * @param insert The long to resolve the conflict of and insert into the hashset.
     * @return A new {@link LongBitHashBucket} based on {@code insert}.
     * @throws OutOfMemoryError When the conflict cannot be resolved by resizing.
     */
    private LongBitBucket grow0(long insert) {
        LongBitBucket[] old;
        return create1(copy0(old = this.buckets, this.buckets = create0(old, insert)), insert);
    }

    private static LongBitBucket[] create0(LongBitBucket[] old, long insert) {
        return new LongBitBucket[size0(insert, old[index0(insert, old)].relative())];
    }

    private static LongBitHashBucket create1(LongBitBucket[] to, long insert) {
        var bucket = new LongBitHashBucket(insert & ~255);
        insert0(bucket, to);
        return bucket;
    }

    private static LongBitSetBucket create2(LongBitBucket[] to, LongBitBucket old) {
        var bucket = new LongBitSetBucket(old);
        insert0(bucket, to);
        return bucket;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static LongBitBucket[] copy0(LongBitBucket[] from, LongBitBucket[] to) {
        LongBitBucket bucket;
        for (int i = 0; i < from.length; i++) {
            if ((bucket = from[i]) != null) insert0(bucket, to);
        }
        return to;
    }

    private static void insert0(LongBitBucket bucket, LongBitBucket[] to) {
        to[index0(bucket.relative(), to)] = bucket;
    }

    /**
     * This attempts to resolve the required size by xoring the colliding bucket's relative
     * and the new long's high 56 bits, getting the lowest bit of the high 56 bits, then
     * bound-checking by converting to an integer then checking if it's less than or equal to zero.
     *
     * @param insert   The long to resolve the size conflict of.
     * @param relative The long to resolve the size conflict against.
     * @return The required size of the array, always positive.
     * @throws OutOfMemoryError When the conflict cannot be resolved by resizing.
     */
    private static int size0(long insert, long relative) {
        int newSize;
        if ((newSize = (int) (Long.lowestOneBit((insert ^ relative) >>> 8) << 1)) <= 0)
            oome0(insert, relative);
        return newSize;
    }

    @Contract("_, _ -> fail")
    private static void oome0(long insert, long relative) {
        throw new OutOfMemoryError("Unable to insert " + insert + " as it collides with " + relative + " in a non-resolvable manner.");
    }

    /**
     * Fetches the hashed index from the given long.
     */
    private int index(long l) {
        return index0(l, buckets);
    }

    private static int index0(long l, LongBitBucket[] buckets) {
        return (int) (l >>> 8) & (buckets.length - 1);
    }

    /**
     * Converts the long set into an NBT compound, storing the internal structure in a raw form.
     * */
    //public NbtCompound toNbt() {
    //    var buckets = this.buckets;
    //    var list = new NbtCompound();
    //    list.putInt("size", size);
    //    list.putInt("arr", buckets.length);
    //    for(int i = 0, l = buckets.length; i < l; i++) {
    //        if(buckets[i] != null) {
    //            list.put(Integer.toString(i), buckets[i].toNbt());
    //        }
    //    }
    //    return list;
    //}

    /**
     * Constructs a long set based off the internal structure stored in the NBT compound.
     *
     * @param list The NBT Compound storing the set.
     * @return A long set based off the stored NBT.
     * */
    //public static LongBitHashSet fromNbt(NbtCompound list) {
    //    var set = new LongBitHashSet();
    //    set.size = list.getInt("size");
    //    var buckets = set.buckets = new LongBitBucket[list.getInt("arr")];
    //    list.getKeys().stream().filter(StringUtils::isNumeric).sorted().forEachOrdered(str -> buckets[Integer.parseInt(str)] = fromNbt0(list.getCompound(str)));
    //    return set;
    //}

    /**
     * Constructs a {@link LongBitBucket} based off the internal structure stored in the NBT compound.
     *
     * @param compound The NBT Compound storing the bucket.
     * @return {@link LongBitHashBucket} or {@link LongBitSetBucket} based on the internal structure.
     * @throws IllegalArgumentException When there's no bucket corresponding to the structure.
     * */
    //private static LongBitBucket fromNbt0(NbtCompound compound) {
    //    if(compound.contains("a", NbtElement.BYTE_ARRAY_TYPE)) {
    //        return new LongBitHashBucket(compound.getLong("r"), compound.getByteArray("a"));
    //    } else if(compound.contains("w", NbtElement.LONG_ARRAY_TYPE)) {
    //        return new LongBitSetBucket(compound.getLong("r"), compound.getLongArray("w"));
    //    }
    //    throw new IllegalArgumentException("unknown type");
    //}

    class j$itr2 implements LongIterator {
        /**
         * Internal reference of buckets to avoid giving the same answer twice,
         * violating the assumption of {@link java.util.Set sets}.
         */
        private final LongBitBucket[] buckets = LongBitHashSet.this.buckets;
        // private LongBitBucket rawBucket;
        private int bucket = -1, bucketIndex, bucketIndexNext, bucketLongs;

        {
            nextBucket();
        }

        private LongBitBucket currentBucket() {
            if (bucket < 0) {
                return null;
            }
            return buckets[bucket];
        }

        private LongBitBucket nextBucket() {
            LongBitBucket b = null;
            int i = bucket;
            do {
                if (++i >= buckets.length) {
                    i = -1;
                    break;
                }
                b = buckets[i];
            } while (b == null);
            bucket = i;
            bucketLongs = 0;
            // rawBucket = b;
            if (b != null) {
                bucketIndex = -1;
                bucketIndexNext = b.nextIndex(-1);
            }
            return b;
        }

        @Override
        public long nextLong() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            bucketLongs++;
            return currentBucket().rawGet(bucketIndex = bucketIndexNext);
        }

        @Override
        public boolean hasNext() {
            if (bucketIndexNext > bucketIndex) {
                return true;
            }
            var bucket = currentBucket();
            if (bucket == null) {
                return false;
            }
            int next = bucket.nextIndex(bucketIndex);
            if (next < 0) {
                bucket = nextBucket();
                return bucket != null;
            } else {
                bucketIndexNext = next;
            }
            return true;
        }

        @Override
        public String toString() {
            return "j$itr2{" +
                    "bucket=" + bucket +
                    ", bucketIndex=" + bucketIndex +
                    ", bucketIndexNext=" + bucketIndexNext +
                    ", bucketLongs=" + bucketLongs +
                    '}';
        }
    }

    /**
     * Long iterator adapted for the bucket-based approach of the {@link LongBitHashSet}.
     */
    static class j$itr implements LongIterator {
        private final LongBitBucket[] buckets;
        boolean peeked;
        int bucket = -1, bucketIndex = -1, bucketLongs;

        /**
         * Keeps an independent reference of the buckets to try to guarantee that
         * the same long will not be iterated over twice.
         * <p>
         * This will also attempt to advance the bucket to the next valid state if possible.
         *
         * @param buckets The internal bucket array from {@link LongBitHashSet#buckets}.
         */
        j$itr(LongBitBucket[] buckets) {
            this.buckets = buckets;
            // Forces iterator to not be at a potentially invalid state.
            advanceBucket(true);
        }

        @Override
        public long nextLong() {
            if (bucket < 0) {
                throw new NoSuchElementException();
            }
            var bucket = buckets[this.bucket];
            if (isInvalid(bucket)) {
                bucket = advanceBucket(false);
            }
            bucketLongs++;
            peeked = false;
            return bucket.rawGet(bucketIndex);
        }

        /**
         * Optimised skip implementation by counting via buckets first,
         * then entering the bucket's optimised skip method.
         *
         * @param n The amount to skip.
         * @return How much was skipped, returning {@code n} if the iterator has something available.
         */
        @Override
        public int skip(int n) {
            int ni = n, r = 0;
            var bucket = buckets[this.bucket];
            while (isInvalid(bucket) || (r = bucket.size() - bucketLongs) >= ni) {
                bucket = advanceBucket(true);
                ni -= r;
                if (bucket == null) return n - ni - 1;
                r = 0;
            }
            bucketIndex = bucket.nextIndex(bucketIndex, ni);
            peeked = true;
            return n;
        }

        /**
         * Advances to the next valid bucket, returning null or failing when there's no more buckets.
         * <p>
         * This linearly scans each bucket until there is a non-null & non-empty bucket available.
         *
         * @param returnOnFail If the method should return instead of throwing on no more buckets.
         * @return The next available bucket, or if there's no more buckets, and {@code returnOnFail}
         * is set to {@code true}, returns null.
         * @throws NoSuchElementException When there's no more buckets and {@code returnOnFail} is set
         *                                to {@code} false.
         */
        @Contract("false -> !null")
        @TestOnly
        LongBitBucket advanceBucket(boolean returnOnFail) {
            LongBitBucket bucket = advanceBucket0();
            if (bucket == null) {
                if (returnOnFail) {
                    return null;
                }
                throw new NoSuchElementException();
            }
            // int i = this.bucket, l = buckets.length;
            // // if(i >= 0 && buckets[i].size() != bucketLongs) {
            // //     throw new AssertionError("Expected " + buckets[i].size() +", got " + bucketLongs + " @ " + i);
            // // }
            // do {
            //     if(++i >= l) {
            //         this.bucket = -1;
            //         if(returnOnFail) return null;
            //         throw new NoSuchElementException();
            //     }
            //     bucket = buckets[i];
            // } while(bucket == null || bucket.size() == 0);
            // this.bucket = i;
            bucketLongs = 0;
            bucketIndex = bucket.nextIndex(-1);
            peeked = true;
            return bucket;
        }

        private LongBitBucket advanceBucket0() {
            LongBitBucket bucket;
            int i = this.bucket, l = buckets.length;
            do {
                if (++i >= l) {
                    i = -1;
                    bucket = null;
                    break;
                }
                bucket = buckets[i];
            } while (bucket == null || bucket.size() == 0);
            this.bucket = i;
            return bucket;
        }

        /**
         * Tests if the bucket is null or has looped back to 0.
         *
         * @param bucket The bucket to test.
         * @return true if the bucket is null or the bucketIndex looped back to -1, false otherwise.
         */
        private boolean isInvalid(LongBitBucket bucket) {
            return !peeked && (bucket == null || (bucketIndex = bucket.nextIndex(bucketIndex)) < 0);
        }

        /**
         * Tests if the iterator has an answer to give.
         * <p>
         * This has a side effect of attempting to advance to the next bucket when
         * the iterator has yet to peek at the buckets.
         *
         * @return true if there's a bucket available, false otherwise.
         */
        @Override
        public boolean hasNext() {
            if (bucket < 0) {
                return false;
            }
            var bucket = buckets[this.bucket];
            if (isInvalid(bucket)) {
                bucket = advanceBucket(true);
            }
            peeked = true;
            return bucket != null;
        }
    }
}
