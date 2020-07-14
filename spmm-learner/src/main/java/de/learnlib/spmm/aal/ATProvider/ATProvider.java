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

import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public interface ATProvider<I, O> {

    Word<I> getAccessSequence(@Nonnull I procedure);

    Function<I, Word<I>> getAccessSequenceProvider();

    Word<I> getTerminatingSequence(@Nonnull I procedure);

    void addTerminatingSequence(@Nonnull I procedure, @Nonnull Word<I> ts);

    Function<I, Word<I>> getTerminatingSequenceProvider();

    Word<O> getTerminatingSequenceOutput(@Nonnull I procedure);

    /**
     * Scans error-free beginning of the query to update assess sequences and terminating sequences.
     * Returns a set of pairs "identifier C of found new procedure in error-free prefix of @param query"
     * -"index of the call of C in error-free prefix of @param query" and a boolean, which indicates
     * whether any assess sequences or terminating sequences were added or changed.
     *
     * @param query must have prefix epsilon.
     */
    Pair<Set<Pair<I, Integer>>, Boolean> findNewProceduresAndUpdateSequences(@Nonnull DefaultQuery<I, Word<O>> query);

    /**
     * Uses procedural learners or some other AssessSequenceTransformer to find shorter
     * assess sequences and terminating sequences by current validInputSymbols.
     */
    void updateSequences(@Nonnull Map<I, ? extends MealyMachine<?, I, ?, O>> procedures,
                         @Nonnull Map<I, ? extends AccessSequenceTransformer<I>> providers,
                         @Nonnull Collection<I> validInputSymbols);

}
