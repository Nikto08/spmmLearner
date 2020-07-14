/* Copyright (C) 2020 Markus Frohme.
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

package de.learnlib.spmm.aal.adapter;

import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.closing.ClosingStrategies;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.spmm.aal.learner.LocalRefinementCounter;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.util.Collections;
import java.util.Objects;

public class LStarAdapter<I, O> extends ExtensibleLStarMealy<I, O>
        implements AccessSequenceTransformer<I>, LocalRefinementCounter {

    private long localRefinements;
    private long sumOfLocalCELengths;

    public LStarAdapter(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
        super(alphabet,
              oracle,
              Collections.singletonList(Word.epsilon()),
              ObservationTableCEXHandlers.CLASSIC_LSTAR,
              ClosingStrategies.CLOSE_FIRST);
    }

    @Override
    protected void refineHypothesisInternal(DefaultQuery<I, Word<O>> ceQuery) {
        localRefinements++;
        sumOfLocalCELengths += ceQuery.getInput().length();
        super.refineHypothesisInternal(ceQuery);
    }

    @Override
    public long getNumberOfLocalRefinements() {
        return localRefinements;
    }

    @Override
    public Word<I> transformAccessSequence(Word<I> word) {
        final MealyMachine<?, I, ?, O> hypothesis = super.getHypothesisModel();
        final ObservationTable<I, Word<O>> observationTable = super.getObservationTable();

        final Object reachedState = hypothesis.getState(word);

        for (final Word<I> shortPrefix : observationTable.getShortPrefixes()) {
            final Object reachedSPState = hypothesis.getState(shortPrefix);

            if (Objects.equals(reachedState, reachedSPState)) {
                return shortPrefix;
            }
        }

        throw new IllegalStateException("This should not have happened");
    }

    @Override
    public long getSumOfLocalCELengths() {
        return sumOfLocalCELengths;
    }

    @Override
    public boolean isAccessSequence(Word<I> word) {
        return this.transformAccessSequence(word).equals(word);
    }

}
