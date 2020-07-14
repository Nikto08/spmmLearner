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

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.spmm.aal.learner.LocalRefinementCounter;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class KearnsVaziraniAdapter<I, O> extends KearnsVaziraniMealy<I, O>
        implements AccessSequenceTransformer<I>, LocalRefinementCounter {

    private long localRefinements;
    private long sumOfLocalCELengths;

    public KearnsVaziraniAdapter(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
        super(alphabet, oracle, false, AcexAnalyzers.LINEAR_FWD);
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Word<O>> ceQuery) {

        boolean result = false;

        while (super.refineHypothesis(ceQuery)) {
            result = true;
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
    public Word<I> transformAccessSequence(Word<I> word) {

        final CompactMealy<I, O> hypothesis = (CompactMealy<I, O>) super.getHypothesisModel();
        final int reachedState = hypothesis.getState(word);

        return super.stateInfos.get(reachedState).accessSequence;
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
