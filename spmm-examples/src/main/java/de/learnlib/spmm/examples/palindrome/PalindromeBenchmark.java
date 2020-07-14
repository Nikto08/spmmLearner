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
package de.learnlib.spmm.examples.palindrome;

import de.learnlib.spmm.aal.ATProvider.SimpleATProvider;
import de.learnlib.spmm.evaluation.BenchmarkUtil;
import de.learnlib.spmm.model.SPMM;
import de.learnlib.spmm.model.componenets.State;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.automata.transout.impl.MealyTransition;

public class PalindromeBenchmark {

    public static void main(String[] args) {
        SPMM<FastMealyState<OutputSymbol>,
                InputSymbol,
                MealyTransition<State<InputSymbol, FastMealyState<OutputSymbol>>, OutputSymbol>,
                OutputSymbol> spmm = PalindromeExample.createSPMMWithAllCallsActivated();

        BenchmarkUtil.printBenchmarkResult(BenchmarkUtil.runBenchmarkForOneSPMM(spmm, SimpleATProvider::new, 1));
    }
}
