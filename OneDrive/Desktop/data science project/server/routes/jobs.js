const express = require('express');
const { body, validationResult } = require('express-validator');
const db = require('../db');

const router = express.Router();

// GET /api/jobs — list jobs with optional filters
router.get('/', (req, res) => {
  const { company_id, status, search, min_cgpa } = req.query;
  let sql = `
    SELECT jp.*, c.name AS company_name, c.industry AS company_industry,
      (SELECT COUNT(*) FROM applications WHERE job_posting_id = jp.id) AS application_count
    FROM job_postings jp
    JOIN companies c ON c.id = jp.company_id
    WHERE 1=1
  `;
  const params = [];

  if (company_id) {
    sql += ' AND jp.company_id = ?';
    params.push(company_id);
  }
  if (status) {
    sql += ' AND jp.status = ?';
    params.push(status);
  }
  if (min_cgpa) {
    sql += ' AND jp.min_cgpa <= ?';
    params.push(min_cgpa);
  }
  if (search) {
    sql += ' AND (jp.title LIKE ? OR c.name LIKE ? OR jp.location LIKE ?)';
    params.push(`%${search}%`, `%${search}%`, `%${search}%`);
  }

  sql += ' ORDER BY jp.created_at DESC';
  res.json(db.prepare(sql).all(...params));
});

// GET /api/jobs/:id — get job detail
router.get('/:id', (req, res) => {
  const job = db.prepare(`
    SELECT jp.*, c.name AS company_name, c.industry AS company_industry, c.website AS company_website
    FROM job_postings jp
    JOIN companies c ON c.id = jp.company_id
    WHERE jp.id = ?
  `).get(req.params.id);

  if (!job) return res.status(404).json({ error: 'Job posting not found' });
  res.json(job);
});

// POST /api/jobs — create new job
const validateJob = [
  body('company_id').isInt().withMessage('Valid company ID required'),
  body('title').trim().notEmpty().withMessage('Job title is required'),
  body('job_type').isIn(['full_time', 'internship', 'contract']).withMessage('Valid job type required'),
  body('min_cgpa').optional().isFloat({ min: 0, max: 10 }).withMessage('CGPA limit must be 0–10'),
];

router.post('/', validateJob, (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) return res.status(400).json({ errors: errors.array() });

  const { company_id, title, description, requirements, salary_min, salary_max, location, job_type, min_cgpa, eligible_depts, deadline } = req.body;

  try {
    const result = db.prepare(`
      INSERT INTO job_postings (company_id, title, description, requirements, salary_min, salary_max, location, job_type, min_cgpa, eligible_depts, deadline)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `).run(company_id, title, description || null, requirements || null, salary_min || null, salary_max || null, location || null, job_type, min_cgpa || 0, eligible_depts || '', deadline || null);

    res.status(201).json({ id: result.lastInsertRowid, message: 'Job posting created successfully' });
  } catch (err) {
    if (err.message.includes('FOREIGN KEY')) return res.status(400).json({ error: 'Company ID does not exist' });
    throw err;
  }
});

// PUT /api/jobs/:id — update job
router.put('/:id', validateJob, (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) return res.status(400).json({ errors: errors.array() });

  const { company_id, title, description, requirements, salary_min, salary_max, location, job_type, min_cgpa, eligible_depts, deadline, status } = req.body;

  const result = db.prepare(`
    UPDATE job_postings SET company_id=?, title=?, description=?, requirements=?,
      salary_min=?, salary_max=?, location=?, job_type=?, min_cgpa=?,
      eligible_depts=?, deadline=?, status=?
    WHERE id=?
  `).run(company_id, title, description || null, requirements || null, salary_min || null, salary_max || null, location || null, job_type, min_cgpa || 0, eligible_depts || '', deadline || null, status || 'open', req.params.id);

  if (result.changes === 0) return res.status(404).json({ error: 'Job posting not found' });
  res.json({ message: 'Job posting updated successfully' });
});

// DELETE /api/jobs/:id — delete job
router.delete('/:id', (req, res) => {
  const result = db.prepare('DELETE FROM job_postings WHERE id = ?').run(req.params.id);
  if (result.changes === 0) return res.status(404).json({ error: 'Job posting not found' });
  res.json({ message: 'Job posting deleted successfully' });
});

module.exports = router;
