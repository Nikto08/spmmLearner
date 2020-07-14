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
package de.learnlib.spmm.model.alphabet;


import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractList;


public class DefaultSPMMOutputAlphabet<O> extends AbstractList<O> implements SPMMOutputAlphabet<O> {


    private final Alphabet<O> internalSymbolOutput;
    private final Alphabet<O> procedureStartOutput;
    private final Alphabet<O> procedureEndOutput;
    private final Alphabet<O> errorOutput;
    private final Alphabet<O> postReturnOutput;

    public DefaultSPMMOutputAlphabet(
            Alphabet<O> internalSymbolOutput,
            O procedureStartSymbol,
            O procedureEndSymbol,
            O errorSymbol,
            O procedureLeftSymbol) {
        this.internalSymbolOutput = internalSymbolOutput;
        this.procedureStartOutput = Alphabets.singleton(procedureStartSymbol);
        this.procedureEndOutput = Alphabets.singleton(procedureEndSymbol);
        this.errorOutput = Alphabets.singleton(errorSymbol);
        this.postReturnOutput = Alphabets.singleton(procedureLeftSymbol);
    }

    @Override
    @Nonnull
    public O getPostReturn() {
        return this.postReturnOutput.getSymbol(0);
    }

    @Override
    public boolean isPostReturn(@Nonnull O symbol) {
        return this.postReturnOutput.containsSymbol(symbol);
    }

    @Override
    @Nonnull
    public O getError() {
        return this.errorOutput.getSymbol(0);
    }

    @Override
    public boolean isErrorSymbol(@Nonnull O symbol) {
        return this.errorOutput.containsSymbol(symbol);
    }

    @Override
    @Nonnull
    public O getProcedureStart() {
        return this.procedureStartOutput.getSymbol(0);
    }

    @Override
    public boolean isProcedureStartSymbol(@Nonnull O symbol) {
        return this.procedureStartOutput.containsSymbol(symbol);
    }

    @Override
    @Nonnull
    public O getProcedureEnd() {
        return this.procedureEndOutput.getSymbol(0);
    }

    @Override
    public boolean isProcedureEndSymbol(@Nonnull O symbol) {
        return this.procedureEndOutput.containsSymbol(symbol);
    }

    @Override
    @Nonnull
    public Alphabet<O> getInternalOutputAlphabet() {
        return internalSymbolOutput;
    }

    @Nullable
    @Override
    public O getSymbol(int index) throws IllegalArgumentException {
        int localIndex = index;

        if (localIndex < internalSymbolOutput.size()) {
            return internalSymbolOutput.getSymbol(localIndex);
        } else {
            localIndex -= internalSymbolOutput.size();
        }

        if (localIndex < procedureStartOutput.size()) {
            return procedureStartOutput.getSymbol(localIndex);
        } else {
            localIndex -= procedureStartOutput.size();
        }

        if (localIndex < procedureEndOutput.size()) {
            return procedureEndOutput.getSymbol(localIndex);
        } else {
            localIndex -= procedureEndOutput.size();
        }

        if (localIndex < errorOutput.size()) {
            return errorOutput.getSymbol(localIndex);
        } else {
            localIndex -= errorOutput.size();
        }


        if (localIndex < postReturnOutput.size()) {
            return postReturnOutput.getSymbol(localIndex);
        } else {
            throw new IllegalArgumentException("Index not within its expected bounds");
        }
    }

    @Override
    public int getSymbolIndex(@Nullable O symbol) throws IllegalArgumentException {
        if (symbol == null) {
            throw new IllegalArgumentException("Alphabet does not contain the queried symbol");
        }
        int offset = 0;

        if (internalSymbolOutput.containsSymbol(symbol)) {
            return internalSymbolOutput.getSymbolIndex(symbol);
        } else {
            offset += internalSymbolOutput.size();
        }

        if (procedureStartOutput.containsSymbol(symbol)) {
            return offset + procedureStartOutput.getSymbolIndex(symbol);
        } else {
            offset += procedureStartOutput.size();
        }

        if (procedureEndOutput.containsSymbol(symbol)) {
            return offset + procedureEndOutput.getSymbolIndex(symbol);
        } else {
            offset += procedureEndOutput.size();

        }

        if (errorOutput.containsSymbol(symbol)) {
            return offset + errorOutput.getSymbolIndex(symbol);
        } else {
            offset += errorOutput.size();

        }

        if (postReturnOutput.containsSymbol(symbol)) {
            return offset + postReturnOutput.getSymbolIndex(symbol);
        } else {
            throw new IllegalArgumentException("Alphabet does not contain the queried symbol");
        }
    }


    @Override
    @Nonnull
    public O get(int index) {
        return getSymbol(index);
    }


    @Override
    public int size() {
        return internalSymbolOutput.size() + procedureStartOutput.size() +
                procedureEndOutput.size() + errorOutput.size() + postReturnOutput.size();
    }


}
