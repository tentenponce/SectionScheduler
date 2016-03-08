/*
 * Copyright © 2015 by Tenten Ponce - All Rights Reserved.
 * This project or any portion thereof may not be reproduced
 * or used in any manner, without the prior permission 
 * from the owner.
 */
package sectionscheduler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.awt.Color;
import java.awt.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTable;
import org.slf4j.LoggerFactory;

/**
 * <H3>Validates input if it is a time or not.</H3>
 * This class validates the input time then it sends information on a JLabel
 * (if error), and if it is valid, it converts it now to a date and format it 
 * to a short time. (Example: 10:00 AM)
 * 
 * @author Tenten Ponce
 * @version 1.0
 * @since October 2015
 */
public class TimeValidator extends DefaultCellEditor {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("hh:mm a"); // used for formatting and parsing
    private static final Calendar CAL = Calendar.getInstance(); //to disassemble time like getting hours, minutes etc.
    private static final DatabaseConnection DB = DatabaseConnection.getInstance(); //Database actions
    private static final Logger log = (Logger) LoggerFactory.getLogger(TimeValidator.class); //log purposes

    private Date value; //temporarily holds the date as DATE
    private String cellval; //temporarily holds the date as STRING
    
    private int row; //row that is being edited
    private int columnNumber; //column that is being edited
    
    private JXLabel tip_lbl;
    private JXTable sched_tbl;
    
    private int CLASS_START; //hours
    private int CLASS_END;
    
    private int CLASS_START_MIN; //minutes
    private int CLASS_END_MIN;

    private String CLASS_START_TIME; //formatted time
    private String CLASS_END_TIME;
    
    
    public TimeValidator(JTextField jt){ //empty constructor, for set and get class hours
        super(jt);
    }

    public TimeValidator(JTextField textField, int columnNumber, JXLabel tip, JXTable jt) {
        super(textField);
        log.setLevel(Level.ALL); //all leveeeelsss.
        this.columnNumber = columnNumber;
        this.tip_lbl = tip;
        this.sched_tbl = jt;
        
        setClassHour(); //to set the class hours to start and end from.
    }
    
    /**
     * get and sets the interval of class hour.
     */
    public void setClassHour() {
        try {
            DB.open("Get Class Hours");
            log.debug("Preparing Statement...");
            DB.s = DB.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            log.debug("Executing Statement...");
            DB.rs = DB.s.executeQuery("SELECT * FROM ClassHours");
            if (DB.rs.next()) { //must be ALWAYS true, database must have initial set of class hours.
                Timestamp timestamp = DB.rs.getTimestamp(1);
                setClassStart(new java.util.Date(timestamp.getTime()));

                timestamp = DB.rs.getTimestamp(2);
                setClassEnd(new java.util.Date(timestamp.getTime()));
            }
        } catch (ClassNotFoundException | SQLException ex) {
            log.error("Error retreiving class hours", ex);
        } finally {
            DB.close();
        }
    }

    /**
     * Disassemble Class Start Time from database.
     * Gets the hour and minutes and set it to a variable.
     * @param t Time to be Disassembled (FROM DATABASE).
     */
    private void setClassStart(Date t) {
        CAL.setTime(t);
        CLASS_START = CAL.get(Calendar.HOUR_OF_DAY); //get 24 hour (00-23)
        CLASS_START_MIN = CAL.get(Calendar.MINUTE); // get minutes
        CLASS_START_TIME = FORMAT.format(t); //format it to hh:mm am/pm
        log.debug("Class Start: {}", CLASS_START_TIME);
    }

    /**
     * Disassemble Class End Time from database.
     * Gets the hour and minutes and set it to a variable.
     * @param t Time to be Disassembled (FROM DATABASE).
     */
    private void setClassEnd(Date t) {
        CAL.setTime(t);
        CLASS_END = CAL.get(Calendar.HOUR_OF_DAY); //get 24 hour (00-23)
        CLASS_END_MIN = CAL.get(Calendar.MINUTE); // get minutes
        CLASS_END_TIME = FORMAT.format(t); //format it to hh:mm am/pm
        log.debug("Class End: {}", CLASS_END_TIME);
    }

    /**
     * Returns Class Start Hour
     * @return 0-23
     */
    public int getClassStart() {
        return CLASS_START;
    }

    /**
     * Returns Class End Hour
     * @return 0-23
     */
    public int getClassEnd() {
        return CLASS_END;
    }
    
    /**
     * Returns Class Start Minute
     * @return 00-59
     */
    public int getCLASS_START_MIN() {
        return CLASS_START_MIN;
    }

    /**
     * Returns Class End Minute
     * @return 00-59
     */
    public int getCLASS_END_MIN() {
        return CLASS_END_MIN;
    }

    /**
     * Returns Class Start Time. (Information Purposes)
     * @return hh:mm am/pm
     */
    public String getClassStartString() {
        return CLASS_START_TIME;
    }

    /**
     * Returns Class End Time. (Information Purposes)
     * @return hh:mm am/pm
     */
    public String getClassEndString() {
        return CLASS_END_TIME;
    }
    
    /**
     * Checks input time if it was inside of the Class Hours.
     * 
     * @param d Date to be check if it was inside class hours
     * @return true if it was valid/classHour or false if not.
     */
    private boolean isClassHours(Date d) {
        CAL.setTime(d);
        int hour = CAL.get(Calendar.HOUR_OF_DAY);
        int min = CAL.get(Calendar.MINUTE);
        log.debug("Checking if it is class hour: {}", FORMAT.format(d));
        if (hour < getClassStart() || hour > getClassEnd() || //check if time input is inside class hours
                (hour == getClassStart() && min < getCLASS_START_MIN()) || //check if hour is equal and minutes is less than class start minute
                (hour == getClassEnd() && min > getCLASS_END_MIN())) { //check if hour is equal and minutes is greater than class end minute
            ((JComponent) getComponent()).setBorder(new LineBorder(Color.red));
            setTip("Class hours starts at " + getClassStartString() + " and ends at "
                    + getClassEndString());
            return false;
        }
        
        log.debug("Time is inside class hour.");
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        cellval = (String) super.getCellEditorValue(); //get the cell value temporarily.

        if ("".equals(cellval)) {
            cellval = null; // make cell null.
            return super.stopCellEditing(); // return to not parse
        }

        try {
            value = (Date) FORMAT.parse(cellval); // check cellvalue if time.
            cellval = FORMAT.format(value); //format it properly if it can be parsed. hour:minute AM/PM

            //checks if it is inside of class hours.  
            if (!isClassHours(value)) {
                return false;
            }

            //check what column
            if (columnNumber == 2) { //time start
                if (sched_tbl.getModel().getValueAt(row, 3) != null) { //check time end if not null and validate if it has value.
                    boolean datevalid = isDateValid(cellval, (String) sched_tbl.getModel().getValueAt(row, 3)); //check time validity

                    if (!datevalid) {
                        ((JComponent) getComponent()).setBorder(new LineBorder(Color.red));
                        return false;
                    }
                } else { //if time end is null, enter time to help end user. (default 1 hour interval)
                    CAL.setTime(FORMAT.parse(cellval)); //parse the PROPER FORMAT to date again.
                    CAL.add(Calendar.HOUR, 1); //increment time start with 1 hour (to help/suggestion)

                    //checks if it is inside of class hours.  
                    if (!isClassHours(CAL.getTime())) {
                        return false;
                    }

                    log.info("Suggesting Time for Time End: {}", FORMAT.format(CAL.getTime()));
                    sched_tbl.getModel().setValueAt(FORMAT.format(CAL.getTime()), row, 3); //set the incremented hour.

                }
            } else { //time end
                if (sched_tbl.getModel().getValueAt(row, 2) != null) { //check time start if not null and validate if it has value.
                    boolean datevalid = isDateValid((String) sched_tbl.getModel().getValueAt(row, 2), cellval); //check time validity.

                    if (!datevalid) {
                        ((JComponent) getComponent()).setBorder(new LineBorder(Color.red));
                        return false;
                    }
                } else { //if time start is null, enter time to help end user (default 1 hour interval).
                    CAL.setTime(FORMAT.parse(cellval)); //parse the PROPER FORMAT to date again.
                    CAL.add(Calendar.HOUR, -1); //decrement time start with 1 hour (to help/suggestion)

                    //checks if it is inside of class hours.  
                    if (!isClassHours(CAL.getTime())) {
                        return false;
                    }

                    log.info("Suggesting Time for Time Start: {}", FORMAT.format(CAL.getTime()));
                    sched_tbl.getModel().setValueAt(FORMAT.format(CAL.getTime()), row, 2); //set the decremented hour.
                }
            }
        } catch (ParseException e) { //if time cannot be parsed.
            ((JComponent) getComponent()).setBorder(new LineBorder(Color.red));
            setTip("Time Format Example: 10:00 AM, 10:0 AM"); 
            return false; //so the user cant move on
        }
        return super.stopCellEditing();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        ((JComponent) getComponent()).setBorder(new LineBorder(Color.black));
        this.row = row; //to get the exact cell being edit
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    @Override
    public Object getCellEditorValue() {
        setTip(""); //reset tip if date is valid.
        return cellval;
    }

    /**
     * Validates input time.
     * Checks time if Time End is bigger.
     * Checks time if class duration is more than 3 hours.
     * Checks time if class duration is less than 1 hour.
     * @param from Time Start
     * @param to Time End
     * @return true if valid, false if not.
     */
    public boolean isDateValid(String from, String to) {
        if (!from.equals("") && !to.equals("")) { //check first if null
            int fromhour = 0;
            int frommin = 0;
            int tohour = 0;
            int tomin = 0;
            try {
                CAL.setTime(FORMAT.parse(from));
                fromhour = CAL.get(Calendar.HOUR_OF_DAY); //get 24 format
                frommin = CAL.get(Calendar.MINUTE); //get minute

                CAL.setTime(FORMAT.parse(to));
                tohour = CAL.get(Calendar.HOUR_OF_DAY); //get 24 format
                tomin = CAL.get(Calendar.MINUTE); //get minute
            } catch (ParseException ex) {
            }

            int gap = tohour - fromhour; //get the gap between the time.
            if (gap < 0) { //if time start is bigger than time end
                setTip("Time Start must be smaller than Time End.");
                return false;
            }

            if (gap > 3 || (gap == 3 && (tomin > frommin))) { //if gap is bigger than mountain. (3 hours hehe.)
                setTip("Class hour cannot exceed three(3) hours.");
                return false;
            }

            if ((gap == 1 && (frommin - tomin) > 0) || gap == 0) { //if less than an hour. (59 mins gap below is prohibited).
                setTip("Class hours minimum is one(1) hour.");
                return false;
            }
        }
        return true;
    }
    
    /**
     * Provides information to the end user and also for the debugger/developer.
     * @param tip Information to be displayed.
     */
    public void setTip(String tip) {
        tip_lbl.setText(tip);
        if(!"".equals(tip)){
            log.info(tip);
        }
    }
}