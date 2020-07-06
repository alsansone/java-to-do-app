package com.adamsansone.todolist;

import com.adamsansone.todolist.datamodel.TodoData;
import com.adamsansone.todolist.datamodel.TodoItem;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

public class Controller {

    @FXML
    private TextArea itemDetailsText;

    @FXML
    private ListView<TodoItem> todoListView;

    @FXML
    private Label deadlineLabel;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private ContextMenu listContextMenu;

    @FXML
    private Button deleteButton;

    @FXML
    private ToggleButton toggleBtn;

    private FilteredList<TodoItem> filteredList;

    public void initialize() {

        // initialize delete button
        deleteButton.setOnAction(actionEvent -> {
            TodoItem item = todoListView.getSelectionModel().getSelectedItem();
            deleteItem(item);
        });
        // end delete button implementation

        /* creates a context menu when an item is selected
        This particular menu allows the user to delete the
        item selected */
        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(actionEvent -> {
            TodoItem item = todoListView.getSelectionModel().getSelectedItem();
            deleteItem(item);
        });
        listContextMenu.getItems().addAll(deleteMenuItem);
        // end context menu implementation

        // shows to-do items on UI
        todoListView.getSelectionModel().selectedItemProperty().addListener((observableValue, todoItem, t1) -> {
            if (t1 != null) {
                TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                itemDetailsText.setText(item.getDetails());
                DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                deadlineLabel.setText(df.format(item.getDeadline()));
            }
        });
        // end show items UI

        /* initialize filtered list to show all items */
        filteredList = new FilteredList<>(TodoData.getInstance().getTodoItems(), todoItem -> true);

        /* sorts to-do items by due date using filtered list*/
        SortedList<TodoItem> sortedList = new SortedList<>(filteredList,
                Comparator.comparing(TodoItem::getDeadline));
        /* end sorted list implementation */

        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();

        todoListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> todoItemListView) {
                ListCell<TodoItem> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(TodoItem todoItem, boolean empty) {
                        super.updateItem(todoItem, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(todoItem.getDescription());
                            LocalDate dueDate = todoItem.getDeadline();
                            LocalDate today = LocalDate.now();
                            if (dueDate.isBefore(today.plusDays(1))) {
                                setTextFill(Color.RED);
                            } else if (dueDate.equals(today.plusDays(1))) {
                                setTextFill(Color.ORANGE);
                            }
                        }
                    }
                };
                cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                    if (isNowEmpty) {
                        cell.setContextMenu(null);
                    } else {
                        cell.setContextMenu(listContextMenu);
                    }
                });

                return cell;
            }
        });
    }

    /* initialize add item dialog and process that request */
    @FXML
    public void showAddItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add To-do Item");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("additemwindow.fxml"));

        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
        }

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
                DialogController controller = fxmlLoader.getController();
                if (controller.isValidInput()) {
                    TodoItem newItem = controller.processResults();
                    todoListView.getSelectionModel().select(newItem);
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Dialog Warning");
                    alert.setHeaderText("Invalid Input");
                    alert.setContentText("Input fields can not be blank");
                    alert.showAndWait();
                    showAddItemDialog();
                }
        }
    }

    // alerts user of deletion request and processes that request
    public void deleteItem(TodoItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Item");
        alert.setHeaderText("Delete item: " + item.getDescription());
        alert.setContentText("Are you sure?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            TodoData.getInstance().deleteTodoItem(item);
        }
    }

    @FXML
    public void handleKeyPressed(KeyEvent event) {
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();

        if (item != null) {
            if (event.getCode().equals(KeyCode.DELETE)) {
                deleteItem(item);
            }
        }
    }

    /* filters items due today when toggle button is pressed */
    @FXML
    public void handleToggleBtn() {
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if (toggleBtn.isSelected()) {
            filteredList.setPredicate(todoItem -> (todoItem.getDeadline().equals(LocalDate.now())));
            if (filteredList.isEmpty()) {
                itemDetailsText.setText("");
                deadlineLabel.setText("");
            } else if (filteredList.contains(selectedItem)) {
                todoListView.getSelectionModel().select(selectedItem);
            } else {
                todoListView.getSelectionModel().selectFirst();
            }
        } else {
            filteredList.setPredicate(todoItem -> true);
            todoListView.getSelectionModel().selectFirst();
        }
    }

    @FXML
    public void handleExit() {
        Platform.exit();
    }

//    @FXML
//    public void showEditItemDialog() {
//        Dialog<ButtonType> dialog = new Dialog<>();
//        dialog.initOwner(mainBorderPane.getScene().getWindow());
//        dialog.setTitle("Edit To-do Item");
//        FXMLLoader fxmlLoader = new FXMLLoader();
//        fxmlLoader.setLocation(getClass().getResource("additemwindow.fxml"));
//
//        try {
//            dialog.getDialogPane().setContent(fxmlLoader.load());
//        } catch (IOException e) {
//            System.out.println("Couldn't load the dialog");
//            e.printStackTrace();
//        }
//
//        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
//        TodoItem item = todoListView.getSelectionModel().getSelectedItem();
//        TextField desc = (TextField) dialog.getDialogPane().lookup("#description");
//        TextArea details = (TextArea) dialog.getDialogPane().lookup("#details");
//        DatePicker date = (DatePicker) dialog.getDialogPane().lookup("#deadlineDate");
//        desc.appendText(item.getDescription());
//        details.appendText(item.getDetails());
//        date.setValue(item.getDeadline());
//
//        Optional<ButtonType> result = dialog.showAndWait();
//        if (result.isPresent() && result.get() == ButtonType.OK) {
//            DialogController controller = fxmlLoader.getController();
//            if (controller.isValidInput()) {
//                TodoItem updatedItem = controller.processResults();
//                todoListView.getSelectionModel().select(updatedItem);
//            } else {
//                Alert alert = new Alert(Alert.AlertType.WARNING);
//                alert.setTitle("Dialog Warning");
//                alert.setHeaderText("Invalid Input");
//                alert.setContentText("Input fields can not be blank");
//                alert.showAndWait();
//                showAddItemDialog();
//            }
//        } else {
//            System.out.println("Cancel button pressed");
//        }
//    }
}
