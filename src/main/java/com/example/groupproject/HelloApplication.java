package com.example.groupproject;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
//JDBC
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;



public class HelloApplication extends Application {

    private ComboBox<String> courseNames;
    private ObservableList<String> courses;
    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();

        //  Comb box for courses
        courses = getCourseNamesFromDatabase();
        courseNames = new ComboBox<>(courses);

        //Radio Buttons
        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton preferencesA = new RadioButton("Weekdays");
        RadioButton preferencesB = new RadioButton("Weekends");
        preferencesA.setToggleGroup(toggleGroup);
        preferencesB.setToggleGroup(toggleGroup);

         // Check Box for Student Status
        CheckBox studentStatus = new CheckBox("    (Check if Yes)");



        HBox preferenceRadioButtons = new HBox(preferencesA, preferencesB);
        preferenceRadioButtons.setSpacing(10);
        TextField txt1 = new TextField();
        TextField txt2 = new TextField();

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        gridPane.add(new Label("Student Number:"), 0, 0);
        gridPane.add(txt1, 1, 0);

        gridPane.add(new Label("Course Name:"), 0, 1);
        gridPane.add(courseNames, 1, 1);

        gridPane.add(new Label("Class Preference:"), 0, 2);
        gridPane.add(preferenceRadioButtons, 1, 2);

        gridPane.add(new Label("Are you an International Student: "), 0, 3);
        gridPane.add(studentStatus, 1, 3);

        gridPane.add(new Label("Enter your Email ID:"), 0, 4);
        gridPane.add(txt2, 1, 4);

        Button registerButton = new Button("Register");
        Button modifyButton = new Button("Modify");
        Button deleteButton = new Button("Delete");

        HBox hBox = new HBox(10);
        hBox.getChildren().addAll(registerButton, modifyButton, deleteButton);

        gridPane.add(hBox, 0, 5, 2, 1);

        // Register button event handler
        registerButton.setOnAction(e -> {
            int studentNumber = Integer.parseInt(txt1.getText());
            String selectedCourse = courseNames.getValue();
            String classPreference = preferencesA.isSelected() ? "Weekdays" : "Weekends";
            boolean isInternationalStudent = studentStatus.isSelected();
            String email = txt2.getText();

            // Calling the method to register the course
            registerCourse(studentNumber, selectedCourse, classPreference, isInternationalStudent, email);
        });

        // Modify button event handler
        modifyButton.setOnAction(e -> {
            int studentNumber = Integer.parseInt(txt1.getText());
            String selectedCourse = courseNames.getValue();
            String classPreference = preferencesA.isSelected() ? "Weekdays" : "Weekends";
            boolean isInternationalStudent = studentStatus.isSelected();
            String email = txt2.getText();

            // Calling the method to modify (update) the course
            modifyCourse(studentNumber, selectedCourse, classPreference, isInternationalStudent, email);
        });

        //Delete button event handler
        deleteButton.setOnAction(e -> {
            int studentNumber = Integer.parseInt(txt1.getText());
            String courseName = courseNames.getValue();
            String classPreference = preferencesA.isSelected() ? "Weekdays" : "Weekends";
            boolean isInternationalStudent = studentStatus.isPressed() ;
            String email = txt2.getText();
            // Calling the method to delete the course
            deleteCourse(studentNumber,courseName,classPreference, isInternationalStudent, email);
        });

        root.setCenter(gridPane);

        Scene scene = new Scene(root, 500, 300);

        primaryStage.setTitle("Student Registration");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private void modifyCourse(int studentNumber, String courseName, String classPreference, boolean isInternationalStudent, String email) {
        String jdbcUrl = "jdbc:oracle:thin:@199.212.26.208:1521:SQLD";
        String username = "COMP228_F23_YUV_18";
        String password = "Balaji2005";

        try (Connection connection = DriverManager.getConnection(jdbcUrl,username,password)) {
            // Update details in Registered_Courses table
            String updateQuery = "UPDATE Registered_Courses SET Course_Name = ?, Class_Preference = ?, International_Student = ?, Email = ? WHERE Student_Number = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                preparedStatement.setString(1, courseName);
                preparedStatement.setString(2, classPreference);
                preparedStatement.setBoolean(3, isInternationalStudent);
                preparedStatement.setString(4, email);
                preparedStatement.setInt(5, studentNumber);

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Course Modification", "Modification successful!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Course Modification", "Modification failed. Student not found.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error connecting to the database.");
        }
    }



    private void registerCourse(int studentNumber, String courseName, String classPreference,
                                boolean isInternationalStudent, String email) {
        String jdbcUrl = "jdbc:oracle:thin:@199.212.26.208:1521:SQLD";
        String username = "COMP228_F23_YUV_18";
        String password = "Balaji2005";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Fetch First_Name and Last_Name from Students table
            String studentQuery = "SELECT First_Name, Last_Name FROM Students WHERE Student_Number = ?";
            try (PreparedStatement studentStatement = connection.prepareStatement(studentQuery)) {
                studentStatement.setInt(1, studentNumber);
                ResultSet studentResult = studentStatement.executeQuery();

                if (studentResult.next()) {
                    String firstName = studentResult.getString("First_Name");
                    String lastName = studentResult.getString("Last_Name");

                    // Fetch Professor_Name from Courses table
                    String courseQuery = "SELECT Professor_Name,COURSE_CODE FROM Courses WHERE Course_Name = ?";
                    try (PreparedStatement courseStatement = connection.prepareStatement(courseQuery)) {

                        courseStatement.setString(1, courseName);
                        ResultSet courseResult = courseStatement.executeQuery();

                        if (courseResult.next()) {
                            String professorName = courseResult.getString("Professor_Name");
                            String Course_code = courseResult.getString("COURSE_CODE");

                            // Insert into Registered_Courses table
                            String insertQuery = "INSERT INTO Registered_Courses (Student_Number, Course_Code, Course_Name, Class_Preference, International_Student, Email, First_Name, Last_Name, Professor_Name) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                                preparedStatement.setInt(1, studentNumber);
                                preparedStatement.setString(2, Course_code);
                                preparedStatement.setString(3, courseName);
                                preparedStatement.setString(4, classPreference);
                                preparedStatement.setBoolean(5, isInternationalStudent);
                                preparedStatement.setString(6, email);
                                preparedStatement.setString(7, firstName);
                                preparedStatement.setString(8, lastName);
                                preparedStatement.setString(9, professorName);

                                int rowsAffected = preparedStatement.executeUpdate();

                                if (rowsAffected > 0) {
                                    showAlert(Alert.AlertType.INFORMATION, "Course Registration", "Registration successful!");
                                } else {
                                    showAlert(Alert.AlertType.ERROR, "Course Registration", "Registration failed.");
                                }
                            }
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Course Not Found", "Course with the given code and name not found.");
                        }
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Student Not Found", "Student with the given number not found.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error connecting to the database.");
        }
    }
    private void deleteCourse(int studentNumber,String courseName,String classPreference,
                              boolean isInternationalStudent, String email) {
        String jdbcUrl = "jdbc:oracle:thin:@199.212.26.208:1521:SQLD";
        String username = "COMP228_F23_YUV_18";
        String password = "Balaji2005";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Delete course from Registered_Courses table
            String deleteQuery = "DELETE FROM Registered_Courses WHERE Student_Number = ? AND Course_Name=? AND CLASS_PREFERENCE= ? AND email= ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
                preparedStatement.setInt(1, studentNumber);
                preparedStatement.setString(2,courseName);
                preparedStatement.setString(3,classPreference);
                preparedStatement.setString(4,email);


                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Course Deletion", "Deletion successful!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Course Deletion", "Deletion failed. Student not found.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error connecting to the database.");
        }
    }
    private ObservableList<String> getCourseNamesFromDatabase() {
        ObservableList<String> courseNames = FXCollections.observableArrayList();

        String jdbcUrl = "jdbc:oracle:thin:@199.212.26.208:1521:SQLD";
        String username = "COMP228_F23_YUV_18";
        String password = "Balaji2005";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            String query = "SELECT Course_Name FROM Courses";
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    String courseName = resultSet.getString("Course_Name");
                    courseNames.add(courseName);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            // Handle the exception (e.g., show an alert)
        }

        return courseNames;
    }



    // Alerts
    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
