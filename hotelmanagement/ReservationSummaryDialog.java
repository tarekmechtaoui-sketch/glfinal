package com.hotelmanagement;

import com.hotelmanagement.Dashboard.Reservation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ReservationSummaryDialog {
    private Stage stage;
    private Reservation reservation;
    private VBox printableContent;

    public ReservationSummaryDialog(Stage owner, Reservation reservation) {
        this.reservation = reservation;
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Reservation Confirmation");

        createUI();
    }

    private void createUI() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f8f9fa; -fx-background-color: #f8f9fa;");

        VBox outerContainer = new VBox(20);
        outerContainer.setPadding(new Insets(30));
        outerContainer.setStyle("-fx-background-color: #f8f9fa;");
        outerContainer.setAlignment(Pos.TOP_CENTER);

        printableContent = new VBox(0);
        printableContent.setMaxWidth(650);
        printableContent.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 5);");

        VBox header = createHeader();
        VBox body = createBody();
        VBox footer = createFooter();

        printableContent.getChildren().addAll(header, body, footer);

        HBox buttonBox = createButtonBox();
        buttonBox.setMaxWidth(650);

        outerContainer.getChildren().addAll(printableContent, buttonBox);
        scrollPane.setContent(outerContainer);

        Scene scene = new Scene(scrollPane, 750, 800);
        stage.setScene(scene);
    }

    private VBox createHeader() {
        VBox header = new VBox(8);
        header.setPadding(new Insets(35, 40, 25, 40));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: linear-gradient(to bottom, #1e3c72, #2a5298); " +
                       "-fx-background-radius: 0 0 0 0;");

        Label title = new Label("RESERVATION CONFIRMED");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);
        title.setTextAlignment(TextAlignment.CENTER);

        Label subtitle = new Label("Thank you for your booking");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.rgb(255, 255, 255, 0.9));

        Region spacer = new Region();
        spacer.setPrefHeight(10);

        HBox idBox = new HBox();
        idBox.setAlignment(Pos.CENTER);
        idBox.setPadding(new Insets(12, 20, 12, 20));
        idBox.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 25;");

        Label idLabel = new Label("Confirmation #" + reservation.getId());
        idLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        idLabel.setTextFill(Color.WHITE);

        idBox.getChildren().add(idLabel);

        header.getChildren().addAll(title, subtitle, spacer, idBox);
        return header;
    }

    private VBox createBody() {
        VBox body = new VBox(0);
        body.setPadding(new Insets(30, 40, 30, 40));

        VBox hotelSection = createInfoSection("Hotel Information",
            new String[]{"Hotel Name"},
            new String[]{reservation.getHotelName()});

        VBox guestSection = createInfoSection("Guest Information",
            new String[]{"Name", "Email", "Phone"},
            new String[]{
                reservation.getCustomerName(),
                reservation.getCustomerEmail() != null ? reservation.getCustomerEmail() : "N/A",
                reservation.getCustomerPhone() != null ? reservation.getCustomerPhone() : "N/A"
            });

        String duration = calculateDuration();
        VBox staySection = createInfoSection("Stay Details",
            new String[]{"Room Number", "Room Type", "Check-in Date", "Check-out Date", "Duration"},
            new String[]{
                String.valueOf(reservation.getRoomNumber()),
                reservation.getRoomType(),
                formatDate(reservation.getCheckIn()),
                formatDate(reservation.getCheckOut()),
                duration
            });

        VBox statusSection = createStatusSection();

        body.getChildren().addAll(hotelSection, createDivider(), guestSection,
                                  createDivider(), staySection, createDivider(), statusSection);
        return body;
    }

    private VBox createInfoSection(String title, String[] labels, String[] values) {
        VBox section = new VBox(12);
        section.setPadding(new Insets(0, 0, 20, 0));

        Label sectionTitle = new Label(title);
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.rgb(30, 60, 114));
        sectionTitle.setPadding(new Insets(0, 0, 8, 0));

        VBox items = new VBox(10);
        for (int i = 0; i < labels.length; i++) {
            HBox item = createInfoItem(labels[i], values[i]);
            items.getChildren().add(item);
        }

        section.getChildren().addAll(sectionTitle, items);
        return section;
    }

    private HBox createInfoItem(String label, String value) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);

        Label labelText = new Label(label + ":");
        labelText.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        labelText.setTextFill(Color.rgb(108, 117, 125));
        labelText.setMinWidth(130);

        Label valueText = new Label(value);
        valueText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        valueText.setTextFill(Color.rgb(33, 37, 41));
        valueText.setWrapText(true);

        item.getChildren().addAll(labelText, valueText);
        return item;
    }

    private VBox createStatusSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(10, 0, 0, 0));

        HBox statusBox = new HBox(12);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPadding(new Insets(16, 30, 16, 30));
        statusBox.setStyle("-fx-background-color: #d4edda; -fx-background-radius: 8; -fx-border-color: #c3e6cb; -fx-border-radius: 8; -fx-border-width: 2;");

        Label checkIcon = new Label("✓");
        checkIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        checkIcon.setTextFill(Color.rgb(40, 167, 69));

        Label statusLabel = new Label("CONFIRMED");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        statusLabel.setTextFill(Color.rgb(40, 167, 69));

        statusBox.getChildren().addAll(checkIcon, statusLabel);
        section.getChildren().add(statusBox);

        return section;
    }

    private VBox createFooter() {
        VBox footer = new VBox(15);
        footer.setPadding(new Insets(25, 40, 35, 40));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-width: 1 0 0 0;");

        Label importantTitle = new Label("Important Information");
        importantTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        importantTitle.setTextFill(Color.rgb(30, 60, 114));

        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(10, 20, 10, 20));

        Label info1 = new Label("• Please present this confirmation at check-in");
        Label info2 = new Label("• Check-in time: 2:00 PM");
        Label info3 = new Label("• Check-out time: 12:00 PM");
        Label info4 = new Label("• Valid photo ID required at check-in");

        for (Label label : new Label[]{info1, info2, info3, info4}) {
            label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            label.setTextFill(Color.rgb(108, 117, 125));
        }

        infoBox.getChildren().addAll(info1, info2, info3, info4);

        Label footerNote = new Label("We look forward to welcoming you!");
        footerNote.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        footerNote.setTextFill(Color.rgb(108, 117, 125));
        footerNote.setStyle("-fx-font-style: italic;");

        footer.getChildren().addAll(importantTitle, infoBox, footerNote);
        return footer;
    }

    private Region createDivider() {
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: #e9ecef;");
        VBox.setMargin(divider, new Insets(0, 0, 20, 0));
        return divider;
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(0, 0, 10, 0));

        Button printButton = new Button("Print Confirmation");
        printButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        printButton.setPrefWidth(180);
        printButton.setPrefHeight(42);
        printButton.setStyle(
            "-fx-background-color: #1e3c72; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );
        printButton.setOnMouseEntered(e ->
            printButton.setStyle(
                "-fx-background-color: #2a5298; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 3);"
            )
        );
        printButton.setOnMouseExited(e ->
            printButton.setStyle(
                "-fx-background-color: #1e3c72; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
            )
        );
        printButton.setOnAction(e -> printConfirmation());

        Button closeButton = new Button("Close");
        closeButton.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        closeButton.setPrefWidth(120);
        closeButton.setPrefHeight(42);
        closeButton.setStyle(
            "-fx-background-color: #6c757d; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );
        closeButton.setOnMouseEntered(e ->
            closeButton.setStyle(
                "-fx-background-color: #5a6268; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand;"
            )
        );
        closeButton.setOnMouseExited(e ->
            closeButton.setStyle(
                "-fx-background-color: #6c757d; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand;"
            )
        );
        closeButton.setOnAction(e -> stage.close());

        buttonBox.getChildren().addAll(printButton, closeButton);
        return buttonBox;
    }

    private String calculateDuration() {
        try {
            java.time.LocalDate checkIn = java.time.LocalDate.parse(reservation.getCheckIn());
            java.time.LocalDate checkOut = java.time.LocalDate.parse(reservation.getCheckOut());
            long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
            return nights + (nights == 1 ? " night" : " nights");
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String formatDate(String dateStr) {
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
            return date.format(formatter);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void printConfirmation() {
        try {
            Printer printer = Printer.getDefaultPrinter();
            if (printer == null) {
                showAlert(Alert.AlertType.WARNING, "No Printer Found",
                    "No default printer found. Please install a printer.");
                return;
            }

            PrinterJob job = PrinterJob.createPrinterJob(printer);
            if (job == null) {
                showAlert(Alert.AlertType.ERROR, "Print Error",
                    "Could not create print job. Please try again.");
                return;
            }

            PageLayout pageLayout = printer.createPageLayout(
                Paper.NA_LETTER,
                PageOrientation.PORTRAIT,
                Printer.MarginType.DEFAULT
            );
            job.getJobSettings().setPageLayout(pageLayout);

            if (job.showPrintDialog(stage)) {
                VBox printNode = createPrintableVersion();

                double scaleX = pageLayout.getPrintableWidth() / printNode.getBoundsInParent().getWidth();
                double scaleY = pageLayout.getPrintableHeight() / printNode.getBoundsInParent().getHeight();
                double scale = Math.min(scaleX, scaleY) * 0.95;

                printNode.getTransforms().add(new javafx.scene.transform.Scale(scale, scale));

                boolean success = job.printPage(pageLayout, printNode);

                if (success) {
                    job.endJob();
                    showAlert(Alert.AlertType.INFORMATION, "Print Successful",
                        "Reservation confirmation has been sent to the printer.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Print Failed",
                        "Failed to print the confirmation. Please try again.");
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Print Error",
                "Error printing confirmation: " + e.getMessage());
        }
    }

    private VBox createPrintableVersion() {
        VBox printVersion = new VBox(0);
        printVersion.setStyle("-fx-background-color: white;");
        printVersion.setPrefWidth(600);

        VBox header = createHeader();
        VBox body = createBody();
        VBox footer = createFooter();

        printVersion.getChildren().addAll(header, body, footer);

        Scene tempScene = new Scene(printVersion);
        return printVersion;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showAndWait() {
        stage.showAndWait();
    }
}
