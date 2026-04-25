package validation;

public final class RegistrationValidator {

    private RegistrationValidator() {
    }

    public static ValidationResult validateForRegister(int studentId, int courseId, double fee) {
        ValidationResult studentResult = CommonValidator.validatePositiveId(studentId, "Student ID");
        if (!studentResult.isValid()) {
            return studentResult;
        }


        ValidationResult courseResult = CommonValidator.validatePositiveId(courseId, "Course ID");
        if (!courseResult.isValid()) {
            return courseResult;
        }

        return CommonValidator.validatePositiveValue(fee, "Fee");
    }

    public static ValidationResult validateForFeeUpdate(int studentId, double fee) {
        ValidationResult studentResult = CommonValidator.validatePositiveId(studentId, "Student ID");
        if (!studentResult.isValid()) {
            return studentResult;
        }


        return CommonValidator.validatePositiveValue(fee, "Fee");
    }

    public static ValidationResult validateForCancel(int studentId, int courseId) {
        ValidationResult studentResult = CommonValidator.validatePositiveId(studentId, "Student ID");
        if (!studentResult.isValid()) {
            return studentResult;
        }

        return CommonValidator.validatePositiveId(courseId, "Course ID");
    }
}

