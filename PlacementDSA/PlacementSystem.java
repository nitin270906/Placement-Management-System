import java.util.ArrayList;
import java.io.*;

/**
 * Core Placement System Logic
 * 
 * This class ties together all 4 DSA concepts:
 * 
 * 1. Eligibility Filter + QuickSort (Searching + Sorting)
 *    - Linear search to filter eligible drives
 *    - QuickSort O(n log n) to rank by CTC descending
 * 
 * 2. Application Status Tracker (Custom Stack - LIFO)
 *    - push() to add new status
 *    - pop() to undo last status
 *    - peek() to view current status
 * 
 * 3. Top-K Placements (Custom Max-Heap)
 *    - insert() all drives into heap
 *    - extractMax() K times to get top drives
 * 
 * 4. Interview Scheduling (Custom Queue - FIFO)
 *    - enqueue() to add student to interview queue
 *    - dequeue() to start next interview
 * 
 * Also includes data persistence (save/load from disk).
 */
public class PlacementSystem {

    private String getDataDir() {
        File placementDSAFolder = new File("PlacementDSA");
        if (placementDSAFolder.exists() && placementDSAFolder.isDirectory()) {
            return "PlacementDSA/data";
        }
        return "data";
    }

    private String getStudentsFile() { return getDataDir() + "/students.txt"; }
    private String getDrivesFile() { return getDataDir() + "/drives.txt"; }
    private String getApplicationsFile() { return getDataDir() + "/applications.txt"; }

    private Student[] students;
    private Drive[] drives;
    private ArrayList<Application> applications;
    private CustomQueue[] interviewQueues;
    private int studentCount;
    private int driveCount;

    public PlacementSystem() {
        students = new Student[50];
        drives = new Drive[20];
        applications = new ArrayList<>();
        interviewQueues = new CustomQueue[20];
        studentCount = 0;
        driveCount = 0;

        if (!loadAllDataFromDisk()) {
            loadSampleData();
            saveAllDataToDisk();
        }
    }

    // ==================== SAMPLE DATA ====================

    private void loadSampleData() {
        // Sample Students with diverse profiles
        addStudent(new Student(1, "Aarav Sharma", 8.5, "CSE", 0, 2027));
        addStudent(new Student(2, "Priya Patel", 9.1, "CSE", 0, 2027));
        addStudent(new Student(3, "Rahul Verma", 7.2, "ECE", 1, 2028));
        addStudent(new Student(4, "Sneha Gupta", 8.8, "IT", 0, 2027));
        addStudent(new Student(5, "Amit Kumar", 6.5, "MECH", 2, 2028));
        addStudent(new Student(6, "Neha Singh", 7.8, "CSE", 0, 2027));
        addStudent(new Student(7, "Vikram Reddy", 8.2, "ECE", 0, 2027));
        addStudent(new Student(8, "Ananya Joshi", 9.4, "CSE", 0, 2027));
        addStudent(new Student(9, "Rohan Mishra", 6.9, "IT", 1, 2028));
        addStudent(new Student(10, "Kavya Nair", 7.5, "CSE", 0, 2027));
        addStudent(new Student(11, "Deepak Yadav", 5.8, "MECH", 3, 2026));
        addStudent(new Student(12, "Pooja Agarwal", 8.0, "ECE", 0, 2027));

        // Sample Placement Drives (targetYear: 0 = ALL batches)
        addDrive(new Drive(1, "TCS", 3.6, "ALL", 6.0, 1, 0));
        addDrive(new Drive(2, "Infosys", 4.5, "ALL", 6.5, 0, 2027));
        addDrive(new Drive(3, "Wipro", 3.8, "ALL", 6.0, 1, 0));
        addDrive(new Drive(4, "Google", 25.0, "CSE", 8.5, 0, 2027));
        addDrive(new Drive(5, "Microsoft", 20.0, "CSE", 8.0, 0, 2027));
        addDrive(new Drive(6, "Amazon", 18.0, "ALL", 7.5, 0, 2027));
        addDrive(new Drive(7, "Deloitte", 7.0, "ALL", 7.0, 0, 2028));
        addDrive(new Drive(8, "Goldman Sachs", 15.0, "CSE", 8.0, 0, 2027));
        addDrive(new Drive(9, "Tata Motors", 5.5, "MECH", 6.5, 1, 2028));
        addDrive(new Drive(10, "HCL Tech", 4.2, "ALL", 6.0, 2, 0));
    }

    private void addStudent(Student s) {
        if (studentCount < students.length) {
            students[studentCount++] = s;
        }
    }

    private void addDrive(Drive d) {
        if (driveCount < drives.length) {
            drives[driveCount] = d;
            interviewQueues[driveCount] = new CustomQueue(50);
            driveCount++;
        }
    }

    // ==================== FEATURE 1: ELIGIBILITY FILTER + QUICKSORT ====================

    /**
     * Filter drives that a student is eligible for based on:
     * - CGPA >= minimum required CGPA
     * - Branch matches (or drive is open to ALL)
     * - Backlogs <= maximum allowed
     * - Target Batch Year matches (or target year is 0 for ALL)
     * 
     * Then sorts eligible drives by CTC using Custom QuickSort (descending).
     * 
     * Time Complexity: O(m) for filtering + O(m log m) for sorting
     * where m = number of eligible drives
     */
    public Drive[] getEligibleDrives(int studentId) {
        Student student = findStudentById(studentId);
        if (student == null) return null;

        // Step 1: Filter eligible drives - O(m) linear scan
        Drive[] eligible = new Drive[driveCount];
        int eligibleCount = 0;

        for (int i = 0; i < driveCount; i++) {
            Drive d = drives[i];
            boolean cgpaOk = student.getCgpa() >= d.getMinCgpa();
            boolean branchOk = d.getEligibleBranch().equals("ALL") ||
                               d.getEligibleBranch().equals(student.getBranch());
            boolean backlogOk = student.getBacklogs() <= d.getMaxBacklogs();
            boolean yearOk = (d.getTargetYear() == 0) || (d.getTargetYear() == student.getBatchYear());

            if (cgpaOk && branchOk && backlogOk && yearOk) {
                eligible[eligibleCount++] = d;
            }
        }

        // Step 2: Sort eligible drives by CTC descending using Custom QuickSort - O(m log m)
        QuickSort.sortByCTC(eligible, eligibleCount);

        // Return trimmed array
        Drive[] result = new Drive[eligibleCount];
        for (int i = 0; i < eligibleCount; i++) {
            result[i] = eligible[i];
        }
        return result;
    }

    // ==================== FEATURE 2: APPLICATION TRACKER (STACK) ====================

    /**
     * Apply a student to a drive.
     * Creates an Application object with a CustomStack to track status history.
     * 
     * Stack operations used:
     * - push("Applied") on creation
     * - push(newStatus) on status update
     * - pop() on undo
     * - peek() to get current status
     */
    public String applyToDrive(int studentId, int driveId) {
        Student student = findStudentById(studentId);
        Drive drive = findDriveById(driveId);

        if (student == null) return "Error: Student not found.";
        if (drive == null) return "Error: Drive not found.";

        // Check if already applied
        for (Application app : applications) {
            if (app.getStudentId() == studentId && app.getDriveId() == driveId) {
                return "Error: " + student.getName() + " has already applied to " + drive.getCompanyName();
            }
        }

        // Check eligibility
        boolean cgpaOk = student.getCgpa() >= drive.getMinCgpa();
        boolean branchOk = drive.getEligibleBranch().equals("ALL") ||
                           drive.getEligibleBranch().equals(student.getBranch());
        boolean backlogOk = student.getBacklogs() <= drive.getMaxBacklogs();

        if (!cgpaOk || !branchOk || !backlogOk) {
            return "Error: " + student.getName() + " is NOT eligible for " + drive.getCompanyName();
        }

        Application app = new Application(studentId, driveId, student.getName(), drive.getCompanyName());
        applications.add(app);
        saveAllDataToDisk();
        return "Success: " + student.getName() + " applied to " + drive.getCompanyName() + " [Status: Applied]";
    }

    /**
     * Update application status - pushes new status onto stack.
     * Valid statuses: Applied -> Shortlisted -> Interview -> Selected/Rejected
     */
    public String updateApplicationStatus(int studentId, int driveId, String newStatus) {
        Application app = findApplication(studentId, driveId);
        if (app == null) return "Error: Application not found.";

        app.updateStatus(newStatus);
        saveAllDataToDisk();
        return "Status updated: " + app.getStudentName() + " -> " + app.getCompanyName() +
               " [New Status: " + newStatus + "]";
    }

    /**
     * Undo last status change - pops from stack.
     * Demonstrates Stack pop() operation.
     */
    public String undoApplicationStatus(int studentId, int driveId) {
        Application app = findApplication(studentId, driveId);
        if (app == null) return "Error: Application not found.";

        String removed = app.undoStatus();
        if (removed == null) {
            return "Cannot undo: Already at initial 'Applied' status.";
        }
        return "Undo successful: Removed '" + removed + "'. Current status: " + app.getCurrentStatus();
    }

    // ==================== FEATURE 3: TOP-K PLACEMENTS (MAX-HEAP) ====================

    /**
     * Get Top-K placement drives by CTC using Custom Max-Heap.
     * 
     * Algorithm:
     * 1. Insert all drives into Max-Heap - O(n log n)
     * 2. Extract max K times - O(K log n)
     * 
     * Total: O(n log n + K log n) = O(n log n)
     * This is more efficient than sorting when K << n.
     */
    public Drive[] getTopKDrives(int k) {
        if (k > driveCount) k = driveCount;

        // Build Max-Heap with all drives
        MaxHeap heap = new MaxHeap(driveCount);
        for (int i = 0; i < driveCount; i++) {
            heap.insert(drives[i]);
        }

        // Extract top K
        Drive[] topK = new Drive[k];
        for (int i = 0; i < k; i++) {
            topK[i] = heap.extractMax();
        }
        return topK;
    }

    /**
     * Get Top-K eligible drives for a specific student using heap.
     */
    public Drive[] getTopKEligibleDrives(int studentId, int k) {
        Drive[] eligible = getEligibleDrives(studentId);
        if (eligible == null || eligible.length == 0) return null;

        if (k > eligible.length) k = eligible.length;

        // Demonstrate heap on eligible drives
        MaxHeap heap = new MaxHeap(eligible.length);
        for (Drive d : eligible) {
            heap.insert(d);
        }

        Drive[] topK = new Drive[k];
        for (int i = 0; i < k; i++) {
            topK[i] = heap.extractMax();
        }
        return topK;
    }

    // ==================== FEATURE 4: INTERVIEW SCHEDULING (QUEUE) ====================

    /**
     * Add student application to the interview queue for a specific drive.
     * Demonstrates Queue enqueue() operation - O(1).
     */
    public String scheduleInterview(int studentId, int driveId) {
        Application app = findApplication(studentId, driveId);
        if (app == null) return "Error: Application not found. Student must apply first.";

        int driveIndex = -1;
        for (int i = 0; i < driveCount; i++) {
            if (drives[i].getId() == driveId) {
                driveIndex = i;
                break;
            }
        }

        if (driveIndex == -1) return "Error: Drive not found.";

        interviewQueues[driveIndex].enqueue(app);
        app.updateStatus("Interview_Scheduled");
        return "Interview scheduled for " + app.getStudentName() + " at " + app.getCompanyName() +
               " [Queue position: " + interviewQueues[driveIndex].getSize() + "]";
    }

    /**
     * Start the next interview by dequeuing the first student.
     * Demonstrates Queue dequeue() operation - O(1).
     */
    public String startNextInterview(int driveId) {
        int driveIndex = -1;
        for (int i = 0; i < driveCount; i++) {
            if (drives[i].getId() == driveId) {
                driveIndex = i;
                break;
            }
        }

        if (driveIndex == -1) return "Error: Drive not found.";

        Application app = interviewQueues[driveIndex].dequeue();
        if (app == null) {
            return "No students waiting in the interview queue for " + drives[driveIndex].getCompanyName();
        }

        app.updateStatus("Interview_In_Progress");
        return "Interview started for " + app.getStudentName() + " at " + app.getCompanyName();
    }

    /**
     * View all students in the interview queue for a drive.
     */
    public void displayInterviewQueue(int driveId) {
        int driveIndex = -1;
        for (int i = 0; i < driveCount; i++) {
            if (drives[i].getId() == driveId) {
                driveIndex = i;
                break;
            }
        }

        if (driveIndex == -1) {
            System.out.println("  Drive not found.");
            return;
        }

        CustomQueue queue = interviewQueues[driveIndex];
        if (queue.isEmpty()) {
            System.out.println("  Interview queue is empty for " + drives[driveIndex].getCompanyName());
            return;
        }

        System.out.println("  Interview Queue for " + drives[driveIndex].getCompanyName() + ":");
        Application[] items = queue.toArray();
        for (int i = 0; i < items.length; i++) {
            System.out.println("    [" + (i + 1) + "] " + items[i].getStudentName() + " (Student ID: " + items[i].getStudentId() + ")");
        }
        System.out.println("  Queue size: " + queue.getSize());
    }

    // ==================== UTILITY / LOOKUP METHODS ====================

    /**
     * Linear search for student by ID - O(n)
     */
    public Student findStudentById(int id) {
        for (int i = 0; i < studentCount; i++) {
            if (students[i].getId() == id) return students[i];
        }
        return null;
    }

    /**
     * Linear search for drive by ID - O(n)
     */
    public Drive findDriveById(int id) {
        for (int i = 0; i < driveCount; i++) {
            if (drives[i].getId() == id) return drives[i];
        }
        return null;
    }

    private Application findApplication(int studentId, int driveId) {
        for (Application app : applications) {
            if (app.getStudentId() == studentId && app.getDriveId() == driveId) {
                return app;
            }
        }
        return null;
    }

    // ==================== DISPLAY METHODS ====================

    public void displayAllStudents() {
        System.out.println("\n  +-----+----------------------+------+--------+----------+-----------+");
        System.out.println("  | ID  | Name                 | CGPA | Branch | Backlogs | Batch     |");
        System.out.println("  +-----+----------------------+------+--------+----------+-----------+");
        for (int i = 0; i < studentCount; i++) {
            System.out.println("  " + students[i]);
        }
        System.out.println("  +-----+----------------------+------+--------+----------+-----------+");
    }

    public void displayAllDrives() {
        System.out.println("\n  +-----+-----------------+------------+----------+---------+-------------+-----------+");
        System.out.println("  | ID  | Company         | CTC        | Branch   | Min CGA | Max Backlog | Year      |");
        System.out.println("  +-----+-----------------+------------+----------+---------+-------------+-----------+");
        for (int i = 0; i < driveCount; i++) {
            System.out.println("  " + drives[i]);
        }
        System.out.println("  +-----+-----------------+------------+----------+---------+-------------+-----------+");
    }

    public void displayAllApplications() {
        if (applications.isEmpty()) {
            System.out.println("\n  No applications yet.");
            return;
        }
        System.out.println("\n  === All Applications ===");
        for (Application app : applications) {
            System.out.println(app);
        }
    }

    public void displayApplicationHistory(int studentId, int driveId) {
        Application app = findApplication(studentId, driveId);
        if (app == null) {
            System.out.println("  Application not found.");
            return;
        }
        app.displayHistory();
    }

    public int getStudentCount() { return studentCount; }
    public int getDriveCount() { return driveCount; }

    // ==================== DATA PERSISTENCE ====================

    public synchronized void saveAllDataToDisk() {
        try {
            File dir = new File(getDataDir());
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 1. Save Students
            PrintWriter studentWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(getStudentsFile()), "UTF-8"));
            for (int i = 0; i < studentCount; i++) {
                Student s = students[i];
                studentWriter.println(
                        s.getId() + "||" +
                        s.getName() + "||" +
                        s.getCgpa() + "||" +
                        s.getBranch() + "||" +
                        s.getBacklogs() + "||" +
                        s.getBatchYear() + "||" +
                        s.getSkills()
                );
            }
            studentWriter.close();

            // 2. Save Drives
            PrintWriter driveWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(getDrivesFile()), "UTF-8"));
            for (int i = 0; i < driveCount; i++) {
                Drive d = drives[i];
                driveWriter.println(
                        d.getId() + "||" +
                        d.getCompanyName() + "||" +
                        d.getCtc() + "||" +
                        d.getEligibleBranch() + "||" +
                        d.getMinCgpa() + "||" +
                        d.getMaxBacklogs() + "||" +
                        d.getTargetYear() + "||" +
                        d.getRequiredSkills() + "||" +
                        d.getJobRole()
                );
            }
            driveWriter.close();

            // 3. Save Applications
            PrintWriter appWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(getApplicationsFile()), "UTF-8"));
            for (Application app : applications) {
                String[] historyItems = app.getStatusHistory().getItems();
                String historyJoined = String.join("~~~", historyItems);
                appWriter.println(
                        app.getStudentId() + "||" +
                        app.getDriveId() + "||" +
                        app.getStudentName() + "||" +
                        app.getCompanyName() + "||" +
                        historyJoined
                );
            }
            appWriter.close();
        } catch (Exception e) {
            System.err.println("Error saving data to disk: " + e.getMessage());
        }
    }

    public synchronized boolean loadAllDataFromDisk() {
        File sf = new File(getStudentsFile());
        File df = new File(getDrivesFile());
        File af = new File(getApplicationsFile());

        if (!sf.exists() || !df.exists()) {
            return false;
        }

        try {
            studentCount = 0;
            driveCount = 0;
            applications.clear();

            // 1. Read Students
            BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(sf), "UTF-8"));
            String line;
            while ((line = sr.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|\\|", -1);
                if (parts.length >= 7) {
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1];
                    double cgpa = Double.parseDouble(parts[2].trim());
                    String branch = parts[3];
                    int backlogs = Integer.parseInt(parts[4].trim());
                    int batchYear = Integer.parseInt(parts[5].trim());
                    String skills = parts[6];

                    Student s = new Student(id, name, cgpa, branch, backlogs, batchYear);
                    s.setSkills(skills);
                    addStudent(s);
                }
            }
            sr.close();

            // 2. Read Drives
            BufferedReader dr = new BufferedReader(new InputStreamReader(new FileInputStream(df), "UTF-8"));
            while ((line = dr.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|\\|", -1);
                if (parts.length >= 9) {
                    int id = Integer.parseInt(parts[0].trim());
                    String companyName = parts[1];
                    double ctc = Double.parseDouble(parts[2].trim());
                    String eligibleBranch = parts[3];
                    double minCgpa = Double.parseDouble(parts[4].trim());
                    int maxBacklogs = Integer.parseInt(parts[5].trim());
                    int targetYear = Integer.parseInt(parts[6].trim());
                    String skills = parts[7];
                    String jobRole = parts[8];

                    Drive d = new Drive(id, companyName, ctc, eligibleBranch, minCgpa, maxBacklogs, targetYear);
                    d.setRequiredSkills(skills);
                    d.setJobRole(jobRole);
                    addDrive(d);
                }
            }
            dr.close();

            // 3. Read Applications
            if (af.exists()) {
                BufferedReader ar = new BufferedReader(new InputStreamReader(new FileInputStream(af), "UTF-8"));
                while ((line = ar.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split("\\|\\|", -1);
                    if (parts.length >= 5) {
                        int sId = Integer.parseInt(parts[0].trim());
                        int dId = Integer.parseInt(parts[1].trim());
                        String sName = parts[2];
                        String cName = parts[3];
                        String historyStr = parts[4];

                        Application app = new Application(sId, dId, sName, cName);
                        if (!historyStr.isEmpty()) {
                            String[] items = historyStr.split("~~~", -1);
                            while (app.getStatusHistory().size() > 0) {
                                app.getStatusHistory().pop();
                            }
                            for (String item : items) {
                                if (!item.isEmpty()) {
                                    app.getStatusHistory().push(item);
                                }
                            }
                        }
                        applications.add(app);
                    }
                }
                ar.close();
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error loading data from disk: " + e.getMessage());
            return false;
        }
    }
}
