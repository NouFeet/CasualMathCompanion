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

import com.nikitakuprins.mathCompanion.datamodel.DataItem;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

public class DialogController {

    @FXML
    private TextField expressionField;

    public TextField getExpressionField() {
        return expressionField;
    }

    public void setExpressionField(String str) {
        this.expressionField.setText(str);
    }

    public DataItem processItem() {
        String expression = expressionField.getText();
        if (Calculations.isValidFormat(expression)) {
            BigDecimal result = Calculations.calculate(expression);
            return new DataItem(expression + " = " + result);
        }
        return null;
    }
}
