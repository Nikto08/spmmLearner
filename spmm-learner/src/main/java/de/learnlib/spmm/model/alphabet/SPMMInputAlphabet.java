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

import net.automatalib.words.VPDAlphabet;

/**
 * A specialized input alphabet for systems of procedural mealy-machines. This is a specialized version of an {@link
 * VPDAlphabet} that limits the number of return symbols to one.
 *
 * @param <I> input symbol type
 */

public interface SPMMInputAlphabet<I> extends VPDAlphabet<I> {

    /**
     * Returns the single return symbol.
     *
     * @return the single return symbol
     */
    default I getReturnSymbol() {
        return getReturnSymbol(0);
    }

}
