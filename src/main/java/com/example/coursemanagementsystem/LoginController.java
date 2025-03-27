package com.example.coursemanagementsystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private Button close;
    @FXML private Button loginbtn;
    @FXML private Button signupbtn;
    @FXML private PasswordField password;
    @FXML private TextField username;
    @FXML private StackPane rootPane;
    @FXML private AnchorPane main_form;
    @FXML private AnchorPane leftPane;
    @FXML private AnchorPane rightPane;

    private Stage stage;
    private Admin admin;

    @FXML
    public void initialize() {
        admin = new Admin();
        try {
            updateButtonVisibility();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error checking admin existence: " + e.getMessage());
        }

        // Center the form and handle full-screen adjustments
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                stage = (Stage) newScene.getWindow();
                centerFormOnStage();
            }
        });
    }

    private void centerFormOnStage() {
        if (stage == null) return;

        // Ensure the form is centered in the StackPane
        StackPane.setAlignment(main_form, Pos.CENTER);

        // Adjust layout on full-screen or resize
        stage.fullScreenProperty().addListener((obs, oldVal, newVal) -> adjustFormLayout());
        stage.widthProperty().addListener((obs, oldVal, newVal) -> adjustFormLayout());
        stage.heightProperty().addListener((obs, oldVal, newVal) -> adjustFormLayout());

        adjustFormLayout();
    }

    private void adjustFormLayout() {
        double stageWidth = stage.getWidth();
        double stageHeight = stage.getHeight();

        // Cap form size at 600x400, but allow it to shrink if stage is smaller
        double formWidth = Math.min(600, stageWidth - 20); // Small padding
        double formHeight = Math.min(400, stageHeight - 20);

        main_form.setPrefWidth(formWidth);
        main_form.setPrefHeight(formHeight);

        // Split left and right panes evenly
        double halfWidth = formWidth / 2;
        leftPane.setPrefWidth(halfWidth);
        rightPane.setPrefWidth(halfWidth);
        AnchorPane.setLeftAnchor(rightPane, halfWidth);

        // Ensure panes stretch to full height
        leftPane.setPrefHeight(formHeight);
        rightPane.setPrefHeight(formHeight);
    }

    @FXML
    public void close() {
        Stage currentStage = (Stage) close.getScene().getWindow();
        DatabaseUtil.shutdown();
        currentStage.close();
    }

    @FXML
    public void loginAdmin() {
        if (username.getText().isEmpty() || password.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields.");
            return;
        }

        try {
            if (admin.validateAdmin(username.getText(), password.getText())) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/coursemanagementsystem/dashboard.fxml"));
                if (loader.getLocation() == null) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Dashboard FXML file not found.");
                    return;
                }
                Parent root = loader.load();
                DashboardController dashboardController = loader.getController();

                if (stage == null) {
                    stage = (Stage) loginbtn.getScene().getWindow();
                }

                dashboardController.setStage(stage);
                dashboardController.setAdmin(admin);

                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Course Management System - Dashboard");
                stage.setMinWidth(800);
                stage.setMinHeight(600);
                stage.centerOnScreen();
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid credentials.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Login failed: " + e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void signupAdmin() {
        if (username.getText().isEmpty() || password.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields.");
            return;
        }

        try {
            admin.signupAdmin(username.getText(), password.getText());
            showAlert(Alert.AlertType.INFORMATION, "Success", "Admin signed up successfully! Please log in.");
            updateButtonVisibility();
            username.clear();
            password.clear();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", e.getMessage());
        }
    }

    private void updateButtonVisibility() throws SQLException {
        boolean adminExists = admin.adminExists();
        loginbtn.setVisible(adminExists);
        signupbtn.setVisible(!adminExists);
        loginbtn.setManaged(adminExists);
        signupbtn.setManaged(!adminExists);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        if (rootPane.getScene() != null) {
            centerFormOnStage();
        }
    }
}