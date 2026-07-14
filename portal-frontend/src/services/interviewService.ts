import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8301/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const storedUser = localStorage.getItem('user');
  if (storedUser) {
    try {
      const user = JSON.parse(storedUser);
      config.headers['X-User-Id'] = user.id;
      config.headers['X-User-Role'] = user.role;
    } catch (e) {
      // ignore malformed stored user
    }
  }
  return config;
});

export interface AssignedQuestion {
  id: number;
  questionText: string;
  topic: string;
  difficulty: string;
}

export interface Answer {
  id: number;
  questionId: string;
  answerText: string;
  score: number | null;
  feedback: string | null;
  submittedAt: string;
}

export interface InterviewSession {
  id: number;
  title: string;
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED';
  candidateId: number;
  candidateName: string;
  candidateEmail: string;
  dueAt: string | null;
  assignedQuestions: AssignedQuestion[];
  createdAt: string;
  completedAt: string | null;
  answers: Answer[];
  evaluation: {
    overallScore: number;
    averageScore: number;
    totalQuestionsAttempted: number;
    summary: string;
    evaluatedAt: string;
  } | null;
}

export { apiClient };

const interviewService = {
  getCandidateSessions: (candidateId: number): Promise<InterviewSession[]> =>
    apiClient.get(`/interviews/candidate/${candidateId}`).then(res => res.data),

  getAdminSessions: (): Promise<InterviewSession[]> =>
    apiClient.get('/interviews').then(res => res.data),

  getSession: (id: string | number): Promise<InterviewSession> =>
    apiClient.get(`/interviews/${id}`).then(res => res.data),

  scheduleInterview: (payload: {
    candidateId: number;
    title: string;
    questionIds: number[];
    dueAt?: string | null;
  }): Promise<InterviewSession> =>
    apiClient.post('/interviews/schedule', payload).then(res => res.data),

  submitAnswer: (sessionId: string | number, questionId: string, answer: string) =>
    apiClient.post(`/interviews/${sessionId}/answers`, {
      questionId,
      answer,
    }).then(res => res.data),

  submitInterview: (sessionId: string | number): Promise<InterviewSession> =>
    apiClient.post(`/interviews/${sessionId}/submit`).then(res => res.data),

  getCandidates: () => apiClient.get('/users', { params: { role: 'CANDIDATE' } }).then(res => res.data),
};

export default interviewService;
