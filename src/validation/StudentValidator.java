package validation;

import model.Student;

public final class StudentValidator {

    private StudentValidator() {
    }

    public static ValidationResult validateForAdd(Student student) {
        if (student == null) {
            return ValidationResult.invalid("Student data is required.");
        }

        ValidationResult idResult = CommonValidator.validatePositiveId(student.getId(), "Student ID");
        if (!idResult.isValid()) {
            return idResult;
        }

        ValidationResult nameResult = CommonValidator.validateNonBlank(student.getName(), "Student name");
        if (!nameResult.isValid()) {
            return nameResult;
        }

        if (student.getAge() <= 0) {
            return ValidationResult.invalid("Age must be greater than 0.");
        }

        return CommonValidator.validatePositiveId(student.getBranchId(), "Branch ID");
    }

    public static ValidationResult validateStudentId(int studentId) {
        return CommonValidator.validatePositiveId(studentId, "Student ID");
    }

    public static ValidationResult validateForNameUpdate(int studentId, String name) {
        ValidationResult idResult = validateStudentId(studentId);
        if (!idResult.isValid()) {
            return idResult;
        }

        return CommonValidator.validateNonBlank(name, "Student name");
    }

    public static ValidationResult validateForAgeUpdate(int studentId, int age) {
        ValidationResult idResult = validateStudentId(studentId);
        if (!idResult.isValid()) {
            return idResult;
        }

        if (age <= 0) {
            return ValidationResult.invalid("Age must be greater than 0.");
        }

        return ValidationResult.valid();
    }

    public static ValidationResult validateForBranchUpdate(int studentId, int branchId) {
        ValidationResult idResult = validateStudentId(studentId);
        if (!idResult.isValid()) {
            return idResult;
        }

        return CommonValidator.validatePositiveId(branchId, "Branch ID");
    }

    public static ValidationResult validateFeeThreshold(double minFee) {
        return CommonValidator.validatePositiveValue(minFee, "Fee threshold");
    }
}

