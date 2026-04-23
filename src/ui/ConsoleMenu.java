package ui;

import input.ConsoleInput;
import model.Registration;
import model.Student;
import services.RegistrationService;
import services.StudentService;

import java.util.List;
import java.util.Map;

public class ConsoleMenu {

    private final StudentService studentService;
    private final RegistrationService registrationService;
    private final ConsoleInput consoleInput;

    public ConsoleMenu(StudentService studentService, RegistrationService registrationService, ConsoleInput consoleInput) {
        this.studentService = studentService;
        this.registrationService = registrationService;
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
                case 11 -> {
                    running = false;
                    System.out.println("Exiting... Goodbye!");
                }
                default -> System.out.println("Invalid choice. Please select 1-11.");
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
        System.out.println("11. Exit");
    }

    private void addStudent() {
        int id = consoleInput.readInt("Enter student ID: ");
        String name = consoleInput.readNonEmpty("Enter name: ");
        int age = consoleInput.readInt("Enter age: ");
        String branch = consoleInput.readNonEmpty("Enter branch: ");

        boolean added = studentService.addStudent(new Student(id, name, age, branch));
        System.out.println(added ? "Student added successfully." : "Failed to add student.");
    }

    private void registerForCourse() {
        int studentId = consoleInput.readInt("Enter student ID: ");
        String course = consoleInput.readNonEmpty("Enter course name: ");
        double fee = consoleInput.readDouble("Enter fee paid: ");

        boolean registered = registrationService.registerForCourse(studentId, course, fee);
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
        String name = consoleInput.readNonEmpty("Enter new name: ");
        String branch = consoleInput.readNonEmpty("Enter new branch: ");

        boolean updated = studentService.updateStudent(id, name, branch);
        System.out.println(updated ? "Student updated successfully." : "Student update failed.");
    }

    private void updateCourseFee() {
        int studentId = consoleInput.readInt("Enter student ID: ");
        String course = consoleInput.readNonEmpty("Enter course name: ");
        double fee = consoleInput.readDouble("Enter new fee: ");

        boolean updated = registrationService.updateCourseFee(studentId, course, fee);
        System.out.println(updated ? "Course fee updated successfully." : "Course fee update failed.");
    }

    private void cancelRegistration() {
        int studentId = consoleInput.readInt("Enter student ID: ");
        String course = consoleInput.readNonEmpty("Enter course name: ");

        boolean cancelled = registrationService.cancelRegistration(studentId, course);
        System.out.println(cancelled ? "Registration cancelled successfully." : "Cancellation failed.");
    }

    private void deleteStudent() {
        int studentId = consoleInput.readInt("Enter student ID to delete: ");
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
            System.out.println("No registrations found.");
            return;
        }

        System.out.println("\nCourse -> Number of Students");
        for (Map.Entry<String, Integer> row : rows.entrySet()) {
            System.out.printf("%s -> %d%n", row.getKey(), row.getValue());
        }
    }
}

