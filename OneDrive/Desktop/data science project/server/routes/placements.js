const express = require('express');
const { body, validationResult } = require('express-validator');
const db = require('../db');

const router = express.Router();

// GET /api/placements — list all placements
router.get('/', (req, res) => {
  const rows = db.prepare(`
    SELECT p.*,
      s.name AS student_name, s.email AS student_email, s.department AS student_department, s.cgpa AS student_cgpa,
      jp.title AS job_title,
      c.name AS company_name
    FROM placements p
    JOIN students s ON s.id = p.student_id
    JOIN job_postings jp ON jp.id = p.job_posting_id
    JOIN companies c ON c.id = p.company_id
    ORDER BY p.offer_date DESC
  `).all();
  res.json(rows);
});

// POST /api/placements — record new placement
const validatePlacement = [
  body('student_id').isInt().withMessage('Valid student ID required'),
  body('job_posting_id').isInt().withMessage('Valid job posting ID required'),
  body('company_id').isInt().withMessage('Valid company ID required'),
  body('ctc').isFloat({ min: 0 }).withMessage('Valid CTC (Salary) package required'),
  body('offer_date').notEmpty().withMessage('Offer date is required')
];

router.post('/', validatePlacement, (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) return res.status(400).json({ errors: errors.array() });

  const { student_id, job_posting_id, company_id, offer_date, joining_date, ctc, offer_letter } = req.body;

  // Verify student exists and is active/eligible
  const student = db.prepare('SELECT status FROM students WHERE id = ?').get(student_id);
  if (!student) return res.status(404).json({ error: 'Student not found' });
  if (student.status === 'not_eligible') return res.status(400).json({ error: 'Student is not eligible for placements' });

  db.exec('BEGIN TRANSACTION');
  try {
    // 1. Insert placement record
    const result = db.prepare(`
      INSERT INTO placements (student_id, job_posting_id, company_id, offer_date, joining_date, ctc, offer_letter)
      VALUES (?, ?, ?, ?, ?, ?, ?)
    `).run(student_id, job_posting_id, company_id, offer_date, joining_date || null, ctc, offer_letter || null);

    // 2. Update student status to 'placed'
    db.prepare("UPDATE students SET status = 'placed' WHERE id = ?").run(student_id);

    // 3. Update application status to 'offer_accepted' if an application exists
    db.prepare(`
      UPDATE applications 
      SET status = 'offer_accepted' 
      WHERE student_id = ? AND job_posting_id = ?
    `).run(student_id, job_posting_id);

    db.exec('COMMIT');
    res.status(201).json({ id: result.lastInsertRowid, message: 'Placement recorded and student status updated to Placed' });
  } catch (err) {
    db.exec('ROLLBACK');
    if (err.message.includes('UNIQUE')) {
      return res.status(409).json({ error: 'Student already has a recorded placement offer' });
    }
    throw err;
  }
});

// PUT /api/placements/:id — update placement details
router.put('/:id', [
  body('ctc').isFloat({ min: 0 }).withMessage('Valid CTC package required'),
  body('offer_date').notEmpty().withMessage('Offer date is required')
], (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) return res.status(400).json({ errors: errors.array() });

  const { offer_date, joining_date, ctc, offer_letter } = req.body;

  const result = db.prepare(`
    UPDATE placements SET offer_date=?, joining_date=?, ctc=?, offer_letter=?
    WHERE id=?
  `).run(offer_date, joining_date || null, ctc, offer_letter || null, req.params.id);

  if (result.changes === 0) return res.status(404).json({ error: 'Placement record not found' });
  res.json({ message: 'Placement details updated successfully' });
});

// DELETE /api/placements/:id — delete placement record and reset student status
router.delete('/:id', (req, res) => {
  const placement = db.prepare('SELECT student_id, job_posting_id FROM placements WHERE id = ?').get(req.params.id);
  if (!placement) return res.status(404).json({ error: 'Placement record not found' });

  db.exec('BEGIN TRANSACTION');
  try {
    // 1. Delete placement
    db.prepare('DELETE FROM placements WHERE id = ?').run(req.params.id);

    // 2. Reset student status back to 'active'
    db.prepare("UPDATE students SET status = 'active' WHERE id = ?").run(placement.student_id);

    // 3. Reset application status to 'selected' or 'applied' (optional, let's reset to selected or let user handle)
    db.prepare(`
      UPDATE applications 
      SET status = 'selected' 
      WHERE student_id = ? AND job_posting_id = ?
    `).run(placement.student_id, placement.job_posting_id);

    db.exec('COMMIT');
    res.json({ message: 'Placement deleted and student status reset to Active' });
  } catch (err) {
    db.exec('ROLLBACK');
    throw err;
  }
});

module.exports = router;
