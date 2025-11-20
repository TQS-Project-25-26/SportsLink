# SQE Tools & Practices – Initial Version

## 1. Introduction
This document defines the initial Software Quality Engineering (SQE) practices and tools to be adopted by the project.  
Its purpose is to establish a baseline for quality, testing strategy, coding standards, and automation practices.

---

## 2. Quality Goals
- Ensure high code reliability and maintainability.  
- Enable early detection of defects.  
- Promote consistent development standards.  
- Support continuous integration and automated validation.  
- Provide transparency of quality metrics across the team.

---

## 3. QA Tools (Initial Selection)

### 3.1 Testing Frameworks
- **Unit tests:** JUnit, Mockito
- **Integration tests:** RestAssured with PostgreSQL
- **Functional tests:** Cucumber(BDD) with Selenium WebDriver
- **Performance tests:** k6

### 3.2 Static Code Analysis
- **SonarQube Cloud**
  - Automated code quality scanning  
  - Security analysis  
  - Duplication detection  
  - Integrated into CI pipeline  
  - **Quality Gate (initial):** No new critical issues and code duplication < 5%


### 3.3 Test Management
- **Test case management:** XRay
- **Defect tracking:** Jira  

### 3.4 Coverage and Reporting
- **Code Coverage:** SonarQube Cloud
- **User Story Coverage:** XRay
- **Minimum code coverage target (initial):** 60%

---

## 4. Quality Practices

### 4.1 Definition of Done (Initial)
A user story is considered “Done” when:
- All acceptance criteria validated  
- Unit tests implemented and passing  
- Minimum code coverage target met 
- No critical static analysis findings
- Documentation updated (if required)

### 4.2 Pull Request Quality Checklist
- Unit tests added/updated  
- All automated checks pass  
- Peer review completed  
- Updated documentation (if applicable)

### 4.3 Test Strategy Overview
- **Unit tests (JUnit, Mockito):** Validate core logic and functions  
- **Integration tests (RestAssured with PostgreSQL):** Validate module interactions and API behaviour  
- **Functional tests (Cucumber BDD with Selenium WebDriver):** Validate user flows and full system interactions  
- **Performance tests (k6):** Validate system performance under load 

- **Regression tests:** Automated when possible (essencially uses the previous tests)
- **Exploratory tests:** Per sprint for new features (manual tests done to test multiple edge cases not considered in the automated tests) 

---

## 5. CI/CD Integration (Initial Overview)
- Trigger unit and integration tests on every push  
- Run full tests on merge requests
- Enforce minimum coverage threshold  
- Run static analysis on every commit  
- Publish test and coverage reports in the pipeline  
- Block merging if critical checks fail


