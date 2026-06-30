const express = require('express');
const { body, validationResult } = require('express-validator');
const db = require('../db');

const router = express.Router();

// GET /api/applications — list all applications with details
router.get('/', (req, res) => {
  const { student_id, job_posting_id, status } = req.query;
  let sql = `
    SELECT a.*, 
      s.name AS student_name, s.email AS student_email, s.department AS student_department, s.cgpa AS student_cgpa,
      jp.title AS job_title, jp.min_cgpa, jp.eligible_depts,
      c.name AS company_name
    FROM applications a
    JOIN students s ON s.id = a.student_id
    JOIN job_postings jp ON jp.id = a.job_posting_id
    JOIN companies c ON c.id = jp.company_id
    WHERE 1=1
  `;
  const params = [];

  if (student_id) {
    sql += ' AND a.student_id = ?';
    params.push(student_id);
  }
  if (job_posting_id) {
    sql += ' AND a.job_posting_id = ?';
    params.push(job_posting_id);
  }
  if (status) {
    sql += ' AND a.status = ?';
    params.push(status);
  }

  sql += ' ORDER BY a.applied_at DESC';
  res.json(db.prepare(sql).all(...params));
});

// GET /api/applications/:id
router.get('/:id', (req, res) => {
  const application = db.prepare(`
    SELECT a.*, 
      s.name AS student_name, s.email AS student_email, s.department AS student_department, s.cgpa AS student_cgpa,
      jp.title AS job_title, jp.min_cgpa, jp.eligible_depts,
      c.name AS company_name
    FROM applications a
    JOIN students s ON s.id = a.student_id
    JOIN job_postings jp ON jp.id = a.job_posting_id
    JOIN companies c ON c.id = jp.company_id
    WHERE a.id = ?
  `).get(req.params.id);

  if (!application) return res.status(404).json({ error: 'Application not found' });
  res.json(application);
});

// POST /api/applications — apply for a job with eligibility validation
const validateApplication = [
  body('student_id').isInt().withMessage('Valid student ID required'),
  body('job_posting_id').isInt().withMessage('Valid job posting ID required'),
];

router.post('/', validateApplication, (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) return res.status(400).json({ errors: errors.array() });

  const { student_id, job_posting_id, notes } = req.body;

  // Retrieve student and job details to check eligibility
  const student = db.prepare('SELECT * FROM students WHERE id = ?').get(student_id);
  if (!student) return res.status(404).json({ error: 'Student not found' });
  if (student.status === 'not_eligible') return res.status(400).json({ error: 'Student is marked as not eligible for placements' });

  const job = db.prepare('SELECT * FROM job_postings WHERE id = ?').get(job_posting_id);
  if (!job) return res.status(404).json({ error: 'Job posting not found' });
  if (job.status !== 'open') return res.status(400).json({ error: 'This job posting is closed or cancelled' });

  // 1. Check CGPA eligibility
  if (student.cgpa < job.min_cgpa) {
    return res.status(403).json({
      error: `Ineligible: Student's CGPA (${student.cgpa}) is lower than the job requirement (${job.min_cgpa})`
    });
  }

  // 2. Check Department eligibility
  if (job.eligible_depts && job.eligible_depts.trim() !== '') {
    const departments = job.eligible_depts.split(',').map(d => d.trim().toUpperCase());
    if (!departments.includes(student.department.trim().toUpperCase())) {
      return res.status(403).json({
        error: `Ineligible: Student's department (${student.department}) is not in eligible departments (${job.eligible_depts})`
      });
    }
  }

  try {
    const result = db.prepare(`
      INSERT INTO applications (student_id, job_posting_id, status, notes)
      VALUES (?, ?, 'applied', ?)
    `).run(student_id, job_posting_id, notes || null);

    res.status(201).json({ id: result.lastInsertRowid, message: 'Application submitted successfully' });
  } catch (err) {
    if (err.message.includes('UNIQUE')) {
      return res.status(409).json({ error: 'Student has already applied for this job' });
    }
    throw err;
  }
});

// PUT /api/applications/:id — update status
router.put('/:id', [
  body('status').isIn(['applied', 'shortlisted', 'interview_scheduled', 'selected', 'rejected', 'offer_accepted', 'offer_declined']).withMessage('Valid status required')
], (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) return res.status(400).json({ errors: errors.array() });

  const { status, notes } = req.body;

  const result = db.prepare(`
    UPDATE applications SET status=?, notes=COALESCE(?, notes)
    WHERE id=?
  `).run(status, notes || null, req.params.id);

  if (result.changes === 0) return res.status(404).json({ error: 'Application not found' });
  res.json({ message: 'Application status updated successfully' });
});

// DELETE /api/applications/:id
router.delete('/:id', (req, res) => {
  const result = db.prepare('DELETE FROM applications WHERE id = ?').run(req.params.id);
  if (result.changes === 0) return res.status(404).json({ error: 'Application not found' });
  res.json({ message: 'Application deleted successfully' });
});

module.exports = router;
