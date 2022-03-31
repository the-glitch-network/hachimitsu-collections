package net.kjp12.hachimitsu.collections;// Created 2022-15-03T23:10:48

import java.util.Arrays;

/**
 * @author KJP12
 * @since ${version}
 **/
class LongBitSetBucket extends LongBitBucket {
    final long[] words;

    LongBitSetBucket(LongBitBucket bucket) {
        super(bucket.relative(), bucket.size());
        copy0(bucket, this.words = new long[4]);
    }

    LongBitSetBucket(long relative, long[] words) {
        super(relative, countBits(words));
        this.words = words;
    }

    private static void copy0(LongBitBucket bucket, long[] words) {
        int i = -1;
        long l;
        while ((i = bucket.nextIndex(i)) >= 0) {
            words[index(l = bucket.rawGet(i))] |= word(l);
        }
    }

    // Suppressed as enhanced for / foreach is more expensive in bytecode,
    // which isn't ideal when trying to keep within the default inlining limit.
    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static int countBits(long[] words) {
        int s = 0;
        for (int i = 0, l = words.length; i < l; i++) s += Long.bitCount(words[i]);
        return s;
    }

    @Override
    public boolean contains(long l) {
        return isRelative(l) && (words[index(l)] & word(l)) != 0;
    }

    @Override
    public boolean remove(long l) {
        if (remove0(words, l)) {
            size--;
            return true;
        }
        return false;
    }

    /**
     * Remove implementation only reliant on the underlying array & long.
     * <p>
     * Intended to keep the methods below the 35-byte threshold for inlining.
     *
     * @param words The backing bitset for the bucket.
     * @param l     The long to removed.
     * @return If the backing bitset was changed as a result of removing the long.
     */
    private static boolean remove0(final long[] words, long l) {
        final int index = index(l);
        return words[index] != (words[index] &= ~word(l));
    }

    @Override
    public boolean add(long l) {
        if (add0(words, l)) {
            size++;
            return true;
        }
        return false;
    }

    /**
     * Add implementation only reliant on the underlying array & long.
     * <p>
     * Intended to keep the methods below the 35-byte threshold for inlining.
     *
     * @param words The backing bitset for the bucket.
     * @param l     The long to insert.
     * @return If the backing bitset was chaanged as a result of adding the long.
     */
    private static boolean add0(final long[] words, long l) {
        final int index = index(l);
        return words[index] != (words[index] |= word(l));
    }

    @Override
    public void clear() {
        Arrays.fill(words, 0L);
    }

    @Override
    public long rawGet(int i) {
        // there's really no need to test if this contains it;
        return relative | i;
    }

    /**
     * {@inheritDoc}
     *
     * <ul>
     * <li>The given index must be &lt; 256 && &gt;= 0, else return -1.</li>
     * <li>The word is derived from the incremented by 1 index shifted to the right by 6.</li>
     * <li>If word < 4 && tmp < 0, continue, else break. Bitwise-variant is used here.</li>
     * <li>Set tmp to return of {@link #nextIndex0(int, int)}.</li>
     * <li>Set index to 0 and increment word by 1.</li>
     * <li>Jump back to if condition. If broken, return tmp.</li>
     * </ul>
     *
     * @implNote This has been explicitly broken up into multiple parts for C2 inlining purposes.
     */
    @Override
    public int nextIndex(int index) {
        int tmp = -1;
        // Thanks to FavoritoHJS#5580 for the bit hackery idea to shrink the bytecode into 35 bytes
        for (int word = ++index >>> 6; ((word - 4) & tmp) < 0; index = 0, word++) {
            tmp = nextIndex0(word, index);
            // if((t = getMaskedWord(w, i)) != 0)
            //     return w << 6 | Long.numberOfTrailingZeros(t);
        }
        return tmp;
    }

    /**
     * Tests to see if the given word masked by current index has any 1 bits,
     * giving the next index as a return.
     * <p>
     * This method has been explicitly split out to allow for quick C2 inlining.
     *
     * @param word  The word from {@link #words}
     * @param index The current index to reference. Used to shift {@code -1L} to the right and mask the word.
     * @return The next index between 0-255 inclusive if there is one for the current word, {@code -1} otherwise.
     * @see #nextIndex(int)
     */
    private int nextIndex0(int word, int index) {
        long tmp;
        if ((tmp = words[word] & (-1L << index /* & 63 implied */)) != 0) {
            return word << 6 | Long.numberOfTrailingZeros(tmp);
        }
        return -1;
    }

    @Override
    public int nextIndex(int i, int s) {
        for (int w = ++i >>> 6, pos; w < 4; i = 0, w++) {
            if ((pos = selPosFromLsbRank(words[w] & (-1L << i), s + 1)) < 0) {
                s -= ~pos;
                continue;
            }
            return w << 6 | pos;
            // long t = words[w] & (-1L << i);
            // // TODO: Combine selPosFromLsbRank & bitCount into same call
            // int l = Long.bitCount(t);
            // if(s > l) {
            //     s -= l;
            //     continue;
            // }
            // return w << 6 | selPosFromLsbRank(t, s + 1);
        }
        return -1;
    }

    /**
     * Gets the position of the nth 1 bit starting with the least significant bits; from the right.
     *
     * @param v    The value to find the nth 1 bit from.
     * @param rank The rank of the 1 bit. Add 1 for next position usage.
     * @return The position of the rth 1 bit in the long, or if overflow, the bitcount of the long with the sign bit set.
     * @author Juha Järvi - Original Implementation of 64-bit MSB
     * @author Nominal Animal - 32-bit & LSB implementation
     * @author KJP12 - 64-bit & Java adaptation of LSB variant
     * @see <a href="https://graphics.stanford.edu/~seander/bithacks.html##SelectPosFromMSBRank">Bit Twiddling Hacks</a>
     * @see <a href="https://stackoverflow.com/a/45487375">Answer by Nominal Animal for Find nth set bit in an int</a>
     */
    private static int selPosFromLsbRank(long v, int rank) {
        long a = (v & 0x5555555555555555L) + ((v >>> 1) & 0x5555555555555555L);
        long b = (a & 0x3333333333333333L) + ((a >>> 2) & 0x3333333333333333L);
        long c = (b & 0x0F0F0F0F0F0F0F0FL) + ((b >>> 4) & 0x0F0F0F0F0F0F0F0FL);
        long d = (c & 0x00FF00FF00FF00FFL) + ((c >>> 8) & 0x00FF00FF00FF00FFL);
        long e = (d & 0x0000FFFF0000FFFFL) + ((d >>> 16) & 0x0000FFFF0000FFFFL);
        int t = (int) (e + (e >>> 32));
        if (t < rank) {
            return ~t;
        }

        t = (int) e & 0xFFFF;
        int position = 0;
        if (rank > t) {
            position += 32;
            rank -= t;
        }

        t = (int) (d >> position) & 0xff;
        if (rank > t) {
            position += 16;
            rank -= t;
        }

        t = (int) (c >> position) & 0xf;
        if (rank > t) {
            position += 8;
            rank -= t;
        }

        t = (int) (b >> position) & 0x7;
        if (rank > t) {
            position += 4;
            rank -= t;
        }

        t = (int) (a >> position) & 0x3;
        if (rank > t) {
            position += 2;
            rank -= t;
        }

        t = (int) (v >> position) & 0x1;
        if (rank > t) position++;

        return position;
    }

    //@Override
    //public NbtElement toNbt() {
    //    var c = new NbtCompound();
    //    c.putLong("r", relative);
    //    c.put("w", new NbtLongArray(words));
    //    return c;
    //}

    private static int index(long l) {
        return (int) (l >>> 6) & 3;
    }

    private static long word(long l) {
        return 1L << l;
    }
}
