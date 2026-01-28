<h1 align="center">🏥 Birzeit University Insurance System – Backend</h1>

<p align="center">
  <strong>Health Insurance Management Backend Platform</strong>
</p>

<hr>

<img width="1918" height="1028" alt="Backend Architecture Overview"
     src="https://github.com/user-attachments/assets/REPLACE_ME" />

<h2>📌 Project Overview</h2>
<p>
  <strong>Birzeit University Insurance System – Backend</strong> is a secure,
  scalable, and enterprise-grade <strong>RESTful backend platform</strong>
  designed to manage health insurance operations for the Birzeit University community.
</p>

<p>
  The backend represents the <strong>core business and data layer</strong> of the system.
  It is responsible for enforcing business rules, handling insurance workflows,
  managing medical data, and ensuring secure access to sensitive information.
</p>

<p>
  This system was developed as a <strong>Graduation Project</strong> for the
  <strong>Department of Computer Science</strong> at Birzeit University and follows
  professional software engineering and healthcare data management standards.
</p>

<hr>

<h2>🎯 Backend Objectives</h2>
<ul>
  <li>Centralize all health insurance business logic</li>
  <li>Provide secure and role-based access to insurance data</li>
  <li>Automate insurance claims and emergency workflows</li>
  <li>Ensure data integrity and transactional consistency</li>
  <li>Expose reliable REST APIs for system clients</li>
</ul>

<hr>

<h2>👥 System Stakeholders (Backend Perspective)</h2>
<p>
  The backend serves multiple stakeholders by enforcing permissions,
  workflows, and data access rules.
</p>

<h3>🗂️ Coordination Admin</h3>
<ul>
  <li>Oversees cross-department insurance operations</li>
  <li>Monitors system activity and workflow consistency</li>
</ul>

<h3>🏥 Medical Admin</h3>
<ul>
  <li>Manages medical entities and records</li>
  <li>Supervises doctors, pharmacies, and radiology units</li>
</ul>

<h3>💼 Insurance Manager</h3>
<ul>
  <li>Creates and manages insurance policies</li>
  <li>Reviews and approves insurance claims</li>
  <li>Handles emergency request decisions</li>
</ul>

<h3>👤 Customer</h3>
<ul>
  <li>Submits insurance claims and emergency requests</li>
  <li>Accesses personal insurance and medical data</li>
</ul>

<h3>👨‍⚕️ Doctor</h3>
<ul>
  <li>Creates medical reports and diagnoses</li>
  <li>Submits prescriptions and test requests</li>
</ul>

<h3>💊 Pharmacy</h3>
<ul>
  <li>Validates prescriptions</li>
  <li>Confirms insurance coverage before dispensing medication</li>
</ul>

<h3>🧪 Radiology</h3>
<ul>
  <li>Uploads imaging results and reports</li>
  <li>Links results to patient medical records</li>
</ul>

<hr>

<h2>🔄 Core Backend Workflows</h2>

<h3>🧾 Insurance Claims Workflow</h3>
<pre>
submitted → reviewed → approved / rejected → reimbursed → closed
</pre>

<h3>🚨 Emergency Requests Workflow</h3>
<pre>
created → evaluated → escalated → approved → closed
</pre>

<p>
  All workflows are strictly enforced at the backend level,
  ensuring auditability and traceability.
</p>

<hr>

<h2>🏗️ Backend Architecture</h2>
<p>
  The backend follows a <strong>layered architecture</strong> that ensures
  separation of concerns and maintainability.
</p>

<pre>
Controller Layer → Service Layer → Repository Layer → PostgreSQL Database
</pre>

<ul>
  <li><strong>Controller Layer:</strong> REST API endpoints and request handling</li>
  <li><strong>Service Layer:</strong> Business logic and workflow enforcement</li>
  <li><strong>Repository Layer:</strong> Data persistence using JPA/Hibernate</li>
</ul>

<hr>

<h2>🗄️ Database Design</h2>
<p>
  The system uses <strong>PostgreSQL</strong> as a relational database to ensure
  strong data consistency and transactional reliability.
</p>

<ul>
  <li>Normalized relational schema</li>
  <li>ACID-compliant transactions</li>
  <li>Foreign key constraints and indexing</li>
  <li>Audit-friendly data structures</li>
</ul>

<hr>

<h2>🧰 Technologies Used</h2>

<h3>Backend Stack</h3>
<ul>
  <li>Java 17</li>
  <li>Spring Boot</li>
  <li>Spring Security</li>
  <li>Spring Data JPA</li>
  <li>Hibernate</li>
</ul>

<h3>Database</h3>
<ul>
  <li>PostgreSQL</li>
</ul>

<hr>

<h2>🗂️ Project Structure</h2>
<pre>
insurance-system-backend/
│
├── src/main/java/
│   └── com/insurance/system/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── model/
│       ├── security/
│       ├── config/
│       └── InsuranceApplication.java
│
├── src/main/resources/
│   ├── application.yml
│   └── application.properties
│
├── pom.xml
└── README.md
</pre>

<hr>

<h2>🚀 Running the Backend</h2>

<h3>Prerequisites</h3>
<ul>
  <li>Java 17+</li>
  <li>PostgreSQL</li>
  <li>Maven</li>
</ul>

<h3>Steps</h3>
<pre>
git clone <backend-repository-url>
cd insurance-system-backend
mvn clean install
mvn spring-boot:run
</pre>

<p>
  The backend runs on:
  <strong>http://localhost:8080</strong>
</p>

<hr>

<h2>📘 API Documentation</h2>
<p>
  API documentation is automatically generated and available at:
</p>

<pre>
http://localhost:8080/swagger-ui.html
</pre>

<hr>

<h2>🔐 Security & Data Protection</h2>
<ul>
  <li>JWT-based authentication</li>
  <li>Role-based access control</li>
  <li>Encrypted credentials</li>
  <li>Validated and sanitized inputs</li>
</ul>

<hr>

<h2>🔮 Future Backend Enhancements</h2>
<ul>
  <li>Advanced reporting and analytics</li>
  <li>Microservices-based refactoring</li>
  <li>External insurance provider integrations</li>
  <li>Audit logging and monitoring</li>
</ul>

<hr>

<h2>📚 Academic Information</h2>
<ul>
  <li><strong>Project Type:</strong> Graduation Project</li>
  <li><strong>Institution:</strong> Birzeit University</li>
  <li><strong>Department:</strong> Computer Science</li>
</ul>

<hr>

<p align="center">
  🏥 <strong>Birzeit University Insurance System – Backend</strong><br>
  A secure and scalable backend platform for healthcare insurance management.
</p>
