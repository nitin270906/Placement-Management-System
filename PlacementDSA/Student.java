import java.util.ArrayList;
import java.util.List;

public class Student {
    private int id;
    private String name;
    private double cgpa;
    private String branch;
    private int backlogs;
    private int batchYear;
    
    // New fields
    private String password;
    private String resumePath;
    private String skills;
    private String emailId;
    private List<String> notifications;

    public Student(int id, String name, double cgpa, String branch, int backlogs, int batchYear) {
        this.id = id;
        this.name = name;
        this.cgpa = cgpa;
        this.branch = branch;
        this.backlogs = backlogs;
        this.batchYear = batchYear;
        this.password = "pass123"; // Default password
        this.resumePath = "";
        this.skills = "";
        this.emailId = "";
        this.notifications = new ArrayList<>();
    }

    public Student(int id, String name, double cgpa, String branch, int backlogs) {
        this(id, name, cgpa, branch, backlogs, 2027);
    }
    
    // Constructor for registration
    public Student(int id, String password, int batchYear) {
        this.id = id;
        this.password = password;
        this.batchYear = batchYear;
        this.name = "New Student";
        this.cgpa = 0.0;
        this.branch = "Unknown";
        this.backlogs = 0;
        this.resumePath = "";
        this.skills = "";
        this.emailId = "";
        this.notifications = new ArrayList<>();
    }

    public Student(int id, String password) {
        this(id, password, 2027);
    }
    
    public List<String> getNotifications() { return notifications; }
    public void addNotification(String message) { this.notifications.add(message); }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }
    
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    
    public int getBacklogs() { return backlogs; }
    public void setBacklogs(int backlogs) { this.backlogs = backlogs; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getResumePath() { return resumePath; }
    public void setResumePath(String resumePath) { this.resumePath = resumePath; }
    
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    
    public String getEmailId() { return emailId; }
    public void setEmailId(String emailId) { this.emailId = emailId; }

    public int getBatchYear() { return batchYear; }
    public void setBatchYear(int batchYear) { this.batchYear = batchYear; }

    @Override
    public String toString() {
        return String.format("| %-3d | %-20s | %-4.2f | %-6s | %-8d | Batch: %d |", id, name, cgpa, branch, backlogs, batchYear);
    }
}
