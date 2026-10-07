import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FileManager {

    private final Path dataDir;
    private final Path studentFile;
    private final Path reportFile;

    public FileManager() {
        this(Path.of("data"));
    }

    public FileManager(Path dataDir) {
        this.dataDir = dataDir;
        this.studentFile = dataDir.resolve("students.txt");
        this.reportFile = dataDir.resolve("report.txt");
    }

    public void initialize() throws IOException {
        Files.createDirectories(dataDir);
    }

    public ArrayList<Student> loadStudents() throws IOException {
        initialize();
        ArrayList<Student> loaded = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        Set<String> rolls = new HashSet<>();

        if (!Files.exists(studentFile)) {
            return loaded;
        }

        int lineNumber = 0;
        try (BufferedReader reader = Files.newBufferedReader(studentFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    throw new IOException("Invalid students.txt at line " + lineNumber + ": blank record.");
                }
                String[] fields = line.split("\\|", -1);
                if (fields.length != 5) {
                    throw new IOException("Invalid students.txt at line " + lineNumber + ": expected 5 fields.");
                }
                int id = parseInt(fields[0], "id", lineNumber);
                if (id <= 0 || !ids.add(id)) {
                    throw new IOException("Invalid students.txt at line " + lineNumber + ": duplicate/invalid ID.");
                }
                String name = fields[1];
                String roll = fields[2].trim().toUpperCase(java.util.Locale.ROOT);
                String department = fields[3];
                if (name.isBlank() || roll.isBlank() || department.isBlank()) {
                    throw new IOException("Invalid students.txt at line " + lineNumber + ": required field missing.");
                }
                if (name.contains("\n") || roll.contains("\n") || department.contains("\n")) {
                    throw new IOException("Invalid students.txt at line " + lineNumber + ": line break in text field.");
                }
                if (!rolls.add(roll)) {
                    throw new IOException("Invalid students.txt at line " + lineNumber + ": duplicate roll number.");
                }

                Student student = new Student(id, name, roll, department);
                String marksField = fields[4];
                if (!marksField.isEmpty()) {
                    String[] markTokens = marksField.split(",", -1);
                    if (markTokens.length != 5) {
                        throw new IOException("Invalid students.txt at line " + lineNumber + ": marks must have five values.");
                    }
                    int[] marks = new int[5];
                    for (int i = 0; i < 5; i++) {
                        marks[i] = parseInt(markTokens[i], "mark", lineNumber);
                        if (marks[i] < 0 || marks[i] > 100) {
                            throw new IOException("Invalid students.txt at line " + lineNumber + ": mark out of range.");
                        }
                    }
                    student.setMarks(marks);
                }
                loaded.add(student);
            }
        }
        return loaded;
    }

    public void saveStudents(ArrayList<Student> students) throws IOException {
        initialize();
        Path tempFile = Files.createTempFile(dataDir, "students", ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(
                    tempFile,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                for (Student student : students) {
                    writer.write(serializeStudent(student));
                    writer.newLine();
                }
            }
            Files.move(tempFile, studentFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(tempFile, studentFile, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    public void saveReport(String text) throws IOException {
        initialize();
        Path tempFile = Files.createTempFile(dataDir, "report", ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(
                    tempFile,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                writer.write(text);
            }
            Files.move(tempFile, reportFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(tempFile, reportFile, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static String serializeStudent(Student student) {
        StringBuilder sb = new StringBuilder();
        sb.append(student.getId()).append('|')
                .append(student.getName()).append('|')
                .append(student.getRollNumber()).append('|')
                .append(student.getDepartment()).append('|');
        if (student.hasMarks()) {
            int[] marks = student.getMarks();
            for (int i = 0; i < marks.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(marks[i]);
            }
        }
        return sb.toString();
    }

    private static int parseInt(String value, String field, int lineNumber) throws IOException {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IOException("Invalid students.txt at line " + lineNumber + ": invalid " + field + ".", e);
        }
    }
}
