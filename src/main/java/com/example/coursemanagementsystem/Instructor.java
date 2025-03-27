package com.example.coursemanagementsystem;

public class Instructor {
    private String instructorId;
    private String name;
    private String email;

    public Instructor(String instructorId, String name, String email) {
        this.instructorId = instructorId;
        this.name = name;
        this.email = email;
    }

    public String getInstructorId() { return instructorId; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return instructorId + ": " + name + " (" + email + ")";
    }
}