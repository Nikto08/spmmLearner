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
package de.learnlib.spmm.model;

import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import net.automatalib.words.Word;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SPMMOutputInterpreter {

    public static <O> boolean outputIndicatesError(
            @Nonnull SPMMOutputAlphabet<O> outputAlphabet, @Nullable Word<O> output) {
        if (output == null || output.size() == 0) {
            return false;
        }
        return outputAlphabet.isErrorSymbol(output.lastSymbol());
    }


    public static <O> int findIndexOFFirstErrorSymbol(
            @Nonnull SPMMOutputAlphabet<O> outputAlphabet, @Nullable Word<O> output) {
        int index = -1;
        if (outputIndicatesError(outputAlphabet, output)) {
            for (O currentSymbol : output) {
                index++;
                if (outputAlphabet.isErrorSymbol(currentSymbol)) {
                    return index;
                }
            }
        }
        return -1;
    }

    public static <O> boolean outputEndsWithReturn(@Nonnull SPMMOutputAlphabet<O> outputAlphabet,
                                                   @Nullable Word<O> output) {
        if (output == null || output.size() == 0) {
            return false;
        }
        return !SPMMOutputInterpreter.outputIndicatesError(outputAlphabet, output)
                && outputAlphabet.isProcedureEndSymbol(output.lastSymbol());
    }

    public static <O> boolean outputEndsWithCall(@Nonnull SPMMOutputAlphabet<O> outputAlphabet, @Nullable Word<O> output) {
        if (output == null || output.size() == 0) {
            return false;
        }
        return !SPMMOutputInterpreter.outputIndicatesError(outputAlphabet, output)
                && outputAlphabet.isProcedureStartSymbol(output.lastSymbol());
    }

    public static <O> int findIndexOFFirstPostReturn(
            @Nonnull SPMMOutputAlphabet<O> outputAlphabet, @Nullable Word<O> output) {
        if (output == null || output.size() == 0) {
            return -1;
        }
        int index = -1;
        for (O currentSymbol : output) {
            index++;
            if (outputAlphabet.isPostReturn(currentSymbol)) {
                return index;
            }
        }
        return -1;
    }


}
