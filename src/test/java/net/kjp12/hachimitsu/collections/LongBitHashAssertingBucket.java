package net.kjp12.hachimitsu.collections;// Created 2022-26-03T11:52:49

import java.util.Arrays;

/**
 * Assertions for the LongBitHashBucket.
 *
 * @author KJP12
 * @since ${version}
 **/
class LongBitHashAssertingBucket extends LongBitHashBucket {

    LongBitHashAssertingBucket(long relative) {
        super(relative);
    }

    LongBitHashAssertingBucket(long relative, byte[] keys) {
        super(relative, keys);
    }

    @Override
    public boolean add(long l) {
        int $size = this.size;
        var $keys = this.keys.clone();
        boolean $ret = super.add(l);
        int c;
        if ((c = count(keys)) != size) {
            var str = String.format("i: %d, %d -> %d: %s -> %s: assert: %d == %d failed", l, $size, size, Arrays.toString($keys), Arrays.toString(keys), c, size);
            if (Helper.HALT_ON_FAILURE) {
                System.err.println(str);
                Runtime.getRuntime().halt(-12);
            }
            throw new AssertionError(str);
        }
        return $ret;
    }
}
