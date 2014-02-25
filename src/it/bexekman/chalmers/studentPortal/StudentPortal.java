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

    static void getInformation(Connection conn, String studentID)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Information for student " + studentID + 
                "\n---------------------------------");
        try {
            Statement infoStatement = conn.createStatement();
            
            appendGeneralStudentInfo(sb, infoStatement, studentID);
            appendReadCoursesInfo(sb, infoStatement, studentID);
            appendRegisteredCoursesInfo(sb, infoStatement, studentID);
            appendGraduationInfo(sb, infoStatement, studentID);
        } 
        catch (SQLException e) {
            System.err.println(e);
            System.out.println("Error getting information for student: " + studentID);
            return;
        }
        sb.append("\n---------------------------------\n\n");
        System.out.println(sb.toString());
    }
    
    static void registerStudent(Connection conn, String studentID, String courseCode)
    {
        // Your implementation here
    }

    static void unregisterStudent(Connection conn, String studentID, String courseCode)
    {
        try {
            Statement statement = conn.createStatement();
            String courseName = getCourseNameByCode(statement, courseCode);
            if (courseName != null) {
                
                if (deleteRegistration(statement, studentID, courseCode)) {
                    System.out.println("You were successfully unregistered from course: " + courseCode + " " + courseName + "!");
                } else {
                    System.out.println("You were never registered for course: " + courseCode + " " + courseName + ", hence you could not be unregistered.");
                }
                
            } else {
                System.out.println("There is no course with coursecode: " + courseCode + ".");
            }
        } catch (SQLException e) {
            System.err.println(e);
            System.out.println("Error when unregistering student: " + studentID + " from course: " + courseCode);
        }
    }


    private static void appendGeneralStudentInfo(StringBuilder sb, Statement infoStatement, String studentID) throws SQLException 
    {
        ResultSet studentInfo = infoStatement.executeQuery("SELECT name,programme,branch " 
                + "FROM StudentsFollowing " 
                + "WHERE personalnbr = '" + studentID + "'");
        
        if (studentInfo.next()) {
            sb.append("Name: " + studentInfo.getString("name") + 
                    "\nProgramme: " + studentInfo.getString("programme"));
            String branch = studentInfo.getString("branch");
            if (branch != null)
                sb.append("\nBranch: " + studentInfo.getString("branch"));
        }
    }

    private static void appendReadCoursesInfo(StringBuilder sb, Statement infoStatement, String studentID) throws SQLException 
    {
        sb.append("\n\nRead courses (name (code), credits: grade):");
        
        ResultSet studentReadCourses = infoStatement.executeQuery("SELECT course, grade, name, credits "
                + "FROM FinishedCourses "
                + "WHERE student = '" + studentID + "'");
        
        while (studentReadCourses.next()) {
            sb.append("\n " + studentReadCourses.getString("name") + 
                    " (" + studentReadCourses.getString("course") + "), " + 
                    studentReadCourses.getInt("credits") + "p: " + 
                    studentReadCourses.getString("grade"));
        }
    }

    private static void appendRegisteredCoursesInfo(StringBuilder sb, Statement infoStatement, String studentID) throws SQLException 
    {
        sb.append("\n\nRegistered courses (name (code), credits: status):");
        
        ResultSet studentRegCourses = infoStatement.executeQuery("WITH RegWithPos AS "
                +    "(SELECT A.course, A.student, A.waitingStatus, B.position "
                +    "FROM Registrations A LEFT OUTER JOIN CourseQueuePositions B "
                +    "ON A.student = B.student AND A.course = B.course) "
                + "SELECT A.name, A.credits, B.course, B.waitingStatus, B.position "
                + "FROM Courses A, RegWithPos B "
                + "WHERE A.code = B.course "
                + "AND B.student = '" + studentID + "'");
        
        while (studentRegCourses.next()) {
            sb.append("\n " + studentRegCourses.getString("name") + 
                    " (" + studentRegCourses.getString("course") + "), " + 
                    studentRegCourses.getInt("credits") + "p: " + 
                    studentRegCourses.getString("waitingStatus"));
            int pos = studentRegCourses.getInt("position");
            if (pos != 0) {
                sb.append(" as nr " + pos);
            }
        }
    }

    private static void appendGraduationInfo(StringBuilder sb, Statement infoStatement, String studentID) throws SQLException 
    {
        ResultSet studentGraduationPath = infoStatement.executeQuery("SELECT totalCredits, mathCredits, researchCredits, nbrSeminarCourses, qualifyForGrad "
                + "FROM PathToGraduation "
                + "WHERE student = '" + studentID + "'");
        
        sb.append("\n\nSeminar courses taken: " + studentGraduationPath.getInt("nbrSeminarCourses"));
        sb.append("\nMath credits taken: " + studentGraduationPath.getInt("mathCredits"));
        sb.append("\nResearch credits taken: " + studentGraduationPath.getInt("researchCredits"));
        sb.append("\nTotal credits taken: " + studentGraduationPath.getInt("totalCredits"));
        sb.append("\nFulfills the requirements for graduation: " + (studentGraduationPath.getString("qualifyForGrad").equalsIgnoreCase("true") ? "yes" : "no"));
    }
    
    // returns null if there is no such course.
    private static String getCourseNameByCode(Statement statement, String courseCode) throws SQLException 
    {
        ResultSet course = statement.executeQuery("SELECT name FROM Courses WHERE code = '" + courseCode + "'");
        
        if (course.next()) {
            return course.getString("name");
        }
        return null; 
    }
    
    // returns true if registration was deleted, false if there was no registration
    private static boolean deleteRegistration(Statement statement, String studentID, String courseCode) throws SQLException {
        int nbrDeletedRows = statement.executeUpdate("DELETE FROM Registrations "
                + "WHERE student = '" + studentID + "' "
                + "AND course = '" + courseCode + "'");
        
        return nbrDeletedRows > 0;
    }

   
}