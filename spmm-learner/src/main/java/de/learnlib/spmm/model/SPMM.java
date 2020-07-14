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
package de.learnlib.spmm.model;

import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.model.componenets.State;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.ts.transout.DeterministicTransitionOutputTS;
import net.automatalib.words.Word;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * A system of procedural mealy-machines.
 *
 * @param <S> state type
 * @param <I> input symbol type @param <I>
 * @param <O> output symbol type
 * @param <J> transition type
 */

public interface SPMM<S, I, J, O> extends
        GraphViewable,
        InputAlphabetHolder<I>,
        DeterministicTransitionOutputTS<State<I, S>, I, J, O>,
        SuffixOutput<I, Word<O>> {


    @Nonnull
    @Override
    SPMMInputAlphabet<I> getInputAlphabet();

    void addActivatedCall(I identifier);

    @Nonnull
    Set<I> getActivatedCalls();

    @Nonnull
    SPMMInputAlphabet<I> getInputAlphabetWithOnlyActivatedCalls();

    @Nonnull
    SPMMOutputAlphabet<O> getOutputAlphabet();

    @Nonnull
    @Override
    Word<O> computeSuffixOutput(@Nonnull Iterable<? extends I> iterable, @Nonnull Iterable<? extends I> iterable1);

    @Override
    Graph<?, ?> graphView();

    @Nullable
    @Override
    State<I, S> getInitialState();

    @Override
    @Nonnull
    Word<O> computeOutput(@Nonnull Iterable<? extends I> input);

    default int size() {
        return getProcedures().values().stream().mapToInt(MealyMachine::size).sum() + 3;
    }

    @Nullable
    I getInitialCall();

    @Nonnull
    Map<I, MealyMachine<S, I, ?, O>> getProcedures();

}
