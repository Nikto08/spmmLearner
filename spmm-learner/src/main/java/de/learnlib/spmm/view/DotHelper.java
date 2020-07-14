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

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.serialization.dot.DefaultDOTVisualizationHelper;
import net.automatalib.words.Alphabet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper-class to aggregate some render definitions for SPMM.
 *
 * @param <S> hypotheses state type
 * @param <I> input symbol type
 * @param <O> output symbol type
 */
class DotHelper<S, I, O> extends DefaultDOTVisualizationHelper<Pair<I, S>, Pair<Pair<I, S>, Pair<I, O>>> {

    private final Map<I, ? extends MealyMachine<S, I, ?, O>> subModels;
    private final Alphabet<I> callAlphabet;
    private final Alphabet<I> returnAlphabet;
    private final I initialCallSymbol;
    private final O closeProcedureSymbol;

    DotHelper(@Nonnull Map<I, ? extends MealyMachine<S, I, ?, O>> subModels,
              @Nonnull Alphabet<I> callAlphabet,
              @Nonnull Alphabet<I> returnAlphabet,
              @Nullable I initialCallSymbol,
              @Nonnull O closeProcedureSymbol) {
        this.subModels = subModels;
        this.callAlphabet = callAlphabet;
        this.returnAlphabet = returnAlphabet;
        this.initialCallSymbol = initialCallSymbol;
        this.closeProcedureSymbol = closeProcedureSymbol;
    }

    @Override
    protected Collection<Pair<I, S>> initialNodes() {
        return subModels.entrySet()
                .stream()
                .map(e -> new Pair<>(e.getKey(), e.getValue().getInitialState()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean getNodeProperties(final Pair<I, S> node, final Map<String, String> properties) {

        properties.put(NodeAttrs.SHAPE, NodeShapes.CIRCLE);
        properties.put(NodeAttrs.LABEL, node.getFirst() + "_" + node.getSecond());

        final I identifier = node.getFirst();
        final MealyMachine<S, I, ?, O> currentMM = subModels.get(identifier);
        if (node.getSecond().equals(currentMM.getInitialState())) {

            if (initialCallSymbol != null && initialCallSymbol.equals(identifier)) {
                properties.put(NodeAttrs.INITIAL, "true");
                properties.put(NodeAttrs.SHAPE, NodeShapes.DOUBLECIRCLE);
            } else {
                properties.put(NodeAttrs.INITIAL, "false");
                properties.put(NodeAttrs.STYLE, CommonStyles.BOLD);
            }

        }

        return super.getNodeProperties(node, properties);
    }

    @Override
    public boolean getEdgeProperties(final Pair<I, S> src,
                                     final Pair<Pair<I, S>, Pair<I, O>> edge,
                                     final Pair<I, S> tgt,
                                     final Map<String, String> properties) {

        super.getEdgeProperties(src, edge, tgt, properties);

        properties.put(EdgeAttrs.LABEL, edge.getSecond().getFirst().toString()
                + ":" + edge.getSecond().getSecond().toString());

        if (callAlphabet.containsSymbol(edge.getSecond().getFirst())) {
            properties.put(EdgeAttrs.STYLE, "dashed");
        }

        if (closeProcedureSymbol.equals(edge.getSecond().getSecond())) {
            properties.put(EdgeAttrs.STYLE, "bold");
        }

        return true;
    }

    @Override
    public void writePreamble(Appendable a) throws IOException {
        a.append("rankdir=LR;\n");
    }
}
