const db = require('./db');

function seed() {
  console.log('Seeding database...');

  // Enable foreign keys
  db.exec('PRAGMA foreign_keys = OFF');

  // Clear existing tables
  db.prepare('DELETE FROM placements').run();
  db.prepare('DELETE FROM interviews').run();
  db.prepare('DELETE FROM applications').run();
  db.prepare('DELETE FROM job_postings').run();
  db.prepare('DELETE FROM companies').run();
  db.prepare('DELETE FROM students').run();

  db.exec('PRAGMA foreign_keys = ON');

  // 1. Insert Students
  const insertStudent = db.prepare(`
    INSERT INTO students (name, email, phone, department, batch_year, cgpa, skills, resume_url, status)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
  `);

  const students = [
    ['Alice Smith', 'alice.smith@college.edu', '+15550101', 'CS', 2026, 9.2, 'JavaScript, React, Node.js, Python', 'https://resume.com/alicesmith', 'active'],
    ['Bob Johnson', 'bob.johnson@college.edu', '+15550102', 'CS', 2026, 8.8, 'Java, Spring Boot, MySQL, AWS', 'https://resume.com/bobjohnson', 'placed'],
    ['Charlie Brown', 'charlie.brown@college.edu', '+15550103', 'IT', 2026, 7.5, 'C++, Python, SQL, HTML/CSS', 'https://resume.com/charliebrown', 'active'],
    ['David Miller', 'david.miller@college.edu', '+15550104', 'ECE', 2026, 8.1, 'C, Embedded C, Verilog, Raspberry Pi', 'https://resume.com/davidmiller', 'placed'],
    ['Eva Davis', 'eva.davis@college.edu', '+15550105', 'EE', 2026, 6.9, 'MATLAB, Control Systems, Power Electronics', 'https://resume.com/evadavis', 'active'],
    ['Frank Wilson', 'frank.wilson@college.edu', '+15550106', 'ME', 2026, 7.8, 'SolidWorks, AutoCAD, Thermodynamics', 'https://resume.com/frankwilson', 'active'],
    ['Grace Lee', 'grace.lee@college.edu', '+15550107', 'CS', 2026, 9.5, 'Go, Python, Kubernetes, Docker, PostgreSQL', 'https://resume.com/gracelee', 'active'],
    ['Henry Jones', 'henry.jones@college.edu', '+15550108', 'IT', 2026, 8.4, 'Kotlin, Swift, Mobile App Dev, Firebase', 'https://resume.com/henryjones', 'placed'],
    ['Ivy Taylor', 'ivy.taylor@college.edu', '+15550109', 'ECE', 2026, 9.0, 'Python, TensorFlow, Deep Learning, OpenCV', 'https://resume.com/ivytaylor', 'active'],
    ['Jack Thomas', 'jack.thomas@college.edu', '+15550110', 'ME', 2026, 5.8, 'SolidWorks, Excel', 'https://resume.com/jackthomas', 'not_eligible']
  ];

  for (const s of students) {
    insertStudent.run(...s);
  }

  // 2. Insert Companies
  const insertCompany = db.prepare(`
    INSERT INTO companies (name, industry, website, hr_name, hr_email, hr_phone, description)
    VALUES (?, ?, ?, ?, ?, ?, ?)
  `);

  const companies = [
    ['Google', 'Technology', 'https://google.com', 'Sarah Jenkins', 'sarah.jenkins@google.com', '+15550201', 'Global technology leader specializing in search, cloud, software, and hardware.'],
    ['Microsoft', 'Technology', 'https://microsoft.com', 'Michael Chang', 'mchang@microsoft.com', '+15550202', 'Empowering every person and organization on the planet to achieve more.'],
    ['Stripe', 'Fintech', 'https://stripe.com', 'Emma Watson', 'ewatson@stripe.com', '+15550203', 'Financial infrastructure for the internet, enabling payments and operations.'],
    ['JPMorgan Chase', 'Finance', 'https://jpmorgan.com', 'Richard Ross', 'rross@jpmorgan.com', '+15550204', 'Leading global financial services firm and one of the largest banking institutions.'],
    ['Tesla', 'Automotive / Energy', 'https://tesla.com', 'Elon Muskett', 'hr@tesla.com', '+15550205', 'Accelerating the world\'s transition to sustainable energy through EVs and clean tech.']
  ];

  for (const c of companies) {
    insertCompany.run(...c);
  }

  // 3. Insert Job Postings
  const insertJob = db.prepare(`
    INSERT INTO job_postings (company_id, title, description, requirements, salary_min, salary_max, location, job_type, min_cgpa, eligible_depts, deadline, status)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `);

  const jobs = [
    [1, 'Software Engineer', 'Develop next-generation search and AI products.', 'BS/MS in CS or related field. Experience in Java, C++, or Python.', 120000, 160000, 'Mountain View, CA', 'full_time', 8.5, 'CS,IT,ECE', '2026-08-15', 'open'],
    [1, 'Cloud Support Associate', 'Help enterprise clients build on Google Cloud Platform.', 'Understanding of networking, Linux, and databases.', 75000, 95000, 'Austin, TX', 'full_time', 7.0, '', '2026-08-01', 'open'],
    [2, 'Software Engineer', 'Work on Windows, Azure, or Office product suites.', 'Strong foundation in computer science fundamentals, data structures, and algorithms.', 115000, 145000, 'Redmond, WA', 'full_time', 8.0, 'CS,IT,ECE', '2026-08-10', 'open'],
    [3, 'Backend Engineer (API)', 'Design APIs that process billions of dollars of online commerce.', 'Proficiency in Ruby, Go, or Java. Solid system design skills.', 130000, 170000, 'San Francisco, CA (Hybrid)', 'full_time', 8.5, 'CS,IT', '2026-07-25', 'open'],
    [4, 'Technology Analyst', 'Build and support software systems for trade processing and analytics.', 'Java/Python skills. Strong analytical and communication skills.', 90000, 110000, 'New York, NY', 'full_time', 7.5, '', '2026-08-20', 'open'],
    [5, 'Embedded Software Intern', 'Write firmware for vehicle safety and autopilot systems.', 'Experience with C/C++, microcontrollers, RTOS, and debugging tools.', 35, 55, 'Palo Alto, CA', 'internship', 7.8, 'ECE,EE,ME', '2026-07-15', 'open'],
    [5, 'Mechanical Engineer', 'Design battery modules and powertrain components.', 'Proficiency in CAD modeling, finite element analysis (FEA), and materials science.', 95000, 125000, 'Fremont, CA', 'full_time', 7.5, 'ME', '2026-07-01', 'closed']
  ];

  for (const j of jobs) {
    insertJob.run(...j);
  }

  // 4. Insert Applications
  const insertApp = db.prepare(`
    INSERT INTO applications (student_id, job_posting_id, status, notes)
    VALUES (?, ?, ?, ?)
  `);

  const applications = [
    [1, 1, 'interview_scheduled', 'Alice passed the first technical round for Google SWE.'],
    [1, 3, 'applied', 'Applied via referral.'],
    [2, 1, 'selected', 'Offer extended and accepted.'],
    [3, 5, 'applied', 'Interested in finance analyst role.'],
    [4, 6, 'selected', 'Outstanding internship interview performance.'],
    [5, 5, 'rejected', 'CGPA below standard eligibility limit.'],
    [7, 1, 'shortlisted', 'High CGPA candidate.'],
    [7, 4, 'interview_scheduled', 'Stripe Backend team scheduled technical test.'],
    [8, 3, 'selected', 'Excellent mobile development credentials. Offer accepted.']
  ];

  for (const a of applications) {
    insertApp.run(...a);
  }

  // 5. Insert Interviews
  const insertInterview = db.prepare(`
    INSERT INTO interviews (application_id, round, scheduled_at, mode, interviewer, feedback, result)
    VALUES (?, ?, ?, ?, ?, ?, ?)
  `);

  const interviews = [
    [1, 1, '2026-06-15 10:00:00', 'virtual', 'John Doe (Google SWE)', 'Strong in algorithms, coded solution in Python cleanly.', 'pass'],
    [1, 2, '2026-07-02 14:00:00', 'virtual', 'Jane Smith (Google Lead)', 'Upcoming system design interview.', null],
    [8, 1, '2026-06-12 11:30:00', 'virtual', 'Alice Carter (Microsoft HR)', 'Good behavioral profile and project alignment.', 'pass'],
    [8, 2, '2026-06-18 15:00:00', 'virtual', 'Ben Vance (Microsoft Principal)', 'Deep Kotlin design expertise. Hire recommendation.', 'pass'],
    [7, 1, '2026-06-25 13:00:00', 'virtual', 'Emily Stark (Stripe Staff)', 'Perfect code execution in Go. Advancing to systems round.', 'pass']
  ];

  for (const i of interviews) {
    insertInterview.run(...i);
  }

  // 6. Insert Placements
  const insertPlacement = db.prepare(`
    INSERT INTO placements (student_id, job_posting_id, company_id, offer_date, joining_date, ctc)
    VALUES (?, ?, ?, ?, ?, ?)
  `);

  const placements = [
    [2, 1, 1, '2026-06-20', '2026-08-01', 150000.00],
    [4, 6, 5, '2026-06-22', '2026-08-15', 105000.00],
    [8, 3, 2, '2026-06-24', '2026-08-10', 135000.00]
  ];

  for (const p of placements) {
    insertPlacement.run(...p);
  }

  console.log('Database seeded successfully!');
}

seed();
db.close();
