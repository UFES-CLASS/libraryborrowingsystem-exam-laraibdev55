/**
 * @author      masjohncook
 * @version     0.0.1
 * @copyright   (C) Copyright 2026
 * @license     None
 * @maintainer  masjohncook
 * @email       mas.john.cook@gmail.com
 * @status      Development
 */
package com.fad.LibrarySystem.controller;

import com.fad.LibrarySystem.model.Book;
import com.fad.LibrarySystem.model.LibraryService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * JavaFX controller for the Books tab (BookTab.fxml).
 *
 * BookFXController connects the FXML layout to the application's data layer.
 * It is instantiated automatically by JavaFX when FXMLLoader loads BookTab.fxml,
 * then App.java calls setService() to inject the shared LibraryService.
 *
 * Responsibilities:
 *   - Bind each TableColumn to the correct Book field (initialize)
 *   - Load and refresh the book table (loadBooks)
 *   - Handle the Search button — filter by genre or show all (handleSearch)
 *   - Handle the Add Book button — show an input dialog, call the service (handleAddBook)
 *   - Handle the Delete Book button — remove the selected row (handleDeleteBook)
 *
 * Key JavaFX concepts used:
 *   - @FXML    : links a Java field to a component defined by fx:id in the FXML
 *   - initialize() : called automatically after the FXML is loaded; safe to configure
 *                    columns here but NOT to load data (service is null at this point)
 *   - setService() : called by App.java after initialize(); data loading goes here
 *   - FXCollections.observableArrayList : wraps a List so JavaFX can watch it and
 *                    redraw the table automatically when contents change
 *   - SimpleStringProperty : wraps a plain String value so a TableColumn can display it
 *   - Dialog / setResultConverter : shows a popup and converts the user's input into
 *                    a result object when they click a button
 *
 * MVC role: Controller (JavaFX)
 * FXML file: src/resources/com/fad/LibrarySystem/view/BookTab.fxml
 */
public class BookFXController {

    // ── FXML field bindings ───────────────────────────────────────────────────
    // Each @FXML field is automatically connected to the matching fx:id in
    // BookTab.fxml when JavaFX loads the file. The name here must exactly
    // match the fx:id value set in SceneBuilder — a mismatch causes a
    // NullPointerException at runtime.

    @FXML private TextField                  genreSearchField; // the search input box
    @FXML private TableView<Book>            bookTable;        // the main table
    @FXML private TableColumn<Book, String>  colBookId;        // "ID" column
    @FXML private TableColumn<Book, String>  colTitle;         // "Title" column
    @FXML private TableColumn<Book, String>  colAuthor;        // "Author" column
    @FXML private TableColumn<Book, String>  colGenre;         // "Genre" column
    @FXML private TableColumn<Book, String>  colStatus;        // "Status" column

    // ── Shared service ────────────────────────────────────────────────────────
    // LibraryService holds all the data (books, members, records) and talks
    // to the database. It is created once in App.java and shared across all
    // tab controllers so they all read/write the same data.

    private LibraryService service;

    // ── Service injection ─────────────────────────────────────────────────────
    // App.java calls this method after loading the FXML to hand over the
    // shared service. Data loading (loadBooks) happens here — NOT in
    // initialize() — because the service is still null when initialize() runs.

    /**
     * Injects the shared LibraryService and populates the table for the first time.
     * Called by App.java immediately after FXMLLoader loads this controller.
     *
     * @param service the application's single LibraryService instance
     */
    public void setService(LibraryService service) {
        this.service = service;
        loadBooks(); // populate the table as soon as the service is ready
    }

    // ── initialize() ─────────────────────────────────────────────────────────
    // JavaFX calls this automatically right after the FXML is loaded.
    // At this point the @FXML fields are wired up, so it is safe to configure
    // the table columns. The service is NOT available yet here, so only do
    // column setup — no data loading.
    //
    // setCellValueFactory tells each column how to extract its display value
    // from a Book object. SimpleStringProperty wraps a plain String so JavaFX
    // can observe it for live updates.

    /**
     * Configures the table columns.
     * Called automatically by JavaFX after the FXML is loaded — before setService().
     */
    @FXML
    public void initialize() {
        colBookId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBookId()));
        colTitle .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        colAuthor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAuthor()));
        colGenre .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGenre()));
        // Status has no direct getter, so we convert the boolean isAvailable() to a label
        colStatus.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().isAvailable() ? "Available" : "Borrowed"));
    }

    // ── loadBooks() ───────────────────────────────────────────────────────────
    // Refreshes the table by reading the current catalog from the service.
    // FXCollections.observableArrayList wraps a regular List so JavaFX knows
    // when the contents change and redraws the table automatically.
    // Called after any operation that changes the book list (add, delete, search).

    /**
     * Reloads the full book catalog into the table.
     * Call this after any add or delete to reflect the latest state.
     */
    private void loadBooks() {
        bookTable.setItems(FXCollections.observableArrayList(service.getCatalog()));
    }

    // ── handleSearch() ────────────────────────────────────────────────────────
    // Triggered by the Search button (onAction="#handleSearch" in BookTab.fxml).
    // Filters the table to books matching the typed genre.
    // If the field is empty, the full catalog is shown instead.

    /**
     * Filters the table by the genre entered in the search field.
     * Clears the filter and shows all books if the field is empty.
     */
    @FXML
    private void handleSearch() {
        String genre = genreSearchField.getText().trim();
        if (genre.isEmpty()) {
            loadBooks(); // no filter — show everything
        } else {
            bookTable.setItems(FXCollections.observableArrayList(service.searchByGenre(genre)));
        }
    }

    // ── handleAddBook() ───────────────────────────────────────────────────────
    // Triggered by the Add Book button (onAction="#handleAddBook" in BookTab.fxml).
    // Opens a Dialog — a small popup window — with four text fields.
    // When the user clicks Add, setResultConverter runs: it reads the fields,
    // validates them, and calls service.addBook() which saves to the DB.
    // If the returned Book is null (duplicate ID or empty fields), an alert is shown.
    // If successful, loadBooks() refreshes the table to show the new entry.

    /**
     * Opens an Add Book dialog, collects input, and saves the new book.
     * Shows a warning if the ID is a duplicate or required fields are empty.
     */
    @FXML
    private void handleAddBook() {
        // Create a blank dialog with a custom title
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("Add Book");

        // Define the buttons that appear at the bottom of the dialog
        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        // Build the form — four labelled text fields stacked in a VBox
        TextField tfId     = new TextField(); tfId.setPromptText("Book ID (e.g. B010)");
        TextField tfTitle  = new TextField(); tfTitle.setPromptText("Title");
        TextField tfAuthor = new TextField(); tfAuthor.setPromptText("Author");
        TextField tfGenre  = new TextField(); tfGenre.setPromptText("Genre");

        VBox box = new VBox(6,               // 6px gap between each child
            new Label("Book ID:"), tfId,
            new Label("Title:"),   tfTitle,
            new Label("Author:"),  tfAuthor,
            new Label("Genre:"),   tfGenre);
        box.setPadding(new Insets(12));      // breathing room around the form
        dialog.getDialogPane().setContent(box);

        // setResultConverter decides what the dialog returns when closed.
        // It runs when the user clicks any button — btn is whichever was clicked.
        // If Cancel was clicked, return null (do nothing).
        // If Add was clicked, validate and call service.addBook().
        dialog.setResultConverter(btn -> {
            if (btn != addBtn) return null;
            String id     = tfId.getText().trim();
            String title  = tfTitle.getText().trim();
            String author = tfAuthor.getText().trim();
            String genre  = tfGenre.getText().trim();
            if (id.isEmpty() || title.isEmpty() || author.isEmpty()) return null;
            return service.addBook(id, title, genre.isEmpty() ? "General" : genre, author);
        });

        // showAndWait blocks until the dialog is closed, then runs the lambda
        // with whatever setResultConverter returned. A null result means the
        // operation failed or was cancelled — show a warning in that case.
        dialog.showAndWait().ifPresent(book -> {
            if (book == null) showAlert("Could not add book. ID may be duplicate or required fields are empty.");
            else loadBooks();
        });
    }

    // ── handleDeleteBook() ────────────────────────────────────────────────────
    // Triggered by the Delete Book button (onAction="#handleDeleteBook" in BookTab.fxml).
    // Reads whichever row the user has clicked in the table.
    // service.removeBook() returns false if the book is currently borrowed,
    // in which case we show an alert instead of deleting.

    /**
     * Deletes the currently selected book.
     * Shows a warning if no row is selected or if the book is currently borrowed.
     */
    @FXML
    private void handleDeleteBook() {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a book from the table first.");
            return;
        }
        boolean ok = service.removeBook(selected.getBookId());
        if (ok) loadBooks();
        else showAlert("Cannot delete \"" + selected.getTitle() + "\" — it is currently borrowed.");
    }

    // ── showAlert() ───────────────────────────────────────────────────────────
    // Convenience method to display a warning popup with a single OK button.
    // Used by any handler that needs to report an error to the user.

    /**
     * Shows a warning alert dialog with the given message and an OK button.
     *
     * @param message the message to display
     */
    private void showAlert(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }
}
