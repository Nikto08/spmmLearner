/* Copyright (C) 2020 Elena Shashko.
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
package de.learnlib.spmm.evaluation;

import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.algorithm.feature.SupportsGrowingAlphabet;
import de.learnlib.spmm.aal.ATProvider.ATProvider;
import de.learnlib.spmm.aal.adapter.*;
import de.learnlib.spmm.aal.learner.LocalRefinementCounter;
import de.learnlib.spmm.aal.learner.SPMMLearner;
import de.learnlib.spmm.model.SPMM;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.model.componenets.State;
import net.automatalib.automata.transout.impl.MealyTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BenchmarkUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkUtil.class);

    public static <S, I, O> BenchmarkResult runBenchmarkForOneSPMM(
            final SPMM<S, I, MealyTransition<State<I, S>, O>, O> sul,
            final BiFunction<SPMMInputAlphabet<I>, SPMMOutputAlphabet<O>, ATProvider<I, O>> atProvider,
            int numOfRuns) {

        final List<LearningStatistics> lstarResult = buildAndRunTest(
                Evaluation.createSPMMLearner(sul, LStarAdapter::new, atProvider),
                sul,
                numOfRuns);
        LearningStatistics averageLStarResult = buildAverageStatictics(lstarResult);

        final List<LearningStatistics> rsResult = buildAndRunTest(
                Evaluation.createSPMMLearner(sul, RivestSchapireAdapter::new, atProvider),
                sul,
                numOfRuns);
        LearningStatistics averageRsResult = buildAverageStatictics(rsResult);

        final List<LearningStatistics> kvResult = buildAndRunTest(
                Evaluation.createSPMMLearner(sul, KearnsVaziraniAdapter::new, atProvider),
                sul,
                numOfRuns);
        LearningStatistics averageKvResult = buildAverageStatictics(kvResult);

        final List<LearningStatistics> dtResult = buildAndRunTest(
                Evaluation.createSPMMLearner(sul, DiscriminationTreeAdapter::new, atProvider),
                sul,
                numOfRuns);
        LearningStatistics averageDtResult = buildAverageStatictics(dtResult);

        final List<LearningStatistics> tttResult = buildAndRunTest(
                Evaluation.createSPMMLearner(sul, TTTAdapter::new, atProvider),
                sul,
                numOfRuns);
        LearningStatistics averageTttResult = buildAverageStatictics(tttResult);

        return new BenchmarkResult(averageTttResult,
                averageDtResult, averageKvResult, averageRsResult, averageLStarResult);
    }

    public static void printStatictics(LearningStatistics statistics) {
        LOGGER.info("Counterexamples: {}", statistics.getNumberOfCounterexamples());
        LOGGER.info("Counterexamples for sequences only: {}", statistics.getNumberOfCEForSequencesOnly());
        LOGGER.info("TS-Conformance Checks: {}", statistics.getNumberOfTSConformanceChecks());
        LOGGER.info("Global Refinement Steps: {}", statistics.getNumberOfGlobalRefinementSteps());
        LOGGER.info("Procedural Membership Queries: {}", statistics.getNumberOfMQs());
        LOGGER.info("Symbols in all MQs together: {}", statistics.getNumberOfSymbols());
        LOGGER.info("SPMM Size in States: {}", statistics.getHypothesisSize());
        LOGGER.info("Learned Exact Model: {} * 100%", statistics.getIsExactModel());
        LOGGER.info("Learning Time: {} ms", statistics.getLearningTime());
        LOGGER.info("================");
    }

    public static void printBenchmarkResult(BenchmarkResult result) {

        LOGGER.info("Begin benchmark");

        LOGGER.info("SPMM [L*]");
        printStatictics(result.getLstarResult());

        LOGGER.info("SPMM [RS]");
               printStatictics(result.getRsResult());

        LOGGER.info("SPMM [KV]");
        printStatictics(result.getKvResult());

        LOGGER.info("SPMM [DT]");
        printStatictics(result.getDtResult());

        LOGGER.info("SPMM [TTT]");
        printStatictics(result.getTttResult());

        LOGGER.info("End benchmark");
    }

    public static LearningStatistics buildAverageStatictics(Collection<LearningStatistics> source) {
        return new LearningStatistics(
                computeAverage(source, LearningStatistics::getNumberOfCounterexamples),
                computeAverage(source, LearningStatistics::getNumberOfCEForSequencesOnly),
                computeAverage(source, LearningStatistics::getNumberOfTSConformanceChecks),
                computeAverage(source, LearningStatistics::getNumberOfGlobalRefinementSteps),
                computeAverage(source, LearningStatistics::getNumberOfMQs),
                computeAverage(source, LearningStatistics::getNumberOfSymbols),
                computeAverage(source, LearningStatistics::getIsExactModel),
                computeAverage(source, LearningStatistics::getLearningTime),
                computeAverage(source, LearningStatistics::getHypothesisSize));

    }

    private static double computeAverage(Collection<LearningStatistics> source,
                                                ToDoubleFunction<LearningStatistics> extractor) {
        return source.stream().mapToDouble(extractor).average().getAsDouble();
    }

    public static <
            I,
            O,
            L extends LearningAlgorithm.MealyLearner<I, O>
                    & SupportsGrowingAlphabet<I>
                    & AccessSequenceTransformer<I>
                    & LocalRefinementCounter>
    List<LearningStatistics> buildAndRunTest(
            final SPMMLearner<?, I, ?, O, L> learner,
            SPMM<?, I, ?, O> sul,
            int numOfRuns) {

        return IntStream.range(0, numOfRuns)
                .mapToObj(i -> LearningRun.run(learner, sul))
                .collect(Collectors.toList());
    }

}
