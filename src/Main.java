import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static final Scanner SC = new Scanner(System.in);

    public static void main(String[] args) {
        FileManager fileManager = new FileManager();
        ResultService service = new ResultService(fileManager);

        try {
            fileManager.initialize();
            service.loadStudents();
        } catch (Exception e) {
            System.out.println("Startup error: " + e.getMessage());
            System.out.println("Correct the data file and restart the application.");
            return;
        }

        while (true) {
            System.out.println("\n===== STUDENT RESULT MANAGEMENT SYSTEM =====");
            System.out.println("1. Teacher menu");
            System.out.println("2. Student menu");
            System.out.println("0. Exit");

            String choice = readLine("Enter choice: ");
            switch (choice) {
                case "1" -> showTeacherMenu(service);
                case "2" -> showStudentMenu(service);
                case "0" -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void showTeacherMenu(ResultService service) {
        while (true) {
            System.out.println("\n===== TEACHER MENU =====");
            System.out.println("1. Register student");
            System.out.println("2. View students");
            System.out.println("3. Enter or update marks");
            System.out.println("4. View all results");
            System.out.println("5. View reports");
            System.out.println("0. Back");

            String choice = readLine("Enter choice: ");
            try {
                switch (choice) {
                    case "1" -> registerStudent(service);
                    case "2" -> viewStudents(service);
                    case "3" -> enterMarks(service);
                    case "4" -> viewAllResults(service);
                    case "5" -> viewReports(service);
                    case "0" -> { return; }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void registerStudent(ResultService service) throws Exception {
        String name = readLine("Name: ");
        String roll = readLine("Roll number: ");
        String dept = readLine("Department: ");
        service.registerStudent(name, roll, dept);
        Student s = service.getStudents().get(service.getStudents().size() - 1);
        System.out.println("Student " + s.getId() + " saved. Roll number: " + s.getRollNumber());
    }

    private static void viewStudents(ResultService service) {
        ArrayList<Student> students = service.getStudents();
        if (students.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (Student student : students) {
            System.out.println(student.getId() + " | " + student.getName() + " | "
                    + student.getRollNumber() + " | " + student.getDepartment());
        }
    }

    private static void enterMarks(ResultService service) throws Exception {
        int id = readInt("Student ID: ");
        Student student = service.findStudentById(id);
        if (student == null) {
            System.out.println("Unknown student ID. Try again.");
            return;
        }
        String[] subjects = service.getSubjects();
        int[] marks = new int[5];
        for (int i = 0; i < 5; i++) {
            marks[i] = readInt(subjects[i] + ": ");
            if (marks[i] < 0 || marks[i] > 100) {
                System.out.println("Marks must be between 0 and 100. Earlier marks remain unchanged.");
                return;
            }
        }
        service.enterMarks(id, marks);
        System.out.println("Marks saved.");
    }

    private static void viewAllResults(ResultService service) {
        ArrayList<Student> students = service.getStudents();
        if (students.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (Student student : students) {
            service.showResult(student);
            System.out.println();
        }
    }

    private static void viewReports(ResultService service) throws Exception {
        System.out.println("\n===== CLASS REPORT =====");
        System.out.print(service.buildReportText());
        String save = readLine("Save report to report.txt? (y/n): ");
        if (save.equalsIgnoreCase("y")) {
            service.saveReport();
            System.out.println("Report saved.");
        }
    }

    private static void showStudentMenu(ResultService service) {
        while (true) {
            int id = readInt("\nEnter student ID: ");
            Student student = service.findStudentById(id);
            if (student == null) {
                System.out.println("Unknown student ID. Try again.");
                continue;
            }

            while (true) {
                System.out.println("\n===== STUDENT MENU =====");
                System.out.println("1. View my profile");
                System.out.println("2. View my result");
                System.out.println("0. Back");
                String choice = readLine("Enter choice: ");

                switch (choice) {
                    case "1" -> student.showProfile();
                    case "2" -> service.showResult(student);
                    case "0" -> { return; }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            }
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            String input = readLine(prompt);
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return SC.nextLine();
    }
}
