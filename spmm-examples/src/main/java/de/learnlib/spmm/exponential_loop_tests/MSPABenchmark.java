/* Copyright (C) 2020 Markus Frohme.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.learnlib.spmm.exponential_loop_tests;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.mtf90.Benchmark;
import com.github.mtf90.BenchmarkSuite;
import com.github.mtf90.Runner;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.algorithm.feature.SupportsGrowingAlphabet;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.spmm.aal.ATProvider.SimpleATProvider;
import de.learnlib.spmm.aal.adapter.DiscriminationTreeAdapter;
import de.learnlib.spmm.aal.adapter.KearnsVaziraniAdapter;
import de.learnlib.spmm.aal.adapter.LStarAdapter;
import de.learnlib.spmm.aal.adapter.RivestSchapireAdapter;
import de.learnlib.spmm.aal.adapter.TTTAdapter;
import de.learnlib.spmm.aal.learner.LocalRefinementCounter;
import de.learnlib.spmm.aal.learner.SPMMLearner;
import de.learnlib.spmm.equivalenceoracle.SPMMEquivalenceOracle;
import de.learnlib.spmm.evaluation.BenchmarkUtil;
import de.learnlib.spmm.evaluation.LearningStatistics;
import de.learnlib.spmm.model.SPMM;
import de.learnlib.spmm.model.SPMMBuilder;
import de.learnlib.spmm.model.alphabet.DefaultSPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.DefaultSPMMOutputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.model.componenets.State;
import de.learnlib.spmm.model.defaultspmm.DefaultSPMMBuilder;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.transout.impl.MealyTransition;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MSPABenchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(MSPABenchmark.class);

    private static final int[] LOOP_SIZE = {3, 10, 30};

    public static void main(String[] args) {
        for (int i = 0; i < LOOP_SIZE.length; i++) {
            LOGGER.info("Loop size: " + LOOP_SIZE[i]);

            final BenchmarkSuite<Character, String> sync = Benchmark.synchronousSystem(LOOP_SIZE[i]);
            final BenchmarkSuite<Character, String> async = Benchmark.asynchronousSystem(LOOP_SIZE[i]);

            LOGGER.info("Sync");
            runMSPABenchmark(sync);
            runSPMMBenchmark(sync);

            LOGGER.info("Async");
            runMSPABenchmark(async);
            runSPMMBenchmark(async);
        }
    }

    private static void runMSPABenchmark(BenchmarkSuite<Character, String> suite) {
        Runner.runLearners(suite, Benchmark.SUCCESS_OUTPUT, Benchmark.ERROR_OUTPUT);
    }

    private static void runSPMMBenchmark(BenchmarkSuite<Character, String> suite) {
        final SPMMInputAlphabet<Character> inputAlphabet = new DefaultSPMMInputAlphabet<>(Benchmark.INTERNAL_ALPHABET,
                Benchmark.CALL_ALPHABET,
                Benchmark.RETURN_SYMBOL);
        final SPMMOutputAlphabet<String> outputAlphabet = new DefaultSPMMOutputAlphabet<>(Benchmark.OUTPUT_ALPHABET,
                Benchmark.SUCCESS_OUTPUT,
                Benchmark.SUCCESS_OUTPUT,
                Benchmark.ERROR_OUTPUT,
                "left");

        runBenchmarkSuite(suite, inputAlphabet, outputAlphabet, 1);
    }

    private static <I, O> void runBenchmarkSuite(BenchmarkSuite<I, O> suite,
                                                 SPMMInputAlphabet<I> inputAlphabet,
                                                 SPMMOutputAlphabet<O> outputAlphabet,
                                                 int numOfRuns) {

        LOGGER.info("Begin benchmark");

        final List<LearningStatistics> lstarResult =
                BenchmarkUtil.buildAndRunTest(createLearner(suite, inputAlphabet, outputAlphabet, LStarAdapter::new),
                        null,
                        numOfRuns);
        LOGGER.info("SPMM [L*]");
        BenchmarkUtil.printStatictics(BenchmarkUtil.buildAverageStatictics(lstarResult));

        final List<LearningStatistics> rsResult = BenchmarkUtil.buildAndRunTest(createLearner(suite,
                inputAlphabet,
                outputAlphabet,
                RivestSchapireAdapter::new),
                null,
                numOfRuns);
        LOGGER.info("SPMM [RS]");
        BenchmarkUtil.printStatictics(BenchmarkUtil.buildAverageStatictics(rsResult));

        final List<LearningStatistics> kvResult = BenchmarkUtil.buildAndRunTest(createLearner(suite,
                inputAlphabet,
                outputAlphabet,
                KearnsVaziraniAdapter::new),
                null,
                numOfRuns);
        LOGGER.info("SPMM [KV]");
        BenchmarkUtil.printStatictics(BenchmarkUtil.buildAverageStatictics(kvResult));

        final List<LearningStatistics> dtResult = BenchmarkUtil.buildAndRunTest(createLearner(suite,
                inputAlphabet,
                outputAlphabet,
                DiscriminationTreeAdapter::new),
                null,
                numOfRuns);
        LOGGER.info("SPMM [DT]");
        BenchmarkUtil.printStatictics(BenchmarkUtil.buildAverageStatictics(dtResult));

        final List<LearningStatistics> tttResult =
                BenchmarkUtil.buildAndRunTest(createLearner(suite, inputAlphabet, outputAlphabet, TTTAdapter::new),
                        null,
                        numOfRuns);
        LOGGER.info("SPMM [TTT]");
        BenchmarkUtil.printStatictics(BenchmarkUtil.buildAverageStatictics(tttResult));

        LOGGER.info("End benchmark");
    }

    private static <S, I, O, L extends LearningAlgorithm.MealyLearner<I, O> & SupportsGrowingAlphabet<I> & AccessSequenceTransformer<I> & LocalRefinementCounter> SPMMLearner<S, I, MealyTransition<State<I, S>, O>, O, L> createLearner(
            BenchmarkSuite<I, O> suite,
            SPMMInputAlphabet<I> inputAlphabet,
            SPMMOutputAlphabet<O> outputAlphabet,
            BiFunction<Alphabet<I>, MembershipOracle<I, Word<O>>, L> learnerProvider) {

        final MembershipOracle<I, Word<O>> mqo = suite.getMembershipOracle();
        final EquivalenceOracle<SuffixOutput<I, Word<O>>, I, Word<O>> eqo = suite.getEquivalenceOracle();
        final SPMMEquivalenceOracle<S, I, O> spmmEqo = new SPMMEquivalenceOracle<S, I, O>() {

            @Nullable
            @Override
            public <J> DefaultQuery<I, Word<O>> getSPMMQueryForCounterExample(@Nonnull SPMM<S, I, J, O> hypothesis,
                                                                              @Nonnull SPMMBuilder<S, I, J, O> builder,
                                                                              @Nonnull Set<I> activeAlphabet) {
                return eqo.findCounterExample(hypothesis, suite.getAlphabet());
            }
        };

        return new SPMMLearner<>(mqo,
                spmmEqo,
                inputAlphabet,
                outputAlphabet,
                learnerProvider,
                new SimpleATProvider<>(inputAlphabet, outputAlphabet),
                new DefaultSPMMBuilder<>());
    }

}
