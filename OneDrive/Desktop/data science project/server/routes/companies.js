const express = require('express');
const { body, validationResult } = require('express-validator');
const db = require('../db');

const router = express.Router();

// GET /api/companies
router.get('/', (req, res) => {
  const { search } = req.query;
  let sql = 'SELECT * FROM companies WHERE 1=1';
  const params = [];
  if (search) { sql += ' AND (name LIKE ? OR industry LIKE ?)'; params.push(`%${search}%`, `%${search}%`); }
  sql += ' ORDER BY name ASC';
  res.json(db.prepare(sql).all(...params));
});

// GET /api/companies/:id
router.get('/:id', (req, res) => {
  const company = db.prepare('SELECT * FROM companies WHERE id = ?').get(req.params.id);
  if (!company) return res.status(404).json({ error: 'Company not found' });
  res.json(company);
});

// GET /api/companies/:id/job_postings
router.get('/:id/job_postings', (req, res) => {
  const rows = db.prepare(`
    SELECT jp.*, 
      (SELECT COUNT(*) FROM applications WHERE job_posting_id = jp.id) AS application_count
    FROM job_postings jp
    WHERE jp.company_id = ?
    ORDER BY jp.created_at DESC
  `).all(req.params.id);
  res.json(rows);
});

// POST /api/companies
const validateCompany = [
  body('name').trim().notEmpty().withMessage('Company name is required'),
  body('hr_email').optional().isEmail().withMessage('Valid HR email required'),
];

router.post('/', validateCompany, (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) return res.status(400).json({ errors: errors.array() });

  const { name, industry, website, hr_name, hr_email, hr_phone, description } = req.body;
  const result = db.prepare(`
    INSERT INTO companies (name, industry, website, hr_name, hr_email, hr_phone, description)
    VALUES (?, ?, ?, ?, ?, ?, ?)
  `).run(name, industry || null, website || null, hr_name || null, hr_email || null, hr_phone || null, description || null);
  res.status(201).json({ id: result.lastInsertRowid, message: 'Company created' });
});

// PUT /api/companies/:id
router.put('/:id', validateCompany, (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) return res.status(400).json({ errors: errors.array() });

  const { name, industry, website, hr_name, hr_email, hr_phone, description } = req.body;
  const result = db.prepare(`
    UPDATE companies SET name=?, industry=?, website=?, hr_name=?, hr_email=?, hr_phone=?, description=?
    WHERE id=?
  `).run(name, industry || null, website || null, hr_name || null, hr_email || null, hr_phone || null, description || null, req.params.id);

  if (result.changes === 0) return res.status(404).json({ error: 'Company not found' });
  res.json({ message: 'Company updated' });
});

// DELETE /api/companies/:id
router.delete('/:id', (req, res) => {
  const result = db.prepare('DELETE FROM companies WHERE id = ?').run(req.params.id);
  if (result.changes === 0) return res.status(404).json({ error: 'Company not found' });
  res.json({ message: 'Company deleted' });
});

module.exports = router;
