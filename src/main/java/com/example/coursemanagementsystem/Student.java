package com.example.coursemanagementsystem;

public class Student {
    private String studentId;
    private String name;
    private String email;

    public Student(String studentId, String name, String email) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
    }

    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return studentId + ": " + name + " (" + email + ")";
    }
}