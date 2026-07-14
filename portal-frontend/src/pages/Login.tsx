import React, { useState } from 'react';
import '../styles/Login.css';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    setError('');

    if (!email || !password) {
      setError('Please fill in all fields');
      return;
    }

    setLoading(true);

    try {
      const response = await fetch('http://localhost:8301/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        setError('Invalid email or password');
        setLoading(false);
        return;
      }

      const user = await response.json();
      localStorage.setItem('user', JSON.stringify(user));
      // Force page reload to ensure state updates
      window.location.href = '/';
    } catch (err) {
      setError('Unable to reach the server. Please try again.');
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <div className="login-header">
          <div className="login-icon">🎓</div>
          <h1>AI Interview Portal</h1>
          <p>Smart Technical Interviewing Platform</p>
        </div>

        <form onSubmit={handleLogin} className="login-form" noValidate>
          {error && <div className="error-message">{error}</div>}

          <div className="form-group">
            <label htmlFor="email">Email Address</label>
            <input
              id="email"
              type="email"
              placeholder="Enter your email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
          </div>

          <button
            type="submit"
            className="btn-login"
            disabled={loading}
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <div className="login-footer">
          <h3>Demo Credentials:</h3>
          <div className="credentials-box">
            <div className="credential-item">
              <strong>Admin Account:</strong>
              <p>Email: admin@interview.com</p>
              <p>Password: admin123</p>
            </div>
            <div className="credential-item">
              <strong>Candidate Account:</strong>
              <p>Email: user@interview.com</p>
              <p>Password: user123</p>
            </div>
          </div>
        </div>
      </div>

      <div className="login-background">
        <div className="floating-box box-1">📊</div>
        <div className="floating-box box-2">💻</div>
        <div className="floating-box box-3">🚀</div>
        <div className="floating-box box-4">🎯</div>
      </div>
    </div>
  );
}
