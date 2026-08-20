/**
 * Student Model for Placement Management System.
 * 
 * Stores student data used for eligibility checks, sorting, and matching.
 * Fields: id, name, cgpa, branch, backlogs, batchYear, skills.
 */
public class Student {
    private int id;
    private String name;
    private double cgpa;
    private String branch;
    private int backlogs;
    private int batchYear;
    private String skills;

    public Student(int id, String name, double cgpa, String branch, int backlogs, int batchYear) {
        this.id = id;
        this.name = name;
        this.cgpa = cgpa;
        this.branch = branch;
        this.backlogs = backlogs;
        this.batchYear = batchYear;
        this.skills = "";
    }

    public Student(int id, String name, double cgpa, String branch, int backlogs) {
        this(id, name, cgpa, branch, backlogs, 2027);
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getCgpa() { return cgpa; }
    public String getBranch() { return branch; }
    public int getBacklogs() { return backlogs; }
    public int getBatchYear() { return batchYear; }
    public String getSkills() { return skills; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }
    public void setBranch(String branch) { this.branch = branch; }
    public void setBacklogs(int backlogs) { this.backlogs = backlogs; }
    public void setBatchYear(int batchYear) { this.batchYear = batchYear; }
    public void setSkills(String skills) { this.skills = skills; }

    @Override
    public String toString() {
        return String.format("| %-3d | %-20s | %-4.2f | %-6s | %-8d | Batch: %d |", id, name, cgpa, branch, backlogs, batchYear);
    }
}
