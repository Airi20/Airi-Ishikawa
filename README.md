# TrussMemberForceCalc

**Truss Member Force Calculation Tool** — An easy-to-use Java application that automatically computes and visualizes internal forces in 2D truss structures.

---

<p align="center">
  <img src="screenshot1.png" alt="TrussForce Screenshot 1" width="600"/>
  <br/>
  <img src="screenshot2.png" alt="TrussForce Screenshot 2" width="600"/>
</p>

---

## About This Project

This **Java-based truss analysis software** calculates internal forces such as **tension and compression** in 2D truss members. The app features an intuitive **Swing GUI** where users can input **node coordinates**, **member connections**, **support conditions** (including pin and roller supports), and **applied loads** in both X and Y directions.

It then **automatically computes reaction forces and member forces** using **equilibrium equations** solved by a custom-built solver with **Gaussian elimination**. The results are visualized with **color-coded truss diagrams**:

- 🔵 Blue indicates **compression** forces (negative)
- 🔴 Red indicates **tension** forces (positive)

---

## Features

- GUI for inputting:
  - Node coordinates and member connectivity
  - Support types (pin, roller with x/y constraints)
  - Loads applied at each node (X/Y directions)
- Automatic calculation of:
  - Reaction forces at supports
  - Internal forces of each truss member
- Visual representation of forces in the truss diagram

---

## Tech Stack

- Language: **Java**
- GUI Framework: **Swing**
- Solver: Custom linear equation solver (Gaussian elimination)

---

## How to Run

Make sure you have Java installed, then run:

```bash
java -jar TrussForceApp.jar

```
---
## 🌐 Language

- This application is primarily written in **Japanese**, including:
  - Source code comments
  - Graphical User Interface (GUI)

> Note: Japanese language proficiency is recommended to use or modify this software.

## Feedback / Questions 💬  
Have any suggestions, questions, or found a bug?  
[Click here to open an issue](https://github.com/Airi20/Truss-Force-Calc/issues/new?title=Feedback&body=Feel+free+to+write+your+thoughts+below%21+%F0%9F%91%87) — I’d love to hear from you!

 * TrussForce © 2025 Airi
 * All rights reserved.
 * Unauthorized use, copying, modification, or distribution of this code is strictly prohibited.
 */
