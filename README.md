PayPoint - JavaFX Cash Register System

Overview

PayPoint is a desktop-based cash register application built using JavaFX. It features a user authentication system, a ribbon tab interface, and MySQL database integration via XAMPP. The application is designed to assist businesses in managing transactions efficiently while ensuring secure user access.

Features

User Authentication:
Employees can log in to access the system.
Admin users have special privileges to add new users.
A secret admin login button is available for administrative access.

Transaction Management:
Calculates total amounts and change.

Database Integration:
Uses MySQL for secure and structured data storage.
XAMPP is required to host the database locally.

AI Assistant:
Uses locally hosted AI to assist in customer service.

System Requirements
Windows OS
Java 11 or later (with JavaFX SDK installed)

NetBeans 8.1 or later
XAMPP (for MySQL database management)
Installation & Setup

1. Clone the Repository
 git clone https://github.com/CXV75/paypoint.git
 cd paypoint

2. Set Up MySQL Database
Open XAMPP and start MySQL.
Create a database named paypoint_db.

Import the SQL file (if provided) into the database.

3. Install and Run Ollama, then, install the model "llama3.2:1b"

4. Configure the Database in Java
Modify the database connection settings in DatabaseConnector.java:
private static final String URL = "jdbc:mysql://localhost:3306/paypoint_db";
private static final String USER = "root";
private static final String PASSWORD = ""; // Default password is empty for XAMPP

5. Run the Project in NetBeans
Open NetBeans and load the project.
Ensure JavaFX is properly configured.
Click Run to start the application.