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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.JXTable;
import org.slf4j.LoggerFactory;
import tenten.TFrame;
import tenten.TUtilities;

/**
 * Displays the detailed schedule of each rooms.
 * @author tenten
 */
public class RoomSchedForm extends TFrame{

    //<editor-fold defaultstate="collapsed" desc="Variables">
    private static final DatabaseConnection DB = DatabaseConnection.getInstance(); //Database actions
    private static final Logger LOG = (Logger) LoggerFactory.getLogger(RoomSchedForm.class);
    
    private static final int FONT_SIZE = 15; //font size most used for this form
    private static final int TOP_LEFT_GAP = 20; //gap of components from the top and left
    private static final int LEFT_GAP = 210; //gap of combo boxes, text fields from the left so they are aligned

    private static final int ROOM_WIDTH = 130; //width of the room combo box
    
    private static final int BUTTON_WIDTH = 130; //width of the buttons
    private static final int BUTTON_HEIGHT = 30; //height of the buttons
    
    private static final String[] ROOM_SCHED_COL = {"Day", "Time Start", "Time End"}; //column for room schedule
    private static String[][] roomSched = {}; //datas for room schedule
        
    private JLabel roomLabel;
    private JLabel roomDeptLabel;
    private JLabel roomFloorLabel;
    private JLabel tipLabel;
    private JComboBox roomCombo;
    private JXTable roomSchedTable;
    private JScrollPane roomSchedScroll;
    private JLabel roomDeptLabelCode;
    private JLabel roomFloorLabelCode;
    private JButton addRoomButton;
    private JButton editRoomButton;
    //</editor-fold>
    
    public RoomSchedForm(int width, int height, int defaultOperation, ImageIcon bg, String title) {
        super(width, height, defaultOperation, bg, title);
        LOG.setLevel(Level.ALL);
        setIcon("IconsImages/calendar.png");
    }

    @Override
    public void init() {
        roomLabel = new JLabel();
        roomDeptLabel = new JLabel();
        roomFloorLabel = new JLabel();
        tipLabel = new JLabel();
        roomCombo = new JComboBox();
        roomSchedTable = new JXTable();
        roomSchedScroll = new JScrollPane();
        roomDeptLabelCode = new JLabel();
        roomFloorLabelCode = new JLabel();
        addRoomButton = new JButton();
        editRoomButton = new JButton();
        //=========================//
        roomLabel.setText("Room(s):");
        roomLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(roomLabel, FONT_SIZE);
        
        addComponent(roomLabel);
        roomLabel.setBounds(SIDE_BORDER_WIDTH + TOP_LEFT_GAP, TITLE_BUTTON_HEIGHT + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(roomLabel), roomLabel.getFont().getSize());
        //=========================//
        roomCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                roomCombo_actionPerformed();
            }
        });
        TUtilities.setPlainFont(roomCombo, FONT_SIZE);
        
        addComponent(roomCombo);
        roomCombo.setBounds(LEFT_GAP, TUtilities.toMiddleOfLabel(roomLabel, roomCombo),
                ROOM_WIDTH, TUtilities.getPrefHeight(roomCombo));
        //=========================//
        roomDeptLabel.setText("Room Department:");
        roomDeptLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(roomDeptLabel, FONT_SIZE);
        
        addComponent(roomDeptLabel);
        roomDeptLabel.setBounds(roomLabel.getX(), TUtilities.belowOf(roomCombo) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(roomDeptLabel), roomDeptLabel.getFont().getSize());
        //=========================//
        roomDeptLabelCode.setForeground(Color.WHITE);
        TUtilities.setPlainFont(roomDeptLabelCode, FONT_SIZE);
        
        addComponent(roomDeptLabelCode);
        roomDeptLabelCode.setBounds(LEFT_GAP, roomDeptLabel.getY(),
                getFrameWidth() - LEFT_GAP - TUtilities.GAP, roomDeptLabelCode.getFont().getSize());
        //=========================//
        roomFloorLabel.setText("Floor Level:");
        roomFloorLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(roomFloorLabel, FONT_SIZE);
        
        addComponent(roomFloorLabel);
        roomFloorLabel.setBounds(roomLabel.getX(), TUtilities.belowOf(roomDeptLabel) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(roomFloorLabel), roomFloorLabel.getFont().getSize());
        //=========================//
        roomFloorLabelCode.setForeground(Color.WHITE);
        TUtilities.setPlainFont(roomFloorLabelCode, FONT_SIZE);
        
        addComponent(roomFloorLabelCode);
        roomFloorLabelCode.setBounds(LEFT_GAP, roomFloorLabel.getY(),
                getFrameWidth() - LEFT_GAP - TUtilities.GAP, roomFloorLabelCode.getFont().getSize());
        //=========================//
        editRoomButton.setText("Edit Room");
        editRoomButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                editRoomButton_actionPerformed();
            }
        });
        TUtilities.setPlainFont(editRoomButton, FONT_SIZE);
        
        addComponent(editRoomButton);
        editRoomButton.setBounds(getWidth() - BUTTON_WIDTH - TOP_LEFT_GAP - SIDE_BORDER_WIDTH,
                getHeight() - BOTTOM_BORDER_HEIGHT - TOP_LEFT_GAP - BUTTON_HEIGHT,
                BUTTON_WIDTH, BUTTON_HEIGHT);
        //=========================//
        addRoomButton.setText("Add Room");
        addRoomButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addRoomButton_actionPerformed();
            }
        });
        TUtilities.setPlainFont(addRoomButton, FONT_SIZE);
        
        addComponent(addRoomButton);
        addRoomButton.setBounds(editRoomButton.getX() - BUTTON_WIDTH - TUtilities.GAP,
                editRoomButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
        //=========================//
        tipLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(tipLabel, FONT_SIZE);
        
        addComponent(tipLabel);
        tipLabel.setBounds(roomLabel.getX(), addRoomButton.getY() + (BUTTON_HEIGHT / 2),
                getFrameWidth() - (getWidth() - addRoomButton.getX()) - roomLabel.getX() - TUtilities.GAP,
                tipLabel.getFont().getSize());
        //=========================//
        roomSchedTable.setRowHeight(30);
        roomSchedTable.setEnabled(false);
        DefaultTableCellRenderer centerTableHeader = (DefaultTableCellRenderer) roomSchedTable.getTableHeader().getDefaultRenderer();
        centerTableHeader.setHorizontalAlignment(JLabel.CENTER);
        TUtilities.setPlainFont(roomSchedTable, FONT_SIZE);
        TUtilities.setBoldFont(roomSchedTable.getTableHeader(), FONT_SIZE);
        //=========================//
        roomSchedScroll.setViewportView(roomSchedTable);
        
        addComponent(roomSchedScroll);
        roomSchedScroll.setBounds(roomLabel.getX(), TUtilities.belowOf(roomFloorLabel) + TOP_LEFT_GAP,
                getFrameWidth() - (TOP_LEFT_GAP * 2), 
                getFrameHeight() - TUtilities.belowOf(roomFloorLabel) - BUTTON_HEIGHT - (TOP_LEFT_GAP * 2));
    }

    @Override
    public void defaultSetup() {
        roomCombo.setModel(new DefaultComboBoxModel(getRooms()));
        LOG.debug("Room Combo Box Size: {}", roomCombo.getModel().getSize());
        if(roomCombo.getModel().getSize() > 0) {
            roomCombo_actionPerformed();
        }
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
    
    private void getRoomInfo(String roomCode){
        //================get Room department and floor==========================//
        LOG.debug("room to get info: {}", roomCode);
        try {
            DB.open("get Subjects Info");
            DB.setQuery("SELECT DepartmentDesc, Floor FROM RoomsTB, DepartmentTB WHERE "
                    + "RoomCode=? AND RoomsTB.DepartmentCode=DepartmentTB.DepartmentCode");
            LOG.debug("Preparing Statement...");
            DB.ps = DB.conn.prepareStatement(DB.getQuery());
            
            DB.ps.setString(1, roomCode);
            
            LOG.debug("Executing Statement...");
            DB.rs = DB.ps.executeQuery();
            
            if(DB.rs.next()){
                roomDeptLabelCode.setText(DB.rs.getString(1));
                roomFloorLabelCode.setText(DB.rs.getString(2) + " Floor");
            }
            
        } catch (SQLException | ClassNotFoundException ex) {
            LOG.error("Error loading subjects info", ex);
        } finally {
            DB.close();
        }
        //================get Room Schedule==========================//
        try {
            DB.open("get Room Schedule");
            DB.setQuery("SELECT Day, TimeStart, TimeEnd FROM SectionSubjTB WHERE "
                    + "Room=? AND TimeStart Is Not Null AND TimeEnd Is Not Null AND Day<>''");
            LOG.debug("Preparing Statement...");
            DB.ps = DB.conn.prepareStatement(DB.getQuery(), ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_READ_ONLY);
            
            DB.ps.setString(1, roomCode);
            
            LOG.debug("Executing Statement...");
            DB.rs = DB.ps.executeQuery();
            
            roomSched = new String[DB.getRecordCount(DB.rs, "Rooms complete")][3];
            
            for(int x = 0; DB.rs.next(); x++) {
                roomSched[x][0] = DB.rs.getString("Day"); //Day
                roomSched[x][1] = TUtilities.FORMAT.format(DB.rs.getTimestamp("TimeStart")); //Time Start
                roomSched[x][2] = TUtilities.FORMAT.format(DB.rs.getTimestamp("TimeEnd")); //Time End
            }
        } catch (SQLException | ClassNotFoundException ex) {
            LOG.error("Error loading room schedule", ex);
        } finally {
            DB.close();
        }
        
        //Set to datas to table properly
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                roomSchedTable.setModel(new DefaultTableModel(roomSched, ROOM_SCHED_COL));
      
                //<editor-fold defaultstate="collapsed" desc="Set icon for Room Schedule">
                DefaultTableCellRenderer cellIconRenderer = new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                            boolean isSelected, boolean hasFocus, int row, int column) {
                        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        setHorizontalAlignment(JLabel.CENTER);
                        
                        switch(column) {
                            case 0: //day
                                setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/calendar_view_week.png"));
                                break;
                            case 1: //time start
                                setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/time.png"));
                                break;
                            case 2: //time end
                                setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/time.png"));
                                break;
                        }
                        return this;
                    }
                };
                //set icon for for section schedule table cell
                for (int x = 0; x < roomSchedTable.getColumnCount(); x++) {
                    roomSchedTable.getColumnModel().getColumn(x).setCellRenderer(cellIconRenderer);
                }
                //</editor-fold>
            }
        });
    }
    
    private void roomCombo_actionPerformed(){
        getRoomInfo(TUtilities.getComboValue(roomCombo));
    }
    
    private void editRoomButton_actionPerformed(){
        
    }
    
    private void addRoomButton_actionPerformed(){
        
    }
}