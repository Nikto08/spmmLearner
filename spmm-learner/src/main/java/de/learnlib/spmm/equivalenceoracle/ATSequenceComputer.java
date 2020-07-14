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
import com.google.common.collect.Sets;
import de.learnlib.spmm.model.SPMMOutputInterpreter;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.util.WordUtils;
import de.learnlib.spmm.util.mapping.Expansion;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.automata.cover.Covers;
import net.automatalib.words.Word;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.Map.Entry;

/**
 * Class, that calculates assess sequences and terminating sequences for all
 * procedures by given SPMM.
 */

class ATSequenceComputer<I, O> {

    private SPMMInputAlphabet<I> inputAlphabet;
    private I startProcedure;
    private final SPMMOutputAlphabet outputAlphabet;

    ATSequenceComputer(SPMMInputAlphabet<I> inputAlphabet,
                       I initialCall,
                       SPMMOutputAlphabet outputAlphabet) {
        this.startProcedure = initialCall;
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = outputAlphabet;
    }

    private boolean wordMakesMMReturn(MealyMachine<?, I, ?, O> mealyMachine, Word<I> localInput) {
        Word<O> output = mealyMachine.computeOutput(localInput);
        return SPMMOutputInterpreter.outputEndsWithReturn(outputAlphabet, output);
    }


    Map<I, Word<I>> calculateTerminatingSequences(
            @Nonnull SPMMInputAlphabet<I> subModelAlphabet,
            @Nonnull Map<I, ? extends MealyMachine<?, I, ?, O>> submodels) {

        final Map<I, Word<I>> terminatingSequences = Maps.newHashMapWithExpectedSize(submodels.size());

        // initial internal sequences
        for (final Entry<I, ? extends MealyMachine<?, I, ?, O>> entry : submodels.entrySet()) {
            final I procedure = entry.getKey();
            final MealyMachine<?, I, ?, O> mealyMachine = entry.getValue();

            if (wordMakesMMReturn(mealyMachine, Word.fromLetter(inputAlphabet.getReturnSymbol()))) {
                terminatingSequences.put(procedure, Word.fromLetter(inputAlphabet.getReturnSymbol()));
            } else {
                final Iterator<Word<I>> iter =
                        Covers.stateCoverIterator(mealyMachine, subModelAlphabet.getInternalAlphabet());
                while (iter.hasNext()) {
                    final Word<I> trace = iter.next();
                    Word<I> inputWithReturn = addReturnSymbol(inputAlphabet, trace);
                    if (wordMakesMMReturn(mealyMachine, inputWithReturn)) {
                        terminatingSequences.put(procedure, inputWithReturn);
                        break;
                    }
                }
            }
        }

        final Set<I> remainingProcedures = new HashSet<>(submodels.keySet());
        remainingProcedures.add(startProcedure);
        remainingProcedures.removeAll(terminatingSequences.keySet());

        boolean stable = false;

        while (!stable) {
            stable = true;

            final Set<I> eligibleInputs = new HashSet<>(subModelAlphabet.getInternalSymbols());
            eligibleInputs.addAll(terminatingSequences.keySet());

            for (final I i : new ArrayList<>(remainingProcedures)) {

                final MealyMachine<?, I, ?, O> mealyMachine = submodels.get(i);
                final Iterator<Word<I>> iter = Covers.stateCoverIterator(mealyMachine, eligibleInputs);

                while (iter.hasNext()) {
                    final Word<I> trace = iter.next();
                    Word<I> inputWithReturn = addReturnSymbol(inputAlphabet, trace);
                    if (wordMakesMMReturn(mealyMachine, inputWithReturn)) {
                        terminatingSequences.put(i,
                                Expansion.expandInput(inputAlphabet, inputWithReturn, terminatingSequences::get));

                        remainingProcedures.remove(i);
                        eligibleInputs.add(i);
                        stable = false;
                        break;
                    }
                }
            }
        }


        if (!remainingProcedures.isEmpty()) {
            throw new AssertionError("There are non-terminating procedures in System Under Learning.");
        }


        return terminatingSequences;
    }

    Map<I, Word<I>> calculateAccessSequences(
            @Nonnull SPMMInputAlphabet<I> subModelAlphabet,
            @Nonnull Map<I, ? extends MealyMachine<?, I, ?, O>> submodels,
            @Nonnull Map<I, Word<I>> terminatingSequences) {

        final Map<I, Word<I>> accessSequences = Maps.newHashMapWithExpectedSize(submodels.size());
        final Set<I> finishedProcedures = Sets.newHashSetWithExpectedSize(submodels.size());

        // initial value
        accessSequences.put(startProcedure, Word.epsilon());
        finishedProcedures.add(startProcedure);

        boolean stable = false;

        while (!stable) {
            stable = true;

            for (final I i : new ArrayList<>(finishedProcedures)) {
                if (!finishedProcedures.containsAll(subModelAlphabet.getCallSymbols())) {
                    stable &= !updateAccessSequencesInternal(
                            subModelAlphabet,
                            i,
                            submodels.get(i),
                            finishedProcedures,
                            terminatingSequences,
                            accessSequences);
                }
            }
        }


        if (!finishedProcedures.containsAll(submodels.keySet())) {
            throw new IllegalStateException("There are non-accessible procedures");
        }

        return accessSequences;
    }

    private boolean updateAccessSequencesInternal(
            SPMMInputAlphabet<I> subModelAlphabet,
            I procedure,
            MealyMachine<?, I, ?, O> mealyMachine,
            Set<I> finishedProcedures,
            Map<I, Word<I>> terminatingSequences,
            Map<I, Word<I>> accessSequences) {

        boolean improved = false;
        final Iterator<Word<I>> transitionCoverIterator =
                Covers.transitionCoverIterator(mealyMachine, subModelAlphabet);

        while (transitionCoverIterator.hasNext()) {
            final Word<I> input = transitionCoverIterator.next();
            final Word<O> output = mealyMachine.computeOutput(input);

            if (inputAlphabet.isCallSymbol(input.lastSymbol()) && outputAlphabet.isProcedureStartSymbol(output.lastSymbol())) {
                final I calledProcedure = input.lastSymbol();
                final Word<I> localAccessSequence = input.prefix(-1);
                final Word<I> expandedInput = Expansion.expandInput(inputAlphabet, localAccessSequence, terminatingSequences::get);
                final Word<I> accessSequenceOfCurrentProcedure = accessSequences.get(procedure);

                final Word<I> potentialAccessSequence = accessSequenceOfCurrentProcedure.concat(Word.fromLetter(procedure), expandedInput);
                final Word<I> oldAccessSequence = accessSequences.get(calledProcedure);

                if (oldAccessSequence == null) {
                    accessSequences.put(calledProcedure, potentialAccessSequence);
                    finishedProcedures.add(calledProcedure);
                    improved = true;
                } else if (potentialAccessSequence.size() < oldAccessSequence.size()) {
                    accessSequences.put(calledProcedure, potentialAccessSequence);
                    improved = true;
                }
            }
        }

        return improved;
    }

    private static <I> Word<I> addReturnSymbol(SPMMInputAlphabet<I> alphabet, Word<I> original) {
        return WordUtils.addSymbol(original, alphabet.getReturnSymbol());
    }


}
