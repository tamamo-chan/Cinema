/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cinema;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.function.BiConsumer;
import javafx.util.Pair;

class User {

    static String url = "jdbc:postgresql://balarama.db.elephantsql.com:5432/vzwjksup";
    static String username = "vzwjksup";
    static String password = "OfSGhD9m8yhKrrOmg5vFJ7jbuXQafQ2o";

    String name;
    String email;
    int phone;
    String pwordHash;
    int uId;
    ArrayList<Group> groups;
    int reward_Available;
    int shifts;
    int privilege;

    public String GetName() {
        return this.name;
    }

    public String GetEmail() {
        return this.email;
    }

    public int GetPhone() {
        return this.phone;
    }

    public void AddUser(int privilege) {

        Connection db;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            db = DriverManager.getConnection(url, username, password);
            st = db.prepareStatement("INSERT INTO users (name, email, password, "
                    + "phone, privilege, shifts, rewards) VALUES (?, ?, ?, ?, ?, 0, 0)");
            st.setString(1, this.GetName());
            st.setString(2, this.GetEmail());
            st.setString(3, "0000");
            st.setInt(4, this.GetPhone());
            st.setInt(5, privilege);

            rs = st.executeQuery();

        } catch (java.sql.SQLException e) {
            System.out.println(e.getMessage());
        }

    }
}

class Group {

    static String url = "jdbc:postgresql://balarama.db.elephantsql.com:5432/vzwjksup";
    static String username = "vzwjksup";
    static String password = "OfSGhD9m8yhKrrOmg5vFJ7jbuXQafQ2o";

    String Name;
    Pair<User, Date> Members;
    User OnCallSuper;

    private String GetName() {
        return this.Name;
    }

    private User GetSuper() {
        return this.OnCallSuper;
    }

    public void AddGroup() {

        Connection db;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            db = DriverManager.getConnection(url, username, password);
            st = db.prepareStatement("INSERT INTO groups (name, super) "
                    + "VALUES (?, ?)");
            st.setString(1, this.GetName());
            st.setInt(2, this.GetSuper().uId);

            rs = st.executeQuery();

        } catch (java.sql.SQLException e) {
            System.out.println(e.getMessage());
        }

    }
}

class Show {

    String name;
    Date SDate;
    Date EDate;
    ArrayList<Shift> Shifts;
    int Room;
}

class Shift {

    Group Group;
    Date SDate;
    Date EDate;
    User person;
    String name;
    int status;
}

/* SQL templates
INSERT INTO users (name, email, password, phone, privilege, shifts, rewards) VALUES ()

INSERT INTO groups (name, super) VALUES (?, ?)

INSERT INTO groupmembers (groupname, uid, joined) VALUES (?, ?, ?)

INSERT INTO shows (name, startdate, enddate, room) VALUES (?, ?, ?, ?)

INSERT INTO shifts (name, startdate, enddate, groupname, sid, status) VALUES (?, ?, ?, ?, ?, ?)
 */
/**
 *
 * @author Piratica
 */
public class Cinema {

    static String url = "jdbc:postgresql://balarama.db.elephantsql.com:5432/vzwjksup";
    static String username = "vzwjksup";
    static String password = "OfSGhD9m8yhKrrOmg5vFJ7jbuXQafQ2o";

    static User currentUser = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException {
        // TODO code application logic here

        try {
            Class.forName("org.postgresql.Driver");
        } catch (java.lang.ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        Scanner scanner = new Scanner(System.in);

        System.out.println("Please login to your user account: ");
        System.out.println("username: ");

        String user_name = scanner.nextLine();

        System.out.println("password: ");

        String pword = scanner.nextLine();

        currentUser = LoginUser(user_name, pword);

        System.out.println("Current user is: " + currentUser.name);

        System.out.println("You have the following options: ");
        System.out.println("1. Do you want to list users of the system?");
        System.out.println("2. Do you want to create a user?");
        System.out.println("3. Do you want to list the current groups?");
        System.out.println("4. Do you want to create a group? (Only Supers)");
        System.out.println("5. Do you want to list the members of groups?");

        String option = scanner.nextLine();

        if (option.equalsIgnoreCase("1")) {

            ListUser();

        } else if (option.equalsIgnoreCase("2")) {

            NewUser(scanner);

        } else if (option.equalsIgnoreCase("3")) {

            ListGroups();

        } else if (option.equalsIgnoreCase("4")) {

            if (currentUser.privilege == 1) {

                CreateGroup(scanner);

            } else {
                System.out.println("You do not have permission to create "
                        + "groups!");
            }

        } else if (option.equalsIgnoreCase("5")) {

        } else {

        }
    }

    private static User LoginUser(String email, String password) {
        // hash password
        LinkedList vars = new LinkedList();
        vars.add(email);
        vars.add(password);
        String query = "SELECT uid, name, email, phone, privilege, shifts, rewards "
                + "FROM users WHERE email = ? AND password = ?";

        BiConsumer<LinkedList, ResultSet> f = (l, rs) -> {
            try {
                while (rs.next()) {
                    l.add(rs.getInt(1));
                    l.add(rs.getString(2));
                    l.add(rs.getString(3));
                    l.add(rs.getInt(4));
                    l.add(rs.getInt(5));
                    l.add(rs.getInt(6));
                    l.add(rs.getInt(7));
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        };
        LinkedList result = Query(query, vars, f);

        if (result.isEmpty()) {
            return null;
        }

        User user = new User();
        user.uId = (int) result.pop();
        user.name = (String) result.pop();
        user.email = (String) result.pop();
        user.phone = (int) result.pop();
        user.privilege = (int) result.pop();
        user.shifts = (int) result.pop();
        user.reward_Available = (int) result.pop();

        return user;
    }

    public static LinkedList Query(String query, LinkedList queryVariables, BiConsumer f) {
        Connection db;
        LinkedList l = new LinkedList();
        try {
            db = DriverManager.getConnection(url, username, password);
            PreparedStatement pquery = db.prepareStatement(query);

            int i = 1;
            while (!queryVariables.isEmpty()) {
                Object temp = queryVariables.pop();
                if (temp instanceof String) {
                    pquery.setString(i, (String) temp);
                } else if (temp instanceof Integer) {
                    pquery.setInt(i, (Integer) temp);
                } else if (temp instanceof java.sql.Date) {
                    pquery.setDate(i, (java.sql.Date) temp);
                } else {
                }
                i++;
            }
            ResultSet rs = pquery.executeQuery();
            f.accept(l, rs);

        } catch (java.sql.SQLException e) {
            System.out.println(e.getMessage());
        }
        return l;
    }

    private static void ListUser() throws SQLException {
        System.out.println("Current users of the system: ");

        Connection db;
        Statement st = null;
        ResultSet rs = null;
        try {
            db = DriverManager.getConnection(url, username, password);
            st = db.createStatement();
            rs = st.executeQuery("SELECT * FROM users");

        } catch (java.sql.SQLException e) {
            System.out.println(e.getMessage());
        }

        int personnr = 1;

        while (rs.next()) {
            System.out.println("Information about user " + personnr + ": ");
            System.out.print("Name: ");
            System.out.println(rs.getString(2));
            System.out.print("Email: ");
            System.out.println(rs.getString(3));
            System.out.print("Phone Number: ");
            System.out.println(rs.getString(5));
            System.out.print("This user has taken " + rs.getString(7)
                    + " shifts and currently has " + rs.getString(8)
                    + " rewards available.");

            personnr++;
            System.out.println();

        }

        rs.close();
    }

    private static void NewUser(Scanner scanner) {

        User user = new User();

        System.out.println("What is the user's name?: ");

        user.name = scanner.nextLine();

        System.out.println("What is the user's email?");

        user.email = scanner.nextLine();

        System.out.println("What is the user's phone number?");

        user.phone = scanner.nextInt();

        System.out.println("Should " + user.GetName() + " be a Super (1) or "
                + "a normal user (0)? Enter a number: ");

        int privilege = scanner.nextInt();

        if (privilege > 1 || privilege < 0) {
            System.out.println("Error, number outside of scope 0-1");
            return;
        }

        user.AddUser(privilege);

        System.out.println("User created!");
    }

    //TODO
    private static void ListGroups() {

        System.out.println("Current users of the system: ");

        Connection db;
        Statement st = null;
        ResultSet rs = null;
        try {
            db = DriverManager.getConnection(url, username, password);
            st = db.createStatement();
            rs = st.executeQuery("SELECT * FROM groups");

        } catch (java.sql.SQLException e) {
            System.out.println(e.getMessage());
        }

        int groupnr = 1;

    }

    private static void CreateGroup(Scanner scanner) {

        Group group = new Group();

        System.out.println("What would you like to name the group?: ");

        group.Name = scanner.nextLine();

        System.out.println("Please enter the email of the Super responsible "
                + "for the group: ");

        String superEmail = scanner.nextLine();

        User tester = new User();

        LinkedList vars = new LinkedList();
        vars.add(superEmail);
        String query = "SELECT uid, name FROM users WHERE email = ? ";

        BiConsumer<LinkedList, ResultSet> f = (l, rs) -> {
            try {
                while (rs.next()) {
                    l.add(rs.getInt(1));
                    l.add(rs.getString(2));
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        };

        LinkedList result = Query(query, vars, f);

        tester.uId = (int) result.pop();
        tester.name = (String) result.pop();

        System.out.println("The super's name is " + tester.name + ", correct? "
                + "(y/n)?: ");

        String reply = scanner.nextLine();

        if (reply.equalsIgnoreCase("y")) {

            group.OnCallSuper = tester;

            group.AddGroup();

            System.out.println("Group added!");
        } else {
            CreateGroup(scanner);
        }

    }

}
