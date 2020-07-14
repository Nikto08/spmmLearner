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

public class BenchmarkResult {

    private final LearningStatistics tttResult;
    private final LearningStatistics dtResult;
    private final LearningStatistics kvResult;
    private final LearningStatistics rsResult;
    private final LearningStatistics lstarResult;

    public BenchmarkResult(LearningStatistics tttResult,
                           LearningStatistics dtResult,
                           LearningStatistics kvResult,
                           LearningStatistics rsResult,
                           LearningStatistics lstarResult) {
        this.tttResult = tttResult;
        this.dtResult = dtResult;
        this.kvResult = kvResult;
        this.rsResult = rsResult;
        this.lstarResult = lstarResult;
    }

    public LearningStatistics getTttResult() {
        return tttResult;
    }

    public LearningStatistics getDtResult() {
        return dtResult;
    }

    public LearningStatistics getKvResult() {
        return kvResult;
    }

    public LearningStatistics getRsResult() {
        return rsResult;
    }

    public LearningStatistics getLstarResult() {
        return lstarResult;
    }
}
