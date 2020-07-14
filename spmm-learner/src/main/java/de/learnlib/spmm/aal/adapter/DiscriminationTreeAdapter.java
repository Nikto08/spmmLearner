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

import de.learnlib.algorithms.discriminationtree.mealy.DTLearnerMealy;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.spmm.aal.learner.LocalRefinementCounter;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class DiscriminationTreeAdapter<I, O> extends DTLearnerMealy<I, O>
        implements AccessSequenceTransformer<I>, LocalRefinementCounter {

    private long localRefinements;
    private long sumOfLocalCELengths;

    public DiscriminationTreeAdapter(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
        super(alphabet, oracle, LocalSuffixFinders.RIVEST_SCHAPIRE, true);
    }

    @Override
    protected boolean refineHypothesisSingle(DefaultQuery<I, Word<O>> ceQuery) {

        final boolean result = super.refineHypothesisSingle(ceQuery);

        if (result) {
            localRefinements++;
            sumOfLocalCELengths += ceQuery.getInput().length();
        }

        return result;
    }

    @Override
    public Word<I> transformAccessSequence(Word<I> word) {
        return super.getHypothesisDS().transformAccessSequence(word);
    }

    @Override
    public boolean isAccessSequence(Word<I> word) {
        return super.getHypothesisDS().isAccessSequence(word);
    }

    @Override
    public long getNumberOfLocalRefinements() {
        return localRefinements;
    }

    @Override
    public long getSumOfLocalCELengths() {
        return sumOfLocalCELengths;
    }

}
