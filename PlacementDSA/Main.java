import java.util.Scanner;

/**
 * ====================================================================
 *        PLACEMENT MANAGEMENT SYSTEM - Standalone Java Application
 * ====================================================================
 * 
 * A console-based application demonstrating Data Structures:
 * 
 * 1. CUSTOM STACK   - Application status tracking (push/pop/peek)
 * 2. CUSTOM MAX-HEAP - Top-K placement drives by CTC (insert/extractMax)
 * 3. CUSTOM QUICKSORT - Sorting eligible drives by CTC (divide & conquer)
 * 
 * All data structures are implemented manually (no java.util.Stack, 
 * no java.util.PriorityQueue, no Collections.sort).
 * 
 * How to run:
 *   javac *.java
 *   java Main
 * 
 * ====================================================================
 */
public class Main {
    private static PlacementSystem system = new PlacementSystem();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║      PLACEMENT MANAGEMENT SYSTEM (DSA Project)          ║");
        System.out.println("║                                                          ║");
        System.out.println("║  Data Structures Used:                                   ║");
        System.out.println("║    1. Custom Stack (Array-based)                         ║");
        System.out.println("║    2. Custom Max-Heap (Array-based)                      ║");
        System.out.println("║    3. Custom QuickSort (Divide & Conquer)                ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1: eligibilityMenu(); break;
                case 2: applicationMenu(); break;
                case 3: topPlacementsMenu(); break;
                case 4: viewDataMenu(); break;
                case 5:
                    System.out.println("\n  Thank you for using Placement Management System!");
                    System.out.println("  Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("  Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    // ==================== MAIN MENU ====================

    private static void displayMainMenu() {
        System.out.println("\n┌──────────────────────────────────────┐");
        System.out.println("│           MAIN MENU                  │");
        System.out.println("├──────────────────────────────────────┤");
        System.out.println("│  1. Check Eligibility (QuickSort)    │");
        System.out.println("│  2. Application Tracker (Stack)      │");
        System.out.println("│  3. Top Placements (Max-Heap)        │");
        System.out.println("│  4. View Data                        │");
        System.out.println("│  5. Exit                             │");
        System.out.println("└──────────────────────────────────────┘");
    }

    // ==================== FEATURE 1: ELIGIBILITY (QuickSort) ====================

    private static void eligibilityMenu() {
        System.out.println("\n  ═══ ELIGIBILITY FILTER + QUICKSORT ═══");
        System.out.println("  DSA: QuickSort O(n log n) to rank eligible drives by CTC");
        System.out.println();

        system.displayAllStudents();
        int studentId = getIntInput("\n  Enter Student ID to check eligibility: ");

        Student student = system.findStudentById(studentId);
        if (student == null) {
            System.out.println("  Student not found!");
            return;
        }

        System.out.println("\n  Checking eligibility for: " + student.getName() +
                " (CGPA: " + student.getCgpa() + ", Branch: " + student.getBranch() +
                ", Batch Year: " + student.getBatchYear() + ", Backlogs: " + student.getBacklogs() + ")");

        Drive[] eligible = system.getEligibleDrives(studentId);

        if (eligible == null || eligible.length == 0) {
            System.out.println("\n  No eligible drives found for this student.");
            return;
        }

        System.out.println("\n  ✓ Eligible Drives (sorted by CTC descending - QuickSort):");
        System.out.println("  +-----+-----------------+------------+----------+---------+-------------+");
        System.out.println("  | ID  | Company         | CTC        | Branch   | Min CGA | Max Backlog |");
        System.out.println("  +-----+-----------------+------------+----------+---------+-------------+");
        for (Drive d : eligible) {
            System.out.println("  " + d);
        }
        System.out.println("  +-----+-----------------+------------+----------+---------+-------------+");
        System.out.println("\n  [QuickSort applied: " + eligible.length + " drives sorted in O(n log n) time]");
    }

    // ==================== FEATURE 2: APPLICATION TRACKER (Stack) ====================

    private static void applicationMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n  ═══ APPLICATION TRACKER (CUSTOM STACK) ═══");
            System.out.println("  DSA: Stack - push O(1), pop O(1), peek O(1)");
            System.out.println();
            System.out.println("  1. Apply to a Drive (push 'Applied')");
            System.out.println("  2. Update Status (push new status)");
            System.out.println("  3. Undo Last Status (pop)");
            System.out.println("  4. View Application History (display stack)");
            System.out.println("  5. View All Applications");
            System.out.println("  6. Back to Main Menu");

            int choice = getIntInput("\n  Enter choice: ");

            switch (choice) {
                case 1: applyToDrive(); break;
                case 2: updateStatus(); break;
                case 3: undoStatus(); break;
                case 4: viewHistory(); break;
                case 5: system.displayAllApplications(); break;
                case 6: back = true; break;
                default: System.out.println("  Invalid choice.");
            }
        }
    }

    private static void applyToDrive() {
        system.displayAllStudents();
        int studentId = getIntInput("\n  Enter Student ID: ");
        system.displayAllDrives();
        int driveId = getIntInput("  Enter Drive ID: ");

        String result = system.applyToDrive(studentId, driveId);
        System.out.println("\n  " + result);
        System.out.println("  [Stack operation: push('Applied') - O(1)]");
    }

    private static void updateStatus() {
        system.displayAllApplications();
        int studentId = getIntInput("\n  Enter Student ID: ");
        int driveId = getIntInput("  Enter Drive ID: ");

        System.out.println("\n  Valid statuses: Shortlisted, Interview, Technical_Round, HR_Round, Selected, Rejected");
        System.out.print("  Enter new status: ");
        String status = scanner.nextLine().trim();

        String result = system.updateApplicationStatus(studentId, driveId, status);
        System.out.println("\n  " + result);
        System.out.println("  [Stack operation: push('" + status + "') - O(1)]");
    }

    private static void undoStatus() {
        system.displayAllApplications();
        int studentId = getIntInput("\n  Enter Student ID: ");
        int driveId = getIntInput("  Enter Drive ID: ");

        String result = system.undoApplicationStatus(studentId, driveId);
        System.out.println("\n  " + result);
        System.out.println("  [Stack operation: pop() - O(1)]");
    }

    private static void viewHistory() {
        int studentId = getIntInput("\n  Enter Student ID: ");
        int driveId = getIntInput("  Enter Drive ID: ");
        System.out.println();
        system.displayApplicationHistory(studentId, driveId);
        System.out.println("\n  [Stack operations demonstrated: Full history shows bottom-to-top order]");
    }

    // ==================== FEATURE 3: TOP PLACEMENTS (Max-Heap) ====================

    private static void topPlacementsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n  ═══ TOP PLACEMENTS (CUSTOM MAX-HEAP) ═══");
            System.out.println("  DSA: Max-Heap - insert O(log n), extractMax O(log n)");
            System.out.println();
            System.out.println("  1. Top-K Drives by CTC (Overall)");
            System.out.println("  2. Top-K Eligible Drives for a Student");
            System.out.println("  3. Back to Main Menu");

            int choice = getIntInput("\n  Enter choice: ");

            switch (choice) {
                case 1: topKOverall(); break;
                case 2: topKForStudent(); break;
                case 3: back = true; break;
                default: System.out.println("  Invalid choice.");
            }
        }
    }

    private static void topKOverall() {
        int k = getIntInput("\n  Enter K (how many top drives to show): ");

        System.out.println("\n  Building Max-Heap with " + system.getDriveCount() + " drives...");
        System.out.println("  Inserting each drive: O(log n) per insertion, O(n log n) total");
        System.out.println("  Extracting top " + k + ": O(K log n)");

        Drive[] topK = system.getTopKDrives(k);

        System.out.println("\n  ★ Top " + topK.length + " Placement Drives by CTC:");
        System.out.println("  +------+-----------------+------------+");
        System.out.println("  | Rank | Company         | CTC        |");
        System.out.println("  +------+-----------------+------------+");
        for (int i = 0; i < topK.length; i++) {
            System.out.printf("  | %-4d | %-15s | %-6.2f LPA |%n", i + 1, topK[i].getCompanyName(), topK[i].getCtc());
        }
        System.out.println("  +------+-----------------+------------+");
        System.out.println("\n  [Max-Heap: " + k + " extractMax() operations performed - O(K log n)]");
    }

    private static void topKForStudent() {
        system.displayAllStudents();
        int studentId = getIntInput("\n  Enter Student ID: ");
        int k = getIntInput("  Enter K (how many top drives): ");

        Drive[] topK = system.getTopKEligibleDrives(studentId, k);

        if (topK == null || topK.length == 0) {
            System.out.println("\n  No eligible drives found for this student.");
            return;
        }

        Student s = system.findStudentById(studentId);
        System.out.println("\n  ★ Top " + topK.length + " Eligible Drives for " + s.getName() + ":");
        System.out.println("  +------+-----------------+------------+");
        System.out.println("  | Rank | Company         | CTC        |");
        System.out.println("  +------+-----------------+------------+");
        for (int i = 0; i < topK.length; i++) {
            System.out.printf("  | %-4d | %-15s | %-6.2f LPA |%n", i + 1, topK[i].getCompanyName(), topK[i].getCtc());
        }
        System.out.println("  +------+-----------------+------------+");
        System.out.println("\n  [Heap used to extract top-" + topK.length + " from eligible set]");
    }

    // ==================== VIEW DATA ====================

    private static void viewDataMenu() {
        System.out.println("\n  ═══ VIEW DATA ═══");
        System.out.println("  1. All Students");
        System.out.println("  2. All Drives");
        System.out.println("  3. All Applications");

        int choice = getIntInput("\n  Enter choice: ");
        switch (choice) {
            case 1: system.displayAllStudents(); break;
            case 2: system.displayAllDrives(); break;
            case 3: system.displayAllApplications(); break;
            default: System.out.println("  Invalid choice.");
        }
    }

    // ==================== INPUT HELPERS ====================

    private static int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("  Invalid input. " + prompt);
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return value;
    }
}
