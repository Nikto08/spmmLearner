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

import javax.annotation.Nonnull;

/**
 * A specialized output alphabet for systems of procedural mealy-machines.
 *
 * @param <O> output symbol type
 */
public interface SPMMOutputAlphabet<O> extends Alphabet<O> {

    @Nonnull
    O getPostReturn();

    boolean isPostReturn(@Nonnull O symbol);

    @Nonnull
    O getError();

    boolean isErrorSymbol(@Nonnull O symbol);

    @Nonnull
    O getProcedureStart();

    boolean isProcedureStartSymbol(@Nonnull O symbol);

    @Nonnull
    O getProcedureEnd();

    boolean isProcedureEndSymbol(@Nonnull O symbol);

    @Nonnull
    Alphabet<O> getInternalOutputAlphabet();

}
