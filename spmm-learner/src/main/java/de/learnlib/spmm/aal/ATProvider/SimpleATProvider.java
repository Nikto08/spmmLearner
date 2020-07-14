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
package de.learnlib.spmm.aal.ATProvider;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.spmm.model.SPMMOutputInterpreter;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.util.WordUtils;
import de.learnlib.spmm.util.mapping.IndexFinder;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class SimpleATProvider<I, O> implements ATProvider<I, O> {


    private final Map<I, Word<I>> accessSequences;
    private final Map<I, Word<I>> terminatingSequences;
    private final Map<I, Word<O>> terminatingSequencesOutput;

    private final SPMMInputAlphabet<I> inputAlphabet;
    private final SPMMOutputAlphabet<O> outputAlphabet;

    public SimpleATProvider(@Nonnull final SPMMInputAlphabet<I> inputAlphabet,
                            @Nonnull SPMMOutputAlphabet<O> outputAlphabet) {
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = outputAlphabet;
        this.accessSequences = Maps.newHashMapWithExpectedSize(inputAlphabet.getNumCalls());
        this.terminatingSequences = Maps.newHashMapWithExpectedSize(inputAlphabet.getNumCalls());
        this.terminatingSequencesOutput = Maps.newHashMapWithExpectedSize(inputAlphabet.getNumCalls());
    }

    @Override
    public Word<I> getAccessSequence(@Nonnull I procedure) {
        return this.accessSequences.get(procedure);
    }

    @Override
    public Function<I, Word<I>> getAccessSequenceProvider() {
        return this.accessSequences::get;
    }

    @Override
    public Word<I> getTerminatingSequence(@Nonnull I procedure) {
        return this.terminatingSequences.get(procedure);
    }

    @Override
    public void addTerminatingSequence(@Nonnull I procedure, @Nonnull Word<I> ts){
        this.terminatingSequences.put(procedure, ts);
    }

    @Override
    public Function<I, Word<I>> getTerminatingSequenceProvider() {
        return this.terminatingSequences::get;
    }

    @Override
    public Word<O> getTerminatingSequenceOutput(@Nonnull I procedure) {
        return this.terminatingSequencesOutput.get(procedure);
    }

    @Override
    public Pair<Set<Pair<I, Integer>>, Boolean> findNewProceduresAndUpdateSequences(
            @Nonnull final DefaultQuery<I, Word<O>> exampleQuery) {

        final Set<Pair<I, Integer>> newProcedures
                = Sets.newHashSetWithExpectedSize(inputAlphabet.getNumCalls() - accessSequences.size());
        boolean changed = false;

        Word<I> input = exampleQuery.getInput();
        Word<O> output = exampleQuery.getOutput();

        if (!WordUtils.wordsHaveSameSize(input, output)) {
            throw new AssertionError("Input and output of a query must have same size.");
        }

        final int errorIndex = SPMMOutputInterpreter.findIndexOFFirstErrorSymbol(outputAlphabet, exampleQuery.getOutput());
        final int postReturnIndex = SPMMOutputInterpreter.findIndexOFFirstPostReturn(outputAlphabet, exampleQuery.getOutput());

        int lastScanned = errorIndex - 1;
        if (errorIndex == -1) {
            lastScanned = postReturnIndex - 1;
            if (postReturnIndex == -1) {
                lastScanned = exampleQuery.getInput().size() - 1;
            }
        }

        for (int i = 0; i <= lastScanned; i++) {
            final I sym = input.getSymbol(i);

            if (this.inputAlphabet.isCallSymbol(sym)) {
                if (!this.accessSequences.containsKey(sym)) {

                    if (i - 1 >= 0) {
                        this.accessSequences.put(sym, input.subWord(0, i));
                    } else {
                        this.accessSequences.put(sym, Word.epsilon());
                    }
                    newProcedures.add(new Pair<>(sym, i));
                    changed = true;
                }
                if (!this.terminatingSequences.containsKey(sym)) {
                    final int returnIdx = IndexFinder.findReturnIndexByCallIndex(inputAlphabet,
                            outputAlphabet, exampleQuery.getInput(), exampleQuery.getOutput(), i);

                    if (returnIdx != -1) {
                        Word<I> inputCandidate = input.subWord(i + 1, returnIdx + 1);
                        if (!this.terminatingSequences.containsKey(sym)
                                || !terminatingSequences.get(sym).equals(inputCandidate)) {
                            changed = true;
                            this.terminatingSequences.put(sym, inputCandidate);
                            this.terminatingSequencesOutput.put(sym, output.subWord(i + 1, returnIdx + 1));
                        }

                    }

                }


            }
        }

        return new Pair<>(newProcedures, changed);
    }

    @Override
    public void updateSequences(@Nonnull final Map<I, ? extends MealyMachine<?, I, ?, O>> procedures,
                                @Nonnull final Map<I, ? extends AccessSequenceTransformer<I>> providers,
                                @Nonnull final Collection<I> validInputSymbols) {
        if (procedures.size() != providers.size()) {
            throw new AssertionError("There exist some procedures with no learners or some learners that" +
                    " belong to no procedure.");
        }
        Map<I, Pair<
                ? extends MealyMachine<?, I, ?, O>,
                ? extends AccessSequenceTransformer<I>>> procedureLearnerPairs
                = Maps.newHashMapWithExpectedSize(procedures.size());

        for (I identifier : procedures.keySet()) {
            if (providers.get(identifier) != null) {
                procedureLearnerPairs.put(identifier, new Pair<>(procedures.get(identifier), (providers.get(identifier))));
            } else {
                throw new AssertionError("There exist some procedures with no learners and some learners" +
                        " that belong to no procedure.");
            }
        }

        this.updateSequences(procedureLearnerPairs, validInputSymbols);
    }


    private void updateSequences(Map<I, Pair<
            ? extends MealyMachine<?, I, ?, O>,
            ? extends AccessSequenceTransformer<I>>> proceduresLearnersPairs,
                                 Collection<I> validInputSymbols) {
        // do nothing
    }
}
