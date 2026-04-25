package validation;

import model.Course;

import java.util.List;

public final class CourseValidator {

    private CourseValidator() {
    }

    public static ValidationResult validateForAdd(int branchId, String courseName) {
        ValidationResult branchResult = validateBranchId(branchId);
        if (!branchResult.isValid()) {
            return branchResult;
        }
        return CommonValidator.validateNonBlank(courseName, "Course name");
    }

    public static ValidationResult validateCourseId(int courseId) {
        return CommonValidator.validatePositiveId(courseId, "Course ID");
    }

    public static ValidationResult validateBranchId(int branchId) {
        return CommonValidator.validatePositiveId(branchId, "Branch ID");
    }

    public static ValidationResult validateCourseSelection(int courseId, List<Course> courses) {
        ValidationResult idResult = validateCourseId(courseId);
        if (!idResult.isValid()) {
            return idResult;
        }

        for (Course course : courses) {
            if (course.getCourseId() == courseId) {
                return ValidationResult.valid();
            }
        }

        return ValidationResult.invalid("Invalid course ID selected.");
    }
}


