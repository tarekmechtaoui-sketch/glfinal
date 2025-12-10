package com.hotelmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Dashboard {

    private TabPane tabPane;
    private Tab hotelsTab;
    private Tab roomsTab;
    private Tab reservationsTab;
    private Tab customersTab;

    public void start(Stage stage) {
        stage.setTitle("Hotel Management - Dashboard");

        // --- Tabs ---
        tabPane = new TabPane();

        hotelsTab = new Tab("Hotels");
        hotelsTab.setContent(createHotelsTab());
        hotelsTab.setClosable(false);

        roomsTab = new Tab("Rooms");
        roomsTab.setContent(createRoomsTab());
        roomsTab.setClosable(false);

        reservationsTab = new Tab("Reservations");
        reservationsTab.setContent(createReservationsTab());
        reservationsTab.setClosable(false);

        customersTab = new Tab("Customers");
        customersTab.setContent(createCustomersTab());
        customersTab.setClosable(false);

        tabPane.getTabs().addAll(hotelsTab, roomsTab, reservationsTab, customersTab);

        // === ADD REFRESH BUTTON HERE ===
        Button refreshButton = new Button("↻ Refresh All");
        refreshButton.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        refreshButton.setOnAction(e -> refreshAllTabs());
        refreshButton.setTooltip(new Tooltip("Refresh all data in all tabs"));

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle(
                "-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand;");
        logoutButton.setOnAction(e -> {
            LoginPage loginPage = new LoginPage();
            loginPage.start(stage);
        });

        Label titleLabel = new Label("Hotel Management System");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        // ADDED: Spacer to push buttons to the right
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox();
        topBar.setSpacing(10);
        topBar.setStyle(
                "-fx-padding: 15; -fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 2 0;");

        // MODIFIED: Add refresh button between title and logout
        topBar.getChildren().addAll(titleLabel, spacer, refreshButton, logoutButton);

        tabPane.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(tabPane);
        root.setStyle("-fx-background-color: #fafafa;");

        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.show();
    }

    // ADDED: Simple refresh all tabs method
    private void refreshAllTabs() {
        try {
            // Store current tab selection
            int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();

            // Refresh each tab by recreating its content
            hotelsTab.setContent(createHotelsTab());
            roomsTab.setContent(createRoomsTab());
            reservationsTab.setContent(createReservationsTab());
            customersTab.setContent(createCustomersTab());

            // Restore tab selection
            tabPane.getSelectionModel().select(selectedIndex);

            showSuccess("All data refreshed successfully!");
        } catch (Exception e) {
            showError("Error refreshing data: " + e.getMessage());
        }
    }

    // =================== Hotels ===================
    private BorderPane createHotelsTab() {
        TableView<Hotel> table = new TableView<>();
        ObservableList<Hotel> data = getHotelsFromDB();
        ObservableList<Hotel> filteredData = FXCollections.observableArrayList(data);

        TableColumn<Hotel, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(80);

        TableColumn<Hotel, String> colName = new TableColumn<>("Hotel Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(200);

        TableColumn<Hotel, String> colDescription = new TableColumn<>("Description");
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDescription.setPrefWidth(250);

        TableColumn<Hotel, String> colAddress = new TableColumn<>("Address");
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colAddress.setPrefWidth(200);

        TableColumn<Hotel, Double> colRating = new TableColumn<>("Rating");
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
        colRating.setPrefWidth(80);

        table.setItems(filteredData);
        table.getColumns().addAll(colId, colName, colDescription, colAddress, colRating);
        table.setStyle("-fx-font-size: 13px;");

        Button addButton = new Button("+ Add Hotel");
        addButton.setStyle(
                "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        addButton.setOnAction(e -> handleAddHotel(table, data, filteredData));

        Button editButton = new Button("Edit Hotel");
        editButton.setStyle(
                "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        editButton.setOnAction(e -> handleEditHotel(table, data, filteredData));

        Button deleteButton = new Button("Delete Hotel");
        deleteButton.setStyle(
                "-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        deleteButton.setOnAction(e -> handleDeleteHotel(table, data, filteredData));

        TextField searchField = new TextField();
        searchField.setPromptText("Search by hotel name or address...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-padding: 10; -fx-font-size: 13px;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterHotels(newVal, data, filteredData);
        });

        HBox controls = new HBox(15, addButton, editButton, deleteButton, searchField);
        controls.setStyle("-fx-padding: 15; -fx-background-color: #f5f5f5;");

        BorderPane pane = new BorderPane();
        pane.setTop(controls);
        pane.setCenter(table);
        pane.setStyle("-fx-background-color: white;");

        return pane;
    }

    private void handleAddHotel(TableView<Hotel> table, ObservableList<Hotel> data,
            ObservableList<Hotel> filteredData) {
        Stage stage = (Stage) table.getScene().getWindow();
        HotelDialog dialog = new HotelDialog(stage, null);
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            try (Connection conn = Database.getConnection()) {
                String sql = "INSERT INTO hotels (name, description, address, rating) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, dialog.getHotelName());
                pstmt.setString(2, dialog.getDescription());
                pstmt.setString(3, dialog.getAddress());
                pstmt.setDouble(4, dialog.getRating());
                pstmt.executeUpdate();

                // Get the generated ID
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                int newId = -1;
                if (generatedKeys.next()) {
                    newId = generatedKeys.getInt(1);
                }

                Hotel newHotel = new Hotel(newId, dialog.getHotelName(), dialog.getDescription(),
                        dialog.getAddress(), dialog.getRating());
                data.add(newHotel);
                filteredData.add(newHotel);

                showSuccess("Hotel added successfully!");
            } catch (Exception e) {
                showError("Error adding hotel: " + e.getMessage());
            }
        }
    }

    private void handleEditHotel(TableView<Hotel> table, ObservableList<Hotel> data,
            ObservableList<Hotel> filteredData) {
        Hotel selectedHotel = table.getSelectionModel().getSelectedItem();
        if (selectedHotel == null) {
            showError("Please select a hotel to edit!");
            return;
        }

        Stage stage = (Stage) table.getScene().getWindow();
        HotelDialog dialog = new HotelDialog(stage, selectedHotel.getId());
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            try (Connection conn = Database.getConnection()) {
                String sql = "UPDATE hotels SET name = ?, description = ?, address = ?, rating = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, dialog.getHotelName());
                pstmt.setString(2, dialog.getDescription());
                pstmt.setString(3, dialog.getAddress());
                pstmt.setDouble(4, dialog.getRating());
                pstmt.setInt(5, selectedHotel.getId());
                pstmt.executeUpdate();

                data.remove(selectedHotel);
                filteredData.remove(selectedHotel);
                Hotel updatedHotel = new Hotel(selectedHotel.getId(), dialog.getHotelName(),
                        dialog.getDescription(), dialog.getAddress(),
                        dialog.getRating());
                data.add(updatedHotel);
                filteredData.add(updatedHotel);

                showSuccess("Hotel updated successfully!");
            } catch (Exception e) {
                showError("Error updating hotel: " + e.getMessage());
            }
        }
    }

    private void handleDeleteHotel(TableView<Hotel> table, ObservableList<Hotel> data,
            ObservableList<Hotel> filteredData) {
        Hotel selectedHotel = table.getSelectionModel().getSelectedItem();
        if (selectedHotel == null) {
            showError("Please select a hotel to delete!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Hotel: " + selectedHotel.getName());
        confirmAlert.setContentText("Are you sure you want to delete this hotel? This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = Database.getConnection()) {
                    String sql = "DELETE FROM hotels WHERE id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, selectedHotel.getId());
                    pstmt.executeUpdate();

                    data.remove(selectedHotel);
                    filteredData.remove(selectedHotel);

                    showSuccess("Hotel deleted successfully!");
                } catch (Exception e) {
                    showError("Error deleting hotel: " + e.getMessage());
                }
            }
        });
    }

    private void filterHotels(String searchText, ObservableList<Hotel> data, ObservableList<Hotel> filteredData) {
        filteredData.clear();
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredData.addAll(data);
        } else {
            String lowerSearch = searchText.toLowerCase();
            for (Hotel hotel : data) {
                if (hotel.getName().toLowerCase().contains(lowerSearch) ||
                        hotel.getAddress().toLowerCase().contains(lowerSearch) ||
                        (hotel.getDescription() != null
                                && hotel.getDescription().toLowerCase().contains(lowerSearch))) {
                    filteredData.add(hotel);
                }
            }
        }
    }

    private ObservableList<Hotel> getHotelsFromDB() {
        ObservableList<Hotel> list = FXCollections.observableArrayList();
        try (Connection conn = Database.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM hotels ORDER BY id");
            while (rs.next()) {
                list.add(new Hotel(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("address"),
                        rs.getDouble("rating")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // If table doesn't exist yet, show empty table
            // You can also create the table here if needed
        }
        return list;
    }

    // =================== Rooms ===================
    private BorderPane createRoomsTab() {

        TableView<Room> table = new TableView<>();
        ObservableList<Room> data = getRoomsFromDB();
        ObservableList<Room> filteredData = FXCollections.observableArrayList(data);

        ComboBox<String> hotelFilterComboBox = new ComboBox<>();
        hotelFilterComboBox.setPromptText("Filter by Hotel");
        hotelFilterComboBox.setPrefWidth(200);
        loadHotelFilters(hotelFilterComboBox, table, data, filteredData);

        TableColumn<Room, Integer> colNumber = new TableColumn<>("Room Number");
        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        colNumber.setPrefWidth(150);

        TableColumn<Room, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setPrefWidth(150);

        TableColumn<Room, Boolean> colAvailable = new TableColumn<>("Available");
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));
        colAvailable.setPrefWidth(120);

        table.setItems(filteredData);
        table.getColumns().addAll(colNumber, colType, colAvailable);
        table.setStyle("-fx-font-size: 13px;");

        Button addButton = new Button("+ Add Room");
        addButton.setStyle(
                "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        addButton.setOnAction(e -> handleAddRoom(table, data, filteredData));

        Button editButton = new Button("Edit Room");
        editButton.setStyle(
                "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        editButton.setOnAction(e -> handleEditRoom(table, data, filteredData));

        Button deleteButton = new Button("Delete Room");
        deleteButton.setStyle(
                "-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        deleteButton.setOnAction(e -> handleDeleteRoom(table, data, filteredData));

        TextField searchField = new TextField();
        searchField.setPromptText("Search by room number or type...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-padding: 10; -fx-font-size: 13px;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterRooms(newVal, data, filteredData);
        });

        HBox controls = new HBox(15, addButton, editButton, deleteButton, searchField, hotelFilterComboBox);
        controls.setStyle("-fx-padding: 15; -fx-background-color: #f5f5f5;");

        BorderPane pane = new BorderPane();
        pane.setTop(controls);
        pane.setCenter(table);
        pane.setStyle("-fx-background-color: white;");

        return pane;
    }

    private void loadHotelFilters(ComboBox<String> comboBox, TableView<Room> table,
            ObservableList<Room> data, ObservableList<Room> filteredData) {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, name FROM hotels ORDER BY name";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            comboBox.getItems().clear();
            comboBox.getItems().add("All Hotels"); // Default option

            while (rs.next()) {
                comboBox.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
            }

            comboBox.setOnAction(e -> {
                String selected = comboBox.getValue();
                if (selected == null || selected.equals("All Hotels")) {
                    filteredData.setAll(data);
                } else {
                    int hotelId = Integer.parseInt(selected.split(" - ")[0]);
                    filteredData.clear();
                    for (Room room : data) {
                        if (room.getHotelId() == hotelId) {
                            filteredData.add(room);
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAddRoom(TableView<Room> table, ObservableList<Room> data, ObservableList<Room> filteredData) {
        Stage stage = (Stage) table.getScene().getWindow();
        RoomDialog dialog = new RoomDialog(stage, null);
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            try (Connection conn = Database.getConnection()) {
                String sql = "INSERT INTO rooms (number, type, available, hotel_id) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, dialog.getRoomNumber());
                pstmt.setString(2, dialog.getRoomType());
                pstmt.setBoolean(3, dialog.isAvailable());
                pstmt.setInt(4, dialog.getHotelId()); // NEW: Add hotel_id
                pstmt.executeUpdate();

                Room newRoom = new Room(dialog.getRoomNumber(), dialog.getRoomType(),
                        dialog.isAvailable(), dialog.getHotelId()); // Updated constructor
                data.add(newRoom);
                filteredData.add(newRoom);

                showSuccess("Room added successfully!");
            } catch (Exception e) {
                showError("Error adding room: " + e.getMessage());
            }
        }
    }

    private void handleEditRoom(TableView<Room> table, ObservableList<Room> data, ObservableList<Room> filteredData) {
        Room selectedRoom = table.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showError("Please select a room to edit!");
            return;
        }

        Stage stage = (Stage) table.getScene().getWindow();
        RoomDialog dialog = new RoomDialog(stage, selectedRoom.getNumber());
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            try (Connection conn = Database.getConnection()) {
                String sql = "UPDATE rooms SET type = ?, available = ?, hotel_id = ? WHERE number = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, dialog.getRoomType());
                pstmt.setBoolean(2, dialog.isAvailable());
                pstmt.setInt(3, dialog.getHotelId()); // NEW: Update hotel_id
                pstmt.setInt(4, selectedRoom.getNumber());
                pstmt.executeUpdate();

                data.remove(selectedRoom);
                filteredData.remove(selectedRoom);
                Room updatedRoom = new Room(selectedRoom.getNumber(), dialog.getRoomType(),
                        dialog.isAvailable(), dialog.getHotelId()); // Updated constructor
                data.add(updatedRoom);
                filteredData.add(updatedRoom);

                showSuccess("Room updated successfully!");
            } catch (Exception e) {
                showError("Error updating room: " + e.getMessage());
            }
        }
    }

    private void handleDeleteRoom(TableView<Room> table, ObservableList<Room> data, ObservableList<Room> filteredData) {
        Room selectedRoom = table.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showError("Please select a room to delete!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Room #" + selectedRoom.getNumber());
        confirmAlert.setContentText("Are you sure you want to delete this room? This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = Database.getConnection()) {
                    String sql = "DELETE FROM rooms WHERE number = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, selectedRoom.getNumber());
                    pstmt.executeUpdate();

                    data.remove(selectedRoom);
                    filteredData.remove(selectedRoom);

                    showSuccess("Room deleted successfully!");
                } catch (Exception e) {
                    showError("Error deleting room: " + e.getMessage());
                }
            }
        });
    }

    private void filterRooms(String searchText, ObservableList<Room> data, ObservableList<Room> filteredData) {
        filteredData.clear();
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredData.addAll(data);
        } else {
            String lowerSearch = searchText.toLowerCase();
            for (Room room : data) {
                if (String.valueOf(room.getNumber()).contains(lowerSearch) ||
                        room.getType().toLowerCase().contains(lowerSearch)) {
                    filteredData.add(room);
                }
            }
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private ObservableList<Room> getRoomsFromDB() {
        ObservableList<Room> list = FXCollections.observableArrayList();
        try (Connection conn = Database.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM rooms");
            while (rs.next()) {
                list.add(new Room(
                        rs.getInt("number"),
                        rs.getString("type"),
                        rs.getBoolean("available"),
                        rs.getInt("hotel_id")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // =================== Reservations ===================
    private BorderPane createReservationsTab() {
        TableView<Reservation> table = new TableView<>();
        ObservableList<Reservation> data = getReservationsFromDB();
        ObservableList<Reservation> filteredData = FXCollections.observableArrayList(data);

        TableColumn<Reservation, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(80);

        TableColumn<Reservation, String> colCustomer = new TableColumn<>("Customer");
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colCustomer.setPrefWidth(150);

        TableColumn<Reservation, Integer> colRoom = new TableColumn<>("Room");
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colRoom.setPrefWidth(100);

        TableColumn<Reservation, String> colCheckIn = new TableColumn<>("Check-in");
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        colCheckIn.setPrefWidth(130);

        TableColumn<Reservation, String> colCheckOut = new TableColumn<>("Check-out");
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        colCheckOut.setPrefWidth(130);

        // In createReservationsTab() method:
        TableColumn<Reservation, String> colHotel = new TableColumn<>("Hotel");
        colHotel.setCellValueFactory(new PropertyValueFactory<>("hotelName"));
        colHotel.setPrefWidth(150);

        table.getColumns().addAll(colId, colCustomer, colHotel, colRoom, colCheckIn, colCheckOut);
        table.setItems(filteredData);
        table.setStyle("-fx-font-size: 13px;");

        Button addButton = new Button("+ Add Reservation");
        addButton.setStyle(
                "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        addButton.setOnAction(e -> handleAddReservation(table, data, filteredData));

        Button editButton = new Button("Edit Reservation");
        editButton.setStyle(
                "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        editButton.setOnAction(e -> handleEditReservation(table, data, filteredData));

        Button deleteButton = new Button("Delete Reservation");
        deleteButton.setStyle(
                "-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        deleteButton.setOnAction(e -> handleDeleteReservation(table, data, filteredData));

        TextField searchField = new TextField();
        searchField.setPromptText("Search by customer or room...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-padding: 10; -fx-font-size: 13px;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterReservations(newVal, data, filteredData);
        });

        HBox controls = new HBox(15, addButton, editButton, deleteButton, searchField);
        controls.setStyle("-fx-padding: 15; -fx-background-color: #f5f5f5;");

        BorderPane pane = new BorderPane();
        pane.setTop(controls);
        pane.setCenter(table);
        pane.setStyle("-fx-background-color: white;");

        ContextMenu contextMenu = new ContextMenu();

        MenuItem viewSummaryItem = new MenuItem("View Summary");
        viewSummaryItem.setOnAction(e -> {
            Reservation selectedReservation = table.getSelectionModel().getSelectedItem();
            if (selectedReservation != null) {
                Stage stage = (Stage) table.getScene().getWindow();
                ReservationSummaryDialog dialog = new ReservationSummaryDialog(stage, selectedReservation);
                dialog.showAndWait();
            }
        });

        MenuItem printItem = new MenuItem("Print Confirmation");
        printItem.setOnAction(e -> {
            Reservation selectedReservation = table.getSelectionModel().getSelectedItem();
            if (selectedReservation != null) {
                Stage stage = (Stage) table.getScene().getWindow();
                ReservationSummaryDialog dialog = new ReservationSummaryDialog(stage, selectedReservation);
                dialog.showAndWait();
            }
        });

        MenuItem markArrivedItem = new MenuItem("Mark as Arrived");
        markArrivedItem.setOnAction(e -> {
            Reservation selectedReservation = table.getSelectionModel().getSelectedItem();
            if (selectedReservation != null) {
                markReservationArrived(selectedReservation);
            }
        });

        contextMenu.getItems().addAll(viewSummaryItem, printItem, new SeparatorMenuItem(), markArrivedItem);

        // Set context menu on table
        table.setContextMenu(contextMenu);

        // Also allow right-click to select row
        table.setRowFactory(tv -> {
            TableRow<Reservation> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton().name().equals("SECONDARY")) {
                    table.getSelectionModel().select(row.getIndex());
                }
            });
            return row;
        });

        return pane;
    }

    private void markReservationArrived(Reservation reservation) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Mark as Arrived");
        confirmAlert.setHeaderText("Reservation #" + reservation.getId());
        confirmAlert.setContentText("Mark " + reservation.getCustomerName() + " as arrived?\n" +
                "Room: " + reservation.getRoomNumber() + "\n" +
                "Hotel: " + reservation.getHotelName());

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = Database.getConnection()) {
                    // You might want to add an 'arrived' column to reservations table
                    String sql = "UPDATE reservations SET status = 'ARRIVED' WHERE id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, reservation.getId());
                    pstmt.executeUpdate();

                    // Also update room availability
                    updateRoomsAvailability();

                    showSuccess(reservation.getCustomerName() + " marked as arrived!");
                     // Refresh the table
                } catch (Exception e) {
                    showError("Error updating reservation: " + e.getMessage());
                }
            }
        });
    }

    private void handleAddReservation(TableView<Reservation> table, ObservableList<Reservation> data,
            ObservableList<Reservation> filteredData) {
        Stage stage = (Stage) table.getScene().getWindow();
        ReservationDialog dialog = new ReservationDialog(stage, null);
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            try (Connection conn = Database.getConnection()) {
                String sql = "INSERT INTO reservations (customer_id, room_number, check_in, check_out) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, dialog.getCustomerId());
                pstmt.setInt(2, dialog.getRoomNumber());
                pstmt.setString(3, dialog.getCheckIn());
                pstmt.setString(4, dialog.getCheckOut());
                pstmt.executeUpdate();

                updateRoomsAvailability();
                ObservableList<Reservation> newData = getReservationsFromDB();
                data.clear();
                data.addAll(newData);
                filteredData.clear();
                filteredData.addAll(newData);

                showSuccess("Reservation added successfully!");
            } catch (Exception e) {
                showError("Error adding reservation: " + e.getMessage());
            }
        }
    }

    private void handleEditReservation(TableView<Reservation> table, ObservableList<Reservation> data,
            ObservableList<Reservation> filteredData) {
        Reservation selectedReservation = table.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            showError("Please select a reservation to edit!");
            return;
        }

        Stage stage = (Stage) table.getScene().getWindow();
        ReservationDialog dialog = new ReservationDialog(stage, selectedReservation.getId());
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            try (Connection conn = Database.getConnection()) {
                String sql = "UPDATE reservations SET check_in = ?, check_out = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, dialog.getCheckIn());
                pstmt.setString(2, dialog.getCheckOut());
                pstmt.setInt(3, selectedReservation.getId());
                pstmt.executeUpdate();

                updateRoomsAvailability();
                ObservableList<Reservation> newData = getReservationsFromDB();
                data.clear();
                data.addAll(newData);
                filteredData.clear();
                filteredData.addAll(newData);

                showSuccess("Reservation updated successfully!");
            } catch (Exception e) {
                showError("Error updating reservation: " + e.getMessage());
            }
        }
    }

    private void handleDeleteReservation(TableView<Reservation> table, ObservableList<Reservation> data,
            ObservableList<Reservation> filteredData) {
        Reservation selectedReservation = table.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            showError("Please select a reservation to delete!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Reservation #" + selectedReservation.getId());
        confirmAlert.setContentText("Are you sure you want to delete this reservation? This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = Database.getConnection()) {
                    String sql = "DELETE FROM reservations WHERE id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, selectedReservation.getId());
                    pstmt.executeUpdate();

                    updateRoomsAvailability();
                    data.remove(selectedReservation);
                    filteredData.remove(selectedReservation);

                    showSuccess("Reservation deleted successfully!");
                } catch (Exception e) {
                    showError("Error deleting reservation: " + e.getMessage());
                }
            }
        });
    }

    private void filterReservations(String searchText, ObservableList<Reservation> data,
            ObservableList<Reservation> filteredData) {
        filteredData.clear();
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredData.addAll(data);
        } else {
            String lowerSearch = searchText.toLowerCase();
            for (Reservation res : data) {
                if (res.getCustomerName().toLowerCase().contains(lowerSearch) ||
                        String.valueOf(res.getRoomNumber()).contains(lowerSearch)) {
                    filteredData.add(res);
                }
            }
        }
    }

    private void updateRoomsAvailability() {
        try (Connection conn = Database.getConnection()) {
            String sql = "UPDATE rooms SET available = CASE WHEN number NOT IN " +
                    "(SELECT room_number FROM reservations WHERE CURDATE() BETWEEN check_in AND check_out) " +
                    "THEN true ELSE false END";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ObservableList<Reservation> getReservationsFromDB() {
        ObservableList<Reservation> list = FXCollections.observableArrayList();
        try (Connection conn = Database.getConnection()) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT r.id, c.name AS customerName, c.email AS customerEmail, " +
                    "c.phone AS customerPhone, r.room_number, r.check_in, r.check_out, " +
                    "h.name AS hotel_name, rm.type AS room_type " + // ADDED room type
                    "FROM reservations r " +
                    "JOIN customers c ON r.customer_id = c.id " +
                    "JOIN rooms rm ON r.room_number = rm.number " +
                    "JOIN hotels h ON rm.hotel_id = h.id " +
                    "ORDER BY r.check_in DESC";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new Reservation(
                        rs.getInt("id"),
                        rs.getString("customerName"),
                        rs.getInt("room_number"),
                        rs.getString("check_in"),
                        rs.getString("check_out"),
                        rs.getString("hotel_name"),
                        rs.getString("customerEmail"),
                        rs.getString("customerPhone"),
                        rs.getString("room_type")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // =================== Customers ===================
    private BorderPane createCustomersTab() {
        TableView<Customer> table = new TableView<>();
        ObservableList<Customer> data = getCustomersFromDB();
        ObservableList<Customer> filteredData = FXCollections.observableArrayList(data);

        TableColumn<Customer, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<Customer, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(150);

        TableColumn<Customer, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(180);

        // ADDED: Phone column
        TableColumn<Customer, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPhone.setPrefWidth(120);

        // ADDED: Date of Birth column
        TableColumn<Customer, String> colDOB = new TableColumn<>("Date of Birth");
        colDOB.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        colDOB.setPrefWidth(120);

        table.setItems(filteredData);
        // UPDATED: Added phone and dob columns
        table.getColumns().addAll(colId, colName, colEmail, colPhone, colDOB);
        table.setStyle("-fx-font-size: 13px;");

        Button addButton = new Button("+ Add Customer");
        addButton.setStyle(
                "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        addButton.setOnAction(e -> handleAddCustomer(table, data, filteredData));

        Button editButton = new Button("Edit Customer");
        editButton.setStyle(
                "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        editButton.setOnAction(e -> handleEditCustomer(table, data, filteredData));

        Button deleteButton = new Button("Delete Customer");
        deleteButton.setStyle(
                "-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        deleteButton.setOnAction(e -> handleDeleteCustomer(table, data, filteredData));

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or email...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-padding: 10; -fx-font-size: 13px;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterCustomers(newVal, data, filteredData);
        });

        HBox controls = new HBox(15, addButton, editButton, deleteButton, searchField);
        controls.setStyle("-fx-padding: 15; -fx-background-color: #f5f5f5;");

        BorderPane pane = new BorderPane();
        pane.setTop(controls);
        pane.setCenter(table);
        pane.setStyle("-fx-background-color: white;");

        return pane;
    }

    private void handleAddCustomer(TableView<Customer> table, ObservableList<Customer> data,
            ObservableList<Customer> filteredData) {
        Stage stage = (Stage) table.getScene().getWindow();
        CustomerDialog dialog = new CustomerDialog(stage, null);
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            try (Connection conn = Database.getConnection()) {
                // UPDATED: Added date_of_birth to SQL
                String sql = "INSERT INTO customers (name, email, phone, address, date_of_birth) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, dialog.getCustomerName());
                pstmt.setString(2, dialog.getEmail());
                pstmt.setString(3, dialog.getPhone());
                pstmt.setString(4, dialog.getAddress());

                // Handle date of birth (can be null)
                String dob = dialog.getDateOfBirth();
                if (dob != null && !dob.isEmpty()) {
                    pstmt.setDate(5, java.sql.Date.valueOf(dob));
                } else {
                    pstmt.setNull(5, java.sql.Types.DATE);
                }

                pstmt.executeUpdate();

                ObservableList<Customer> newData = getCustomersFromDB();
                data.clear();
                data.addAll(newData);
                filteredData.clear();
                filteredData.addAll(newData);

                showSuccess("Customer added successfully!");
            } catch (Exception e) {
                showError("Error adding customer: " + e.getMessage());
            }
        }
    }

    private void handleEditCustomer(TableView<Customer> table, ObservableList<Customer> data,
            ObservableList<Customer> filteredData) {
        Customer selectedCustomer = table.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            showError("Please select a customer to edit!");
            return;
        }

        Stage stage = (Stage) table.getScene().getWindow();
        CustomerDialog dialog = new CustomerDialog(stage, selectedCustomer.getId());
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            try (Connection conn = Database.getConnection()) {
                // UPDATED: Added date_of_birth to SQL
                String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, address = ?, date_of_birth = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, dialog.getCustomerName());
                pstmt.setString(2, dialog.getEmail());
                pstmt.setString(3, dialog.getPhone());
                pstmt.setString(4, dialog.getAddress());

                // Handle date of birth (can be null)
                String dob = dialog.getDateOfBirth();
                if (dob != null && !dob.isEmpty()) {
                    pstmt.setDate(5, java.sql.Date.valueOf(dob));
                } else {
                    pstmt.setNull(5, java.sql.Types.DATE);
                }

                pstmt.setInt(6, selectedCustomer.getId());
                pstmt.executeUpdate();

                ObservableList<Customer> newData = getCustomersFromDB();
                data.clear();
                data.addAll(newData);
                filteredData.clear();
                filteredData.addAll(newData);

                showSuccess("Customer updated successfully!");
            } catch (Exception e) {
                showError("Error updating customer: " + e.getMessage());
            }
        }
    }

    private void handleDeleteCustomer(TableView<Customer> table, ObservableList<Customer> data,
            ObservableList<Customer> filteredData) {
        Customer selectedCustomer = table.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            showError("Please select a customer to delete!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Customer: " + selectedCustomer.getName());
        confirmAlert.setContentText("Are you sure you want to delete this customer? This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = Database.getConnection()) {
                    String sql = "DELETE FROM customers WHERE id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, selectedCustomer.getId());
                    pstmt.executeUpdate();

                    data.remove(selectedCustomer);
                    filteredData.remove(selectedCustomer);

                    showSuccess("Customer deleted successfully!");
                } catch (Exception e) {
                    showError("Error deleting customer: " + e.getMessage());
                }
            }
        });
    }

    private void filterCustomers(String searchText, ObservableList<Customer> data,
            ObservableList<Customer> filteredData) {
        filteredData.clear();
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredData.addAll(data);
        } else {
            String lowerSearch = searchText.toLowerCase();
            for (Customer customer : data) {
                if (customer.getName().toLowerCase().contains(lowerSearch) ||
                        customer.getEmail().toLowerCase().contains(lowerSearch)) {
                    filteredData.add(customer);
                }
            }
        }
    }

    private ObservableList<Customer> getCustomersFromDB() {
        ObservableList<Customer> list = FXCollections.observableArrayList();
        try (Connection conn = Database.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM customers");
            while (rs.next()) {
                list.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"), // ADDED
                        rs.getDate("date_of_birth") // ADDED
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // =================== Nested Model Classes ===================
    public static class Hotel {
        private int id;
        private String name;
        private String description;
        private String address;
        private double rating;

        public Hotel(int id, String name, String description, String address, double rating) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.address = address;
            this.rating = rating;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getAddress() {
            return address;
        }

        public double getRating() {
            return rating;
        }
    }

    public static class Room {
        private int number;
        private String type;
        private boolean available;
        private int hotelId; // NEW: Add hotel ID
        private String hotelName; // Optional: Add hotel name for display

        public Room(int number, String type, boolean available, int hotelId) {
            this.number = number;
            this.type = type;
            this.available = available;
            this.hotelId = hotelId;
        }

        public int getNumber() {
            return number;
        }

        public String getType() {
            return type;
        }

        public boolean getAvailable() {
            return available;
        }

        public int getHotelId() {
            return hotelId;
        } // NEW: Getter
    }

    public static class Reservation {
        private int id;
        private String customerName;
        private int roomNumber;
        private String checkIn;
        private String checkOut;
        private String hotelName;
        private String customerEmail; // ADDED
        private String customerPhone; // ADDED
        private String roomType; // ADDED
        private double roomPrice; // ADDED (optional)

        // UPDATED Constructor
        public Reservation(int id, String customerName, int roomNumber,
                String checkIn, String checkOut, String hotelName,
                String customerEmail, String customerPhone, String roomType) {
            this.id = id;
            this.customerName = customerName;
            this.roomNumber = roomNumber;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.hotelName = hotelName;
            this.customerEmail = customerEmail;
            this.customerPhone = customerPhone;
            this.roomType = roomType;
            this.roomPrice = 0.0; // Default or calculate based on room type
        }
        // REQUIRED GETTERS ----------------------------------------

        public int getId() {
            return id;
        }

        public String getCustomerName() {
            return customerName;
        }

        public int getRoomNumber() {
            return roomNumber;
        }

        public String getCheckIn() {
            return checkIn;
        }

        public String getCheckOut() {
            return checkOut;
        }

        public String getHotelName() {
            return hotelName;
        }

        public String getCustomerEmail() {
            return customerEmail;
        }

        public String getCustomerPhone() {
            return customerPhone;
        }

        public String getRoomType() {
            return roomType;
        }

        public double getRoomPrice() {
            return roomPrice;
        }
    }

    public static class Customer {
        private int id;
        private String name;
        private String email;
        private String phone; // ADDED
        private String dateOfBirth; // ADDED

        public Customer(int id, String name, String email, String phone, java.sql.Date dateOfBirth) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone != null ? phone : "";

            // Format date for display
            if (dateOfBirth != null) {
                this.dateOfBirth = dateOfBirth.toString(); // YYYY-MM-DD format
            } else {
                this.dateOfBirth = "";
            }
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        // ADDED: Phone getter
        public String getPhone() {
            return phone;
        }

        // ADDED: Date of birth getter
        public String getDateOfBirth() {
            return dateOfBirth;
        }
    }

    // Simple in-place ReservationSummaryDialog so the constructor ReservationSummaryDialog(Stage, Reservation)
    // resolves to a known type using the nested Reservation model defined above.
    public static class ReservationSummaryDialog extends Dialog<Void> {
        public ReservationSummaryDialog(Stage owner, Reservation reservation) {
            // Set owner and basic dialog properties
            initOwner(owner);
            setTitle("Reservation Summary");
            setHeaderText("Reservation #" + reservation.getId());

            // Build a simple read-only summary content
            StringBuilder sb = new StringBuilder();
            sb.append("Customer: ").append(reservation.getCustomerName()).append("\n");
            sb.append("Email: ").append(reservation.getCustomerEmail() == null ? "" : reservation.getCustomerEmail()).append("\n");
            sb.append("Phone: ").append(reservation.getCustomerPhone() == null ? "" : reservation.getCustomerPhone()).append("\n");
            sb.append("Hotel: ").append(reservation.getHotelName() == null ? "" : reservation.getHotelName()).append("\n");
            sb.append("Room: ").append(reservation.getRoomNumber()).append("\n");
            sb.append("Room Type: ").append(reservation.getRoomType() == null ? "" : reservation.getRoomType()).append("\n");
            sb.append("Check-in: ").append(reservation.getCheckIn() == null ? "" : reservation.getCheckIn()).append("\n");
            sb.append("Check-out: ").append(reservation.getCheckOut() == null ? "" : reservation.getCheckOut()).append("\n");

            TextArea area = new TextArea(sb.toString());
            area.setEditable(false);
            area.setWrapText(true);
            area.setPrefWidth(480);
            area.setPrefHeight(260);

            DialogPane pane = getDialogPane();
            pane.setContent(area);
            pane.getButtonTypes().add(ButtonType.CLOSE);
        }
    }

}