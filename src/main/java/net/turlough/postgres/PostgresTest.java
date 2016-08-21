package net.turlough.postgres;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.*;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by turlough on 21/08/16.
 */
public class PostgresTest {

    Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    class Blog {
        public int id;
        public String subject;
        public String permalink;
    }

    public static void main(String[] args) {
        new PostgresTest();
    }

    public PostgresTest() {
        Connection conn = null;
        LinkedList listOfBlogs = new LinkedList();

        // connect to the database
        conn = connectToDatabaseOrDie();
        String subject = "subject " + System.currentTimeMillis();
        String permalink = "link " + System.currentTimeMillis();
        insert(conn, subject, permalink);

        // get the data
        populateListOfTopics(conn, listOfBlogs);

        // print the results
        printTopics(listOfBlogs);
    }

    private void printTopics(LinkedList listOfBlogs) {
        Iterator it = listOfBlogs.iterator();
        while (it.hasNext()) {
            Blog blog = (Blog) it.next();
            System.out.println(gson.toJson(blog));
        }
    }

    private void insert(Connection conn, String subject, String permalink){
        try {
            Statement st = conn.createStatement();
            st.execute(
                    String.format("INSERT INTO blog VALUES (%d, '%s','%s');",
                    System.currentTimeMillis() % 65536,
                    subject,
                    permalink
            ));
            st.close();
        } catch (SQLException se) {
            System.err.println("Threw a SQLException inerting a row.");
            System.err.println(se.getMessage());
        }
    }

    private void populateListOfTopics(Connection conn, LinkedList listOfBlogs) {
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, subject, permalink FROM blog ORDER BY id");
            while (rs.next()) {
                Blog blog = new Blog();
                blog.id = rs.getInt("id");
                blog.subject = rs.getString("subject");
                blog.permalink = rs.getString("permalink");
                listOfBlogs.add(blog);
            }
            rs.close();
            st.close();
        } catch (SQLException se) {
            System.err.println("Threw a SQLException in select statement.");
            System.err.println(se.getMessage());
        }
    }

    private Connection connectToDatabaseOrDie() {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost/testdb";
            conn = DriverManager.getConnection(url, "postgres", "s0crates");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(2);
        }
        return conn;
    }

}
