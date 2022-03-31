package net.kjp12.hachimitsu.collections;// Created 2022-27-03T14:18:59

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;

/**
 * @author KJP12
 * @since ${version}
 **/
@State(Scope.Thread)
public class LongOpenHashSetBenchmark {
    private LongOpenHashSet lbhs = new LongOpenHashSet();
    private Random random = new Random(661823121367760917L);

    @State(Scope.Thread)
    public static class Remove {
        private LongOpenHashSet lbhs = new LongOpenHashSet();
        private Random random = new Random(661823121367760917L);

        {
            for (int i = 0; i < 0xFFFFFF; i++) {
                lbhs.add(i);
            }
        }
    }

    @State(Scope.Thread)
    public static class RandData {
        private LongOpenHashSet lbhs = new LongOpenHashSet();
        private Random random = new Random(661823121367760917L);

        {
            for (int i = 0; i < 0xFFFFFF; i++) {
                lbhs.add(random.nextLong() & 0x7FFFFFFFL);
            }
        }
    }

    //@Benchmark
    public void add() {
        lbhs.add(random.nextLong() & 0xFFFFFFL);
    }

    //@Benchmark
    @OperationsPerInvocation(1000)
    public void add1K() {
        for (int i = 0; i < 1000; i++) {
            lbhs.add(random.nextLong() & 0xFFFFFFL);
        }
    }

    //@Benchmark
    public static void rm(Remove remove) {
        remove.lbhs.remove(remove.random.nextLong() & 0xFFFFFFL);
    }

    //@Benchmark
    @OperationsPerInvocation(1000)
    public static void rm1K(Remove remove) {
        for (int i = 0; i < 1000; i++) {
            remove.lbhs.remove(remove.random.nextLong() & 0xFFFFFFL);
        }
    }

    @Benchmark
    //@Measurement(iterations = 5, time = 60, timeUnit = TimeUnit.SECONDS)
    public static void itr(RandData rd, Blackhole bh) {
        var itr = rd.lbhs.iterator();
        while (itr.hasNext()) {
            bh.consume(itr.nextLong());
        }
    }
}
