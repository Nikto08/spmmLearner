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

import com.google.common.collect.Sets;
import de.learnlib.api.oracle.QueryAnswerer;
import de.learnlib.spmm.model.SPMM;
import de.learnlib.spmm.model.alphabet.DefaultSPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.model.componenets.InitialProcedure;
import de.learnlib.spmm.model.componenets.State;
import de.learnlib.spmm.view.ReachabilityView;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.MealyTransition;
import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.Graph;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.ts.transout.DeterministicTransitionOutputTS;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;


/**
 * An implementation of SPMM. This implementation focuses on the model of initial MM
 * to automatically learn the language of the system under learning WITHOUT empty word.
 */
public class DefaultSPMM<S, I, O> implements
        SPMM<S, I, MealyTransition<State<I, S>, O>, O>,
        DeterministicTransitionOutputTS<State<I, S>, I, MealyTransition<State<I, S>, O>, O>,
        SimpleDTS<State<I, S>, I>,
        QueryAnswerer<I, Word<O>> {


    private final InitialProcedure<S, I, O> initialProcedure;
    private SPMMInputAlphabet<I> inputAlphabet;
    private Set<I> activatedCalls;
    private final SPMMOutputAlphabet<O> outputAlphabet;
    private Map<I, ? extends MealyMachine<S, I, ?, O>> procedures;

    // use DefaultSPMMBuilder
    DefaultSPMM(@Nonnull SPMMInputAlphabet<I> inputAlphabet,
                @Nonnull SPMMOutputAlphabet<O> outputAlphabet,
                @Nullable I initialCall,
                @Nonnull Map<I, ? extends MealyMachine<S, I, ?, O>> procedures
    ) {
        this.inputAlphabet = inputAlphabet;
        this.activatedCalls = Sets.newHashSetWithExpectedSize(inputAlphabet.getNumCalls());
        this.outputAlphabet = outputAlphabet;
        this.initialProcedure = new InitialProcedure<>(initialCall, outputAlphabet.getError());
        this.procedures = procedures;
    }

    /**
     * State<I,S> has in S info about call Symbol, that identifies procedure of state S
     * last output may be null, if we are at initial state
     */
    @Nullable
    @Override
    public MealyTransition<State<I, S>, O> getTransition(@Nonnull State<I, S> currentState, @Nullable I input) {
        if (input == null) {
            return null;
        }
        if (!this.isValidSPMM()) {
            return initialProcedure.globalErrorTransition;

        } else if (initialProcedure.containsState(currentState)) {
            if (initialProcedure.initialState.equals(currentState)) {
                if (initialProcedure.callSymbol.equals(input)) {
                    final MealyMachine<S, I, ?, O> calledMM = this.procedures.get(input);

                    final S nextState = calledMM.getInitialState();
                    final Stack<State<I, S>> stack = new Stack<>();
                    stack.add(initialProcedure.initialState);

                    return new MealyTransition<>(
                            new State<>(initialProcedure.callSymbol, nextState, stack),
                            outputAlphabet.getProcedureStart());
                }
            }
            return initialProcedure.globalErrorTransition;


            // current state is NOT a state of initial procedure
        } else {
            if (inputAlphabet.isInternalSymbol(input)) {
                return getTransitionInsideProcedure(currentState, input);

            } else if (inputAlphabet.isCallSymbol(input)) {
                return getTransitionProcedureCall(currentState, input);

            } else if (inputAlphabet.isReturnSymbol(input)) {
                return getTransitionProcedureReturn(currentState, input);
            }
        }

        //return initialProcedure.globalErrorTransition;
        throw new IllegalStateException("could not find transition for given state and input");
    }


    @Nonnull
    public Word<O> answerQuery(@Nonnull Word<I> input) {
        return this.computeSuffixOutput(Word.epsilon(), input);
    }

    @Nonnull
    @Override
    /** output will only be provided for suffix
     */
    public Word<O> answerQuery(@Nonnull Word<I> prefix, @Nonnull Word<I> suffix) {
        return this.computeSuffixOutput(prefix, suffix);
    }

    @Override
    @Nonnull
    public Word<O> computeSuffixOutput(@Nonnull Iterable<? extends I> iterable, @Nonnull Iterable<? extends I> iterable1) {
        State<I, S> stateAfterReadingFirstPart = this.readInputFromInitialState(iterable, false).getSecond();
        return this.readInput(iterable1, stateAfterReadingFirstPart, true).getFirst();
    }

    @Override
    @Nonnull
    public Word<O> computeOutput(@Nonnull Iterable<? extends I> input) {
        return this.readInputFromInitialState(input, true).getFirst();
    }

    public boolean isValidSPMM() {
        return this.initialProcedure.callSymbol != null;
    }

    @Nonnull
    @Override
    public SPMMInputAlphabet<I> getInputAlphabet() {
        return inputAlphabet;
    }

    @Override
    public void addActivatedCall(@Nonnull I identifier) {
        if (inputAlphabet.getCallSymbols().contains(identifier)) {
            activatedCalls.add(identifier);
        } else {
            throw new IllegalStateException("all activated calls must be symbols " +
                    "from call alphabet of input alphabet");
        }
    }

    @Override
    @Nonnull
    public Set<I> getActivatedCalls() {
        return activatedCalls;
    }

    @Override
    @Nonnull
    public SPMMInputAlphabet<I> getInputAlphabetWithOnlyActivatedCalls() {
        return new DefaultSPMMInputAlphabet<>(
                inputAlphabet.getInternalAlphabet(),
                Alphabets.fromCollection(activatedCalls),
                inputAlphabet.getReturnSymbol());
    }

    @Nonnull
    @Override
    public SPMMOutputAlphabet<O> getOutputAlphabet() {
        return outputAlphabet;
    }

    @Override
    public Graph<?, ?> graphView() {

        return new ReachabilityView<>(initialProcedure.callSymbol,
                getInputAlphabetWithOnlyActivatedCalls(), outputAlphabet, procedures);
    }

    @Nullable
    @Override
    public State<I, S> getInitialState() {
        return initialProcedure.initialState;
    }

    @Override
    public Map<I, MealyMachine<S, I, ?, O>> getProcedures() {
        return Collections.unmodifiableMap(procedures);
    }

    @Nullable
    @Override
    public State<I, S> getSuccessor(@Nonnull State<I, S> currentState, @Nullable I i) {
        if (i == null || this.getTransition(currentState, i) == null) {
            return null;
        }
        return this.getTransition(currentState, i).getSuccessor();
    }

    @Nonnull
    @Override
    public State<I, S> getSuccessor(@Nonnull MealyTransition<State<I, S>, O> t) {
        return t.getSuccessor();
    }

    @Nonnull
    @Override
    public O getTransitionOutput(@Nonnull MealyTransition<State<I, S>, O> t) {
        return t.getOutput();
    }

    @Override
    @Nullable
    public I getInitialCall() {
        return initialProcedure.callSymbol;
    }


    ///////////////////////////////////////////////////////////////////////////////////////
    //////////////////////// next transition logic ////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////


    private MealyTransition<State<I, S>, O> getTransitionInsideProcedure(State<I, S> currentState, I input) {

        // get proper MM by known call symbol as identifier outputAlphabet.getInitError());
        final I identifier = currentState.getFirst();
        final MealyMachine<S, I, ?, O> currentMM = this.procedures.get(identifier);

        // in the model of current MM find proper next state
        final O output = currentMM.getOutput(currentState.getSecond(), input);
        final S nextState = currentMM.getSuccessor(currentState.getSecond(), input);

        // same stack because no procedure call
        return new MealyTransition<>(new State<>(identifier, nextState, currentState.getStack()),
                output);

    }


    private MealyTransition<State<I, S>, O> getTransitionProcedureCall(State<I, S> currentState, I input) {

        if (activatedCalls.contains(input)) {
            final I identifier = currentState.getFirst();
            final MealyMachine<S, I, ?, O> currentMM = this.procedures.get(identifier);
            final O checkOutput = currentMM.getOutput(currentState.getSecond(), input);

            if (outputAlphabet.getProcedureStart().equals(checkOutput)) {
                final MealyMachine<S, I, ?, O> calledMM = this.procedures.get(input);

                final S nextState = calledMM.getInitialState();

                return new MealyTransition<>(
                        new State<>(input, nextState, currentState.getStack(), currentState),
                        outputAlphabet.getProcedureStart());
            } else {
                return new MealyTransition<>(new State<>(identifier,
                        currentMM.getSuccessor(currentState.getSecond(), input), currentState.getStack()),
                        checkOutput);
            }
        } else {
            final MealyMachine<S, I, ?, O> calledMM = this.procedures.get(input);
            final S nextState = calledMM.getInitialState();

            return new MealyTransition<>(
                    new State<>(input, nextState, currentState.getStack(), currentState),
                    outputAlphabet.getProcedureStart());
        }
    }


    private MealyTransition<State<I, S>, O> getTransitionProcedureReturn(State<I, S> currentState, I input) {

        final I identifier = currentState.getFirst();
        final MealyMachine<S, I, ?, O> currentMM = this.procedures.get(identifier);
        final O checkOutput = currentMM.getOutput(currentState.getSecond(), input);

        if (activatedCalls.contains(identifier)) {
            if (outputAlphabet.getProcedureEnd().equals(checkOutput)) {

                final State<I, S> previousState = currentState.getStack().peek();

                if (previousState == initialProcedure.initialState) {
                    if (this.initialProcedure.callSymbol.equals(identifier)) {
                        return new MealyTransition<>(initialProcedure.terminatingState,
                                outputAlphabet.getProcedureEnd());
                    } else {
                        return initialProcedure.globalErrorTransition;
                    }
                } else {
                    final I previousIdentifier = previousState.getFirst();

                    final MealyMachine<S, I, ?, O> returnMM = this.procedures.get(previousIdentifier);
                    final S nextState = returnMM.getSuccessor(previousState.getSecond(), currentState.getFirst());

                    return new MealyTransition<>(new State<>(previousIdentifier, nextState, previousState.getStack()),
                            outputAlphabet.getProcedureEnd());
                }

            }
        }

        return new MealyTransition<>(new State<>(identifier,
                currentMM.getSuccessor(currentState.getSecond(), input), currentState.getStack()),
                checkOutput);

    }


    /////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////// reading input and producing output logic ///////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////

    private Pair<Word<O>, State<I, S>> readInputFromInitialState(Iterable<? extends I> input,
                                                                 boolean recordOutput) {
        return readInput(input, this.initialProcedure.initialState, recordOutput);
    }

    private Pair<Word<O>, State<I, S>> readInput(Iterable<? extends I> input,
                                                 State<I, S> fromState,
                                                 boolean recordOutput) {
        List<O> outputList = new ArrayList<>();
        State<I, S> currentState = fromState;

        for (I symbol : input) {
            MealyTransition<State<I, S>, O> transition = this.getTransition(currentState, symbol);
            if (recordOutput) {
                outputList.add(transition.getOutput());
            }
            currentState = transition.getSuccessor();
        }

        return new Pair<>(Word.fromList(outputList), currentState);
    }


}
