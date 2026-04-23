package model;

public class Registration {
    private int regId;
    private int studentId;
    private String courseName;
    private double feesPaid;

    public Registration() {
    }

    public Registration(int regId, int studentId, String courseName, double feesPaid) {
        this.regId = regId;
        this.studentId = studentId;
        this.courseName = courseName;
        this.feesPaid = feesPaid;
    }

    public int getRegId() {
        return regId;
    }

    public void setRegId(int regId) {
        this.regId = regId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public double getFeesPaid() {
        return feesPaid;
    }

    public void setFeesPaid(double feesPaid) {
        this.feesPaid = feesPaid;
    }
}

