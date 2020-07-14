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

import com.google.common.collect.Sets;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.spmm.aal.ATProvider.ATProvider;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.util.mapping.IndexFinder;
import de.learnlib.spmm.util.mapping.Projection;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper class wraps all expansion and projection operations for SPMMRefiner.
 *
 * @param <I> input symbol type
 * @param <O> output symbol type
 */
class Mapper<I, O> {

    private final ATProvider<I, O> atProvider;
    private final SPMMInputAlphabet<I> inputAlphabet;
    private final SPMMOutputAlphabet<O> outputAlphabet;


    Mapper(ATProvider<I, O> atProvider,
           SPMMInputAlphabet<I> inputAlphabet,
           SPMMOutputAlphabet<O> outputAlphabet
    ) {
        this.atProvider = atProvider;
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = outputAlphabet;
    }

    ATProvider<I, O> getATProvider() {
        return atProvider;
    }

    void updateATProvider(Map<I, ? extends MealyMachine<?, I, ?, O>> procedures,
                          Map<I, ? extends AccessSequenceTransformer<I>> sublearners,
                          Collection<I> activeAlphabet) {
        atProvider.updateSequences(procedures, sublearners, activeAlphabet);
    }

    Pair<Set<Pair<I, Integer>>, Boolean> findNewProceduresAndUpdateATSequences(DefaultQuery<I, Word<O>> query) {
        return atProvider.findNewProceduresAndUpdateSequences(query);
    }

    Set<I> getOnlyTerminatingProcedures(Set<I> toCheck) {
        final Set<I> checkedProcedures = Sets.newHashSetWithExpectedSize(toCheck.size());

        for (I identifier : toCheck) {
            if (atProvider.getAccessSequence(identifier) != null
                    && atProvider.getTerminatingSequence(identifier) != null
                    && outputAlphabet.isProcedureEndSymbol(atProvider.getTerminatingSequenceOutput(identifier).lastSymbol())) {
                checkedProcedures.add(identifier);
            }
        }

        return checkedProcedures;
    }

    DefaultQuery<I, Word<O>> getTerminatingQueryPrependedByCall(I identifier) {
        return new DefaultQuery<>(getTSPrependedByCall(identifier), getTSOutputPrependedByOpen(identifier));
    }

    Word<I> getTSPrependedByCall(I procedureIdentificator) {
        final Word<I> terminatingSequence = atProvider.getTerminatingSequence(procedureIdentificator);
        final WordBuilder<I> embeddedTS = new WordBuilder<>(terminatingSequence.size() + 1);
        embeddedTS.append(procedureIdentificator);
        embeddedTS.append(terminatingSequence);
        return embeddedTS.toWord();
    }

    Word<O> getTSOutputPrependedByOpen(I procedure) {
        final Word<O> terminatingSequenceOutput = atProvider.getTerminatingSequenceOutput(procedure);
        final WordBuilder<O> embeddedTS = new WordBuilder<>(terminatingSequenceOutput.size() + 1);
        embeddedTS.append(outputAlphabet.getProcedureStart());
        embeddedTS.append(terminatingSequenceOutput);
        return embeddedTS.toWord();
    }

    int findCallIndexOfProcedure(DefaultQuery<I, Word<O>> query, int index) {
        return IndexFinder.findCallIndexOfCurrentProcedure(inputAlphabet,
                outputAlphabet, query.getInput(), query.getOutput(), index);
    }

    DefaultQuery<I, Word<O>> getLocalInputAndOutput(DefaultQuery<I, Word<O>> query, int callIdx) {
        int lastIdx = IndexFinder.findLastIndexOfCurrentProcedure(inputAlphabet,
                outputAlphabet, query.getInput(), query.getOutput(), callIdx + 1);

        return Projection.projectExpandedInputOutputPair(inputAlphabet, outputAlphabet, query.getInput().subWord(
                callIdx + 1, lastIdx + 1), query.getOutput().subWord(callIdx + 1, lastIdx + 1));
    }

}
