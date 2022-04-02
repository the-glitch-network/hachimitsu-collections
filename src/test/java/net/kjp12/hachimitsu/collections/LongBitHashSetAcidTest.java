package net.kjp12.hachimitsu.collections;// Created 2022-31-03T15:47:06

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author KJP12
 * @since ${version}
 **/
public class LongBitHashSetAcidTest {
    private Random random = new Random(196188877885538304L);

    @Test
    public void add32768() {
        int size = 0;
        var lbhs = new LongBitHashSet();
        var lohs = new LongOpenHashSet();

        for (int i = 0; i < 32768; i++) {
            long rnd = random.nextLong() & 0xFFFFFFFFL;
            // Cache contains from before addition.
            boolean bc = lbhs.contains(rnd);
            boolean oc = lohs.contains(rnd);

            // Store results of adding to the sets.
            boolean ba = lbhs.add(rnd);
            boolean oa = lohs.add(rnd);

            // xor is used as each are mutually exclusive.
            // If both return true, then there's a bug of double insertion.
            // If both return false, then there's a bug of no insertion.
            assertTrue(bc ^ ba, "lbhs: bad return");
            assertTrue(oc ^ oa, "lohs: bad return (This should never happen!)");

            // The following two *must always be true*
            assertTrue(lbhs.contains(rnd), "lbhs: failed to add");
            assertTrue(lohs.contains(rnd), "lohs: failed to add (This should never happen!)");

            if (ba | oa) {
                size++;
            }
        }

        // Do assertions on what's expected of the container
        assertEquals(size, lbhs.size(), "size counter failure");
        assertEquals(lohs.size(), lbhs.size(), "size mismatch");
        assertEquals(lohs, lbhs, "lohs -> lbhs content mismatch");
        assertEquals(lbhs, lohs, "lbhs -> lohs content mismatch");

        sinkIterator(lbhs, lbhs.iterator(), size);
        sinkIterator(lbhs, lbhs.new j$itr2(), size);
    }

    private static void sinkIterator(LongCollection lbhs, LongIterator itr, int size) {
        long l;
        var lohs = new LongOpenHashSet();
        int itc = 0;
        while (itr.hasNext()) {
            l = itr.nextLong();
            assertTrue(lbhs.contains(l), "j$itr produced non-existent long");
            assertTrue(lohs.add(l), "j$itr produced " + l + " twice");
            itc++;
        }
        assertEquals(size, itc, "j$itr either skipped or double-produced long");
    }
}
