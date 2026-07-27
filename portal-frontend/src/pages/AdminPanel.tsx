import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import interviewService, { apiClient, InterviewSession } from '../services/interviewService.ts';
import '../styles/AdminPanel.css';

interface Question {
  id?: number;
  questionText: string;
  topic: string;
  difficulty: string;
  isActive: boolean;
}

interface User {
  id?: number;
  name: string;
  email: string;
  password?: string;
  role: string;
  isActive: boolean;
}

interface ScheduleForm {
  candidateId: string;
  title: string;
  dueAt: string;
  questionIds: number[];
}

const emptyScheduleForm: ScheduleForm = { candidateId: '', title: '', dueAt: '', questionIds: [] };

interface AdminPanelProps {
  onLogout: () => void;
}

export default function AdminPanel({ onLogout }: AdminPanelProps) {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('interviews');
  const [questions, setQuestions] = useState<Question[]>([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);

  const [formData, setFormData] = useState<Question>({
    questionText: '',
    topic: 'JavaScript',
    difficulty: 'MEDIUM',
    isActive: true,
  });

  const [users, setUsers] = useState<User[]>([]);
  const [userError, setUserError] = useState('');
  const [userFormData, setUserFormData] = useState<User>({
    name: '',
    email: '',
    password: '',
    role: 'CANDIDATE',
    isActive: true,
  });

  const [sessions, setSessions] = useState<InterviewSession[]>([]);
  const [sessionsLoading, setSessionsLoading] = useState(false);
  const [showScheduleForm, setShowScheduleForm] = useState(false);
  const [scheduleForm, setScheduleForm] = useState<ScheduleForm>(emptyScheduleForm);
  const [scheduleError, setScheduleError] = useState('');
  const [expandedSessionId, setExpandedSessionId] = useState<number | null>(null);

  const topics = ['JavaScript', 'React', 'System Design', 'Database', 'Backend', 'Cloud', 'Security'];
  const difficulties = ['EASY', 'MEDIUM', 'HARD'];

  useEffect(() => {
    fetchQuestions();
    fetchUsers();
  }, []);

  useEffect(() => {
    if (activeTab === 'interviews') {
      fetchSessions();
    }
  }, [activeTab]);

  const fetchQuestions = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get('/questions');
      setQuestions(response.data);
    } catch (error) {
      console.error('Failed to fetch questions:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddQuestion = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingId) {
        await apiClient.put(`/questions/${editingId}`, formData);
      } else {
        await apiClient.post('/questions', formData);
      }
      setShowForm(false);
      setEditingId(null);
      setFormData({ questionText: '', topic: 'JavaScript', difficulty: 'MEDIUM', isActive: true });
      fetchQuestions();
    } catch (error) {
      console.error('Failed to save question:', error);
    }
  };

  const handleEdit = (question: Question) => {
    setFormData(question);
    setEditingId(question.id || null);
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this question?')) return;

    try {
      await apiClient.delete(`/questions/${id}`);
      fetchQuestions();
    } catch (error) {
      console.error('Failed to delete question:', error);
    }
  };

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get('/users');
      setUsers(response.data);
    } catch (error) {
      console.error('Failed to fetch users:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddUser = async (e: React.FormEvent) => {
    e.preventDefault();
    setUserError('');
    try {
      await apiClient.post('/users', userFormData);
      setShowForm(false);
      setUserFormData({ name: '', email: '', password: '', role: 'CANDIDATE', isActive: true });
      fetchUsers();
    } catch (error: any) {
      console.error('Failed to create user:', error);
      setUserError(error?.response?.data?.message || 'Failed to create user');
    }
  };

  const handleDeleteUser = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this user?')) return;

    try {
      await apiClient.delete(`/users/${id}`);
      fetchUsers();
    } catch (error) {
      console.error('Failed to delete user:', error);
    }
  };

  const fetchSessions = async () => {
    setSessionsLoading(true);
    try {
      const data = await interviewService.getAdminSessions();
      setSessions(data);
    } catch (error) {
      console.error('Failed to fetch interviews:', error);
    } finally {
      setSessionsLoading(false);
    }
  };

  const candidates = users.filter(u => u.role === 'CANDIDATE' && u.isActive);

  const toggleQuestionInSchedule = (id: number) => {
    setScheduleForm(prev => ({
      ...prev,
      questionIds: prev.questionIds.includes(id)
        ? prev.questionIds.filter(qid => qid !== id)
        : [...prev.questionIds, id],
    }));
  };

  const handleScheduleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setScheduleError('');

    if (!scheduleForm.candidateId) {
      setScheduleError('Please select a candidate');
      return;
    }
    if (scheduleForm.questionIds.length === 0) {
      setScheduleError('Please select at least one question');
      return;
    }

    try {
      await interviewService.scheduleInterview({
        candidateId: Number(scheduleForm.candidateId),
        title: scheduleForm.title || `Interview - ${new Date().toLocaleDateString()}`,
        questionIds: scheduleForm.questionIds,
        dueAt: scheduleForm.dueAt ? new Date(scheduleForm.dueAt).toISOString() : null,
      });
      setShowScheduleForm(false);
      setScheduleForm(emptyScheduleForm);
      fetchSessions();
    } catch (error: any) {
      console.error('Failed to schedule interview:', error);
      setScheduleError(error?.response?.data?.message || 'Failed to schedule interview');
    }
  };

  const stats = {
    totalQuestions: questions.length,
    byTopic: topics.map(t => ({ topic: t, count: questions.filter(q => q.topic === t).length })),
    byDifficulty: difficulties.map(d => ({ difficulty: d, count: questions.filter(q => q.difficulty === d).length })),
  };

  const formatScore = (session: InterviewSession) => {
    if (session.status !== 'COMPLETED' || !session.evaluation) return 'Pending';
    return `${session.evaluation.averageScore.toFixed(1)} / 10`;
  };

  return (
    <div className="admin-panel">
      <header className="admin-header">
        <h1>🔧 Admin Dashboard</h1>
        <div className="admin-header-actions">
          <button className="btn-back" onClick={() => navigate('/admin')}>🔄 Refresh</button>
          <button className="btn-logout" onClick={onLogout}>🚪 Logout</button>
        </div>
      </header>

      <div className="admin-container">
        <nav className="admin-nav">
          <button
            className={`nav-item ${activeTab === 'interviews' ? 'active' : ''}`}
            onClick={() => setActiveTab('interviews')}
          >
            🗓️ Interviews
          </button>
          <button
            className={`nav-item ${activeTab === 'questions' ? 'active' : ''}`}
            onClick={() => setActiveTab('questions')}
          >
            📝 Questions
          </button>
          <button
            className={`nav-item ${activeTab === 'analytics' ? 'active' : ''}`}
            onClick={() => setActiveTab('analytics')}
          >
            📊 Analytics
          </button>
          <button
            className={`nav-item ${activeTab === 'users' ? 'active' : ''}`}
            onClick={() => setActiveTab('users')}
          >
            👥 Users
          </button>
          <button
            className={`nav-item ${activeTab === 'settings' ? 'active' : ''}`}
            onClick={() => setActiveTab('settings')}
          >
            ⚙️ Settings
          </button>
        </nav>

        <div className="admin-content">
          {/* Interviews Tab */}
          {activeTab === 'interviews' && (
            <div className="tab-content">
              <div className="content-header">
                <h2>Scheduled Interviews</h2>
                <button className="btn-primary" onClick={() => {
                  setShowScheduleForm(!showScheduleForm);
                  setScheduleError('');
                }}>
                  {showScheduleForm ? '❌ Close' : '➕ Schedule Interview'}
                </button>
              </div>

              {showScheduleForm && (
                <form className="question-form" onSubmit={handleScheduleSubmit}>
                  {scheduleError && <div className="error-message">{scheduleError}</div>}

                  <div className="form-row">
                    <div className="form-group">
                      <label>Candidate *</label>
                      <select
                        value={scheduleForm.candidateId}
                        onChange={(e) => setScheduleForm({ ...scheduleForm, candidateId: e.target.value })}
                        required
                      >
                        <option value="">Select a candidate...</option>
                        {candidates.map(c => (
                          <option key={c.id} value={c.id}>{c.name} ({c.email})</option>
                        ))}
                      </select>
                    </div>

                    <div className="form-group">
                      <label>Due Date (optional)</label>
                      <input
                        type="date"
                        value={scheduleForm.dueAt}
                        onChange={(e) => setScheduleForm({ ...scheduleForm, dueAt: e.target.value })}
                      />
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Interview Title</label>
                    <input
                      type="text"
                      value={scheduleForm.title}
                      onChange={(e) => setScheduleForm({ ...scheduleForm, title: e.target.value })}
                      placeholder="e.g. Frontend Engineer - Round 1"
                    />
                  </div>

                  <div className="form-group">
                    <label>Assign Questions * ({scheduleForm.questionIds.length} selected)</label>
                    <div className="questions-list">
                      {questions.filter(q => q.isActive).map((q) => (
                        <div key={q.id} className="question-item">
                          <div className="question-content">
                            <p className="question-text">
                              <label style={{ cursor: 'pointer' }}>
                                <input
                                  type="checkbox"
                                  checked={scheduleForm.questionIds.includes(q.id!)}
                                  onChange={() => toggleQuestionInSchedule(q.id!)}
                                  style={{ marginRight: '0.6rem' }}
                                />
                                {q.questionText}
                              </label>
                            </p>
                            <div className="question-meta">
                              <span className="badge topic">{q.topic}</span>
                              <span className={`badge difficulty ${q.difficulty.toLowerCase()}`}>{q.difficulty}</span>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="form-buttons">
                    <button type="submit" className="btn-primary">Schedule Interview</button>
                    <button
                      type="button"
                      className="btn-secondary"
                      onClick={() => {
                        setShowScheduleForm(false);
                        setScheduleForm(emptyScheduleForm);
                      }}
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              )}

              <div className="users-list">
                <h3>All Scheduled Interviews</h3>
                {sessionsLoading ? (
                  <p className="loading">Loading interviews...</p>
                ) : sessions.length === 0 ? (
                  <p className="loading">No interviews scheduled yet.</p>
                ) : (
                  <table className="users-table">
                    <thead>
                      <tr>
                        <th>Candidate</th>
                        <th>Title</th>
                        <th>Status</th>
                        <th>Score</th>
                        <th>Due</th>
                        <th>Created</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {sessions.map((s) => (
                        <React.Fragment key={s.id}>
                          <tr>
                            <td>{s.candidateName}<br /><small>{s.candidateEmail}</small></td>
                            <td>{s.title}</td>
                            <td><span className="badge topic">{s.status}</span></td>
                            <td>{formatScore(s)}</td>
                            <td>{s.dueAt ? new Date(s.dueAt).toLocaleDateString() : '—'}</td>
                            <td>{new Date(s.createdAt).toLocaleDateString()}</td>
                            <td>
                              <button
                                className="btn-edit"
                                onClick={() => setExpandedSessionId(expandedSessionId === s.id ? null : s.id)}
                              >
                                {expandedSessionId === s.id ? 'Hide' : 'View'}
                              </button>
                            </td>
                          </tr>
                          {expandedSessionId === s.id && (
                            <tr>
                              <td colSpan={7}>
                                <div className="questions-list">
                                  {s.recordingAvailable && (
                                    <div className="recording-playback">
                                      <p><strong>🎥 Interview Recording</strong></p>
                                      <video
                                        controls
                                        src={interviewService.getRecordingUrl(s.id)}
                                        style={{ maxWidth: '480px', width: '100%' }}
                                      />
                                    </div>
                                  )}
                                  {s.assignedQuestions.map((q) => {
                                    const answer = s.answers.find(a => a.questionId === q.id.toString());
                                    return (
                                      <div key={q.id} className="question-item" style={{ alignItems: 'flex-start' }}>
                                        <div className="question-content">
                                          <p className="question-text">{q.questionText}</p>
                                          <div className="question-meta" style={{ marginBottom: '0.6rem' }}>
                                            <span className="badge topic">{q.topic}</span>
                                            <span className={`badge difficulty ${q.difficulty.toLowerCase()}`}>{q.difficulty}</span>
                                          </div>
                                          {answer ? (
                                            <>
                                              <p><strong>Answer:</strong> {answer.answerText}</p>
                                              <p><strong>Score:</strong> {answer.score != null ? `${answer.score}/10` : 'Pending'}</p>
                                              {answer.feedback && <p><strong>Feedback:</strong> {answer.feedback}</p>}
                                            </>
                                          ) : (
                                            <p><em>Not answered yet</em></p>
                                          )}
                                        </div>
                                      </div>
                                    );
                                  })}
                                </div>
                              </td>
                            </tr>
                          )}
                        </React.Fragment>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          )}

          {/* Questions Tab */}
          {activeTab === 'questions' && (
            <div className="tab-content">
              <div className="content-header">
                <h2>Interview Questions Management</h2>
                <button className="btn-primary" onClick={() => {
                  setShowForm(!showForm);
                  if (showForm) {
                    setEditingId(null);
                    setFormData({ questionText: '', topic: 'JavaScript', difficulty: 'MEDIUM', isActive: true });
                  }
                }}>
                  {showForm ? '❌ Close' : '➕ Add New Question'}
                </button>
              </div>

              {showForm && (
                <form className="question-form" onSubmit={handleAddQuestion}>
                  <div className="form-group">
                    <label>Question Text *</label>
                    <textarea
                      value={formData.questionText}
                      onChange={(e) => setFormData({ ...formData, questionText: e.target.value })}
                      placeholder="Enter question text..."
                      rows={4}
                      required
                    />
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label>Topic *</label>
                      <select
                        value={formData.topic}
                        onChange={(e) => setFormData({ ...formData, topic: e.target.value })}
                        required
                      >
                        {topics.map(t => <option key={t} value={t}>{t}</option>)}
                      </select>
                    </div>

                    <div className="form-group">
                      <label>Difficulty *</label>
                      <select
                        value={formData.difficulty}
                        onChange={(e) => setFormData({ ...formData, difficulty: e.target.value })}
                        required
                      >
                        {difficulties.map(d => <option key={d} value={d}>{d}</option>)}
                      </select>
                    </div>

                    <div className="form-group">
                      <label>
                        <input
                          type="checkbox"
                          checked={formData.isActive}
                          onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                        />
                        Active
                      </label>
                    </div>
                  </div>

                  <div className="form-buttons">
                    <button type="submit" className="btn-primary">
                      {editingId ? 'Update Question' : 'Add Question'}
                    </button>
                    <button
                      type="button"
                      className="btn-secondary"
                      onClick={() => {
                        setShowForm(false);
                        setEditingId(null);
                        setFormData({ questionText: '', topic: 'JavaScript', difficulty: 'MEDIUM', isActive: true });
                      }}
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              )}

              <div className="questions-stats">
                <div className="stat">
                  <div className="stat-value">{stats.totalQuestions}</div>
                  <div className="stat-label">Total Questions</div>
                </div>
                {stats.byDifficulty.map(d => (
                  <div key={d.difficulty} className="stat">
                    <div className="stat-value">{d.count}</div>
                    <div className="stat-label">{d.difficulty}</div>
                  </div>
                ))}
              </div>

              {loading ? (
                <p className="loading">Loading questions...</p>
              ) : (
                <div className="questions-list">
                  {questions.map((q) => (
                    <div key={q.id} className="question-item">
                      <div className="question-content">
                        <p className="question-text">{q.questionText}</p>
                        <div className="question-meta">
                          <span className="badge topic">{q.topic}</span>
                          <span className={`badge difficulty ${q.difficulty.toLowerCase()}`}>{q.difficulty}</span>
                          <span className={`badge status ${q.isActive ? 'active' : 'inactive'}`}>
                            {q.isActive ? '✓ Active' : '✗ Inactive'}
                          </span>
                        </div>
                      </div>
                      <div className="question-actions">
                        <button className="btn-edit" onClick={() => handleEdit(q)}>Edit</button>
                        <button className="btn-delete" onClick={() => handleDelete(q.id!)}>Delete</button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Analytics Tab */}
          {activeTab === 'analytics' && (
            <div className="tab-content">
              <h2>📊 Interview Analytics</h2>
              <div className="analytics-grid">
                <div className="analytics-card">
                  <h3>Questions by Topic</h3>
                  <div className="chart-container">
                    {stats.byTopic.map(t => (
                      <div key={t.topic} className="chart-bar">
                        <div className="bar-label">{t.topic}</div>
                        <div className="bar-value">
                          <div className="bar" style={{ width: `${(t.count / Math.max(...stats.byTopic.map(x => x.count), 1)) * 100}%` }}></div>
                          <span>{t.count}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="analytics-card">
                  <h3>Questions by Difficulty</h3>
                  <div className="pie-chart">
                    {stats.byDifficulty.map((d) => (
                      <div key={d.difficulty} className={`pie-segment difficulty-${d.difficulty.toLowerCase()}`}>
                        <span>{d.difficulty}: {d.count}</span>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="analytics-card">
                  <h3>Interviews</h3>
                  <div className="status-list">
                    <div className="status-item">
                      <span>Total Scheduled</span>
                      <span className="status-value">{sessions.length}</span>
                    </div>
                    <div className="status-item">
                      <span>Completed</span>
                      <span className="status-value">{sessions.filter(s => s.status === 'COMPLETED').length}</span>
                    </div>
                    <div className="status-item">
                      <span>Total Questions</span>
                      <span className="status-value">{stats.totalQuestions}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Users Tab */}
          {activeTab === 'users' && (
            <div className="tab-content">
              <div className="content-header">
                <h2>👥 Users & Credentials Management</h2>
                <button className="btn-primary" onClick={() => {
                  setShowForm(!showForm);
                  setUserError('');
                }}>
                  {showForm ? '❌ Close' : '➕ Create User'}
                </button>
              </div>

              {showForm && (
                <form className="user-form" onSubmit={handleAddUser}>
                  {userError && <div className="error-message">{userError}</div>}

                  <div className="form-group">
                    <label>Full Name *</label>
                    <input
                      type="text"
                      value={userFormData.name}
                      onChange={(e) => setUserFormData({ ...userFormData, name: e.target.value })}
                      placeholder="Enter user full name"
                      required
                    />
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label>Email *</label>
                      <input
                        type="email"
                        value={userFormData.email}
                        onChange={(e) => setUserFormData({ ...userFormData, email: e.target.value })}
                        placeholder="user@example.com"
                        required
                      />
                    </div>

                    <div className="form-group">
                      <label>Password *</label>
                      <input
                        type="password"
                        value={userFormData.password}
                        onChange={(e) => setUserFormData({ ...userFormData, password: e.target.value })}
                        placeholder="Set password"
                        required
                      />
                    </div>
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label>Role *</label>
                      <select
                        value={userFormData.role}
                        onChange={(e) => setUserFormData({ ...userFormData, role: e.target.value })}
                        required
                      >
                        <option value="CANDIDATE">Candidate (Interviewee)</option>
                        <option value="ADMIN">Admin</option>
                      </select>
                    </div>

                    <div className="form-group">
                      <label>
                        <input
                          type="checkbox"
                          checked={userFormData.isActive}
                          onChange={(e) => setUserFormData({ ...userFormData, isActive: e.target.checked })}
                        />
                        Active
                      </label>
                    </div>
                  </div>

                  <div className="form-buttons">
                    <button type="submit" className="btn-primary">Create User</button>
                    <button
                      type="button"
                      className="btn-secondary"
                      onClick={() => {
                        setShowForm(false);
                        setUserFormData({ name: '', email: '', password: '', role: 'CANDIDATE', isActive: true });
                      }}
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              )}

              <div className="users-list">
                <h3>System Users</h3>
                {loading ? (
                  <p className="loading">Loading users...</p>
                ) : (
                  <table className="users-table">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Status</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {users.map((u) => (
                        <tr key={u.id}>
                          <td>{u.name}</td>
                          <td>{u.email}</td>
                          <td><span className="badge topic">{u.role}</span></td>
                          <td>
                            <span className={`badge status ${u.isActive ? 'active' : 'inactive'}`}>
                              {u.isActive ? '✓ Active' : '✗ Inactive'}
                            </span>
                          </td>
                          <td>
                            <button className="btn-delete" onClick={() => handleDeleteUser(u.id!)}>Delete</button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          )}

          {/* Settings Tab */}
          {activeTab === 'settings' && (
            <div className="tab-content">
              <h2>⚙️ System Settings</h2>
              <div className="settings-container">
                <div className="setting-item">
                  <h3>Interview Configuration</h3>
                  <p>Questions per interview: set individually when scheduling</p>
                  <p>Scheduling: simple assignment, no enforced time window</p>
                </div>
                <div className="setting-item">
                  <h3>Evaluation Settings</h3>
                  <p>AI Model: Active</p>
                  <p>Auto-evaluation: Enabled on answer submission</p>
                  <p>Score visibility: Admin only</p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
