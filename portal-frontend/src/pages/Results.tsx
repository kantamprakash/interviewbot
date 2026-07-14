import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/Results.css';

export default function Results() {
  const navigate = useNavigate();

  return (
    <div className="results">
      <header className="results-header">
        <h1>✅ Interview Submitted</h1>
      </header>

      <section className="score-section">
        <div style={{
          background: 'white',
          borderRadius: '12px',
          padding: '3rem',
          textAlign: 'center',
          boxShadow: '0 8px 24px rgba(0, 0, 0, 0.15)',
        }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🎉</div>
          <h2 style={{ margin: '0 0 1rem', color: '#1a1f36' }}>Thank you for completing your interview!</h2>
          <p style={{ color: '#7a8396', fontSize: '1.05rem', lineHeight: 1.6 }}>
            Your answers have been submitted and are being reviewed. The admin
            will evaluate your responses and follow up with you regarding the
            results — scores are not shown to candidates directly.
          </p>
          <button className="btn-primary" style={{ marginTop: '1.5rem' }} onClick={() => navigate('/')}>
            ← Back to My Interviews
          </button>
        </div>
      </section>
    </div>
  );
}
