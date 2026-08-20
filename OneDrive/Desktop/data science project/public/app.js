// ── STATE MANAGEMENT ──────────────────────────────────────────────────────────
const state = {
  activeTab: 'dashboard',
  students: [],
  companies: [],
  jobs: [],
  applications: [],
  interviews: [],
  placements: [],
  analytics: {},
  charts: {
    salary: null,
    dept: null
  }
};

const API_BASE = '/api';

// ── INIT & EVENT LISTENERS ──────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  initClock();
  initRouting();
  initFormHandlers();
  initFilterHandlers();
  
  // Load initial data
  loadData();
});

// Live Clock Helper
function initClock() {
  const clockEl = document.getElementById('live-clock');
  const updateClock = () => {
    const now = new Date();
    clockEl.textContent = now.toLocaleDateString('en-US', {
      month: 'long',
      day: 'numeric',
      year: 'numeric'
    }) + ' ' + now.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: true
    });
  };
  updateClock();
  setInterval(updateClock, 1000);
}

// Client Side Router
function initRouting() {
  const menuItems = document.querySelectorAll('.menu-item');
  menuItems.forEach(item => {
    item.addEventListener('click', (e) => {
      e.preventDefault();
      const tab = item.getAttribute('data-tab');
      switchTab(tab);
    });
  });

  // Handle back/forward and direct links
  window.addEventListener('hashchange', () => {
    const hash = window.location.hash.substring(1);
    if (hash && ['dashboard', 'students', 'companies', 'jobs', 'applications', 'interviews', 'placements'].includes(hash)) {
      switchTab(hash, false);
    }
  });

  // Initial routing check
  const initialHash = window.location.hash.substring(1);
  if (initialHash) {
    switchTab(initialHash, false);
  }
}

function switchTab(tab, updateHash = true) {
  state.activeTab = tab;
  
  // Update sidebar active state
  document.querySelectorAll('.menu-item').forEach(item => {
    if (item.getAttribute('data-tab') === tab) {
      item.classList.add('active');
    } else {
      item.classList.remove('active');
    }
  });

  // Update panels display
  document.querySelectorAll('.tab-panel').forEach(panel => {
    if (panel.id === `tab-${tab}`) {
      panel.classList.add('active');
    } else {
      panel.classList.remove('active');
    }
  });

  if (updateHash) {
    window.location.hash = tab;
  }

  // Refresh tab data
  loadTabSpecificData(tab);
}

// ── API DATA LOADERS ────────────────────────────────────────────────────────
async function loadData() {
  await loadAnalytics();
  loadTabSpecificData(state.activeTab);
}

async function loadTabSpecificData(tab) {
  switch (tab) {
    case 'dashboard':
      await loadAnalytics();
      break;
    case 'students':
      await loadStudents();
      break;
    case 'companies':
      await loadCompanies();
      break;
    case 'jobs':
      await loadJobs();
      break;
    case 'applications':
      await loadApplications();
      break;
    case 'interviews':
      await loadInterviews();
      break;
    case 'placements':
      await loadPlacements();
      break;
  }
}

// ── GET API ENDPOINTS CALLS ─────────────────────────────────────────────────

async function loadAnalytics() {
  try {
    const res = await fetch(`${API_BASE}/analytics/dashboard`);
    const data = await res.json();
    state.analytics = data;

    // Set metrics
    document.getElementById('stat-placement-rate').textContent = `${data.counts.placement_rate}%`;
    document.getElementById('stat-placed').textContent = data.counts.placed_students;
    document.getElementById('stat-companies').textContent = data.counts.companies;
    document.getElementById('stat-open-jobs').textContent = data.counts.active_jobs;

    renderRecentPlacementsFeed(data.recent_placements);
    renderUpcomingInterviewsFeed(data.upcoming_interviews);
    renderCharts(data);
  } catch (err) {
    console.error('Failed to load dashboard analytics:', err);
  }
}

async function loadStudents() {
  try {
    const search = document.getElementById('filter-student-search').value;
    const department = document.getElementById('filter-student-dept').value;
    const status = document.getElementById('filter-student-status').value;

    let url = `${API_BASE}/students?`;
    if (search) url += `search=${encodeURIComponent(search)}&`;
    if (department) url += `department=${encodeURIComponent(department)}&`;
    if (status) url += `status=${encodeURIComponent(status)}&`;

    const res = await fetch(url);
    const data = await res.json();
    state.students = data;
    renderStudentsTable(data);
  } catch (err) {
    console.error('Failed to load students directory:', err);
  }
}

async function loadCompanies() {
  try {
    const search = document.getElementById('filter-company-search').value;
    let url = `${API_BASE}/companies?`;
    if (search) url += `search=${encodeURIComponent(search)}`;

    const res = await fetch(url);
    const data = await res.json();
    state.companies = data;
    renderCompaniesTable(data);
  } catch (err) {
    console.error('Failed to load companies:', err);
  }
}

async function loadJobs() {
  try {
    const search = document.getElementById('filter-job-search').value;
    const status = document.getElementById('filter-job-status').value;

    let url = `${API_BASE}/jobs?`;
    if (search) url += `search=${encodeURIComponent(search)}&`;
    if (status) url += `status=${encodeURIComponent(status)}&`;

    const res = await fetch(url);
    const data = await res.json();
    state.jobs = data;
    renderJobsGrid(data);
  } catch (err) {
    console.error('Failed to load jobs list:', err);
  }
}

async function loadApplications() {
  try {
    const search = document.getElementById('filter-app-search').value;
    const status = document.getElementById('filter-app-status').value;

    let url = `${API_BASE}/applications?`;
    if (status) url += `status=${encodeURIComponent(status)}&`;

    const res = await fetch(url);
    let data = await res.json();
    
    // Client-side search filtering student/company name since backend doesn't support search on application route
    if (search) {
      const q = search.toLowerCase();
      data = data.filter(a => 
        a.student_name.toLowerCase().includes(q) || 
        a.company_name.toLowerCase().includes(q) || 
        a.job_title.toLowerCase().includes(q)
      );
    }
    
    state.applications = data;
    renderApplicationsTable(data);
  } catch (err) {
    console.error('Failed to load applications:', err);
  }
}

async function loadInterviews() {
  try {
    const result = document.getElementById('filter-interview-result').value;
    let url = `${API_BASE}/interviews?`;
    if (result) url += `result=${encodeURIComponent(result)}&`;

    const res = await fetch(url);
    const data = await res.json();
    state.interviews = data;
    renderInterviewsTable(data);
  } catch (err) {
    console.error('Failed to load interviews:', err);
  }
}

async function loadPlacements() {
  try {
    const res = await fetch(`${API_BASE}/placements`);
    const data = await res.json();
    state.placements = data;
    renderPlacementsTable(data);
  } catch (err) {
    console.error('Failed to load placements list:', err);
  }
}

// ── DOM RENDERERS ───────────────────────────────────────────────────────────

function renderRecentPlacementsFeed(placements) {
  const container = document.getElementById('feed-recent-placements');
  if (!placements || placements.length === 0) {
    container.innerHTML = '<div class="empty-state">No offers recorded yet.</div>';
    return;
  }

  container.innerHTML = placements.map(p => `
    <div class="feed-item">
      <div class="feed-item-detail">
        <span class="feed-item-title">${p.student_name}</span>
        <span class="feed-item-subtitle">${p.department} placed at ${p.company_name}</span>
      </div>
      <div class="feed-item-meta">
        <span class="value">$${p.ctc.toLocaleString()}</span>
        <span class="date">${formatDate(p.offer_date)}</span>
      </div>
    </div>
  `).join('');
}

function renderUpcomingInterviewsFeed(interviews) {
  const container = document.getElementById('feed-upcoming-interviews');
  if (!interviews || interviews.length === 0) {
    container.innerHTML = '<div class="empty-state">No interviews scheduled.</div>';
    return;
  }

  container.innerHTML = interviews.map(i => `
    <div class="feed-item">
      <div class="feed-item-detail">
        <span class="feed-item-title">${i.student_name}</span>
        <span class="feed-item-subtitle">${i.company_name} &bull; ${i.job_title}</span>
      </div>
      <div class="feed-item-meta">
        <span class="badge-mode" style="background: rgba(99, 102, 241, 0.1); color: #818cf8;">R${i.round} &bull; ${i.mode.replace('_', ' ')}</span>
        <span class="date">${formatDateTime(i.scheduled_at)}</span>
      </div>
    </div>
  `).join('');
}

function renderStudentsTable(students) {
  const tbody = document.getElementById('students-table-body');
  if (!students || students.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" class="empty-state">No students found matches criteria.</td></tr>';
    return;
  }

  tbody.innerHTML = students.map(s => `
    <tr>
      <td>
        <div class="user-profile" style="padding: 0; gap: 10px;">
          <img src="https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(s.name)}" alt="Avatar" style="width:30px; height:30px; border-radius:50%;">
          <div style="display:flex; flex-direction:column;">
            <strong style="font-size:14px;">${s.name}</strong>
            <span style="font-size:11px; color:var(--text-dim);">${s.email}</span>
          </div>
        </div>
      </td>
      <td>${s.department}</td>
      <td>${s.batch_year}</td>
      <td><strong>${s.cgpa.toFixed(2)}</strong></td>
      <td><span style="font-size:12px; color:var(--text-muted); max-width: 250px; display: inline-block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${s.skills || 'None'}</span></td>
      <td><span class="badge-status ${s.status}">${s.status.replace('_', ' ')}</span></td>
      <td>
        ${s.resume_url ? `<a href="${s.resume_url}" target="_blank" class="resume-link"><i class="fa-solid fa-file-pdf"></i> Resume</a>` : '<span style="color:var(--text-dim);">No link</span>'}
      </td>
      <td class="actions-col">
        <div class="table-actions">
          <button class="action-btn edit" onclick="editStudent(${s.id})" title="Edit Profile"><i class="fa-solid fa-pen"></i></button>
          <button class="action-btn delete" onclick="deleteStudent(${s.id})" title="Delete Profile"><i class="fa-solid fa-trash"></i></button>
        </div>
      </td>
    </tr>
  `).join('');
}

function renderCompaniesTable(companies) {
  const tbody = document.getElementById('companies-table-body');
  if (!companies || companies.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" class="empty-state">No recruiting partners registered.</td></tr>';
    return;
  }

  tbody.innerHTML = companies.map(c => `
    <tr>
      <td><strong>${c.name}</strong></td>
      <td>${c.industry || 'Tech'}</td>
      <td>${c.hr_name || 'N/A'}</td>
      <td>${c.hr_email || 'N/A'}</td>
      <td>${c.hr_phone || 'N/A'}</td>
      <td>
        ${c.website ? `<a href="${c.website}" target="_blank" class="resume-link"><i class="fa-solid fa-globe"></i> Website</a>` : 'N/A'}
      </td>
      <td style="font-size:13px; color:var(--text-muted); max-width:200px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${c.description || ''}</td>
      <td class="actions-col">
        <div class="table-actions">
          <button class="action-btn edit" onclick="editCompany(${c.id})" title="Edit Details"><i class="fa-solid fa-pen"></i></button>
          <button class="action-btn delete" onclick="deleteCompany(${c.id})" title="Remove Recruiter"><i class="fa-solid fa-trash"></i></button>
        </div>
      </td>
    </tr>
  `).join('');
}

function renderJobsGrid(jobs) {
  const grid = document.getElementById('jobs-grid-container');
  if (!jobs || jobs.length === 0) {
    grid.innerHTML = '<div class="empty-state" style="grid-column: span 3;">No job postings available.</div>';
    return;
  }

  grid.innerHTML = jobs.map(j => `
    <div class="job-card">
      <div>
        <div class="job-card-header">
          <div>
            <span class="role-title">${j.title}</span>
            <div class="comp-name">${j.company_name}</div>
          </div>
          <span class="badge-status ${j.status}">${j.status}</span>
        </div>
        <div class="job-details-meta">
          <span class="job-meta-item"><i class="fa-solid fa-location-dot"></i> ${j.location || 'Remote'}</span>
          <span class="job-meta-item"><i class="fa-solid fa-clock"></i> ${j.job_type.replace('_', ' ')}</span>
          <span class="job-meta-item"><i class="fa-solid fa-money-bill-wave"></i> $${j.salary_min ? j.salary_min.toLocaleString() : 'N/A'} - $${j.salary_max ? j.salary_max.toLocaleString() : 'N/A'}</span>
        </div>
        <div class="job-desc-preview">${j.description || 'No description provided.'}</div>
        <div class="job-eligibility">
          <div class="eligibility-row">
            <span class="eligibility-label">Min CGPA</span>
            <span class="eligibility-val">${j.min_cgpa ? j.min_cgpa.toFixed(1) : 'None'}</span>
          </div>
          <div class="eligibility-row">
            <span class="eligibility-label">Departments</span>
            <span class="eligibility-val" style="color: var(--primary); font-weight:600;">${j.eligible_depts || 'All Branches'}</span>
          </div>
          <div class="eligibility-row">
            <span class="eligibility-label">Deadline</span>
            <span class="eligibility-val">${j.deadline ? formatDate(j.deadline) : 'No Deadline'}</span>
          </div>
        </div>
      </div>
      <div class="job-card-footer">
        <div class="job-card-actions">
          <button class="action-btn edit" onclick="editJob(${j.id})"><i class="fa-solid fa-pen"></i></button>
          <button class="action-btn delete" onclick="deleteJob(${j.id})"><i class="fa-solid fa-trash"></i></button>
        </div>
        ${j.status === 'open' ? `<button class="btn btn-secondary btn-sm" onclick="openApplyModalWithJob(${j.id})">Apply Student</button>` : ''}
      </div>
    </div>
  `).join('');
}

function renderApplicationsTable(apps) {
  const tbody = document.getElementById('applications-table-body');
  if (!apps || apps.length === 0) {
    tbody.innerHTML = '<tr><td colspan="9" class="empty-state">No student applications submitted.</td></tr>';
    return;
  }

  tbody.innerHTML = apps.map(a => `
    <tr>
      <td><strong>${a.student_name}</strong></td>
      <td>${a.student_department}</td>
      <td><strong>${a.student_cgpa.toFixed(2)}</strong></td>
      <td>${a.company_name}</td>
      <td>${a.job_title}</td>
      <td>${formatDate(a.applied_at)}</td>
      <td><span class="badge-status ${a.status}">${a.status.replace('_', ' ')}</span></td>
      <td style="font-size:12px; color:var(--text-muted); max-width: 150px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${a.notes || ''}</td>
      <td class="actions-col">
        <div class="table-actions">
          <button class="action-btn edit" onclick="updateAppStatusPrompt(${a.id}, '${a.status}', '${a.student_name}', '${a.job_title}')" title="Change pipeline status"><i class="fa-solid fa-shuffle"></i></button>
          <button class="action-btn" onclick="openScheduleModal(${a.id}, '${a.student_name}', '${a.job_title}')" title="Schedule Interview"><i class="fa-solid fa-calendar-plus"></i></button>
          <button class="action-btn delete" onclick="deleteApplication(${a.id})" title="Withdraw Application"><i class="fa-solid fa-circle-xmark"></i></button>
        </div>
      </td>
    </tr>
  `).join('');
}

function renderInterviewsTable(interviews) {
  const tbody = document.getElementById('interviews-table-body');
  if (!interviews || interviews.length === 0) {
    tbody.innerHTML = '<tr><td colspan="10" class="empty-state">No interviews recorded.</td></tr>';
    return;
  }

  tbody.innerHTML = interviews.map(i => `
    <tr>
      <td><strong>${i.student_name}</strong></td>
      <td>${i.company_name}</td>
      <td>${i.job_title}</td>
      <td><span style="font-weight:600; color:var(--accent);">Round ${i.round}</span></td>
      <td><strong>${formatDateTime(i.scheduled_at)}</strong></td>
      <td><span style="text-transform: capitalize;">${i.mode.replace('_', ' ')}</span></td>
      <td>${i.interviewer || 'Not Assigned'}</td>
      <td style="font-size:12px; color:var(--text-muted); max-width: 150px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${i.feedback || 'No feedback yet.'}</td>
      <td>
        <span class="badge-status ${i.result ? i.result : 'pending'}">${i.result ? i.result : 'scheduled'}</span>
      </td>
      <td class="actions-col">
        <div class="table-actions">
          <button class="action-btn edit" onclick="editInterview(${i.id})" title="Update Outcome & Feedback"><i class="fa-solid fa-clipboard-question"></i></button>
          <button class="action-btn delete" onclick="deleteInterview(${i.id})" title="Cancel Interview"><i class="fa-solid fa-trash"></i></button>
        </div>
      </td>
    </tr>
  `).join('');
}

function renderPlacementsTable(placements) {
  const tbody = document.getElementById('placements-table-body');
  if (!placements || placements.length === 0) {
    tbody.innerHTML = '<tr><td colspan="9" class="empty-state">No placement records archived.</td></tr>';
    return;
  }

  tbody.innerHTML = placements.map(p => `
    <tr>
      <td><strong>${p.student_name}</strong></td>
      <td>${p.student_department}</td>
      <td>${p.company_name}</td>
      <td>${p.job_title}</td>
      <td>${formatDate(p.offer_date)}</td>
      <td>${p.joining_date ? formatDate(p.joining_date) : 'Pending'}</td>
      <td><strong style="color:var(--color-placed);">$${p.ctc.toLocaleString()}</strong></td>
      <td>
        ${p.offer_letter ? `<a href="${p.offer_letter}" target="_blank" class="resume-link"><i class="fa-solid fa-file-invoice-dollar"></i> Letter</a>` : '<span style="color:var(--text-dim);">N/A</span>'}
      </td>
      <td class="actions-col">
        <div class="table-actions">
          <button class="action-btn edit" onclick="editPlacement(${p.id})" title="Edit offer parameters"><i class="fa-solid fa-pen"></i></button>
          <button class="action-btn delete" onclick="deletePlacement(${p.id})" title="Revoke Record"><i class="fa-solid fa-trash"></i></button>
        </div>
      </td>
    </tr>
  `).join('');
}

// ── CHARTS RENDERER ─────────────────────────────────────────────────────────

function renderCharts(data) {
  const ctxSalary = document.getElementById('chart-salary-distribution').getContext('2d');
  const ctxDept = document.getElementById('chart-dept-placements').getContext('2d');

  // Destroy previous charts to avoid canvas hover overlapping issues
  if (state.charts.salary) state.charts.salary.destroy();
  if (state.charts.dept) state.charts.dept.destroy();

  // Color scheme constants
  const purpleGlow = '#8B5CF6';
  const blueGlow = '#6366F1';
  const emeraldGlow = '#10B981';

  // 1. Salary Distribution (Bar Chart)
  const salaryDist = data.salary_distribution;
  state.charts.salary = new Chart(ctxSalary, {
    type: 'bar',
    data: {
      labels: ['Under $50k', '$50k - $90k', '$90k - $130k', 'Above $130k'],
      datasets: [{
        label: 'Placed Students',
        data: [salaryDist.under_50k, salaryDist.tier_50k_90k, salaryDist.tier_90k_130k, data.salary_distribution.above_130k],
        backgroundColor: [
          'rgba(99, 102, 241, 0.45)',
          'rgba(139, 92, 246, 0.45)',
          'rgba(236, 72, 153, 0.45)',
          'rgba(16, 185, 129, 0.45)'
        ],
        borderColor: [
          '#6366F1',
          '#8B5CF6',
          '#EC4899',
          '#10B981'
        ],
        borderWidth: 1.5,
        borderRadius: 6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false }
      },
      scales: {
        x: { grid: { color: 'rgba(255, 255, 255, 0.05)' }, ticks: { color: '#9CA3AF', font: { family: 'Outfit' } } },
        y: { grid: { color: 'rgba(255, 255, 255, 0.05)' }, ticks: { precision: 0, color: '#9CA3AF', font: { family: 'Outfit' } } }
      }
    }
  });

  // 2. Department Placements (Horizontal Bar Chart)
  const depts = data.dept_stats.map(d => d.department);
  const rates = data.dept_stats.map(d => d.placement_rate || 0);

  state.charts.dept = new Chart(ctxDept, {
    type: 'bar',
    data: {
      labels: depts,
      datasets: [{
        label: 'Placement %',
        data: rates,
        backgroundColor: 'rgba(99, 102, 241, 0.55)',
        borderColor: '#6366F1',
        borderWidth: 1.5,
        borderRadius: 6,
        barThickness: 24
      }]
    },
    options: {
      indexAxis: 'y',
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false }
      },
      scales: {
        x: { 
          max: 100, 
          grid: { color: 'rgba(255, 255, 255, 0.05)' }, 
          ticks: { callback: value => value + '%', color: '#9CA3AF', font: { family: 'Outfit' } } 
        },
        y: { grid: { display: false }, ticks: { color: '#9CA3AF', font: { family: 'Outfit' } } }
      }
    }
  });
}

// ── CRUD OPERATIONS & MODALS ────────────────────────────────────────────────

// Generic open/close modal utilities
function openModal(id) {
  document.getElementById(id).classList.add('active');
}

function closeModal(id) {
  document.getElementById(id).classList.remove('active');
  const errDiv = document.getElementById(id.replace('modal', 'error'));
  if (errDiv) {
    errDiv.style.display = 'none';
    errDiv.textContent = '';
  }
}

// Global search handling across entire tables
document.getElementById('global-search').addEventListener('input', (e) => {
  const query = e.target.value.toLowerCase();
  
  if (state.activeTab === 'students') {
    document.getElementById('filter-student-search').value = query;
    loadStudents();
  } else if (state.activeTab === 'companies') {
    document.getElementById('filter-company-search').value = query;
    loadCompanies();
  } else if (state.activeTab === 'jobs') {
    document.getElementById('filter-job-search').value = query;
    loadJobs();
  } else if (state.activeTab === 'applications') {
    document.getElementById('filter-app-search').value = query;
    loadApplications();
  }
});

// Setup modal listings helpers
async function populateCompanyDropdown(selectId) {
  try {
    const res = await fetch(`${API_BASE}/companies`);
    const companies = await res.json();
    const select = document.getElementById(selectId);
    select.innerHTML = '<option value="">Select Recruiter</option>' + 
      companies.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
  } catch (err) {
    console.error('Failed to populate companies dropdown:', err);
  }
}

async function populateStudentDropdown(selectId) {
  try {
    // Only fetch active eligible students
    const res = await fetch(`${API_BASE}/students?status=active`);
    const students = await res.json();
    const select = document.getElementById(selectId);
    select.innerHTML = '<option value="">Select Candidate</option>' + 
      students.map(s => `<option value="${s.id}">${s.name} (CGPA: ${s.cgpa.toFixed(2)}, ${s.department})</option>`).join('');
  } catch (err) {
    console.error('Failed to populate student dropdown:', err);
  }
}

async function populateJobDropdown(selectId) {
  try {
    const res = await fetch(`${API_BASE}/jobs?status=open`);
    const jobs = await res.json();
    const select = document.getElementById(selectId);
    select.innerHTML = '<option value="">Select Job Opening</option>' + 
      jobs.map(j => `<option value="${j.id}">${j.company_name} - ${j.title}</option>`).join('');
  } catch (err) {
    console.error('Failed to populate job dropdown:', err);
  }
}

// 1. STUDENTS
function openStudentModal(student = null) {
  const form = document.getElementById('student-form');
  form.reset();
  document.getElementById('student-id').value = '';
  document.getElementById('student-status-field').style.display = student ? 'flex' : 'none';
  document.getElementById('student-modal-title').textContent = student ? 'Edit Student Profile' : 'Register Student';
  
  if (student) {
    document.getElementById('student-id').value = student.id;
    document.getElementById('student-name').value = student.name;
    document.getElementById('student-email').value = student.email;
    document.getElementById('student-phone').value = student.phone || '';
    document.getElementById('student-dept').value = student.department;
    document.getElementById('student-batch').value = student.batch_year;
    document.getElementById('student-cgpa').value = student.cgpa;
    document.getElementById('student-skills').value = student.skills || '';
    document.getElementById('student-resume').value = student.resume_url || '';
    document.getElementById('student-status').value = student.status;
  }
  openModal('modal-student');
}

async function editStudent(id) {
  try {
    const res = await fetch(`${API_BASE}/students/${id}`);
    const student = await res.json();
    openStudentModal(student);
  } catch (err) {
    console.error(err);
  }
}

async function deleteStudent(id) {
  if (!confirm('Are you sure you want to remove this student? This will delete all their applications and interviews!')) return;
  try {
    const res = await fetch(`${API_BASE}/students/${id}`, { method: 'DELETE' });
    const data = await res.json();
    alert(data.message);
    loadStudents();
    loadAnalytics();
  } catch (err) {
    console.error(err);
  }
}

// 2. COMPANIES
function openCompanyModal(company = null) {
  const form = document.getElementById('company-form');
  form.reset();
  document.getElementById('company-id').value = '';
  document.getElementById('company-modal-title').textContent = company ? 'Edit Company details' : 'Add Corporate Partner';

  if (company) {
    document.getElementById('company-id').value = company.id;
    document.getElementById('company-name').value = company.name;
    document.getElementById('company-industry').value = company.industry || '';
    document.getElementById('company-website').value = company.website || '';
    document.getElementById('company-hr').value = company.hr_name || '';
    document.getElementById('company-hr-email').value = company.hr_email || '';
    document.getElementById('company-hr-phone').value = company.hr_phone || '';
    document.getElementById('company-desc').value = company.description || '';
  }
  openModal('modal-company');
}

async function editCompany(id) {
  try {
    const res = await fetch(`${API_BASE}/companies/${id}`);
    const company = await res.json();
    openCompanyModal(company);
  } catch (err) {
    console.error(err);
  }
}

async function deleteCompany(id) {
  if (!confirm('Warning: Deleting a company will remove all their posted job openings and candidate interviews! Do you wish to continue?')) return;
  try {
    const res = await fetch(`${API_BASE}/companies/${id}`, { method: 'DELETE' });
    const data = await res.json();
    alert(data.message);
    loadCompanies();
    loadAnalytics();
  } catch (err) {
    console.error(err);
  }
}

// 3. JOBS
async function openJobModal(job = null) {
  const form = document.getElementById('job-form');
  form.reset();
  document.getElementById('job-id').value = '';
  document.getElementById('job-status-field').style.display = job ? 'flex' : 'none';
  document.getElementById('job-modal-title').textContent = job ? 'Modify Job Posting' : 'Post Job vacancy';

  await populateCompanyDropdown('job-company');

  if (job) {
    document.getElementById('job-id').value = job.id;
    document.getElementById('job-company').value = job.company_id;
    document.getElementById('job-title').value = job.title;
    document.getElementById('job-type').value = job.job_type;
    document.getElementById('job-location').value = job.location || '';
    document.getElementById('job-salary-min').value = job.salary_min || '';
    document.getElementById('job-salary-max').value = job.salary_max || '';
    document.getElementById('job-min-cgpa').value = job.min_cgpa || 0;
    document.getElementById('job-eligible-depts').value = job.eligible_depts || '';
    document.getElementById('job-deadline').value = job.deadline || '';
    document.getElementById('job-status').value = job.status;
    document.getElementById('job-desc').value = job.description || '';
    document.getElementById('job-reqs').value = job.requirements || '';
  }
  openModal('modal-job');
}

async function editJob(id) {
  try {
    const res = await fetch(`${API_BASE}/jobs/${id}`);
    const job = await res.json();
    openJobModal(job);
  } catch (err) {
    console.error(err);
  }
}

async function deleteJob(id) {
  if (!confirm('Are you sure you want to delete this job posting? This will remove related applications.')) return;
  try {
    const res = await fetch(`${API_BASE}/jobs/${id}`, { method: 'DELETE' });
    const data = await res.json();
    alert(data.message);
    loadJobs();
    loadAnalytics();
  } catch (err) {
    console.error(err);
  }
}

// 4. APPLICATIONS / APPLY
async function openApplyModal() {
  const form = document.getElementById('apply-form');
  form.reset();
  document.getElementById('apply-error').style.display = 'none';
  
  await populateStudentDropdown('apply-student');
  await populateJobDropdown('apply-job');
  openModal('modal-apply');
}

async function openApplyModalWithJob(jobId) {
  await openApplyModal();
  document.getElementById('apply-job').value = jobId;
}

async function updateAppStatusPrompt(id, currentStatus, studentName, jobTitle) {
  const statusOptions = ['applied', 'shortlisted', 'interview_scheduled', 'selected', 'rejected', 'offer_accepted', 'offer_declined'];
  
  let promptMsg = `Update application status for ${studentName} (${jobTitle}):\n\nChoose one of the following exactly:\n`;
  statusOptions.forEach((opt, idx) => { promptMsg += `${idx + 1}. ${opt}\n`; });
  
  const response = prompt(promptMsg, currentStatus);
  if (response === null) return;
  
  let newStatus = response.trim().toLowerCase();
  
  // Support numeric selections in prompt
  const valIndex = parseInt(newStatus) - 1;
  if (!isNaN(valIndex) && valIndex >= 0 && valIndex < statusOptions.length) {
    newStatus = statusOptions[valIndex];
  }

  if (!statusOptions.includes(newStatus)) {
    alert('Invalid status choice entered!');
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/applications/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: newStatus })
    });
    const data = await res.json();
    if (res.ok) {
      alert(data.message);
      loadApplications();
      loadAnalytics();
    } else {
      alert(data.error || 'Failed to update status');
    }
  } catch (err) {
    console.error(err);
  }
}

async function deleteApplication(id) {
  if (!confirm('Are you sure you want to withdraw/delete this application?')) return;
  try {
    const res = await fetch(`${API_BASE}/applications/${id}`, { method: 'DELETE' });
    const data = await res.json();
    alert(data.message);
    loadApplications();
    loadAnalytics();
  } catch (err) {
    console.error(err);
  }
}

// 5. INTERVIEWS
function openScheduleModal(appId, studentName = '', jobTitle = '') {
  const form = document.getElementById('interview-form');
  form.reset();
  document.getElementById('interview-id').value = '';
  document.getElementById('interview-app-id').value = appId;
  document.getElementById('interview-app-details').value = studentName && jobTitle ? `${studentName} ➔ ${jobTitle}` : `App ID: ${appId}`;
  document.getElementById('interview-result-field').style.display = 'none';
  document.getElementById('interview-modal-title').textContent = 'Schedule Interview Round';
  openModal('modal-interview');
}

async function editInterview(id) {
  try {
    const res = await fetch(`${API_BASE}/interviews`);
    const list = await res.json();
    const interview = list.find(i => i.id === id);
    if (!interview) return alert('Interview details not found');

    const form = document.getElementById('interview-form');
    form.reset();
    document.getElementById('interview-modal-title').textContent = 'Evaluate Interview Outcome';
    document.getElementById('interview-id').value = interview.id;
    document.getElementById('interview-app-id').value = interview.application_id;
    document.getElementById('interview-app-details').value = `${interview.student_name} ➔ ${interview.job_title}`;
    document.getElementById('interview-round').value = interview.round;
    document.getElementById('interview-date').value = interview.scheduled_at.replace(' ', 'T');
    document.getElementById('interview-mode').value = interview.mode;
    document.getElementById('interview-interviewer').value = interview.interviewer || '';
    document.getElementById('interview-feedback').value = interview.feedback || '';
    
    document.getElementById('interview-result-field').style.display = 'flex';
    document.getElementById('interview-result').value = interview.result || '';

    openModal('modal-interview');
  } catch (err) {
    console.error(err);
  }
}

async function deleteInterview(id) {
  if (!confirm('Cancel and delete this interview entry?')) return;
  try {
    const res = await fetch(`${API_BASE}/interviews/${id}`, { method: 'DELETE' });
    const data = await res.json();
    alert(data.message);
    loadInterviews();
    loadAnalytics();
  } catch (err) {
    console.error(err);
  }
}

// 6. PLACEMENTS
async function openPlacementModal(placement = null) {
  const form = document.getElementById('placement-form');
  form.reset();
  document.getElementById('placement-id').value = '';
  document.getElementById('placement-error').style.display = 'none';

  // Load students (any status since we can edit placement too) and jobs, companies
  try {
    const sRes = await fetch(`${API_BASE}/students`);
    const students = await sRes.json();
    document.getElementById('placement-student').innerHTML = '<option value="">Select Placed Student</option>' +
      students.map(s => `<option value="${s.id}">${s.name} (${s.department})</option>`).join('');

    const jRes = await fetch(`${API_BASE}/jobs`);
    const jobs = await jRes.json();
    document.getElementById('placement-job').innerHTML = '<option value="">Select Job Role</option>' +
      jobs.map(j => `<option value="${j.id}">${j.company_name} - ${j.title}</option>`).join('');

    const cRes = await fetch(`${API_BASE}/companies`);
    const companies = await cRes.json();
    document.getElementById('placement-company').innerHTML = '<option value="">Select Company</option>' +
      companies.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
  } catch (err) {
    console.error(err);
  }

  if (placement) {
    document.getElementById('placement-id').value = placement.id;
    document.getElementById('placement-student').value = placement.student_id;
    document.getElementById('placement-job').value = placement.job_posting_id;
    document.getElementById('placement-company').value = placement.company_id;
    document.getElementById('placement-ctc').value = placement.ctc;
    document.getElementById('placement-offer-date').value = placement.offer_date;
    document.getElementById('placement-joining-date').value = placement.joining_date || '';
    document.getElementById('placement-letter').value = placement.offer_letter || '';
  }

  openModal('modal-placement');
}

async function editPlacement(id) {
  try {
    const res = await fetch(`${API_BASE}/placements`);
    const list = await res.json();
    const placement = list.find(p => p.id === id);
    if (!placement) return alert('Placement record not found');
    await openPlacementModal(placement);
  } catch (err) {
    console.error(err);
  }
}

async function deletePlacement(id) {
  if (!confirm('Danger: Revoking a placement will set the student status back to Active. Proceed?')) return;
  try {
    const res = await fetch(`${API_BASE}/placements/${id}`, { method: 'DELETE' });
    const data = await res.json();
    alert(data.message);
    loadPlacements();
    loadAnalytics();
  } catch (err) {
    console.error(err);
  }
}

// ── FORM SUBMISSION CONTROLLERS ─────────────────────────────────────────────

function initFormHandlers() {
  // 1. Student Submit
  document.getElementById('student-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('student-id').value;
    const payload = {
      name: document.getElementById('student-name').value,
      email: document.getElementById('student-email').value,
      phone: document.getElementById('student-phone').value,
      department: document.getElementById('student-dept').value,
      batch_year: parseInt(document.getElementById('student-batch').value),
      cgpa: parseFloat(document.getElementById('student-cgpa').value),
      skills: document.getElementById('student-skills').value,
      resume_url: document.getElementById('student-resume').value,
      status: document.getElementById('student-status').value || 'active'
    };

    try {
      const url = id ? `${API_BASE}/students/${id}` : `${API_BASE}/students`;
      const method = id ? 'PUT' : 'POST';
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const data = await res.json();
      if (res.ok) {
        alert(data.message || 'Saved successfully');
        closeModal('modal-student');
        loadStudents();
        loadAnalytics();
      } else {
        alert(data.error || JSON.stringify(data.errors));
      }
    } catch (err) {
      console.error(err);
    }
  });

  // 2. Company Submit
  document.getElementById('company-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('company-id').value;
    const payload = {
      name: document.getElementById('company-name').value,
      industry: document.getElementById('company-industry').value,
      website: document.getElementById('company-website').value,
      hr_name: document.getElementById('company-hr').value,
      hr_email: document.getElementById('company-hr-email').value,
      hr_phone: document.getElementById('company-hr-phone').value,
      description: document.getElementById('company-desc').value
    };

    try {
      const url = id ? `${API_BASE}/companies/${id}` : `${API_BASE}/companies`;
      const method = id ? 'PUT' : 'POST';
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const data = await res.json();
      if (res.ok) {
        alert(data.message || 'Company saved');
        closeModal('modal-company');
        loadCompanies();
        loadAnalytics();
      } else {
        alert(data.error || JSON.stringify(data.errors));
      }
    } catch (err) {
      console.error(err);
    }
  });

  // 3. Job Submit
  document.getElementById('job-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('job-id').value;
    const payload = {
      company_id: parseInt(document.getElementById('job-company').value),
      title: document.getElementById('job-title').value,
      job_type: document.getElementById('job-type').value,
      location: document.getElementById('job-location').value,
      salary_min: parseFloat(document.getElementById('job-salary-min').value) || null,
      salary_max: parseFloat(document.getElementById('job-salary-max').value) || null,
      min_cgpa: parseFloat(document.getElementById('job-min-cgpa').value) || 0,
      eligible_depts: document.getElementById('job-eligible-depts').value,
      deadline: document.getElementById('job-deadline').value || null,
      status: document.getElementById('job-status').value || 'open',
      description: document.getElementById('job-desc').value,
      requirements: document.getElementById('job-reqs').value
    };

    try {
      const url = id ? `${API_BASE}/jobs/${id}` : `${API_BASE}/jobs`;
      const method = id ? 'PUT' : 'POST';
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const data = await res.json();
      if (res.ok) {
        alert(data.message || 'Job published');
        closeModal('modal-job');
        loadJobs();
        loadAnalytics();
      } else {
        alert(data.error || JSON.stringify(data.errors));
      }
    } catch (err) {
      console.error(err);
    }
  });

  // 4. Apply Application Submit
  document.getElementById('apply-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
      student_id: parseInt(document.getElementById('apply-student').value),
      job_posting_id: parseInt(document.getElementById('apply-job').value),
      notes: document.getElementById('apply-notes').value
    };

    const errorDiv = document.getElementById('apply-error');
    errorDiv.style.display = 'none';

    try {
      const res = await fetch(`${API_BASE}/applications`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const data = await res.json();
      if (res.ok) {
        alert(data.message);
        closeModal('modal-apply');
        loadApplications();
        loadAnalytics();
      } else {
        errorDiv.textContent = data.error || 'Failed to submit application';
        errorDiv.style.display = 'block';
      }
    } catch (err) {
      console.error(err);
    }
  });

  // 5. Schedule/outcome Interview Submit
  document.getElementById('interview-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('interview-id').value;
    const appId = parseInt(document.getElementById('interview-app-id').value);
    
    // Replace T with space to match SQLite DATETIME format
    const timeVal = document.getElementById('interview-date').value.replace('T', ' ');

    const payload = {
      application_id: appId,
      round: parseInt(document.getElementById('interview-round').value),
      scheduled_at: timeVal,
      mode: document.getElementById('interview-mode').value,
      interviewer: document.getElementById('interview-interviewer').value,
      feedback: document.getElementById('interview-feedback').value,
      result: document.getElementById('interview-result').value || null
    };

    try {
      const url = id ? `${API_BASE}/interviews/${id}` : `${API_BASE}/interviews`;
      const method = id ? 'PUT' : 'POST';
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const data = await res.json();
      if (res.ok) {
        alert(data.message);
        closeModal('modal-interview');
        loadInterviews();
        loadApplications();
        loadAnalytics();
      } else {
        alert(data.error || JSON.stringify(data.errors));
      }
    } catch (err) {
      console.error(err);
    }
  });

  // 6. Record Placement Offer Submit
  document.getElementById('placement-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('placement-id').value;
    
    const payload = {
      student_id: parseInt(document.getElementById('placement-student').value),
      job_posting_id: parseInt(document.getElementById('placement-job').value),
      company_id: parseInt(document.getElementById('placement-company').value),
      ctc: parseFloat(document.getElementById('placement-ctc').value),
      offer_date: document.getElementById('placement-offer-date').value,
      joining_date: document.getElementById('placement-joining-date').value || null,
      offer_letter: document.getElementById('placement-letter').value || null
    };

    const errorDiv = document.getElementById('placement-error');
    errorDiv.style.display = 'none';

    try {
      const url = id ? `${API_BASE}/placements/${id}` : `${API_BASE}/placements`;
      const method = id ? 'PUT' : 'POST';
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const data = await res.json();
      if (res.ok) {
        alert(data.message);
        closeModal('modal-placement');
        loadPlacements();
        loadStudents();
        loadAnalytics();
      } else {
        errorDiv.textContent = data.error || 'Failed to archive placement details';
        errorDiv.style.display = 'block';
      }
    } catch (err) {
      console.error(err);
    }
  });
}

// ── FILTER TRIGGER INITIALIZERS ─────────────────────────────────────────────

function initFilterHandlers() {
  // Students
  document.getElementById('filter-student-search').addEventListener('input', debounce(loadStudents, 250));
  document.getElementById('filter-student-dept').addEventListener('change', loadStudents);
  document.getElementById('filter-student-status').addEventListener('change', loadStudents);

  // Companies
  document.getElementById('filter-company-search').addEventListener('input', debounce(loadCompanies, 250));

  // Jobs
  document.getElementById('filter-job-search').addEventListener('input', debounce(loadJobs, 250));
  document.getElementById('filter-job-status').addEventListener('change', loadJobs);

  // Applications
  document.getElementById('filter-app-search').addEventListener('input', debounce(loadApplications, 250));
  document.getElementById('filter-app-status').addEventListener('change', loadApplications);

  // Interviews
  document.getElementById('filter-interview-result').addEventListener('change', loadInterviews);
}

// Debounce helper utility
function debounce(func, wait) {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

// ── UTILITIES ───────────────────────────────────────────────────────────────

function formatDate(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric'
  });
}

function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return '';
  // Handle space separator from SQLite DATETIME
  const date = new Date(dateTimeStr.replace(' ', 'T'));
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: true
  });
}
