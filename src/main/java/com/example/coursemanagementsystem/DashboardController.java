package com.example.coursemanagementsystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DashboardController {

    private Stage stage;
    private Admin admin;

    // Main Panes
    @FXML private AnchorPane courseManagementPane;
    @FXML private AnchorPane InstructorManagementPane;
    @FXML private AnchorPane StudentManagementPane;

    // Course Management Forms and Fields
    @FXML private AnchorPane addCourseForm, removeCourseForm, modifyCourseForm, removeCourseStudentForm, removeCourseInstructorForm, viewCourseForm, viewAllCourseForm;
    @FXML private TextField courseIdField, courseNameField, creditsField, maxStudentsField;
    @FXML private TextField removeCourseIdField;
    @FXML private TextField modifyCourseIdField, modifyCourseNameField, modifyCreditsField, modifyMaxStudentsField;
    @FXML private TextField removeStudentCourseIdField, removeCourseStudentIdField;
    @FXML private TextField removeInstructorCourseIdField;
    @FXML private TextField viewCourseIdField;
    @FXML private TextArea courseDataTextArea, removeCourseDataTextArea, modifyCourseDataTextArea, removeStudentStatusArea, removeInstructorStatusArea, courseDetailsArea, allCoursesArea;

    // Instructor Management Forms and Fields
    @FXML private AnchorPane addInstructorForm, removeInstructorForm, modifyInstructorForm, viewInstructorForm, assignInstructorForm;
    @FXML private TextField instructorIdField, instructorNameField, instructorEmailField;
    @FXML private TextField removeInstructorIdField;
    @FXML private TextField modifyInstructorIdField, modifyInstructorNameField, modifyInstructorEmailField, modifyInstructorCourseField;
    @FXML private TextField viewInstructorIdField;
    @FXML private TextField assignCourseIdField, assignInstructorIdField;
    @FXML private TextArea instructorDataTextArea, removeInstructorDataTextArea, modifyInstructorDataTextArea, instructorDetailsArea, assignInstructorDataTextArea;

    // Student Management Forms and Fields
    @FXML private AnchorPane addStudentForm, removeStudentForm, modifyStudentForm, viewStudentForm, enrollStudentForm;
    @FXML private TextField studentIdField, studentNameField, studentEmailField;
    @FXML private TextField removeStudentIdField;
    @FXML private TextField modifyStudentIdField, modifyStudentNameField, modifyStudentEmailField;
    @FXML private TextField viewStudentIdField;
    @FXML private TextField enrollCourseIdField, enrollStudentIdField;
    @FXML private TextArea studentDataTextArea, removeStudentDataTextArea, modifyStudentDataTextArea, studentDetailsArea, enrollStudentDataTextArea;

    // Sidebar Labels
    @FXML private Label headerLabel;
    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        if (admin == null) {
            System.out.println("Admin not set yet. Waiting for setAdmin().");
        } else {
            closeAllPanes();
            courseManagementPane.setVisible(true);
            addCourseForm.setVisible(true);
        }
        Arrays.asList(courseDataTextArea, removeCourseDataTextArea, modifyCourseDataTextArea, removeStudentStatusArea,
                        removeInstructorStatusArea, courseDetailsArea, allCoursesArea, instructorDataTextArea,
                        removeInstructorDataTextArea, modifyInstructorDataTextArea, instructorDetailsArea,
                        assignInstructorDataTextArea, studentDataTextArea, removeStudentDataTextArea,
                        modifyStudentDataTextArea, studentDetailsArea, enrollStudentDataTextArea)
                .forEach(textArea -> textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12;"));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
        if (admin != null && courseManagementPane != null) {
            closeAllPanes();
            courseManagementPane.setVisible(true);
            addCourseForm.setVisible(true);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private String toUpperCaseId(String id) {
        return id != null ? id.toUpperCase() : null;
    }

    private String capitalizeName(String name) {
        return name == null || name.isEmpty() ? name :
                name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    private String formatExcelTable(String[] headers, String[] data) {
        int[] colWidths = headers.length == 3 ? new int[]{15, 20, 30} :
                headers.length == 4 && headers[0].equals("S.No") ? new int[]{10, 15, 20, 30} :
                        headers.length == 4 ? new int[]{15, 20, 15, 20} :
                                headers.length == 5 ? new int[]{15, 20, 10, 15, 20} : new int[]{15, 20, 10, 15};

        return Arrays.stream(new String[]{
                "+" + String.join("", Arrays.stream(colWidths).mapToObj(w -> "-".repeat(w) + "+").toArray(String[]::new)),
                "|" + String.join("|", Arrays.stream(headers).map(h -> String.format("%-" + colWidths[Arrays.asList(headers).indexOf(h)] + "s", h)).toArray(String[]::new)) + "|",
                "+" + String.join("", Arrays.stream(colWidths).mapToObj(w -> "-".repeat(w) + "+").toArray(String[]::new)),
                "|" + String.join("|", Arrays.stream(data).map(d -> String.format("%-" + colWidths[Arrays.asList(data).indexOf(d)] + "s",
                        d != null && d.length() > colWidths[Arrays.asList(data).indexOf(d)] ? d.substring(0, colWidths[Arrays.asList(data).indexOf(d)]) : d != null ? d : "None")).toArray(String[]::new)) + "|",
                "+" + String.join("", Arrays.stream(colWidths).mapToObj(w -> "-".repeat(w) + "+").toArray(String[]::new))
        }).collect(Collectors.joining("\n"));
    }

    // Sidebar Event Handlers
    @FXML private void showCoursePane() { closeAllPanes(); courseManagementPane.setVisible(true); addCourseForm.setVisible(true); }
    @FXML private void showInstructorPane() { closeAllPanes(); InstructorManagementPane.setVisible(true); addInstructorForm.setVisible(true); }
    @FXML private void showStudentPane() { closeAllPanes(); StudentManagementPane.setVisible(true); addStudentForm.setVisible(true); }

    @FXML
    private void logout() throws IOException {
        stage.close();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/coursemanagementsystem/Login.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.setTitle("Course Management System - Login");
        stage.show();
    }

    private void closeAllPanes() {
        Arrays.asList(courseManagementPane, InstructorManagementPane, StudentManagementPane)
                .forEach(pane -> pane.setVisible(false));
        closeCourseForms();
        closeInstructorForms();
        closeStudentForms();
    }

    // Course Management Event Handlers
    @FXML private void addCourseAction() { closeCourseForms(); clearCourseTextArea(); addCourseForm.setVisible(true); }
    @FXML private void removeCourseAction() { closeCourseForms(); clearCourseTextArea(); removeCourseForm.setVisible(true); }
    @FXML private void modifyCourseAction() { closeCourseForms(); clearCourseTextArea(); modifyCourseForm.setVisible(true); }
    @FXML private void removeCourseStudentAction() { closeCourseForms(); clearCourseTextArea(); removeCourseStudentForm.setVisible(true); }
    @FXML private void removeCourseInstructorAction() { closeCourseForms(); clearCourseTextArea(); removeCourseInstructorForm.setVisible(true); }
    @FXML private void allCoursesAction() { closeCourseForms(); clearCourseTextArea(); viewAllCourseForm.setVisible(true); }
    @FXML private void closeaction() { closeAllPanes(); clearCourseTextArea(); }
    @FXML private void viewCourseAction() { closeCourseForms(); clearCourseTextArea(); viewCourseForm.setVisible(true); }

    private void closeCourseForms() {
        Arrays.asList(addCourseForm, removeCourseForm, modifyCourseForm, removeCourseStudentForm,
                        removeCourseInstructorForm, viewCourseForm, viewAllCourseForm)
                .forEach(form -> form.setVisible(false));
        clearCourseFields();
    }

    private void clearCourseFields() {
        Arrays.asList(courseIdField, courseNameField, creditsField, maxStudentsField, removeCourseIdField,
                        modifyCourseIdField, modifyCourseNameField, modifyCreditsField, modifyMaxStudentsField,
                        removeStudentCourseIdField, removeCourseStudentIdField, removeInstructorCourseIdField,
                        viewCourseIdField)
                .forEach(TextField::clear);
    }

    private void clearCourseTextArea() {
        Arrays.asList(courseDataTextArea, removeCourseDataTextArea, modifyCourseDataTextArea,
                        removeStudentStatusArea, removeInstructorStatusArea, courseDetailsArea, allCoursesArea)
                .forEach(TextArea::clear);
    }

    @FXML
    private void addCourse() {
        try {
            String courseId = toUpperCaseId(courseIdField.getText().trim());
            String courseName = capitalizeName(courseNameField.getText().trim());
            int credits = Integer.parseInt(creditsField.getText().trim());
            int maxStudents = Integer.parseInt(maxStudentsField.getText().trim());
            if (courseId.isEmpty() || courseName.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Course ID and name cannot be empty.");
                return;
            }
            Course course = new Course(courseId, courseName, credits, maxStudents);
            admin.addCourse(course);
            Course addedCourse = admin.getCourseDetails(courseId);
            courseDataTextArea.setText("Course Added:\n" + formatExcelTable(
                    new String[]{"Course ID", "Name", "Credits", "Max Students", "Instructor"},
                    new String[]{addedCourse.getCourseId(), addedCourse.getCourseName(),
                            String.valueOf(addedCourse.getCredits()), String.valueOf(addedCourse.getMaxStudents()),
                            addedCourse.getInstructorId() != null ? addedCourse.getInstructorId() : "None"}
            ));
            showAlert(Alert.AlertType.INFORMATION, "Success", "Course added successfully.");
            clearCourseFields();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid credits or max students value.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void removeCourse() {
        String courseId = toUpperCaseId(removeCourseIdField.getText().trim());
        if (courseId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Course ID cannot be empty.");
            return;
        }
        try {
            Course course = admin.getCourseDetails(courseId);
            if (course == null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Course not found.");
                return;
            }
            admin.removeCourse(courseId);
            removeCourseDataTextArea.setText("Course Removed:\n" + formatExcelTable(
                    new String[]{"Course ID", "Name", "Credits", "Max Students", "Instructor"},
                    new String[]{course.getCourseId(), course.getCourseName(), String.valueOf(course.getCredits()),
                            String.valueOf(course.getMaxStudents()), course.getInstructorId() != null ? course.getInstructorId() : "None"}
            ));
            showAlert(Alert.AlertType.INFORMATION, "Success", "Course removed successfully.");
            clearCourseFields();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void loadCourseDetails() {
        String courseId = toUpperCaseId(modifyCourseIdField.getText().trim());
        if (courseId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Course ID cannot be empty.");
            return;
        }
        try {
            Course course = admin.getCourseDetails(courseId);
            if (course != null) {
                Arrays.asList(
                        (Runnable) () -> modifyCourseNameField.setText(course.getCourseName()),
                        (Runnable) () -> modifyCreditsField.setText(String.valueOf(course.getCredits())),
                        (Runnable) () -> modifyMaxStudentsField.setText(String.valueOf(course.getMaxStudents()))
                ).forEach(Runnable::run);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Course details loaded.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "Course not found.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void modifyCourse() {
        try {
            String courseId = toUpperCaseId(modifyCourseIdField.getText().trim());
            String courseName = capitalizeName(modifyCourseNameField.getText().trim());
            int credits = Integer.parseInt(modifyCreditsField.getText().trim());
            int maxStudents = Integer.parseInt(modifyMaxStudentsField.getText().trim());
            if (courseId.isEmpty() || courseName.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Course ID and name cannot be empty.");
                return;
            }
            Course course = new Course(courseId, courseName, credits, maxStudents);
            admin.modifyCourse(course);
            Course updatedCourse = admin.getCourseDetails(courseId);
            modifyCourseDataTextArea.setText("Course Updated:\n" + formatExcelTable(
                    new String[]{"Course ID", "Name", "Credits", "Max Students", "Instructor"},
                    new String[]{updatedCourse.getCourseId(), updatedCourse.getCourseName(),
                            String.valueOf(updatedCourse.getCredits()), String.valueOf(updatedCourse.getMaxStudents()),
                            updatedCourse.getInstructorId() != null ? updatedCourse.getInstructorId() : "None"}
            ));
            showAlert(Alert.AlertType.INFORMATION, "Success", "Course updated successfully.");
            clearCourseFields();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid credits or max students value.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void removeStudentFromCourse() {
        String courseId = toUpperCaseId(removeStudentCourseIdField.getText().trim());
        String studentId = toUpperCaseId(removeCourseStudentIdField.getText().trim());
        if (courseId.isEmpty() || studentId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Course ID and Student ID cannot be empty.");
            return;
        }
        try {
            Student student = admin.getStudentDetails(studentId);
            if (student == null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Student not found.");
                return;
            }
            admin.removeStudentFromCourse(courseId, studentId);
            removeStudentStatusArea.setText("Student Removed from Course " + courseId + ":\n" + formatExcelTable(
                    new String[]{"Student ID", "Name", "Email"},
                    new String[]{student.getStudentId(), student.getName(), student.getEmail()}
            ));
            showAlert(Alert.AlertType.INFORMATION, "Success", "Student " + studentId + " removed from course " + courseId + ".");
            clearCourseFields();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void removeInstructorFromCourse() {
        String courseId = toUpperCaseId(removeInstructorCourseIdField.getText().trim());
        if (courseId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Course ID cannot be empty.");
            return;
        }
        try {
            Course course = admin.getCourseDetails(courseId);
            if (course == null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Course not found.");
                return;
            }
            String instructorId = course.getInstructorId();
            Instructor instructor = instructorId != null ? admin.getInstructorDetails(instructorId) : null;
            admin.removeInstructorFromCourse(courseId);
            removeInstructorStatusArea.setText("Instructor Removed from Course " + courseId + ":\n" +
                    (instructor != null ? formatExcelTable(
                            new String[]{"Instructor ID", "Name", "Email"},
                            new String[]{instructor.getInstructorId(), instructor.getName(), instructor.getEmail()}
                    ) : "No instructor was assigned.\n"));
            showAlert(Alert.AlertType.INFORMATION, "Success", "Instructor removed from course " + courseId + ".");
            clearCourseFields();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void viewCourse() {
        String courseId = toUpperCaseId(viewCourseIdField.getText().trim());
        if (courseId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Course ID cannot be empty.");
            viewCourseIdField.clear();
            return;
        }
        try {
            Course course = admin.getCourseDetails(courseId);
            if (course == null) {
                viewCourseIdField.clear();
                courseDetailsArea.clear();
                showAlert(Alert.AlertType.ERROR, "Error", "Course with ID " + courseId + " not found.");
                return;
            }

            courseDetailsArea.clear();
            courseDetailsArea.appendText("Course Details:\n" + formatExcelTable(
                    new String[]{"Course ID", "Name", "Credits", "Max Students", "Instructor"},
                    new String[]{course.getCourseId(), course.getCourseName(), String.valueOf(course.getCredits()),
                            String.valueOf(course.getMaxStudents()), course.getInstructorId() != null ? course.getInstructorId() : "None"}
            ));

            String sql = "SELECT student_id FROM course_students WHERE course_id = ?";
            final int[] studentCount = {0};
            StringBuilder studentDetails = new StringBuilder();
            DatabaseUtil.executeQuery(sql, rs -> {
                try {
                    int[] colWidths = {10, 15, 20, 30};
                    String[] headers = {"S.No", "Student ID", "Name", "Email"};
                    studentDetails.append(Arrays.stream(colWidths).mapToObj(w -> "+" + "-".repeat(w)).collect(Collectors.joining("")) + "+\n");
                    studentDetails.append(Arrays.stream(headers).map(h -> String.format("%-" + colWidths[Arrays.asList(headers).indexOf(h)] + "s", h))
                            .collect(Collectors.joining("|", "|", "|\n")));
                    studentDetails.append(Arrays.stream(colWidths).mapToObj(w -> "+" + "-".repeat(w)).collect(Collectors.joining("")) + "+\n");

                    while (rs.next()) {
                        String studentId = rs.getString("student_id");
                        Student student = admin.getStudentDetails(studentId);
                        if (student != null) {
                            studentCount[0]++;
                            studentDetails.append("|").append(String.format("%-10s", studentCount[0])).append("|")
                                    .append(String.format("%-15s", student.getStudentId())).append("|")
                                    .append(String.format("%-20s", student.getName().length() > 20 ? student.getName().substring(0, 20) : student.getName())).append("|")
                                    .append(String.format("%-30s", student.getEmail().length() > 30 ? student.getEmail().substring(0, 30) : student.getEmail())).append("|\n");
                            studentDetails.append(Arrays.stream(colWidths).mapToObj(w -> "+" + "-".repeat(w)).collect(Collectors.joining("")) + "+\n");
                        }
                    }
                    if (studentCount[0] > 0) studentDetails.setLength(studentDetails.length() - (Arrays.stream(colWidths).sum() + colWidths.length + 1));
                } catch (SQLException e) {
                    throw new RuntimeException("Error fetching student details: " + e.getMessage(), e);
                }
            }, courseId);

            courseDetailsArea.appendText("\nStudents Enrolled: " + studentCount[0] + "/" + course.getMaxStudents() + "\n");
            if (studentCount[0] > 0) courseDetailsArea.appendText("Enrolled Students:\n" + studentDetails.toString());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to view course: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void displayAllCourses() {
        try {
            ArrayList<Course> courses = admin.getAllCourses();
            allCoursesArea.clear();
            if (courses.isEmpty()) {
                allCoursesArea.setText("No courses available.");
            } else {
                allCoursesArea.appendText("All Courses:\n");
                int[] colWidths = {15, 20, 10, 15, 20};
                String[] headers = {"Course ID", "Name", "Credits", "Max Students", "Instructor"};
                allCoursesArea.appendText(Arrays.stream(colWidths).mapToObj(w -> "+" + "-".repeat(w)).collect(Collectors.joining("")) + "+\n");
                allCoursesArea.appendText(Arrays.stream(headers).map(h -> String.format("%-" + colWidths[Arrays.asList(headers).indexOf(h)] + "s", h))
                        .collect(Collectors.joining("|", "|", "|\n")));
                allCoursesArea.appendText(Arrays.stream(colWidths).mapToObj(w -> "+" + "-".repeat(w)).collect(Collectors.joining("")) + "+\n");

                courses.forEach(course -> {
                    allCoursesArea.appendText("|");
                    Arrays.asList(course.getCourseId(), course.getCourseName(), String.valueOf(course.getCredits()),
                                    String.valueOf(course.getMaxStudents()), course.getInstructorId() != null ? course.getInstructorId() : "None")
                            .forEach(value -> allCoursesArea.appendText(String.format("%-" + colWidths[Arrays.asList(headers).indexOf(headers[Arrays.asList(course.getCourseId(), course.getCourseName(),
                                            String.valueOf(course.getCredits()), String.valueOf(course.getMaxStudents()), course.getInstructorId() != null ? course.getInstructorId() : "None").indexOf(value)])] + "s",
                                    value.length() > colWidths[Arrays.asList(headers).indexOf(headers[Arrays.asList(course.getCourseId(), course.getCourseName(), String.valueOf(course.getCredits()),
                                            String.valueOf(course.getMaxStudents()), course.getInstructorId() != null ? course.getInstructorId() : "None").indexOf(value)])] ? value.substring(0, colWidths[Arrays.asList(headers).indexOf(headers[Arrays.asList(course.getCourseId(),
                                            course.getCourseName(), String.valueOf(course.getCredits()), String.valueOf(course.getMaxStudents()), course.getInstructorId() != null ? course.getInstructorId() : "None").indexOf(value)])]) : value) + "|"));
                    allCoursesArea.appendText("\n" + Arrays.stream(colWidths).mapToObj(w -> "+" + "-".repeat(w)).collect(Collectors.joining("")) + "+\n");
                });
                allCoursesArea.setText(allCoursesArea.getText().substring(0, allCoursesArea.getText().length() - (Arrays.stream(colWidths).sum() + colWidths.length + 1)));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    // Instructor Management Event Handlers
    @FXML private void addInstructorAction() { closeInstructorForms(); clearInstructorTextArea(); addInstructorForm.setVisible(true); }
    @FXML private void removeInstructorAction() { closeInstructorForms(); clearInstructorTextArea(); removeInstructorForm.setVisible(true); }
    @FXML private void modifyInstructorAction() { closeInstructorForms(); clearInstructorTextArea(); modifyInstructorForm.setVisible(true); }
    @FXML private void viewInstructorAction() { closeInstructorForms(); clearInstructorTextArea(); viewInstructorForm.setVisible(true); }
    @FXML private void assignInstructorAction() { closeInstructorForms(); clearInstructorTextArea(); assignInstructorForm.setVisible(true); }

    private void closeInstructorForms() {
        Arrays.asList(addInstructorForm, removeInstructorForm, modifyInstructorForm, viewInstructorForm, assignInstructorForm)
                .forEach(form -> form.setVisible(false));
        clearInstructorFields();
    }

    private void clearInstructorFields() {
        Arrays.asList(instructorIdField, instructorNameField, instructorEmailField, removeInstructorIdField,
                        modifyInstructorIdField, modifyInstructorNameField, modifyInstructorEmailField,
                        modifyInstructorCourseField, viewInstructorIdField, assignCourseIdField, assignInstructorIdField)
                .forEach(TextField::clear);
    }

    private void clearInstructorTextArea() {
        Arrays.asList(instructorDataTextArea, removeInstructorDataTextArea, modifyInstructorDataTextArea,
                        instructorDetailsArea, assignInstructorDataTextArea)
                .forEach(TextArea::clear);
    }

    @FXML
    private void addInstructor() {
        String instructorId = toUpperCaseId(instructorIdField.getText().trim());
        String name = capitalizeName(instructorNameField.getText().trim());
        String email = instructorEmailField.getText().trim();
        if (instructorId.isEmpty() || name.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields must be filled.");
            return;
        }
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid email format.");
            return;
        }
        try {
            Instructor instructor = new Instructor(instructorId, name, email);
            admin.addInstructor(instructor);
            Instructor addedInstructor = admin.getInstructorDetails(instructorId);
            instructorDataTextArea.setText("Instructor Added:\n" + formatExcelTable(
                    new String[]{"Instructor ID", "Name", "Email"},
                    new String[]{addedInstructor.getInstructorId(), addedInstructor.getName(), addedInstructor.getEmail()}
            ));
            showAlert(Alert.AlertType.INFORMATION, "Success", "Instructor added successfully.");
            clearInstructorFields();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void removeInstructor() {
        String instructorId = toUpperCaseId(removeInstructorIdField.getText().trim());
        if (instructorId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Instructor ID cannot be empty.");
            return;
        }
        try {
            Instructor instructor = admin.getInstructorDetails(instructorId);
            if (instructor == null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Instructor not found.");
                return;
            }
            admin.removeInstructor(instructorId);
            removeInstructorDataTextArea.setText("Instructor Removed:\n" + formatExcelTable(
                    new String[]{"Instructor ID", "Name", "Email"},
                    new String[]{instructor.getInstructorId(), instructor.getName(), instructor.getEmail()}
            ));
            showAlert(Alert.AlertType.INFORMATION, "Success", "Instructor removed successfully.");
            clearInstructorFields();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void loadInstructorDetails() {
        String instructorId = toUpperCaseId(modifyInstructorIdField.getText().trim());
        if (instructorId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Instructor ID cannot be empty.");
            return;
        }
        try {
            Instructor instructor = admin.getInstructorDetails(instructorId);
            if (instructor != null) {
                Arrays.asList(
                        (Runnable) () -> modifyInstructorNameField.setText(instructor.getName()),
                        (Runnable) () -> modifyInstructorEmailField.setText(instructor.getEmail())
                ).forEach(Runnable::run);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Instructor details loaded.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "Instructor not found.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void modifyInstructor() {
        String instructorId = toUpperCaseId(modifyInstructorIdField.getText().trim());
        String name = capitalizeName(modifyInstructorNameField.getText().trim());
        String email = modifyInstructorEmailField.getText().trim();
        if (instructorId.isEmpty() || name.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields must be filled.");
            return;
        }
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid email format.");
            return;
        }
        try {
            Instructor instructor = new Instructor(instructorId, name, email);
            admin.modifyInstructor(instructor);
            Instructor updatedInstructor = admin.getInstructorDetails(instructorId);
            modifyInstructorDataTextArea.setText("Instructor Updated:\n" + formatExcelTable(
                    new String[]{"Instructor ID", "Name", "Email"},
                    new String[]{updatedInstructor.getInstructorId(), updatedInstructor.getName(), updatedInstructor.getEmail()}
            ));
            showAlert(Alert.AlertType.INFORMATION, "Success", "Instructor updated successfully.");
            clearInstructorFields();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void viewInstructor() {
        String instructorId = toUpperCaseId(viewInstructorIdField.getText().trim());
        if (instructorId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Instructor ID cannot be empty.");
            return;
        }
        try {
            Instructor instructor = admin.getInstructorDetails(instructorId);
            if (instructor != null) {
                instructorDetailsArea.setText("Instructor Details:\n" + formatExcelTable(
                        new String[]{"Instructor ID", "Name", "Email"},
                        new String[]{instructor.getInstructorId(), instructor.getName(), instructor.getEmail()}
                ));
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "Instructor not found.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void assignInstructor() {
        String courseId = toUpperCaseId(assignCourseIdField.getText().trim());
        String instructorId = toUpperCaseId(assignInstructorIdField.getText().trim());
        if (courseId.isEmpty() || instructorId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Course ID and Instructor ID cannot be empty.");
            return;
        }
        try {
            admin.assignInstructor(courseId, instructorId);
            Course course = admin.getCourseDetails(courseId);
            Instructor instructor = admin.getInstructorDetails(instructorId);
            assignInstructorDataTextArea.setText("Instructor Assigned:\n" + formatExcelTable(
                    new String[]{"Course ID", "Course Name", "Instructor ID", "Instructor Name"},
                    new String[]{course.getCourseId(), course.getCourseName(), instructor.getInstructorId(), instructor.getName()}
            ));
            showAlert(Alert.AlertType.INFORMATION, "Success", "Instructor " + instructorId + " assigned to course " + courseId + ".");
            clearInstructorFields();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    // Student Management Event Handlers
    @FXML private void addStudentAction() { closeStudentForms(); clearStudentTextArea(); addStudentForm.setVisible(true); }
    @FXML private void removeStudentAction() { closeStudentForms(); clearStudentTextArea(); removeStudentForm.setVisible(true); }
    @FXML private void modifyStudentAction() { closeStudentForms(); clearStudentTextArea(); modifyStudentForm.setVisible(true); }
    @FXML private void viewStudentAction() { closeStudentForms(); clearStudentTextArea(); viewStudentForm.setVisible(true); }
    @FXML private void enrollStudentAction() { closeStudentForms(); clearStudentTextArea(); enrollStudentForm.setVisible(true); }

    private void closeStudentForms() {
        Arrays.asList(addStudentForm, removeStudentForm, modifyStudentForm, viewStudentForm, enrollStudentForm)
                .forEach(form -> form.setVisible(false));
        clearStudentFields();
    }

    private void clearStudentFields() {
        Arrays.asList(studentIdField, studentNameField, studentEmailField, removeStudentIdField,
                        modifyStudentIdField, modifyStudentNameField, modifyStudentEmailField,
                        viewStudentIdField, enrollCourseIdField, enrollStudentIdField)
                .forEach(TextField::clear);
    }

    private void clearStudentTextArea() {
        Arrays.asList(studentDataTextArea, removeStudentDataTextArea, modifyStudentDataTextArea,
                        studentDetailsArea, enrollStudentDataTextArea)
                .forEach(TextArea::clear);
    }

    @FXML
    private void addStudent() {
        String studentId = toUpperCaseId(studentIdField.getText().trim());
        String name = capitalizeName(studentNameField.getText().trim());
        String email = studentEmailField.getText().trim();
        if (studentId.isEmpty() || name.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields must be filled.");
            return;
        }
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid email format.");
            return;
        }
        try {
            Student student = new Student(studentId, name, email);
            admin.addStudent(student);
            Student addedStudent = admin.getStudentDetails(studentId);
            studentDataTextArea.setText("Student Added:\n" + formatExcelTable(
                    new String[]{"Student ID", "Name", "Email"},
                    new String[]{addedStudent.getStudentId(), addedStudent.getName(), addedStudent.getEmail()}
            ));
            showAlert(Alert.AlertType.INFORMATION, "Success", "Student added successfully.");
            clearStudentFields();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void removeStudent() {
        String studentId = toUpperCaseId(removeStudentIdField.getText().trim());
        if (studentId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Student ID cannot be empty.");
            return;
        }
        try {
            Student student = admin.getStudentDetails(studentId);
            if (student == null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Student not found.");
                return;
            }
            admin.removeStudent(studentId);
            removeStudentDataTextArea.setText("Student Removed:\n" + formatExcelTable(
                    new String[]{"Student ID", "Name", "Email"},
                    new String[]{student.getStudentId(), student.getName(), student.getEmail()}
            ));
            showAlert(Alert.AlertType.INFORMATION, "Success", "Student removed successfully.");
            clearStudentFields();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void loadStudentDetails() {
        String studentId = toUpperCaseId(modifyStudentIdField.getText().trim());
        if (studentId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Student ID cannot be empty.");
            return;
        }
        try {
            Student student = admin.getStudentDetails(studentId);
            if (student != null) {
                Arrays.asList(
                        (Runnable) () -> modifyStudentNameField.setText(student.getName()),
                        (Runnable) () -> modifyStudentEmailField.setText(student.getEmail())
                ).forEach(Runnable::run);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Student details loaded.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "Student not found.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void modifyStudent() {
        String studentId = toUpperCaseId(modifyStudentIdField.getText().trim());
        String name = capitalizeName(modifyStudentNameField.getText().trim());
        String email = modifyStudentEmailField.getText().trim();
        if (studentId.isEmpty() || name.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields must be filled.");
            return;
        }
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid email format.");
            return;
        }
        try {
            Student student = new Student(studentId, name, email);
            admin.modifyStudent(student);
            Student updatedStudent = admin.getStudentDetails(studentId);
            modifyStudentDataTextArea.setText("Student Updated:\n" + formatExcelTable(
                    new String[]{"Student ID", "Name", "Email"},
                    new String[]{updatedStudent.getStudentId(), updatedStudent.getName(), updatedStudent.getEmail()}
            ));
            showAlert(Alert.AlertType.INFORMATION, "Success", "Student updated successfully.");
            clearStudentFields();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void viewStudent() {
        String studentId = toUpperCaseId(viewStudentIdField.getText().trim());
        if (studentId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Student ID cannot be empty.");
            return;
        }
        try {
            Student student = admin.getStudentDetails(studentId);
            if (student == null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Student not found.");
                return;
            }

            studentDetailsArea.clear();
            studentDetailsArea.appendText("Student Details:\n" + formatExcelTable(
                    new String[]{"Student ID", "Name", "Email"},
                    new String[]{student.getStudentId(), student.getName(), student.getEmail()}
            ));

            ArrayList<Course> enrolledCourses = admin.getEnrolledCoursesForStudent(studentId);
            studentDetailsArea.appendText("\nEnrolled Courses: " + enrolledCourses.size() + "/6\n");
            if (!enrolledCourses.isEmpty()) {
                studentDetailsArea.appendText("Courses Enrolled:\n");
                int[] colWidths = {15, 20, 10, 15};
                String[] headers = {"Course ID", "Course Name", "Credits", "Instructor"};
                studentDetailsArea.appendText(Arrays.stream(colWidths).mapToObj(w -> "+" + "-".repeat(w)).collect(Collectors.joining("")) + "+\n");
                studentDetailsArea.appendText(Arrays.stream(headers).map(h -> String.format("%-" + colWidths[Arrays.asList(headers).indexOf(h)] + "s", h))
                        .collect(Collectors.joining("|", "|", "|\n")));
                studentDetailsArea.appendText(Arrays.stream(colWidths).mapToObj(w -> "+" + "-".repeat(w)).collect(Collectors.joining("")) + "+\n");

                enrolledCourses.forEach(course -> {
                    studentDetailsArea.appendText("|");
                    Arrays.asList(course.getCourseId(), course.getCourseName(), String.valueOf(course.getCredits()),
                                    course.getInstructorId() != null ? course.getInstructorId() : "None")
                            .forEach(value -> studentDetailsArea.appendText(String.format("%-" + colWidths[Arrays.asList(headers).indexOf(headers[Arrays.asList(course.getCourseId(),
                                            course.getCourseName(), String.valueOf(course.getCredits()), course.getInstructorId() != null ? course.getInstructorId() : "None").indexOf(value)])] + "s",
                                    value.length() > colWidths[Arrays.asList(headers).indexOf(headers[Arrays.asList(course.getCourseId(), course.getCourseName(), String.valueOf(course.getCredits()),
                                            course.getInstructorId() != null ? course.getInstructorId() : "None").indexOf(value)])] ? value.substring(0, colWidths[Arrays.asList(headers).indexOf(headers[Arrays.asList(course.getCourseId(),
                                            course.getCourseName(), String.valueOf(course.getCredits()), course.getInstructorId() != null ? course.getInstructorId() : "None").indexOf(value)])]) : value) + "|"));
                    studentDetailsArea.appendText("\n" + Arrays.stream(colWidths).mapToObj(w -> "+" + "-".repeat(w)).collect(Collectors.joining("")) + "+\n");
                });
                studentDetailsArea.setText(studentDetailsArea.getText().substring(0, studentDetailsArea.getText().length() - (Arrays.stream(colWidths).sum() + colWidths.length + 1)));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void enrollStudent() {
        String courseId = toUpperCaseId(enrollCourseIdField.getText().trim());
        String studentId = toUpperCaseId(enrollStudentIdField.getText().trim());
        if (courseId.isEmpty() || studentId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Course ID and Student ID cannot be empty.");
            return;
        }
        try {
            admin.enrollStudent(courseId, studentId);
            Student student = admin.getStudentDetails(studentId);
            Course course = admin.getCourseDetails(courseId);
            enrollStudentDataTextArea.setText("Student Enrolled:\n" + formatExcelTable(
                    new String[]{"Student ID", "Student Name", "Course ID", "Course Name"},
                    new String[]{student.getStudentId(), student.getName(), course.getCourseId(), course.getCourseName()}
            ));
            showAlert(Alert.AlertType.INFORMATION, "Success", "Student " + studentId + " enrolled in course " + courseId + ".");
            clearStudentFields();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }
}