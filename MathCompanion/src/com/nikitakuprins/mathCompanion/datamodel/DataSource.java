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
package com.nikitakuprins.mathCompanion.datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;

public class DataSource {

    private static final DataSource instance = new DataSource();
    private static final String fileName = "/Users/nikitakuprin/IdeaProjects/MathCompanion/src/data.txt"; // Define path to data.txt

    private ObservableList<DataItem> dataItems;

    public static DataSource getInstance() {
        return instance;
    }

    public ObservableList<DataItem> getDataItems() {
        return dataItems;
    }

    public void addItem(DataItem item) {
        dataItems.add(item);
    }

    public void editItem(DataItem item, DataItem newItem) {
        dataItems.set(dataItems.indexOf(item), newItem);
    }

    public void removeItem(DataItem item) {
        dataItems.remove(item);
    }

    public void loadTodoItems() {
        dataItems = FXCollections.observableArrayList();
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String input;
            while ((input = br.readLine()) != null) {
                DataItem item = new DataItem(input);
                dataItems.add(item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void storeTodoItems() {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            for (DataItem item : dataItems) {
                bw.write(item.getExpression());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
