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
package de.learnlib.spmm.util;

import net.automatalib.words.Word;


/**
 * Utility class for doing operations on any words.
 */

public class WordUtils {

    public static <O> int getFirstIndexWhenWordsDiffer(Word<O> first, Word<O> second) {
        if (first == null) {
            if (second == null) {
                return -1;
            } else {
                return 0;
            }
        } else {

            // take smallest size to avoid IndexOutOfBoundException
            boolean sameSize = true;
            int commonSize = first.size();
            if (second.size() < commonSize) {
                commonSize = second.size();
                sameSize = false;
            }

            for (int index = 0; index < commonSize; index++) {
                if (first.getSymbol(index) == null) {
                    if (second.getSymbol(index) != null) {
                        return index;
                    }
                } else if (!first.getSymbol(index).equals(second.getSymbol(index))) {
                    return index;
                }
            }

            if (!sameSize) {
                return commonSize;
            }

            return -1;
        }
    }

    public static <O> boolean wordsAreEqual(Word<O> first, Word<O> second) {
        return getFirstIndexWhenWordsDiffer(first, second) == -1;
    }

    public static <O, I> boolean wordsHaveSameSize(Word<O> first, Word<I> second) {
        if (first == null) {
            if (second == null) {
                return true;
            } else {
                return false;
            }
        } else {
            if (second == null) {
                return false;
            } else {
                return first.size() == second.size();
            }
        }
    }

    public static <O> Word<O> addSymbol(Word<O> word, O toAdd) {
        if (word == null) {
            if (toAdd == null) {
                return null;
            } else {
                return Word.fromLetter(toAdd);
            }
        }
        if (toAdd == null) {
            return word;
        } else {
            return Word.fromWords(word, Word.fromLetter(toAdd));
        }
    }

}
