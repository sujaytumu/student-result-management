public class Teacher extends User {

    public Teacher() {
        super(0, "Teacher");
    }

    @Override
    public void showProfile() {
        System.out.println("Teacher ID: " + getId());
        System.out.println("Teacher Name: " + getName());
    }
}
