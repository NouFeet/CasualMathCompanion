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
import com.nikitakuprins.mathCompanion.datamodel.DataSource;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;

public class Controller {

    @FXML
    private BorderPane borderPane;

    @FXML
    private Label digitsAmount;
    @FXML
    private Label symbolsAmount;

    @FXML
    private ListView<DataItem> listView;

    @FXML
    private ToggleButton filterPlusButton;
    @FXML
    private ToggleButton filterMinusButton;
    @FXML
    private ToggleButton filterMultiplyButton;
    @FXML
    private ToggleButton filterDivideButton;

    private final Predicate<DataItem> wantAllItems = (item -> true);

    private final Map<ToggleButton, Predicate<DataItem>> buttonToFilter = new HashMap<>();
    private FilteredList<DataItem> filteredList;

    @FXML
    private Label resultArea;

    private BigDecimal number;
    private BigDecimal temp;
    private final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private String operation = "";
    private boolean isTypingNum = true;

    @FXML
    protected void initialize() {

        buttonToFilter.put(filterPlusButton, createFilterBySign('+'));
        buttonToFilter.put(filterMinusButton, createFilterBySign('-'));
        buttonToFilter.put(filterMultiplyButton, createFilterBySign('*'));
        buttonToFilter.put(filterDivideButton, createFilterBySign('/'));


        listView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldItem, newItem) -> {
            if (newItem != null) {
                DataItem item = listView.getSelectionModel().getSelectedItem();
                String str = item.getExpression();
                digitsAmount.setText(Calculations.countOfDigits(str) + "");
                symbolsAmount.setText(Calculations.countOfSigns(str) + "");
            }
        });

        filteredList = new FilteredList<>(DataSource.getInstance().getDataItems(), wantAllItems);

        listView.setItems(filteredList);
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.getSelectionModel().selectFirst();

    }

    //Calculator ->

    @FXML
    private void handleCalculatorButton(ActionEvent evt) {
        String buttonTitle = ((Button) evt.getSource()).getText();
        String oldResult = resultArea.getText();
        StringBuilder resultBuilder = new StringBuilder(oldResult);

        if (buttonTitle.matches("[0-9]")) {
            if (oldResult.matches("[+\\-x÷]|-*0")) {
                resultBuilder = new StringBuilder();
            }
            resultBuilder.append(buttonTitle);
            isTypingNum = true;
        } else if (buttonTitle.matches("[+\\-x÷]") && (operation.isEmpty() || !isTypingNum)) {
            operation = buttonTitle;
            resultBuilder = new StringBuilder(buttonTitle);
            isTypingNum = false;
        } else if (buttonTitle.equals("AC")) {
            resultBuilder = new StringBuilder("0");
            number = BigDecimal.ZERO;
            temp = BigDecimal.ZERO;
            isTypingNum = true;
            operation = "";

        } else if (isTypingNum) {
            if (buttonTitle.equals("%")) {
                BigDecimal result = Calculations.calculate(number, ONE_HUNDRED, "÷");
                resultBuilder = new StringBuilder(result + "");
            } else if (buttonTitle.equals("+/-")) {
                if (resultBuilder.indexOf("-") != -1) {
                    resultBuilder.deleteCharAt(0);
                } else {
                    resultBuilder.insert(0, "-");
                }
            } else if (buttonTitle.equals(".") && !oldResult.contains(".")) {
                resultBuilder.append(buttonTitle);
            } else if (!operation.isEmpty()) {
                BigDecimal result = Calculations.calculate(number, temp, operation);
                resultBuilder = new StringBuilder(result + "");
                operation = "";
            }
        }

        String newResult = resultBuilder.toString();

        if (newResult.length() <= 13) {
            resultArea.setText(newResult);
            if (operation.isEmpty()) {
                number = new BigDecimal(newResult);
            } else if (isTypingNum) {
                temp = new BigDecimal(newResult);
            }
        } else {
            resultArea.setText("Out of bounds");
        }
    }

    //Expressions list ->

    private Predicate<DataItem> createFilterBySign(Character sign) {
        return (item -> {
            Set<Character> signs = Calculations.getExpressionSigns(item.getExpression());
            return signs.stream().anyMatch(character -> character == sign);
        });
    }

    @FXML
    private void handleFilterButton() {
        DataItem selectedItem = listView.getSelectionModel().getSelectedItem();
        Predicate<DataItem> predicate = getFilterPredicate();

        filteredList.setPredicate(predicate);

        if (filteredList.contains(selectedItem)) {
            listView.getSelectionModel().select(selectedItem);
        } else {
            listView.getSelectionModel().selectFirst();
        }
    }

    private Predicate<DataItem> getFilterPredicate() {
        List<Predicate<DataItem>> excludePredicatesList = new ArrayList<>();

        for (ToggleButton toggleButton : buttonToFilter.keySet()) {
            if (!toggleButton.isSelected()) {
                excludePredicatesList.add(buttonToFilter.get(toggleButton));
            }
        }

        // If we selected all filters or deselected every filter, then show all expressions.
        if (excludePredicatesList.size() == 4 || excludePredicatesList.size() == 0) {
            return wantAllItems;
        }

        return Predicate.not(excludePredicatesList.stream().reduce(Predicate::or).orElse(wantAllItems));
    }

    private enum ProcessOption {
        NEW, EDIT
    }

    @FXML
    private void newItemDialog() {
        createAndShowDialog(ProcessOption.NEW);
    }

    @FXML
    private void editItemDialog() {
        createAndShowDialog(ProcessOption.EDIT);
    }

    private void createAndShowDialog(ProcessOption processOption) {
        // Creating Dialog
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("itemDialog.fxml"));
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(borderPane.getScene().getWindow());
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }

        DialogController controller = fxmlLoader.getController();
        DataItem selectedItem = listView.getSelectionModel().getSelectedItem();

        // Designing Dialog
        if(processOption.equals(ProcessOption.EDIT)) {
            dialog.setTitle("Editing Item");
            String selectedExpression = selectedItem.toString();
            controller.setExpressionField(selectedExpression.substring(0, selectedExpression.indexOf("=") - 1));
        } else if (processOption.equals(ProcessOption.NEW)) {
            dialog.setTitle("Creating Item");
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        // Processing result of the dialog
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
            DataItem newItem = controller.createItem();
            processItem(newItem, selectedItem, processOption);
        }
    }

    private void processItem(DataItem newItem, DataItem selectedItem, ProcessOption processOption) {
        if (newItem == null) {
            createErrorAlert();
        }

        if (processOption.equals(ProcessOption.NEW)) {
            DataSource.getInstance().addItem(newItem);
        } else if (processOption.equals(ProcessOption.EDIT)) {
            DataSource.getInstance().editItem(selectedItem, newItem);
        }
        listView.getSelectionModel().selectFirst();
    }

    private void createErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(borderPane.getScene().getWindow());
        alert.setHeaderText("Invalid format of data");
        alert.setContentText("Possible mistakes:\n" +
                "1. The text is empty\n" +
                "2. The text contains invalid symbols like: 'A','$','!'\n" +
                "3. The text contains has DECIMAL numbers but it should be INTEGERS.\n" +
                "4. The text does not have enough spaces or they are redundant");
        alert.show();
    }

    @FXML
    private void deleteItem() {
        DataItem item = listView.getSelectionModel().getSelectedItem();
        if (item != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(borderPane.getScene().getWindow());
            alert.setTitle("Delete Todo Item");
            alert.setHeaderText("Delete item: " + item.getExpression());
            alert.setContentText("Are you sure?  Press OK to confirm, or cancel to Back out.");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && (result.get() == ButtonType.OK)) {
                DataSource.getInstance().removeItem(item);
            }
        }
    }

    @FXML
    private void handleKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.DELETE) ||
                keyEvent.getCode().equals(KeyCode.BACK_SPACE)) {
            deleteItem();
        }
    }

    @FXML
    private void handleExit() {
        Platform.exit();
        System.exit(0);
    }

}
