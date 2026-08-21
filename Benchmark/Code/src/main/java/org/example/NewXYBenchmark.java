/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.example;

import org.example.benchmarkcode.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class NewXYBenchmark {

    private static final String DATA_IN = "C:\\Users\\gijsv\\Desktop\\StrategoInstall\\StrategoBenchmarkJMH\\benchmark\\inp-xy17.aterm";
    private static final int WARMUP_ITERATIONS = 5;
    private static final int RUN_ITERATIONS = 10;
    private static final int BATCH_SIZE = 1;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(NewXYBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                //.addProfiler(WinPerfAsmProfiler.class)
                //.addProfiler(GCProfiler.class)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = WARMUP_ITERATIONS, batchSize = BATCH_SIZE)
    @Measurement(iterations = RUN_ITERATIONS, batchSize = BATCH_SIZE)
    public void xyOriginal(Blackhole bh) {
        bh.consume(str2_example_xy.mainNoExit(new String[]{DATA_IN}));
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = WARMUP_ITERATIONS, batchSize = BATCH_SIZE)
    @Measurement(iterations = RUN_ITERATIONS, batchSize = BATCH_SIZE)
    public void xyUnfused(Blackhole bh) {
        bh.consume(str2_example_xy_unfused.mainNoExit(new String[]{DATA_IN}));
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = WARMUP_ITERATIONS, batchSize = BATCH_SIZE)
    @Measurement(iterations = RUN_ITERATIONS, batchSize = BATCH_SIZE)
    public void xyFused(Blackhole bh) {
        bh.consume(str2_example_xy_fused.mainNoExit(new String[]{DATA_IN}));
    }

}
