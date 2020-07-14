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
package de.learnlib.spmm.aal.learner;

import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.algorithm.feature.SupportsGrowingAlphabet;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.filter.statistic.oracle.JointCounterOracle;
import de.learnlib.spmm.aal.ATProvider.ATProvider;
import de.learnlib.spmm.aal.learner.refiner.SPMMRefiner;
import de.learnlib.spmm.equivalenceoracle.SPMMEquivalenceOracle;
import de.learnlib.spmm.model.SPMM;
import de.learnlib.spmm.model.SPMMBuilder;
import de.learnlib.spmm.model.SPMMOutputInterpreter;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.util.MQUtil;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

/**
 * Learning Algorithm for SPMM.
 *
 * @param <S> state type
 * @param <I> input symbol type @param <I>
 * @param <O> output symbol type
 * @param <J> transition type
 * @param <L> procedural learner type
 */
public class SPMMLearner<S,
        I,
        J,
        O,
        L extends LearningAlgorithm.MealyLearner<I, O>
                & SupportsGrowingAlphabet<I>
                & AccessSequenceTransformer<I>
                & LocalRefinementCounter>
        implements LearningAlgorithm<SPMM<S, I, J, O>, I, Word<O>>, LocalRefinementCounter {
    private final JointCounterOracle<I, Word<O>> mqOracle;
    private final SPMMEquivalenceOracle<S, I, O> eqOracle;
    private final SPMMRefiner<S, I, J, O, L> refiner;
    private final SPMMBuilder<S, I, J, O> builder;
    private final SPMMInputAlphabet<I> inputAlphabet;
    private final SPMMOutputAlphabet<O> outputAlphabet;

    public SPMMLearner(@Nonnull MembershipOracle<I, Word<O>> mqOracle,
                       @Nonnull SPMMEquivalenceOracle<S, I, O> eqOracle,
                       @Nonnull SPMMInputAlphabet<I> inputAlphabet,
                       @Nonnull SPMMOutputAlphabet<O> outputAlphabet,
                       @Nonnull BiFunction<Alphabet<I>, MembershipOracle<I, Word<O>>, L> learnerProvider,
                       @Nonnull ATProvider<I, O> atrProvider,
                       @Nonnull SPMMBuilder<S, I, J, O> builder) {
        this.eqOracle = eqOracle;
        this.mqOracle = new JointCounterOracle<>(mqOracle);
        this.builder = builder;
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = outputAlphabet;
        this.refiner = new SPMMRefiner<>(this.inputAlphabet, this.outputAlphabet, this.mqOracle, learnerProvider, atrProvider, this.builder);
    }

    @Nonnull
    public SPMM<S, I, J, O> computeLearnedModel() {

        refiner.startLearning();
        SPMM<S, I, J, O> hyp = refiner.getHypothesisModel();

        DefaultQuery<I, Word<O>> counterexample;

        while ((counterexample = eqOracle.getSPMMQueryForCounterExample(hyp, builder, refiner.getActiveAlphabet())) != null) {
            boolean refined = false;
            if (refiner.refineHypothesis(counterexample)) {
                refined = true;
                // Visualization.visualize(hyp);
            }

            hyp = refiner.getHypothesisModel();

            if (!refined) {
                Visualization.visualize(hyp);
                throw new AssertionError("Hypothesis could not be refined using counterexample " +
                        counterexample);
            }

            int firstPostReturnIndex = SPMMOutputInterpreter.findIndexOFFirstPostReturn(
                    outputAlphabet, counterexample.getOutput());
            if (firstPostReturnIndex == -1) {
                firstPostReturnIndex = counterexample.getOutput().length();
            }
            if (MQUtil.isCounterexample(
                    new DefaultQuery<>(counterexample.getInput().subWord(0, firstPostReturnIndex),
                            counterexample.getOutput().subWord(0, firstPostReturnIndex)),
                    hyp)) {
                throw new AssertionError("After refining hypothesis using counterexample " +
                        counterexample.getInput() + " it is still a counterexample. That means that the refinement " +
                        "was not done properly." +
                        " Oracle output is " + mqOracle.answerQuery(counterexample.getInput()) +
                        ". Hypothesis output is " + hyp.computeOutput(counterexample.getInput()));
            }

        }

        //Visualization.visualize(hyp);
        return hyp;

    }


    @Override
    public void startLearning() {
        // do nothing, as we have to wait for evidence that the potential main procedure actually terminates
    }

    @Override
    public boolean refineHypothesis(@Nonnull DefaultQuery<I, Word<O>> oracleQuery) {
        return refiner.refineHypothesis(oracleQuery);
    }


    @Override
    @Nonnull
    public SPMM<S, I, J, O> getHypothesisModel() {
        return refiner.getHypothesisModel();
    }

    public long getNumberOfMembershipQueries() {
        return mqOracle.getQueryCount();
    }

    public long getNumberOfMembershipSymbols() {
        return mqOracle.getSymbolCount();
    }

    public long getNumberOfGlobalRefinements() {
        return refiner.getNumberOfGlobalRefinements();
    }

    public long getNumberOfCounterexamples() {
        return refiner.getNumberOfCounterexamples();
    }

    public long getNumberOfCEForSequencesOnly() {
        return refiner.getNumberOfCEForSequencesOnly();
    }

    public long getNumberOfTSConformanceChecks() {
        return refiner.getNumberOfTSConformanceChecks();
    }

    @Override
    public long getNumberOfLocalRefinements() {
        return refiner.getNumberOfLocalRefinements();
    }

    @Override
    public long getSumOfLocalCELengths() {
        return refiner.getSumOfLocalCELengths();
    }


}
