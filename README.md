# Student Course Registration & Fee Management System

Console-based Java JDBC application with layered architecture:

- Bootstrap: `src/Main.java` (starts system only)
- UI Layer: `src/ui/ConsoleMenu.java`
- Input Layer: `src/input/ConsoleInput.java`
- Service Layer: `src/services/StudentService.java`, `src/services/RegistrationService.java`
- Course + Branch Services: `src/services/CourseService.java`, `src/services/BranchService.java`
- DAO Layer: `src/dao/StudentDAO.java`, `src/dao/RegistrationDAO.java`, `src/dao/CourseDAO.java`, `src/dao/BranchDAO.java`
- DB Utility: `src/util/DBUtil.java`
- Models: `src/model/*` including `Course.java` and `Branch.java`

## Features implemented

1. Add student (duplicate ID, empty name/branch, invalid age checks)
2. Register for course (transaction + duplicate registration check + fee validation + select branch then course)
3. View all students with courses (LEFT JOIN; includes students without registrations)
4. Search student by ID and list all registered courses
5. Update student details (name, branch)
6. Update course fee (must remain positive)
7. Cancel course registration
8. Delete student (transaction: delete registrations then student)
9. High paying students report (fee threshold)
10. Course-wise student count report
11. Add new course (under a selected branch)
12. Add new branch

## Database setup

Default DB connection values are in `src/util/DBUtil.java` and can be overridden via environment variables:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`

Example default URL:

`jdbc:mysql://localhost:3306/student_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`

`DBUtil.initializeDatabase()` creates tables automatically if they do not exist.

It ensures these core tables exist:

- `student`
- `branches`
- `courses`
- `registration` with `course_id` foreign key support

## Build and run (Maven)

The project includes `pom.xml` with MySQL JDBC dependency (`mysql-connector-j`).

```zsh
cd "/Users/milendrakumarbaghel/Documents/java-workspace/Student Management System"
mvn -q -DskipTests compile
mvn -q exec:java
```

## Build and run (plain javac/java)

```zsh
cd "/Users/milendrakumarbaghel/Documents/java-workspace/Student Management System"
find src -name "*.java" -print0 | xargs -0 javac
java -cp src Main
```

## First-principles checklist per critical operation

### Register for course (transaction)

- Input validation: student ID > 0, course not blank, fee > 0
- Failure conditions: student missing, duplicate registration, DB insert error
- Atomic unit: duplicate-check + insert registration
- Inconsistency risk: partial registration write if transaction not used

### Delete student (transaction)

- Input validation: student ID > 0
- Failure conditions: student missing, delete registration/student query failure
- Atomic unit: delete registrations + delete student
- Inconsistency risk: orphan registrations or undeleted student if one step fails

### Other operations

- Add/Update/Search/Cancel/Reports each validates inputs and fails gracefully on empty data or SQL errors
- All SQL uses `PreparedStatement` and DAO methods use `try-with-resources`
