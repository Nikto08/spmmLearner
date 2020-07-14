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
package de.learnlib.spmm.equivalenceoracle;

import com.google.common.collect.Maps;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.spmm.model.SPMM;
import de.learnlib.spmm.model.SPMMBuilder;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.util.mapping.Expansion;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.words.Word;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class SPMMEqOr<S, I, O>
        implements SPMMEquivalenceOracle<S, I, O> {

    private final SPMM<?, I, ?, O> spmm;

    private final Map<I, Word<I>> terminatingSequences;
    private final Map<I, Word<I>> accessSequences;

    private final Map<I, Word<O>> terminatingSequencesOutput;
    private final Map<I, Word<O>> accessSequencesOutput;


    public SPMMEqOr(SPMM<?, I, ?, O> spmm) {
        this.spmm = spmm;

        ATSequenceComputer<I, O> ATSequenceComputer;
        ATSequenceComputer = new ATSequenceComputer<>(
                spmm.getInputAlphabet(),
                spmm.getInitialCall(),
                spmm.getOutputAlphabet());


        terminatingSequences = ATSequenceComputer.calculateTerminatingSequences(
                spmm.getInputAlphabet(),
                spmm.getProcedures());

        accessSequences = ATSequenceComputer.calculateAccessSequences(
                spmm.getInputAlphabet(),
                spmm.getProcedures(),
                terminatingSequences);

        accessSequencesOutput = Maps.newHashMapWithExpectedSize(spmm.getProcedures().size());
        terminatingSequencesOutput = Maps.newHashMapWithExpectedSize(spmm.getProcedures().size());


        for (I identifier : spmm.getProcedures().keySet()) {
            final Word<I> completeInput = Word.fromWords(
                    accessSequences.get(identifier),
                    Word.fromLetter(identifier),
                    terminatingSequences.get(identifier));

            final Word<O> completeOutput = spmm.computeOutput(completeInput);
            accessSequencesOutput.put(identifier, completeOutput.subWord(0, accessSequences.get(identifier).length()));
            int terminatingSequenceStart = accessSequences.get(identifier).length() + 1;
            int terminatingSequenceEnd = terminatingSequenceStart + terminatingSequences.get(identifier).length();
            terminatingSequencesOutput.put(identifier, completeOutput.subWord(
                    terminatingSequenceStart, terminatingSequenceEnd));
        }
    }


    @Nullable
    @Override
    public <J> DefaultQuery<I, Word<O>> getSPMMQueryForCounterExample(
            @Nonnull SPMM<S, I, J, O> hypothesis,
            @Nonnull SPMMBuilder<S, I, J, O> builder,
            @Nonnull Set<I> activeAlphabet) {

        checkAlphabetCompatibility(hypothesis);

        // first counterexample
        if (!Objects.equals(spmm.getInitialCall(), hypothesis.getInitialCall())) {
            // any input suffices as counterexample
            final I input = spmm.getInputAlphabet().getInternalSymbol(0);
            final Word<I> ce = Word.fromSymbols(spmm.getInitialCall(), input);

            return new DefaultQuery<>(ce, spmm.computeOutput(ce));
        }

        for (final I identifier : hypothesis.getProcedures().keySet()) {

            final MealyMachine<?, I, ?, O> spmmProcedure = this.spmm.getProcedures().get(identifier);
            MealyMachine<?, I, ?, O> hypProcedure = hypothesis.getProcedures().get(identifier);

            Word<I> sepWord = Automata.findSeparatingWord(spmmProcedure, hypProcedure, activeAlphabet);


            if (sepWord != null) {
                final Word<I> counterexampleWithReturn = Word.fromWords(
                        sepWord,
                        Word.fromLetter(spmm.getInputAlphabet().getReturnSymbol()));

                //   System.out.println("Found separating word: " + sepWord + " in procedure " + identifier);

                final Word<O> counterexampleOutput = spmmProcedure.computeOutput(counterexampleWithReturn);
                // final Word<O> hypothesisOutput = hypProcedure.computeOutput(counterexampleWithReturn);

                final DefaultQuery<I, Word<O>> globalQuery = Expansion.buildGlobalInputAndOutputForProcedure(
                        spmm.getInputAlphabet(),
                        spmm.getOutputAlphabet(),
                        identifier,
                        counterexampleWithReturn,
                        counterexampleOutput,
                        accessSequences::get,
                        terminatingSequences::get,
                        accessSequencesOutput::get,
                        terminatingSequencesOutput::get);

                /* System.out.println("Local counterexample has input: " + counterexampleWithReturn
                        + " , output: " + counterexampleOutput);
                System.out.println("Hypothesis local output: " + hypothesisOutput);
                System.out.println("Expanded counterexample has input: " + globalQuery.getInput()
                        + " , output: " + globalQuery.getOutput());*/

                return globalQuery;
            }
        }

        // if not all procedures in hypothesis have access sequences
        if (!hypothesis.getProcedures().keySet().containsAll(spmm.getInputAlphabet().getCallAlphabet())) {
            // assess sequence based counterexample

            for (I identifier : spmm.getProcedures().keySet()) {
                if (!hypothesis.getProcedures().keySet().contains(identifier)) {
                    final Word<I> input = Word.fromWords(
                            accessSequences.get(identifier),
                            Word.fromLetter(identifier),
                            Word.fromLetter(spmm.getInputAlphabet().getInternalSymbol(0)));

                    return new DefaultQuery<>(input, spmm.computeOutput(input));
                }
            }
        }

        // if not all procedures in hypothesis have terminating sequences
        if (!activeAlphabet.containsAll(spmm.getInputAlphabet().getCallAlphabet())) {
            for (I identifier : spmm.getProcedures().keySet()) {
                if (!activeAlphabet.contains(identifier)) {
                    final Word<I> input = Word.fromWords(
                            accessSequences.get(identifier),
                            Word.fromLetter(identifier),
                            terminatingSequences.get(identifier));

                    return new DefaultQuery<>(input, spmm.computeOutput(input));
                }
            }
        }

        return null;
    }


    private void checkAlphabetCompatibility(SPMM<?, I, ?, O> hypothesis) {
        if (!spmm.getInputAlphabet().getCallAlphabet().containsAll(
                hypothesis.getInputAlphabet().getCallAlphabet())
        ) {
            throw new IllegalArgumentException("call alphabet of the hypothesis must be a subset" +
                    "of the call alphabet of spmm of the Equivalence Oracle");
        }
        if (!hypothesis.getInputAlphabet().getInternalAlphabet().containsAll(
                spmm.getInputAlphabet().getInternalAlphabet())
                || !spmm.getInputAlphabet().getInternalAlphabet().containsAll(
                hypothesis.getInputAlphabet().getInternalAlphabet())
        ) {
            throw new IllegalArgumentException("internal alphabet of the hypothesis must be equal with the" +
                    "internal alphabet of spmm of the Equivalence Oracle");
        }
        if (!hypothesis.getInputAlphabet().getReturnAlphabet().containsAll(
                spmm.getInputAlphabet().getReturnAlphabet())
                || !spmm.getInputAlphabet().getReturnAlphabet().containsAll(
                hypothesis.getInputAlphabet().getReturnAlphabet())
        ) {
            throw new IllegalArgumentException("return alphabet of the hypothesis must be equal with the" +
                    "return alphabet of spmm of the Equivalence Oracle");
        }
    }

    private MealyMachine<?, I, ?, O> completeProcedure(MealyMachine<?, I, ?, O> procedure, Collection<I> inputs) {

        final SPMMInputAlphabet<I> inputAlphabet = spmm.getInputAlphabet();
        final SPMMOutputAlphabet<O> outputAlphabet = spmm.getOutputAlphabet();


        final CompactMealy<I, O> copy = new CompactMealy<>(inputAlphabet, procedure.size() + 1);
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.STATE_BY_STATE, procedure, inputs, copy);


        // adding a potential second sink holds no consequences for equivalence checks
        final Integer sink = copy.addState();
        for (final I i : inputAlphabet) {
            copy.addTransition(sink, i, sink, outputAlphabet.getError());
        }

        // complete copy with "error" transitions so that a counterexample always holds a successful invocation
        // including a terminating sequence
        for (final Integer s : copy) {
            for (final I i : inputAlphabet) {
                if (copy.getTransition(s, i) == null) {
                    copy.addTransition(s, i, sink, outputAlphabet.getError());
                }
            }
        }

        return copy;
    }


}
