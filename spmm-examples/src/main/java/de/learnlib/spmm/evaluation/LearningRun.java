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
package de.learnlib.spmm.evaluation;

import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.algorithm.feature.SupportsGrowingAlphabet;
import de.learnlib.spmm.aal.learner.LocalRefinementCounter;
import de.learnlib.spmm.aal.learner.SPMMLearner;
import de.learnlib.spmm.model.SPMM;
import de.learnlib.spmm.model.SPMMEquivalenceChecker;
import org.apache.commons.lang3.time.StopWatch;


public final class LearningRun {

    private LearningRun() {
    }

    public static <I, O,
            L extends LearningAlgorithm.MealyLearner<I, O>
                    & SupportsGrowingAlphabet<I>
                    & AccessSequenceTransformer<I>
                    & LocalRefinementCounter> LearningStatistics run(
            SPMMLearner<?, I, ?, O, L> learner, SPMM<?, I, ?, O> sul) {

        final StopWatch sw = StopWatch.createStarted();
        SPMM<?, I, ?, O> model = learner.computeLearnedModel();
        sw.stop();

        return new LearningStatistics(
                learner.getNumberOfCounterexamples(),
                learner.getNumberOfCEForSequencesOnly(),
                learner.getNumberOfTSConformanceChecks(),
                learner.getNumberOfGlobalRefinements(),
                learner.getNumberOfMembershipQueries(),
                learner.getNumberOfMembershipSymbols(),
                SPMMEquivalenceChecker.haveIsomorphProceduralMap(sul, model) ? 1 : 0,
                sw.getTime(),
                learner.getHypothesisModel().size());
    }

}
