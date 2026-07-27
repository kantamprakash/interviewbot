import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom';
import Login from './pages/Login.tsx';
import InterviewFlow from './pages/InterviewFlow.tsx';
import MyInterviews from './pages/MyInterviews.tsx';
import Results from './pages/Results.tsx';
import AdminPanel from './pages/AdminPanel.tsx';
import './styles/App.css';

interface User {
  id: number;
  email: string;
  name: string;
  role: string;
}

function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const [user, setUser] = useState<User | null>(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      try {
        return JSON.parse(storedUser);
      } catch (e) {
        console.error('Failed to parse user:', e);
        localStorage.removeItem('user');
      }
    }
    return null;
  });

  useEffect(() => {
    const checkAuth = () => {
      const storedUser = localStorage.getItem('user');
      if (storedUser) {
        try {
          setUser(JSON.parse(storedUser));
        } catch (e) {
          console.error('Failed to parse user:', e);
          localStorage.removeItem('user');
          setUser(null);
        }
      } else {
        setUser(null);
      }
    };

    checkAuth();

    window.addEventListener('storage', checkAuth);
    return () => window.removeEventListener('storage', checkAuth);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('user');
    setUser(null);
    navigate('/login');
  };

  if (!user) {
    return (
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="*" element={<Login />} />
      </Routes>
    );
  }

  const isAdmin = user.role === 'ADMIN';

  // Admins operate entirely within the full-page Admin Panel.
  if (isAdmin) {
    return (
      <Routes>
        <Route path="/admin" element={<AdminPanel onLogout={handleLogout} />} />
        <Route path="*" element={<Navigate to="/admin" replace />} />
      </Routes>
    );
  }

  const isSinglePageView = ['/interview', '/results'].some(path =>
    location.pathname.startsWith(path)
  );

  if (isSinglePageView) {
    return (
      <Routes>
        <Route path="/interview/:sessionId" element={<InterviewFlow />} />
        <Route path="/results/:sessionId" element={<Results />} />
      </Routes>
    );
  }

  const userInitial = user.name.charAt(0).toUpperCase();

  return (
    <div className="app-container">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span style={{ fontSize: '1.5rem' }}>🎓</span>
          <h2>AI Interview</h2>
        </div>
        <nav className="sidebar-nav">
          <button className="sidebar-nav-item active" onClick={() => navigate('/')}>
            <span className="sidebar-nav-icon">📋</span>
            <span>My Interviews</span>
          </button>
          <button className="sidebar-nav-item" onClick={handleLogout}>
            <span className="sidebar-nav-icon">🚪</span>
            <span>Logout</span>
          </button>
        </nav>
      </aside>
      <main>
        <header className="page-header">
          <h1>Interview Portal</h1>
          <div className="header-actions">
            <div className="user-profile">
              <div className="user-avatar">{userInitial}</div>
              <div>
                <div style={{ fontWeight: 600, fontSize: '0.95rem' }}>{user.name}</div>
                <div style={{ fontSize: '0.85rem', color: '#7a8396' }}>{user.role}</div>
              </div>
            </div>
          </div>
        </header>
        <div className="page-content">
          <Routes>
            <Route path="/" element={<MyInterviews />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </main>
    </div>
  );
}

function App() {
  return (
    <Router>
      <AppLayout />
    </Router>
  );
}

export default App;
