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
package de.learnlib.spmm.model.defaultspmm;

import com.google.common.collect.Maps;
import de.learnlib.spmm.model.SPMM;
import de.learnlib.spmm.model.SPMMBuilder;
import de.learnlib.spmm.model.alphabet.DefaultSPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.model.componenets.State;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.MealyTransition;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class DefaultSPMMBuilder<S, I, O>
        implements SPMMBuilder<S, I, MealyTransition<State<I, S>, O>, O> {

    @Override
    @Nonnull
    public DefaultSPMM<S, I, O> createSPMM(@Nonnull SPMMInputAlphabet<I> inputAlphabet,
                                           @Nonnull SPMMOutputAlphabet<O> outputAlphabet,
                                           @Nullable I initialCall,
                                           @Nonnull Map<I, ? extends MealyMachine<S, I, ?, O>> procedures) {
        if (this.inputAlphabetCorrect(inputAlphabet)
                && this.outputAlphabetCorrect(outputAlphabet)
                && this.proceduresCorrect(procedures, inputAlphabet)
                && this.initialCallCorrect(inputAlphabet, initialCall)) {
            return new DefaultSPMM<>(inputAlphabet, outputAlphabet, initialCall, procedures);
        }
        throw new IllegalStateException("something went wrong by building DefaultSPMM, " +
                "check that your arguments are correct");
    }

    @Override
    @Nonnull
    public DefaultSPMM<S, I, O> createSPMM(@Nonnull SPMMInputAlphabet<I> inputAlphabet,
                                           @Nonnull Collection<I> activatedCalls,
                                           @Nonnull SPMMOutputAlphabet<O> outputAlphabet,
                                           @Nullable I initialCall,
                                           @Nonnull Map<I, MealyMachine<S, I, ?, O>> procedures) {
        DefaultSPMM<S, I, O> spmm = this.createSPMM(inputAlphabet, outputAlphabet, initialCall, procedures);
        for (I call : activatedCalls) {
            if (inputAlphabet.contains(call)) {
                spmm.addActivatedCall(call);
            } else {
                throw new IllegalStateException("all activated calls must be symbols " +
                        "from call alphabet of input alphabet");
            }
        }
        return spmm;
    }


    @Override
    @Nonnull
    public DefaultSPMM<S, I, O> createEmptySPMM(@Nonnull SPMMInputAlphabet<I> inputAlphabet,
                                                @Nonnull SPMMOutputAlphabet<O> outputAlphabet) {
        SPMMInputAlphabet<I> newInputAlphabet = new DefaultSPMMInputAlphabet<>(
                inputAlphabet.getInternalAlphabet(),
                Alphabets.fromList(new ArrayList<>()),
                inputAlphabet.getReturnSymbol()
        );
        return new DefaultSPMM<>(newInputAlphabet, outputAlphabet, null, Collections.emptyMap());
    }

    private boolean inputAlphabetCorrect(SPMMInputAlphabet<I> inputAlphabet) {
        for (I symbol : inputAlphabet) {
            if (symbol == null) {
                throw new IllegalArgumentException("input symbol may not be null");
            }
        }
        return true;
    }

    private boolean outputAlphabetCorrect(SPMMOutputAlphabet<O> outputAlphabet) {
        for (O symbol : outputAlphabet) {
            if (symbol == null) {
                throw new IllegalArgumentException("output symbol may not be null");
            }
        }
        return true;
    }


    private boolean proceduresCorrect(Map<I, ? extends MealyMachine<S, I, ?, O>> procedures,
                                      SPMMInputAlphabet<I> alphabet) {
        for (I callSymbol : procedures.keySet()) {
            if (callSymbol == null || !alphabet.isCallSymbol(callSymbol)) {
                throw new IllegalArgumentException("every identifier in the map must be a call symbol");
            }
        }
        for (MealyMachine<?, I, ?, O> mm : procedures.values()) {
            if (mm == null) {
                throw new IllegalArgumentException("a procedure may not be null");
            }
        }
        if (procedures.size() != alphabet.getNumCalls()) {
            throw new IllegalArgumentException("every call symbol must respond to one procedure");
        }
        return true;
    }

    private boolean initialCallCorrect(SPMMInputAlphabet<I> alphabet, I initialCall) {
        // we allow initial call being null, even if spmm contains procedures
        if (initialCall != null && !alphabet.getCallAlphabet().containsSymbol(initialCall)) {
            throw new IllegalArgumentException("initial call must be a call symbol from input alphabet or null");
        }
        return true;
    }

    @Override
    @Nonnull
    public DefaultSPMM<S, I, O> copySPMMAddProcedure(
            @Nonnull SPMM<S, I, MealyTransition<State<I, S>, O>, O> original,
            @Nonnull I callSymbol, @Nonnull MealyMachine<S, I, ?, O> procedure) {
        return createDublicateSPMMWithNewProcedure(original, callSymbol, procedure, false);
    }

    @Override
    @Nonnull
    public DefaultSPMM<S, I, O> copySPMMAddInitialCallProcedure(
            @Nonnull SPMM<S, I, MealyTransition<State<I, S>, O>, O> original,
            @Nonnull I callSymbol, @Nonnull MealyMachine<S, I, ?, O> procedure) {
        return createDublicateSPMMWithNewProcedure(original, callSymbol, procedure, true);

    }

    private DefaultSPMM<S, I, O> createDublicateSPMMWithNewProcedure(
            SPMM<S, I, MealyTransition<State<I, S>, O>, O> original,
            I callSymbol, MealyMachine<S, I, ?, O> procedure, boolean newProcedureIsInitialCall) {

        ArrayList<I> listOfAllCallSymbols = new ArrayList<>(original.getInputAlphabet().getCallAlphabet());
        listOfAllCallSymbols.add(callSymbol);
        Alphabet<I> newCallAlphabet = Alphabets.fromList(listOfAllCallSymbols);
        SPMMInputAlphabet<I> newInputAlphabet = new DefaultSPMMInputAlphabet<>(
                original.getInputAlphabet().getInternalAlphabet(),
                newCallAlphabet,
                original.getInputAlphabet().getReturnSymbol()
        );

        final Map<I, MealyMachine<S, I, ?, O>> newProcedures =
                Maps.newHashMapWithExpectedSize(original.getProcedures().size() + 1);

        newProcedures.putAll(original.getProcedures());
        newProcedures.put(callSymbol, procedure);

        I newInitialCall = original.getInitialCall();

        if (newProcedureIsInitialCall) {
            newInitialCall = callSymbol;
        }

        return this.createSPMM(newInputAlphabet,
                original.getOutputAlphabet(),
                newInitialCall,
                newProcedures);
    }

    @Override
    public SPMMInputAlphabet<I> filterCallAlphabet(@Nonnull Collection<I> filter,
                                                   @Nonnull SPMMInputAlphabet<I> inputAlphabet) {
        ArrayList<I> filteredCalls = new ArrayList<>();
        for (I call : inputAlphabet.getCallAlphabet()) {
            if (filter.contains(call)) {
                filteredCalls.add(call);
            }
        }
        return new DefaultSPMMInputAlphabet<>(
                inputAlphabet.getInternalAlphabet(),
                Alphabets.fromList(filteredCalls),
                inputAlphabet.getReturnSymbol()
        );

    }

}

