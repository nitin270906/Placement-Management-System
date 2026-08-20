const express = require('express');
const db = require('../db');

const router = express.Router();

// GET /api/analytics/dashboard
router.get('/dashboard', (req, res) => {
  try {
    // 1. Basic Counts
    const studentCounts = db.prepare(`
      SELECT 
        COUNT(*) AS total,
        SUM(CASE WHEN status = 'placed' THEN 1 ELSE 0 END) AS placed,
        SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) AS active,
        SUM(CASE WHEN status = 'not_eligible' THEN 1 ELSE 0 END) AS not_eligible
      FROM students
    `).get();

    const companyCount = db.prepare('SELECT COUNT(*) AS count FROM companies').get().count;
    const activeJobsCount = db.prepare("SELECT COUNT(*) AS count FROM job_postings WHERE status = 'open'").get().count;
    const totalAppsCount = db.prepare('SELECT COUNT(*) AS count FROM applications').get().count;

    // 2. Salary Package Metrics (for placed students)
    const salaryMetrics = db.prepare(`
      SELECT 
        AVG(ctc) AS avg_ctc,
        MAX(ctc) AS max_ctc,
        MIN(ctc) AS min_ctc
      FROM placements
    `).get();

    // 3. Department-wise Placement Rates
    // We group by department and count total eligible students (placed + active) vs placed
    const deptStats = db.prepare(`
      SELECT 
        department,
        COUNT(*) AS total_students,
        SUM(CASE WHEN status = 'placed' THEN 1 ELSE 0 END) AS placed_students,
        ROUND(
          (SUM(CASE WHEN status = 'placed' THEN 1.0 ELSE 0.0 END) / 
          SUM(CASE WHEN status IN ('active', 'placed') THEN 1.0 ELSE 0.0 END)) * 100, 
          1
        ) AS placement_rate
      FROM students
      GROUP BY department
    `).all();

    // 4. Salary Distribution Groups
    const salaryDistribution = db.prepare(`
      SELECT 
        SUM(CASE WHEN ctc < 50000 THEN 1 ELSE 0 END) AS tier_1,       -- Under 50k
        SUM(CASE WHEN ctc >= 50000 AND ctc < 90000 THEN 1 ELSE 0 END) AS tier_2, -- 50k - 90k
        SUM(CASE WHEN ctc >= 90000 AND ctc < 130000 THEN 1 ELSE 0 END) AS tier_3, -- 90k - 130k
        SUM(CASE WHEN ctc >= 130000 THEN 1 ELSE 0 END) AS tier_4      -- 130k+
      FROM placements
    `).get();

    // 5. Recent placements feed
    const recentPlacements = db.prepare(`
      SELECT p.id, s.name AS student_name, s.department, c.name AS company_name, p.ctc, p.offer_date
      FROM placements p
      JOIN students s ON s.id = p.student_id
      JOIN companies c ON c.id = p.company_id
      ORDER BY p.offer_date DESC
      LIMIT 5
    `).all();

    // 6. Upcoming Interviews feed
    const upcomingInterviews = db.prepare(`
      SELECT i.id, s.name AS student_name, c.name AS company_name, jp.title AS job_title, i.round, i.scheduled_at, i.mode
      FROM interviews i
      JOIN applications a ON a.id = i.application_id
      JOIN students s ON s.id = a.student_id
      JOIN job_postings jp ON jp.id = a.job_posting_id
      JOIN companies c ON c.id = jp.company_id
      WHERE i.result IS NULL OR i.result = 'pending'
      ORDER BY i.scheduled_at ASC
      LIMIT 5
    `).all();

    // Calculate overall placement rate
    const eligibleStudentsCount = (studentCounts.total || 0) - (studentCounts.not_eligible || 0);
    const placementRate = eligibleStudentsCount > 0 
      ? Math.round(((studentCounts.placed || 0) / eligibleStudentsCount) * 100 * 10) / 10 
      : 0;

    res.json({
      counts: {
        total_students: studentCounts.total || 0,
        placed_students: studentCounts.placed || 0,
        active_students: studentCounts.active || 0,
        not_eligible_students: studentCounts.not_eligible || 0,
        eligible_students: eligibleStudentsCount,
        companies: companyCount,
        active_jobs: activeJobsCount,
        total_applications: totalAppsCount,
        placement_rate: placementRate
      },
      salary: {
        avg_ctc: Math.round(salaryMetrics.avg_ctc || 0),
        max_ctc: salaryMetrics.max_ctc || 0,
        min_ctc: salaryMetrics.min_ctc || 0
      },
      dept_stats: deptStats,
      salary_distribution: {
        under_50k: salaryDistribution.tier_1 || 0,
        tier_50k_90k: salaryDistribution.tier_2 || 0,
        tier_90k_130k: salaryDistribution.tier_3 || 0,
        above_130k: salaryDistribution.tier_4 || 0
      },
      recent_placements: recentPlacements,
      upcoming_interviews: upcomingInterviews
    });
  } catch (err) {
    console.error('Analytics Error:', err);
    res.status(500).json({ error: 'Failed to compute analytics' });
  }
});

module.exports = router;
