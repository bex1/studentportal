package it.bexekman.chalmers.studentPortal;

import java.sql.*; // JDBC stuff.
import java.io.*;  // Reading user input.

public class StudentPortal
{
	/* This is the driving engine of the program. It parses the
	 * command-line arguments and calls the appropriate methods in
	 * the other classes.
	 *
	 * You should edit this file in two ways:
	 * 	1) 	Insert your database username and password (no @medic1!)
	 *		in the proper places.
	 *	2)	Implement the three functions getInformation, registerStudent
	 *		and unregisterStudent.
	 */
	public static void main(String[] args)
	{
		if (args.length == 1) {
			try {
				DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
				String url = "jdbc:oracle:thin:@tycho.ita.chalmers.se:1521/kingu.ita.chalmers.se";
				String userName = "vtda357_004"; // Your username goes here!
				String password = "bexekman"; // Your password goes here!
				Connection conn = DriverManager.getConnection(url,userName,password);

				String student = args[0]; // This is the identifier for the student.
				BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("Welcome!");
				while(true) {
					System.out.println("Please choose a mode of operation:");
					System.out.print("? > ");
					String mode = input.readLine();
					if ((new String("information")).startsWith(mode.toLowerCase())) {
						/* Information mode */
						getInformation(conn, student);
					} else if ((new String("register")).startsWith(mode.toLowerCase())) {
						/* Register student mode */
						System.out.print("Register for what course? > ");
						String course = input.readLine();
						registerStudent(conn, student, course);
					} else if ((new String("unregister")).startsWith(mode.toLowerCase())) {
						/* Unregister student mode */
						System.out.print("Unregister from what course? > ");
						String course = input.readLine();
						unregisterStudent(conn, student, course);
					} else if ((new String("quit")).startsWith(mode.toLowerCase())) {
						System.out.println("Goodbye!");
						break;
					} else {
						System.out.println("Unknown argument, please choose either information, register, unregister or quit!");
						continue;
					}
				}
				conn.close();
			} catch (SQLException e) {
				System.err.println(e);
				System.exit(2);
			} catch (IOException e) {
				System.err.println(e);
				System.exit(2);
			}
		} else {
			System.err.println("Wrong number of arguments");
			System.exit(3);
		}
	}

	static void getInformation(Connection conn, String student)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Information for student " + student + 
				"\n---------------------------------");
		try {
			Statement infoStatement = conn.createStatement();
			ResultSet studentInfo = infoStatement.executeQuery("SELECT name,programme,branch " +
															   "FROM StudentsFollowing " +
															   "WHERE personalnbr = '" + student + "'");
			if (studentInfo.next()) {
				sb.append("Name: " + studentInfo.getString("name") + 
						"\nProgramme: " + studentInfo.getString("programme"));
				String branch = studentInfo.getString("branch");
				if (branch != null)
					sb.append("\nBranch: " + studentInfo.getString("branch"));
			}
			
			sb.append("\n\nRead courses (name (code), credits: grade):");
			ResultSet studentReadCourses = infoStatement.executeQuery("SELECT course, grade, name, credits " +
																	  "FROM FinishedCourses " +
																	  "WHERE student = '" + student + "'");
			while (studentReadCourses.next()) {
				sb.append("\n " + studentReadCourses.getString("name") + 
						  " (" + studentReadCourses.getString("course") + "), " + 
						  studentReadCourses.getInt("credits") + "p: " + 
						  studentReadCourses.getString("grade"));
			}
			
			sb.append("\n\nRegistered courses (name (code), credits: status):");
			ResultSet studentRegCourses = infoStatement.executeQuery("SELECT course, waitingStatus  " +
					  												 "FROM Registrations A, Courses B   " +
					  												 "WHERE student = '" + student + "'");
			

		} catch (SQLException e) {
			System.err.println(e);
			return;
		}
		System.out.println(sb.toString());
	}


	static void registerStudent(Connection conn, String student, String course)
	{
		// Your implementation here
	}

	static void unregisterStudent(Connection conn, String student, String course)
	{
		// Your implementation here
	}
}