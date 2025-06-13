# Airi-Ishikawa
A Java-based app that calculates forces in 2D truss structures and visualizes them with color-coded diagrams. Built with Swing, powered by math. 2Dトラス構造の反力・部材力を自動計算し、構造図を可視化するJavaアプリケーションです。

**Note**: The UI and code comments are written in **Japanese**.  
日本語を前提としたソフトウェアです。ご了承ください。

<p align="center">
  <img src="screenshots/screenshot1.png" alt="TrussForce Screenshot 1" width="600"/>
  <br/>
  <img src="screenshots/screenshot2.png" alt="TrussForce Screenshot 2" width="600"/>
</p>


# TrussForce
**TrussForce** is a Java-based application that allows users to automatically compute the internal forces of 2D truss structures. It provides an intuitive UI for inputting node coordinates, member connections, and support conditions (pin, roller with x/y constraints), and visualizes the calculated truss diagram.

## 🧠 Features

- GUI-based input for:
  - Node coordinates
  - Member definitions
  - Support conditions (pin/roller supports with x/y constraints)
- Load application per node
- Automatic calculation of:
  - Reaction forces
  - Internal member forces
- Truss diagram visualization with color-coded members:
  - 🔵 **Blue** for compression (negative)
  - 🔴 **Red** for tension (positive)

## ⚙️ Tech Stack

- Language: **Java**
- GUI: **Swing**
- Math: Custom-built solver using equilibrium equations and Gaussian elimination

## ▶️ How to Run

Make sure you have Java installed. Then run:

```bash
java -jar TrussForce.jar

## 🌐 Language

- This application is primarily written in **Japanese**, including:
  - Source code comments
  - Graphical User Interface (GUI)

> Note: Japanese language proficiency is recommended to use or modify this software.

