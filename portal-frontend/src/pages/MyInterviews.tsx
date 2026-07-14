import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import interviewService, { InterviewSession } from '../services/interviewService.ts';
import '../styles/Dashboard.css';

const STATUS_LABELS: Record<string, string> = {
  SCHEDULED: 'Not Started',
  IN_PROGRESS: 'In Progress',
  COMPLETED: 'Submitted',
};

export default function MyInterviews() {
  const navigate = useNavigate();
  const [sessions, setSessions] = useState<InterviewSession[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSessions();
  }, []);

  const fetchSessions = async () => {
    setLoading(true);
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      const data = await interviewService.getCandidateSessions(user.id);
      setSessions(data);
    } catch (error) {
      console.error('Failed to fetch interviews:', error);
    } finally {
      setLoading(false);
    }
  };

  const stats = {
    total: sessions.length,
    scheduled: sessions.filter(s => s.status === 'SCHEDULED').length,
    inProgress: sessions.filter(s => s.status === 'IN_PROGRESS').length,
    completed: sessions.filter(s => s.status === 'COMPLETED').length,
  };

  return (
    <div className="dashboard">
      <header>
        <h1>🎓 My Interviews</h1>
      </header>

      <main>
        <div className="stats-section">
          <div className="stat-card">
            <h3>Total Assigned</h3>
            <div className="value">{stats.total}</div>
          </div>
          <div className="stat-card">
            <h3>Not Started</h3>
            <div className="value">{stats.scheduled}</div>
          </div>
          <div className="stat-card">
            <h3>In Progress</h3>
            <div className="value">{stats.inProgress}</div>
          </div>
          <div className="stat-card">
            <h3>Submitted</h3>
            <div className="value">{stats.completed}</div>
          </div>
        </div>

        <div className="sessions">
          <h2>📋 Assigned Interviews</h2>
          {loading ? (
            <div className="empty-state">
              <div className="loading"></div>
              <p>Loading your interviews...</p>
            </div>
          ) : sessions.length === 0 ? (
            <div className="empty-state">
              <p>No interviews have been scheduled for you yet. Check back later.</p>
            </div>
          ) : (
            <ul>
              {sessions.map((session) => (
                <li key={session.id}>
                  <div>
                    <span>{session.title}</span>
                    <br />
                    <small>
                      Status: <strong>{STATUS_LABELS[session.status] || session.status}</strong>
                      {' | '}{session.assignedQuestions.length} question{session.assignedQuestions.length === 1 ? '' : 's'}
                      {session.dueAt && <> {' | '}Due: {new Date(session.dueAt).toLocaleDateString()}</>}
                    </small>
                  </div>
                  {session.status === 'COMPLETED' ? (
                    <button disabled>Submitted — awaiting review</button>
                  ) : (
                    <button onClick={() => navigate(`/interview/${session.id}`)}>
                      {session.status === 'IN_PROGRESS' ? 'Continue Interview' : 'Start Interview'}
                    </button>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      </main>
    </div>
  );
}
