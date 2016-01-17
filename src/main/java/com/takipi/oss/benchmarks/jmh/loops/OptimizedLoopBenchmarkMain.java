package com.takipi.oss.benchmarks.jmh.loops;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@State(Scope.Benchmark)
public class OptimizedLoopBenchmarkMain {
    final int size = 100000;
    List<Integer> integers = null;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(OptimizedLoopBenchmarkMain.class.getSimpleName())
                .warmupIterations(8)
                .warmupTime(TimeValue.seconds(2))
                .measurementIterations(8)
                .measurementTime(TimeValue.seconds(2))
                .threads(1)
                .jvmArgs("-Xmx1g"
                        //, "-XX:+UnlockDiagnosticVMOptions"
                        //, "-XX:+PrintInlining"
                        , "-XX:MaxInlineLevel=15"
                )
                .forks(1)
                .build();
        new Runner(opt).run();
    }

    @Setup
    public void setup() {
        integers = new ArrayList<>(size);
        populate(integers);
    }

    public void populate(List<Integer> list) {
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            list.add(Integer.valueOf(random.nextInt(1000000)));
        }
    }


    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(2)
    @Measurement(iterations = 5)
    @Warmup(iterations = 5)
    public int forMax2Integer() {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            max = Integer.max(max, integers.get(i));
        }
        return max;
    }


    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(2)
    @Measurement(iterations = 3)
    @Warmup(iterations = 3)
    public int streamMaxInteger() {
        return integers.stream().mapToInt(Integer::intValue).reduce(Integer.MIN_VALUE, Integer::max);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(2)
    @Measurement(iterations = 3)
    @Warmup(iterations = 3)
    public int lambdaMaxInteger() {
        return integers.stream().mapToInt(Integer::intValue).reduce(Integer.MIN_VALUE, (a, b) -> Integer.max(a, b));
    }


    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(2)
    @Measurement(iterations = 5)
    @Warmup(iterations = 3)
    public int streamBoxingMaxInteger() {
        return integers.stream().reduce(Integer.MIN_VALUE, OptimizedLoopBenchmarkMain::max);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(2)
    @Measurement(iterations = 5)
    @Warmup(iterations = 3)
    public int lambdaBoxingMaxInteger() {
        return integers.stream().reduce(Integer.MIN_VALUE, (a, b) -> max(a, b));
    }



    private static Integer max(Integer a, Integer b) {
        return (a >= b) ? a : b;
    }

}
