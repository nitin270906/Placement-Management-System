public class Drive {
    private int id;
    private String companyName;
    private double ctc; // in LPA
    private String eligibleBranch; // "ALL" means all branches eligible
    private double minCgpa;
    private int maxBacklogs;
    
    // New fields
    private String requiredSkills;
    private String driveDate;
    private String registerBy;
    private String venue;
    private String jobRole;
    private String specialization;
    private String status;
    private int targetYear; // 0 = ALL batch years, or specific year e.g. 2027, 2028

    private String jobDescription;

    public Drive(int id, String companyName, double ctc, String eligibleBranch, double minCgpa, int maxBacklogs) {
        this(id, companyName, ctc, eligibleBranch, minCgpa, maxBacklogs, 0);
    }

    public Drive(int id, String companyName, double ctc, String eligibleBranch, double minCgpa, int maxBacklogs, int targetYear) {
        this.id = id;
        this.companyName = companyName;
        this.ctc = ctc;
        this.eligibleBranch = eligibleBranch;
        this.minCgpa = minCgpa;
        this.maxBacklogs = maxBacklogs;
        this.targetYear = targetYear;
        this.requiredSkills = "";
        
        // Defaults for fields
        this.driveDate = "Will be Notified Later";
        this.registerBy = "06/07/2026 10:00";
        this.venue = "From Your Respective location";
        this.jobRole = "Software Engineer";
        this.specialization = "Core Technology";
        this.status = "Registration Open";
        this.jobDescription = "Responsible for designing, developing, testing, and deploying robust scalable software solutions.";
    }

    public int getId() { return id; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public double getCtc() { return ctc; }
    public void setCtc(double ctc) { this.ctc = ctc; }
    
    public String getEligibleBranch() { return eligibleBranch; }
    public void setEligibleBranch(String eligibleBranch) { this.eligibleBranch = eligibleBranch; }
    
    public double getMinCgpa() { return minCgpa; }
    public void setMinCgpa(double minCgpa) { this.minCgpa = minCgpa; }
    
    public int getMaxBacklogs() { return maxBacklogs; }
    public void setMaxBacklogs(int maxBacklogs) { this.maxBacklogs = maxBacklogs; }
    
    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
    
    public String getDriveDate() { return driveDate; }
    public void setDriveDate(String driveDate) { this.driveDate = driveDate; }
    
    public String getRegisterBy() { return registerBy; }
    public void setRegisterBy(String registerBy) { this.registerBy = registerBy; }
    
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    
    public String getJobRole() { return jobRole; }
    public void setJobRole(String jobRole) { this.jobRole = jobRole; }
    
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public int getTargetYear() { return targetYear; }
    public void setTargetYear(int targetYear) { this.targetYear = targetYear; }

    @Override
    public String toString() {
        String yearStr = targetYear == 0 ? "ALL" : String.valueOf(targetYear);
        return String.format("| %-3d | %-15s | %-6.2f LPA | %-8s | %-7.2f | %-11d | Target Year: %-4s |",
                id, companyName, ctc, eligibleBranch, minCgpa, maxBacklogs, yearStr);
    }
}
