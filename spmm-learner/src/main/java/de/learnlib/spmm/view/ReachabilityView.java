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
package de.learnlib.spmm.view;

import com.google.common.collect.Maps;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.Graph;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.impl.Alphabets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Graph representation of procedures.
 *
 * @param <S> hypotheses state type
 * @param <I> input symbol type
 * @param <O> output symbol type
 */
public class ReachabilityView<S, I, O>
        implements Graph<Pair<I, S>, Pair<Pair<I, S>, Pair<I, O>>> {

    private final I initialCallSymbol;
    private final List<I> inputAlphabet;
    private final SPMMOutputAlphabet<O> outputAlphabet;
    private final Map<I, ? extends MealyMachine<S, I, ?, O>> subModels;

    private final SPMMInputAlphabet<I> proceduralAlphabet;
    private final Map<I, Map<S, Boolean>> displayableNodesMap;

    public ReachabilityView(@Nullable final I initialCallSymbol,
                            @Nonnull final SPMMInputAlphabet<I> inputAlphabetWithOnlyActivatedSymbols,
                            @Nonnull final SPMMOutputAlphabet outputAlphabet,
                            @Nonnull final Map<I, ? extends MealyMachine<S, I, ?, O>> subModels) {
        this.initialCallSymbol = initialCallSymbol;
        this.proceduralAlphabet = inputAlphabetWithOnlyActivatedSymbols;
        this.outputAlphabet = outputAlphabet;
        this.subModels = subModels;

        this.inputAlphabet = new ArrayList<>(
                (proceduralAlphabet.getNumInternals()
                        + proceduralAlphabet.getNumReturns() + subModels.size()));
        this.inputAlphabet.addAll(proceduralAlphabet.getInternalAlphabet());
        this.inputAlphabet.addAll(proceduralAlphabet.getReturnAlphabet());
        this.inputAlphabet.addAll(subModels.keySet());

        this.displayableNodesMap = Maps.newHashMapWithExpectedSize(proceduralAlphabet.getNumCalls());
        this.computeNodesToDisplay();
    }

    private void computeNodesToDisplay() {

        for (final Map.Entry<I, ? extends MealyMachine<S, I, ?, O>> e : subModels.entrySet()) {

            final I symbol = e.getKey();
            final MealyMachine<S, I, ?, O> subModel = e.getValue();

            final Map<S, Boolean> displayMap = Maps.newHashMapWithExpectedSize(this.proceduralAlphabet.size());

            for (final S s : subModel.getStates()) {
                displayMap.put(s, true);
            }

            this.displayableNodesMap.put(symbol, displayMap);
        }
    }

    @Override
    public Collection<Pair<I, S>> getNodes() {
        final List<Pair<I, S>> result = new LinkedList<>();

        for (final Map.Entry<I, ? extends MealyMachine<S, I, ?, O>> e : subModels.entrySet()) {
            final I procedure = e.getKey();
            final MealyMachine<S, I, ?, O> subModel = e.getValue();

            final Map<S, Boolean> localDisplayMap = this.displayableNodesMap.get(procedure);

            subModel.getStates()
                    .stream()
                    .filter(localDisplayMap::containsKey)
                    .filter(localDisplayMap::get)
                    .forEach(s -> result.add(new Pair<>(procedure, s)));
        }

        return result;
    }

    @Override
    public Collection<Pair<Pair<I, S>, Pair<I, O>>> getOutgoingEdges(Pair<I, S> node) {

        final I procedure = node.getFirst();
        final S state = node.getSecond();

        final MealyMachine<S, I, ?, O> subModel = this.subModels.get(procedure);
        final Map<S, Boolean> localDisplayMap = this.displayableNodesMap.get(procedure);

        final List<Pair<Pair<I, S>, Pair<I, O>>> result = new LinkedList<>();

        for (final I i : this.proceduralAlphabet) {
            final Boolean isDisplayed = localDisplayMap.get(subModel.getSuccessor(state, i));
            final O output = subModel.getOutput(state, i);

            if (isDisplayed != null && isDisplayed) {
                result.add(new Pair<>(node, new Pair<>(i, output)));
            }
        }

        return result;
    }

    @Override
    public Pair<I, S> getTarget(Pair<Pair<I, S>, Pair<I, O>> edge) {
        final Pair<I, S> state = edge.getFirst();

        final I input = edge.getSecond().getFirst();
        final I identifier = state.getFirst();

        final MealyMachine<S, I, ?, O> subModel = subModels.get(identifier);

        final S next = subModel.getSuccessor(state.getSecond(), input);
        return new Pair<>(state.getFirst(), next);
    }

    @Override
    public VisualizationHelper<Pair<I, S>, Pair<Pair<I, S>, Pair<I, O>>> getVisualizationHelper() {
        return new DotHelper<>(subModels,
                Alphabets.fromCollection(subModels.keySet()),
                proceduralAlphabet.getReturnAlphabet(),
                this.initialCallSymbol,
                outputAlphabet.getProcedureEnd());
    }
}
