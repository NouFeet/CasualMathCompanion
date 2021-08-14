/*
 * Copyright 2021 Nikita Kuprins
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
package com.nikitakuprins.mathCompanion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;


class CalculationsTest {

    @ParameterizedTest
    @CsvSource(value = {
            "272 + 250 = 522 :9",
            "272 + 250 - 100 = 422 :12",
            "47 * 95 + 9 * 56 * 46 + 95 - 87 * 74 - 75 / 83 = 21305.0964 :28"
    }, delimiter = ':')
    public void countOfDigits(String expressions, long result) {
        Assertions.assertEquals(result, Calculations.countOfDigits(expressions));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "272 + 250 = 522 :2",
            "272 + 250 - 100 = 422 :3",
            "47 * 95 + 9 * 56 * 46 + 95 - 87 * 74 - 75 / 83 = 21305.0964 :10"
    }, delimiter = ':')
    public void countOfSigns(String expressions, long result) {
        Assertions.assertEquals(result, Calculations.countOfSigns(expressions));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "272 + 250", "430 + 876", "-900 + 800", "200 - 25 * 100 / 25"
    })
    public void correctFormatTrue(String expression) {
        Assertions.assertTrue(Calculations.isValidFormat(expression));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Hello272A + 250", "430 + 87GD6 = 1306", "-900 + 800 = -10MIW0",
            "272+ 250", "430+876 1306", "900 + 800 =-100",
            "+ 250 522", "430+ 1306", "900 + 800D",
            "272+ 250  522", "430876 1306"
    })
    public void correctFormatFalse(String expression) {
        Assertions.assertFalse(Calculations.isValidFormat(expression));
    }


    @ParameterizedTest
    @CsvSource(value = {
            "80 / -2 / 2=-20.0000",
            "-81 / -52 - 34 - 3 - 46 * 13 / 67 - 8 - 20 / 42=-52.8439",
            "60 - 86 / 79 - 74 * 5 / 77 * 55 * 95 * 94 + 39=-2359976.0686",
            "47 * 17 - 99 * 20 * 31 - 86 / 89 - 5 + 16 + 94=-60476.9663",
            "2 + 2 / 7=2.2857",
            "2 / 7 + 2=2.2857",
            "2 + 4 - 5 + 10=11",
            "2 + 2=4"
    }, delimiter = '=')
    public void calculateWithString(String expressions, BigDecimal result) {
        Assertions.assertEquals(result, Calculations.calculate(expressions));
    }
}