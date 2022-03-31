package net.kjp12.hachimitsu.collections;// Created 2022-27-03T14:18:59

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.BitSet;
import java.util.Random;

/**
 * @author KJP12
 * @since ${version}
 **/
@State(Scope.Thread)
public class BitSetBenchmark {
    private BitSet lbhs = new BitSet();
    private Random random = new Random(661823121367760917L);

    @State(Scope.Thread)
    public static class Remove {
        private BitSet lbhs = new BitSet();
        private Random random = new Random(661823121367760917L);

        {
            lbhs.set(0, 0xFFFFFE);
        }
    }

    @State(Scope.Thread)
    public static class RandData {
        private BitSet lbhs = new BitSet();
        private Random random = new Random(661823121367760917L);

        {
            for (int i = 0; i < 0xFFFFFF; i++) {
                lbhs.set((int) random.nextLong() & 0x7FFFFFFF);
            }
        }
    }

    //@Benchmark
    public void add() {
        lbhs.set((int) random.nextLong() & 0xFFFFFF);
    }

    //@Benchmark
    @OperationsPerInvocation(1000)
    public void add1K() {
        for (int i = 0; i < 1000; i++) {
            lbhs.set((int) random.nextLong() & 0xFFFFFF);
        }
    }

    //@Benchmark
    public static void rm(Remove remove) {
        remove.lbhs.set((int) remove.random.nextLong() & 0xFFFFFF, false);
    }

    //@Benchmark
    @OperationsPerInvocation(1000)
    public static void rm1K(Remove remove) {
        for (int i = 0; i < 1000; i++) {
            remove.lbhs.set((int) remove.random.nextLong() & 0xFFFFFF, false);
        }
    }

    @Benchmark
    //@Measurement(iterations = 5, time = 60, timeUnit = TimeUnit.SECONDS)
    public static void itr(RandData rd, Blackhole bh) {
        var bs = rd.lbhs;
        int i = -1;
        while ((i = bs.nextSetBit(++i)) >= 0) {
            bh.consume(bs.get(i));
        }
    }
}
