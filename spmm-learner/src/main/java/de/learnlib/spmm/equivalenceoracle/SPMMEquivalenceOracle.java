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

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.spmm.model.SPMM;
import de.learnlib.spmm.model.SPMMBuilder;
import net.automatalib.words.Word;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Equivalence Oracle for SPMM.
 *
 * @param <S> state type
 * @param <I> input symbol type @param <I>
 * @param <O> output symbol type
 */
public interface SPMMEquivalenceOracle<S, I, O> {

    /**
     * if null returned no counterexample could be found
     * query for SPMM of SPMMEquivalenceOracle will be returned
     * this method returns only counterexample made of symbols
     * from activeAlphabet
     */
    @Nullable
    <J> DefaultQuery<I, Word<O>> getSPMMQueryForCounterExample(
            @Nonnull SPMM<S, I, J, O> hypothesis,
            @Nonnull SPMMBuilder<S, I, J, O> builder,
            @Nonnull Set<I> activeAlphabet);

}
