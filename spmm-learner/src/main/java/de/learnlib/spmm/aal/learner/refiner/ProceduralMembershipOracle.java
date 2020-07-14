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
package de.learnlib.spmm.aal.learner.refiner;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.Query;
import de.learnlib.spmm.aal.ATProvider.ATProvider;
import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import de.learnlib.spmm.model.alphabet.SPMMOutputAlphabet;
import de.learnlib.spmm.util.mapping.Expansion;
import de.learnlib.spmm.util.mapping.Projection;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Membership oracle for single procedure. Works by means of input expansion, delegating query answering to
 * SPMM Membership Oracle and projecting the result.
 *
 * @param <I> input symbol type
 * @param <O> output symbol type
 */
public class ProceduralMembershipOracle<I, O> implements MembershipOracle<I, Word<O>> {

    private final SPMMInputAlphabet<I> inputAlphabet;
    private final SPMMOutputAlphabet<O> outputAlphabet;
    private final MembershipOracle<I, Word<O>> delegate;
    private final I procedure;
    private final ATProvider<I, O> atProvider;

    public ProceduralMembershipOracle(SPMMInputAlphabet<I> inputAlphabet,
                                      SPMMOutputAlphabet<O> outputAlphabet,
                                      MembershipOracle<I, Word<O>> delegate,
                                      I procedure,
                                      ATProvider<I, O> atProvider) {
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = outputAlphabet;
        this.delegate = delegate;
        this.procedure = procedure;
        this.atProvider = atProvider;
    }

    @Override
    public void processQuery(@Nullable Query<I, Word<O>> query) {
        if (query != null) {
            this.delegate.processQuery(new ExpandedQuery(query));
        }
    }

    @Override
    public void processQueries(@Nullable Collection<? extends Query<I, Word<O>>> collection) {
        if (collection != null && !collection.isEmpty()) {
            final List<Query<I, Word<O>>> transformedQueries = new ArrayList<>(collection.size());

            for (final Query<I, Word<O>> q : collection) {
                transformedQueries.add(new ExpandedQuery(q));
            }

            this.delegate.processQueries(transformedQueries);
        }
    }

    private Pair<Word<I>, Query<I, Word<O>>> expandLocalInput(Query<I, Word<O>> localQuery) {

        Query<I, Word<O>> resultQuery = Expansion.buildGlobalInputForProcedure(
                inputAlphabet,
                procedure,
                localQuery,
                atProvider.getAccessSequenceProvider(),
                atProvider.getTerminatingSequenceProvider());

        return new Pair<>(atProvider.getAccessSequence(this.procedure), resultQuery);
    }

    private Word<O> projectGlobalOutput(
            Query<I, Word<O>> transformedQuery,
            Word<O> globalOutput,
            Query<I, Word<O>> originalQuery,
            Word<I> assessSequence) {

        DefaultQuery<I, Word<O>> projectedMonolithQuery = Projection.projectInputOutputPair(
                inputAlphabet,
                outputAlphabet,
                transformedQuery.getInput().subWord(assessSequence.length() + 1),
                globalOutput.subWord(assessSequence.length() + 1));

        DefaultQuery<I, Word<O>> projectedQuery = new DefaultQuery<>(
                projectedMonolithQuery.getInput().subWord(0, originalQuery.getPrefix().length()),
                projectedMonolithQuery.getInput().subWord(originalQuery.getPrefix().length()),
                projectedMonolithQuery.getOutput().subWord(originalQuery.getPrefix().length())
        );


        // test logic
        // System.out.println("Local query: " + originalQuery + " , local output: " + projectedQuery.getOutput());
        //System.out.println("Expanded query: " + transformedQuery + " , expanded output: "
        //        + globalOutput.subWord(globalOutput.length() - transformedQuery.getSuffix().length()));

        if (!projectedQuery.getInput().equals(originalQuery.getInput())) {
            throw new AssertionError("Expanded and then projected Local query q" +
                    " must be equal with q before this both operations. However now Local query" +
                    " before expansion and projection is " + originalQuery + " and the same query" +
                    " after expansion and projection is " + projectedQuery);
        }

        return projectedQuery.getOutput();
    }


    private class ExpandedQuery extends Query<I, Word<O>> {

        private final Query<I, Word<O>> originalQuery;
        private final Query<I, Word<O>> transformedQuery;
        private final Word<I> assessSequence;

        ExpandedQuery(Query<I, Word<O>> originalQuery) {
            this.originalQuery = originalQuery;
            Pair<Word<I>, Query<I, Word<O>>> temporal = expandLocalInput(originalQuery);
            this.transformedQuery = temporal.getSecond();
            this.assessSequence = temporal.getFirst();
        }


        @Override
        public void answer(@Nullable Word<O> globalOutput) {
            if (globalOutput != null) {
                originalQuery.answer(projectGlobalOutput(transformedQuery, globalOutput, originalQuery, assessSequence));
            }
        }


        @Override
        public Word<I> getPrefix() {
            return Word.epsilon();
        }

        @Override
        public Word<I> getSuffix() {
            return this.transformedQuery.getInput();
        }

        @Override
        public Word<I> getInput() {
            return this.transformedQuery.getInput();
        }
    }
}
