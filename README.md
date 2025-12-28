# Sorting-Visualization
Sorting Algorithm Visualizer in Java (Swing)

NeonSortStudio is a desktop-based sorting algorithm visualizer built using Java Swing.
It helps students understand how different sorting algorithms work through real-time animations, color indicators, pointers, and live statistics.

The project focuses on clarity, interaction, and visual learning rather than just showing final sorted output.

Features

Visualizes multiple sorting algorithms step by step

Smooth animated movement using simple physics

Real-time comparison and swap counters

Time complexity display for each algorithm

Pause and resume sorting at any time

Adjustable speed control

Two view modes

Node view (circular nodes)

Bar view (histogram style)

Manual input or random array generation

Clean dark themed user interface

Algorithms Implemented

Merge Sort

Quick Sort

Insertion Sort

Selection Sort

Bubble Sort

Each algorithm shows:

Active comparisons

Swaps

Current pointers (i, j, pivot, key)

Final sorted state highlighted clearly

Technologies Used

Java

Java Swing (GUI)

AWT Graphics2D

Multithreading (for sorting without freezing UI)

How It Works (High Level)

Each number is represented as a VisualNode

Nodes move smoothly toward target positions using simple interpolation

Sorting runs in a separate thread to keep the UI responsive

Pause and resume is handled using a shared lock

Comparisons and swaps are counted live and updated on screen

Different colors represent different states:

Default

Comparing

Pivot or key

Sorted

How to Run the Project

Clone the repository

git clone https://github.com/your-username/NeonSortStudio.git


Open the project in any Java IDE
(IntelliJ IDEA, Eclipse, NetBeans)

Make sure JDK 8 or higher is installed

Run the file:

NeonSortStudio.java

Controls Guide

Algorithm Selector
Choose the sorting algorithm

Speed Slider
Control animation delay

Play
Start sorting

Pause / Resume
Pause or continue the algorithm

Reset
Reset colors and counters

Input Field
Add custom values (comma separated)

Randomize
Generate random array

View Toggle
Switch between Nodes and Bars view

Educational Purpose

This project is designed for:

Computer Science students

Data Structures and Algorithms learning

Visual understanding of sorting behavior

Demonstrating multithreading and GUI concepts in Java

It is especially useful for beginners who struggle to understand sorting logic through code alone.

Project Structure (Main Classes)

NeonSortStudio
Main application window and controller

VisualNode
Represents each array element visually

CanvasPanel
Responsible for drawing animations and pointers

Screenshots (Optional but Recommended)

You should add screenshots here for GitHub:

Intro Screen

Sorting in Node View

Sorting in Bar View

Merge Sort recursion visualization

Example:

![Sorting Visualization](screenshots/sort.png)

Future Improvements

Add Heap Sort and Radix Sort

Step-by-step manual mode

Sound effects for swaps and comparisons

Export sorting steps as data

Better mobile scaling

Author

Saifullah Khan Jadoon
Computer Science Student
Sorting Algorithm Visualizer Project

License

This project is open for educational use.
You may modify and reuse it with proper credit.
