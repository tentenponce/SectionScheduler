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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXList;
import org.slf4j.LoggerFactory;
import tenten.TFrame;
import tenten.TUtilities;

/**
 * This class adds subject to the database properly. It checks
 * whether the subject is already registered.
 * @author tenten
 */
public class AddSubjectForm extends TFrame {

    //<editor-fold defaultstate="collapsed" desc="Variables">
    private static final DatabaseConnection DB = DatabaseConnection.getInstance(); //Database actions
    private static final Logger log = (Logger) LoggerFactory.getLogger(AddSubjectForm.class);
    
    private static final int FONT_SIZE = 15; //font size widely used in this form
    private static final int LEFT_GAP = 210; //gap of combo boxes, text fields from the left so they are aligned
    private static final int TOP_LEFT_GAP = 20; //gap of components from the top and left
    
    private static final int COMBO_WIDTH = 200; //width of the year and sem combo box
    
    private static final int UNIT_FIELD_WIDTH = 45; //width of the unit Field
    
    private static final int LIST_BOX_WIDTH = 180; //width of the list box (avail list, course list)
    private static final int LIST_BOX_HEIGHT = 200; //height of the list box (avail list, course list)
    
    private static final int BUTTON_WIDTH = 130; //width of the buttons
    private static final int BUTTON_HEIGHT = 30; //height of the buttons
    
    DefaultListModel availListModel; //list model of the available on what courses JList
    DefaultListModel courseListModel; //list model of courses on JList
    private String[] subjCode; //assume first that theres no subjects. Storage for Subjcode
    
    private JLabel subjDescLabel;
    private JLabel subjCodeLabel;
    private JLabel unitLabel;
    private JLabel semSchedLabel;
    private JLabel yrLvlLabel;
    private JLabel preReqLabel;
    private JLabel availCourseLabel;
    private JLabel courseListLabel;
    private JXLabel tipLabel;
    private JTextField subjDescField;
    private JTextField subjCodeField;
    private JTextField unitField;
    private JComboBox semSchedCombo;
    private JComboBox yrLvlCombo;
    private JComboBox preReqCombo;
    private JXList availCourseList;
    private JXList courseList;
    private JScrollPane availCourseScroll;
    private JScrollPane courseListScroll;
    private JButton addButton;
    private JButton removeButton;
    private JButton addSubjButton;
    //</editor-fold>
    
    public AddSubjectForm(int width, int height, int defaultOperation, ImageIcon bg, String title) {
        super(width, height, defaultOperation, bg, title);
        log.setLevel(Level.ALL);
        setIcon("IconsImages/book_add.png");
    }

    @Override
    public void init() {
        subjDescLabel = new JLabel();
        subjCodeLabel = new JLabel();
        unitLabel = new JLabel();
        semSchedLabel = new JLabel();
        yrLvlLabel = new JLabel();
        preReqLabel = new JLabel();
        availCourseLabel = new JLabel();
        courseListLabel = new JLabel();
        tipLabel = new JXLabel();
        subjDescField = new JTextField();
        subjCodeField = new JTextField();
        unitField = new JTextField();
        semSchedCombo = new JComboBox();
        yrLvlCombo = new JComboBox();
        preReqCombo = new JComboBox();
        availCourseList = new JXList();
        courseList = new JXList();
        availCourseScroll = new JScrollPane();
        courseListScroll = new JScrollPane();
        addButton = new JButton();
        removeButton = new JButton();
        addSubjButton = new JButton();
        
        availListModel = new DefaultListModel();
        courseListModel = new DefaultListModel();
        //=========================//
        subjDescLabel.setText("Subject Description:");
        subjDescLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(subjDescLabel, FONT_SIZE);
        
        addComponent(subjDescLabel);
        subjDescLabel.setBounds(SIDE_BORDER_WIDTH + TOP_LEFT_GAP, TITLE_BUTTON_HEIGHT + TUtilities.GAP,
                TUtilities.getPrefWidth(subjDescLabel), subjDescLabel.getFont().getSize());
        //=========================//
        subjDescField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e){
                subjDescField_keyTyped();
            }
        });
        TUtilities.setPlainFont(subjDescField, FONT_SIZE);
        
        addComponent(subjDescField);
        subjDescField.setBounds(LEFT_GAP, 
                TUtilities.toMiddleOfLabel(subjDescLabel, subjDescField), 
                getFrameWidth() - LEFT_GAP - TUtilities.GAP,
                TUtilities.getPrefHeight(subjDescField));
        //=========================//
        subjCodeLabel.setText("Subject Code:");
        subjCodeLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(subjCodeLabel, FONT_SIZE);
        
        addComponent(subjCodeLabel);
        subjCodeLabel.setBounds(subjDescLabel.getX(), TUtilities.belowOf(subjDescField) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(subjCodeLabel), subjCodeLabel.getFont().getSize());
        //=========================//
        subjCodeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e){
                subjCodeField_keyTyped();
            }
        });
        TUtilities.setPlainFont(subjCodeField, FONT_SIZE);
        
        addComponent(subjCodeField);
        subjCodeField.setBounds(LEFT_GAP,
                TUtilities.toMiddleOfLabel(subjCodeLabel, subjCodeField),
                getFrameWidth() - LEFT_GAP - TUtilities.GAP,
                TUtilities.getPrefHeight(subjCodeField));
        //=========================//
        unitLabel.setText("Unit(s)");
        unitLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(unitLabel, FONT_SIZE);
        
        addComponent(unitLabel);
        unitLabel.setBounds(subjDescLabel.getX(), TUtilities.belowOf(subjCodeField) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(unitLabel), unitLabel.getFont().getSize());
        //=========================//
        unitField.setDocument(new JTextFieldNumberValidator(1, unitField, tipLabel));
        unitField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e){
                unitField_keyTyped();
            }
        });
        TUtilities.setPlainFont(unitField, FONT_SIZE);
        
        addComponent(unitField);
        unitField.setBounds(LEFT_GAP, TUtilities.toMiddleOfLabel(unitLabel, unitField),
                UNIT_FIELD_WIDTH, TUtilities.getPrefHeight(unitField));
        //=========================//
        semSchedLabel.setText("Semester:");
        semSchedLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(semSchedLabel, FONT_SIZE);
        
        addComponent(semSchedLabel);
        semSchedLabel.setBounds(subjDescLabel.getX(), TUtilities.belowOf(unitField) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(semSchedLabel), semSchedLabel.getFont().getSize());
        //=========================//
        semSchedCombo.setModel(new DefaultComboBoxModel(new String[] {"1st Semester", "2nd Semester"}));
        TUtilities.setPlainFont(semSchedCombo, FONT_SIZE);
        
        addComponent(semSchedCombo);
        semSchedCombo.setBounds(LEFT_GAP, TUtilities.toMiddleOfLabel(semSchedLabel, semSchedCombo),
                COMBO_WIDTH, TUtilities.getPrefHeight(semSchedCombo));
        //=========================//
        yrLvlLabel.setText("Year Level:");
        yrLvlLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(yrLvlLabel, FONT_SIZE);
        
        addComponent(yrLvlLabel);
        yrLvlLabel.setBounds(subjDescLabel.getX(), TUtilities.belowOf(semSchedCombo) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(yrLvlLabel), yrLvlLabel.getFont().getSize());
        //=========================//
        yrLvlCombo.setModel(new DefaultComboBoxModel(new String[] {"1st Year", "2nd Year",
                            "3rd Year", "4th Year", "5th Year", "6th Year"}));
        TUtilities.setPlainFont(yrLvlCombo, FONT_SIZE);
        
        addComponent(yrLvlCombo);
        yrLvlCombo.setBounds(LEFT_GAP, TUtilities.toMiddleOfLabel(yrLvlLabel, yrLvlCombo),
                COMBO_WIDTH, TUtilities.getPrefHeight(yrLvlCombo));
        //=========================//
        preReqLabel.setText("Pre Requisite:");
        preReqLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(preReqLabel, FONT_SIZE);
        
        addComponent(preReqLabel);
        preReqLabel.setBounds(subjDescLabel.getX(), TUtilities.belowOf(yrLvlCombo) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(preReqLabel), preReqLabel.getFont().getSize());
        //=========================//
        preReqCombo.setModel(new DefaultComboBoxModel(getSubjects()));
        TUtilities.setPlainFont(preReqCombo, FONT_SIZE);
        
        addComponent(preReqCombo);
        preReqCombo.setBounds(LEFT_GAP, TUtilities.toMiddleOfLabel(preReqLabel, preReqCombo),
                getFrameWidth() - LEFT_GAP - TUtilities.GAP, TUtilities.getPrefHeight(preReqCombo));
        //=========================//
        availCourseLabel.setText("Available on what Courses:");
        availCourseLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(availCourseLabel, FONT_SIZE);
        
        addComponent(availCourseLabel);
        availCourseLabel.setBounds(((getWidth() / 2) / 2) - (TUtilities.getPrefWidth(availCourseLabel) / 2) + SIDE_BORDER_WIDTH - (BUTTON_WIDTH / 2),
                TUtilities.belowOf(preReqCombo) + TOP_LEFT_GAP, TUtilities.getPrefWidth(availCourseLabel),
                availCourseLabel.getFont().getSize());
        //=========================//
        String[] courses = getCourses();
        for (String course : courses) {
            courseListModel.addElement(course);
        }
        availCourseList.setModel(availListModel); // set their model. for me to add elements or remove from list model.
        TUtilities.setPlainFont(availCourseList, FONT_SIZE);
        //=========================//
        availCourseScroll.setViewportView(availCourseList);
        availCourseScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        addComponent(availCourseScroll);
        availCourseScroll.setBounds((getWidth() / 4) - (LIST_BOX_WIDTH / 2) + SIDE_BORDER_WIDTH - (BUTTON_WIDTH / 2),
                TUtilities.belowOf(availCourseLabel) + TUtilities.GAP, LIST_BOX_WIDTH, LIST_BOX_HEIGHT);
        //=========================//
        courseListLabel.setText("Course List:");
        courseListLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(courseListLabel, FONT_SIZE);
        
        addComponent(courseListLabel);
        courseListLabel.setBounds((getWidth() / 2) + ((getWidth() / 2) / 2) + (BUTTON_WIDTH / 2) - (TUtilities.getPrefWidth(courseListLabel) / 2) - SIDE_BORDER_WIDTH,
                availCourseLabel.getY(), TUtilities.getPrefWidth(courseListLabel), 
                courseListLabel.getFont().getSize());
        //=========================//
        courseList.setModel(courseListModel); //set their model. for me to add elements or remove from list model.
        TUtilities.setPlainFont(courseList, FONT_SIZE);
        //=========================//
        courseListScroll.setViewportView(courseList);
        courseListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        addComponent(courseListScroll);
        courseListScroll.setBounds((getWidth() / 2) + ((getWidth() / 2) / 2) + (BUTTON_WIDTH / 2) - (LIST_BOX_WIDTH / 2) - SIDE_BORDER_WIDTH,
                availCourseScroll.getY(), LIST_BOX_WIDTH, LIST_BOX_HEIGHT);
        //=========================//
        addButton.setText("<< Add");
        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addButton_actionPerformed();
            }
        });
        TUtilities.setPlainFont(addButton, FONT_SIZE);
        
        addComponent(addButton);
        addButton.setBounds((getWidth() / 2) - (BUTTON_WIDTH / 2), availCourseScroll.getY() + TOP_LEFT_GAP, 
                BUTTON_WIDTH, BUTTON_HEIGHT);
        //=========================//
        removeButton.setText(">> Remove");
        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                removeButton_actionPerformed();
            }
        });
        TUtilities.setPlainFont(removeButton, FONT_SIZE);
        
        addComponent(removeButton);
        removeButton.setBounds(addButton.getX(), TUtilities.belowOf(addButton) + TUtilities.GAP,
                BUTTON_WIDTH, BUTTON_HEIGHT);
        //=========================//
        addSubjButton.setText("Add Subject");
        addSubjButton.setIcon(TUtilities.getIconFromResource(getClass(), "/IconsImages/book_add.png"));
        addSubjButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addSubjButton_actionPerformed();
            }
        });
        TUtilities.setPlainFont(addSubjButton, FONT_SIZE);
        
        addComponent(addSubjButton);
        addSubjButton.setBounds(getWidth() - BUTTON_WIDTH - SIDE_BORDER_WIDTH - TOP_LEFT_GAP,
                getHeight() - BOTTOM_BORDER_HEIGHT - BUTTON_HEIGHT - TOP_LEFT_GAP,
                BUTTON_WIDTH, BUTTON_HEIGHT);
        //=========================//
        tipLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(tipLabel, FONT_SIZE);
        
        addComponent(tipLabel);
        tipLabel.setBounds(subjDescLabel.getX(), addSubjButton.getY() + (addSubjButton.getHeight() / 2),
                getFrameWidth() - addSubjButton.getWidth() - TOP_LEFT_GAP
                - subjDescLabel.getX(), tipLabel.getFont().getSize());
    }

    @Override
    public void defaultSetup() {
        
    }
    
    /**
     * Loads all the subjects (code and desc) to an array
     * from database.
     * @return array of subjects (code and desc)
     */
    private String[] getSubjects(){
        String[] subjTemp = {""}; //initally set empty value, array to be shown. (SubjCode + SUbjName)
        subjCode = new String[1]; //declare here because it will throw null pointer exception
        subjCode[0] = ""; //initally set empty value (SubjCode only)
        
        try {
            DB.open("Get Subjects");
            log.debug("Creating normal statement...");
            DB.s = DB.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            DB.setQuery("SELECT SubjCode, SubjName FROM SubjectsTB");
            
            log.debug("Executing Statement...");
            DB.rs = DB.s.executeQuery(DB.getQuery());
            
            if(DB.rs.next()){
                subjCode = new String[DB.getRecordCount(DB.rs, "Counting subjects") + 1]; //+ 1 for no prereq
                subjTemp = new String[subjCode.length];
                
                log.trace(">> Entering Loop, get all subjects for pre requisites");
                subjCode[0] = ""; //Add Option, Empty Pre Requisite
                subjTemp[0] = ""; //add option empty pre req
                for(int x = 1; DB.rs.next(); x++){ //start at 1 because 0 index is no prereq option
                    subjCode[x] = DB.rs.getString("SubjCode"); //get subjcode only
                    subjTemp[x] = DB.rs.getString("SubjCode") + " " + DB.rs.getString("SubjName"); //get both
                    
                    log.debug(subjCode[x]);
                }
                log.trace("<< Loop");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            log.error("Error getting subjects", ex);
        } finally {
            DB.close();
        }    

        return subjTemp;
    }
    
    /**
     * clear selection of both JList (course, availcourse)
     * change background color of availcourse_list to white. (reset errors)
     */
    private void resetLists(){
        courseList.clearSelection();
        availCourseList.clearSelection();
        
        availCourseList.setBackground(Color.WHITE);
    }
    
    /**
     * Gets all of the registered courses from the
     * database.
     * @return Array of courses.
     */
    private String[] getCourses() {
        String[] CourseArray = {"Empty"}; //for it not to return null 
        try {
            DB.open("get course list");
            log.debug("Preparing normal statement...");
            DB.s = DB.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            log.debug("Executing statement...");
            DB.rs = DB.s.executeQuery("SELECT CourseCode FROM CourseTB");
            CourseArray = new String[DB.getRecordCount(DB.rs, "course list")];
            
            log.debug("Entering loop to get courses...");
            for(int counter = 0; DB.rs.next(); counter++) {
                CourseArray[counter] = DB.rs.getString(1);
            }
            log.debug("loop finished.");

        } catch (ClassNotFoundException | SQLException e) {
            log.error("Error in retreiving course list", e);
        } finally {
            DB.close();
        }
        
        log.debug("returning course list count: {}", CourseArray.length);
        return CourseArray;
    }
    
    /**
     * resets the whole form, clears text boxes and lists.
     */
    private void resetFields(){
        subjDescField.setText("");
        subjCodeField.setText("");
        unitField.setText("1");
        semSchedCombo.setSelectedIndex(0);
        yrLvlCombo.setSelectedIndex(0);
        preReqCombo.setSelectedIndex(0);
        
        log.debug("Available Course List Count to be Removed: {}", availCourseList.getElementCount());
        if(availCourseList.getElementCount() > 0){ //there must be atleast 1 element
            availCourseList.setSelectionInterval(0, availCourseList.getElementCount() - 1); //select it all
        }
        
        removeButton.doClick();
    }
    
    /**
     * Converts the chosen semester to integer for proper 
     * and easy query. (1st Semester = 1, 2nd Semester = 2)
     * 
     * @return 1 or 2
     */
    private int getSemSched() {
        int sem;

        if (String.valueOf(semSchedCombo.getSelectedItem()).equals("1st Semester")) {
            sem = 1;
        } else {
            sem = 2;
        }

        return sem;
    }
    
    /**
     * Converts the chosen year level for proper
     * and easy query. (1st Year = 1, 2nd Year = 2...)
     * (Limited to 6th Year ONLY.)
     * 
     * @return number 1-5
     */
    private int getYearLvl() {
        int yrlvl = 0;

        switch (String.valueOf(yrLvlCombo.getSelectedItem())) {
            case "1st Year":
                yrlvl = 1;
                break;
            case "2nd Year":
                yrlvl = 2;
                break;
            case "3rd Year":
                yrlvl = 3;
                break;
            case "4th Year":
                yrlvl = 4;
                break;
            case "5th Year":
                yrlvl = 5;
                break;
            case "6th Year":
                yrlvl = 6;
                break;
            default:
                log.error("System is limited to log 6th year courses only.");
        }

        return yrlvl;
    }
    
    private void subjDescField_keyTyped(){
        subjDescField.setBackground(Color.WHITE);
    }
    
    private void subjCodeField_keyTyped(){
        subjCodeField.setBackground(Color.WHITE);
    }
    
    private void unitField_keyTyped(){
        unitField.setBackground(Color.WHITE);
    }
    
    private void addButton_actionPerformed(){
        int[] selectedIndex = courseList.getSelectedIndices(); //get selected index of course
        
        for(int x = 0; x < selectedIndex.length; x++){
            availListModel.addElement(courseListModel.getElementAt(selectedIndex[x])); //add selected course to available courses
        }
        
        for(int x = selectedIndex.length - 1; x >= 0; x--){ //remove from bottom otherwise it will remove wrong indexes.
            courseListModel.remove(selectedIndex[x]); 
        }
        
        resetLists();
    }
    
    private void removeButton_actionPerformed(){
        int[] selectedIndex = availCourseList.getSelectedIndices(); //get selected index of course
        
        for(int x = 0; x < selectedIndex.length; x++){
            courseListModel.addElement(availListModel.getElementAt(selectedIndex[x])); //put it back on course list
        }
        
        for(int x = selectedIndex.length - 1; x >= 0; x--){ //remove from bottom otherwise it will remove wrong indexes.
            availListModel.remove(selectedIndex[x]);
        }
        
        resetLists();
    }
    
    private void addSubjButton_actionPerformed(){
        boolean isError = false;
        
        if(unitField.getText().equals("") || unitField.getText().equals("0")){
            unitField.setBackground(Color.red);
            TUtilities.setTip("Fill up fields.", tipLabel);
            isError = true;
        }
        
        if((subjDescField.getText().trim()).equals("")){
            subjDescField.setBackground(Color.red);
            TUtilities.setTip("Fill up fields.", tipLabel);
            isError = true;
        }
        
        if((subjCodeField.getText().trim()).equals("")){
            subjCodeField.setBackground(Color.red);
            TUtilities.setTip("Fill up fields.", tipLabel);
            isError = true;
        }
        
        if(availCourseList.getElementCount() == 0){
            availCourseList.setBackground(Color.red);
            TUtilities.setTip("Fill up fields.", tipLabel);
            isError = true;
        }
        
        //============CHECK IF SUBJECT EXISTS================//
        try{
            DB.open("Check duplicate subjects");
            DB.setQuery("SELECT SubjCode FROM SubjectsTB WHERE SubjCode=?");
            
            log.debug("Preparing Statement...");
            DB.ps = DB.conn.prepareStatement(DB.getQuery());
            
            DB.ps.setString(1, subjCodeField.getText().trim().toUpperCase()); //subjcode
            
            log.debug("Executing Statement...");
            DB.rs = DB.ps.executeQuery();
            
            if(DB.rs.next()){ //check if subjcode exists
                isError = true; 
                TUtilities.setTip("Subject Code already registered.", tipLabel);
            }
        } catch (ClassNotFoundException | SQLException ex) {
            log.error("Error checking duplicate subjects", ex);
        } finally {
            DB.close();
        }
        
        
        if(!isError){
            //=============ADD SUBJECT=======================//
            try {
                DB.open("Add " + subjCodeField.getText().trim() + " subject");
                DB.setQuery("INSERT INTO SubjectsTB"
                        + "(SubjCode, SubjName, Unit, SemSched, YearLvl, PreRequisites) "
                        + "VALUES(?,?,?,?,?,?)");
                log.debug("Preparing Statement...");
                DB.ps = DB.conn.prepareStatement(DB.getQuery());
                
                DB.ps.setString(1, subjCodeField.getText().trim().toUpperCase()); //subjcode
                DB.ps.setString(2, subjDescField.getText().trim()); //subj desc
                DB.ps.setInt(3, Integer.parseInt(unitField.getText())); //unit
                DB.ps.setInt(4, getSemSched()); //semsched
                DB.ps.setInt(5, getYearLvl()); // yearlvl
                DB.ps.setString(6, subjCode[preReqCombo.getSelectedIndex() == -1 ? 
                        0 : preReqCombo.getSelectedIndex()]); //pre req, get the index of the selected item from the subjects to get code only.
                
                log.debug("Executing Statement...");
                DB.ps.executeUpdate();
            } catch (ClassNotFoundException | SQLException ex){
                log.error("Cannot add subject", ex);
            } finally {
                DB.close();
            }
            
            //================SET SUBJECT COURSE================//
            try {
                DB.open("Add Subject Course");
                DB.setQuery("INSERT INTO SubjCourse"
                        + "(SubjCode, CourseCode) "
                        + "VALUES(?,?)");
                log.debug("Preparing Statement (Batch)...");
                DB.ps = DB.conn.prepareStatement(DB.getQuery());
                DB.conn.setAutoCommit(false); //for batch
                
                for(int x = 0; x < availCourseList.getElementCount(); x++){
                    DB.ps.setString(1, subjCodeField.getText().trim().toUpperCase()); //subjcode
                    DB.ps.setString(2, String.valueOf(availCourseList.getModel().getElementAt(x))); //course code
                    
                    log.debug("subject is now available on {}", 
                            String.valueOf(availCourseList.getModel().getElementAt(x)));
                    DB.ps.addBatch(); //add to queue (batch)
                }
                
                log.debug("Executing batch...");
                try {
                    DB.ps.executeBatch();
                    
                    log.debug("Commiting insert...");
                    DB.conn.commit();
                } catch (SQLException ex) {
                    log.error("Inserting subject courses failed.", ex);
                    DB.conn.rollback();
                }
                
                
            } catch (ClassNotFoundException | SQLException ex){
                log.error("Cannot add subject", ex);
            } finally {
                DB.close();
            }
            
            String subjCodeTemp = subjCodeField.getText().trim().toUpperCase(); //pass it here because it will be reset
            resetFields(); //reset all
            TUtilities.setTip(subjCodeTemp + " Successfully Added.", tipLabel);
        }
    }
}
