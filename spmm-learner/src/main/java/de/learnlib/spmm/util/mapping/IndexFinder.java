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

import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.util.WordUtils;
import net.automatalib.words.Word;

import javax.annotation.Nonnull;


public class IndexFinder {

    public static <I, O> int findCallIndexOfCurrentProcedure(
            @Nonnull SPMMInputAlphabet<I> inputAlphabet,
            @Nonnull SPMMOutputAlphabet<O> outputAlphabet,
            @Nonnull final Word<I> input,
            @Nonnull final Word<O> output,
            final int index) {

        if (!WordUtils.wordsHaveSameSize(input, output)) {
            throw new AssertionError("input and output must have same size.");
        }

        int balance = 0;

        if (outputAlphabet.isPostReturn(output.getSymbol(index))
                || outputAlphabet.isProcedureEndSymbol(output.getSymbol(index))) {
            balance = 1;
        } else if (outputAlphabet.isProcedureStartSymbol(output.getSymbol(index))) {
            balance = -1;
        }

        for (int i = index; i >= 0; i--) {
            final I inputSymbol = input.getSymbol(i);
            final O outputSymbol = output.getSymbol(i);

            if (inputAlphabet.isReturnSymbol(inputSymbol) && outputAlphabet.isProcedureEndSymbol(output.getSymbol(i))) {
                balance--;
            }

            if (inputAlphabet.isCallSymbol(inputSymbol) && outputAlphabet.isProcedureStartSymbol(outputSymbol)) {
                if (balance == 0) {
                    return i;
                } else {
                    balance++;
                }
            }
        }

        return -1;
    }

    public static <I, O> int findReturnIndexOfCurrentProcedure(
            @Nonnull SPMMInputAlphabet<I> inputAlphabet,
            @Nonnull SPMMOutputAlphabet<O> outputAlphabet,
            @Nonnull final Word<I> input,
            @Nonnull final Word<O> output,
            final int index) {

        if (!WordUtils.wordsHaveSameSize(input, output)) {
            throw new AssertionError("input and output must have same size.");
        }
        int balance = 0;

        for (int i = index; i < input.size(); i++) {
            final I inputSymbol = input.getSymbol(i);
            final O outputSymbol = output.getSymbol(i);

            if (inputAlphabet.isReturnSymbol(inputSymbol)
                    && outputAlphabet.isProcedureEndSymbol(outputSymbol)) {
                if (balance == 0) {
                    return i;
                } else {
                    balance--;
                }
            }
            if (inputAlphabet.isCallSymbol(inputSymbol)
                    && outputAlphabet.isProcedureStartSymbol(outputSymbol)) {
                balance++;
            }
        }

        return -1;
    }

    public static <I, O> int findReturnIndexByCallIndex(
            @Nonnull SPMMInputAlphabet<I> inputAlphabet,
            @Nonnull SPMMOutputAlphabet<O> outputAlphabet,
            @Nonnull final Word<I> input,
            @Nonnull final Word<O> output,
            final int callIndex) {

        if (!WordUtils.wordsHaveSameSize(input, output)) {
            throw new AssertionError("input and output must have same size.");
        }

        if (callIndex >= input.size()) {
            return -1;
        }
        return findReturnIndexOfCurrentProcedure(inputAlphabet, outputAlphabet, input, output, callIndex + 1);
    }


    public static <I, O> int findLastIndexOfCurrentProcedure(
            @Nonnull SPMMInputAlphabet<I> inputAlphabet,
            @Nonnull SPMMOutputAlphabet<O> outputAlphabet,
            @Nonnull final Word<I> input,
            @Nonnull final Word<O> output,
            final int index) {

        int closeIndex = findReturnIndexOfCurrentProcedure(inputAlphabet, outputAlphabet, input, output, index);

        if (closeIndex == -1) {
            return input.size() - 1;
        } else {
            for (int i = closeIndex; i < input.size(); i++) {
                if (outputAlphabet.isPostReturn(output.getSymbol(i))) {
                    closeIndex++;
                }

            }
        }

        return closeIndex;
    }


    /**
     * supposed guarantee, that child procedure is balanced
     */
    public static <I> int findChildReturnIndexByCallIndex(@Nonnull SPMMInputAlphabet<I> inputAlphabet,
                                                          @Nonnull final Word<I> input,
                                                          final int callIndex) {
        if (callIndex >= input.size()) {
            return -1;
        }
        int balance = 0;

        for (int i = callIndex + 1; i < input.size(); i++) {
            final I inputSymbol = input.getSymbol(i);

            if (inputAlphabet.isReturnSymbol(inputSymbol)) {
                if (balance == 0) {
                    return i;
                } else {
                    balance--;
                }
            }
            if (inputAlphabet.isCallSymbol(inputSymbol)) {
                balance++;
            }
        }

        return -1;
    }

}
