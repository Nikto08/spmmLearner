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

import de.learnlib.spmm.evaluation.BenchmarkUtil;
import de.learnlib.spmm.model.alphabet.DefaultSPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.DefaultSPMMOutputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import net.automatalib.words.impl.Alphabets;

public class LargeSystemsBenchmark {


    public static void main(String[] args) {

        SPMMInputAlphabet<Integer> inputAlphabet
                = new DefaultSPMMInputAlphabet<>(Alphabets.integers(145, 165), Alphabets.integers(0,99), 125);
        SPMMOutputAlphabet<Integer> outputAlphabet
                = new DefaultSPMMOutputAlphabet<>(Alphabets.integers(175, 195),
                112, 116, 118, 120);

        BenchmarkUtil.printBenchmarkResult(SystemBenchmark.run(50, inputAlphabet, outputAlphabet, 100));
    }


}
