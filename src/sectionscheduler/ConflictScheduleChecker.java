/*
 * Copyright © 2015 by Tenten Ponce - All Rights Reserved.
 * This project or any portion thereof may not be reproduced
 * or used in any manner, without the prior permission 
 * from the owner.
 */
package sectionscheduler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import org.jdesktop.swingx.JXTable;
import org.slf4j.LoggerFactory;

/**
 * <H3>Decision Support Class: Checks for conflicts and suggests schedules.</H3>
 * 
 * This class checks for ALL of the conflicts on the schedule
 * that was recorded on the database to avoid creating schedules
 * that has the same time, or when a class is still on going,
 * and also suggests for rooms schedule(s) that is/are still available.
 * 
 * @author Tenten Ponce
 * @version 1.1
 * @since October 2015
 */

public class ConflictScheduleChecker {
    
    private static final DatabaseConnection DB = DatabaseConnection.getInstance(); //Database actions
    private static final Calendar CAL = Calendar.getInstance();
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("hh:mm a"); // used for formatting and parsing
    private static final Logger log = (Logger) LoggerFactory.getLogger(ConflictScheduleChecker.class);
    private static final String[] completedRow = new String[4]; //container for completed row to compare others.

    private boolean[] isCompleteRow; //tells ALL table row if completed or not.
    private static int conflictRow1, conflictRow2;
    private static int hourstart1 = 0, hourstart2 = 0, hourend1 = 0, hourend2 = 0; //hour holder of comparing and compared to
    private static int minend2 = 0, minstart1 = 0, minend1 = 0, minstart2 = 0; //minute holder of comparing and compared to
    private static String conflictSection;

    public ConflictScheduleChecker(){
        log.setLevel(Level.ALL); //all log levels
    }
    
    /**
     * This method checks the table if there was a conflict.
     * It has two stages:
     * 
     * 1.) check each row if it is completed or not and store it in a boolean array.
     * 2.) loop through the again the table, check if the row is completed from
     * the boolean array, and compare it to the table AGAIN(LOOP) that was completed.
     * 
     * After checking, it highlights the row index of the conflicted rows.
     * (THIS DOES NOT INCLUDE ROOMS BECAUSE A SECTION MUST BE ON 1 CLASS ON THE SAME
     * TIME AND DAY).
     * 
     * @param jt The table that will be scanned for conflicts.
     * @return true if there was a conflict, and false if not.
     */
    @SuppressWarnings("SleepWhileInLoop")
    public boolean isTableHasConflict(JXTable jt){
        log.trace(">> isTableHasConflict()");
        
        int row = jt.getModel().getRowCount();
        int col = jt.getModel().getColumnCount();
        
        log.debug("{} rows found", row);
        isCompleteRow = new boolean[row]; //to see if what row is completed
        int completeRow = 0; //how many row is completed.
        
        //========check what row is completed and count them also.=============//
        log.trace(">> Entering loop check index of complete rows and count them");
        for(int x = 0; x < row; x++){
            isCompleteRow[x] = true; //assume first that the row is complete.
            for(int y = 0; y < col; y++){
                if(y == 2 || y == 3){ //check only if it is timestart, timeend, day
                    if(jt.getModel().getValueAt(x, y) == null){
                        //if there's null, stop looping tru columns and go to next row.
                        isCompleteRow[x] = false; // row is not complete.
                        log.debug("row {} is not complete.", x);
                        y = col;
                    } 
                } else if (y == 4) {
                    if(jt.getModel().getValueAt(x, y) == null){
                        isCompleteRow[x] = false; // row is not complete.
                        log.debug("row {} is not complete.", x);
                        y = col;
                    } else if ("".equals(String.valueOf(jt.getModel().getValueAt(x, y)))) {
                        isCompleteRow[x] = false; // row is not complete.
                        log.debug("row {} is not complete.", x);
                        y = col;
                    } 
                }
            }
            
            if(isCompleteRow[x]){ //if the row is complete, increment to check how many are completed later.
                completeRow++;
                log.debug("row {} is complete.", x);
            }
        }
        log.trace("<< Loop");
        
        //==============compare each row to the other rows.=========================//
        log.debug("Complete rows: {}", completeRow);
        if(completeRow > 1){
            log.trace(">> Entering Loop to compare all of the complete rows...");
            for(int x = 0; x < isCompleteRow.length; x++){
                if(isCompleteRow[x]){ //if it is a completed row, loop it and check on the other completed rows.
                    //Store row values on an single array.
                    completedRow[0] = String.valueOf(jt.getModel().getValueAt(x, 2)); //time start
                    completedRow[1] = String.valueOf(jt.getModel().getValueAt(x, 3)); //time end
                    completedRow[2] = String.valueOf(jt.getModel().getValueAt(x, 4)); //day
                    
                    log.debug("Comparing {} row", x);
                    log.debug("Time Start: {}", completedRow[0]);
                    log.debug("Time End: {}", completedRow[1]);
                    log.debug("Day: {}", completedRow[2]);
                    log.trace(">> Entering Loop to compare the complete row to the other completed rows");
                    for(int y = 0; y < isCompleteRow.length; y++) {//and loop it to the rows then compare.
                        if (x != y && isCompleteRow[y]) {//skip if it was the same row(duh) or if it was not completed                  
                            try {
                                /*get their 24 hour to check conflicts.*/
                                log.debug("Comparing to {} row...", y);
                                log.debug("Time Start: {}", String.valueOf(jt.getModel().getValueAt(y, 2)));
                                log.debug("Time End: {}", String.valueOf(jt.getModel().getValueAt(y, 3)));
                                log.debug("Day: {}", String.valueOf(jt.getModel().getValueAt(y, 4)));
                                //row that is being hold. (compared from)
                                CAL.setTime(FORMAT.parse(completedRow[0])); //time start
                                hourstart1 = CAL.get(Calendar.HOUR_OF_DAY);
                                minstart1 = CAL.get(Calendar.MINUTE); //minute bumps

                                CAL.setTime(FORMAT.parse(completedRow[1])); //time end
                                hourend1 = CAL.get(Calendar.HOUR_OF_DAY);
                                minend1 = CAL.get(Calendar.MINUTE); //minute bumps
                                
                                //variables that will be compared on the row that is being hold. (compared to)
                                CAL.setTime(FORMAT.parse(String.valueOf(jt.getModel().getValueAt(y, 2)))); //time start
                                hourstart2 = CAL.get(Calendar.HOUR_OF_DAY);
                                minstart2 = CAL.get(Calendar.MINUTE); //minute bumps
                                
                                CAL.setTime(FORMAT.parse(String.valueOf(jt.getModel().getValueAt(y, 3)))); //time end
                                hourend2 = CAL.get(Calendar.HOUR_OF_DAY);
                                minend2 = CAL.get(Calendar.MINUTE); //minute bumps

                            } catch (ParseException ex) {/* IT CANT BE HERE */} 
                            //compare the whole row if there was a confict.
                            if ((hourstart1 == hourstart2 //if time start is equal
                                    || hourend1 == hourend2 //if time end is equal
                                    || (hourstart1 < hourstart2 && hourend1 > hourstart2) //if time start is between time start and end of another row
                                    || (hourstart1 < hourend2 && hourend1 > hourend2) //if time end is between time start and end of another row.
                                    || (hourstart1 == hourend2 && minend2 > minstart1) //if class is not finished (minutes left)
                                    || (hourstart2 == hourend1 && minend1 > minstart2)) //vice versa
                                    && completedRow[2].equals(String.valueOf(jt.getModel().getValueAt(y, 4)))){ //day if equal
                                    /*It doesnt matter if they are on different rooms, section cannot be divided*/
                                    //completedRow[3].equals(String.valueOf(jt.getModel().getValueAt(y, 5))) // room
                            
                                //do this if the codition is true
                                log.debug("Conflict Schedule has found! {} row and {} row", x, y);
                                setRow1(x);
                                setRow2(y);

                                jt.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); //highlighting
                                jt.clearSelection(); //highlighting
                                jt.addRowSelectionInterval(getRow1(), getRow1()); //highlighting
                                jt.addRowSelectionInterval(getRow2(), getRow2()); //highlighting

                                log.trace("<< Exiting ALL Loops");
                                log.trace("<< isTableHasConflict()");
                                return true;
                            }
                        }
                    }
                    log.trace("<< Exiting Loop...");
                }
            }
            log.trace("<< Exiting Loop...");
        }
        
        log.debug("No Conflict has found.");
        log.trace("<< isTableHasConflict()");
        
        return false;
    }
    
    /**
     * Checks Schedule to be created if has conflict on other section
     * schedule.
     * Queries the room schedule and loop each completed row to check
     * if there's conflict.
     * 
     * @param jt Table to be check on room schedules.
     * @param sem Semester of the section. (Different Semesters will not
     * considered as conflict.
     * @param sectionCode sectionCode to be avoided
     * @return true if it has conflict on room schedules, and false if not.
     */
    @SuppressWarnings("SleepWhileInLoop")
    public boolean isScheduleHasConflict(JXTable jt, int sem, String sectionCode){
        log.trace(">> isScheduleHasConflict()");
        
        int row = jt.getModel().getRowCount();
        int col = jt.getModel().getColumnCount();
        
        log.debug("{} rows found", row);
        isCompleteRow = new boolean[row]; //to see if what row is completed
        int completeRow = 0; //how many row is completed.
        
        //========check what row is completed and count them also.=============//
        log.trace(">> Entering loop check index of complete rows and count them");
        for(int x = 0; x < row; x++){
            isCompleteRow[x] = true; //assume first that the row is complete.
            for(int y = 0; y < col; y++){
                if(y == 2 || y == 3){ //check only if it is timestart, timeend, day, room
                    if(jt.getModel().getValueAt(x, y) == null){
                        //if there's null, stop looping tru columns and go to next row.
                        isCompleteRow[x] = false; // row is not complete.
                        log.debug("row {} is not complete.", x);
                        y = col;
                    }
                } else if (y == 4 || y == 5) {
                    if(jt.getModel().getValueAt(x, y) == null){
                        isCompleteRow[x] = false; // row is not complete.
                        log.debug("row {} is not complete.", x);
                        y = col;
                    } else if ("".equals(String.valueOf(jt.getModel().getValueAt(x, y)))) {
                        isCompleteRow[x] = false; // row is not complete.
                        log.debug("row {} is not complete.", x);
                        y = col;
                    }
                }
            }
            
            if(isCompleteRow[x]){ //if the row is complete, increment to check how many are completed later.
                completeRow++;
                log.debug("row {} is complete.", x);
            }
        }
        log.trace("<< Loop");
        
        if(completeRow == 0){
            return false; //exit because there's no row completed.
        }
        //======================GET ROOM SCHED===================//
        String[][] roomSchedule = {};
        try{
            DB.open("Room Schedule");
            log.debug("Preparing normal Statement...");
            DB.s = DB.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            log.debug("Executing statement...");
            DB.rs = DB.s.executeQuery("SELECT TimeStart, TimeEnd, Day, Room, SectionCode FROM SectionSubjTB "
                    + "WHERE TimeStart IS NOT NULL AND TimeEnd IS NOT NULL AND Day IS NOT NULL AND Day!='' AND "
                    + "Room IS NOT NULL AND Room!='' AND SectionCode!='" + sectionCode + "'"); //get complete rows
            
            roomSchedule = new String[DB.getRecordCount(DB.rs, "Room Schedule")][5]; //TimeStart, Time End, Day, Room Code, SectionCode
            
            log.trace(">> Entering Loop put records on array");
            for(int x = 0; DB.rs.next(); x++){
                roomSchedule[x][0] = FORMAT.format(DB.rs.getTimestamp("TimeStart"));
                roomSchedule[x][1] = FORMAT.format(DB.rs.getTimestamp("TimeEnd"));
                roomSchedule[x][2] = DB.rs.getString("Day");
                roomSchedule[x][3] = DB.rs.getString("Room");
                roomSchedule[x][4] = DB.rs.getString("SectionCode");
            }
            log.trace("<< Exiting Loop");
        } catch (ClassNotFoundException | SQLException ex) {
            log.error("Error retreiving room schedule", ex);
        } finally{
            DB.close();
        }
        
        log.trace(">> Entering Loop Compare completed rows on room schedules");
        for (int x = 0; x < isCompleteRow.length; x++) {
            if (isCompleteRow[x]) {
                completedRow[0] = String.valueOf(jt.getModel().getValueAt(x, 2)); //time start
                completedRow[1] = String.valueOf(jt.getModel().getValueAt(x, 3)); //time end
                completedRow[2] = String.valueOf(jt.getModel().getValueAt(x, 4)); //day
                completedRow[3] = String.valueOf(jt.getModel().getValueAt(x, 5)); //room
                
                //row being checked
                jt.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); //highlighting
                jt.clearSelection(); //highlighting
                jt.addRowSelectionInterval(x, x); //highlighting
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                log.debug("Comparing {} row", x);
                log.debug("Time Start: {}", completedRow[0]);
                log.debug("Time End: {}", completedRow[1]);
                log.debug("Day: {}", completedRow[2]);
                log.debug("Room: {}", completedRow[3]);
                
                log.trace(">> Entering Loop Complete row is found, compare to room schedules.");
                for (int y = 0; y < roomSchedule.length; y++) {
                    try {
                        /*get their 24 hour to check conflicts.*/
                        log.debug("Comparing to {} row...", y);
                        log.debug("Time Start: {}", roomSchedule[y][0]);
                        log.debug("Time End: {}", roomSchedule[y][1]);
                        log.debug("Day: {}", roomSchedule[y][2]);
                        log.debug("Room: {}", roomSchedule[y][3]);
                        //row that is being hold. (compared from)
                        CAL.setTime(FORMAT.parse(completedRow[0])); //time start
                        hourstart1 = CAL.get(Calendar.HOUR_OF_DAY);
                        minstart1 = CAL.get(Calendar.MINUTE); //minute bumps
                        
                        CAL.setTime(FORMAT.parse(completedRow[1])); //time end
                        hourend1 = CAL.get(Calendar.HOUR_OF_DAY);
                        minend1 = CAL.get(Calendar.MINUTE); //minute bumps
                        
                        //variables that will be compared on the row that is being hold. (compared to)
                        CAL.setTime(FORMAT.parse(roomSchedule[y][0])); //time start
                        hourstart2 = CAL.get(Calendar.HOUR_OF_DAY);
                        minstart2 = CAL.get(Calendar.MINUTE); //minute bumps

                        CAL.setTime(FORMAT.parse(roomSchedule[y][1])); //time end
                        hourend2 = CAL.get(Calendar.HOUR_OF_DAY);
                        minend2 = CAL.get(Calendar.MINUTE); //minute bumps

                    } catch (ParseException ex) {/* IT CANT BE HERE */}
                    
                    if ((hourstart1 == hourstart2 //if time start is equal
                            || hourend1 == hourend2 //if time end is equal
                            || (hourstart1 < hourstart2 && hourend1 > hourstart2) //if time start is between time start and end of another row
                            || (hourstart1 < hourend2 && hourend1 > hourend2) //if time end is between time start and end of another row.
                            || (hourstart2 < hourstart1 && hourend2 > hourstart1) //vice versa
                            || (hourstart2 < hourend1 && hourend2 > hourend2) //vice versa
                            || (hourstart2 == hourend1 && minend1 > minstart2) //if class is not finished (minutes left)
                            || (hourstart1 == hourend2 && minend2 > minstart1)) //vice versa
                            && completedRow[2].equals(roomSchedule[y][2]) //day if equal
                            /*NOW It matters if they are on different rooms.*/
                            && completedRow[3].equals(roomSchedule[y][3])) { // room if equal

                        //do this if the codition is true
                        log.debug("Conflict found");
                       
                        setConflictSection(roomSchedule[y][4]);
                        log.debug("Section that is conflicted: {}", getConflictSection());
                        log.trace("<< Exiting ALL Loops");
                        log.trace("<< isScheduleHasConflict()");

                        jt.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); //highlighting
                        jt.clearSelection(); //highlighting
                        jt.addRowSelectionInterval(x, x); //highlighting
                        return true;

                    }
                }
                log.trace("<< Exiting Loop");
            }
        }
        log.trace("<< Exiting Loop");
        
        jt.clearSelection(); //highlighting
        
        log.debug("No Conflict has found.");
        log.trace("<< isScheduleHasConflict()");
        return false;
    }
    
    /**
     * This method has three steps to complete. 
     * 1st step: it removes all of the time that was outside of the
     * class hours.
     * 2nd step: it marks all of the time as start if a class will start
     * on that specific hour, and it changes the time to that exact time 
     * when the class starts. (Example: Starts at 1:30 PM, true on 1:00 PM,
     * then it changes to 1:30 PM)
     * 3rd step: it gets the interval. the start of the interval was if there
     * is no class to start at that time, then it loops, and when it reaches
     * a time where a class will start, it marks as the end of the interval
     * and adds it to the combo box. Then it will continue to loop again, and
     * start another interval until it reaches the class hour limit (end).
     * @param roomCode Room to be check
     * @param day What day (Sunday-Saturday)
     * @return 
     */
    public ArrayList<String> getAvailableHours(String roomCode, String day){
        String[][] usedTime = {}; //time start and time end
        int[] givenTime = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23}; // time object
        String[] availTime = new String[givenTime.length]; //available time of the room
        SimpleDateFormat hour24Format = new SimpleDateFormat("HH:mm");
        boolean[] isStart = new boolean[givenTime.length]; //true if there was a class on that time
        
        ArrayList<String> availableTime = new ArrayList();
        
        try {
            DB.open("Check for available hours");
            DB.setQuery("SELECT TimeStart, TimeEnd FROM SectionSubjTB WHERE "
                    + "Room=? AND Day=? AND TimeStart Is Not Null AND TimeEnd Is Not Null");
            
            log.debug("Preparing Statement...");
            DB.ps = DB.conn.prepareStatement(DB.getQuery(), 
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            DB.ps.setString(1, roomCode);
            DB.ps.setString(2, day);
            
            log.debug("Executing Statement...");
            DB.rs = DB.ps.executeQuery();
            
            int rsLength = DB.getRecordCount(DB.rs, "Completed Time");
            usedTime = new String[rsLength][2]; //two columns, time start and time end
            
            for(int x = 0; DB.rs.next(); x++) {
                usedTime[x][0] = FORMAT.format(DB.rs.getTimestamp("TimeStart"));
                usedTime[x][1] = FORMAT.format(DB.rs.getTimestamp("TimeEnd"));
            }
            
        } catch (SQLException | ClassNotFoundException ex) {
            log.error("Error checking for available hours of room", ex);
        } finally {
            DB.close();
        }
        
        //==================remove time that are outside of the class hours====================//
        TimeValidator tv = new TimeValidator(new JTextField());
        tv.setClassHour(); //set the class hours
        
        int hourStart = tv.getClassStart();
        int hourEnd = tv.getClassEnd();
        
        int minStart = tv.getCLASS_START_MIN();
        int minEnd = tv.getCLASS_END_MIN();
        
        log.debug("hour start {}, hour end {}", hourStart, hourEnd);
        log.debug("min start {}, min end {}", minStart, minEnd);
        
        for(int x = 0; x < givenTime.length; x++) {
            log.debug("Currently checking time: {}", givenTime[x]);
            
            if(givenTime[x] < hourStart || givenTime[x] > hourEnd){ //check if class not started on this time
                availTime[x] = ""; //empty because it is not available
            } else { //else the time is available
                log.debug("Date to format: {}", givenTime[x] + ":00");
                try {
                    availTime[x] = FORMAT.format(hour24Format.parse(givenTime[x] + ":00"));
                } catch (ParseException ex) {
                    log.error("Parsing Date error: {}", givenTime[x] + ":00", ex);
                }
            }
            
            log.debug("available time: {}", availTime[x]);
        }
               
        //==================mark all of the time that class has start====================//
        //==================and change them to where the time has start==================//
        for (int x = 0; x < usedTime.length; x++) {
            log.debug("Time checking: {}", availTime[x]);
            try {
                CAL.setTime(FORMAT.parse(usedTime[x][0])); //time start column
            } catch (ParseException ex) { //must not happen because it came from database
                log.error("Error parsing time: {}", usedTime[x][0], ex);
            }

            hourStart = CAL.get(Calendar.HOUR_OF_DAY);
            minStart = CAL.get(Calendar.MINUTE);

            try {
                CAL.setTime(FORMAT.parse(usedTime[x][1])); //time end column
            } catch (ParseException ex) {
                log.error("Error parsing time: {}", usedTime[x][1], ex);
            }

            hourEnd = CAL.get(Calendar.HOUR_OF_DAY);
            minEnd = CAL.get(Calendar.MINUTE);

            log.debug("checking on: {}:{} (time start)", hourStart, minStart);
            log.debug("checking on: {}:{} (time end)", hourEnd, minEnd);

            for (int y = 0; y < givenTime.length; y++) {
                if (!availTime[y].equals("")) { //check if available time is not available already (wtf?)
                    log.debug("Checking time: {}, hour: {}", availTime[y], givenTime[y]);
                    
                    if (givenTime[y] == hourStart) { //check if hour start is equal
                        isStart[y] = true;
                        if (minStart > 0) { //check if minute did not start at exact o clock
                            try {
                                availTime[y] = FORMAT.format(hour24Format.parse(hourStart + ":" + minStart));
                            } catch (ParseException ex) {
                                log.error("Error parsing time: {}", hourStart + ":" + minStart, ex);
                            }
                        }
                    } else if (givenTime[y] > hourStart && givenTime[y] < hourEnd) { //if the time was inside, then it is not available
                        availTime[y] = "";
                    } else if (givenTime[y] == hourEnd) {
                        if (minEnd > 0) { //check if minute did not start at exact o clock
                            try {
                                availTime[y] = FORMAT.format(hour24Format.parse(hourEnd + ":" + minEnd));
                            } catch (ParseException ex) {
                                log.error("Error parsing time: {}", hourEnd + ":" + minEnd, ex);
                            }
                        }
                    }
                }
                log.debug("available time: {}, start: {}", availTime[y], isStart[y]);
            }
        }
        
        //==================get the intervals of available hours====================//
        String timeStartInterval = "";
        String timeEndInterval;
        String timeInterval;
        
        for(int x = 0; x < givenTime.length; x ++) {            
            log.debug("Checking Time: {}", availTime[x]);
            if(!availTime[x].equals("")) {
                if(!isStart[x]) {
                    if(timeStartInterval.equals("")) {
                        timeStartInterval = availTime[x];
                        
                        log.debug("Start Interval: {}", availTime[x]);
                    }
                } else if(!timeStartInterval.equals("")) {
                    timeEndInterval = availTime[x]; //get the last interval because class has start here
                    timeInterval = timeStartInterval + " - " + timeEndInterval; //build the available time interval
                    timeStartInterval = ""; //reset it

                    log.debug("Interval finished. Interval: {}", timeInterval);
                    availableTime.add(timeInterval); //add it to combo box
                }
            }
        }
       
        if (!timeStartInterval.equals("") && !timeStartInterval.equals(tv.getClassEndString())) {
            timeEndInterval = tv.getClassEndString(); //get the last interval because class has start here
            timeInterval = timeStartInterval + " - " + timeEndInterval; //build the available time interval

            log.debug("Interval finished. Interval: {}", timeInterval);
            availableTime.add(timeInterval); //add it to combo box
        }
        
        if(availableTime.isEmpty()) {
            availableTime.add("Not Available");
        }
        
        return availableTime;
    }
    
    /**
     * Returns the section with the same schedule, conflict.
     * @return section Code
     */
    public String getConflictSection() {
        return conflictSection;
    }

    /**
     * sets the section with the same schedule being checked.
     * @param conflictSection section Code of the section with the
     * same schedule.
     */
    private static void setConflictSection(String conflictSection) {
        ConflictScheduleChecker.conflictSection = conflictSection;
    }
    
    /**
     * 
     * @return The first conflicted row.
     */
    private int getRow1(){
        return conflictRow1;
    }
    
    /**
     * 
     * @return the second conflicted row.
     */
    private int getRow2(){
        return conflictRow2;
    }
    
    /**
     * 
     * @param row sets the first conflicted row.
     */
    private void setRow1(int row){
        conflictRow1 = row;
    }
    
    /**
     * 
     * @param row sets the second conflicted row.
     */
    private void setRow2(int row){
        conflictRow2 = row;
    }
}