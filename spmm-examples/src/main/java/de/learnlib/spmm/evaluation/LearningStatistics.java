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

public class LearningStatistics{

    private final double numberOfCounterexamples;
    private final double numberOfCEForSequencesOnly;
    private final double numberOfTSConformanceChecks;
    private final double numberOfGlobalRefinementSteps;
    private final double numberOfMQs;
    private final double numberOfSymbols;
    private final double isExactModel;
    private final double learningTime;
    private final double hypothesisSize;

    public LearningStatistics(double numberOfCounterexamples,
                              double numberOfCEForSequencesOnly,
                              double numberOfTSConformanceChecks,
                              double numberOfGlobalRefinementSteps,
                              double numberOfMQs,
                              double numberOfSymbols,
                              double isExactModel,
                              double learningTime,
                              double hypothesisSize) {
        this.numberOfCounterexamples = numberOfCounterexamples;
        this.numberOfCEForSequencesOnly = numberOfCEForSequencesOnly;
        this.numberOfTSConformanceChecks = numberOfTSConformanceChecks;
        this.numberOfGlobalRefinementSteps = numberOfGlobalRefinementSteps;
        this.numberOfMQs = numberOfMQs;
        this.numberOfSymbols = numberOfSymbols;
        this.isExactModel = isExactModel;
        this.learningTime = learningTime;
        this.hypothesisSize = hypothesisSize;
    }

    public double getNumberOfCounterexamples() {
        return numberOfCounterexamples;
    }

    public double getNumberOfCEForSequencesOnly() {
        return numberOfCEForSequencesOnly;
    }

    public double getNumberOfTSConformanceChecks() {
        return numberOfTSConformanceChecks;
    }

    public double getNumberOfGlobalRefinementSteps() {
        return numberOfGlobalRefinementSteps;
    }

    public double getNumberOfMQs() {
        return numberOfMQs;
    }

    public double getNumberOfSymbols() {
        return numberOfSymbols;
    }

    public double getIsExactModel(){ return isExactModel; }

    public double getLearningTime() {
        return learningTime;
    }

    public double getHypothesisSize() {
        return hypothesisSize;
    }
}