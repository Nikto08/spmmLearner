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
import de.learnlib.spmm.aal.ATProvider.ATProvider;
import de.learnlib.spmm.aal.ATProvider.SimpleATProvider;
import de.learnlib.spmm.model.alphabet.DefaultSPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.DefaultSPMMOutputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.util.mapping.Expansion;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class TestExpansionWithATProvider {

    private SPMMInputAlphabet<InputSymbol> inputAlphabet;
    private ATProvider<InputSymbol, OutputSymbol> simpleATProvider;

    public TestExpansionWithATProvider() {
        final Alphabet<InputSymbol> callAlphabet = Alphabets.fromArray(InputSymbol.P, InputSymbol.T);
        final Alphabet<InputSymbol> internalAlphabet = Alphabets.fromArray(InputSymbol.a, InputSymbol.b, InputSymbol.c);

        final SPMMInputAlphabet<InputSymbol> inputAlphabet = new DefaultSPMMInputAlphabet<>(
                internalAlphabet, callAlphabet, InputSymbol.R);

        final Alphabet<OutputSymbol> internalOutputAlphabet = Alphabets.fromArray(OutputSymbol.a,
                OutputSymbol.b, OutputSymbol.c);

        final SPMMOutputAlphabet<OutputSymbol> outputAlphabet =
                new DefaultSPMMOutputAlphabet<>(
                        internalOutputAlphabet,
                        OutputSymbol.open, OutputSymbol.close, OutputSymbol.error, OutputSymbol.left);

        this.inputAlphabet = inputAlphabet;
        this.simpleATProvider = new SimpleATProvider<>(inputAlphabet, outputAlphabet);
    }


    @Test
    public void firstTestExpansionWithATProvider() {
       // learn AT sequences from OracleQuery Pa
        DefaultQuery<InputSymbol, Word<OutputSymbol>> paQuery = new DefaultQuery<>(
                Word.fromSymbols(InputSymbol.P, InputSymbol.a));
        paQuery.answer(Word.fromSymbols(OutputSymbol.open, OutputSymbol.a));
        simpleATProvider.findNewProceduresAndUpdateSequences(paQuery);

        assertEquals(Word.epsilon(), simpleATProvider.getAccessSequence(InputSymbol.P));

        //expand local input a

        assertEquals(Word.fromLetter(InputSymbol.a),
                Expansion.expandInput(inputAlphabet, Word.fromLetter(InputSymbol.a),
                simpleATProvider.getTerminatingSequenceProvider()));

    }


    @Test
    public void secondTestExpansionWithATProvider() {
        // learn AT sequences from OracleQuery PbPTcTccRc
        WordBuilder<InputSymbol> secondInputBuilder = new WordBuilder<>();
        secondInputBuilder.append(InputSymbol.P);
        secondInputBuilder.append(InputSymbol.b);
        secondInputBuilder.append(InputSymbol.P);
        secondInputBuilder.append(InputSymbol.T);
        secondInputBuilder.append(InputSymbol.c);
        secondInputBuilder.append(InputSymbol.T);
        secondInputBuilder.append(InputSymbol.c);
        secondInputBuilder.append(InputSymbol.c);
        secondInputBuilder.append(InputSymbol.R);
        secondInputBuilder.append(InputSymbol.c);

        WordBuilder<OutputSymbol> secondOutputBuilder = new WordBuilder<>();
        secondOutputBuilder.append(OutputSymbol.open);
        secondOutputBuilder.append(OutputSymbol.b);
        secondOutputBuilder.append(OutputSymbol.open);
        secondOutputBuilder.append(OutputSymbol.open);
        secondOutputBuilder.append(OutputSymbol.c);
        secondOutputBuilder.append(OutputSymbol.open);
        secondOutputBuilder.append(OutputSymbol.c);
        secondOutputBuilder.append(OutputSymbol.c);
        secondOutputBuilder.append(OutputSymbol.close);
        secondOutputBuilder.append(OutputSymbol.c);

        DefaultQuery<InputSymbol, Word<OutputSymbol>> pbPTcTcTccRcQuery = new DefaultQuery<>(
                secondInputBuilder.toWord());
        pbPTcTcTccRcQuery.answer(secondOutputBuilder.toWord());
        simpleATProvider.findNewProceduresAndUpdateSequences(pbPTcTcTccRcQuery);

        assertEquals(Word.epsilon(), simpleATProvider.getAccessSequence(InputSymbol.P));

        WordBuilder<InputSymbol> secondExpectedTerminatingSequenceInput = new WordBuilder<>();
        secondExpectedTerminatingSequenceInput.append(InputSymbol.T);
        secondExpectedTerminatingSequenceInput.append(InputSymbol.c);
        secondExpectedTerminatingSequenceInput.append(InputSymbol.T);
        secondExpectedTerminatingSequenceInput.append(InputSymbol.c);
        secondExpectedTerminatingSequenceInput.append(InputSymbol.c);
        secondExpectedTerminatingSequenceInput.append(InputSymbol.R);
        secondExpectedTerminatingSequenceInput.append(InputSymbol.c);
        secondExpectedTerminatingSequenceInput.append(InputSymbol.R);

        WordBuilder<OutputSymbol> secondExpectedTerminatingSequenceOutput = new WordBuilder<>();
        secondExpectedTerminatingSequenceOutput.append(OutputSymbol.open);
        secondExpectedTerminatingSequenceOutput.append(OutputSymbol.c);
        secondExpectedTerminatingSequenceOutput.append(OutputSymbol.open);
        secondExpectedTerminatingSequenceOutput.append(OutputSymbol.c);
        secondExpectedTerminatingSequenceOutput.append(OutputSymbol.c);
        secondExpectedTerminatingSequenceOutput.append(OutputSymbol.close);
        secondExpectedTerminatingSequenceOutput.append(OutputSymbol.c);
        secondExpectedTerminatingSequenceOutput.append(OutputSymbol.error);

        //expand local input a

        assertEquals(Word.fromLetter(InputSymbol.a),
                Expansion.expandInput(inputAlphabet, Word.fromLetter(InputSymbol.a),
                        simpleATProvider.getTerminatingSequenceProvider()));


    }


}
