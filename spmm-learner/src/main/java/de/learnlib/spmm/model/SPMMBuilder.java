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
import net.automatalib.automata.transout.MealyMachine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;


/**
 * A builder for systems of procedural mealy-machines.
 *
 * @param <S> state type
 * @param <I> input symbol type @param <I>
 * @param <O> output symbol type
 * @param <J> transition type
 */
public interface SPMMBuilder<S, I, J, O> {

    @Nonnull
    SPMM<S, I, J, O> createSPMM(@Nonnull SPMMInputAlphabet<I> inputAlphabet,
                                   @Nonnull SPMMOutputAlphabet<O> outputAlphabet,
                                   @Nullable I initialCall,
                                   @Nonnull Map<I, ? extends MealyMachine<S, I, ?, O>> procedures);

    @Nonnull
    SPMM<S, I, J, O> createSPMM(@Nonnull SPMMInputAlphabet<I> inputAlphabet,
                                   @Nonnull Collection<I> activatedCalls,
                                   @Nonnull SPMMOutputAlphabet<O> outputAlphabet,
                                   @Nullable I initialCall,
                                   @Nonnull Map<I, MealyMachine<S, I, ?, O>> procedures);

    @Nonnull
    SPMM<S, I, J, O> createEmptySPMM(@Nonnull SPMMInputAlphabet<I> inputAlphabet,
                                        @Nonnull SPMMOutputAlphabet<O> outputAlphabet);

    @Nonnull
    SPMM<S, I, J, O> copySPMMAddProcedure(@Nonnull SPMM<S, I, J, O> original,
                                             @Nonnull I callSymbol, @Nonnull MealyMachine<S, I, ?, O> procedure);

    @Nonnull
    SPMM<S, I, J, O> copySPMMAddInitialCallProcedure(@Nonnull SPMM<S, I, J, O> original,
                                                        @Nonnull I callSymbol, @Nonnull MealyMachine<S, I, ?, O> procedure);

    @Nonnull
    SPMMInputAlphabet<I> filterCallAlphabet(@Nonnull Collection<I> filter, @Nonnull SPMMInputAlphabet<I> inputAlphabet);

}
