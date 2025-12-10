package com.hotelmanagement;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReservationDialog {
    private Stage stage;
    private boolean confirmed = false;
    private ComboBox<String> hotelComboBox;
    private ComboBox<String> customerComboBox;
    private ComboBox<Integer> roomComboBox;
    private DatePicker checkInPicker;
    private DatePicker checkOutPicker;
    private Integer reservationId;

    public ReservationDialog(Stage owner, Integer existingReservationId) {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

        this.reservationId = existingReservationId;

        if (existingReservationId != null) {
            stage.setTitle("Edit Reservation");
        } else {
            stage.setTitle("Add Reservation");
        }

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(15);

        Label hotelLabel = new Label("Hotel:");
        hotelComboBox = new ComboBox<>();
        loadHotels();
        hotelComboBox.setPrefWidth(200);
        
        // Add listener to load rooms when hotel is selected
        hotelComboBox.setOnAction(e -> {
            if (hotelComboBox.getValue() != null && reservationId == null) {
                int hotelId = getSelectedHotelId();
                loadAvailableRooms(hotelId);
            }
        });

        Label customerLabel = new Label("Customer:");
        customerComboBox = new ComboBox<>();
        loadCustomers();
        customerComboBox.setPrefWidth(200);

        Label roomLabel = new Label("Room:");
        roomComboBox = new ComboBox<>();
        // Don't load rooms initially - wait for hotel selection
        roomComboBox.setPrefWidth(200);

        Label checkInLabel = new Label("Check-In Date:");
        checkInPicker = new DatePicker();
        checkInPicker.setPrefWidth(200);

        Label checkOutLabel = new Label("Check-Out Date:");
        checkOutPicker = new DatePicker();
        checkOutPicker.setPrefWidth(200);

        Button saveButton = new Button("Save");
        saveButton.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        saveButton.setOnAction(e -> {
            if (validateInput()) {
                confirmed = true;
                stage.close();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-padding: 8 20;");
        cancelButton.setOnAction(e -> stage.close());

        grid.add(hotelLabel, 0, 0);
        grid.add(hotelComboBox, 1, 0);
        grid.add(customerLabel, 0, 1);
        grid.add(customerComboBox, 1, 1);
        grid.add(roomLabel, 0, 2);
        grid.add(roomComboBox, 1, 2);
        grid.add(checkInLabel, 0, 3);
        grid.add(checkInPicker, 1, 3);
        grid.add(checkOutLabel, 0, 4);
        grid.add(checkOutPicker, 1, 4);
        grid.add(saveButton, 0, 5);
        grid.add(cancelButton, 1, 5);

        if (existingReservationId != null) {
            loadReservationData(existingReservationId);
        } else {
            // Make hotel selection required for new reservations
            hotelComboBox.setPromptText("Select a hotel first");
            roomComboBox.setDisable(true);
        }

        Scene scene = new Scene(grid, 450, 350);
        stage.setScene(scene);
    }

    private void loadHotels() {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, name FROM hotels ORDER BY name";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                hotelComboBox.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
            }
        } catch (Exception e) {
            showError("Error loading hotels: " + e.getMessage());
        }
    }

    private void loadCustomers() {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, name FROM customers ORDER BY name";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                customerComboBox.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
            }
        } catch (Exception e) {
            showError("Error loading customers: " + e.getMessage());
        }
    }

    private void loadAvailableRooms(Integer hotelId) {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT r.number FROM rooms r " +
                        "WHERE r.available = true AND r.hotel_id = ? " +
                        "AND r.number NOT IN ( " +
                        "    SELECT room_number FROM reservations " +
                        "    WHERE (check_in <= ? AND check_out >= ?) " +
                        "    OR (? BETWEEN check_in AND check_out) " +
                        "    OR (? BETWEEN check_in AND check_out)" +
                        ") " +
                        "ORDER BY r.number";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, hotelId);
            
            // Set date parameters to check for availability
            if (checkInPicker.getValue() != null && checkOutPicker.getValue() != null) {
                pstmt.setString(2, checkOutPicker.getValue().toString());
                pstmt.setString(3, checkInPicker.getValue().toString());
                pstmt.setString(4, checkInPicker.getValue().toString());
                pstmt.setString(5, checkOutPicker.getValue().toString());
            } else {
                // If dates not selected yet, just get all available rooms
                sql = "SELECT number FROM rooms WHERE available = true AND hotel_id = ? ORDER BY number";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, hotelId);
            }

            ResultSet rs = pstmt.executeQuery();

            roomComboBox.getItems().clear();
            roomComboBox.setDisable(false);
            while (rs.next()) {
                roomComboBox.getItems().add(rs.getInt("number"));
            }
            
            if (roomComboBox.getItems().isEmpty()) {
                roomComboBox.setPromptText("No available rooms");
            }
        } catch (Exception e) {
            showError("Error loading rooms: " + e.getMessage());
        }
    }

    private void loadReservationData(int reservationId) {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT r.id, r.customer_id, c.name, r.room_number, r.check_in, r.check_out, " +
                        "rm.hotel_id, h.name as hotel_name " +
                        "FROM reservations r " +
                        "JOIN customers c ON r.customer_id = c.id " +
                        "JOIN rooms rm ON r.room_number = rm.number " +
                        "JOIN hotels h ON rm.hotel_id = h.id " +
                        "WHERE r.id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reservationId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int hotelId = rs.getInt("hotel_id");
                String hotelName = rs.getString("hotel_name");
                hotelComboBox.setValue(hotelId + " - " + hotelName);
                hotelComboBox.setDisable(true); // Hotel cannot be changed for existing reservation
                
                int customerId = rs.getInt("customer_id");
                String customerName = rs.getString("name");
                customerComboBox.setValue(customerId + " - " + customerName);
                customerComboBox.setDisable(true); // Customer cannot be changed for existing reservation

                // Load rooms for this hotel
                loadAvailableRooms(hotelId);
                
                int currentRoom = rs.getInt("room_number");
                roomComboBox.setValue(currentRoom);

                checkInPicker.setValue(rs.getDate("check_in").toLocalDate());
                checkOutPicker.setValue(rs.getDate("check_out").toLocalDate());
            }
        } catch (Exception e) {
            showError("Error loading reservation data: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (hotelComboBox.getValue() == null || hotelComboBox.getValue().trim().isEmpty()) {
            showError("Hotel is required!");
            return false;
        }

        if (customerComboBox.getValue() == null || customerComboBox.getValue().trim().isEmpty()) {
            showError("Customer is required!");
            return false;
        }

        if (roomComboBox.getValue() == null) {
            showError("Room is required!");
            return false;
        }

        if (checkInPicker.getValue() == null) {
            showError("Check-In date is required!");
            return false;
        }

        if (checkOutPicker.getValue() == null) {
            showError("Check-Out date is required!");
            return false;
        }

        if (checkOutPicker.getValue().isBefore(checkInPicker.getValue()) ||
                checkOutPicker.getValue().isEqual(checkInPicker.getValue())) {
            showError("Check-Out date must be after Check-In date!");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showAndWait() {
        stage.showAndWait();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getHotelId() {
        String value = hotelComboBox.getValue();
        return Integer.parseInt(value.split(" - ")[0]);
    }

    public int getCustomerId() {
        String value = customerComboBox.getValue();
        return Integer.parseInt(value.split(" - ")[0]);
    }

    public int getRoomNumber() {
        return roomComboBox.getValue();
    }

    public String getCheckIn() {
        return checkInPicker.getValue().toString();
    }

    public String getCheckOut() {
        return checkOutPicker.getValue().toString();
    }
    
    private int getSelectedHotelId() {
        if (hotelComboBox.getValue() != null) {
            String value = hotelComboBox.getValue();
            return Integer.parseInt(value.split(" - ")[0]);
        }
        return -1;
    }
}