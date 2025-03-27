package com.example.coursemanagementsystem;

public class Course {
    private String courseId;
    private String courseName;
    private int credits;
    private int maxStudents;
    private String instructorId;

    public Course(String courseId, String courseName, int credits, int maxStudents) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credits = credits;
        this.maxStudents = maxStudents;
    }

    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public int getCredits() { return credits; }
    public int getMaxStudents() { return maxStudents; }
    public String getInstructorId() { return instructorId; }

    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }

    @Override
    public String toString() {
        return courseId + ": " + courseName + " (" + credits + " credits, Max: " + maxStudents + ")";
    }
}
