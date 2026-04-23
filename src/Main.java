import input.ConsoleInput;
import services.RegistrationService;
import services.StudentService;
import ui.ConsoleMenu;
import util.DBUtil;

public class Main {
    public static void main(String[] args) {
        DBUtil.initializeDatabase();

        StudentService studentService = new StudentService();
        RegistrationService registrationService = new RegistrationService();
        ConsoleInput consoleInput = new ConsoleInput();

        ConsoleMenu consoleMenu = new ConsoleMenu(studentService, registrationService, consoleInput);
        consoleMenu.start();
    }
}
