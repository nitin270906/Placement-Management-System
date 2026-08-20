import java.util.ArrayList;
import java.io.*;

/**
 * Core Placement System Logic
 * 
 * This class ties together all 3 DSA features:
 * 1. Eligibility Filter + QuickSort (Searching + Sorting)
 * 2. Application Status Tracker (Custom Stack - LIFO)
 * 3. Top-K Placements (Custom Max-Heap)
 * 
 * Also uses HashMap concept via manual linear search for lookups.
 * Includes Automatic Data Persistence (Save & Restore from Disk).
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
        students = new Student[100];
        drives = new Drive[50];
        applications = new ArrayList<>();
        interviewQueues = new CustomQueue[50];
        studentCount = 0;
        driveCount = 0;

        if (!loadAllDataFromDisk()) {
            loadSampleData();
            saveAllDataToDisk();
        }
    }

    // ==================== DATA LOADING ====================

    private void loadSampleData() {
        // Sample Students across diverse branches with 8-digit IDs & Strong Passwords
        Student s1 = new Student(10000001, "Aarav Sharma", 8.5, "CSE", 0, 2027);
        s1.setPassword("Pass@123");
        s1.setSkills("java, spring boot, sql, microservices, data structures");
        addStudent(s1);

        Student s2 = new Student(10000002, "Priya Patel", 9.1, "AI & DS", 0, 2027);
        s2.setPassword("Pass@123");
        s2.setSkills("python, machine learning, deep learning, pytorch, nlp");
        addStudent(s2);

        Student s3 = new Student(10000003, "Rahul Verma", 7.2, "ECE", 1, 2028);
        s3.setPassword("Pass@123");
        s3.setSkills("embedded systems, c++, vlsi, matlab");
        addStudent(s3);

        Student s4 = new Student(10000004, "Sneha Gupta", 8.8, "IT", 0, 2027);
        s4.setPassword("Pass@123");
        s4.setSkills("react, node.js, javascript, html, css");
        addStudent(s4);

        Student s5 = new Student(10000005, "Amit Kumar", 6.5, "MECH", 2, 2028);
        s5.setPassword("Pass@123");
        s5.setSkills("cad, solidworks, robotics, thermodynamics");
        addStudent(s5);

        Student s6 = new Student(10000006, "Neha Singh", 7.8, "CIVIL", 0, 2027);
        s6.setPassword("Pass@123");
        s6.setSkills("autocad, structural analysis, project management");
        addStudent(s6);

        Student s7 = new Student(10000007, "Vikram Reddy", 8.2, "EEE", 0, 2027);
        s7.setPassword("Pass@123");
        s7.setSkills("power systems, matlab, c++, iot");
        addStudent(s7);

        Student s8 = new Student(10000008, "Ananya Joshi", 9.4, "CSE", 0, 2027);
        s8.setPassword("Pass@123");
        s8.setSkills("python, machine learning, cloud, docker, java");
        addStudent(s8);

        Student s9 = new Student(10000009, "Rohan Mishra", 7.9, "AUTOMOBILE", 0, 2027);
        s9.setPassword("Pass@123");
        s9.setSkills("cad, robotics, thermodynamics, solidworks");
        addStudent(s9);

        Student s10 = new Student(10000010, "Kavya Nair", 8.1, "CHEMICAL", 0, 2027);
        s10.setPassword("Pass@123");
        s10.setSkills("chemical kinetics, matlab, data analysis");
        addStudent(s10);

        // Sample Placement Drives across diverse branches with detailed JDs
        Drive d1 = new Drive(1, "Google", 25.0, "CSE", 8.5, 0, 2027);
        d1.setJobRole("Software Development Engineer (SDE-1)");
        d1.setRequiredSkills("java, c++, data structures, algorithms");
        d1.setRegisterBy("15/08/2026 23:59");
        d1.setDriveDate("20/08/2026");
        d1.setVenue("Virtual / Online Coding Platform");
        d1.setJobDescription("Join Google's Core Engineering team to build large-scale distributed cloud systems, real-time query engines, and high-performance algorithms.");
        addDrive(d1);

        Drive d2 = new Drive(2, "NVIDIA", 22.0, "AI & DS", 8.0, 0, 2027);
        d2.setJobRole("AI / CUDA Systems Research Engineer");
        d2.setRequiredSkills("python, machine learning, deep learning, pytorch, c++");
        d2.setRegisterBy("18/08/2026 20:00");
        d2.setDriveDate("25/08/2026");
        d2.setVenue("NVIDIA R&D Campus, Bengaluru");
        d2.setJobDescription("Work on next-gen Generative AI models, GPU acceleration kernels, and LLM optimization tools using PyTorch and CUDA.");
        addDrive(d2);

        Drive d3 = new Drive(3, "Qualcomm", 16.5, "ECE", 7.5, 0, 2027);
        d3.setJobRole("SoC Firmware & Embedded Hardware Engineer");
        d3.setRequiredSkills("embedded systems, c++, vlsi, matlab");
        d3.setRegisterBy("20/08/2026 18:00");
        d3.setDriveDate("28/08/2026");
        d3.setVenue("Qualcomm India Tower, Hyderabad");
        d3.setJobDescription("Design and optimize Snapdragon mobile processor firmware, 5G RF modem code, and low-latency embedded Linux kernels.");
        addDrive(d3);

        Drive d4 = new Drive(4, "Tesla India R&D", 19.0, "AUTOMOBILE", 7.5, 0, 2027);
        d4.setJobRole("EV Battery & Powertrain Design Engineer");
        d4.setRequiredSkills("cad, solidworks, robotics, thermodynamics");
        d4.setRegisterBy("22/08/2026 17:00");
        d4.setDriveDate("30/08/2026");
        d4.setVenue("Tesla Tech Hub, Pune");
        d4.setJobDescription("Design electric vehicle battery thermal management systems, chassis structural CAD models, and automated robotic assembly simulations.");
        addDrive(d4);

        Drive d5 = new Drive(5, "L&T Construction", 7.5, "CIVIL", 6.5, 1, 0);
        d5.setJobRole("Graduate Civil Structural Engineer");
        d5.setRequiredSkills("autocad, structural analysis, project management");
        d5.setRegisterBy("25/08/2026 23:59");
        d5.setDriveDate("02/09/2026");
        d5.setVenue("L&T Campus, Chennai");
        d5.setJobDescription("Oversee mega-infrastructure projects including bridges, high-rise towers, and metro rail structural analysis using AutoCAD and STAAD Pro.");
        addDrive(d5);

        Drive d6 = new Drive(6, "Schneider Electric", 9.0, "EEE", 7.0, 0, 2027);
        d6.setJobRole("Smart Grid & Electrical Automation Engineer");
        d6.setRequiredSkills("power systems, matlab, c++, iot");
        d6.setRegisterBy("28/08/2026 18:00");
        d6.setDriveDate("05/09/2026");
        d6.setVenue("Schneider Innovation Hub, Gurgaon");
        d6.setJobDescription("Engineers smart power grids, renewable energy sub-stations, and IoT-enabled industrial power automation switchboards.");
        addDrive(d6);

        Drive d7 = new Drive(7, "Reliance Industries", 8.5, "CHEMICAL", 6.8, 0, 0);
        d7.setJobRole("Chemical Process & Refinery Plant Engineer");
        d7.setRequiredSkills("chemical kinetics, matlab, data analysis");
        d7.setRegisterBy("30/08/2026 17:00");
        d7.setDriveDate("08/09/2026");
        d7.setVenue("RIL Complex, Jamnagar");
        d7.setJobDescription("Monitor and optimize petrochemical refining columns, polymer synthesis reactors, and green hydrogen energy units.");
        addDrive(d7);

        Drive d8 = new Drive(8, "Tata Motors", 6.8, "MECH", 6.5, 1, 2028);
        d8.setJobRole("Vehicle Dynamics & Mechatronics Engineer");
        d8.setRequiredSkills("cad, solidworks, robotics, thermodynamics");
        d8.setRegisterBy("02/09/2026 17:00");
        d8.setDriveDate("10/09/2026");
        d8.setVenue("Tata Motors Plant, Jamshedpur");
        d8.setJobDescription("Work on commercial vehicle chassis stress testing, CAD 3D modeling, and mechatronic suspension system calibration.");
        addDrive(d8);

        Drive d9 = new Drive(9, "Microsoft", 20.0, "ALL", 8.0, 0, 2027);
        d9.setJobRole("Cloud & Full Stack Engineer (Azure)");
        d9.setRequiredSkills("python, react, node.js, cloud, docker");
        d9.setRegisterBy("05/09/2026 20:00");
        d9.setDriveDate("12/09/2026");
        d9.setVenue("Microsoft IDC, Hyderabad");
        d9.setJobDescription("Build enterprise Azure cloud microservices, web user interfaces, and scalable API backend infrastructures.");
        addDrive(d9);

        Drive d10 = new Drive(10, "TCS Digital", 7.5, "ALL", 6.5, 0, 0);
        d10.setJobRole("Cybersecurity Specialist & Penetration Tester");
        d10.setRequiredSkills("cybersecurity, c++, networking, git, sql");
        d10.setRegisterBy("10/09/2026 23:59");
        d10.setDriveDate("18/09/2026");
        d10.setVenue("TCS Digital Innovation Hub");
        d10.setJobDescription("Perform ethical hacking, cloud security audits, vulnerability assessments, and secure code reviews for global enterprise software.");
        addDrive(d10);

        Drive d11 = new Drive(11, "Apple", 28.5, "CSE", 8.5, 0, 2027);
        d11.setJobRole("iOS Systems & Swift Kernel Engineer");
        d11.setRequiredSkills("c++, java, data structures, algorithms, git");
        d11.setRegisterBy("12/09/2026 23:59");
        d11.setDriveDate("20/09/2026");
        d11.setVenue("Apple India R&D Center, Bengaluru");
        d11.setJobDescription("Engineers core iOS Darwin kernel subsystems, low-overhead Swift runtime performance, and Apple Silicon hardware abstraction layers.");
        addDrive(d11);

        Drive d12 = new Drive(12, "Adobe", 24.0, "AI & DS", 8.2, 0, 2027);
        d12.setJobRole("Computer Vision & Creative Cloud Engineer");
        d12.setRequiredSkills("python, machine learning, deep learning, c++, nlp");
        d12.setRegisterBy("14/09/2026 20:00");
        d12.setDriveDate("22/09/2026");
        d12.setVenue("Adobe Systems Campus, Noida");
        d12.setJobDescription("Develop AI-powered photo and video editing algorithms (Firefly GenAI), neural rendering graphics pipelines, and multimedia cloud engines.");
        addDrive(d12);

        Drive d13 = new Drive(13, "Goldman Sachs", 21.0, "ALL", 8.0, 0, 2027);
        d13.setJobRole("Quantitative Risk & High-Frequency Trading Analyst");
        d13.setRequiredSkills("c++, python, sql, data structures, matlab");
        d13.setRegisterBy("16/09/2026 18:00");
        d13.setDriveDate("24/09/2026");
        d13.setVenue("Goldman Sachs Tower, Bengaluru");
        d13.setJobDescription("Build sub-millisecond ultra-low-latency financial trading algorithms, risk simulation engines, and global market predictive analytics.");
        addDrive(d13);

        Drive d14 = new Drive(14, "Salesforce", 19.5, "IT", 7.8, 0, 2027);
        d14.setJobRole("Enterprise Cloud Platform Architect");
        d14.setRequiredSkills("java, spring boot, react, node.js, cloud");
        d14.setRegisterBy("18/09/2026 23:59");
        d14.setDriveDate("26/09/2026");
        d14.setVenue("Salesforce Tower, Hyderabad");
        d14.setJobDescription("Architect enterprise multi-tenant CRM cloud services, real-time data streaming pipelines, and microservices architecture on AWS.");
        addDrive(d14);

        Drive d15 = new Drive(15, "AMD", 18.0, "ECE", 7.5, 0, 2027);
        d15.setJobRole("GPU Microarchitecture & Silicon Design Engineer");
        d15.setRequiredSkills("vlsi, embedded systems, c++, matlab");
        d15.setRegisterBy("20/09/2026 18:00");
        d15.setDriveDate("28/09/2026");
        d15.setVenue("AMD Tech Center, Hyderabad");
        d15.setJobDescription("Design Ryzen and Radeon micro-architecture logic gates, Verilog RTL code, and post-silicon hardware validation suites.");
        addDrive(d15);

        Drive d16 = new Drive(16, "Intel", 17.5, "ECE", 7.5, 0, 2027);
        d16.setJobRole("Core Hardware & Chip Verification Specialist");
        d16.setRequiredSkills("vlsi, embedded systems, c++, power systems");
        d16.setRegisterBy("22/09/2026 17:00");
        d16.setDriveDate("30/09/2026");
        d16.setVenue("Intel India Campus, Bengaluru");
        d16.setJobDescription("Perform SystemVerilog verification, UVM testbench creation, and power/performance optimization for next-gen Xeon server processors.");
        addDrive(d16);

        Drive d17 = new Drive(17, "J.P. Morgan Chase", 16.0, "ALL", 7.5, 0, 2027);
        d17.setJobRole("FinTech & Distributed Blockchain Systems Engineer");
        d17.setRequiredSkills("java, spring boot, sql, cloud, docker");
        d17.setRegisterBy("25/09/2026 23:59");
        d17.setDriveDate("03/10/2026");
        d17.setVenue("JPMC Global Technology Center, Mumbai");
        d17.setJobDescription("Develop high-throughput banking payment gateways, ledger security infrastructure, and enterprise distributed blockchain solutions.");
        addDrive(d17);

        Drive d18 = new Drive(18, "Boeing", 14.5, "AUTOMOBILE", 7.2, 0, 2027);
        d18.setJobRole("Aerospace Structural & Flight Systems Engineer");
        d18.setRequiredSkills("cad, solidworks, thermodynamics, robotics");
        d18.setRegisterBy("28/09/2026 18:00");
        d18.setDriveDate("05/10/2026");
        d18.setVenue("Boeing Engineering Test Center, Bengaluru");
        d18.setJobDescription("Design commercial aircraft composite wing structures, CFD aerodynamic fluid simulations, and autonomous flight control actuators.");
        addDrive(d18);

        Drive d19 = new Drive(19, "Siemens Healthineers", 13.0, "EEE", 7.0, 0, 2027);
        d19.setJobRole("Medical Diagnostic & Imaging Systems Engineer");
        d19.setRequiredSkills("power systems, matlab, embedded systems, c++");
        d19.setRegisterBy("30/09/2026 17:00");
        d19.setDriveDate("08/10/2026");
        d19.setVenue("Siemens Healthineers Campus, Bengaluru");
        d19.setJobDescription("Develop high-voltage power electronics for MRI scanners, CT scan signal processors, and embedded medical device software.");
        addDrive(d19);

        Drive d20 = new Drive(20, "BOSCH", 11.0, "MECH", 6.8, 1, 2028);
        d20.setJobRole("Autonomous Driving & ADAS Sensor Systems Specialist");
        d20.setRequiredSkills("cad, robotics, embedded systems, matlab");
        d20.setRegisterBy("02/10/2026 17:00");
        d20.setDriveDate("10/10/2026");
        d20.setVenue("BOSCH Automotive Hub, Coimbatore");
        d20.setJobDescription("Calibrate LiDAR/Radar sensor fusion algorithms, automated emergency braking systems, and electric vehicle powertrain controllers.");
        addDrive(d20);

        Drive d21 = new Drive(21, "Pfizer R&D", 10.5, "CHEMICAL", 7.0, 0, 2027);
        d21.setJobRole("Bio-Chemical Process & Pharma Analytics Associate");
        d21.setRequiredSkills("chemical kinetics, matlab, data analysis, python");
        d21.setRegisterBy("05/10/2026 20:00");
        d21.setDriveDate("12/10/2026");
        d21.setVenue("Pfizer Research Center, Chennai");
        d21.setJobDescription("Formulate chemical reaction kinetics for vaccine synthesis, bioreactor scale-up simulations, and automated quality control pipelines.");
        addDrive(d21);
    }

    private void addStudent(Student s) {
        if (s == null) return;
        if (studentCount >= students.length) {
            Student[] newStudents = new Student[students.length * 2];
            System.arraycopy(students, 0, newStudents, 0, students.length);
            students = newStudents;
        }
        students[studentCount++] = s;
    }

    private void addDrive(Drive d) {
        if (d == null) return;
        if (driveCount >= drives.length) {
            int newCap = drives.length * 2;
            Drive[] newDrives = new Drive[newCap];
            CustomQueue[] newQueues = new CustomQueue[newCap];
            System.arraycopy(drives, 0, newDrives, 0, drives.length);
            System.arraycopy(interviewQueues, 0, newQueues, 0, interviewQueues.length);
            drives = newDrives;
            interviewQueues = newQueues;
        }
        drives[driveCount] = d;
        if (interviewQueues[driveCount] == null) {
            interviewQueues[driveCount] = new CustomQueue(50);
        }
        driveCount++;
    }

    // ==================== FEATURE 1: ELIGIBILITY FILTER + QUICKSORT ====================

    /**
     * Filter drives that a student is eligible for based on:
     * - CGPA >= minimum required CGPA
     * - Branch matches (or drive is open to ALL)
     * - Backlogs <= maximum allowed
     * - Target Batch Year matches student's Batch Year (or target year is 0 for ALL)
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
            String reqBranch = d.getEligibleBranch() != null ? d.getEligibleBranch().trim().toUpperCase() : "ALL";
            String stBranch = student.getBranch() != null ? student.getBranch().trim().toUpperCase() : "";
            boolean branchOk = reqBranch.equals("ALL") || reqBranch.contains(stBranch) || (stBranch.length() > 0 && stBranch.contains(reqBranch));
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

        // Check if registration is open for this drive
        if ("Registration Closed".equalsIgnoreCase(drive.getStatus()) || "Closed".equalsIgnoreCase(drive.getStatus())) {
            return "Error: Registration is CLOSED for " + drive.getCompanyName() + ". Applications are no longer accepted.";
        }

        // Check if student has uploaded resume
        if (student.getResumePath() == null || student.getResumePath().trim().isEmpty()) {
            return "Error: Please upload your resume first before applying for any drive.";
        }

        // Check if already applied
        for (Application app : applications) {
            if (app.getStudentId() == studentId && app.getDriveId() == driveId) {
                return "Error: " + student.getName() + " has already applied to " + drive.getCompanyName();
            }
        }

        // Check eligibility first
        boolean cgpaOk = student.getCgpa() >= drive.getMinCgpa();
        boolean branchOk = drive.getEligibleBranch().equals("ALL") ||
                           drive.getEligibleBranch().equals(student.getBranch());
        boolean backlogOk = student.getBacklogs() <= drive.getMaxBacklogs();

        if (!cgpaOk || !branchOk || !backlogOk) {
            return "Error: " + student.getName() + " is NOT eligible for " + drive.getCompanyName();
        }

        Application app = new Application(studentId, driveId, student.getName(), drive.getCompanyName());
        applications.add(app);
        student.addNotification("Successfully registered for placement drive at " + drive.getCompanyName() + " [Role: " + drive.getJobRole() + "]");
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
        Student student = findStudentById(studentId);
        if (student != null) {
            if ("Selected".equalsIgnoreCase(newStatus)) {
                student.addNotification("🎉 Congratulations! You have been selected by " + app.getCompanyName() + "!");
            } else if ("Rejected".equalsIgnoreCase(newStatus)) {
                student.addNotification("Application Update: Status for " + app.getCompanyName() + " is updated to Rejected.");
            } else {
                student.addNotification("Application Update for " + app.getCompanyName() + ": Status changed to " + newStatus);
            }
        }
        saveAllDataToDisk();
        return "Status updated: " + app.getStudentName() + " -> " + app.getCompanyName() +
               " [New Status: " + newStatus + "]";
    }
    
    public String shortlistResume(int studentId, int driveId) {
        Application app = findApplication(studentId, driveId);
        if (app == null) return "Error: Application not found.";

        app.updateStatus("Shortlisted");
        Student student = findStudentById(studentId);
        if (student != null) {
            student.addNotification("Your resume has been shortlisted by " + app.getCompanyName() + "!");
        }
        saveAllDataToDisk();
        return "Resume shortlisted for " + app.getStudentName() + " at " + app.getCompanyName();
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
        saveAllDataToDisk();
        return "Undo successful: Removed '" + removed + "'. Current status: " + app.getCurrentStatus();
    }

    // ==================== FEATURE 4: INTERVIEW SCHEDULER (QUEUE) ====================

    /**
     * Add student application to the interview queue for a specific drive.
     */
    public String scheduleInterview(int studentId, int driveId) {
        Application app = findApplication(studentId, driveId);
        if (app == null) return "Error: Application not found.";

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
        
        Student student = findStudentById(studentId);
        if (student != null) {
            Drive drive = drives[driveIndex];
            String date = (drive != null) ? drive.getDriveDate() : "Notified Later";
            student.addNotification("Congratulations, you have an interview in " + app.getCompanyName() + " on " + date);
        }
        
        saveAllDataToDisk();
        return "Interview scheduled for " + app.getStudentName() + " at " + app.getCompanyName();
    }

    /**
     * Start the next interview by dequeuing the first student.
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
        saveAllDataToDisk();
        return "Interview started for " + app.getStudentName() + " at " + app.getCompanyName();
    }

    public CustomQueue getInterviewQueue(int driveId) {
        for (int i = 0; i < driveCount; i++) {
            if (drives[i].getId() == driveId) {
                return interviewQueues[i];
            }
        }
        return null;
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
     * 
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

        // Already sorted by QuickSort in getEligibleDrives, but demonstrating heap too
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

    // ==================== UTILITY / LOOKUP METHODS ====================

    public Student findStudentById(int id) {
        // Linear search - O(n)
        for (int i = 0; i < studentCount; i++) {
            if (students[i].getId() == id) return students[i];
        }
        return null;
    }

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
        System.out.println("\n  +-----+----------------------+------+--------+----------+");
        System.out.println("  | ID  | Name                 | CGPA | Branch | Backlogs |");
        System.out.println("  +-----+----------------------+------+--------+----------+");
        for (int i = 0; i < studentCount; i++) {
            System.out.println("  " + students[i]);
        }
        System.out.println("  +-----+----------------------+------+--------+----------+");
    }

    public void displayAllDrives() {
        System.out.println("\n  +-----+-----------------+------------+----------+---------+-------------+");
        System.out.println("  | ID  | Company         | CTC        | Branch   | Min CGA | Max Backlog |");
        System.out.println("  +-----+-----------------+------------+----------+---------+-------------+");
        for (int i = 0; i < driveCount; i++) {
            System.out.println("  " + drives[i]);
        }
        System.out.println("  +-----+-----------------+------------+----------+---------+-------------+");
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
    public ArrayList<Application> getApplications() { return applications; }
    
    // ==================== NEW FEATURES ====================
    
    public boolean registerStudent(int id, String password, int batchYear) {
        if (findStudentById(id) != null) {
            return false; // Student already exists
        }
        
        if (studentCount >= students.length) {
            Student[] newStudents = new Student[students.length * 2];
            System.arraycopy(students, 0, newStudents, 0, students.length);
            students = newStudents;
        }
        
        Student newStudent = new Student(id, password, batchYear);
        students[studentCount++] = newStudent;
        saveAllDataToDisk();
        return true;
    }

    public boolean registerStudent(int id, String password) {
        return registerStudent(id, password, 2027);
    }
    
    public boolean authenticateAdmin(String username, String password) {
        return "admin".equals(username) && "admin".equals(password);
    }
    
    public Student authenticateStudent(int id, String password) {
        Student s = findStudentById(id);
        if (s != null && s.getPassword().equals(password)) {
            return s;
        }
        return null;
    }
    
    public int getPlacedCount() {
        int count = 0;
        for (int i = 0; i < studentCount; i++) {
            boolean placed = false;
            for (Application app : applications) {
                if (app.getStudentId() == students[i].getId() && "Selected".equals(app.getCurrentStatus())) {
                    placed = true;
                    break;
                }
            }
            if (placed) count++;
        }
        return count;
    }
    
    public double getHighestCTC() {
        double max = 0.0;
        for (int i = 0; i < driveCount; i++) {
            if (drives[i].getCtc() > max) max = drives[i].getCtc();
        }
        return max;
    }

    public double getAverageCTC() {
        if (driveCount == 0) return 0.0;
        double sum = 0.0;
        for (int i = 0; i < driveCount; i++) {
            sum += drives[i].getCtc();
        }
        return sum / driveCount;
    }

    public int getPlacedCountByBranch(String branch) {
        int count = 0;
        for (int i = 0; i < studentCount; i++) {
            if (students[i].getBranch().equalsIgnoreCase(branch)) {
                for (Application app : applications) {
                    if (app.getStudentId() == students[i].getId() && "Selected".equalsIgnoreCase(app.getCurrentStatus())) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }

    public int getStudentCountByBranch(String branch) {
        int count = 0;
        for (int i = 0; i < studentCount; i++) {
            if (students[i].getBranch().equalsIgnoreCase(branch)) count++;
        }
        return count;
    }

    public String getMissingSkills(Student student, Drive drive) {
        String[] studentSkills = student.getSkills().toLowerCase().split(",");
        String[] requiredSkills = drive.getRequiredSkills().toLowerCase().split(",");
        
        if (requiredSkills.length == 0 || (requiredSkills.length == 1 && requiredSkills[0].trim().isEmpty())) {
            return "None (All skills met)";
        }
        
        ArrayList<String> missing = new ArrayList<>();
        for (String req : requiredSkills) {
            req = req.trim();
            if (req.isEmpty()) continue;
            boolean found = false;
            for (String skill : studentSkills) {
                if (skill.trim().equalsIgnoreCase(req)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missing.add(req);
            }
        }
        return missing.isEmpty() ? "None (100% Match!)" : String.join(", ", missing);
    }
    
    public double calculateMatchScore(Student student, Drive drive) {
        String[] studentSkills = student.getSkills().toLowerCase().split(",");
        String[] requiredSkills = drive.getRequiredSkills().toLowerCase().split(",");
        
        if (requiredSkills.length == 0 || (requiredSkills.length == 1 && requiredSkills[0].trim().isEmpty())) {
            return 100.0; // No specific skills required -> full match
        }
        
        int matches = 0;
        int validReqCount = 0;
        for (String req : requiredSkills) {
            req = req.trim();
            if (req.isEmpty()) continue;
            validReqCount++;
            for (String skill : studentSkills) {
                if (skill.trim().equalsIgnoreCase(req)) {
                    matches++;
                    break;
                }
            }
        }
        
        if (validReqCount == 0) return 100.0;
        return (double) matches / validReqCount * 100.0;
    }
    
    public Student[] getStudents() {
        Student[] result = new Student[studentCount];
        System.arraycopy(students, 0, result, 0, studentCount);
        return result;
    }
    
    public Drive[] getDrives() {
        Drive[] result = new Drive[driveCount];
        System.arraycopy(drives, 0, result, 0, driveCount);
        return result;
    }
    
    public void notifyTargetedStudents(Drive d) {
        String targetText = d.getTargetYear() == 0 ? "All Batches" : d.getTargetYear() + " Batch";
        String notificationMsg = "📢 [New Drive Alert]: " + d.getCompanyName() + " drive (" + d.getJobRole() + 
                ", CTC: " + d.getCtc() + " LPA) for " + targetText + " is now open!";
        
        for (int i = 0; i < studentCount; i++) {
            Student s = students[i];
            if (d.getTargetYear() == 0 || d.getTargetYear() == s.getBatchYear()) {
                s.addNotification(notificationMsg);
            }
        }
    }

    public void addNewDrive(Drive d) {
        addDrive(d);
        notifyTargetedStudents(d);
        saveAllDataToDisk();
    }

    public int getRegisteredStudentCount(int driveId) {
        int count = 0;
        for (Application app : applications) {
            if (app.getDriveId() == driveId) {
                count++;
            }
        }
        return count;
    }

    public ArrayList<Application> getApplicationsForDrive(int driveId) {
        ArrayList<Application> result = new ArrayList<>();
        for (Application app : applications) {
            if (app.getDriveId() == driveId) {
                result.add(app);
            }
        }
        return result;
    }

    public boolean updateDrive(int id, String companyName, double ctc, String eligibleBranch, 
                               double minCgpa, int maxBacklogs, String skills, 
                               String jobRole, String specialization, String status, 
                               String driveDate, String registerBy) {
        Drive drive = findDriveById(id);
        if (drive == null) return false;

        drive.setCompanyName(companyName);
        drive.setCtc(ctc);
        drive.setEligibleBranch(eligibleBranch);
        drive.setMinCgpa(minCgpa);
        drive.setMaxBacklogs(maxBacklogs);
        drive.setRequiredSkills(skills);
        drive.setJobRole(jobRole);
        drive.setSpecialization(specialization);
        drive.setStatus(status);
        drive.setDriveDate(driveDate);
        drive.setRegisterBy(registerBy);
        saveAllDataToDisk();
        return true;
    }

    public String toggleDriveRegistration(int driveId) {
        Drive d = findDriveById(driveId);
        if (d == null) return "Error: Drive not found.";
        if ("Registration Open".equalsIgnoreCase(d.getStatus()) || "Open".equalsIgnoreCase(d.getStatus())) {
            d.setStatus("Registration Closed");
            saveAllDataToDisk();
            return "Registration for " + d.getCompanyName() + " has been CLOSED.";
        } else {
            d.setStatus("Registration Open");
            saveAllDataToDisk();
            return "Registration for " + d.getCompanyName() + " has been OPENED.";
        }
    }

    public String sendPersonalMessage(int studentId, String message) {
        Student s = findStudentById(studentId);
        if (s == null) return "Error: Student not found with ID " + studentId;
        if (message == null || message.trim().isEmpty()) return "Error: Message cannot be empty.";
        
        s.addNotification("👤 [Personal Message from Admin]: " + message.trim());
        saveAllDataToDisk();
        return "Personal message sent successfully to " + s.getName() + " (ID: " + studentId + ")!";
    }

    // ==================== DATA PERSISTENCE METHODS ====================

    private String getQueuesFile() { return getDataDir() + "/queues.txt"; }

    private String cleanStr(String str) {
        if (str == null) return "";
        return str.replace("||", " ").replace("~~~", " ").replace("\n", " ").replace("\r", " ");
    }

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
                String notifJoined = String.join("~~~", s.getNotifications());
                studentWriter.println(
                        s.getId() + "||" +
                        cleanStr(s.getName()) + "||" +
                        cleanStr(s.getPassword()) + "||" +
                        s.getCgpa() + "||" +
                        cleanStr(s.getBranch()) + "||" +
                        s.getBacklogs() + "||" +
                        s.getBatchYear() + "||" +
                        cleanStr(s.getSkills()) + "||" +
                        cleanStr(s.getResumePath()) + "||" +
                        cleanStr(s.getEmailId()) + "||" +
                        cleanStr(notifJoined)
                );
            }
            studentWriter.close();

            // 2. Save Drives
            PrintWriter driveWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(getDrivesFile()), "UTF-8"));
            for (int i = 0; i < driveCount; i++) {
                Drive d = drives[i];
                driveWriter.println(
                        d.getId() + "||" +
                        cleanStr(d.getCompanyName()) + "||" +
                        d.getCtc() + "||" +
                        cleanStr(d.getEligibleBranch()) + "||" +
                        d.getMinCgpa() + "||" +
                        d.getMaxBacklogs() + "||" +
                        d.getTargetYear() + "||" +
                        cleanStr(d.getRequiredSkills()) + "||" +
                        cleanStr(d.getDriveDate()) + "||" +
                        cleanStr(d.getRegisterBy()) + "||" +
                        cleanStr(d.getVenue()) + "||" +
                        cleanStr(d.getJobRole()) + "||" +
                        cleanStr(d.getSpecialization()) + "||" +
                        cleanStr(d.getStatus()) + "||" +
                        cleanStr(d.getJobDescription())
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
                        cleanStr(app.getStudentName()) + "||" +
                        cleanStr(app.getCompanyName()) + "||" +
                        cleanStr(historyJoined)
                );
            }
            appWriter.close();

            // 4. Save Interview Queues
            PrintWriter queueWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(getQueuesFile()), "UTF-8"));
            for (int i = 0; i < driveCount; i++) {
                CustomQueue q = interviewQueues[i];
                if (q != null && !q.isEmpty()) {
                    Application[] apps = q.toArray();
                    StringBuilder sb = new StringBuilder();
                    for (Application app : apps) {
                        if (sb.length() > 0) sb.append("~~~");
                        sb.append(app.getStudentId());
                    }
                    queueWriter.println(drives[i].getId() + "||" + sb.toString());
                }
            }
            queueWriter.close();
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
                if (parts.length >= 11) {
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1];
                    String password = parts[2];
                    double cgpa = Double.parseDouble(parts[3].trim());
                    String branch = parts[4];
                    int backlogs = Integer.parseInt(parts[5].trim());
                    int batchYear = Integer.parseInt(parts[6].trim());
                    String skills = parts[7];
                    String resumePath = parts[8];
                    String emailId = parts[9];
                    String notifsStr = parts[10];

                    Student s = new Student(id, name, cgpa, branch, backlogs, batchYear);
                    s.setPassword(password);
                    s.setSkills(skills);
                    s.setResumePath(resumePath);
                    s.setEmailId(emailId);

                    if (!notifsStr.isEmpty()) {
                        String[] notifs = notifsStr.split("~~~", -1);
                        for (String n : notifs) {
                            if (!n.isEmpty()) {
                                s.addNotification(n);
                            }
                        }
                    }
                    addStudent(s);
                }
            }
            sr.close();

            // 2. Read Drives
            BufferedReader dr = new BufferedReader(new InputStreamReader(new FileInputStream(df), "UTF-8"));
            while ((line = dr.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|\\|", -1);
                if (parts.length >= 14) {
                    int id = Integer.parseInt(parts[0].trim());
                    String companyName = parts[1];
                    double ctc = Double.parseDouble(parts[2].trim());
                    String eligibleBranch = parts[3];
                    double minCgpa = Double.parseDouble(parts[4].trim());
                    int maxBacklogs = Integer.parseInt(parts[5].trim());
                    int targetYear = Integer.parseInt(parts[6].trim());
                    String skills = parts[7];
                    String driveDate = parts[8];
                    String registerBy = parts[9];
                    String venue = parts[10];
                    String jobRole = parts[11];
                    String specialization = parts[12];
                    String status = parts[13];
                    String jobDesc = (parts.length >= 15) ? parts[14] : "Standard Job Description";

                    Drive d = new Drive(id, companyName, ctc, eligibleBranch, minCgpa, maxBacklogs, targetYear);
                    d.setRequiredSkills(skills);
                    d.setDriveDate(driveDate);
                    d.setRegisterBy(registerBy);
                    d.setVenue(venue);
                    d.setJobRole(jobRole);
                    d.setSpecialization(specialization);
                    d.setStatus(status);
                    d.setJobDescription(jobDesc);
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

            // 4. Read Queues
            File qf = new File(getQueuesFile());
            if (qf.exists()) {
                BufferedReader qr = new BufferedReader(new InputStreamReader(new FileInputStream(qf), "UTF-8"));
                while ((line = qr.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split("\\|\\|", -1);
                    if (parts.length >= 2) {
                        int dId = Integer.parseInt(parts[0].trim());
                        int driveIndex = -1;
                        for (int i = 0; i < driveCount; i++) {
                            if (drives[i].getId() == dId) {
                                driveIndex = i;
                                break;
                            }
                        }
                        if (driveIndex != -1 && interviewQueues[driveIndex] != null) {
                            String[] sIds = parts[1].split("~~~", -1);
                            for (String sIdStr : sIds) {
                                if (!sIdStr.trim().isEmpty()) {
                                    int sId = Integer.parseInt(sIdStr.trim());
                                    Application app = findApplication(sId, dId);
                                    if (app != null) {
                                        interviewQueues[driveIndex].enqueue(app);
                                    }
                                }
                            }
                        }
                    }
                }
                qr.close();
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error loading data from disk: " + e.getMessage());
            return false;
        }
    }
}
