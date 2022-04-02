package net.kjp12.hachimitsu.collections;// Created 2022-31-03T21:17:22

import java.util.Random;

/**
 * @author KJP12
 * @since ${version}
 **/
public class TestApp {
    private static LongBitHashSet gen() {
        var rng = new Random(196188877885538304L);
        var lbhs = new LongBitHashSet();
        for (int i = 0; i < 0xFFFFFF; i++) {
            lbhs.add(rng.nextLong() & 0x7FFFFFFFL);
        }
        return lbhs;
    }

    public static void main(String[] args) {
        var lbhs = gen();
        long l = 0;
        for (int i = 0; i < 255; i++) {
            l += spin(lbhs);
        }
        System.out.println(l);
    }

    private static long spin(LongBitHashSet lbhs) {
        var itr = lbhs.new j$itr2();
        long l = 1L;
        while (itr.hasNext()) {
            l *= itr.nextLong();
        }
        return l;
    }
}
