const express = require('express');
const { body, validationResult } = require('express-validator');
const db = require('../db');

const router = express.Router();

// GET /api/students  — list with optional filters
router.get('/', (req, res) => {
  const { department, batch_year, status, search } = req.query;
  let sql = 'SELECT * FROM students WHERE 1=1';
  const params = [];

  if (department) { sql += ' AND department = ?'; params.push(department); }
  if (batch_year) { sql += ' AND batch_year = ?'; params.push(batch_year); }
  if (status)     { sql += ' AND status = ?';     params.push(status); }
  if (search)     { sql += ' AND (name LIKE ? OR email LIKE ?)'; params.push(`%${search}%`, `%${search}%`); }

  sql += ' ORDER BY name ASC';
  res.json(db.prepare(sql).all(...params));
});

// GET /api/students/:id
router.get('/:id', (req, res) => {
  const student = db.prepare('SELECT * FROM students WHERE id = ?').get(req.params.id);
  if (!student) return res.status(404).json({ error: 'Student not found' });
  res.json(student);
});

// GET /api/students/:id/applications
router.get('/:id/applications', (req, res) => {
  const rows = db.prepare(`
    SELECT a.*, jp.title AS job_title, c.name AS company_name, jp.job_type, jp.location
    FROM applications a
    JOIN job_postings jp ON jp.id = a.job_posting_id
    JOIN companies c ON c.id = jp.company_id
    WHERE a.student_id = ?
    ORDER BY a.applied_at DESC
  `).all(req.params.id);
  res.json(rows);
});

// POST /api/students
const validateStudent = [
  body('name').trim().notEmpty().withMessage('Name is required'),
  body('email').isEmail().withMessage('Valid email required'),
  body('department').trim().notEmpty().withMessage('Department is required'),
  body('batch_year').isInt({ min: 2000, max: 2100 }).withMessage('Valid batch year required'),
  body('cgpa').optional().isFloat({ min: 0, max: 10 }).withMessage('CGPA must be 0–10'),
];

router.post('/', validateStudent, (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) return res.status(400).json({ errors: errors.array() });

  const { name, email, phone, department, batch_year, cgpa, skills, resume_url } = req.body;
  try {
    const result = db.prepare(`
      INSERT INTO students (name, email, phone, department, batch_year, cgpa, skills, resume_url)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    `).run(name, email, phone || null, department, batch_year, cgpa || 0, skills || '', resume_url || null);
    res.status(201).json({ id: result.lastInsertRowid, message: 'Student created' });
  } catch (err) {
    if (err.message.includes('UNIQUE')) return res.status(409).json({ error: 'Email already exists' });
    throw err;
  }
});

// PUT /api/students/:id
router.put('/:id', validateStudent, (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) return res.status(400).json({ errors: errors.array() });

  const { name, email, phone, department, batch_year, cgpa, skills, resume_url, status } = req.body;
  const result = db.prepare(`
    UPDATE students SET name=?, email=?, phone=?, department=?, batch_year=?,
      cgpa=?, skills=?, resume_url=?, status=?
    WHERE id=?
  `).run(name, email, phone || null, department, batch_year, cgpa || 0, skills || '', resume_url || null, status || 'active', req.params.id);

  if (result.changes === 0) return res.status(404).json({ error: 'Student not found' });
  res.json({ message: 'Student updated' });
});

// DELETE /api/students/:id
router.delete('/:id', (req, res) => {
  const result = db.prepare('DELETE FROM students WHERE id = ?').run(req.params.id);
  if (result.changes === 0) return res.status(404).json({ error: 'Student not found' });
  res.json({ message: 'Student deleted' });
});

module.exports = router;
