package validation;

import model.Branch;

import java.util.List;

public final class BranchValidator {

    private BranchValidator() {
    }

    public static ValidationResult validateBranchId(int branchId) {
        return CommonValidator.validatePositiveId(branchId, "Branch ID");
    }

    public static ValidationResult validateBranchName(String branchName) {
        return CommonValidator.validateNonBlank(branchName, "Branch name");
    }

    public static ValidationResult validateBranchSelection(int branchId, List<Branch> branches) {
        ValidationResult idResult = validateBranchId(branchId);
        if (!idResult.isValid()) {
            return idResult;
        }

        for (Branch branch : branches) {
            if (branch.getBranchId() == branchId) {
                return ValidationResult.valid();
            }
        }

        return ValidationResult.invalid("Invalid branch ID selected.");
    }
}


