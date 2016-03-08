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
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import org.slf4j.LoggerFactory;
import tenten.TFrame;
import tenten.TUtilities;

/**
 * Check for room availability per day.
 * @author tenten
 */
public class RoomAvailableForm extends TFrame{

    //<editor-fold defaultstate="collapsed" desc="Variables">
    private static final DatabaseConnection DB = DatabaseConnection.getInstance(); //Database actions
    private static final Logger LOG = (Logger) LoggerFactory.getLogger(RoomSchedForm.class);
    private static final ConflictScheduleChecker CONFLICT_SCHEDULE_CHECKER = new ConflictScheduleChecker();
    
    private static final int SUN_COL = 0;
    private static final int MON_COL = 1;
    private static final int TUE_COL = 2;
    private static final int WED_COL = 3;
    private static final int THU_COL = 4;
    private static final int FRI_COL = 5;
    private static final int SAT_COL = 6;
    
    private static final int FONT_SIZE = 15; //font size most used for this form
    public static final int TOP_LEFT_GAP = 20; //gap of components from the top and left
    private static final int LEFT_GAP = 210; //gap of combo boxes, text fields from the left so they are aligned
    
    private static final int ROOM_WIDTH = 130; //width of the room combo box
    
    private static final int BUTTON_WIDTH = 210; //width of the button
    private static final int BUTTON_HEIGHT = 30; //height of the button
    
    public static final int LIST_WIDTH = 140; //width of the jlists
    
    private static DefaultListModel sunModel;
    private static DefaultListModel monModel;
    private static DefaultListModel tueModel;
    private static DefaultListModel wedModel;
    private static DefaultListModel thuModel;
    private static DefaultListModel friModel;
    private static DefaultListModel satModel;
    
    private JLabel roomLabel;
    private JLabel roomAvailSchedLabel;
    private JLabel sunLabel;
    private JLabel monLabel;
    private JLabel tueLabel;
    private JLabel wedLabel;
    private JLabel thuLabel;
    private JLabel friLabel;
    private JLabel satLabel;
    private JLabel tipLabel;
    private JComboBox roomCombo;
    private JList sunList;
    private JList monList;
    private JList tueList;
    private JList wedList;
    private JList thuList;
    private JList friList;
    private JList satList;
    private JScrollPane sunScroll;
    private JScrollPane monScroll;
    private JScrollPane tueScroll;
    private JScrollPane wedScroll;
    private JScrollPane thuScroll;
    private JScrollPane friScroll;
    private JScrollPane satScroll;
    private JButton checkAvailButton;
    //</editor-fold>
    
    public RoomAvailableForm(int width, int height, int defaultOperation, ImageIcon bg, String title) {
        super(width, height, defaultOperation, bg, title);
        LOG.setLevel(Level.ALL);
        setIcon("IconsImages/calendar.png");
    }

    @Override
    public void init() {
        roomLabel = new JLabel();
        roomAvailSchedLabel = new JLabel();
        sunLabel = new JLabel();
        monLabel  = new JLabel();
        tueLabel = new JLabel();
        wedLabel  = new JLabel();
        thuLabel = new JLabel();
        friLabel = new JLabel();
        satLabel = new JLabel();
        tipLabel = new JLabel();
        roomCombo = new JComboBox();
        sunList = new JList();
        monList = new JList();
        tueList = new JList();
        wedList = new JList();
        thuList = new JList();
        friList = new JList();
        satList = new JList();
        sunScroll = new JScrollPane();
        monScroll = new JScrollPane();
        tueScroll = new JScrollPane();
        wedScroll = new JScrollPane();
        thuScroll = new JScrollPane();
        friScroll = new JScrollPane();
        satScroll = new JScrollPane();
        checkAvailButton = new JButton();
        sunModel = new DefaultListModel();
        monModel = new DefaultListModel();
        tueModel = new DefaultListModel();
        wedModel = new DefaultListModel();
        thuModel = new DefaultListModel();
        friModel = new DefaultListModel();
        satModel = new DefaultListModel();
        //=========================//
        roomLabel.setText("Room(s):");
        roomLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(roomLabel, FONT_SIZE);
        
        addComponent(roomLabel);
        roomLabel.setBounds(SIDE_BORDER_WIDTH + TOP_LEFT_GAP, TITLE_BUTTON_HEIGHT + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(roomLabel), roomLabel.getFont().getSize());
        //=========================//
        TUtilities.setPlainFont(roomCombo, FONT_SIZE);
        
        addComponent(roomCombo);
        roomCombo.setBounds(LEFT_GAP, TUtilities.toMiddleOfLabel(roomLabel, roomCombo),
                ROOM_WIDTH, TUtilities.getPrefHeight(roomCombo));
        //=========================//
        roomAvailSchedLabel.setText("Room Available Schedule:");
        roomAvailSchedLabel.setForeground(Color.WHITE);
        roomAvailSchedLabel.setHorizontalAlignment(JLabel.CENTER);
        TUtilities.setBoldFont(roomAvailSchedLabel, FONT_SIZE);
        
        addComponent(roomAvailSchedLabel);
        roomAvailSchedLabel.setBounds(roomLabel.getX(), TUtilities.belowOf(roomCombo) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(roomAvailSchedLabel), roomAvailSchedLabel.getFont().getSize());
        //=========================//
        sunList.setModel(sunModel);
        sunList.setEnabled(true);
        TUtilities.setPlainFont(sunList, FONT_SIZE);
        //=========================//
        sunScroll.setViewportView(sunList);
        
        addComponent(sunScroll);
        sunScroll.setBounds(roomLabel.getX(), 
                TUtilities.belowOf(roomAvailSchedLabel) + TOP_LEFT_GAP + FONT_SIZE + TOP_LEFT_GAP, LIST_WIDTH, 
                getFrameHeight() - TUtilities.belowOf(roomAvailSchedLabel) - (TOP_LEFT_GAP * 4) - (FONT_SIZE * 2));
        //=========================//
        sunLabel.setText("Sunday");
        sunLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(sunLabel, FONT_SIZE);
        
        addComponent(sunLabel);
        sunLabel.setBounds(TUtilities.toRightOf(sunScroll) - (LIST_WIDTH / 2) - (TUtilities.getPrefWidth(sunLabel) / 2), 
                TUtilities.belowOf(roomAvailSchedLabel) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(sunLabel), sunLabel.getFont().getSize());
        //=========================//
        monList.setModel(monModel);
        monList.setEnabled(true);
        TUtilities.setPlainFont(monList, FONT_SIZE);
        //=========================//
        monScroll.setViewportView(monList);
        
        addComponent(monScroll);
        monScroll.setBounds(TUtilities.toRightOf(sunScroll), sunScroll.getY(), LIST_WIDTH,
                sunScroll.getHeight());
        //=========================//
        monLabel.setText("Monday");
        monLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(monLabel, FONT_SIZE);
        
        addComponent(monLabel);
        monLabel.setBounds(TUtilities.toRightOf(monScroll) - (LIST_WIDTH / 2) - (TUtilities.getPrefWidth(monLabel) / 2),
                sunLabel.getY(), TUtilities.getPrefWidth(monLabel), monLabel.getFont().getSize());
        //=========================//
        tueList.setModel(tueModel);
        tueList.setEnabled(true);
        TUtilities.setPlainFont(tueList, FONT_SIZE);
        //=========================//
        tueScroll.setViewportView(tueList);
        
        addComponent(tueScroll);
        tueScroll.setBounds(TUtilities.toRightOf(monScroll), sunScroll.getY(), LIST_WIDTH,
                sunScroll.getHeight());
        //=========================//
        tueLabel.setText("Tuesday");
        tueLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(tueLabel, FONT_SIZE);
        
        addComponent(tueLabel);
        tueLabel.setBounds(TUtilities.toRightOf(tueScroll) - (LIST_WIDTH / 2) - (TUtilities.getPrefWidth(tueLabel) / 2),
                sunLabel.getY(), TUtilities.getPrefWidth(tueLabel), tueLabel.getFont().getSize());
        //=========================//
        wedList.setModel(wedModel);
        wedList.setEnabled(true);
        TUtilities.setPlainFont(wedList, FONT_SIZE);
        //=========================//
        wedScroll.setViewportView(wedList);
        
        addComponent(wedScroll);
        wedScroll.setBounds(TUtilities.toRightOf(tueScroll), sunScroll.getY(), LIST_WIDTH, 
                sunScroll.getHeight());
        //=========================//
        wedLabel.setText("Wednesday");
        wedLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(wedLabel, FONT_SIZE);
        
        addComponent(wedLabel);
        wedLabel.setBounds(TUtilities.toRightOf(wedScroll) - (LIST_WIDTH / 2) - (TUtilities.getPrefWidth(wedLabel) / 2),
                sunLabel.getY(), TUtilities.getPrefWidth(wedLabel), wedLabel.getFont().getSize());
        //=========================//
        thuList.setModel(thuModel);
        thuList.setEnabled(true);
        TUtilities.setPlainFont(thuList, FONT_SIZE);
        //=========================//
        thuScroll.setViewportView(thuList);
        
        addComponent(thuScroll);
        thuScroll.setBounds(TUtilities.toRightOf(wedScroll), sunScroll.getY(), LIST_WIDTH,
                sunScroll.getHeight());
        //=========================//
        thuLabel.setText("Thursday");
        thuLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(thuLabel, FONT_SIZE);
        
        addComponent(thuLabel);
        thuLabel.setBounds(TUtilities.toRightOf(thuScroll) - (LIST_WIDTH / 2) - (TUtilities.getPrefWidth(thuLabel) / 2),
                sunLabel.getY(), TUtilities.getPrefWidth(thuLabel), thuLabel.getFont().getSize());
        //=========================//
        friList.setModel(friModel);
        friList.setEnabled(true);
        TUtilities.setPlainFont(friList, FONT_SIZE);
        //=========================//
        friScroll.setViewportView(friList);
        
        addComponent(friScroll);
        friScroll.setBounds(TUtilities.toRightOf(thuScroll), sunScroll.getY(), LIST_WIDTH,
                sunScroll.getHeight());
        //=========================//
        friLabel.setText("Friday");
        friLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(friLabel, FONT_SIZE);
        
        addComponent(friLabel);
        friLabel.setBounds(TUtilities.toRightOf(friScroll) - (LIST_WIDTH / 2) - (TUtilities.getPrefWidth(friLabel)/ 2),
                sunLabel.getY(), TUtilities.getPrefWidth(friLabel), friLabel.getFont().getSize());
        //=========================//
        satList.setModel(satModel);
        satList.setEnabled(true);
        TUtilities.setPlainFont(satList, FONT_SIZE);
        //=========================//
        satScroll.setViewportView(satList);
        
        addComponent(satScroll);
        satScroll.setBounds(TUtilities.toRightOf(friScroll), sunScroll.getY(), LIST_WIDTH,
                sunScroll.getHeight());
        //=========================//
        satLabel.setText("Saturday");
        satLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(satLabel, FONT_SIZE);
        
        addComponent(satLabel);
        satLabel.setBounds(TUtilities.toRightOf(satScroll) - (LIST_WIDTH / 2) - (TUtilities.getPrefWidth(satLabel) / 2),
                sunLabel.getY(), TUtilities.getPrefWidth(satLabel), satLabel.getFont().getSize());
        //=========================//
        checkAvailButton.setText("Check Available Schedule");
        checkAvailButton.setIcon(TUtilities.getIconFromResource(getClass(), "/IconsImages/application_form_magnify.png"));
        checkAvailButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                checkAvailButton_actionPerformed();
            }
        });
        TUtilities.setPlainFont(checkAvailButton, FONT_SIZE);
        
        addComponent(checkAvailButton);
        checkAvailButton.setBounds(getWidth() - SIDE_BORDER_WIDTH - TOP_LEFT_GAP - BUTTON_WIDTH,
                roomCombo.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
        //=========================//
        tipLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(tipLabel, FONT_SIZE);
        
        addComponent(tipLabel);
        tipLabel.setBounds(roomLabel.getX(), TUtilities.belowOf(sunScroll) + TOP_LEFT_GAP,
                getFrameWidth() - (TOP_LEFT_GAP * 2), tipLabel.getFont().getSize());
    }

    @Override
    public void defaultSetup() {
        roomCombo.setModel(new DefaultComboBoxModel(getRooms()));
    }
    
    private String[] getRooms() {
        String[] roomsTemp = {""};
        try {
            DB.open("Get registered rooms");
            LOG.debug("Preparing normal statement...");
            DB.s = DB.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            LOG.debug("Executing statement...");
            DB.rs = DB.s.executeQuery("SELECT RoomCode, DepartmentCode FROM RoomsTB");

            roomsTemp = new String[DB.getRecordCount(DB.rs, "Rooms")];
            LOG.debug("Rooms counted: {}", roomsTemp.length);
            
            for(int x = 0; DB.rs.next(); x++) {
                roomsTemp[x] = DB.rs.getString(1);
            }

        } catch (ClassNotFoundException | SQLException ex) {
            LOG.error("Error retreiving room list", ex);
        } finally {
            DB.close();
        }
        
        return roomsTemp;
    }
    
    private void checkAvailButton_actionPerformed(){
        //clear the list boxes
        sunModel.clear();
        monModel.clear();
        tueModel.clear();
        wedModel.clear();
        thuModel.clear();
        friModel.clear();
        satModel.clear();
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        checkAvailButton.setEnabled(false);
        
        new SwingWorker(){
            @Override
            protected Void doInBackground(){
                TUtilities.setTip("Loading Sunday Schedule...", tipLabel);
                ArrayList<String> sunArray = CONFLICT_SCHEDULE_CHECKER.getAvailableHours(TUtilities.getComboValue(roomCombo), "Sunday");
                for (int x = 0; x < sunArray.size(); x++) {
                    sunModel.addElement(sunArray.get(x));
                }
                
                TUtilities.setTip("Loading Monday Schedule...", tipLabel);
                ArrayList<String> monArray = CONFLICT_SCHEDULE_CHECKER.getAvailableHours(TUtilities.getComboValue(roomCombo), "Monday");
                for (int x = 0; x < monArray.size(); x++) {
                    monModel.addElement(monArray.get(x));
                }
                
                TUtilities.setTip("Loading Tuesday Schedule...", tipLabel);
                ArrayList<String> tueArray = CONFLICT_SCHEDULE_CHECKER.getAvailableHours(TUtilities.getComboValue(roomCombo), "Tuesday");
                for (int x = 0; x < tueArray.size(); x++) {
                    tueModel.addElement(tueArray.get(x));
                }

                TUtilities.setTip("Loading Wednesday Schedule...", tipLabel);
                ArrayList<String> wedArray = CONFLICT_SCHEDULE_CHECKER.getAvailableHours(TUtilities.getComboValue(roomCombo), "Wednesday");
                for (int x = 0; x < wedArray.size(); x++) {
                    wedModel.addElement(wedArray.get(x));
                }

                TUtilities.setTip("Loading Thursday Schedule...", tipLabel);
                ArrayList<String> thuArray = CONFLICT_SCHEDULE_CHECKER.getAvailableHours(TUtilities.getComboValue(roomCombo), "Thursday");
                for (int x = 0; x < thuArray.size(); x++) {
                    thuModel.addElement(thuArray.get(x));
                }

                TUtilities.setTip("Loading Friday Schedule...", tipLabel);
                ArrayList<String> friArray = CONFLICT_SCHEDULE_CHECKER.getAvailableHours(TUtilities.getComboValue(roomCombo), "Friday");
                for (int x = 0; x < friArray.size(); x++) {
                    friModel.addElement(friArray.get(x));
                }

                TUtilities.setTip("Loading Saturday Schedule...", tipLabel);
                ArrayList<String> satArray = CONFLICT_SCHEDULE_CHECKER.getAvailableHours(TUtilities.getComboValue(roomCombo), "Saturday");
                for (int x = 0; x < satArray.size(); x++) {
                    satModel.addElement(satArray.get(x));
                }
                return null;
            }

            @Override
            public void done() {
                setCursor(null);
                checkAvailButton.setEnabled(true);
                roomAvailSchedLabel.setText("Room Available Schedule: " + TUtilities.getComboValue(roomCombo));
                roomAvailSchedLabel.setBounds(roomAvailSchedLabel.getX(), roomAvailSchedLabel.getY(),
                        TUtilities.getPrefWidth(roomAvailSchedLabel), roomAvailSchedLabel.getHeight());
                TUtilities.setTip("", tipLabel);
            }
        }.execute();
    }
}
