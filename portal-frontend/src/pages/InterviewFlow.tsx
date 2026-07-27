import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import interviewService, { InterviewSession, AssignedQuestion } from '../services/interviewService.ts';
import useInterviewRecording from '../hooks/useInterviewRecording.ts';
import useSpeechToText from '../hooks/useSpeechToText.ts';
import '../styles/InterviewFlow.css';

export default function InterviewFlow() {
  const { sessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();
  const { isRecording, stopAndGetBlob } = useInterviewRecording();
  const [session, setSession] = useState<InterviewSession | null>(null);
  const [answeredIds, setAnsweredIds] = useState<Set<string>>(new Set());
  const [questionIndex, setQuestionIndex] = useState(0);
  const [answerText, setAnswerText] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState('');
  const [timeElapsed, setTimeElapsed] = useState(0);

  const appendTranscript = (text: string) => {
    if (!text) return;
    setAnswerText((prev) => (prev.trim() ? `${prev.trim()} ${text}` : text));
  };
  const {
    isSupported: speechSupported,
    isListening,
    interimTranscript,
    startListening,
    stopListening,
  } = useSpeechToText(appendTranscript);

  useEffect(() => {
    loadSession();
    const timer = setInterval(() => setTimeElapsed((t) => t + 1), 1000);
    return () => clearInterval(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadSession = async () => {
    try {
      setIsLoading(true);
      const data = await interviewService.getSession(sessionId!);

      if (data.status === 'COMPLETED') {
        navigate('/');
        return;
      }

      const answeredMap = new Map(data.answers.map((a) => [a.questionId, a.answerText]));
      const firstUnanswered = data.assignedQuestions.findIndex((q) => !answeredMap.has(q.id.toString()));
      const startIndex = firstUnanswered === -1 ? 0 : firstUnanswered;

      setSession(data);
      setAnsweredIds(new Set(answeredMap.keys()));
      setQuestionIndex(startIndex);
      setAnswerText(answeredMap.get(data.assignedQuestions[startIndex]?.id.toString()) || '');
    } catch (err) {
      console.error('Failed to load interview:', err);
      setError('Unable to load this interview. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const goToQuestion = (index: number, questions: AssignedQuestion[], answeredMap: Map<string, string>) => {
    stopListening();
    setQuestionIndex(index);
    setAnswerText(answeredMap.get(questions[index].id.toString()) || '');
  };

  const handleSaveAndNext = async () => {
    stopListening();
    if (!answerText.trim()) {
      alert('Please provide an answer before continuing');
      return;
    }
    if (!session) return;

    const currentQuestion = session.assignedQuestions[questionIndex];
    const isLastQuestion = questionIndex === session.assignedQuestions.length - 1;

    setIsSaving(true);
    try {
      await interviewService.submitAnswer(session.id, currentQuestion.id.toString(), answerText);
      setAnsweredIds((prev) => new Set(prev).add(currentQuestion.id.toString()));

      if (isLastQuestion) {
        await interviewService.submitInterview(session.id);
        try {
          const recordingBlob = await stopAndGetBlob();
          if (recordingBlob) {
            await interviewService.uploadRecording(session.id, recordingBlob);
          }
        } catch (recordingErr) {
          console.error('Failed to upload interview recording:', recordingErr);
        }
        navigate(`/results/${session.id}`);
      } else {
        const answeredMap = new Map(session.answers.map((a) => [a.questionId, a.answerText]));
        answeredMap.set(currentQuestion.id.toString(), answerText);
        goToQuestion(questionIndex + 1, session.assignedQuestions, answeredMap);
      }
    } catch (err) {
      console.error('Failed to save answer:', err);
      alert('Failed to save your answer. Please try again.');
    } finally {
      setIsSaving(false);
    }
  };

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  if (isLoading) {
    return (
      <div className="interview-flow">
        <main className="interview-main">
          <div className="loading-state">
            <div className="spinner"></div>
            <p>Loading interview...</p>
          </div>
        </main>
      </div>
    );
  }

  if (error || !session) {
    return (
      <div className="interview-flow">
        <main className="interview-main">
          <div className="error-state">
            <p>{error || 'Unable to load interview.'}</p>
            <button className="btn-primary" onClick={() => navigate('/')}>Back to My Interviews</button>
          </div>
        </main>
      </div>
    );
  }

  const totalQuestions = session.assignedQuestions.length;
  const currentQuestion = session.assignedQuestions[questionIndex];
  const isLastQuestion = questionIndex === totalQuestions - 1;
  const progressPercentage = (answeredIds.size / totalQuestions) * 100;

  return (
    <div className="interview-flow">
      <header className="interview-header">
        <div className="header-left">
          <h1>🎯 {session.title}</h1>
          <p className="session-id">Candidate: {session.candidateName}</p>
        </div>
        <div className="header-right">
          {isRecording && (
            <div className="recording-indicator">
              <span className="recording-dot">🔴</span>
              <span>Recording</span>
            </div>
          )}
          <div className="timer">
            <span className="timer-icon">⏱️</span>
            <span>{formatTime(timeElapsed)}</span>
          </div>
          <button className="btn-exit" onClick={() => navigate('/')}>Exit</button>
        </div>
      </header>

      <div className="progress-container">
        <div className="progress-info">
          <span>Question {questionIndex + 1} of {totalQuestions}</span>
          <span className="score-badge">Answered: {answeredIds.size}/{totalQuestions}</span>
        </div>
        <div className="progress-bar">
          <div className="progress-fill" style={{ width: `${progressPercentage}%` }}></div>
        </div>
      </div>

      <main className="interview-main">
        <div className="question-container">
          <div className="question-card">
            <div className="question-header">
              <h2 className="question-text">{currentQuestion.questionText}</h2>
              <div className="question-meta">
                <span className="badge topic-badge">{currentQuestion.topic}</span>
                <span className={`badge difficulty-badge ${currentQuestion.difficulty.toLowerCase()}`}>
                  {currentQuestion.difficulty}
                </span>
              </div>
            </div>

            <div className="answer-section">
              <div className="answer-section-header">
                <label htmlFor="answer">Your Answer:</label>
                {speechSupported && (
                  <button
                    type="button"
                    className={`btn-mic ${isListening ? 'listening' : ''}`}
                    onClick={() => (isListening ? stopListening() : startListening())}
                  >
                    {isListening ? '⏹ Stop Dictation' : '🎤 Speak Answer'}
                  </button>
                )}
              </div>
              <textarea
                id="answer"
                className="answer-textarea"
                value={answerText}
                onChange={(e) => setAnswerText(e.target.value)}
                placeholder="Enter your detailed answer here..."
                rows={8}
              />
              {isListening && interimTranscript && (
                <p className="interim-transcript">{interimTranscript}</p>
              )}
              <div className="input-hint">
                💡 Tip: Provide a detailed answer including examples, edge cases, and best practices.
              </div>
            </div>

            <div className="action-buttons">
              {questionIndex > 0 && (
                <button
                  className="btn-primary"
                  onClick={() => {
                    const answeredMap = new Map(session.answers.map((a) => [a.questionId, a.answerText]));
                    answeredMap.set(currentQuestion.id.toString(), answerText);
                    goToQuestion(questionIndex - 1, session.assignedQuestions, answeredMap);
                  }}
                  disabled={isSaving}
                >
                  ⬅️ Previous
                </button>
              )}
              <button className={isLastQuestion ? 'btn-next' : 'btn-submit'} onClick={handleSaveAndNext} disabled={isSaving}>
                {isSaving
                  ? 'Saving...'
                  : isLastQuestion
                    ? '🏁 Submit Interview'
                    : '➡️ Save & Next'}
              </button>
            </div>
          </div>

          <aside className="interview-sidebar">
            <div className="stats-box">
              <h3>📊 Progress</h3>
              <div className="stat-item">
                <span>Questions Answered:</span>
                <strong>{answeredIds.size}/{totalQuestions}</strong>
              </div>
              <div className="stat-item">
                <span>Time Elapsed:</span>
                <strong>{formatTime(timeElapsed)}</strong>
              </div>
            </div>

            <div className="tips-box">
              <h3>💡 Tips for Success</h3>
              <ul>
                <li>Answer comprehensively</li>
                <li>Include practical examples</li>
                <li>Mention edge cases</li>
                <li>Discuss trade-offs</li>
                <li>Be concise but detailed</li>
              </ul>
            </div>
          </aside>
        </div>
      </main>
    </div>
  );
}
