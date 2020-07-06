package com.adamsansone.todolist;

import com.adamsansone.todolist.datamodel.TodoData;
import com.adamsansone.todolist.datamodel.TodoItem;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class DialogController {

    @FXML
    private TextField description;

    @FXML
    private TextArea details;

    @FXML
    private DatePicker deadlineDate;

    public TodoItem processResults() {
        String desc = description.getText().trim().toUpperCase();
        String detail = details.getText().trim();
        LocalDate date = deadlineDate.getValue();

        TodoItem newItem = new TodoItem(desc, detail, date);
        TodoData.getInstance().addTodoItem(newItem);
        return newItem;
    }

    public boolean isValidInput() {
        return !description.getText().isBlank() && !details.getText().isBlank() && deadlineDate.getValue() != null;
    }
}
