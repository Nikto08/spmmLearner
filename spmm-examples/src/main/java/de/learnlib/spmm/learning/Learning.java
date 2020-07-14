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
package de.learnlib.spmm.learning;

import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.algorithm.feature.SupportsGrowingAlphabet;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.spmm.aal.ATProvider.ATProvider;
import de.learnlib.spmm.aal.learner.LocalRefinementCounter;
import de.learnlib.spmm.aal.learner.SPMMLearner;
import de.learnlib.spmm.equivalenceoracle.SPMMEqOr;
import de.learnlib.spmm.equivalenceoracle.SPMMEquivalenceOracle;
import de.learnlib.spmm.model.SPMM;
import de.learnlib.spmm.model.componenets.State;
import de.learnlib.spmm.model.defaultspmm.DefaultSPMMBuilder;
import net.automatalib.automata.transout.impl.MealyTransition;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.util.function.BiFunction;

public class Learning {

    public static <S,
            I,
            O,
            L extends LearningAlgorithm.MealyLearner<I, O>
                    & SupportsGrowingAlphabet<I>
                    & AccessSequenceTransformer<I>
                    & LocalRefinementCounter>
    SPMM<S, I, MealyTransition<State<I, S>, O>, O> learnSystemUsingLearnerProvider(
            final SPMM<S, I, MealyTransition<State<I, S>, O>, O> sul,
            final BiFunction<Alphabet<I>, MembershipOracle<I, Word<O>>, L> learnerProvider,
            final ATProvider<I, O> atrProvider) {
        final MembershipOracle<I, Word<O>> mqOracle = new SimulatorOracle<>(sul);
        final SPMMEquivalenceOracle<S, I, O> eqOracle = new SPMMEqOr<>(sul);

        final SPMMLearner<S, I, MealyTransition<State<I, S>, O>, O, L> learner =
                new SPMMLearner<>(mqOracle,
                        eqOracle,
                        sul.getInputAlphabet(),
                        sul.getOutputAlphabet(),
                        learnerProvider,
                        atrProvider,
                        new DefaultSPMMBuilder<>());

        return learner.computeLearnedModel();

    }

}
