package net.kjp12.hachimitsu.collections;// Created 2022-11-03T01:41:39

import java.util.Arrays;

/**
 * A hash-based bucket of longs, storing the least significant 8 bits in a hash array
 * to reconstruct the long.
 *
 * @author KJP12
 * @since ${version}
 **/
class LongBitHashBucket extends LongBitBucket {
    protected byte[] keys;

    /**
     * Initialises the bucket with the relative long (assumed to be & ~255) and a new hash array.
     */
    LongBitHashBucket(long relative) {
        super(relative, 0);
        keys = new byte[4];
        keys[0] = -1;
    }

    /**
     * Constructs the bucket with the given relative long and hash array.
     * <p>
     * It is assumed that the hash array is always in hash-order. Undefined
     * or broken behaviour may occur if not.
     * <p>
     * The size is recalculated on creation.
     *
     * @param relative The long to construct longs from. Assumed to be & ~255
     * @param keys     The hash array of bytes to reconstruct longs with.
     */
    LongBitHashBucket(long relative, byte[] keys) {
        super(relative, count(keys));
        this.keys = keys;
    }

    protected static int count(byte[] keys) {
        int size = 0;
        if (keys[0] != (byte) -1) size = 1;
        for (int i = 0; ++i < keys.length; ) {
            if (keys[i] != 0) size++;
        }
        return size;
    }

    @Override
    public boolean contains(long l) {
        return isRelative(l) && keys[index(l)] == (byte) l;
    }

    @Override
    public boolean remove(long l) {
        if (isRelative(l) && remove0(keys, (byte) l)) {
            size--;
            return true;
        }
        return false;
    }

    private static boolean remove0(byte[] keys, byte entry) {
        int i = entry & (keys.length - 1);
        if (keys[i] == entry) {
            keys[i] = (byte) (i == 0 ? -1 : 0);
            return true;
        }
        return false;
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
        byte b;
        return isRelative(l) && (add0(keys, b = (byte) l) || resize(b));
    }

    private static boolean add0(byte[] keys, byte entry) {
        int i = entry & (keys.length - 1);
        if (keys[i] == (i == 0 ? -1 : 0)) {
            keys[i] = entry;
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        keys[0] = -1;
        Arrays.fill(keys, 1, keys.length, (byte) 0);
    }

    @Override
    public long rawGet(int i) {
        return relative | (keys[i] & 255);
    }

    @Override
    public int nextIndex(int i) {
        if (i < 0) {
            if (keys[0] != (byte) -1) return 0;
            i = 0;
        }
        int l = keys.length;
        while (++i < l && keys[i] == 0) ;
        if (i >= l) {
            return -1;
        }
        return i;
    }

    @Override
    public int nextIndex(int i, int s) {
        if (i < 0) {
            if (keys[0] != (byte) -1 && --s == 0) return 0;
            i = 0;
        }
        int l = keys.length;
        while (++i < l && keys[i] == 0 || --s > 0) ;
        return i >= l ? -1 : i;
    }

    //@Override
    //public NbtCompound toNbt() {
    //    var c = new NbtCompound();
    //    c.putLong("r", relative);
    //    c.put("a", new NbtByteArray(keys));
    //    return c;
    //}

    private boolean resize(byte l) {
        int nl;
        if (keys[index(l)] == l || // ) return false;
                // Too large; longs will be more efficient.
                /*if(*/(nl = size(l)) > 16) return false;
        insert0(l, copy0(this.keys, this.keys = new byte[nl]));
        return true;
    }

    private static byte[] copy0(byte[] old, byte[] keys) {
        keys[0] = -1;
        byte k = old[0];
        if (k != -1) insert0(k, keys);
        for (int i = 1; i < old.length; i++) {
            if ((k = old[i]) != 0) insert0(k, keys);
        }
        return keys;
    }

    private static void insert0(byte b, byte[] bucket) {
        bucket[b & (bucket.length - 1)] = b;
    }

    private int index(long l) {
        return (int) l & (keys.length - 1);
    }

    private int size(int l) {
        return Integer.lowestOneBit(l ^ keys[index(l)]) << 1;
    }
}
