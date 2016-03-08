/*
 * Copyright © 2015 by Exequiel Egbert V. Ponce - All Rights Reserved.
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.jdesktop.swingx.JXLabel;
import org.slf4j.LoggerFactory;
import tenten.TFrame;
import tenten.TUtilities;

/**
 * Deletes subject from database
 * @author tenten
 */
public class DeleteSubjectForm extends TFrame{
    
    //<editor-fold defaultstate="collapsed" desc="Variables">
    private static final DatabaseConnection DB = DatabaseConnection.getInstance(); //Database actions
    private static final Logger log = (Logger) LoggerFactory.getLogger(DeleteSubjectForm.class);
    
    private static final int FONT_SIZE = 15; //font size widely used in this form
    private static final int LEFT_GAP = 210; //gap of combo boxes, text fields from the left so they are aligned
    private static final int TOP_LEFT_GAP = 20; //gap of components from the top and left
    
    private static final int BUTTON_WIDTH = 150; //width of the buttons
    private static final int BUTTON_HEIGHT = 30; //height of the buttons
    
    private String[] subjCode, subjTemp; //Storage for Subjcode (without null and with null)
    
    private JLabel subjDelLabel;
    private JLabel subjDescLabel;
    private JLabel subjCodeLabel;
    private JLabel unitLabel;
    private JLabel semSchedLabel;
    private JLabel yrLvlLabel;
    private JLabel preReqLabel;
    private JLabel availCourseLabel;
    private JXLabel tipLabel;
    private JComboBox subjDelCombo;
    private JComboBox availCourseCombo;
    private JLabel subjDescLabelCode;
    private JLabel subjCodeLabelCode;
    private JLabel unitLabelCode;
    private JLabel semSchedLabelCode;
    private JLabel yrLvlLabelCode;
    private JLabel preReqLabelCode;
    private JButton delSubjButton;
    //</editor-fold>
    
    public DeleteSubjectForm(int width, int height, int defaultOperation, ImageIcon bg, String title) {
        super(width, height, defaultOperation, bg, title);
        log.setLevel(Level.ALL);
        setIcon("IconsImages/book_delete.png");
    }

    @Override
    public void init() {
        subjDelLabel = new JLabel();
        subjDescLabel = new JLabel();
        subjCodeLabel = new JLabel();
        unitLabel = new JLabel();
        semSchedLabel = new JLabel();
        yrLvlLabel = new JLabel();
        preReqLabel = new JLabel();
        availCourseLabel = new JLabel();
        tipLabel = new JXLabel();
        subjDelCombo = new JComboBox();
        availCourseCombo = new JComboBox();
        subjDescLabelCode = new JLabel();
        subjCodeLabelCode = new JLabel();
        unitLabelCode = new JLabel();
        semSchedLabelCode = new JLabel();
        yrLvlLabelCode = new JLabel();
        preReqLabelCode = new JLabel();
        delSubjButton = new JButton();
        //=========================//
        subjDelLabel.setText("Choose Subject to Delete:");
        subjDelLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(subjDelLabel, FONT_SIZE);
        
        addComponent(subjDelLabel);
        subjDelLabel.setBounds(SIDE_BORDER_WIDTH + TOP_LEFT_GAP, TITLE_BUTTON_HEIGHT + TUtilities.GAP,
                TUtilities.getPrefWidth(subjDelLabel), subjDelLabel.getFont().getSize());
        //=========================//
        subjDelCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                subjDelCombo_actionPerformed();
            }
        });
        TUtilities.setPlainFont(subjDelCombo, FONT_SIZE);
        
        addComponent(subjDelCombo);
        subjDelCombo.setBounds(LEFT_GAP, TUtilities.toMiddleOfLabel(subjDelLabel, subjDelCombo),
                getFrameWidth() - LEFT_GAP - TUtilities.GAP, TUtilities.getPrefHeight(subjDelCombo));
        //=========================//
        subjDescLabel.setText("Subject Description:");
        subjDescLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(subjDescLabel, FONT_SIZE);
        
        addComponent(subjDescLabel);
        subjDescLabel.setBounds(subjDelLabel.getX(), TUtilities.belowOf(subjDelCombo) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(subjDescLabel), subjDescLabel.getFont().getSize());
        //=========================//
        subjDescLabelCode.setForeground(Color.WHITE);
        TUtilities.setPlainFont(subjDescLabelCode, FONT_SIZE);
        
        addComponent(subjDescLabelCode);
        subjDescLabelCode.setBounds(LEFT_GAP, subjDescLabel.getY(),
                getFrameWidth() - LEFT_GAP - TUtilities.GAP, subjDescLabel.getFont().getSize());
        //=========================//
        subjCodeLabel.setText("Subject Code:");
        subjCodeLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(subjCodeLabel, FONT_SIZE);
        
        addComponent(subjCodeLabel);
        subjCodeLabel.setBounds(subjDelLabel.getX(), TUtilities.belowOf(subjDescLabel) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(subjCodeLabel), subjCodeLabel.getFont().getSize());
        //=========================//
        subjCodeLabelCode.setForeground(Color.WHITE);
        TUtilities.setPlainFont(subjCodeLabelCode, FONT_SIZE);
        
        addComponent(subjCodeLabelCode);
        subjCodeLabelCode.setBounds(LEFT_GAP, subjCodeLabel.getY(),
                getFrameWidth() - LEFT_GAP - TUtilities.GAP, subjCodeLabelCode.getFont().getSize());
        //=========================//
        unitLabel.setText("Unit(s)");
        unitLabel.setForeground(Color.WHITE);
        TUtilities.setPlainFont(unitLabel, FONT_SIZE);
        
        addComponent(unitLabel);
        unitLabel.setBounds(subjDelLabel.getX(), TUtilities.belowOf(subjCodeLabel) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(unitLabel), unitLabel.getFont().getSize());
        //=========================//
        unitLabelCode.setForeground(Color.WHITE);
        TUtilities.setPlainFont(unitLabelCode, FONT_SIZE);
        
        addComponent(unitLabelCode);
        unitLabelCode.setBounds(LEFT_GAP, unitLabel.getY(),
                getFrameWidth() - LEFT_GAP - TUtilities.GAP, unitLabelCode.getFont().getSize());
        //=========================//
        semSchedLabel.setText("Semester:");
        semSchedLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(semSchedLabel, FONT_SIZE);
        
        addComponent(semSchedLabel);
        semSchedLabel.setBounds(subjDelLabel.getX(), TUtilities.belowOf(unitLabel) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(semSchedLabel), semSchedLabel.getFont().getSize());
        //=========================//
        semSchedLabelCode.setForeground(Color.WHITE);
        TUtilities.setPlainFont(semSchedLabelCode, FONT_SIZE);
        
        addComponent(semSchedLabelCode);
        semSchedLabelCode.setBounds(LEFT_GAP, semSchedLabel.getY(),
                getFrameWidth() - LEFT_GAP - TUtilities.GAP, semSchedLabelCode.getFont().getSize());
        //=========================//
        yrLvlLabel.setText("Year Level:");
        yrLvlLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(yrLvlLabel, FONT_SIZE);
        
        addComponent(yrLvlLabel);
        yrLvlLabel.setBounds(subjDelLabel.getX(), TUtilities.belowOf(semSchedLabel) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(yrLvlLabel), yrLvlLabel.getFont().getSize());
        //=========================//
        yrLvlLabelCode.setForeground(Color.WHITE);
        TUtilities.setPlainFont(yrLvlLabelCode, FONT_SIZE);
        
        addComponent(yrLvlLabelCode);
        yrLvlLabelCode.setBounds(LEFT_GAP, yrLvlLabel.getY(), 
                getFrameWidth() - LEFT_GAP - TUtilities.GAP, yrLvlLabelCode.getFont().getSize());
        //=========================//
        preReqLabel.setText("Pre-Requisite:");
        preReqLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(preReqLabel, FONT_SIZE);
        
        addComponent(preReqLabel);
        preReqLabel.setBounds(subjDelLabel.getX(), TUtilities.belowOf(yrLvlLabel) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(preReqLabel), preReqLabel.getFont().getSize());
        //=========================//
        preReqLabelCode.setForeground(Color.WHITE);
        TUtilities.setPlainFont(preReqLabelCode, FONT_SIZE);
        
        addComponent(preReqLabelCode);
        preReqLabelCode.setBounds(LEFT_GAP, preReqLabel.getY(),
                getFrameWidth() - LEFT_GAP - TUtilities.GAP, preReqLabelCode.getFont().getSize());
        //=========================//
        availCourseLabel.setText("Available on Courses:");
        availCourseLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(availCourseLabel, FONT_SIZE);
        
        addComponent(availCourseLabel);
        availCourseLabel.setBounds(subjDelLabel.getX(), TUtilities.belowOf(preReqLabel) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(availCourseLabel), availCourseLabel.getFont().getSize());
        //=========================//
        TUtilities.setPlainFont(availCourseCombo, FONT_SIZE);
        
        addComponent(availCourseCombo);
        availCourseCombo.setBounds(LEFT_GAP, TUtilities.toMiddleOfLabel(availCourseLabel, availCourseCombo),
                getFrameWidth() - LEFT_GAP - TUtilities.GAP, TUtilities.getPrefHeight(availCourseCombo));
        //=========================//
        delSubjButton.setText("Delete Subject");
        delSubjButton.setIcon(TUtilities.getIconFromResource(getClass(), "/IconsImages/book_delete.png"));
        delSubjButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                delSubjButton_actionPerformed();
            }
        });
        TUtilities.setPlainFont(delSubjButton, FONT_SIZE);
        
        addComponent(delSubjButton);
        delSubjButton.setBounds(getWidth() - SIDE_BORDER_WIDTH - BUTTON_WIDTH - TOP_LEFT_GAP,
                getHeight() - BOTTOM_BORDER_HEIGHT - BUTTON_HEIGHT - TOP_LEFT_GAP,
                BUTTON_WIDTH, BUTTON_HEIGHT);
        //=========================//
        tipLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(tipLabel, FONT_SIZE);
        
        addComponent(tipLabel);
        tipLabel.setBounds(subjDelLabel.getX(), delSubjButton.getY() + (BUTTON_HEIGHT / 2),
                getFrameWidth() - BUTTON_WIDTH - TOP_LEFT_GAP - subjDelLabel.getX(),
                tipLabel.getFont().getSize());
    }

    @Override
    public void defaultSetup() {
        subjDelCombo.setModel(new DefaultComboBoxModel(getSubjects()));
        if(subjDelCombo.getModel().getSize() > 0){
            subjDelCombo_actionPerformed();
        }
    }
    
    private String[] getSubjects(){
        subjTemp = new String[1]; //declare here to avoid null pointer
        subjTemp[0] = ""; //initally set empty value, array to be shown. (SubjCode + SUbjName)
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
                subjCode = new String[DB.getRecordCount(DB.rs, "Counting subjects") + 1]; //+1 because there's subject who doesnt have prerequisites 
                subjTemp = new String[subjCode.length - 1]; //-1, remove the option of ""
                
                subjCode[0] = ""; //null option
                log.trace(">> Entering Loop, get all subjects for pre requisites");
                for(int x = 1; DB.rs.next(); x++){//start at 1 because subjCode 0 index has "" value
                    subjCode[x] = DB.rs.getString("SubjCode"); //get subjcode only
                    subjTemp[x - 1] = DB.rs.getString("SubjCode") + " " + DB.rs.getString("SubjName"); //get both -1 because this has no "" value on 0 index.
                    
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
     * Loads the subject information on fields
     * @param subjectCode subject to be loaded information.
     */
    private void getSubjInfo(String subjectCode){
        TUtilities.setTip("Loading " + subjectCode + " Information...", tipLabel);
        //===load subject info=====//
        try {
            DB.open("Get Subject Attributes");
            DB.setQuery("SELECT * FROM SubjectsTB WHERE SubjCode=?");
            
            log.debug("Preparing Statement....");
            DB.ps = DB.conn.prepareStatement(DB.getQuery());
            
            DB.ps.setString(1, subjectCode);
            
            log.debug("Executing Statement...");
            DB.rs = DB.ps.executeQuery();
            
            if(DB.rs.next()){
                subjDescLabelCode.setText(DB.rs.getString("SubjName")); //load subject description
                subjCodeLabelCode.setText(DB.rs.getString("SubjCode")); //load subject code
                unitLabelCode.setText(String.valueOf(DB.rs.getInt("Unit"))); //load unit 
                semSchedLabelCode.setText((DB.rs.getInt("SemSched") == 1) ? "1st Semester" : "2nd Semester"); //load semester string
                yrLvlLabelCode.setText(getYearLvlValue(DB.rs.getInt("Yearlvl"))); //load year level (to String)
                log.debug("Pre Requisite: {}", Arrays.asList(subjCode).indexOf(DB.rs.getString("PreRequisites")));
                if(Arrays.asList(subjCode).indexOf(DB.rs.getString("PreRequisites")) > 0){ //if 0, then subject has no pre requisite
                    preReqLabelCode.setText(subjTemp[Arrays.asList(subjCode).indexOf(DB.rs.getString("PreRequisites")) - 1]); //load subject pre prequsites by finding subject code in subjcode array using search index by string. -1 because 0 of subject code is "" while 0 of subjtemp is not ""
                } else {
                    preReqLabelCode.setText("(No Pre-Requisite)");
                }
            }else{
                log.warn("Subject information does not exist?");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            log.error("Error getting subject attributes", ex);
        } finally {
            DB.close();
        }
        //=====load subject available on what courses====//
        try {
            DB.open("Get Subject Attributes");
            DB.setQuery("SELECT CourseCode FROM SubjCourse WHERE SubjCode=?");
            
            log.debug("Preparing Statement....");
            DB.ps = DB.conn.prepareStatement(DB.getQuery());
            
            DB.ps.setString(1, subjectCode);
            
            log.debug("Executing Statement...");
            DB.rs = DB.ps.executeQuery();
            
            availCourseCombo.removeAllItems(); //clear and fill
            
            while(DB.rs.next()) { //adds all courses that the subject are available on.
                availCourseCombo.addItem(DB.rs.getString("CourseCode"));
            }
            
        } catch (ClassNotFoundException | SQLException ex) {
            log.error("Error getting subject attributes", ex);
            TUtilities.setTip("Error Subject Information.", tipLabel);
        } finally {
            DB.close();
        }
        
        TUtilities.setTip("", tipLabel);
    }
    
    /**
     * get Year String value of year level integer.
     * @param yrlvl 1-6
     * @return 1st year - 6th year
     */
    private String getYearLvlValue(int yrlvl){
        String yearval = "";
        
        switch(yrlvl){
            case 1:
                yearval = "1st Year";
                break;
            case 2:
                yearval = "2nd Year";
                break;
            case 3:
                yearval = "3rd Year";
                break;
            case 4:
                yearval = "4th Year";
                break;
            case 5:
                yearval = "5th Year";
                break;
            case 6:
                yearval = "6th Year";
                break;
            default:
                log.error("System is limited to log 6th year courses only."); 
        }
        return yearval;
    }
    
    private void subjDelCombo_actionPerformed(){
        getSubjInfo(subjCode[subjDelCombo.getSelectedIndex() + 1]);
    }
    
    private void delSubjButton_actionPerformed(){
        if(subjDelCombo.getModel().getSize() < 1) {
            TUtilities.setTip("No Subjects Registered.", tipLabel);
            return;
        }
        
        String subjCodeToDelete = subjCode[subjDelCombo.getSelectedIndex() + 1];
        Boolean isError = false;
        
        try {
            DB.open("Check if subject is in use");
            DB.setQuery("SELECT SectionCode FROM SectionSubjTB WHERE SubjCode=?");
            log.debug("Preparing Statement...");
            DB.ps = DB.conn.prepareStatement(DB.getQuery());
            
            DB.ps.setString(1, subjCodeToDelete);
            
            log.debug("Executing Statement..");
            DB.rs = DB.ps.executeQuery();
            
            if(DB.rs.next()) {
                TUtilities.setTip("Subject is used by " + DB.rs.getString("SectionCode"), tipLabel);
                isError = true; //dont return here because database must be close
            }
            
        } catch (SQLException | ClassNotFoundException ex) {
            log.error("Error Checking subject in use", ex);
        } finally {
            DB.close();
        }
        
        if(isError) {
            return;
        }
                
        if(JOptionPane.showConfirmDialog(rootPane, 
                        "Are you sure you want to delete " + subjCodeToDelete + "?",
                        "Delete Subject", JOptionPane.YES_NO_OPTION, 
                        JOptionPane.WARNING_MESSAGE, null) == JOptionPane.NO_OPTION){
            return; //exit if no. continue if yes.
        }
        
        TUtilities.setTip("Deleting Subject: " + subjCodeToDelete, tipLabel);
        try {
            DB.open("Deleting Subject...");
            
            //two statements, because per execute statement, must close before executing again so separate.
            
            //Delete on subjects table first
            DB.setQuery("DELETE * FROM SubjectsTB WHERE SubjCode='" + subjCodeToDelete + "'");
            
            log.debug("Preparing Statement...");
            DB.ps = DB.conn.prepareStatement(DB.getQuery());
            
            log.debug("Executing Statement...");
            DB.ps.executeUpdate();
            
            //Next, Delete on subject course table
            DB.setQuery("DELETE * FROM SubjCourse WHERE SubjCode='" + subjCodeToDelete + "'");
            
            log.debug("Preparing Statement...");
            DB.s = DB.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            log.debug("Executing Statement...");
            DB.s.executeUpdate(DB.getQuery());
            
            defaultSetup(); //reset form
            TUtilities.setTip(subjCodeToDelete + " Successfully Deleted.", tipLabel);
        } catch (ClassNotFoundException | SQLException ex) {
            log.error("Cannot Delete Subject.", ex);
            TUtilities.setTip("Error Deleting Subject.", tipLabel);
        } finally {
            DB.close();
        }
    }
}
