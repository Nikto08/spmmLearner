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
package de.learnlib.spmm.model.componenets;

import net.automatalib.automata.transout.impl.MealyTransition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InitialProcedure<S, I, O> {

    public final State<I, S> globalSink = new State<I, S>(null, null) {

        @Override
        public String toString() {
            return "initial sink";
        }
    };

    public final State<I, S> initialState = new State<I, S>(null, null) {

        @Override
        public String toString() {
            return "init";
        }
    };

    public final State<I, S> terminatingState = new State<I, S>(null, null) {

        @Override
        public String toString() {
            return "accept";
        }
    };


    public final I callSymbol;
    /**
     * global Error transition stays for "called procedure not exists"
     * or "input not started with initial call"
     * or read something after return from initial call"
     */
    public final MealyTransition<State<I, S>, O> globalErrorTransition;


    public InitialProcedure(@Nullable I initialCall, @Nonnull O errorSymbol) {
        this.callSymbol = initialCall;
        this.globalErrorTransition = new MealyTransition<>(globalSink, errorSymbol);
    }


    public boolean containsState(@Nullable State<I, S> state) {
        return (initialState == state ||
                terminatingState == state ||
                globalSink == state);
    }

}
