package com.example.coursemanagementsystem;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;

public class Admin {

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: " + e.getMessage(), e);
        }
    }

    // Admin Management
    public boolean adminExists() throws SQLException {
        String sql = "SELECT COUNT(*) FROM admin";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (conn == null) throw new SQLException("Database connection failed.");
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    public void signupAdmin(String username, String password) throws SQLException {
        String hashedPassword = hashPassword(password);
        String sql = "INSERT INTO admin (username, password) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                throw new SQLException("Username '" + username + "' is already taken.", e);
            }
            throw new SQLException("Error signing up admin: " + e.getMessage(), e);
        }
    }

    public boolean validateAdmin(String username, String password) throws SQLException {
        String hashedPassword = hashPassword(password);
        String sql = "SELECT * FROM admin WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    // Course Management
    public void addCourse(Course course) throws SQLException {
        String sql = "INSERT INTO courses (course_id, course_name, credits, max_students) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmt.setString(1, course.getCourseId());
            pstmt.setString(2, course.getCourseName());
            pstmt.setInt(3, course.getCredits());
            pstmt.setInt(4, course.getMaxStudents());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                throw new SQLException("Course with ID " + course.getCourseId() + " already exists.", e);
            }
            throw new SQLException("Error adding course: " + e.getMessage(), e);
        }
    }

    public void removeCourse(String courseId) throws SQLException {
        String sqlDeleteEnrollments = "DELETE FROM course_students WHERE course_id = ?";
        String sqlDeleteCourse = "DELETE FROM courses WHERE course_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmtEnrollments = conn.prepareStatement(sqlDeleteEnrollments);
             PreparedStatement pstmtCourse = conn.prepareStatement(sqlDeleteCourse)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            conn.setAutoCommit(false);
            try {
                pstmtEnrollments.setString(1, courseId);
                pstmtEnrollments.executeUpdate();
                pstmtCourse.setString(1, courseId);
                int rowsAffected = pstmtCourse.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Course with ID " + courseId + " does not exist.");
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Course getCourseDetails(String courseId) throws SQLException {
        String sql = "SELECT * FROM courses WHERE course_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmt.setString(1, courseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Course course = new Course(
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getInt("credits"),
                        rs.getInt("max_students")
                );
                course.setInstructorId(rs.getString("instructor_id"));
                return course;
            }
            return null;
        }
    }

    public void modifyCourse(Course course) throws SQLException {
        String sql = "UPDATE courses SET course_name = ?, credits = ?, max_students = ? WHERE course_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmt.setString(1, course.getCourseName());
            pstmt.setInt(2, course.getCredits());
            pstmt.setInt(3, course.getMaxStudents());
            pstmt.setString(4, course.getCourseId());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Course with ID " + course.getCourseId() + " does not exist.");
            }
        }
    }

    public ArrayList<Course> getAllCourses() throws SQLException {
        ArrayList<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses";
        DatabaseUtil.executeQuery(sql, rs -> {
            try {
                while (rs.next()) {
                    Course course = new Course(
                            rs.getString("course_id"),
                            rs.getString("course_name"),
                            rs.getInt("credits"),
                            rs.getInt("max_students")
                    );
                    course.setInstructorId(rs.getString("instructor_id"));
                    courses.add(course);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return courses;
    }

    public void removeStudentFromCourse(String courseId, String studentId) throws SQLException {
        String sql = "DELETE FROM course_students WHERE course_id = ? AND student_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmt.setString(1, courseId);
            pstmt.setString(2, studentId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Student " + studentId + " is not enrolled in course " + courseId + " or course/student does not exist.");
            }
        }
    }

    public void removeInstructorFromCourse(String courseId) throws SQLException {
        String sql = "UPDATE courses SET instructor_id = NULL WHERE course_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmt.setString(1, courseId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Course with ID " + courseId + " does not exist.");
            }
        }
    }

    // Instructor Management
    public void addInstructor(Instructor instructor) throws SQLException {
        String sql = "INSERT INTO instructors (instructor_id, name, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmt.setString(1, instructor.getInstructorId());
            pstmt.setString(2, instructor.getName());
            pstmt.setString(3, instructor.getEmail());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                throw new SQLException("Instructor with ID " + instructor.getInstructorId() + " already exists.", e);
            }
            throw new SQLException("Error adding instructor: " + e.getMessage(), e);
        }
    }

    public void removeInstructor(String instructorId) throws SQLException {
        String sqlUpdateCourses = "UPDATE courses SET instructor_id = NULL WHERE instructor_id = ?";
        String sqlDeleteInstructor = "DELETE FROM instructors WHERE instructor_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdateCourses);
             PreparedStatement pstmtDelete = conn.prepareStatement(sqlDeleteInstructor)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            conn.setAutoCommit(false);
            try {
                pstmtUpdate.setString(1, instructorId);
                pstmtUpdate.executeUpdate();
                pstmtDelete.setString(1, instructorId);
                int rowsAffected = pstmtDelete.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Instructor with ID " + instructorId + " does not exist.");
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Instructor getInstructorDetails(String instructorId) throws SQLException {
        String sql = "SELECT * FROM instructors WHERE instructor_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmt.setString(1, instructorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Instructor(
                        rs.getString("instructor_id"),
                        rs.getString("name"),
                        rs.getString("email")
                );
            }
            return null;
        }
    }

    public void modifyInstructor(Instructor instructor) throws SQLException {
        String sql = "UPDATE instructors SET name = ?, email = ? WHERE instructor_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmt.setString(1, instructor.getName());
            pstmt.setString(2, instructor.getEmail());
            pstmt.setString(3, instructor.getInstructorId());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Instructor with ID " + instructor.getInstructorId() + " does not exist.");
            }
        }
    }

    public void assignInstructor(String courseId, String instructorId) throws SQLException {
        String sql = "UPDATE courses SET instructor_id = ? WHERE course_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmt.setString(1, instructorId);
            pstmt.setString(2, courseId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Course with ID " + courseId + " does not exist or instructor assignment failed.");
            }
        }
    }

    // Student Management
    public void addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (student_id, name, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmt.setString(1, student.getStudentId());
            pstmt.setString(2, student.getName());
            pstmt.setString(3, student.getEmail());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                throw new SQLException("Student with ID " + student.getStudentId() + " already exists.", e);
            }
            throw new SQLException("Error adding student: " + e.getMessage(), e);
        }
    }

    public void removeStudent(String studentId) throws SQLException {
        String sqlDeleteEnrollments = "DELETE FROM course_students WHERE student_id = ?";
        String sqlDeleteStudent = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmtEnrollments = conn.prepareStatement(sqlDeleteEnrollments);
             PreparedStatement pstmtStudent = conn.prepareStatement(sqlDeleteStudent)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            conn.setAutoCommit(false);
            try {
                pstmtEnrollments.setString(1, studentId);
                pstmtEnrollments.executeUpdate();
                pstmtStudent.setString(1, studentId);
                int rowsAffected = pstmtStudent.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Student with ID " + studentId + " does not exist.");
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Student getStudentDetails(String studentId) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Student(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("email")
                );
            }
            return null;
        }
    }

    public void modifyStudent(Student student) throws SQLException {
        String sql = "UPDATE students SET name = ?, email = ? WHERE student_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getEmail());
            pstmt.setString(3, student.getStudentId());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Student with ID " + student.getStudentId() + " does not exist.");
            }
        }
    }

    public void enrollStudent(String courseId, String studentId) throws SQLException {
        // Check current enrollment count
        String sqlCheckEnrollments = "SELECT COUNT(*) FROM course_students WHERE student_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmtCheckEnrollments = conn.prepareStatement(sqlCheckEnrollments)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmtCheckEnrollments.setString(1, studentId);
            ResultSet rs = pstmtCheckEnrollments.executeQuery();
            if (rs.next() && rs.getInt(1) >= 6) {
                throw new SQLException("Student " + studentId + " is already enrolled in the maximum of 6 courses.");
            }
        }

        String sqlCheckCapacity = "SELECT max_students, (SELECT COUNT(*) FROM course_students WHERE course_id = ?) AS enrolled FROM courses WHERE course_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheckCapacity)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmtCheck.setString(1, courseId);
            pstmtCheck.setString(2, courseId);
            ResultSet rs = pstmtCheck.executeQuery();
            if (rs.next()) {
                int maxStudents = rs.getInt("max_students");
                int enrolled = rs.getInt("enrolled");
                if (enrolled >= maxStudents) {
                    throw new SQLException("Course " + courseId + " is full.");
                }
            } else {
                throw new SQLException("Course with ID " + courseId + " does not exist.");
            }
        }

        String sqlCheckStudent = "SELECT COUNT(*) FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmtCheckStudent = conn.prepareStatement(sqlCheckStudent)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmtCheckStudent.setString(1, studentId);
            ResultSet rs = pstmtCheckStudent.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                throw new SQLException("Student with ID " + studentId + " does not exist.");
            }
        }

        String sql = "INSERT INTO course_students (course_id, student_id) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmt.setString(1, courseId);
            pstmt.setString(2, studentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                throw new SQLException("Student " + studentId + " is already enrolled in course " + courseId + ".", e);
            }
            throw new SQLException("Error enrolling student: " + e.getMessage(), e);
        }
    }

    // New method to get enrolled courses for a student
    public ArrayList<Course> getEnrolledCoursesForStudent(String studentId) throws SQLException {
        ArrayList<Course> enrolledCourses = new ArrayList<>();
        String sql = "SELECT c.course_id, c.course_name, c.credits, c.max_students, c.instructor_id, i.name AS instructor_name " +
                "FROM course_students cs " +
                "JOIN courses c ON cs.course_id = c.course_id " +
                "LEFT JOIN instructors i ON c.instructor_id = i.instructor_id " +
                "WHERE cs.student_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Database connection failed.");
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Course course = new Course(
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getInt("credits"),
                        rs.getInt("max_students")
                );
                String instructorId = rs.getString("instructor_id");
                if (instructorId != null) {
                    course.setInstructorId(instructorId + " (" + rs.getString("instructor_name") + ")");
                }
                enrolledCourses.add(course);
            }
        }
        return enrolledCourses;
    }
}