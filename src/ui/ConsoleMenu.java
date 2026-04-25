package ui;

import input.ConsoleInput;
import model.Branch;
import model.Course;
import model.Registration;
import model.Student;
import services.BranchService;
import services.CourseService;
import services.RegistrationService;
import services.StudentService;
import validation.BranchValidator;
import validation.CourseValidator;
import validation.StudentValidator;
import validation.ValidationResult;

import java.util.List;
import java.util.Map;


public class ConsoleMenu {

    private final StudentService studentService;
    private final RegistrationService registrationService;
    private final CourseService courseService;
    private final BranchService branchService;
    private final ConsoleInput consoleInput;

    public ConsoleMenu(StudentService studentService, RegistrationService registrationService, CourseService courseService, BranchService branchService, ConsoleInput consoleInput) {
        this.studentService = studentService;
        this.registrationService = registrationService;
        this.courseService = courseService;
        this.branchService = branchService;
        this.consoleInput = consoleInput;
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = consoleInput.readInt("Choose an option: ");

            switch (choice) {
                case 1 -> addStudent();
                case 2 -> registerForCourse();
                case 3 -> viewAllStudents();
                case 4 -> searchStudentById();
                case 5 -> updateStudent();
                case 6 -> updateCourseFee();
                case 7 -> cancelRegistration();
                case 8 -> deleteStudent();
                case 9 -> highPayingReport();
                case 10 -> courseWiseCount();
                case 11 -> addCourse();
                case 12 -> addBranch();
                case 13 -> showAllBranches();
                case 14 -> showAllCourses();
                case 15 -> {
                    running = false;
                    System.out.println("Exiting... Goodbye!");
                }
                default -> System.out.println("Invalid choice. Please select 1-15.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\nStudent Course Registration & Fee Management\n");
        System.out.println("1. Add Student");
        System.out.println("2. Register for Course");
        System.out.println("3. View All Students with Courses");
        System.out.println("4. Search Student by ID");
        System.out.println("5. Update Student");
        System.out.println("6. Update Course Fee");
        System.out.println("7. Cancel Registration");
        System.out.println("8. Delete Student");
        System.out.println("9. High Paying Students Report");
        System.out.println("10. Course-wise Student Count");
        System.out.println("11. Add New Course");
        System.out.println("12. Add New Branch");
        System.out.println("13. Show All Branches");
        System.out.println("14. Show All Courses");
        System.out.println("15. Exit");
    }

    private void addStudent() {
        int id = consoleInput.readInt("Enter student ID: ");
        if (!validateStudentIdForAdd(id)) {
            return;
        }

        String name = consoleInput.readNonEmpty("Enter name: ");
        int age = consoleInput.readInt("Enter age: ");
        Branch selectedBranch = selectBranch();
        if (selectedBranch == null) {
            return;
        }

        boolean added = studentService.addStudent(new Student(id, name, age, selectedBranch.getBranchId(), selectedBranch.getBranchName()));
        System.out.println(added ? "Student added successfully." : "Failed to add student.");
    }

    private void registerForCourse() {
        int studentId = consoleInput.readInt("Enter student ID: ");
        Student student = getExistingStudentById(studentId);
        if (student == null) {
            return;
        }

        Course selectedCourse = selectCourseByBranch(student.getBranchId());
        if (selectedCourse == null) {
            return;
        }
        double fee = consoleInput.readDouble("Enter fee paid: ");

        boolean registered = registrationService.registerForCourse(studentId, selectedCourse.getCourseId(), fee);
        System.out.println(registered ? "Registration successful." : "Registration failed.");
    }

    private void viewAllStudents() {
        List<Map<String, Object>> rows = studentService.viewAllStudentsWithRegistrations();
        if (rows.isEmpty()) {
            System.out.println("No students found.");
            return;
        }

        System.out.println("\nID | Name | Age | Branch | Course | Fees Paid");
        System.out.println("----------------------------------------------------");
        for (Map<String, Object> row : rows) {
            String course = row.get("courseName") == null ? "-" : String.valueOf(row.get("courseName"));
            String fee = row.get("courseName") == null ? "-" : String.valueOf(row.get("feesPaid"));
            System.out.printf("%d | %s | %d | %s | %s | %s%n",
                    (int) row.get("id"), row.get("name"), (int) row.get("age"), row.get("branch"), course, fee);
        }
    }

    private void searchStudentById() {
        int id = consoleInput.readInt("Enter student ID to search: ");
        Student student = studentService.findStudentById(id);

        if (student == null) {
            System.out.println("Student not found.");
            return;
        }

        System.out.printf("Student: %d | %s | %d | %s%n", student.getId(), student.getName(), student.getAge(), student.getBranch());

        List<Registration> regs = studentService.findRegistrationsByStudentId(id);
        if (regs.isEmpty()) {
            System.out.println("No course registrations found.");
            return;
        }

        System.out.println("Registered Courses:");
        for (Registration reg : regs) {
            System.out.printf("- %s (Fees Paid: %.2f)%n", reg.getCourseName(), reg.getFeesPaid());
        }
    }

    private void updateStudent() {
        int id = consoleInput.readInt("Enter student ID: ");
        if (getExistingStudentById(id) == null) {
            return;
        }

        System.out.println("1. Update Name");
        System.out.println("2. Update Age");
        System.out.println("3. Update Branch (after updating branch student needed to reregister for courses under new branch)");
        int updateChoice = consoleInput.readInt("Choose update option: ");

        switch (updateChoice) {
            case 1 -> {
                String name = consoleInput.readNonEmpty("Enter new name: ");
                boolean updated = studentService.updateStudentName(id, name);
                System.out.println(updated ? "Student name updated successfully." : "Student name update failed.");
            }
            case 2 -> {
                int age = consoleInput.readInt("Enter new age: ");
                boolean updated = studentService.updateStudentAge(id, age);
                System.out.println(updated ? "Student age updated successfully." : "Student age update failed.");
            }
            case 3 -> {
                Branch selectedBranch = selectBranch();
                if (selectedBranch == null) {
                    return;
                }

                boolean updated = studentService.updateStudentBranch(id, selectedBranch.getBranchId());
                System.out.println(updated
                        ? "Student branch updated and courses aligned to the new branch."
                        : "Student branch update failed.");
            }
            default -> System.out.println("Invalid update option. Please select 1, 2 or 3.");
        }
    }

    private void updateCourseFee() {
        int studentId = consoleInput.readInt("Enter student ID: ");
        if (getExistingStudentById(studentId) == null) {
            return;
        }
        double fee = consoleInput.readDouble("Enter new fee: ");

        boolean updated = registrationService.updateCourseFee(studentId, fee);
        System.out.println(updated ? "Course fee updated successfully." : "Course fee update failed.");
    }

    private void cancelRegistration() {
        int studentId = consoleInput.readInt("Enter student ID: ");
        if (getExistingStudentById(studentId) == null) {
            return;
        }

        Branch selectedBranch = selectBranch();
        if (selectedBranch == null) {
            return;
        }

        Course selectedCourse = selectCourseByBranch(selectedBranch.getBranchId());
        if (selectedCourse == null) {
            return;
        }

        boolean cancelled = registrationService.cancelRegistration(studentId, selectedCourse.getCourseId());
        System.out.println(cancelled ? "Registration cancelled successfully." : "Cancellation failed.");
    }

    private void deleteStudent() {
        int studentId = consoleInput.readInt("Enter student ID to delete: ");
        if (getExistingStudentById(studentId) == null) {
            return;
        }

        boolean deleted = studentService.deleteStudent(studentId);
        System.out.println(deleted ? "Student deleted successfully." : "Student deletion failed.");
    }

    private void highPayingReport() {
        double threshold = consoleInput.readDouble("Enter minimum fee threshold: ");
        List<Map<String, Object>> rows = studentService.getHighPayingStudents(threshold);

        if (rows.isEmpty()) {
            System.out.println("No students found for this fee threshold.");
            return;
        }

        System.out.println("\nHigh Paying Students:");
        for (Map<String, Object> row : rows) {
            System.out.printf("%d | %s | %s | %.2f%n",
                    (int) row.get("id"), row.get("name"), row.get("courseName"), (double) row.get("feesPaid"));
        }
    }

    private void courseWiseCount() {
        Map<String, Integer> rows = registrationService.getCourseWiseStudentCount();
        if (rows.isEmpty()) {
            System.out.println("No courses found.");
            return;
        }

        System.out.println("\nCourse -> Number of Students");
        for (Map.Entry<String, Integer> row : rows.entrySet()) {
            System.out.printf("%s -> %d%n", row.getKey(), row.getValue());
        }
    }

    private void addCourse() {
        Branch selectedBranch = selectBranch();
        if (selectedBranch == null) {
            return;
        }

        String courseName = consoleInput.readNonEmpty("Enter course name: ");
        boolean added = courseService.addCourse(selectedBranch.getBranchId(), courseName);
        System.out.println(added ? "Course added successfully." : "Failed to add course.");
    }

    private void addBranch() {
        String branchName = consoleInput.readNonEmpty("Enter branch name: ");
        boolean added = branchService.addBranch(branchName);
        System.out.println(added ? "Branch added successfully." : "Failed to add branch.");
    }

    private void showAllBranches() {
        List<Branch> branches = branchService.getAllBranches();
        if (branches.isEmpty()) {
            System.out.println("No branches found.");
            return;
        }

        System.out.println("\nBranch ID | Branch Name");
        System.out.println("-----------------------");
        for (Branch branch : branches) {
            System.out.printf("%d | %s%n", branch.getBranchId(), branch.getBranchName());
        }
    }

    private void showAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        if (courses.isEmpty()) {
            System.out.println("No courses found.");
            return;
        }

        System.out.println("\nCourse ID | Branch ID | Course Name");
        System.out.println("-----------------------------------");
        for (Course course : courses) {
            System.out.printf("%d | %d | %s%n", course.getCourseId(), course.getBranchId(), course.getCourseName());
        }
    }

    private Branch selectBranch() {
        List<Branch> branches = branchService.getAllBranches();
        if (branches.isEmpty()) {
            System.out.println("No branches available. Please add a branch first.");
            return null;
        }

        System.out.println("\nAvailable Branches:");
        for (Branch branch : branches) {
            System.out.printf("%d | %s%n", branch.getBranchId(), branch.getBranchName());
        }

        while (true) {
            int branchId = consoleInput.readInt("Select branch ID: ");
            ValidationResult validationResult = BranchValidator.validateBranchSelection(branchId, branches);
            if (validationResult.isValid()) {
                return getBranchById(branchId, branches);
            }
            System.out.println(validationResult.getMessage() + " Try again.");
        }
    }

    private Course selectCourseByBranch(int branchId) {
        List<Course> courses = courseService.getCoursesByBranchId(branchId);
        if (courses.isEmpty()) {
            System.out.println("No courses available for this branch. Please add a course first.");
            return null;
        }

        System.out.println("\nAvailable Courses:");
        for (Course course : courses) {
            System.out.printf("%d | %s%n", course.getCourseId(), course.getCourseName());
        }

        while (true) {
            int courseId = consoleInput.readInt("Select course ID: ");
            ValidationResult validationResult = CourseValidator.validateCourseSelection(courseId, courses);
            if (validationResult.isValid()) {
                return getCourseById(courseId, courses);
            }
            System.out.println(validationResult.getMessage() + " Try again.");
        }
    }

    private Branch getBranchById(int branchId, List<Branch> branches) {
        for (Branch branch : branches) {
            if (branch.getBranchId() == branchId) {
                return branch;
            }
        }
        return null;
    }

    private Course getCourseById(int courseId, List<Course> courses) {
        for (Course course : courses) {
            if (course.getCourseId() == courseId) {
                return course;
            }
        }
        return null;
    }

    private boolean validateStudentIdForAdd(int studentId) {
        ValidationResult validationResult = StudentValidator.validateStudentId(studentId);
        if (!validationResult.isValid()) {
            System.out.println(validationResult.getMessage());
            return false;
        }

        if (studentService.findStudentById(studentId) != null) {
            System.out.println("Failure: duplicate student ID.");
            return false;
        }

        return true;
    }

    private Student getExistingStudentById(int studentId) {
        ValidationResult validationResult = StudentValidator.validateStudentId(studentId);
        if (!validationResult.isValid()) {
            System.out.println(validationResult.getMessage());
            return null;
        }

        Student student = studentService.findStudentById(studentId);
        if (student == null) {
            System.out.println("Student not found.");
        }

        return student;
    }
}

