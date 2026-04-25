import input.ConsoleInput;
import services.BranchService;
import services.CourseService;
import services.RegistrationService;
import services.StudentService;
import ui.ConsoleMenu;
import util.DBUtil;

public class Main {
    public static void main(String[] args) {
        DBUtil.initializeDatabase();

        StudentService studentService = new StudentService();
        RegistrationService registrationService = new RegistrationService();
        BranchService branchService = new BranchService();
        CourseService courseService = new CourseService();
        ConsoleInput consoleInput = new ConsoleInput();

        ConsoleMenu consoleMenu = new ConsoleMenu(studentService, registrationService, courseService, branchService, consoleInput);
        consoleMenu.start();
    }
}
