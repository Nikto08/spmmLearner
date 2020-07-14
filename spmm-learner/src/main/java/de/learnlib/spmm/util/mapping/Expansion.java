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
import de.learnlib.api.query.Query;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class Expansion {


    /**
     * Replaces all abstracted procedure calls with their corresponding terminating sequence.
     */
    @Nonnull
    public static <I> Word<I> expandInput(@Nonnull SPMMInputAlphabet<I> alphabet,
                                          @Nonnull final Word<I> input,
                                          @Nonnull final Function<I, Word<I>> terminatingSequenceProvider) {
        final WordBuilder<I> wb = new WordBuilder<>();

        for (final I symbol : input) {

            if (alphabet.isCallSymbol(symbol)) {
                wb.append(symbol);
                wb.append(terminatingSequenceProvider.apply(symbol));
            } else {
                wb.append(symbol);
            }
        }

        return wb.toWord();
    }

    @Nonnull
    public static <I, O> Query<I, Word<O>> buildGlobalInputForProcedure(
            @Nonnull SPMMInputAlphabet<I> alphabet,
            @Nonnull I identifier,
            @Nonnull Query<I, Word<O>> localQuery,
            @Nonnull final Function<I, Word<I>> accessSequenceProvider,
            @Nonnull final Function<I, Word<I>> terminatingSequenceProvider) {

        final WordBuilder<I> prefixBuilder = new WordBuilder<>();
        prefixBuilder.append(accessSequenceProvider.apply(identifier));
        prefixBuilder.append(identifier);

        prefixBuilder.append(Expansion.expandInput(
                alphabet, localQuery.getPrefix(), terminatingSequenceProvider));

        final WordBuilder<I> suffixBuilder = new WordBuilder<>();
        suffixBuilder.append(Expansion.expandInput(
                alphabet, localQuery.getSuffix(), terminatingSequenceProvider));


        return new DefaultQuery<>(prefixBuilder.toWord(), suffixBuilder.toWord());
    }


    /**
     * Replaces all abstracted procedure calls with their corresponding terminating sequence
     * unless their output is error of left
     */
    @Nonnull
    public static <I, O> Pair<Word<I>, Word<O>> expandInputOutputPair(
            @Nonnull SPMMInputAlphabet<I> inputAlphabet,
            @Nonnull SPMMOutputAlphabet<O> outputAlphabet,
            @Nonnull final Word<I> input,
            @Nonnull final Word<O> output,
            @Nonnull final Function<I, Word<I>> terminatingSequenceProvider,
            @Nonnull final Function<I, Word<O>> terminatingSequenceOutputProvider) {
        if (input.length() != output.length()) {
            throw new IllegalArgumentException("Length of input and output must be equal.");
        }
        final WordBuilder<I> inputBuilder = new WordBuilder<>();
        final WordBuilder<O> outputBuilder = new WordBuilder<>();
        boolean errorOccured = false;
        boolean procedureReturned = false;

        for (int index = 0; index < input.length(); index++) {
            final I inputSymbol = input.getSymbol(index);
            final O outputSymbol = output.getSymbol(index);

            if (outputAlphabet.isPostReturn(outputSymbol)) {
                procedureReturned = true;
            } else if (outputAlphabet.isErrorSymbol(outputSymbol)) {
                errorOccured = true;
            }

            if (inputAlphabet.isCallSymbol(inputSymbol) && !errorOccured && !procedureReturned) {

                inputBuilder.append(inputSymbol);
                inputBuilder.append(terminatingSequenceProvider.apply(inputSymbol));

                if (procedureReturned) {
                    for (int iterations = terminatingSequenceProvider.apply(inputSymbol).length();
                         iterations > -1; iterations--) {
                        outputBuilder.append(outputAlphabet.getPostReturn());
                    }
                } else if (errorOccured) {
                    for (int iterations = terminatingSequenceProvider.apply(inputSymbol).length();
                         iterations > -1; iterations--) {
                        outputBuilder.append(outputAlphabet.getError());
                    }
                } else {
                    outputBuilder.append(outputAlphabet.getProcedureStart());
                    outputBuilder.append(terminatingSequenceOutputProvider.apply(inputSymbol));
                }

            } else {
                inputBuilder.append(inputSymbol);
                outputBuilder.append(outputSymbol);
            }
        }

        return new Pair<>(inputBuilder.toWord(), outputBuilder.toWord());
    }

    @Nonnull
    public static <I, O> DefaultQuery<I, Word<O>> buildGlobalInputAndOutputForProcedure(
            @Nonnull SPMMInputAlphabet<I> inputAlphabet,
            @Nonnull SPMMOutputAlphabet<O> outputAlphabet,
            @Nonnull I identifier,
            @Nonnull Word<I> localInput,
            @Nonnull Word<O> localOutput,
            @Nonnull final Function<I, Word<I>> accessSequenceProvider,
            @Nonnull final Function<I, Word<I>> terminatingSequenceProvider,
            @Nonnull final Function<I, Word<O>> accessSequencesOutputProvider,
            @Nonnull final Function<I, Word<O>> terminatingSequenceOutputProvider) {

        final WordBuilder<I> inputBuilder = new WordBuilder<>();
        inputBuilder.append(accessSequenceProvider.apply(identifier));
        inputBuilder.append(identifier);

        final WordBuilder<O> outputBuilder = new WordBuilder<>();
        outputBuilder.append(accessSequencesOutputProvider.apply(identifier));
        outputBuilder.append(outputAlphabet.getProcedureStart());

        Pair<Word<I>, Word<O>> expandedLocalQuery = Expansion.expandInputOutputPair(
                inputAlphabet, outputAlphabet, localInput, localOutput, terminatingSequenceProvider,
                terminatingSequenceOutputProvider
        );
        inputBuilder.append(expandedLocalQuery.getFirst());
        outputBuilder.append(expandedLocalQuery.getSecond());


        return new DefaultQuery<>(inputBuilder.toWord(), outputBuilder.toWord());
    }

}
