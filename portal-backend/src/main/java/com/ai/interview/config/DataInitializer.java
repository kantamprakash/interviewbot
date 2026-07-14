package com.ai.interview.config;

import com.ai.interview.entity.Question;
import com.ai.interview.entity.User;
import com.ai.interview.repository.QuestionRepository;
import com.ai.interview.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Bean
    public ApplicationRunner initializer() {
        return args -> {
            // Create admin user
            if (userRepository.findByEmail("admin@interview.com").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@interview.com");
                admin.setName("Admin User");
                admin.setRole("ADMIN");
                admin.setPassword("admin123"); // In production, use bcrypt
                admin.setIsActive(true);
                userRepository.save(admin);
            }

            // Create demo candidate user
            if (userRepository.findByEmail("user@interview.com").isEmpty()) {
                User demoUser = new User();
                demoUser.setEmail("user@interview.com");
                demoUser.setName("Interview User");
                demoUser.setRole("CANDIDATE");
                demoUser.setPassword("user123"); // In production, use bcrypt
                demoUser.setIsActive(true);
                userRepository.save(demoUser);
            }

            // Seed questions if table is empty
            if (questionRepository.count() == 0) {
                List<Question> questions = Arrays.asList(
                        // JavaScript - 10 questions
                        createQuestion("Explain the difference between var, let, and const in JavaScript.", "JavaScript", "EASY", "Scope, hoisting, reassignment", 1L),
                        createQuestion("What is hoisting in JavaScript?", "JavaScript", "EASY", "Variable and function hoisting, TDZ", 1L),
                        createQuestion("Explain closures in JavaScript with an example.", "JavaScript", "EASY", "Function scope, lexical environment", 1L),
                        createQuestion("What is the event loop and how does it work?", "JavaScript", "MEDIUM", "Call stack, callback queue, event loop", 1L),
                        createQuestion("Explain prototypal inheritance in JavaScript.", "JavaScript", "MEDIUM", "Prototype chain, Object.create()", 1L),
                        createQuestion("What are Promises and how do they differ from async/await?", "JavaScript", "MEDIUM", "Promise states, async/await syntax", 1L),
                        createQuestion("Implement a debounce function and explain its use cases.", "JavaScript", "HARD", "Function debouncing, timing, implementation", 1L),
                        createQuestion("Explain how 'this' binding works in different contexts.", "JavaScript", "HARD", "Context binding, call, apply, bind", 1L),
                        createQuestion("What are WeakMap and WeakSet? When would you use them?", "JavaScript", "HARD", "Memory management, weak references", 1L),
                        createQuestion("Explain call, apply, and bind methods.", "JavaScript", "MEDIUM", "Context, arguments, method binding", 1L),
                        createQuestion("What is the difference between == and === in JavaScript?", "JavaScript", "EASY", "Type coercion, strict equality", 1L),
                        createQuestion("Explain the difference between null and undefined.", "JavaScript", "EASY", "Type checking, default values", 1L),
                        createQuestion("Explain the difference between synchronous and asynchronous JavaScript.", "JavaScript", "MEDIUM", "Blocking vs non-blocking, callbacks", 1L),
                        createQuestion("What is the difference between map, filter, and reduce?", "JavaScript", "MEDIUM", "Array methods, use cases", 1L),
                        createQuestion("Explain event delegation and its benefits.", "JavaScript", "MEDIUM", "Event bubbling, performance", 1L),
                        createQuestion("What is destructuring and how is it used with objects and arrays?", "JavaScript", "MEDIUM", "Object/array destructuring, defaults", 1L),
                        createQuestion("Explain the difference between shallow copy and deep copy.", "JavaScript", "MEDIUM", "Object.assign, spread, structuredClone", 1L),
                        createQuestion("What are template literals and tagged templates?", "JavaScript", "MEDIUM", "String interpolation, tagged functions", 1L),
                        // React - 10 questions
                        createQuestion("Explain the component lifecycle in React.", "React", "EASY", "Mounting, updating, unmounting phases", 1L),
                        createQuestion("What is the difference between state and props?", "React", "EASY", "Mutability, ownership, usage", 1L),
                        createQuestion("When should you use useEffect and how does dependency array work?", "React", "MEDIUM", "Side effects, dependencies, cleanup", 1L),
                        createQuestion("Explain React hooks and create a custom hook example.", "React", "MEDIUM", "Hooks rules, custom hooks", 1L),
                        createQuestion("What are render props and how do they differ from HOC?", "React", "HARD", "Pattern comparison, use cases", 1L),
                        createQuestion("Explain concurrent rendering and Suspense in React 18.", "React", "HARD", "Concurrent features, Suspense", 1L),
                        createQuestion("How does React.memo improve performance?", "React", "MEDIUM", "Memoization, re-renders", 1L),
                        createQuestion("Explain controlled vs uncontrolled components.", "React", "MEDIUM", "Form handling patterns", 1L),
                        createQuestion("What is context API and when should you use it?", "React", "MEDIUM", "State management, prop drilling", 1L),
                        createQuestion("Design a state management solution for a complex React app.", "React", "HARD", "Redux, Zustand, Context", 1L),
                        createQuestion("What is JSX and how does it differ from HTML?", "React", "EASY", "Syntax extension, expressions, compilation", 1L),
                        createQuestion("What is the virtual DOM and why does React use it?", "React", "EASY", "Diffing, reconciliation, performance", 1L),
                        createQuestion("How do you pass data from a child component to a parent?", "React", "EASY", "Callback props, lifting state up", 1L),
                        createQuestion("Explain the useReducer hook and when to prefer it over useState.", "React", "MEDIUM", "Complex state logic, reducer pattern", 1L),
                        createQuestion("What is prop drilling and how can it be avoided?", "React", "MEDIUM", "Context API, composition", 1L),
                        createQuestion("Explain the key prop in lists and why it matters for reconciliation.", "React", "MEDIUM", "List rendering, reconciliation, keys", 1L),
                        createQuestion("What is the difference between useMemo and useCallback?", "React", "MEDIUM", "Memoization, referential equality", 1L),
                        createQuestion("Explain error boundaries in React.", "React", "MEDIUM", "componentDidCatch, fallback UI", 1L),
                        // System Design - 10 questions
                        createQuestion("Design a URL shortening service like bit.ly.", "System Design", "HARD", "Database, API, scalability", 1L),
                        createQuestion("Design a real-time chat application.", "System Design", "HARD", "WebSocket, message queue, scaling", 1L),
                        createQuestion("Design a social media feed system.", "System Design", "HARD", "Feed algorithm, caching, distribution", 1L),
                        createQuestion("Design a distributed cache system.", "System Design", "HARD", "Consistency, eviction, replication", 1L),
                        createQuestion("Design an e-commerce system handling millions of transactions.", "System Design", "HARD", "Payment, inventory, scalability", 1L),
                        createQuestion("Design a video streaming service like YouTube.", "System Design", "HARD", "Encoding, CDN, storage", 1L),
                        createQuestion("How would you design a notification system?", "System Design", "MEDIUM", "Queue, delivery, reliability", 1L),
                        createQuestion("Design a file storage system like Google Drive.", "System Design", "HARD", "Storage, sync, sharing", 1L),
                        createQuestion("Design a search engine indexing system.", "System Design", "HARD", "Crawling, indexing, ranking", 1L),
                        createQuestion("Design a real-time collaborative editing tool.", "System Design", "HARD", "Conflict resolution, OT/CRDT", 1L),
                        createQuestion("What is the difference between vertical and horizontal scaling?", "System Design", "EASY", "Scale up vs scale out", 1L),
                        createQuestion("What is a load balancer and why is it used?", "System Design", "EASY", "Traffic distribution, availability", 1L),
                        createQuestion("Explain the client-server architecture model.", "System Design", "EASY", "Request/response, separation of concerns", 1L),
                        createQuestion("What is caching and why is it useful in system design?", "System Design", "EASY", "Latency reduction, cache hit/miss", 1L),
                        createQuestion("What is the difference between latency and throughput?", "System Design", "EASY", "Response time vs capacity", 1L),
                        createQuestion("Explain the CAP theorem and its trade-offs.", "System Design", "MEDIUM", "Consistency, availability, partition tolerance", 1L),
                        createQuestion("What is a message queue and when would you use one?", "System Design", "MEDIUM", "Decoupling, async processing, reliability", 1L),
                        createQuestion("Design a rate limiter for an API.", "System Design", "MEDIUM", "Token bucket, sliding window", 1L),
                        createQuestion("Explain database replication and its purpose.", "System Design", "MEDIUM", "Master-replica, read scaling, failover", 1L),
                        createQuestion("What is a CDN and how does it improve performance?", "System Design", "MEDIUM", "Edge caching, latency reduction", 1L),
                        createQuestion("Explain the difference between synchronous and asynchronous communication between services.", "System Design", "MEDIUM", "Blocking calls, event-driven", 1L),
                        createQuestion("How would you design an API gateway?", "System Design", "MEDIUM", "Routing, auth, rate limiting", 1L),
                        createQuestion("Explain consistent hashing and why it is used in distributed systems.", "System Design", "MEDIUM", "Hash ring, minimal rebalancing", 1L),
                        createQuestion("Design a health-check and monitoring approach for a distributed system.", "System Design", "MEDIUM", "Heartbeats, alerting, observability", 1L),
                        // Database - 10 questions
                        createQuestion("Explain ACID properties and their importance.", "Database", "MEDIUM", "Atomicity, Consistency, Isolation, Durability", 1L),
                        createQuestion("What is database normalization and why is it important?", "Database", "MEDIUM", "1NF, 2NF, 3NF, redundancy", 1L),
                        createQuestion("Explain different types of joins in SQL.", "Database", "MEDIUM", "INNER, LEFT, RIGHT, FULL joins", 1L),
                        createQuestion("What are indexes and how do they impact performance?", "Database", "MEDIUM", "B-tree, hash indexes, trade-offs", 1L),
                        createQuestion("Explain the difference between SQL and NoSQL databases.", "Database", "MEDIUM", "ACID vs BASE, schema, use cases", 1L),
                        createQuestion("What is sharding and how does it scale databases?", "Database", "HARD", "Sharding strategy, consistency", 1L),
                        createQuestion("Explain query optimization techniques.", "Database", "HARD", "Indexing, execution plans, profiling", 1L),
                        createQuestion("Design a database schema for a complex application.", "Database", "HARD", "Normalization, relationships", 1L),
                        createQuestion("What is eventual consistency?", "Database", "MEDIUM", "CAP theorem, distributed systems", 1L),
                        createQuestion("Explain transactions and isolation levels.", "Database", "HARD", "ACID, READ UNCOMMITTED, SERIALIZABLE", 1L),
                        createQuestion("What is a primary key and why is it important?", "Database", "EASY", "Uniqueness, identification", 1L),
                        createQuestion("What is a foreign key and what does it enforce?", "Database", "EASY", "Referential integrity, relationships", 1L),
                        createQuestion("What is the difference between DELETE, TRUNCATE, and DROP?", "Database", "EASY", "DDL vs DML, rollback behavior", 1L),
                        createQuestion("What is a database schema?", "Database", "EASY", "Tables, columns, relationships", 1L),
                        createQuestion("What is the difference between a clustered and non-clustered index?", "Database", "EASY", "Storage order, lookup performance", 1L),
                        createQuestion("Explain the difference between OLTP and OLAP systems.", "Database", "MEDIUM", "Transaction processing vs analytics", 1L),
                        createQuestion("What is a stored procedure and when would you use one?", "Database", "MEDIUM", "Precompiled logic, performance, reuse", 1L),
                        createQuestion("Explain optimistic vs pessimistic locking.", "Database", "MEDIUM", "Concurrency control strategies", 1L),
                        createQuestion("What is connection pooling and why is it important?", "Database", "MEDIUM", "Resource reuse, performance", 1L),
                        // Backend - 10 questions
                        createQuestion("Explain the Node.js event-driven architecture.", "Backend", "MEDIUM", "Event emitter, non-blocking I/O", 1L),
                        createQuestion("What is middleware in Express.js?", "Backend", "EASY", "Request processing, chain", 1L),
                        createQuestion("How do you handle errors in Node.js applications?", "Backend", "MEDIUM", "Try-catch, error handlers, logging", 1L),
                        createQuestion("Explain clustering and multi-threading in Node.js.", "Backend", "HARD", "Worker threads, clustering", 1L),
                        createQuestion("Design a RESTful API architecture.", "Backend", "MEDIUM", "HTTP methods, status codes, design patterns", 1L),
                        createQuestion("How do you optimize Node.js performance?", "Backend", "HARD", "Profiling, caching, async patterns", 1L),
                        createQuestion("Explain JWT authentication and best practices.", "Backend", "MEDIUM", "Token structure, security", 1L),
                        createQuestion("Design a microservices architecture.", "Backend", "HARD", "Service discovery, communication", 1L),
                        createQuestion("What are streams in Node.js and when to use them?", "Backend", "MEDIUM", "Stream types, backpressure", 1L),
                        createQuestion("Explain rate limiting and how to implement it.", "Backend", "MEDIUM", "Algorithms, token bucket", 1L),
                        createQuestion("What is the difference between GET and POST HTTP methods?", "Backend", "EASY", "Idempotency, request body, caching", 1L),
                        createQuestion("What are HTTP status codes and what do the main categories mean?", "Backend", "EASY", "2xx, 3xx, 4xx, 5xx", 1L),
                        createQuestion("What is an API and what is a RESTful API?", "Backend", "EASY", "Resources, HTTP verbs, statelessness", 1L),
                        createQuestion("What is the purpose of environment variables in backend applications?", "Backend", "EASY", "Configuration, secrets, portability", 1L),
                        createQuestion("Explain the difference between authentication and authorization.", "Backend", "MEDIUM", "Identity verification vs access control", 1L),
                        createQuestion("What is API versioning and why is it necessary?", "Backend", "MEDIUM", "Backward compatibility, strategies", 1L),
                        createQuestion("Explain idempotency in API design and why it matters.", "Backend", "MEDIUM", "Safe retries, PUT vs POST", 1L),
                        createQuestion("What is dependency injection and why is it useful?", "Backend", "MEDIUM", "Inversion of control, testability", 1L),
                        // Cloud & DevOps - 10 questions
                        createQuestion("What is containerization and how does Docker work?", "Cloud", "MEDIUM", "Images, containers, registry", 1L),
                        createQuestion("Explain Kubernetes and container orchestration.", "Cloud", "HARD", "Pods, services, deployments", 1L),
                        createQuestion("Design a CI/CD pipeline.", "Cloud", "HARD", "Build, test, deploy automation", 1L),
                        createQuestion("What is infrastructure as code?", "Cloud", "MEDIUM", "Terraform, CloudFormation", 1L),
                        createQuestion("Explain load balancing strategies.", "Cloud", "MEDIUM", "Round-robin, least connection", 1L),
                        createQuestion("Design a disaster recovery strategy.", "Cloud", "HARD", "Backup, replication, failover", 1L),
                        createQuestion("What is auto-scaling and how does it work?", "Cloud", "MEDIUM", "Metrics, policies, orchestration", 1L),
                        createQuestion("Explain monitoring and logging best practices.", "Cloud", "MEDIUM", "Metrics, logs, alerts", 1L),
                        createQuestion("Design a multi-region deployment strategy.", "Cloud", "HARD", "Data replication, latency", 1L),
                        createQuestion("What is serverless architecture?", "Cloud", "MEDIUM", "Functions, benefits, trade-offs", 1L),
                        createQuestion("What is the difference between IaaS, PaaS, and SaaS?", "Cloud", "EASY", "Service models, responsibility boundaries", 1L),
                        createQuestion("What is a virtual machine and how does it differ from a container?", "Cloud", "EASY", "Hypervisor, OS-level virtualization", 1L),
                        createQuestion("What is a cloud region and availability zone?", "Cloud", "EASY", "Geographic distribution, fault isolation", 1L),
                        createQuestion("What is object storage and how does it differ from block storage?", "Cloud", "EASY", "S3-style storage, use cases", 1L),
                        createQuestion("What is a content delivery network (CDN)?", "Cloud", "EASY", "Edge locations, caching", 1L),
                        createQuestion("Explain blue-green deployment and canary releases.", "Cloud", "MEDIUM", "Deployment strategies, rollback", 1L),
                        createQuestion("What is a service mesh and what problems does it solve?", "Cloud", "MEDIUM", "Sidecar proxies, traffic management", 1L),
                        createQuestion("Explain the difference between horizontal pod autoscaling and cluster autoscaling in Kubernetes.", "Cloud", "MEDIUM", "Pod scaling vs node scaling", 1L),
                        createQuestion("What is infrastructure drift and how do you prevent it?", "Cloud", "MEDIUM", "IaC state management, reconciliation", 1L),
                        // Security - 10 questions
                        createQuestion("Explain OWASP Top 10 vulnerabilities.", "Security", "HARD", "Injection, XSS, CSRF, auth", 1L),
                        createQuestion("What is SQL injection and how to prevent it?", "Security", "MEDIUM", "Parameterized queries, validation", 1L),
                        createQuestion("Explain cross-site scripting (XSS) attacks.", "Security", "MEDIUM", "Types, prevention, CSP", 1L),
                        createQuestion("What is CSRF and how to mitigate it?", "Security", "MEDIUM", "Tokens, SameSite, validation", 1L),
                        createQuestion("Explain OAuth2 and OpenID Connect.", "Security", "HARD", "Authorization flows, use cases", 1L),
                        createQuestion("Design a secure authentication system.", "Security", "HARD", "Hashing, salting, MFA", 1L),
                        createQuestion("What is encryption and different types?", "Security", "MEDIUM", "Symmetric, asymmetric, hashing", 1L),
                        createQuestion("Explain secure coding practices.", "Security", "MEDIUM", "Input validation, error handling", 1L),
                        createQuestion("What is penetration testing?", "Security", "MEDIUM", "Methods, tools, reporting", 1L),
                        createQuestion("Design a secure data pipeline.", "Security", "HARD", "Encryption, access control, audit", 1L),
                        createQuestion("What is the difference between hashing and encryption?", "Security", "EASY", "One-way vs reversible, use cases", 1L),
                        createQuestion("What is two-factor authentication (2FA)?", "Security", "EASY", "Something you know/have/are", 1L),
                        createQuestion("What is HTTPS and why is it important?", "Security", "EASY", "TLS, encryption in transit", 1L),
                        createQuestion("What is the principle of least privilege?", "Security", "EASY", "Minimal access, risk reduction", 1L),
                        createQuestion("What is a firewall and what does it protect against?", "Security", "EASY", "Network filtering, access control", 1L),
                        createQuestion("Explain the difference between symmetric and asymmetric encryption.", "Security", "MEDIUM", "Key management, use cases", 1L),
                        createQuestion("What is a security token (JWT) and what are its components?", "Security", "MEDIUM", "Header, payload, signature", 1L),
                        createQuestion("Explain man-in-the-middle attacks and how to prevent them.", "Security", "MEDIUM", "TLS, certificate pinning", 1L),
                        createQuestion("What is security by design and why is it important?", "Security", "MEDIUM", "Shift-left security, threat modeling", 1L)
                );

                questionRepository.saveAll(questions);
            }
        };
    }

    private Question createQuestion(String text, String topic, String difficulty, String keyPoints, Long userId) {
        Question q = new Question();
        q.setQuestionText(text);
        q.setTopic(topic);
        q.setDifficulty(difficulty);
        q.setExpectedKeyPoints(keyPoints);
        q.setCreatedByUserId(userId);
        q.setIsActive(true);
        return q;
    }
}
