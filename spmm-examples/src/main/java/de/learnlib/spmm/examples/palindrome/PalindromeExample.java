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


import de.learnlib.spmm.model.SPMM;
import de.learnlib.spmm.model.alphabet.DefaultSPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.DefaultSPMMOutputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.model.componenets.State;
import de.learnlib.spmm.model.defaultspmm.DefaultSPMMBuilder;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.automata.transout.impl.MealyTransition;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public final class PalindromeExample {

    private static SPMM<FastMealyState<OutputSymbol>,
            InputSymbol,
            MealyTransition<State<InputSymbol, FastMealyState<OutputSymbol>>, OutputSymbol>,
            OutputSymbol> palindromeSPMM = createSPMMWithAllCallsActivated();

    public static void main(String[] args) {
        testPalindromeSPMM();
    }

    @Test
    public static void testPalindromeSPMM() {
        checkOutputsInP();
        checkOutputSequences();
        Visualization.visualize(palindromeSPMM);
    }


    public static SPMM<FastMealyState<OutputSymbol>,
            InputSymbol,
            MealyTransition<State<InputSymbol, FastMealyState<OutputSymbol>>, OutputSymbol>,
            OutputSymbol> createSPMMWithAllCallsActivated() {
        SPMM<FastMealyState<OutputSymbol>,
                InputSymbol,
                MealyTransition<State<InputSymbol, FastMealyState<OutputSymbol>>, OutputSymbol>,
                OutputSymbol> spmm = createSPMM();
        spmm.addActivatedCall(InputSymbol.P);
        spmm.addActivatedCall(InputSymbol.T);
        return spmm;
    }

    public static SPMM<FastMealyState<OutputSymbol>,
            InputSymbol,
            MealyTransition<State<InputSymbol, FastMealyState<OutputSymbol>>, OutputSymbol>,
            OutputSymbol> createSPMM() {

        final Alphabet<InputSymbol> callAlphabet = Alphabets.fromArray(InputSymbol.P, InputSymbol.T);
        final Alphabet<InputSymbol> internalAlphabet = Alphabets.fromArray(InputSymbol.a, InputSymbol.b, InputSymbol.c);

        final SPMMInputAlphabet<InputSymbol> inputAlphabet = new DefaultSPMMInputAlphabet<>(
                internalAlphabet, callAlphabet, InputSymbol.R);

        final Alphabet<OutputSymbol> internalOutputAlphabet = Alphabets.fromArray(OutputSymbol.a,
                OutputSymbol.b, OutputSymbol.c);

        final SPMMOutputAlphabet<OutputSymbol> outputAlphabet =
                new DefaultSPMMOutputAlphabet<OutputSymbol>(
                        internalOutputAlphabet,
                        OutputSymbol.open, OutputSymbol.close, OutputSymbol.error, OutputSymbol.left);

        // @formatter:off
        final FastMealy<InputSymbol, OutputSymbol> sProcedure =
                AutomatonBuilders.forMealy(new FastMealy<InputSymbol, OutputSymbol>(inputAlphabet))
                        .withInitial("p0")
                        .from("p0")
                        .on(InputSymbol.P).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.T).withOutput(OutputSymbol.open).to("p5")
                        .on(InputSymbol.a).withOutput(OutputSymbol.a).to("p1")
                        .on(InputSymbol.b).withOutput(OutputSymbol.b).to("p2")
                        .on(InputSymbol.c).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.R).withOutput(OutputSymbol.error).to("p6")
                        .from("p1")
                        .on(InputSymbol.P).withOutput(OutputSymbol.open).to("p3")
                        .on(InputSymbol.T).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.a).withOutput(OutputSymbol.a).to("p5")
                        .on(InputSymbol.b).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.c).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.R).withOutput(OutputSymbol.error).to("p6")
                        .from("p2")
                        .on(InputSymbol.P).withOutput(OutputSymbol.open).to("p4")
                        .on(InputSymbol.T).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.a).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.b).withOutput(OutputSymbol.b).to("p5")
                        .on(InputSymbol.c).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.R).withOutput(OutputSymbol.error).to("p6")
                        .from("p3")
                        .on(InputSymbol.P).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.T).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.a).withOutput(OutputSymbol.a).to("p5")
                        .on(InputSymbol.b).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.c).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.R).withOutput(OutputSymbol.error).to("p6")
                        .from("p4")
                        .on(InputSymbol.P).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.T).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.a).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.b).withOutput(OutputSymbol.b).to("p5")
                        .on(InputSymbol.c).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.R).withOutput(OutputSymbol.error).to("p6")
                        .from("p5")
                        .on(InputSymbol.P).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.T).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.a).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.b).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.c).withOutput(OutputSymbol.error).to("p6")
                        .on(InputSymbol.R).withOutput(OutputSymbol.close).to("p7")
                        .from("p6")
                        .on(InputSymbol.P).withOutput(OutputSymbol.error).loop()
                        .on(InputSymbol.T).withOutput(OutputSymbol.error).loop()
                        .on(InputSymbol.a).withOutput(OutputSymbol.error).loop()
                        .on(InputSymbol.b).withOutput(OutputSymbol.error).loop()
                        .on(InputSymbol.c).withOutput(OutputSymbol.error).loop()
                        .on(InputSymbol.R).withOutput(OutputSymbol.error).loop()
                        .from("p7")
                        .on(InputSymbol.P).withOutput(OutputSymbol.left).loop()
                        .on(InputSymbol.T).withOutput(OutputSymbol.left).loop()
                        .on(InputSymbol.a).withOutput(OutputSymbol.left).loop()
                        .on(InputSymbol.b).withOutput(OutputSymbol.left).loop()
                        .on(InputSymbol.c).withOutput(OutputSymbol.left).loop()
                        .on(InputSymbol.R).withOutput(OutputSymbol.left).loop()
                        .create();

        final FastMealy<InputSymbol, OutputSymbol> tProcedure =
                AutomatonBuilders.forMealy(new FastMealy<InputSymbol, OutputSymbol>(inputAlphabet))
                        .withInitial("t0")
                        .from("t0")
                        .on(InputSymbol.P).withOutput(OutputSymbol.open).to("t3")
                        .on(InputSymbol.T).withOutput(OutputSymbol.error).to("t4")
                        .on(InputSymbol.a).withOutput(OutputSymbol.error).to("t4")
                        .on(InputSymbol.b).withOutput(OutputSymbol.error).to("t4")
                        .on(InputSymbol.c).withOutput(OutputSymbol.c).to("t1")
                        .on(InputSymbol.R).withOutput(OutputSymbol.error).to("t4")
                        .from("t1")
                        .on(InputSymbol.P).withOutput(OutputSymbol.error).to("t4")
                        .on(InputSymbol.T).withOutput(OutputSymbol.open).to("t2")
                        .on(InputSymbol.a).withOutput(OutputSymbol.error).to("t4")
                        .on(InputSymbol.b).withOutput(OutputSymbol.error).to("t4")
                        .on(InputSymbol.c).withOutput(OutputSymbol.c).to("t3")
                        .on(InputSymbol.R).withOutput(OutputSymbol.error).to("t4")
                        .from("t2")
                        .on(InputSymbol.P).withOutput(OutputSymbol.error).to("t4")
                        .on(InputSymbol.T).withOutput(OutputSymbol.error).to("t4")
                        .on(InputSymbol.a).withOutput(OutputSymbol.error).to("t4")
                        .on(InputSymbol.b).withOutput(OutputSymbol.error).to("t4")
                        .on(InputSymbol.c).withOutput(OutputSymbol.c).to("t3")
                        .on(InputSymbol.R).withOutput(OutputSymbol.error).to("t4")
                        .from("t3")
                        .on(InputSymbol.P).withOutput(OutputSymbol.error).to("t4")
                        .on(InputSymbol.T).withOutput(OutputSymbol.error).to("t4")
                        .on(InputSymbol.a).withOutput(OutputSymbol.error).to("t4")
                        .on(InputSymbol.b).withOutput(OutputSymbol.error).to("t4")
                        .on(InputSymbol.c).withOutput(OutputSymbol.error).to("t4")
                        .on(InputSymbol.R).withOutput(OutputSymbol.close).to("t5")
                        .from("t4")
                        .on(InputSymbol.P).withOutput(OutputSymbol.error).loop()
                        .on(InputSymbol.T).withOutput(OutputSymbol.error).loop()
                        .on(InputSymbol.a).withOutput(OutputSymbol.error).loop()
                        .on(InputSymbol.b).withOutput(OutputSymbol.error).loop()
                        .on(InputSymbol.c).withOutput(OutputSymbol.error).loop()
                        .on(InputSymbol.R).withOutput(OutputSymbol.error).loop()
                        .from("t5")
                        .on(InputSymbol.P).withOutput(OutputSymbol.left).loop()
                        .on(InputSymbol.T).withOutput(OutputSymbol.left).loop()
                        .on(InputSymbol.a).withOutput(OutputSymbol.left).loop()
                        .on(InputSymbol.b).withOutput(OutputSymbol.left).loop()
                        .on(InputSymbol.c).withOutput(OutputSymbol.left).loop()
                        .on(InputSymbol.R).withOutput(OutputSymbol.left).loop()
                        .create();
        //@formatter:on

        final Map<InputSymbol, FastMealy<InputSymbol, OutputSymbol>> subModels = new HashMap<>();

        subModels.put(InputSymbol.P, sProcedure);
        subModels.put(InputSymbol.T, tProcedure);

        final DefaultSPMMBuilder<FastMealyState<OutputSymbol>,
                InputSymbol,
                OutputSymbol> builder = new DefaultSPMMBuilder<>();

        final SPMM<FastMealyState<OutputSymbol>,
                InputSymbol,
                MealyTransition<State<InputSymbol, FastMealyState<OutputSymbol>>, OutputSymbol>,
                OutputSymbol> spmm = builder.createSPMM(
                inputAlphabet,
                outputAlphabet,
                InputSymbol.P,
                subModels);

        return spmm;

    }


    @Test
    private static void checkOutputsInP() {

        SPMM<FastMealyState<OutputSymbol>,
                InputSymbol,
                MealyTransition<State<InputSymbol, FastMealyState<OutputSymbol>>, OutputSymbol>,
                OutputSymbol> spmm = palindromeSPMM;

        State<InputSymbol, FastMealyState<OutputSymbol>> start = spmm.getInitialState();

        // read "P'"
        assertEquals(OutputSymbol.open, spmm.getTransition(start, InputSymbol.P).getOutput());

        final Stack<State<InputSymbol, FastMealyState<OutputSymbol>>> stack = new Stack<>();
        stack.add(start);

        State<InputSymbol, FastMealyState<OutputSymbol>> pInit = new State<InputSymbol, FastMealyState<OutputSymbol>>(
                InputSymbol.P, spmm.getProcedures().get(InputSymbol.P).getInitialState(), stack);

        State<InputSymbol, FastMealyState<OutputSymbol>> test0 = spmm.getSuccessor(start, InputSymbol.P);

        assertEquals(pInit, test0);

        // read "P'a"
        assertEquals(OutputSymbol.a, spmm.getTransition(test0, InputSymbol.a).getOutput());

        State<InputSymbol, FastMealyState<OutputSymbol>> test1 = spmm.getSuccessor(test0, InputSymbol.a);

        // read "P'aP'"
        assertEquals(OutputSymbol.open, spmm.getTransition(test1, InputSymbol.P).getOutput());

        State<InputSymbol, FastMealyState<OutputSymbol>> test2 = spmm.getSuccessor(test1, InputSymbol.P);

        // read "P'aP'P'"
        assertEquals(OutputSymbol.error, spmm.getTransition(test2, InputSymbol.P).getOutput());

        // read "P'aP'a"
        assertEquals(OutputSymbol.a, spmm.getTransition(test2, InputSymbol.a).getOutput());

        State<InputSymbol, FastMealyState<OutputSymbol>> test3 = spmm.getSuccessor(test2, InputSymbol.a);

        // read "P'aP'aR"
        assertEquals(OutputSymbol.error, spmm.getTransition(test3, InputSymbol.R).getOutput());
        assertNotSame(OutputSymbol.a, spmm.getTransition(test3, InputSymbol.R).getOutput());

        // read "P'aa"
        assertEquals(OutputSymbol.a, spmm.getTransition(test1, InputSymbol.a).getOutput());

        State<InputSymbol, FastMealyState<OutputSymbol>> test4 = spmm.getSuccessor(test1, InputSymbol.a);

        // read "P'aaR"
        assertEquals(OutputSymbol.close, spmm.getTransition(test4, InputSymbol.R).getOutput());
        assertNotSame(OutputSymbol.error, spmm.getTransition(test4, InputSymbol.R).getOutput());

    }


    @Test
    private static void checkOutputSequences() {

        SPMM<FastMealyState<OutputSymbol>,
                InputSymbol,
                MealyTransition<State<InputSymbol, FastMealyState<OutputSymbol>>, OutputSymbol>,
                OutputSymbol> spmm = palindromeSPMM;

        // positive example "P'aaR"
        // expected output is "openaaclose"
        List<InputSymbol> aaList = new ArrayList<>();
        aaList.add(InputSymbol.P);
        aaList.add(InputSymbol.a);
        aaList.add(InputSymbol.a);
        aaList.add(InputSymbol.R);
        List<OutputSymbol> aaOutputList = new ArrayList<>();
        aaOutputList.add(OutputSymbol.open);
        aaOutputList.add(OutputSymbol.a);
        aaOutputList.add(OutputSymbol.a);
        aaOutputList.add(OutputSymbol.close);
        assertThat(spmm.computeOutput(Word.fromList(aaList)), is(equalTo(Word.fromList(aaOutputList))));

        // negative example "P'aR"
        // expected output is "openerrorerror"
        List<InputSymbol> aList = new ArrayList<>();
        aList.add(InputSymbol.P);
        aList.add(InputSymbol.a);
        aList.add(InputSymbol.R);
        List<OutputSymbol> aOutputList = new ArrayList<>();
        aOutputList.add(OutputSymbol.open);
        aOutputList.add(OutputSymbol.a);
        aOutputList.add(OutputSymbol.error);
        assertThat(spmm.computeOutput(Word.fromList(aList)), is(equalTo(Word.fromList(aOutputList))));

        // negative example "P'aaRT'bRR"
        List<InputSymbol> aabList = new ArrayList<>();
        aabList.add(InputSymbol.P);
        aabList.add(InputSymbol.a);
        aabList.add(InputSymbol.a);
        aabList.add(InputSymbol.R);
        aabList.add(InputSymbol.T);
        aabList.add(InputSymbol.b);
        aabList.add(InputSymbol.R);
        List<OutputSymbol> aabOutputList = new ArrayList<>();
        aabOutputList.add(OutputSymbol.open);
        aabOutputList.add(OutputSymbol.a);
        aabOutputList.add(OutputSymbol.a);
        aabOutputList.add(OutputSymbol.close);
        aabOutputList.add(OutputSymbol.error);
        aabOutputList.add(OutputSymbol.error);
        aabOutputList.add(OutputSymbol.error);
        assertThat(spmm.computeOutput(Word.fromList(aabList)), is(equalTo(Word.fromList(aabOutputList))));

        // test what happens by empty input
        List<InputSymbol> emptyList = new ArrayList<>();
        assertThat(spmm.computeOutput(Word.fromList(emptyList)), is(equalTo(Word.epsilon())));
        assertThat(spmm.computeSuffixOutput(Word.fromList(emptyList),
                Word.fromList(emptyList)), is(equalTo(Word.epsilon())));


        // positive example "P'bP'aP'bP'aP'bP'bP'bP'aP'aP'bP'bP'bbRbRbRaRaRbRbRbRaRbRaRbR"
        List<InputSymbol> bababbbaabbbbbbaabbbababList = new ArrayList<>();
        bababbbaabbbbbbaabbbababList.add(InputSymbol.P);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.b);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.P);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.a);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.P);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.b);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.P);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.a);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.P);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.b);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.P);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.b);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.P);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.b);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.P);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.a);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.P);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.a);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.P);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.b);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.P);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.b);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.P);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.b);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.b);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.R);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.b);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.R);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.b);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.R);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.a);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.R);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.a);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.R);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.b);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.R);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.b);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.R);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.b);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.R);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.a);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.R);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.b);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.R);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.a);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.R);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.b);
        bababbbaabbbbbbaabbbababList.add(InputSymbol.R);

        List<OutputSymbol> bababbbaabbbbbbaabbbababOutputList = new ArrayList<>();
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.open);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.b);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.open);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.a);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.open);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.b);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.open);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.a);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.open);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.b);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.open);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.b);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.open);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.b);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.open);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.a);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.open);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.a);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.open);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.b);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.open);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.b);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.open);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.b);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.b);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.close);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.b);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.close);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.b);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.close);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.a);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.close);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.a);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.close);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.b);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.close);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.b);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.close);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.b);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.close);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.a);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.close);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.b);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.close);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.a);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.close);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.b);
        bababbbaabbbbbbaabbbababOutputList.add(OutputSymbol.close);

        assertThat(spmm.computeOutput(Word.fromList(bababbbaabbbbbbaabbbababList)),
                is(equalTo(Word.fromList(bababbbaabbbbbbaabbbababOutputList))));


        // negative example "P'bP'aaaRbR"
        List<InputSymbol> baaabList = new ArrayList<>();
        baaabList.add(InputSymbol.P);
        baaabList.add(InputSymbol.b);
        baaabList.add(InputSymbol.P);
        baaabList.add(InputSymbol.a);
        baaabList.add(InputSymbol.a);
        baaabList.add(InputSymbol.a);
        baaabList.add(InputSymbol.R);
        baaabList.add(InputSymbol.b);
        baaabList.add(InputSymbol.R);
        List<OutputSymbol> baaabOutputList = new ArrayList<>();
        baaabOutputList.add(OutputSymbol.open);
        baaabOutputList.add(OutputSymbol.b);
        baaabOutputList.add(OutputSymbol.open);
        baaabOutputList.add(OutputSymbol.a);
        baaabOutputList.add(OutputSymbol.a);
        baaabOutputList.add(OutputSymbol.error);
        baaabOutputList.add(OutputSymbol.error);
        baaabOutputList.add(OutputSymbol.error);
        baaabOutputList.add(OutputSymbol.error);
        assertThat(spmm.computeOutput(Word.fromList(baaabList)), is(equalTo(Word.fromList(baaabOutputList))));

        // positive example "P'T'ccRR"
        List<InputSymbol> ccList = new ArrayList<>();
        ccList.add(InputSymbol.P);
        ccList.add(InputSymbol.T);
        ccList.add(InputSymbol.c);
        ccList.add(InputSymbol.c);
        ccList.add(InputSymbol.R);
        ccList.add(InputSymbol.R);
        List<OutputSymbol> ccOutputList = new ArrayList<>();
        ccOutputList.add(OutputSymbol.open);
        ccOutputList.add(OutputSymbol.open);
        ccOutputList.add(OutputSymbol.c);
        ccOutputList.add(OutputSymbol.c);
        ccOutputList.add(OutputSymbol.close);
        ccOutputList.add(OutputSymbol.close);
        assertThat(spmm.computeOutput(Word.fromList(ccList)), is(equalTo(Word.fromList(ccOutputList))));

        // negative example "T'ccR"
        List<InputSymbol> ccWList = new ArrayList<>();
        ccWList.add(InputSymbol.T);
        ccWList.add(InputSymbol.c);
        ccWList.add(InputSymbol.c);
        ccWList.add(InputSymbol.R);
        List<OutputSymbol> ccWOutputList = new ArrayList<>();
        ccWOutputList.add(OutputSymbol.error);
        ccWOutputList.add(OutputSymbol.error);
        ccWOutputList.add(OutputSymbol.error);
        ccWOutputList.add(OutputSymbol.error);
        assertThat(spmm.computeOutput(Word.fromList(ccWList)), is(equalTo(Word.fromList(ccWOutputList))));

        // positive example "P'T'cT'P'aaRRcRR"
        List<InputSymbol> caacList = new ArrayList<>();
        caacList.add(InputSymbol.P);
        caacList.add(InputSymbol.T);
        caacList.add(InputSymbol.c);
        caacList.add(InputSymbol.T);
        caacList.add(InputSymbol.P);
        caacList.add(InputSymbol.a);
        caacList.add(InputSymbol.a);
        caacList.add(InputSymbol.R);
        caacList.add(InputSymbol.R);
        caacList.add(InputSymbol.c);
        caacList.add(InputSymbol.R);
        caacList.add(InputSymbol.R);
        List<OutputSymbol> caacOutputList = new ArrayList<>();
        caacOutputList.add(OutputSymbol.open);
        caacOutputList.add(OutputSymbol.open);
        caacOutputList.add(OutputSymbol.c);
        caacOutputList.add(OutputSymbol.open);
        caacOutputList.add(OutputSymbol.open);
        caacOutputList.add(OutputSymbol.a);
        caacOutputList.add(OutputSymbol.a);
        caacOutputList.add(OutputSymbol.close);
        caacOutputList.add(OutputSymbol.close);
        caacOutputList.add(OutputSymbol.c);
        caacOutputList.add(OutputSymbol.close);
        caacOutputList.add(OutputSymbol.close);
        assertThat(spmm.computeOutput(Word.fromList(caacList)), is(equalTo(Word.fromList(caacOutputList))));


        List<InputSymbol> prefixOfcaac = caacList.subList(0, 9);
        List<InputSymbol> suffixOfcaac = caacList.subList(9, 12);
        List<OutputSymbol> outputBySuffixOfcaac = caacOutputList.subList(9, 12);

        assertThat(spmm.computeSuffixOutput(
                Word.fromList(prefixOfcaac), Word.fromList(suffixOfcaac)),
                is(equalTo(Word.fromList(outputBySuffixOfcaac))));


        // positive example "acabccbaca"
        List<InputSymbol> acabccbacaList = new ArrayList<>();
        acabccbacaList.add(InputSymbol.P);
        acabccbacaList.add(InputSymbol.a);
        acabccbacaList.add(InputSymbol.P);
        acabccbacaList.add(InputSymbol.T);
        acabccbacaList.add(InputSymbol.c);
        acabccbacaList.add(InputSymbol.T);
        acabccbacaList.add(InputSymbol.P);
        acabccbacaList.add(InputSymbol.a);
        acabccbacaList.add(InputSymbol.P);
        acabccbacaList.add(InputSymbol.b);
        acabccbacaList.add(InputSymbol.P);
        acabccbacaList.add(InputSymbol.T);
        acabccbacaList.add(InputSymbol.c);
        acabccbacaList.add(InputSymbol.c);
        acabccbacaList.add(InputSymbol.R);
        acabccbacaList.add(InputSymbol.R);
        acabccbacaList.add(InputSymbol.b);
        acabccbacaList.add(InputSymbol.R);
        acabccbacaList.add(InputSymbol.a);
        acabccbacaList.add(InputSymbol.R);
        acabccbacaList.add(InputSymbol.R);
        acabccbacaList.add(InputSymbol.c);
        acabccbacaList.add(InputSymbol.R);
        acabccbacaList.add(InputSymbol.R);
        acabccbacaList.add(InputSymbol.a);
        acabccbacaList.add(InputSymbol.R);
        List<OutputSymbol> acabccbacaOutputList = new ArrayList<>();
        acabccbacaOutputList.add(OutputSymbol.open);
        acabccbacaOutputList.add(OutputSymbol.a);
        acabccbacaOutputList.add(OutputSymbol.open);
        acabccbacaOutputList.add(OutputSymbol.open);
        acabccbacaOutputList.add(OutputSymbol.c);
        acabccbacaOutputList.add(OutputSymbol.open);
        acabccbacaOutputList.add(OutputSymbol.open);
        acabccbacaOutputList.add(OutputSymbol.a);
        acabccbacaOutputList.add(OutputSymbol.open);
        acabccbacaOutputList.add(OutputSymbol.b);
        acabccbacaOutputList.add(OutputSymbol.open);
        acabccbacaOutputList.add(OutputSymbol.open);
        acabccbacaOutputList.add(OutputSymbol.c);
        acabccbacaOutputList.add(OutputSymbol.c);
        acabccbacaOutputList.add(OutputSymbol.close);
        acabccbacaOutputList.add(OutputSymbol.close);
        acabccbacaOutputList.add(OutputSymbol.b);
        acabccbacaOutputList.add(OutputSymbol.close);
        acabccbacaOutputList.add(OutputSymbol.a);
        acabccbacaOutputList.add(OutputSymbol.close);
        acabccbacaOutputList.add(OutputSymbol.close);
        acabccbacaOutputList.add(OutputSymbol.c);
        acabccbacaOutputList.add(OutputSymbol.close);
        acabccbacaOutputList.add(OutputSymbol.close);
        acabccbacaOutputList.add(OutputSymbol.a);
        acabccbacaOutputList.add(OutputSymbol.close);
        assertThat(spmm.computeOutput(Word.fromList(acabccbacaList)),
                is(equalTo(Word.fromList(acabccbacaOutputList))));


        List<InputSymbol> prefixOfacabccbaca = acabccbacaList.subList(0, 6);
        List<InputSymbol> suffixOfacabccbaca = acabccbacaList.subList(6, acabccbacaList.size());
        List<OutputSymbol> outputBysuffixOfacabccbaca = acabccbacaOutputList.subList(6, acabccbacaList.size());
        assertThat(spmm.computeSuffixOutput(
                Word.fromList(prefixOfacabccbaca), Word.fromList(suffixOfacabccbaca)),
                is(equalTo(Word.fromList(outputBysuffixOfacabccbaca))));

        // negative example P'T'ccRcR
        List<InputSymbol> cccList = new ArrayList<>();
        cccList.add(InputSymbol.P);
        cccList.add(InputSymbol.T);
        cccList.add(InputSymbol.c);
        cccList.add(InputSymbol.c);
        cccList.add(InputSymbol.R);
        cccList.add(InputSymbol.c);
        cccList.add(InputSymbol.R);
        List<OutputSymbol> cccOutputList = new ArrayList<>();
        cccOutputList.add(OutputSymbol.open);
        cccOutputList.add(OutputSymbol.open);
        cccOutputList.add(OutputSymbol.c);
        cccOutputList.add(OutputSymbol.c);
        cccOutputList.add(OutputSymbol.close);
        cccOutputList.add(OutputSymbol.error);
        cccOutputList.add(OutputSymbol.error);

        assertThat(spmm.computeOutput(Word.fromList(cccList)), is(equalTo(Word.fromList(cccOutputList))));


    }


}
