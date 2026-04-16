# Fraud Detection Engine — Product Requirements

## Executive Summary
Build a banking transaction system with real-time fraud detection using custom data structures, PostgreSQL, and JDBC. Each phase produces working, runnable code.

## Success Criteria
- ✅ All 7 PostgreSQL tables created and normalized
- ✅ Custom data structures (Graph, MinHeap, Deque, Stack) built from scratch
- ✅ Role-based UI (Customer, Analyst, Admin menus)
- ✅ Fraud detection pipeline with 4 rules
- ✅ ACID transactions with automatic rollback + freeze
- ✅ Clean git history, CV-ready

## Technical Constraints
- **Language**: Java 17+
- **Database**: PostgreSQL 15+
- **ORM**: NONE — raw JDBC only
- **Build Tool**: Maven
- **Custom DS**: No java.util for Graph, Heap, Deque, Stack

## Phase 0 Tasks
1. Create Maven project structure ← START HERE
2. Create pom.xml with PostgreSQL JDBC
3. Design 7 tables in PostgreSQL
4. Create views and triggers
5. Verify all tables in pgAdmin
6. Write DBConnection.java
7. Test JDBC connection

## Success Verification (Phase 0)
```bash
mvn clean compile      # No errors
psql -d fraud_engine -c "\dt"  # Shows 7 tables
```
