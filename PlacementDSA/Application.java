/**
 * Application class representing a student's application to a placement drive.
 * Uses CustomStack to track status history (DSA: Stack - LIFO).
 * 
 * Status Flow: Applied -> Shortlisted -> Interview -> Selected/Rejected
 * Undo operation pops the last status (demonstrates stack pop).
 */
public class Application {
    private int studentId;
    private int driveId;
    private String studentName;
    private String companyName;
    private CustomStack statusHistory;

    public Application(int studentId, int driveId, String studentName, String companyName) {
        this.studentId = studentId;
        this.driveId = driveId;
        this.studentName = studentName;
        this.companyName = companyName;
        this.statusHistory = new CustomStack(10);
        // Initial status when application is created
        this.statusHistory.push("Applied");
    }

    public int getStudentId() { return studentId; }
    public int getDriveId() { return driveId; }
    public String getStudentName() { return studentName; }
    public String getCompanyName() { return companyName; }
    public CustomStack getStatusHistory() { return statusHistory; }

    // Get current status (peek at top of stack) - O(1)
    public String getCurrentStatus() {
        return statusHistory.peek();
    }

    // Update status (push to stack) - O(1)
    public void updateStatus(String newStatus) {
        statusHistory.push(newStatus);
    }

    // Undo last status change (pop from stack) - O(1)
    public String undoStatus() {
        if (statusHistory.size() <= 1) {
            return null; // Cannot undo initial "Applied" status
        }
        return statusHistory.pop();
    }

    // Get full status history
    public void displayHistory() {
        System.out.println("  Application: " + studentName + " -> " + companyName);
        statusHistory.display();
    }

    // Get number of status changes
    public int getStatusCount() {
        return statusHistory.size();
    }

    @Override
    public String toString() {
        return String.format("  %s -> %s [Current: %s] (%d status changes)",
                studentName, companyName, getCurrentStatus(), getStatusCount());
    }
}
