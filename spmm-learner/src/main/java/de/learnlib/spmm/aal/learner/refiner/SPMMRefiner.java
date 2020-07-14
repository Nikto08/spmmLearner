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
package de.learnlib.spmm.aal.learner.refiner;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.algorithm.feature.SupportsGrowingAlphabet;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.spmm.aal.ATProvider.ATProvider;
import de.learnlib.spmm.aal.learner.LocalRefinementCounter;
import de.learnlib.spmm.model.SPMM;
import de.learnlib.spmm.model.SPMMBuilder;
import de.learnlib.spmm.model.SPMMOutputInterpreter;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.util.WordUtils;
import de.learnlib.util.MQUtil;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiFunction;


public class SPMMRefiner<S, I, J, O,
        L extends LearningAlgorithm.MealyLearner<I, O>
                & SupportsGrowingAlphabet<I> & AccessSequenceTransformer<I>
                & LocalRefinementCounter>
        implements LearningAlgorithm<SPMM<S, I, J, O>, I, Word<O>>, LocalRefinementCounter {

    private final SPMMInputAlphabet<I> inputAlphabet;
    private final SPMMOutputAlphabet<O> outputAlphabet;
    private final MembershipOracle<I, Word<O>> oracle;
    private final BiFunction<Alphabet<I>, MembershipOracle<I, Word<O>>, L> learnerProvider;
    private final Mapper<I, O> mapper;
    private final SPMMBuilder<S, I, J, O> builder;

    private final Map<I, L> subRefiners;
    private final Set<I> activeAlphabet;
    private I initialCallSymbol;
    private long numberOfRefinements = 0;
    private long numberOfCounterexamples = 0;
    private long numberOfCEForSequencesOnly = 0;
    private long numberOfTSConformanceChecks = 0;

    public SPMMRefiner(@Nonnull final SPMMInputAlphabet<I> inputAlphabet,
                       @Nonnull final SPMMOutputAlphabet<O> outputAlphabet,
                       @Nonnull final MembershipOracle<I, Word<O>> oracle,
                       @Nonnull final BiFunction<Alphabet<I>, MembershipOracle<I, Word<O>>, L> learnerProvider,
                       @Nonnull final ATProvider<I, O> atProvider,
                       @Nonnull final SPMMBuilder<S, I, J, O> builder) {
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = outputAlphabet;
        this.oracle = oracle;
        this.learnerProvider = learnerProvider;
        this.builder = builder;

        this.subRefiners = Maps.newHashMapWithExpectedSize(inputAlphabet.getNumCalls());
        this.mapper = new Mapper<>(atProvider, inputAlphabet, outputAlphabet);
        this.activeAlphabet = Sets.newHashSetWithExpectedSize(inputAlphabet.getNumCalls()
                + inputAlphabet.getNumInternals() + inputAlphabet.getNumReturns());
        this.activeAlphabet.addAll(inputAlphabet.getInternalAlphabet());
        this.activeAlphabet.addAll(inputAlphabet.getReturnAlphabet());
    }

    @Override
    public void startLearning() {
        // do nothing, as we have to wait for evidence that the potential main procedure actually terminates
    }

    @Override
    /** oracle query must contain all the symbols to assess local counterexample
     *  followed by expanded local counterexample
     *  no symbols after local counterexample are expected, especially if local counterexample
     *  has some post-return output symbols they must stay untouched
     */
    public boolean refineHypothesis(@Nonnull DefaultQuery<I, Word<O>> oracleQuery) {
        if (!WordUtils.wordsHaveSameSize(oracleQuery.getInput(), oracleQuery.getOutput())) {
            throw new AssertionError("Input and output oracleQuery must have same size."
                    + " but now input is " + oracleQuery.getInput() + " and output is"
                    + oracleQuery.getOutput());
        }

        if (!inputAlphabet.isCallSymbol(oracleQuery.getInput().firstSymbol())) {
            throw new AssertionError("oracleQuery must begin with a call.");
        }

        boolean sequencesChanged = findNewProceduresOrNewSequences(oracleQuery);

        boolean refined = false;
        while (refineHypothesisInternal(oracleQuery)) {
            numberOfRefinements++;
            refined = true;
        }

        if (sequencesChanged && !refined) {
            numberOfCEForSequencesOnly++;
        }

        if (sequencesChanged || refined) {
            numberOfCounterexamples++;
        }

        return sequencesChanged || refined;
    }


    @Override
    @Nonnull
    public SPMM<S, I, J, O> getHypothesisModel() {
        if (this.subRefiners.isEmpty()) {
            return builder.createEmptySPMM(inputAlphabet, outputAlphabet);
        }

        final Map<I, MealyMachine<S, I, ?, O>> subModels = getSubModels();

        SPMMInputAlphabet<I> currentHypothesisInputAlphabet = builder.filterCallAlphabet(subRefiners.keySet(), inputAlphabet);
        Alphabet<I> activatedCallsAlphabet = builder.filterCallAlphabet(activeAlphabet, inputAlphabet).getCallAlphabet();
        return builder.createSPMM(currentHypothesisInputAlphabet, activatedCallsAlphabet, outputAlphabet,
                initialCallSymbol, subModels);
    }

    @Nonnull
    public Set<I> getActiveAlphabet() {
        return this.activeAlphabet;
    }

    private boolean refineHypothesisInternal(DefaultQuery<I, Word<O>> oracleQuery) {

        SPMM<S, I, J, O> hypothesis = this.getHypothesisModel();

        final Word<I> input = oracleQuery.getInput();
        final Word<O> oracleOutput = oracleQuery.getOutput();
        final Word<O> hypothesisOutput = hypothesis.computeOutput(input);

        boolean localRefinement = false;

        final int firstDifferentOutputIdx = WordUtils.getFirstIndexWhenWordsDiffer(
                oracleOutput, hypothesisOutput);

        if (!WordUtils.wordsHaveSameSize(oracleOutput, hypothesisOutput)) {
            throw new AssertionError("Oracle output must have same size as hypothesis output."
                    + " However now oracle output is " + oracleOutput + " and hypothesis output is "
                    + hypothesisOutput);
        }


        if (firstDifferentOutputIdx == 0) {
            // the ce is not useful here in this case. Index 0 is the index of Initial Call
            // Initial Call is set in the method findNewProcedures that is always executed
            // before refineHypothesisInternal
            return false;
        } else {

            // extract local counterexample
            final int callIdx;
            if (firstDifferentOutputIdx == -1) {
                return false;
            } else {
                callIdx = mapper.findCallIndexOfProcedure(oracleQuery, firstDifferentOutputIdx);
            }

            final DefaultQuery<I, Word<O>> localQuery;
            final MealyLearner<I, O> localLearner;

            if (callIdx > -1) {
                localQuery = mapper.getLocalInputAndOutput(oracleQuery, callIdx);
                localLearner = this.subRefiners.get(input.getSymbol(callIdx));
            } else {
                throw new AssertionError("could not find call index of procedure to refine. Oracle query is " +
                        oracleQuery + " hypothesis output is " + hypothesisOutput);
            }

            if (!MQUtil.isCounterexample(localQuery, localLearner.getHypothesisModel())) {
                /*System.out.println("local query " + localQuery + " is not a counterexample" +
                        " for procedure " + input.getSymbol(callIdx) + ".");
                System.out.println("local procedure gives output "
                        + localLearner.getHypothesisModel().computeOutput(localQuery.getInput()));*/
                return false;
            }

           /* System.out.println("local counterexample for procedure " + input.getSymbol(callIdx) +
                    " is " + localQuery + ".");
            System.out.println("hypothesis procedure output is "
                    + localLearner.getHypothesisModel().computeOutput(localQuery.getInput()) + ".");*/

            localRefinement = localRefinement | localLearner.refineHypothesis(localQuery);

            //Visualization.visualize(hypothesis);
            if (!localRefinement) {
                throw new AssertionError("No refinement to hypothesis was done using counterexample "
                        + oracleQuery);
            }
        }
        return true;
    }

    private boolean findNewProceduresOrNewSequences(DefaultQuery<I, Word<O>> query) {
        boolean changedInformationAboutProcedures = false;
        final int errorIndex = SPMMOutputInterpreter.findIndexOFFirstErrorSymbol(outputAlphabet, query.getOutput());
        final int postReturnIndex = SPMMOutputInterpreter.findIndexOFFirstPostReturn(outputAlphabet, query.getOutput());
        if (errorIndex != 0 && postReturnIndex != 0) {
            if (initialCallSymbol == null
                    || !initialCallSymbol.equals(query.getInput().firstSymbol())) {
                changedInformationAboutProcedures = true;
                this.initialCallSymbol = query.getInput().firstSymbol();
            }
        }

        final Pair<Set<Pair<I, Integer>>, Boolean> queryScanResult = mapper.findNewProceduresAndUpdateATSequences(query);
        final Set<Pair<I, Integer>> newProcedures = queryScanResult.getFirst();
        changedInformationAboutProcedures |= queryScanResult.getSecond();


        Collection<I> terminatingProcedures = mapper.getOnlyTerminatingProcedures(this.subRefiners.keySet());

        for (final I call : terminatingProcedures) {
            if (!this.activeAlphabet.contains(call)) {
                this.activeAlphabet.add(call);

                // System.out.println("found new terminating procedure " + call);

                for (final L learner : this.subRefiners.values()) {
                    learner.addAlphabetSymbol(call);
                }
            }
        }

        for (final Pair<I, Integer> pair : newProcedures) {

            I identifier = pair.getFirst();
            final L newLearner = learnerProvider.apply(this.inputAlphabet.getInternalAlphabet(),
                    new ProceduralMembershipOracle<>(
                            inputAlphabet,
                            outputAlphabet,
                            oracle,
                            identifier,
                            mapper.getATProvider()));
            this.subRefiners.put(identifier, newLearner);

            newLearner.startLearning();

            newLearner.addAlphabetSymbol(this.inputAlphabet.getReturnSymbol());
            for (final I call : terminatingProcedures) {
                newLearner.addAlphabetSymbol(call);
            }

            if (!mapper.getOnlyTerminatingProcedures(new HashSet<>(Collections.singletonList(identifier))).isEmpty()) {
                this.activeAlphabet.add(identifier);


                for (final L learner : this.subRefiners.values()) {
                    learner.addAlphabetSymbol(identifier);
                }
            }
        }

        if (!newProcedures.isEmpty() || changedInformationAboutProcedures) {
            this.updateInputChecker();

            while (!isTSConform()) {
                this.updateInputChecker();
            }

            return true;
        }

        return false;
    }

    private Map<I, MealyMachine<S, I, ?, O>> getSubModels() {
        final Map<I, MealyMachine<S, I, ?, O>> subModels =
                Maps.newHashMapWithExpectedSize(this.subRefiners.size());

        for (final Map.Entry<I, L> entry : this.subRefiners.entrySet()) {
            subModels.put(entry.getKey(), (MealyMachine<S, I, ?, O>) entry.getValue().getHypothesisModel());
        }

        return subModels;
    }

    private boolean isTSConform() {
        boolean tsConform = true;
        numberOfTSConformanceChecks++;
        for (final I symbol : this.activeAlphabet) {
            if (this.inputAlphabet.isCallSymbol(symbol)) {
                tsConform = tsConform & !makeProcedureTSConform(symbol,
                        this.getSubModels());
            }
        }
        return tsConform;
    }

    private boolean makeProcedureTSConform(I identifier, Map<I, MealyMachine<S, I, ?, O>> mmModels) {
        boolean refined = false;
        final DefaultQuery<I, Word<O>> extendedTQ = mapper.getTerminatingQueryPrependedByCall(identifier);

        for (int i = 0; i < extendedTQ.getInput().size(); i++) {
            final I sym = extendedTQ.getInput().getSymbol(i);

            if (this.inputAlphabet.isCallSymbol(sym)) {

                final DefaultQuery<I, Word<O>> projectedTerminatingQuery
                        = mapper.getLocalInputAndOutput(extendedTQ, i);

                Word<O> subModelOutput = mmModels.get(sym).computeOutput(projectedTerminatingQuery.getInput());


                if (!WordUtils.wordsAreEqual(
                        subModelOutput,
                        projectedTerminatingQuery.getOutput())) {

                    //  System.out.println("Procedure " + sym + " is not TS conform");
                    //  System.out.println("TS is " + relevantProjectedTerminatingInput);
                    // System.out.println("Expected output after reading TS is " + relevantProjectedTerminatingOutput);
                    // System.out.println("but got output " + relevantSubModelOutput);

                    refined = true;
                    subRefiners.get(sym).refineHypothesis(new DefaultQuery<>(projectedTerminatingQuery.getInput(),
                            projectedTerminatingQuery.getOutput()));
                }
            }
        }

        return refined;
    }


    private void updateInputChecker() {
        this.mapper.updateATProvider(
                this.getSubModels(),
                subRefiners,
                activeAlphabet);
    }

    public long getNumberOfGlobalRefinements() {
        return numberOfRefinements;
    }

    @Override
    public long getNumberOfLocalRefinements() {
        long numberOfLocalRefinements = 0;

        for (L subLearner : subRefiners.values()) {
            numberOfLocalRefinements += subLearner.getNumberOfLocalRefinements();
        }

        return numberOfLocalRefinements;
    }

    @Override
    public long getSumOfLocalCELengths() {
        long sumOfLocalCELengths = 0;

        for (L subLearner : subRefiners.values()) {
            sumOfLocalCELengths += subLearner.getSumOfLocalCELengths();
        }

        return sumOfLocalCELengths;
    }

    public long getNumberOfCounterexamples() {
        return numberOfCounterexamples;
    }

    public long getNumberOfCEForSequencesOnly() {
        return numberOfCEForSequencesOnly;
    }

    public long getNumberOfTSConformanceChecks() {
        return numberOfTSConformanceChecks;
    }

    private boolean wordMakesMMReturn(MealyMachine<?, I, ?, O> mealyMachine, Word<I> localInput) {
        Word<O> output = mealyMachine.computeOutput(localInput);
        return SPMMOutputInterpreter.outputEndsWithReturn(outputAlphabet, output);
    }

    private static <I> Word<I> addReturnSymbol(SPMMInputAlphabet<I> alphabet, Word<I> original) {
        return WordUtils.addSymbol(original, alphabet.getReturnSymbol());
    }

}