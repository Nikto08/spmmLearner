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
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.Alphabets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DefaultSPMMInputAlphabet<I> extends AbstractList<I> implements SPMMInputAlphabet<I>, VPDAlphabet<I> {

    private final Alphabet<I> internalAlphabet;
    private final Alphabet<I> callAlphabet;
    private final Alphabet<I> returnOuterAlphabet;

    public DefaultSPMMInputAlphabet(@Nonnull Alphabet<I> internalAlphabet,
                                    @Nonnull Alphabet<I> callAlphabet,
                                    @Nonnull I returnSymbol) {
        this.internalAlphabet = internalAlphabet;
        this.callAlphabet = callAlphabet;
        this.returnOuterAlphabet = Alphabets.singleton(returnSymbol);

        validateDisjointness(internalAlphabet, SymbolType.INTERNAL, callAlphabet, returnOuterAlphabet);
        validateDisjointness(callAlphabet, SymbolType.CALL, returnOuterAlphabet);
    }

    @SafeVarargs
    private static <I> void validateDisjointness(Collection<I> source,
                                                 SymbolType type,
                                                 Collection<I>... rest) {
        final Set<I> sourceAsSet = new HashSet<>(source);
        final int initialSize = sourceAsSet.size();

        for (Collection<I> c : rest) {
            sourceAsSet.removeAll(c);
        }

        if (sourceAsSet.size() < initialSize) {
            throw new IllegalArgumentException(
                    "The set of " + type + " symbols is not disjoint with the sets of other symbols.");
        }
    }

    @Override
    @Nonnull
    public Alphabet<I> getCallAlphabet() {
        return callAlphabet;
    }

    @Override
    public int size() {
        return internalAlphabet.size() + callAlphabet.size() + returnOuterAlphabet.size();
    }

    @Override
    @Nonnull
    public I getCallSymbol(int index) {
        return callAlphabet.getSymbol(index);
    }

    @Override
    @Nonnull
    public I get(int index) {
        return getSymbol(index);
    }

    @Override
    public int getCallSymbolIndex(@Nonnull I symbol) {
        return callAlphabet.getSymbolIndex(symbol);
    }

    @Override
    @Nonnull
    public I getSymbol(int index) {
        int localIndex = index;

        if (localIndex < internalAlphabet.size()) {
            return internalAlphabet.getSymbol(localIndex);
        } else {
            localIndex -= internalAlphabet.size();
        }

        if (localIndex < callAlphabet.size()) {
            return callAlphabet.getSymbol(localIndex);
        } else {
            localIndex -= callAlphabet.size();
        }

        if (localIndex < returnOuterAlphabet.size()) {
            return returnOuterAlphabet.getSymbol(localIndex);
        } else {
            throw new IllegalArgumentException("Index not within its expected bounds");
        }
    }

    @Override
    public int getNumCalls() {
        return callAlphabet.size();
    }

    @Override
    public int getSymbolIndex(@Nullable I symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("Alphabet does not contain the queried symbol");
        }
        int offset = 0;

        if (internalAlphabet.containsSymbol(symbol)) {
            return internalAlphabet.getSymbolIndex(symbol);
        } else {
            offset += internalAlphabet.size();
        }

        if (callAlphabet.containsSymbol(symbol)) {
            return offset + callAlphabet.getSymbolIndex(symbol);
        } else {
            offset += callAlphabet.size();
        }

        if (returnOuterAlphabet.containsSymbol(symbol)) {
            return offset + returnOuterAlphabet.getSymbolIndex(symbol);
        } else {
            throw new IllegalArgumentException("Alphabet does not contain the queried symbol");
        }
    }

    @Override
    @Nonnull
    public Alphabet<I> getInternalAlphabet() {
        return internalAlphabet;
    }

    @Override
    public boolean containsSymbol(@Nonnull I symbol) {
        return internalAlphabet.containsSymbol(symbol) || callAlphabet.containsSymbol(symbol) ||
                returnOuterAlphabet.containsSymbol(symbol);
    }

    @Override
    @Nonnull
    public I getInternalSymbol(int index) {

        if (index < internalAlphabet.size()) {
            return internalAlphabet.getSymbol(index);
        } else {
            throw new IllegalArgumentException("Index not within its expected bounds");
        }
    }

    @Override
    public int getInternalSymbolIndex(@Nonnull I symbol) {
        if (internalAlphabet.containsSymbol(symbol)) {
            return internalAlphabet.getSymbolIndex(symbol);
        } else {
            throw new IllegalArgumentException("Alphabet does not contain the queried symbol");
        }

    }

    @Override
    public int getNumInternals() {
        return internalAlphabet.size();
    }

    @Override
    @Nonnull
    public Alphabet<I> getReturnAlphabet() {
        return returnOuterAlphabet;
    }

    @Override
    @Nonnull
    public I getReturnSymbol(int index) {
        return returnOuterAlphabet.getSymbol(index);
    }

    @Override
    public int getReturnSymbolIndex(@Nonnull I symbol) {
        return returnOuterAlphabet.getSymbolIndex(symbol);
    }

    @Override
    public int getNumReturns() {
        return returnOuterAlphabet.size();
    }

    @Override
    @Nonnull
    public SymbolType getSymbolType(@Nonnull I symbol) {
        if (internalAlphabet.containsSymbol(symbol)) {
            return SymbolType.INTERNAL;
        } else if (callAlphabet.containsSymbol(symbol)) {
            return SymbolType.CALL;
        } else if (returnOuterAlphabet.containsSymbol(symbol)) {
            return SymbolType.RETURN;
        } else {
            throw new IllegalArgumentException("Symbol is not contained in this alphabet");
        }
    }

    @Override
    @Nonnull
    public Collection<I> getInternalSymbols() {
        return getInternalAlphabet();
    }

    @Override
    @Nonnull
    public Collection<I> getReturnSymbols() {
        return getReturnAlphabet();
    }

    @Override
    @Nonnull
    public Collection<I> getCallSymbols() {
        return getCallAlphabet();
    }

    @Override
    public boolean isCallSymbol(@Nonnull I symbol) {
        return this.callAlphabet.containsSymbol(symbol);
    }

    @Override
    public boolean isInternalSymbol(@Nonnull I symbol) {
        return this.internalAlphabet.containsSymbol(symbol);
    }

    @Override
    public boolean isReturnSymbol(@Nonnull I symbol) {
        return this.returnOuterAlphabet.containsSymbol(symbol);
    }
}
