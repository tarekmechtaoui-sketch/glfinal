package com.hotelmanagement;

import com.hotelmanagement.Dashboard.Reservation;

import javafx.geometry.Insets;
import javafx.print.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ReservationSummaryDialog {
    private Stage stage;
    private Reservation reservation;
    
    public ReservationSummaryDialog(Stage owner, Reservation reservation) {
        this.reservation = reservation;
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Reservation Summary - #" + reservation.getId());
        
        createUI();
    }
    
    private void createUI() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(25));
        mainLayout.setStyle("-fx-background-color: white;");
        
        // Header
        Label title = new Label("RESERVATION CONFIRMATION");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #2c3e50;");
        
        Label reservationId = new Label("Reservation ID: " + reservation.getId());
        reservationId.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        // Main content
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 0, 15, 0));
        
        // Hotel Information
        Label hotelHeader = new Label("HOTEL INFORMATION");
        hotelHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        hotelHeader.setStyle("-fx-text-fill: #3498db;");
        grid.add(hotelHeader, 0, 0, 2, 1);
        
        grid.add(new Label("Hotel:"), 0, 1);
        grid.add(new Label(reservation.getHotelName()), 1, 1);
        
        // Customer Information
        Label customerHeader = new Label("CUSTOMER INFORMATION");
        customerHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        customerHeader.setStyle("-fx-text-fill: #3498db;");
        grid.add(customerHeader, 0, 2, 2, 1);
        
        grid.add(new Label("Name:"), 0, 3);
        grid.add(new Label(reservation.getCustomerName()), 1, 3);
        
        grid.add(new Label("Email:"), 0, 4);
        grid.add(new Label(reservation.getCustomerEmail()), 1, 4);
        
        grid.add(new Label("Phone:"), 0, 5);
        grid.add(new Label(reservation.getCustomerPhone() != null ? reservation.getCustomerPhone() : "N/A"), 1, 5);
        
        // Reservation Details
        Label detailsHeader = new Label("RESERVATION DETAILS");
        detailsHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        detailsHeader.setStyle("-fx-text-fit: #3498db;");
        grid.add(detailsHeader, 0, 6, 2, 1);
        
        grid.add(new Label("Room #:"), 0, 7);
        grid.add(new Label(String.valueOf(reservation.getRoomNumber())), 1, 7);
        
        grid.add(new Label("Room Type:"), 0, 8);
        grid.add(new Label(reservation.getRoomType()), 1, 8);
        
        grid.add(new Label("Check-in:"), 0, 9);
        grid.add(new Label(reservation.getCheckIn()), 1, 9);
        
        grid.add(new Label("Check-out:"), 0, 10);
        grid.add(new Label(reservation.getCheckOut()), 1, 10);
        
        // Calculate duration (simple calculation)
        try {
            java.time.LocalDate checkIn = java.time.LocalDate.parse(reservation.getCheckIn());
            java.time.LocalDate checkOut = java.time.LocalDate.parse(reservation.getCheckOut());
            long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
            grid.add(new Label("Duration:"), 0, 11);
            grid.add(new Label(nights + " night(s)"), 1, 11);
        } catch (Exception e) {
            // Ignore date parsing errors
        }
        
        // Status
        Label statusHeader = new Label("STATUS");
        statusHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statusHeader.setStyle("-fx-text-fill: #3498db;");
        grid.add(statusHeader, 0, 12, 2, 1);
        
        Label statusLabel = new Label("CONFIRMED");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        statusLabel.setStyle("-fx-text-fill: #27ae60;");
        grid.add(statusLabel, 0, 13, 2, 1);
        
        // Footer/Notes
        Label notes = new Label("Please present this confirmation at check-in.\n" +
                               "Check-in time: 2:00 PM | Check-out time: 12:00 PM");
        notes.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        notes.setStyle("-fx-text-fill: #7f8c8d; -fx-padding: 10 0 0 0;");
        
        // Print button
        Button printButton = new Button("🖨️ Print Confirmation");
        printButton.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand;");
        printButton.setOnAction(e -> printConfirmation(mainLayout));
        
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                            "-fx-padding: 10 25; -fx-cursor: hand;");
        closeButton.setOnAction(e -> stage.close());
        
        // Button container
        HBox buttonBox = new HBox(15);
        buttonBox.getChildren().addAll(printButton, closeButton);
        
        mainLayout.getChildren().addAll(title, reservationId, grid, notes, buttonBox);
        
        Scene scene = new Scene(mainLayout, 500, 650);
        stage.setScene(scene);
    }
    
    private void printConfirmation(VBox content) {
        try {
            Printer printer = Printer.getDefaultPrinter();
            if (printer == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Printer Found");
                alert.setHeaderText(null);
                alert.setContentText("No default printer found. Please install a printer.");
                alert.showAndWait();
                return;
            }
            
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(stage)) {
                // Scale content for printing
                content.setScaleX(0.9);
                content.setScaleY(0.9);
                
                boolean success = job.printPage(content);
                if (success) {
                    job.endJob();
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Print Successful");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Reservation confirmation printed successfully!");
                    successAlert.showAndWait();
                }
                
                // Reset scale
                content.setScaleX(1.0);
                content.setScaleY(1.0);
            }
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Print Error");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Error printing confirmation: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }
    
    public void showAndWait() {
        stage.showAndWait();
    }
}