import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ResultService {

    private static final String[] SUBJECTS = {
            "Java", "Mathematics", "English", "DBMS", "Networks"
    };

    private final ArrayList<Student> students = new ArrayList<>();
    private final HashSet<String> rollNumbers = new HashSet<>();
    private final FileManager fileManager;
    private int nextStudentId = 1;

    public ResultService(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void loadStudents() throws Exception {
        students.clear();
        rollNumbers.clear();
        nextStudentId = 1;
        ArrayList<Student> loaded = fileManager.loadStudents();
        int maxId = 0;
        for (Student student : loaded) {
            students.add(student);
            rollNumbers.add(student.getRollNumber());
            maxId = Math.max(maxId, student.getId());
        }
        nextStudentId = maxId + 1;
    }

    public void registerStudent(String name, String rollNumber, String department) throws Exception {
        name = name == null ? "" : name.trim();
        rollNumber = rollNumber == null ? "" : rollNumber.trim().toUpperCase(Locale.ROOT);
        department = department == null ? "" : department.trim();

        validateText(name, "Name");
        validateText(rollNumber, "Roll number");
        validateText(department, "Department");

        if (name.contains("|") || rollNumber.contains("|") || department.contains("|")) {
            throw new IllegalArgumentException("Text fields cannot contain | or line breaks.");
        }
        if (rollNumbers.contains(rollNumber)) {
            throw new IllegalArgumentException("Duplicate roll number.");
        }

        Student student = new Student(nextStudentId, name, rollNumber, department);
        ArrayList<Student> proposed = new ArrayList<>(students);
        proposed.add(student);
        fileManager.saveStudents(proposed);
        students.add(student);
        rollNumbers.add(rollNumber);
        nextStudentId++;
    }

    public ArrayList<Student> getStudents() {
        return new ArrayList<>(students);
    }

    public Student findStudentById(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                return student;
            }
        }
        return null;
    }

    public void enterMarks(int studentId, int[] marks) throws Exception {
        Student student = findStudentById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Unknown student ID.");
        }
        validateMarks(marks);
        student.setMarks(marks);
        fileManager.saveStudents(students);
    }

    public void showResult(int studentId) {
        Student student = findStudentById(studentId);
        if (student == null) {
            System.out.println("Unknown student ID.");
            return;
        }
        showResult(student);
    }

    public void showResult(Student student) {
        System.out.println("\n===== " + student.getName().toUpperCase(Locale.ROOT) + " RESULT =====");
        System.out.println("Student ID: " + student.getId());
        System.out.println("Name: " + student.getName());
        System.out.println("Roll Number: " + student.getRollNumber());
        System.out.println("Department: " + student.getDepartment());

        if (!student.hasMarks()) {
            System.out.println("Marks not entered.");
            return;
        }

        int[] marks = student.getMarks();
        for (int i = 0; i < SUBJECTS.length; i++) {
            System.out.println(SUBJECTS[i] + ": " + marks[i]);
        }
        System.out.println("Total: " + student.getTotal() + " / 500");
        System.out.printf("Average: %.2f%n", student.getAverage());
        System.out.println("Result: " + (student.isPass() ? "PASS" : "FAIL"));
    }

    public ReportData buildReport() {
        int total = students.size();
        int withMarks = 0;
        int awaiting = 0;
        int pass = 0;
        int fail = 0;
        double highestAverage = 0.0;

        for (Student student : students) {
            if (!student.hasMarks()) {
                awaiting++;
                continue;
            }
            withMarks++;
            if (student.isPass()) {
                pass++;
            } else {
                fail++;
            }
            highestAverage = Math.max(highestAverage, student.getAverage());
        }
        return new ReportData(total, withMarks, awaiting, pass, fail, highestAverage);
    }

    public String buildReportText() {
        ReportData r = buildReport();
        StringBuilder sb = new StringBuilder();
        sb.append("Total students: ").append(r.totalStudents).append('\n');
        sb.append("Students with marks: ").append(r.studentsWithMarks).append('\n');
        sb.append("Students awaiting marks: ").append(r.studentsAwaitingMarks).append('\n');
        sb.append("PASS: ").append(r.pass).append('\n');
        sb.append("FAIL: ").append(r.fail).append('\n');
        if (r.studentsWithMarks > 0) {
            sb.append(String.format(Locale.ROOT, "Highest average: %.2f%n", r.highestAverage));
        } else {
            sb.append("Highest average: N/A\n");
        }
        return sb.toString();
    }

    public void saveReport() throws Exception {
        fileManager.saveReport(buildReportText());
    }

    public String[] getSubjects() {
        return SUBJECTS.clone();
    }

    private static void validateText(String value, String field) {
        if (value.isBlank() || value.contains("\n") || value.contains("\r")) {
            throw new IllegalArgumentException(field + " is required.");
        }
    }

    public static void validateMarks(int[] marks) {
        if (marks == null || marks.length != 5) {
            throw new IllegalArgumentException("Exactly five marks are required.");
        }
        for (int mark : marks) {
            if (mark < 0 || mark > 100) {
                throw new IllegalArgumentException("Marks must be between 0 and 100.");
            }
        }
    }

    public static class ReportData {
        public final int totalStudents;
        public final int studentsWithMarks;
        public final int studentsAwaitingMarks;
        public final int pass;
        public final int fail;
        public final double highestAverage;

        public ReportData(int totalStudents, int studentsWithMarks, int studentsAwaitingMarks,
                          int pass, int fail, double highestAverage) {
            this.totalStudents = totalStudents;
            this.studentsWithMarks = studentsWithMarks;
            this.studentsAwaitingMarks = studentsAwaitingMarks;
            this.pass = pass;
            this.fail = fail;
            this.highestAverage = highestAverage;
        }
    }
}
