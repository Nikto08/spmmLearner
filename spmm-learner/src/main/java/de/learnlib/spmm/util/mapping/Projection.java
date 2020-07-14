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
package de.learnlib.spmm.util.mapping;

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.util.WordUtils;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import javax.annotation.Nonnull;

public class Projection {

    /**
     * replaces all inner calls with an abstracted procedure call.
     */
    @Nonnull
    public static <I, O> DefaultQuery<I, Word<O>> projectInputOutputPair(
            @Nonnull SPMMInputAlphabet<I> inputAlphabet,
            @Nonnull SPMMOutputAlphabet<O> outputAlphabet,
            @Nonnull final Word<I> input,
            @Nonnull final Word<O> output) {


        if (!WordUtils.wordsHaveSameSize(input, output)) {
            throw new AssertionError("Input and output must have same size.");
        }
        int firstReturnPosition = IndexFinder.findReturnIndexOfCurrentProcedure(
                inputAlphabet, outputAlphabet, input, output, 0);

        if (firstReturnPosition >= input.length()) {
            throw new AssertionError("Position of first return in the input is not a valid input symbol position");
        }


        final WordBuilder<I> inputBuilder = new WordBuilder<>(input.size());
        final WordBuilder<O> outputBuilder = new WordBuilder<>(output.size());

        for (int i = 0; i < input.size(); i++) {
            final I symbol = input.getSymbol(i);


            inputBuilder.append(input.getSymbol(i));
            if (firstReturnPosition == -1 || i <= firstReturnPosition) {
                outputBuilder.append(output.getSymbol(i));
            } else {
                outputBuilder.append(outputAlphabet.getPostReturn());
            }


            if (inputAlphabet.isCallSymbol(symbol)) {
                final int childReturnIdx = IndexFinder.findChildReturnIndexByCallIndex(inputAlphabet,
                        input, i);
                if (childReturnIdx == -1) {
                    i = input.size();
                } else {
                    i = childReturnIdx;
                }
            }
        }

        return new DefaultQuery<>(inputBuilder.toWord(), outputBuilder.toWord());
    }

    /**
     * replaces all inner calls with an abstracted procedure call
     */
    @Nonnull
    public static <I, O> DefaultQuery<I, Word<O>> projectExpandedInputOutputPair(
            @Nonnull SPMMInputAlphabet<I> inputAlphabet,
            @Nonnull SPMMOutputAlphabet<O> outputAlphabet,
            @Nonnull final Word<I> input,
            @Nonnull final Word<O> output) {


        if (!WordUtils.wordsHaveSameSize(input, output)) {
            throw new AssertionError("Input and output must have same size.");
        }
        int firstReturnPosition = IndexFinder.findReturnIndexOfCurrentProcedure(
                inputAlphabet, outputAlphabet, input, output, 0);

        if (firstReturnPosition >= input.length()) {
            throw new AssertionError("Position of first return in the input is not a valid input symbol position");
        }


        final WordBuilder<I> inputBuilder = new WordBuilder<>(input.size());
        final WordBuilder<O> outputBuilder = new WordBuilder<>(output.size());

        for (int i = 0; i < input.size(); i++) {
            final I symbol = input.getSymbol(i);


            inputBuilder.append(input.getSymbol(i));
            if (firstReturnPosition == -1 || i <= firstReturnPosition) {
                outputBuilder.append(output.getSymbol(i));
            } else {
                outputBuilder.append(outputAlphabet.getPostReturn());
            }


            if (inputAlphabet.isCallSymbol(symbol) && !outputAlphabet.isErrorSymbol(output.getSymbol(i))
                    && !outputAlphabet.isPostReturn(output.getSymbol(i))) {
                final int childReturnIdx = IndexFinder.findChildReturnIndexByCallIndex(inputAlphabet,
                        input, i);
                if (childReturnIdx == -1) {
                    i = input.size();
                } else {
                    i = childReturnIdx;
                }
            }
        }

        return new DefaultQuery<>(inputBuilder.toWord(), outputBuilder.toWord());
    }


}
