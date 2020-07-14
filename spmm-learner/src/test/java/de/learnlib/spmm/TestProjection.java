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
package de.learnlib.spmm;

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.spmm.model.alphabet.DefaultSPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.DefaultSPMMOutputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.util.mapping.Projection;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import org.junit.Test;


import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TestProjection {

    private SPMMInputAlphabet<InputSymbol> inputAlphabet;
    private SPMMOutputAlphabet<OutputSymbol> outputAlphabet;


    public TestProjection() {
        final Alphabet<InputSymbol> callAlphabet = Alphabets.fromArray(InputSymbol.P, InputSymbol.T);
        final Alphabet<InputSymbol> internalAlphabet = Alphabets.fromArray(InputSymbol.a, InputSymbol.b, InputSymbol.c);

        this.inputAlphabet = new DefaultSPMMInputAlphabet<>(
                internalAlphabet, callAlphabet, InputSymbol.R);


        final Alphabet<OutputSymbol> internalOutputAlphabet = Alphabets.fromArray(OutputSymbol.a,
                OutputSymbol.b, OutputSymbol.c);

        this.outputAlphabet =
                new DefaultSPMMOutputAlphabet<>(
                        internalOutputAlphabet,
                        OutputSymbol.open, OutputSymbol.close, OutputSymbol.error, OutputSymbol.left);

    }

    @Test
    public void firstTestProjection() {
        // global input PaPaaRPaaRR
        WordBuilder<InputSymbol> firstInputBuilder = new WordBuilder<>();
        firstInputBuilder.append(InputSymbol.a);
        firstInputBuilder.append(InputSymbol.P);
        firstInputBuilder.append(InputSymbol.a);
        firstInputBuilder.append(InputSymbol.a);
        firstInputBuilder.append(InputSymbol.R);
        firstInputBuilder.append(InputSymbol.P);
        firstInputBuilder.append(InputSymbol.a);
        firstInputBuilder.append(InputSymbol.a);
        firstInputBuilder.append(InputSymbol.R);
        firstInputBuilder.append(InputSymbol.R);

        WordBuilder<OutputSymbol> firstFullOutputBuilder = new WordBuilder<>();
        firstFullOutputBuilder.append(OutputSymbol.a);
        firstFullOutputBuilder.append(OutputSymbol.open);
        firstFullOutputBuilder.append(OutputSymbol.a);
        firstFullOutputBuilder.append(OutputSymbol.a);
        firstFullOutputBuilder.append(OutputSymbol.close);
        firstFullOutputBuilder.append(OutputSymbol.error);
        firstFullOutputBuilder.append(OutputSymbol.error);
        firstFullOutputBuilder.append(OutputSymbol.error);
        firstFullOutputBuilder.append(OutputSymbol.error);
        firstFullOutputBuilder.append(OutputSymbol.error);

        DefaultQuery<InputSymbol, Word<OutputSymbol>> firstProjectedFullQuery
                = Projection.projectInputOutputPair(
                inputAlphabet,
                outputAlphabet,
                firstInputBuilder.toWord(),
                firstFullOutputBuilder.toWord()
        );

        WordBuilder<InputSymbol> expectedFullInputBuilder = new WordBuilder<>();
        expectedFullInputBuilder.append(InputSymbol.a);
        expectedFullInputBuilder.append(InputSymbol.P);
        expectedFullInputBuilder.append(InputSymbol.P);
        expectedFullInputBuilder.append(InputSymbol.R);

        WordBuilder<OutputSymbol> expectedFullOutputBuilder = new WordBuilder<>();
        expectedFullOutputBuilder.append(OutputSymbol.a);
        expectedFullOutputBuilder.append(OutputSymbol.open);
        expectedFullOutputBuilder.append(OutputSymbol.error);
        expectedFullOutputBuilder.append(OutputSymbol.error);

        assertEquals(expectedFullInputBuilder.toWord(), firstProjectedFullQuery.getSuffix());
        assertEquals(expectedFullOutputBuilder.toWord(), firstProjectedFullQuery.getOutput());

    }

    @Test
    public void secondTestProjection() {
        // global input PTccRR
        WordBuilder<InputSymbol> secondInputBuilder = new WordBuilder<>();
        secondInputBuilder.append(InputSymbol.c);
        secondInputBuilder.append(InputSymbol.c);
        secondInputBuilder.append(InputSymbol.R);
        secondInputBuilder.append(InputSymbol.R);

        WordBuilder<OutputSymbol> secondFullOutputBuilder = new WordBuilder<>();
        secondFullOutputBuilder.append(OutputSymbol.c);
        secondFullOutputBuilder.append(OutputSymbol.c);
        secondFullOutputBuilder.append(OutputSymbol.close);
        secondFullOutputBuilder.append(OutputSymbol.close);

        DefaultQuery<InputSymbol, Word<OutputSymbol>> secondFullProjectedQuery
                = Projection.projectInputOutputPair(
                inputAlphabet,
                outputAlphabet,
                secondInputBuilder.toWord(),
                secondFullOutputBuilder.toWord()
        );

        ArrayList<InputSymbol> secondFullExpectedInput = new ArrayList<>();
        secondFullExpectedInput.add(InputSymbol.c);
        secondFullExpectedInput.add(InputSymbol.c);
        secondFullExpectedInput.add(InputSymbol.R);
        secondFullExpectedInput.add(InputSymbol.R);

        ArrayList<OutputSymbol> secondFullExpectedOutput = new ArrayList<>();
        secondFullExpectedOutput.add(OutputSymbol.c);
        secondFullExpectedOutput.add(OutputSymbol.c);
        secondFullExpectedOutput.add(OutputSymbol.close);
        secondFullExpectedOutput.add(OutputSymbol.left);

        assertEquals(Word.fromList(secondFullExpectedInput), secondFullProjectedQuery.getSuffix());
        assertEquals(Word.fromList(secondFullExpectedOutput), secondFullProjectedQuery.getOutput());

    }

    @Test
    public void thirdTestOutputProjection() {
        // Local query:Query[b|P /open], local output:open
        // Expanded query:Query[P b| P b R /null],  expanded output: open b error
        WordBuilder<InputSymbol> thirdInputBuilder = new WordBuilder<>();
        thirdInputBuilder.append(InputSymbol.b);
        thirdInputBuilder.append(InputSymbol.P);
        thirdInputBuilder.append(InputSymbol.b);
        thirdInputBuilder.append(InputSymbol.R);

        WordBuilder<OutputSymbol> thirdFullOutputBuilder = new WordBuilder<>();
        thirdFullOutputBuilder.append(OutputSymbol.b);
        thirdFullOutputBuilder.append(OutputSymbol.open);
        thirdFullOutputBuilder.append(OutputSymbol.b);
        thirdFullOutputBuilder.append(OutputSymbol.error);

        DefaultQuery<InputSymbol, Word<OutputSymbol>> secondFullProjectedQuery
                = Projection.projectInputOutputPair(
                inputAlphabet,
                outputAlphabet,
                thirdInputBuilder.toWord(), thirdFullOutputBuilder.toWord()
        );

        ArrayList<InputSymbol> thirdFullExpectedInput = new ArrayList<>();
        thirdFullExpectedInput.add(InputSymbol.b);
        thirdFullExpectedInput.add(InputSymbol.P);

        ArrayList<OutputSymbol> thirdFullExpectedOutput = new ArrayList<>();
        thirdFullExpectedOutput.add(OutputSymbol.b);
        thirdFullExpectedOutput.add(OutputSymbol.open);

        assertEquals(Word.fromList(thirdFullExpectedInput), secondFullProjectedQuery.getSuffix());
        assertEquals(Word.fromList(thirdFullExpectedOutput), secondFullProjectedQuery.getOutput());

    }




  /*

    Local query:Query[b|a /error],  local output:error
    Expanded query:Query[ P b|a /null],  expanded output:error

    Local query:Query[b|b /b], local output:b
    Expanded query:Query[ P b|b /null],expanded output:b

    Local query:Query[b|P /open], local output:open
    Expanded query:Query[ P b| P b R /null],  expanded output: open b error

    Local query:Query[ a a|P /error], local output:error
    Expanded query:Query[ P a  a|  P a a R /null],  expanded output:  error error  error error

    Local query:Query[a| a R / a close],
    local output:  a close
    Expanded query:Query[ P a|  a R /null],  expanded output:   a close

    */


}
