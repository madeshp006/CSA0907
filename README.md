# CSA09 - Java Fundamentals & Object-Oriented Programming (CO1)
## Assignment 1: Campus Smart-Parking System

**Student Details:**
- **Name:** P. Madesh
- **Register Number:** 192365007
- **Course Code:** CSA09 (CO1)
- **Institution:** SIMATS Engineering (Department of Computer Science and Engineering)
- **Development Environment:** Java(TM) SE 25.0.2 LTS, Windows PowerShell
- **Submission Repository:** `CSA09-192365007-Assignment1`

---

## Repository Structure & Task Organization

```text
M:\JAVA\
│
├── ASSIGNMENT 1_Answers_P_Madesh_192365007.docx   <- Complete 36-page Word Report
├── ASSIGNMENT 1_Answers_P_Madesh_192365007.pdf    <- Formatted Evaluation PDF
├── README.md                                      <- Execution & Verification Guide
│
├── screenshots\                                   <- Execution Terminal Screenshots & UML Diagram
│   ├── task03_output.png
│   ├── task06_uml_diagram.png
│   ├── task08_output.png
│   ├── task11_output.png
│   └── task12_output.png
│
└── src\                                           <- Working Java Source Code
    ├── task03\
    │   └── ParkingFacility.java                   (Task 3: Jagged Array Allocation & Release)
    ├── task08\
    │   └── FeeDemo.java                           (Task 8: Inheritance, Overriding & Dynamic Dispatch)
    ├── task11\
    │   └── CampusParkingSystem.java               (Task 11: Complete Integrated System & Test Suite)
    └── task12\
        └── DesignChallengeDemo.java               (Task 12: Day-4 Challenge with E-Bikes & Vans)
```

---

## Compilation & Execution Instructions

Ensure JDK 21+ or JDK 25 is installed and configured in your `PATH`.

### 1. Task 3 – ParkingFacility (Complete the Code)
```powershell
javac m:\JAVA\src\task03\ParkingFacility.java
java -cp m:\JAVA\src\task03 ParkingFacility
```
**Expected Output:**
```text
[0, 0]
[0, 1]
[1, 0]
null
true false false
[0, 1]
```

### 2. Task 8 – FeeDemo (Polymorphism & Vehicle Hierarchy)
```powershell
javac m:\JAVA\src\task08\FeeDemo.java
java -cp m:\JAVA\src\task08 FeeDemo
```
**Expected Output:**
```text
TwoWheeler TN01A1 -> Rs 30.0
Car TN02B2 -> Rs 90.0
ElectricCar TN03C3 -> Rs 210.0
true
```

### 3. Task 11 – CampusParkingSystem (Complete System & Benchmarks)
```powershell
javac m:\JAVA\src\task11\CampusParkingSystem.java
java -cp m:\JAVA\src\task11 CampusParkingSystem
```
**Results:** Validates normal operations, boundary limits (full facility rejection, zero-minute stays, 60-min vs 61-min ceiling slab transitions), invalid duplicate plate rejections, and achieves exact match with the hand-calculated Rs 370.00 benchmark.

### 4. Task 12 – DesignChallengeDemo (Day-4 Design Challenge)
```powershell
javac m:\JAVA\src\task12\DesignChallengeDemo.java
java -cp m:\JAVA\src\task12 DesignChallengeDemo
```
**Results:** Validates asymmetric 5-floor facility (280 slots), E-Bikes (flat Rs 15 + charging), Delivery Vans (2x car rate peak pricing 9 AM - 5 PM), and One-Day Visitor Pass (Rs 150 prepaid).

---

## Academic Integrity
Submitted in accordance with SIMATS Engineering Academic Regulations. All programs compiled, executed, and tested locally by **P. Madesh (192365007)**.
