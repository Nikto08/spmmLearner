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
package de.learnlib.spmm.efficiency_tests;

import de.learnlib.spmm.aal.ATProvider.SimpleATProvider;
import de.learnlib.spmm.evaluation.BenchmarkResult;
import de.learnlib.spmm.evaluation.BenchmarkUtil;
import de.learnlib.spmm.evaluation.LearningStatistics;
import de.learnlib.spmm.model.SPMM;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.model.componenets.State;
import net.automatalib.automata.transout.impl.MealyTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static de.learnlib.spmm.evaluation.BenchmarkUtil.buildAverageStatictics;

public class SystemBenchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkUtil.class);

    public static <I, O> BenchmarkResult run(
            int numberSystems,
            SPMMInputAlphabet<I> inputAlphabet,
            SPMMOutputAlphabet<O> outputAlphabet,
            int procedureSize){
        List<BenchmarkResult> singleSystemsResults = new LinkedList<>();

        for (int i = 0; i < numberSystems; i++) {
            Random random = new Random(89 + i * 8);
            LOGGER.info("current seed is " + (89 + i * 8));

            SPMM<Integer, I, MealyTransition<State<I, Integer>, O>, O> spmm
                    = Generator.create(random, inputAlphabet, outputAlphabet, procedureSize);

            singleSystemsResults.add(BenchmarkUtil.runBenchmarkForOneSPMM(spmm, SimpleATProvider::new, 1));
            // singleSystemsResults.get(singleSystemsResults.size() - 1));
        }

        List<LearningStatistics> allLStar = new LinkedList<>();
        List<LearningStatistics> allRS = new LinkedList<>();
        List<LearningStatistics> allKV = new LinkedList<>();
        List<LearningStatistics> allDT = new LinkedList<>();
        List<LearningStatistics> allTTT = new LinkedList<>();

        for (BenchmarkResult current : singleSystemsResults){
            allLStar.add(current.getLstarResult());
            allRS.add(current.getRsResult());
            allKV.add(current.getKvResult());
            allDT.add(current.getDtResult());
            allTTT.add(current.getTttResult());
        }

        BenchmarkResult average = new BenchmarkResult(
                buildAverageStatictics(allTTT),
                buildAverageStatictics(allDT),
                buildAverageStatictics(allKV),
                buildAverageStatictics(allRS),
                buildAverageStatictics(allLStar)
        );

        return average;
    }

}
