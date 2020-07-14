/* Copyright (C) 2019 Markus Frohme.
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
package de.learnlib.spmm.efficiency_tests;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.model.defaultspmm.DefaultSPMM;
import de.learnlib.spmm.model.defaultspmm.DefaultSPMMBuilder;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.cover.Covers;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.util.graphs.ShortestPaths;
import net.automatalib.words.Word;
import net.automatalib.words.impl.SimpleAlphabet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Generator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Generator.class);

    public static <I, O> DefaultSPMM<Integer, I, O> create(Random random,
                                                           SPMMInputAlphabet<I> inputAlphabet,
                                                           SPMMOutputAlphabet<O> outputAlphabet,
                                                           int procedureSize) {

        final Map<I, MealyMachine<Integer, I, ?, O>> procedures =
                Maps.newHashMapWithExpectedSize(inputAlphabet.getNumCalls());

        final int numCalls = inputAlphabet.getNumCalls();
        final int numInternals = inputAlphabet.getNumInternals();

        final List<I> proceduralAlphabet = new ArrayList<>(numCalls + numInternals);
        proceduralAlphabet.addAll(inputAlphabet.getCallAlphabet());
        proceduralAlphabet.addAll(inputAlphabet.getInternalAlphabet());

        final I initialProcedure = inputAlphabet.getCallSymbol(random.nextInt(numCalls));
        final Set<I> discoveredCalls = Sets.newHashSetWithExpectedSize(numCalls);
        final Queue<I> queue = new ArrayDeque<>(numCalls);

        discoveredCalls.add(initialProcedure);
        queue.add(initialProcedure);

        int stateNumber = 1;

        while (!queue.isEmpty()) {
            final I p = queue.peek();

            LOGGER.debug("Generating procedure for '{}'", p);

            final CompactMealy<I, O> procedure =
                    generateProcedure(random, inputAlphabet, proceduralAlphabet, outputAlphabet, procedureSize - 2);

            if (!canTerminate(procedure, procedure.getInitialState(), inputAlphabet, outputAlphabet)) {
                // if we cannot terminate from the initial state, don't even bother further analyzing the system
                continue;
            }

            final Set<I> newReachables = findReachableProcedures(procedure, inputAlphabet, outputAlphabet);
            newReachables.removeAll(discoveredCalls);

            // continue exploring
            // * if we discover new procedures
            // * if previous procedures discovered more calls than necessary
            // * we reached the last procedure
            if (!newReachables.isEmpty() || discoveredCalls.size() > stateNumber || stateNumber == numCalls) {
                queue.addAll(newReachables);
                discoveredCalls.addAll(newReachables);

                procedures.put(p, procedure);

                // remove element to investigate next
                queue.poll();
                stateNumber++;
            }
            // otherwise repeat the loop and generate a difference Mealy machine
        }

        return new DefaultSPMMBuilder<Integer, I, O>().createSPMM(inputAlphabet,
                                                                  inputAlphabet.getCallAlphabet(),
                                                                  outputAlphabet,
                                                                  initialProcedure,
                                                                  procedures);
    }

    private static <I, O> CompactMealy<I, O> generateProcedure(Random random,
                                                               SPMMInputAlphabet<I> inputAlphabet,
                                                               Collection<I> proceduralAlphabet,
                                                               SPMMOutputAlphabet<O> outputAlphabet,
                                                               int procedureSize) {

        final CompactMealy<I, O> procedure = RandomAutomata.randomMealy(random,
                                                                        procedureSize,
                                                                        // use new alphabet instance so we don't accidentally add return symbol
                                                                        new SimpleAlphabet<>(proceduralAlphabet),
                                                                        outputAlphabet.getInternalOutputAlphabet());

        // add return symbol
        procedure.addAlphabetSymbol(inputAlphabet.getReturnSymbol());

        // cache original states
        final List<Integer> originalStates = new ArrayList<>(procedure.getStates());

        // add error- and left-sink
        final Integer sink = procedure.addState();
        final Integer left = procedure.addState();
        for (final I i : inputAlphabet) {
            procedure.addTransition(sink, i, sink, outputAlphabet.getError());
            procedure.addTransition(left, i, left, outputAlphabet.getPostReturn());
        }

        // update call outputs
        for (final Integer s : originalStates) {
            for (final I call : inputAlphabet.getCallAlphabet()) {
                final O output = random.nextBoolean() ? outputAlphabet.getProcedureStart() : outputAlphabet.getError();

                if (output.equals(outputAlphabet.getProcedureStart())) {
                    procedure.setTransition(s, call, s, output);
                } else { // error
                    procedure.setTransition(s, call, sink, output);
                }
            }
        }

        // add return transitions
        for (final Integer s : originalStates) {
            final O output = random.nextBoolean() ? outputAlphabet.getError() : outputAlphabet.getProcedureEnd();
            if (output.equals(outputAlphabet.getProcedureEnd())) {
                procedure.setTransition(s, inputAlphabet.getReturnSymbol(), left, output);
            } else {
                procedure.setTransition(s, inputAlphabet.getReturnSymbol(), sink, output);
            }
        }

        //        Visualization.visualize(procedure);
        return procedure;
    }

    public static <S, I, O> Set<I> findReachableProcedures(MealyMachine<S, I, ?, O> procedure,
                                                           SPMMInputAlphabet<I> inputAlphabet,
                                                           SPMMOutputAlphabet<O> outputAlphabet) {

        final StateIDs<S> ids = procedure.stateIDs();
        final boolean[] canTerminate = new boolean[procedure.size()];

        for (int i = 0; i < procedure.size(); i++) {
            canTerminate[i] = canTerminate(procedure, ids.getState(i), inputAlphabet, outputAlphabet);
        }

        final Set<I> reachableProcedures = Sets.newHashSetWithExpectedSize(inputAlphabet.getNumCalls());
        final Iterator<Word<I>> iter = Covers.transitionCoverIterator(procedure, inputAlphabet);

        while (iter.hasNext()) {
            Word<I> input = iter.next();
            if (canTerminate[ids.getStateId(procedure.getState(input))]) {
                for (final I i : input) {
                    if (inputAlphabet.isCallSymbol(i)) {
                        reachableProcedures.add(i);
                    }
                }
            }
        }

        return reachableProcedures;
    }

    public static <S, I, O> boolean canTerminate(MealyMachine<S, I, ?, O> procedure,
                                                 S initialState,
                                                 SPMMInputAlphabet<I> inputAlphabet,
                                                 SPMMOutputAlphabet<O> outputAlphabet) {

        final Predicate<S> successfulReturnCheck =
                (s) -> outputAlphabet.isProcedureEndSymbol(procedure.getOutput(s, inputAlphabet.getReturnSymbol()));

        return ShortestPaths.shortestPath(procedure.transitionGraphView(inputAlphabet),
                                          initialState,
                                          procedure.size(),
                                          successfulReturnCheck) != null;
    }
}