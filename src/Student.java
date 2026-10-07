public class Student extends User {

    private String rollNumber;
    private String department;
    private int[] marks;

    public Student(int id, String name, String rollNumber, String department) {
        super(id, name);
        this.rollNumber = rollNumber;
        this.department = department;
        this.marks = null;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getDepartment() {
        return department;
    }

    public boolean hasMarks() {
        return marks != null;
    }

    public void setMarks(int[] marks) {
        if (marks == null || marks.length != 5) {
            throw new IllegalArgumentException("Exactly five marks are required.");
        }
        this.marks = marks.clone();
    }

    public int[] getMarks() {
        return marks == null ? null : marks.clone();
    }

    @Override
    public void showProfile() {
        System.out.println("Student ID: " + getId());
        System.out.println("Name: " + getName());
        System.out.println("Roll Number: " + rollNumber);
        System.out.println("Department: " + department);
    }

    public int getTotal() {
        if (!hasMarks()) {
            return 0;
        }
        int total = 0;
        for (int mark : marks) {
            total += mark;
        }
        return total;
    }

    public double getAverage() {
        if (!hasMarks()) {
            return 0.0;
        }
        return getTotal() / 5.0;
    }

    public boolean isPass() {
        if (!hasMarks()) {
            return false;
        }
        for (int mark : marks) {
            if (mark < 40) {
                return false;
            }
        }
        return true;
    }
}
