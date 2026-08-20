/**
 * Drive Model for Placement Management System.
 * 
 * Represents a company placement drive with eligibility criteria.
 * Used by QuickSort (sort by CTC) and MaxHeap (Top-K drives).
 */
public class Drive {
    private int id;
    private String companyName;
    private double ctc;             // in LPA (Lakhs Per Annum)
    private String eligibleBranch;  // "ALL" means all branches eligible
    private double minCgpa;
    private int maxBacklogs;
    private int targetYear;         // 0 = ALL batch years
    private String requiredSkills;
    private String jobRole;

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
        this.jobRole = "Software Engineer";
    }

    // Getters
    public int getId() { return id; }
    public String getCompanyName() { return companyName; }
    public double getCtc() { return ctc; }
    public String getEligibleBranch() { return eligibleBranch; }
    public double getMinCgpa() { return minCgpa; }
    public int getMaxBacklogs() { return maxBacklogs; }
    public int getTargetYear() { return targetYear; }
    public String getRequiredSkills() { return requiredSkills; }
    public String getJobRole() { return jobRole; }

    // Setters
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setCtc(double ctc) { this.ctc = ctc; }
    public void setEligibleBranch(String eligibleBranch) { this.eligibleBranch = eligibleBranch; }
    public void setMinCgpa(double minCgpa) { this.minCgpa = minCgpa; }
    public void setMaxBacklogs(int maxBacklogs) { this.maxBacklogs = maxBacklogs; }
    public void setTargetYear(int targetYear) { this.targetYear = targetYear; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
    public void setJobRole(String jobRole) { this.jobRole = jobRole; }

    @Override
    public String toString() {
        String yearStr = targetYear == 0 ? "ALL" : String.valueOf(targetYear);
        return String.format("| %-3d | %-15s | %-6.2f LPA | %-8s | %-7.2f | %-11d | Year: %-4s |",
                id, companyName, ctc, eligibleBranch, minCgpa, maxBacklogs, yearStr);
    }
}
