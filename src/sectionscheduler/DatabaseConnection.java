/*
 * Copyright © 2015 by Tenten Ponce - All Rights Reserved.
 * This project or any portion thereof may not be reproduced
 * or used in any manner, without the prior permission 
 * from the owner.
 */
package sectionscheduler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

/**
 * <H3>Handles the connection to the database.</H3>
 * This class connects the system to the database using dynamic path, it gets
 * the path of the system so the database must be together with the system, it
 * also disconnects the system properly by closing first the result set,
 * statement, prepared statement and lastly the connection to avoid unnecessary
 * errors.
 *
 * @author Tenten Ponce
 * @version 1.0
 * @since October 2015
 */
public final class DatabaseConnection {
    
    private static DatabaseConnection instance;

    private static final String APP_PATH = "jdbc:ucanaccess://" + appPath() + "\\maindb.accdb";
    private static final Logger log = (Logger) LoggerFactory.getLogger(DatabaseConnection.class);
    
    /** 
     * So i will not declare this again and again on all of the class.
     * Also, it provides easy closing of resources. I think this is safe
     * because every time I used this resources, I closed them immediately
     * so they do not hold any information for a long time.
     */
    public Connection conn;
    public Statement s;
    public PreparedStatement ps;
    public ResultSet rs;
    /***********************************************************/
    
    private static String query; //handles query 
    private static String disconMsg; //Disconnect Message
    
    /**
     * Singleton Class, restricts the instantiation of class to one object.
     * 
     * @return instance of the class
     */
    public static DatabaseConnection getInstance(){
        synchronized(DatabaseConnection.class){
            if(instance == null){
                log.setLevel(Level.ALL);
                instance = new DatabaseConnection();
            }
        }
        
        return instance;
    }
        
    /**
     * Opens Database using ucanaccess.
     *
     * @throws ClassNotFoundException if ucanaccess library is not found.
     * @throws SQLException if app path, or password for database is wrong
     */
    public void open() throws ClassNotFoundException, SQLException {
        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        //MainParent.class.getProtectionDomain().getCodeSource().getLocation();
        conn = DriverManager.getConnection(APP_PATH, "", "");
        log.debug("Connected to Database");
    }
    
    /**
     * Still Opens Database using ucanaccess with a logging option
     * @param msg message to be logged.
     * @throws ClassNotFoundException if ucanaccess library is not found.
     * @throws SQLException if app path, or password for database is wrong.
     */
    public void open(String msg) throws ClassNotFoundException, SQLException {
        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        //MainParent.class.getProtectionDomain().getCodeSource().getLocation();
        conn = DriverManager.getConnection(APP_PATH, "", "");
        log.debug("Connected to Database: {}", msg);
        disconMsg = msg;
    }

    /**
     * Closes the database properly.
     */
    public void close() {
        try {
            if (rs != null) {
                rs.close();
            }
            if (s != null) {
                s.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
            log.debug("Disconnected to Database: {}", disconMsg);
        } catch (Exception ex) {
            log.error("Error in closing database resources", ex);
        }
    }

    /**
     * Returns the query that has been set.
     *
     * @return String query
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the query to be executed.
     *
     * @param query query to be executed.
     */
    public void setQuery(String query) {
        DatabaseConnection.query = query;
    }

    /**
     * Go to last row to return the number of records are there 
     * in a result set.
     * @param resultset the result set to be check how many records.
     * @param msg what is being counted (debugging purposes).
     * @return number of records in the result set.
     */
    public int getRecordCount(ResultSet resultset, String msg) {
        log.debug("Starting to count records of {}", msg);
        int count;
        try {
            resultset.last();
            count = resultset.getRow();
            resultset.beforeFirst();
        } catch (SQLException ex) {
            log.warn("Error in counting records in result set", ex);
            count = 0; //return with 0 if error.
        }

        log.debug("Counted records: {}", count);
        return count;
    }

    /**
     * Gets the directory where the system is.
     * @return String path/directory of the system.
     */
    public static String appPath() {
        return System.getProperty("user.dir");
    }
}
