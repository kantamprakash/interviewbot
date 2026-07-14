# AI Interview Portal

A technical interviewing platform where admins schedule interviews for
candidates by assigning questions from a question bank. Candidates log in,
answer only their assigned questions, and submit. Each answer is scored
automatically by an LLM on submission — scores and feedback are visible to
admins only; candidates just see a submission confirmation.

## Architecture

| Module | Stack | Port | Purpose |
|---|---|---|---|
| `portal-frontend` | React + TypeScript (`react-scripts`) | 3000 | Admin panel + candidate portal UI |
| `portal-backend` | Spring Boot, H2 (in-memory) | 8301 | Core API: users, questions, interviews, evaluations |
| `ai-service` | Spring Boot + Spring AI (Ollama) | 8302 | LLM-based answer evaluation |
| `llm-integration` | Java library | — | Shared LLM provider utilities |

`portal-backend` calls `ai-service` synchronously when a candidate submits an
answer; `ai-service` calls a local Ollama model to score and give feedback.

## Prerequisites

- Java 25 (for `portal-backend` and `llm-integration`)
- Java 21 (for `ai-service`)
- Node.js + npm (for `portal-frontend`)
- Maven
- [Ollama](https://ollama.com) running locally with a chat model pulled
  (defaults to `mistral-nemo`, override with the `OLLAMA_MODEL` env var)

## Running locally

Start Ollama first:

```bash
ollama serve
```

Then, in separate terminals:

```bash
# ai-service (Java 21)
cd ai-service
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn spring-boot:run

# portal-backend (Java 25)
cd portal-backend
JAVA_HOME=$(/usr/libexec/java_home -v 25) mvn spring-boot:run

# portal-frontend
cd portal-frontend
npm install
npm start
```

Open http://localhost:3000.

> If your Maven is configured behind a corporate proxy/mirror that fails to
> resolve public dependencies, pass `-s path/to/settings.xml` with a
> Maven Central mirror override to the `mvn` commands above.

## Demo credentials

| Role | Email | Password |
|---|---|---|
| Admin | `admin@interview.com` | `admin123` |
| Candidate | `user@interview.com` | `user123` |

## Workflow

1. **Admin** logs in, opens the *Interviews* tab, picks a candidate and one
   or more questions from the question bank, and schedules the interview.
2. **Candidate** logs in and sees only their assigned interview(s) under
   *My Interviews*.
3. Candidate answers each assigned question and submits. Every answer is
   evaluated by the LLM in `ai-service` immediately, but the score/feedback
   is never sent back to the candidate.
4. Once all questions are answered and the interview is submitted, an
   aggregate evaluation (average score + combined feedback) is computed.
5. **Admin** views the score and per-question feedback for any completed
   interview from the *Interviews* tab — this is the only place scores are
   ever shown.

## Notes

- Authentication is a lightweight header-based scheme
  (`X-User-Id` / `X-User-Role`) suitable for local development/demo use, not
  production-grade security — passwords are stored in plaintext in the H2
  database.
- The H2 database is in-memory (`ddl-auto: create-drop`), so all data resets
  on every `portal-backend` restart; the question bank is reseeded
  automatically via `DataInitializer`.
