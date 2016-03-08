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
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTable;
import org.slf4j.LoggerFactory;
import tenten.TFrame;
import tenten.TUtilities;

/**
 * This class UPDATES sections properly, by using classes that validates input.
 * Look for TimeValidator.class, JTextFieldNumberValidator.class and
 * ConflictScheduleChecker for more info.
 *
 * @author Tenten Ponce
 * @version 1.0
 * @since October 2015
 */
public class EditSectionForm extends TFrame{

    private static final Logger log = (Logger) LoggerFactory.getLogger(MainForm.class); //log purposes
    private static final DatabaseConnection DB = DatabaseConnection.getInstance(); //My Database
    private static final ConflictScheduleChecker CONFLICT_SCHEDULE_CHECKER = new ConflictScheduleChecker();
    
    private static final int TOP_LEFT_GAP = 20; //gap of components from the top and left
    private static final int FONT_SIZE = 15; //my most used font size for this form
    
    private static final int SECTION_CODE_WIDTH = 150; //width of section code
    private static final int SEM_CODE_WIDTH = 50; //width of semester (1 or 2)
    
    private static final int BUTTON_HEIGHT = 30; //height for update schedule button
    private static final int BUTTON_WIDTH = 160; //width for update schedule button
    
    private static final int SCHED_ROW_HEIGHT = 25; //row height of schedule table
    
    private static final String[] SCHEDULE_COL = {"Subject Code", "Subject Name",
        "Time Start", "Time End", "Day", "Room", "Professor"};
        private static final String TIME_TOOLTIP = "Ex: 10:00 AM";
    @SuppressWarnings("UseOfObsoleteCollectionType")
    private static final Vector<String> SCHEDULE_COL_VECTOR = new Vector<>(Arrays.asList(SCHEDULE_COL)); //converts schedule column to vector to set it as model.
    private static boolean isCompleteRow[]; //holds the row that are completed
    
    private JLabel sectionLabel;
    private JLabel sectionCodeLabel;
    private JLabel semLabel;
    private JLabel semCodeLabel;
    private JXLabel tipLabel;
    private JXTable schedTable;
    private JScrollPane schedScroll;
    private JButton updateButton;
    
    public EditSectionForm(int width, int height, int defaultOperation, ImageIcon bg, String title, 
            DefaultTableModel tableModel, String sectionCode, int semester){
        super(width, height, defaultOperation, bg, title); //sets the frame first before adding anything!
        log.setLevel(Level.ALL); //include trace level
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                schedTable.setModel(new DefaultTableModel(tableModel.getDataVector(), SCHEDULE_COL_VECTOR) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        if (column == 0 || column == 1) { //subjcode and subjname not editable
                            return false;
                        } else {
                            updateButton.setEnabled(true);
                            return true;
                        }
                    }
                }); //get passed model from schedule table on main form.
                defaultSetup2(); //call after schedule table is set.
            }
        });

        sectionCodeLabel.setText(sectionCode); //set section code
        semCodeLabel.setText(String.valueOf(semester)); //set semester
        setIcon("IconsImages/application_edit.png");
    }
    
    @Override
    public void init() {
        sectionLabel = new JLabel();
        sectionCodeLabel = new JLabel();
        semLabel = new JLabel();
        semCodeLabel = new JLabel();
        tipLabel = new JXLabel();
        schedTable = new JXTable();
        schedScroll = new JScrollPane();
        updateButton = new JButton();
        //=========================//
        sectionLabel.setText("Section:");
        sectionLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(sectionLabel, FONT_SIZE);
        
        addComponent(sectionLabel);
        sectionLabel.setBounds(SIDE_BORDER_WIDTH + TOP_LEFT_GAP, TITLE_BUTTON_HEIGHT + TUtilities.GAP,
                TUtilities.getPrefWidth(sectionLabel), sectionLabel.getFont().getSize());
        //=========================//
        sectionCodeLabel.setForeground(Color.WHITE);
        TUtilities.setPlainFont(sectionCodeLabel, FONT_SIZE);
        
        addComponent(sectionCodeLabel);
        sectionCodeLabel.setBounds(TUtilities.toRightOf(sectionLabel) + TUtilities.GAP,
                sectionLabel.getY(), SECTION_CODE_WIDTH, sectionCodeLabel.getFont().getSize());
        //=========================//
        semLabel.setText("Semester:");
        semLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(semLabel, FONT_SIZE);
        
        addComponent(semLabel);
        semLabel.setBounds(TUtilities.toRightOf(sectionCodeLabel) + TUtilities.GAP,
                sectionLabel.getY(), TUtilities.getPrefWidth(semLabel), semLabel.getFont().getSize());
        //=========================//
        semCodeLabel.setForeground(Color.WHITE);
        TUtilities.setPlainFont(semCodeLabel, FONT_SIZE);
        
        addComponent(semCodeLabel);
        semCodeLabel.setBounds(TUtilities.toRightOf(semLabel) + TUtilities.GAP,
                sectionLabel.getY(), SEM_CODE_WIDTH, semCodeLabel.getFont().getSize());
        //=========================//
        schedTable.setRowHeight(SCHED_ROW_HEIGHT);
        schedTable.setSortable(false);
        schedTable.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer centerTableHeader = (DefaultTableCellRenderer) schedTable.getTableHeader().getDefaultRenderer();
        centerTableHeader.setHorizontalAlignment(JLabel.CENTER);
        TUtilities.setPlainFont(schedTable, FONT_SIZE);
        TUtilities.setBoldFont(schedTable.getTableHeader(), FONT_SIZE);
        //=========================//
        schedScroll.setViewportView(schedTable);
        
        addComponent(schedScroll);
        schedScroll.setBounds(sectionLabel.getX(), TUtilities.belowOf(sectionLabel) + TOP_LEFT_GAP,
                getWidth() - (sectionLabel.getX() * 2),
                getFrameHeight() - TUtilities.belowOf(sectionLabel) - TUtilities.GAP - TOP_LEFT_GAP - BUTTON_HEIGHT);
        //=========================//
        updateButton.setText("Update Schedule");
        updateButton.setIcon(TUtilities.getIconFromResource(getClass(), "/IconsImages/calendar_edit.png"));
        updateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateButton_actionPerformed();
            }
        });
        TUtilities.setBoldFont(updateButton, FONT_SIZE);
        
        addComponent(updateButton);
        updateButton.setBounds(getWidth() - BUTTON_WIDTH - TOP_LEFT_GAP - SIDE_BORDER_WIDTH,
                TUtilities.belowOf(schedScroll) + TUtilities.GAP, BUTTON_WIDTH, BUTTON_HEIGHT);
        //=========================//
        tipLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(tipLabel, FONT_SIZE);
        
        addComponent(tipLabel);
        tipLabel.setBounds(sectionLabel.getX(), updateButton.getY() + (updateButton.getHeight() / 2),
                getFrameWidth() - BUTTON_WIDTH - TOP_LEFT_GAP, tipLabel.getFont().getSize());
    }
    
    /**
     * Converts util.Date to sql.Date. (for PreparedStatements)
     *
     * @param d util.Date to be converted
     * @return converted util.Date (sql.Date)
     */
    private java.sql.Date getSQLDate(String d) {
        java.util.Date date;
        try {
            date = TUtilities.FORMAT.parse(d);
            log.debug("Returning parsed date: {}", new java.sql.Date(date.getTime()));
            return new java.sql.Date(date.getTime());
        } catch (ParseException ex) {
            log.error("Date cannot parse: {}", d);
        }

        return null;
    }

    @Override
    public void defaultSetup() {/*Empty because schedule table must be set first. look for defaultSetup2()*/}
    
    /**
     * default setup is here so the schedule Table is set before calling default setup.
     */
    private void defaultSetup2(){
        log.trace(">> defaultSetup2()");
        //==============CELL VALIDATE========================//
        schedTable.getColumnModel().getColumn(2).setCellEditor(new TimeValidator(
                new JTextField(), 2, tipLabel, schedTable)); //the magical time validator
        schedTable.getColumnModel().getColumn(3).setCellEditor(new TimeValidator(
                new JTextField(), 3, tipLabel, schedTable)); //the magical time vaidator (again).

        DefaultTableCellRenderer timeRenderer = new DefaultTableCellRenderer();
        timeRenderer.setToolTipText(TIME_TOOLTIP);

        schedTable.getColumnModel().getColumn(2).setCellRenderer(timeRenderer); //tooltippppssss
        schedTable.getColumnModel().getColumn(3).setCellRenderer(timeRenderer);// tolteps again
        //===============Day and Room========================//
        JComboBox day_cmbox = new JComboBox(TUtilities.DAYS);
        schedTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(day_cmbox)); //Day Column

        JComboBox room_cmbox = new JComboBox();
        try {
            DB.open("Get registered rooms");
            log.debug("Preparing normal statement...");
            DB.s = DB.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            log.debug("Executing statement...");
            DB.rs = DB.s.executeQuery("SELECT RoomCode FROM RoomsTB");

            log.debug("Rooms counted: {}", DB.getRecordCount(DB.rs, "Room code list"));

            room_cmbox.addItem(""); //adding null for option

            while (DB.rs.next()) {
                room_cmbox.addItem(DB.rs.getString(1));
            }

        } catch (ClassNotFoundException | SQLException ex) {
            log.error("Error retreiving room list", ex);
        } finally {
            DB.close();
        }

        schedTable.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(room_cmbox)); //Room Column

        isCompleteRow = new boolean[schedTable.getRowCount()];
        log.trace(">> Entering Loop, Check completed Rows...");
        for (int x = 0; x < schedTable.getRowCount(); x++) {
            isCompleteRow[x] = schedTable.getModel().getValueAt(x, 2) != null && //time Start
                    schedTable.getModel().getValueAt(x, 3) != null && //Time End
                    schedTable.getModel().getValueAt(x, 4) != null && //day
                    schedTable.getModel().getValueAt(x, 5) != null; //room

            log.debug("Is {} row complete: {}", x, isCompleteRow[x]);
        }
        log.trace("<< Exiting Loop...");
        log.trace("<< defaultSetup2()");
    }
    
    private void updateButton_actionPerformed(){
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        updateButton.setEnabled(false);

        new SwingWorker() {
            @Override
            protected Void doInBackground() {
                TUtilities.setTip("Validating Table...", tipLabel);
                if (CONFLICT_SCHEDULE_CHECKER.isTableHasConflict(schedTable)) {
                    TUtilities.setTip("Conflict Schedule in the table found.", tipLabel);
                    return null; //exit method immediately because conflict found.
                }

                if (CONFLICT_SCHEDULE_CHECKER.isScheduleHasConflict(schedTable,
                        Integer.parseInt(semCodeLabel.getText()), sectionCodeLabel.getText())) {
                    TUtilities.setTip("Conflict has found on other section: " + 
                            CONFLICT_SCHEDULE_CHECKER.getConflictSection(), tipLabel);
                    return null; //exit method immediately because conflict found.

                }

                try {
                    DB.open("update section");

                    DB.setQuery("UPDATE SectionSubjTB SET "
                            + "TimeStart=?, TimeEnd=?, Day=?, Room=?, ProfName=? "
                            + "WHERE SectionCode=? AND SubjCode=? AND Semester=?");

                    log.debug("Preparing Statement (BATCH)...");
                    DB.ps = DB.conn.prepareStatement(DB.getQuery());

                    DB.conn.setAutoCommit(false); //for batch update

                    //=================ADD TO BATCH EACH ROW===========================//
                    TUtilities.setTip("Updating Table...", tipLabel);
                    String[] schedule = new String[schedTable.getModel().getColumnCount()]; //1 row only, after looping columns, add to batch then put new values
                    log.trace("Entering Loop update table...");
                    for (int x = 0; x < schedTable.getModel().getRowCount(); x++) {
                        for (int y = 0; y < schedTable.getModel().getColumnCount(); y++) {
                            if (schedTable.getModel().getValueAt(x, y) != null) {
                                schedule[y]
                                        = String.valueOf(schedTable.getModel().getValueAt(x, y)); //get values  
                            } else {
                                schedule[y] = ""; //put "" if null
                            }

                            log.debug("Storing {} on scheduleArray...", schedule[y]);
                        }

                        //===========Setup Datas to be added on batch=============//
                        if (!"".equals(schedule[2])) { //Time Start
                            DB.ps.setDate(1, getSQLDate(schedule[2]));
                        } else {
                            DB.ps.setNull(1, java.sql.Types.DATE);
                        }

                        if (!"".equals(schedule[3])) { //Time End
                            DB.ps.setDate(2, getSQLDate(schedule[3]));
                        } else {
                            DB.ps.setNull(2, java.sql.Types.DATE);
                        }

                        DB.ps.setString(3, schedule[4]); //day
                        DB.ps.setString(4, schedule[5]); //RoomCode
                        DB.ps.setString(5, schedule[6]); //Professor

                        DB.ps.setString(6, sectionCodeLabel.getText()); //Section Code
                        DB.ps.setString(7, schedule[0]); //Subj Code
                        DB.ps.setInt(8, Integer.parseInt(semCodeLabel.getText())); // Semester

                        DB.ps.addBatch();
                        
                        log.debug("Adding {} batch...", x);
                    }

                    log.trace("<< Loop");
                } catch (ClassNotFoundException | SQLException ex) {
                    log.error("Error updating section", ex);
                    DB.close();
                    return null; //exit when there's error readying batch.
                }

                //=========UPDATE AND COMMIT TO DATABASE===================//
                TUtilities.setTip("Committing Changes...", tipLabel);
                try {
                    log.debug("Executing Batch: Updating section {}", sectionCodeLabel.getText());
                    DB.ps.executeBatch();

                    log.debug("Commiting...");
                    DB.conn.commit();
                } catch (SQLException ex) {
                    log.error("Updating Section Schedule failed", ex);
                    try {
                        log.debug("Rolling back changes...");
                        DB.conn.rollback(); //undo all changes when error happen.
                    } catch (SQLException e) {
                        log.error("Rollback failed.", e); //MUST NOT HAPPEN
                    }
                } finally {
                    DB.close();
                    TUtilities.setTip("Done.", tipLabel);
                    MainForm mg = MainForm.getInstance(); //refresh main 
                    mg.getSectionList(); //refresh main
                }
                return null;
            }
            
            @Override
            public void done() {
                setCursor(null);
                updateButton.setEnabled(true);
            }
            
        }.execute();
    }
}
