const express = require('express');
const { body, validationResult } = require('express-validator');
const db = require('../db');

const router = express.Router();

// GET /api/interviews — list interviews with optional filters
router.get('/', (req, res) => {
  const { result, mode } = req.query;
  let sql = `
    SELECT i.*, 
      s.name AS student_name, s.department AS student_department, s.id AS student_id,
      jp.title AS job_title, jp.id AS job_posting_id,
      c.name AS company_name
    FROM interviews i
    JOIN applications a ON a.id = i.application_id
    JOIN students s ON s.id = a.student_id
    JOIN job_postings jp ON jp.id = a.job_posting_id
    JOIN companies c ON c.id = jp.company_id
    WHERE 1=1
  `;
  const params = [];

  if (result) {
    if (result === 'null') {
      sql += ' AND i.result IS NULL';
    } else {
      sql += ' AND i.result = ?';
      params.push(result);
    }
  }
  if (mode) {
    sql += ' AND i.mode = ?';
    params.push(mode);
  }

  sql += ' ORDER BY i.scheduled_at ASC';
  res.json(db.prepare(sql).all(...params));
});

// POST /api/interviews — schedule interview
const validateInterview = [
  body('application_id').isInt().withMessage('Valid application ID required'),
  body('scheduled_at').notEmpty().withMessage('Scheduled date/time is required'),
  body('round').optional().isInt({ min: 1 }).withMessage('Round number must be positive integer'),
  body('mode').optional().isIn(['in_person', 'virtual', 'telephonic']).withMessage('Valid interview mode required')
];

router.post('/', validateInterview, (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) return res.status(400).json({ errors: errors.array() });

  const { application_id, round, scheduled_at, mode, interviewer, feedback, result: interviewResult } = req.body;

  try {
    // Check if application exists
    const app = db.prepare('SELECT * FROM applications WHERE id = ?').get(application_id);
    if (!app) return res.status(404).json({ error: 'Application not found' });

    const result = db.prepare(`
      INSERT INTO interviews (application_id, round, scheduled_at, mode, interviewer, feedback, result)
      VALUES (?, ?, ?, ?, ?, ?, ?)
    `).run(application_id, round || 1, scheduled_at, mode || 'virtual', interviewer || null, feedback || null, interviewResult || null);

    // If scheduled, automatically update application status to 'interview_scheduled'
    db.prepare("UPDATE applications SET status = 'interview_scheduled' WHERE id = ?").run(application_id);

    res.status(201).json({ id: result.lastInsertRowid, message: 'Interview scheduled successfully' });
  } catch (err) {
    throw err;
  }
});

// PUT /api/interviews/:id — update interview feedback or result
router.put('/:id', [
  body('scheduled_at').notEmpty().withMessage('Scheduled date/time is required'),
  body('round').isInt({ min: 1 }).withMessage('Round number must be positive integer'),
  body('mode').isIn(['in_person', 'virtual', 'telephonic']).withMessage('Valid interview mode required'),
  body('result').optional({ nullable: true }).isIn(['pass', 'fail', 'pending', null]).withMessage('Valid result required')
], (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) return res.status(400).json({ errors: errors.array() });

  const { round, scheduled_at, mode, interviewer, feedback, result } = req.body;

  const updateResult = db.prepare(`
    UPDATE interviews SET round=?, scheduled_at=?, mode=?, interviewer=?, feedback=?, result=?
    WHERE id=?
  `).run(round, scheduled_at, mode, interviewer || null, feedback || null, result || null, req.params.id);

  if (updateResult.changes === 0) return res.status(404).json({ error: 'Interview not found' });

  // Optional automation: if result is fail, automatically set application status to 'rejected'
  if (result === 'fail') {
    const interview = db.prepare('SELECT application_id FROM interviews WHERE id = ?').get(req.params.id);
    db.prepare("UPDATE applications SET status = 'rejected' WHERE id = ?").run(interview.application_id);
  } else if (result === 'pass') {
    // If passed, set status to shortlisted/advanced
    const interview = db.prepare('SELECT application_id FROM interviews WHERE id = ?').get(req.params.id);
    db.prepare("UPDATE applications SET status = 'shortlisted' WHERE id = ?").run(interview.application_id);
  }

  res.json({ message: 'Interview updated successfully' });
});

// DELETE /api/interviews/:id
router.delete('/:id', (req, res) => {
  const result = db.prepare('DELETE FROM interviews WHERE id = ?').run(req.params.id);
  if (result.changes === 0) return res.status(404).json({ error: 'Interview not found' });
  res.json({ message: 'Interview deleted successfully' });
});

module.exports = router;
