package model;

public class Course {
    private int courseId;
    private int branchId;
    private String courseName;

    public Course() {
    }

    public Course(int courseId, int branchId, String courseName) {
        this.courseId = courseId;
        this.branchId = branchId;
        this.courseName = courseName;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}

