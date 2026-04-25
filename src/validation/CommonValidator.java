package validation;

public final class CommonValidator {

    private CommonValidator() {
    }

    public static ValidationResult validatePositiveId(int id, String fieldName) {
        if (id <= 0) {
            return ValidationResult.invalid(fieldName + " must be positive.");
        }
        return ValidationResult.valid();
    }

    public static ValidationResult validateNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.invalid(fieldName + " cannot be empty.");
        }
        return ValidationResult.valid();
    }

    public static ValidationResult validatePositiveValue(double value, String fieldName) {
        if (value <= 0) {
            return ValidationResult.invalid(fieldName + " must be positive.");
        }
        return ValidationResult.valid();
    }
}

