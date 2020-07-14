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
package de.learnlib.spmm.aal.adapter;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.spmm.aal.learner.LocalRefinementCounter;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import javax.annotation.Nonnull;

/**
 * Adapter for using {@link TTTLearnerMealy} as a procedural learner.
 *
 * @param <I> input symbol type
 * @param <O> output symbol type
 */
public class TTTAdapter<I, O>
        extends TTTLearnerMealy<I, O>
        implements AccessSequenceTransformer<I>,
        LocalRefinementCounter {

    private long localRefinements;
    private long sumOfLocalCELengths;

    public TTTAdapter(@Nonnull Alphabet<I> alphabet, @Nonnull MembershipOracle<I, Word<O>> oracle) {
        super(alphabet, oracle, AcexAnalyzers.BINARY_SEARCH_BWD);
    }

    @Override
    public boolean refineHypothesisSingle(@Nonnull DefaultQuery<I, Word<O>> ceQuery) {

        final boolean result = super.refineHypothesisSingle(ceQuery);


        if (result) {
            localRefinements++;
            sumOfLocalCELengths += ceQuery.getInput().length();
        }

        return result;
    }

    @Override
    public long getNumberOfLocalRefinements() {
        return localRefinements;
    }

    @Override
    public Word<I> transformAccessSequence(@Nonnull Word<I> word) {
        return super.getHypothesisDS().getState(word).getAccessSequence();
    }

    @Override
    public long getSumOfLocalCELengths() {
        return sumOfLocalCELengths;
    }

    @Override
    public boolean isAccessSequence(@Nonnull Word<I> word) {
        return this.transformAccessSequence(word).equals(word);
    }

}
