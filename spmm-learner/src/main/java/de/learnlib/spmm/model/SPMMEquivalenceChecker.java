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

import de.learnlib.spmm.model.alphabet.SPMMInputAlphabet;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.automata.Automata;

import javax.annotation.Nullable;

public class SPMMEquivalenceChecker {


    public static <S, I, J, O>
    boolean haveIsomorphProceduralMap(@Nullable SPMM<?, I, ?, O> first, @Nullable SPMM<?, I, ?, O> second) {
        if (first == null) {
            return second == null;
        }
        if (second == null) {
            return false;
        }

        if (first.getProcedures().isEmpty()) {
            return second.getProcedures().isEmpty();
        }

        if(inputAlphabetsEqual(first.getInputAlphabet(), second.getInputAlphabet())
        && first.getActivatedCalls().containsAll(second.getActivatedCalls()) &&
        second.getActivatedCalls().containsAll(first.getActivatedCalls())
        ) {

            for (final I procedure : first.getInputAlphabet().getCallAlphabet()) {
                final MealyMachine<?, I, ?, O> expectedProcedure = first.getProcedures().get(procedure);
                final MealyMachine<?, I, ?, O> actualProcedure = second.getProcedures().get(procedure);

                if (!Automata.testEquivalence(expectedProcedure, actualProcedure, first.getInputAlphabet())) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    public static <I> boolean inputAlphabetsEqual(SPMMInputAlphabet<I> first, SPMMInputAlphabet<I> second) {
        if (first == null) {
            return second == null;
        }
        if (second == null) {
            return false;
        }

        if (!first.getCallAlphabet().containsAll(
                second.getCallAlphabet()) || !second.getCallAlphabet().containsAll(
                first.getCallAlphabet())
        ) {
            return false;
        }
        if (!first.getInternalAlphabet().containsAll(
                second.getInternalAlphabet())
                || !second.getInternalAlphabet().containsAll(
                first.getInternalAlphabet())
        ) {
            return false;
        }
        if (!first.getReturnAlphabet().containsAll(
                second.getReturnAlphabet())
                || !second.getReturnAlphabet().containsAll(
                first.getReturnAlphabet())
        ) {
            return false;
        }
        return true;
    }


}
