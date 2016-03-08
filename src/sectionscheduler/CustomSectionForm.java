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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
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
 *
 * @author tenten
 */
public class CustomSectionForm extends TFrame{

    //<editor-fold defaultstate="collapsed" desc="Variables">
    private static final Logger LOG = (Logger) LoggerFactory.getLogger(CustomSectionForm.class); //log purposes
    private static final DatabaseConnection DB = DatabaseConnection.getInstance(); //My Database
    private static final ConflictScheduleChecker CONFLICT_SCHEDULE_CHECKER = new ConflictScheduleChecker();
    
    private static final int LEFT_GAP = 210; //gap of combo boxes, text fields from the left so they are aligned
    private static final int TOP_LEFT_GAP = 20; //gap of components from the top and left
    private static final int FONT_SIZE = 15; //my most used font size for this form
    
    private static final int COMBO_WIDTH = 200; //width of the year combo box
    
    private static final int FIELD_WIDTH = 45; //width of the text field, max reg and irreg
    private static final int FIELD_LIMIT = 2; //limit of characters on max reg and irreg
    
    private static final int BUTTON_HEIGHT = 30; //height for create section and schedule buttons
    private static final int BUTTON_WIDTH = 150; //width for create section and schedule buttons
    
    private static final int SCHED_ROW_HEIGHT = 25; //row height of schedule table
    
    private final static String[] SCHEDULE_COL = {"Subject Code", "Subject Description",
        "Time Start", "Time End", "Day", "Room", "Professor"};
    private final static String TIME_TOOLTIP = "Ex: 10:00 AM";
        
    private JLabel sectionCodeLabel;
    private JLabel semLabel;
    private JLabel subjLabel;
    private JLabel maxRegStudLabel;
    private JLabel maxIrregStudLabel;
    private JLabel schedForLabel;
    private JXLabel tipLabel;
    private JComboBox semCombo;
    private JComboBox subjCombo;
    private JTextField sectionCodeField;
    private JTextField maxRegStudField;
    private JTextField maxIrregStudField;
    private JXTable schedTable;
    private JScrollPane schedScroll;
    private JButton addSubjButton;
    private JButton removeSubjButton;
    private JButton createSchedButton;
    //</editor-fold>
    
    public CustomSectionForm(int width, int height, int defaultOperation, ImageIcon bg, String title) {
        super(width, height, defaultOperation, bg, title);
        LOG.setLevel(Level.ALL);
        setIcon("IconsImages/application_xp_terminal.png");
    }

    @Override
    public void init() {
        sectionCodeLabel = new JLabel();
        semLabel = new JLabel();
        subjLabel = new JLabel();
        maxRegStudLabel = new JLabel();
        maxIrregStudLabel = new JLabel();
        schedForLabel = new JLabel();
        tipLabel = new JXLabel();
        semCombo = new JComboBox();
        subjCombo = new JComboBox();
        sectionCodeField = new JTextField();
        maxRegStudField = new JTextField();
        maxIrregStudField = new JTextField();
        schedTable = new JXTable();
        schedScroll = new JScrollPane();
        addSubjButton = new JButton();
        removeSubjButton = new JButton();
        createSchedButton = new JButton();
        //=========================//
        sectionCodeLabel.setText("Section Code:");
        sectionCodeLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(sectionCodeLabel, FONT_SIZE);
        
        addComponent(sectionCodeLabel);
        sectionCodeLabel.setBounds(SIDE_BORDER_WIDTH + TOP_LEFT_GAP,
                TITLE_BUTTON_HEIGHT + TOP_LEFT_GAP, TUtilities.getPrefWidth(sectionCodeLabel),
                FONT_SIZE);
        //=========================//
        sectionCodeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e){
                sectionCodeField_keyTyped();
            }
        });
        TUtilities.setPlainFont(sectionCodeField, FONT_SIZE);
        
        addComponent(sectionCodeField);
        sectionCodeField.setBounds(LEFT_GAP, 
                TUtilities.toMiddleOfLabel(sectionCodeLabel, sectionCodeField),
                getFrameWidth() - LEFT_GAP - TUtilities.GAP, 
                TUtilities.getPrefHeight(sectionCodeField));
        //=========================//
        semLabel.setText("Semester:");
        semLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(semLabel, FONT_SIZE);
        
        addComponent(semLabel);
        semLabel.setBounds(sectionCodeLabel.getX(), 
                TUtilities.belowOf(sectionCodeField) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(semLabel), FONT_SIZE);
        //=========================//
        semCombo.setModel(new DefaultComboBoxModel(new String[]{"1st Semester", "2nd Semester"}));
        TUtilities.setPlainFont(semCombo, FONT_SIZE);
        
        addComponent(semCombo);
        semCombo.setBounds(LEFT_GAP, TUtilities.toMiddleOfLabel(semLabel, semCombo),
                COMBO_WIDTH, TUtilities.getPrefHeight(semCombo));
        //=========================//
        maxRegStudLabel.setText("Max Regular Students:");
        maxRegStudLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(maxRegStudLabel, FONT_SIZE);
        
        addComponent(maxRegStudLabel);
        maxRegStudLabel.setBounds(sectionCodeLabel.getX(), TUtilities.belowOf(semCombo) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(maxRegStudLabel),(int) maxRegStudLabel.getFont().getSize());
        //=========================//
        maxRegStudField.setDocument(new JTextFieldNumberValidator(FIELD_LIMIT, maxRegStudField, tipLabel));
        maxRegStudField.setHorizontalAlignment(JLabel.CENTER);
        maxRegStudField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent evt) {
                maxRegStudField_keyTyped();
            }
        });
        TUtilities.setPlainFont(maxRegStudField, FONT_SIZE);
        
        addComponent(maxRegStudField);
        maxRegStudField.setBounds(LEFT_GAP, TUtilities.toMiddleOfLabel(maxRegStudLabel, maxRegStudField),
                FIELD_WIDTH, (int) maxRegStudField.getPreferredSize().getHeight());
        //=========================//
        maxIrregStudLabel.setText("Max Irregular Students:");
        maxIrregStudLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(maxIrregStudLabel, FONT_SIZE);
        
        addComponent(maxIrregStudLabel);
        maxIrregStudLabel.setBounds(sectionCodeLabel.getX(), TUtilities.belowOf(maxRegStudField) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(maxIrregStudLabel), (int) maxIrregStudLabel.getFont().getSize());
        //=========================//
        maxIrregStudField.setDocument(new JTextFieldNumberValidator((FIELD_LIMIT), maxIrregStudField, tipLabel));
        maxIrregStudField.setHorizontalAlignment(JLabel.CENTER);
        maxIrregStudField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent evt){
                maxIrregStudField_keyTyped();
            }
        });
        TUtilities.setPlainFont(maxIrregStudField, FONT_SIZE);
        
        addComponent(maxIrregStudField);
        maxIrregStudField.setBounds(LEFT_GAP, TUtilities.toMiddleOfLabel(maxIrregStudLabel, maxIrregStudField),
                FIELD_WIDTH, (int) maxIrregStudField.getPreferredSize().getHeight());
        //=========================//
        subjLabel.setText("Subjects:");
        subjLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(subjLabel, FONT_SIZE);
        
        addComponent(subjLabel);
        subjLabel.setBounds(sectionCodeLabel.getX(), 
                TUtilities.belowOf(maxIrregStudField) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(subjLabel), FONT_SIZE);
        //=========================//
        TUtilities.setPlainFont(subjCombo, FONT_SIZE);
        
        addComponent(subjCombo);
        subjCombo.setBounds(LEFT_GAP, TUtilities.toMiddleOfLabel(subjLabel, subjCombo),
                getFrameWidth() - LEFT_GAP - TUtilities.GAP,
                TUtilities.getPrefHeight(subjCombo));
        //=========================//
        removeSubjButton.setText("Remove Subject");
        removeSubjButton.setIcon(TUtilities.getIconFromResource(getClass(), "/IconsImages/arrow_up.png"));
        removeSubjButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                removeSubjButton_actionPerformed();
            }
        });
        TUtilities.setPlainFont(removeSubjButton, FONT_SIZE);
        
        addComponent(removeSubjButton);
        removeSubjButton.setBounds(getWidth() - SIDE_BORDER_WIDTH - BUTTON_WIDTH - TOP_LEFT_GAP,
                TUtilities.belowOf(subjCombo) + TUtilities.GAP, BUTTON_WIDTH, BUTTON_HEIGHT);
        //=========================//
        addSubjButton.setText("Add Subject");
        addSubjButton.setIcon(TUtilities.getIconFromResource(getClass(), "/IconsImages/arrow_down.png"));
        addSubjButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addSubjButton_actionPerformed();
            }
        });
        TUtilities.setPlainFont(addSubjButton, FONT_SIZE);
        
        addComponent(addSubjButton);
        addSubjButton.setBounds(removeSubjButton.getX() - BUTTON_WIDTH - TUtilities.GAP, 
                removeSubjButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
        //=========================//
        schedForLabel.setText("Schedule For:");
        schedForLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(schedForLabel, FONT_SIZE);
        
        addComponent(schedForLabel);
        schedForLabel.setBounds(sectionCodeLabel.getX(), 
                TUtilities.belowOf(addSubjButton) + TOP_LEFT_GAP,
                getFrameWidth() - sectionCodeLabel.getX() * 2,
                FONT_SIZE);
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
        schedScroll.setBounds(sectionCodeLabel.getX(), TUtilities.belowOf(schedForLabel) + TUtilities.GAP,
                getFrameWidth() - (TOP_LEFT_GAP * 2), 
                getFrameHeight() - TUtilities.belowOf(schedForLabel) - (TUtilities.GAP * 2) - BUTTON_HEIGHT);
        //=========================//
        createSchedButton.setText("Create Schedule");
        createSchedButton.setIcon(TUtilities.getIconFromResource(getClass(), "/IconsImages/calendar_add.png"));
        createSchedButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                createSchedButton_actionPerformed();
            }
        });
        TUtilities.setBoldFont(createSchedButton, FONT_SIZE);
        
        addComponent(createSchedButton);
        createSchedButton.setBounds(getWidth() - SIDE_BORDER_WIDTH - TOP_LEFT_GAP - BUTTON_WIDTH, 
                getHeight() - BOTTOM_BORDER_HEIGHT - BUTTON_HEIGHT - TUtilities.GAP,
                BUTTON_WIDTH, BUTTON_HEIGHT);
        //=========================//
        tipLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(tipLabel, FONT_SIZE);
        
        addComponent(tipLabel);
        tipLabel.setBounds(sectionCodeLabel.getX(), createSchedButton.getY() + (createSchedButton.getHeight() / 2),
                getFrameWidth() - createSchedButton.getWidth() - TOP_LEFT_GAP
                - sectionCodeLabel.getX(), tipLabel.getFont().getSize());
    }

    @Override
    public void defaultSetup() {
        subjCombo.setModel(new DefaultComboBoxModel(getSubjects()));
        resetTable();
        maxRegStudField.setText("0"); //initially set 10 students to regular
        maxIrregStudField.setText("10"); //and 0 students on irregular
        sectionCodeField.setText("");
    }
    
    /**
     * Loads all the subjects (code and desc) to an array
     * from database.
     * @return array of subjects (code and desc)
     */
    private String[] getSubjects(){
        String[] subjTemp = {""}; //initally set empty value, array to be shown. (SubjCode + SUbjName)
        
        try {
            DB.open("Get Subjects");
            LOG.debug("Creating normal statement...");
            DB.s = DB.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            DB.setQuery("SELECT SubjCode, SubjName FROM SubjectsTB");
            
            LOG.debug("Executing Statement...");
            DB.rs = DB.s.executeQuery(DB.getQuery());
            
            if(DB.rs.next()){
                subjTemp = new String[DB.getRecordCount(DB.rs, "Counting subjects")];
                
                LOG.trace(">> Entering Loop, get all subjects for pre requisites");
                for(int x = 0; DB.rs.next(); x++){ //start at 1 because 0 index is no prereq option
                    subjTemp[x] = DB.rs.getString("SubjCode") + "  " + DB.rs.getString("SubjName"); //get both
                    
                    LOG.debug(subjTemp[x]);
                }
                LOG.trace("<< Loop");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            LOG.error("Error getting subjects", ex);
        } finally {
            DB.close();
        }    

        return subjTemp;
    }
    
    /**
     * Resets the schedule table properly. This
     * kind of method avoids vector error.
     * (IndexOutofBoundsException)
     * 
     */
    private void resetTable() {
        LOG.debug("Scheduling to reset table...");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LOG.debug("Resetting table...");
                schedTable.setModel(new DefaultTableModel(new Object[][]{}, SCHEDULE_COL) {
                    @Override
                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        return !(columnIndex == 0 || columnIndex == 1); //subjcode and subjname not editable
                    }
                });

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
                    LOG.debug("Preparing normal statement...");
                    DB.s = DB.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    LOG.debug("Executing statement...");
                    DB.rs = DB.s.executeQuery("SELECT RoomCode FROM RoomsTB");

                    LOG.debug("Rooms counted: {}", DB.getRecordCount(DB.rs, "Room code list"));

                    room_cmbox.addItem(""); //adding null for option

                    while (DB.rs.next()) {
                        room_cmbox.addItem(DB.rs.getString(1));
                    }

                } catch (ClassNotFoundException | SQLException ex) {
                    LOG.error("Error retreiving room list", ex);
                } finally {
                    DB.close();
                }

                schedTable.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(room_cmbox)); //Room Column
            };
        });
    }
    
    /**
     * Converts the chosen semester to integer for proper 
     * and easy query. (1st Semester = 1, 2nd Semester = 2)
     * 
     * @param semSchedTxt 1st Semester or 2nd Semester
     * @return 1 or 2
     */
    private int getSemSched(String semSchedTxt) {
        int sem;

        if (semSchedTxt.equals("1st Semester")) {
            sem = 1;
        } else {
            sem = 2;
        }

        return sem;
    }
    
    /**
     * Converts util.Date to sql.Date.
     * (for PreparedStatements)
     * @param d util.Date to be converted
     * @return converted util.Date (sql.Date)
     */
    private java.sql.Date getSQLDate(String d) {
        java.util.Date date;
        try {
            date = TUtilities.FORMAT.parse(d);
            LOG.debug("Returning parsed date: {}", new java.sql.Date(date.getTime()));
            return new java.sql.Date(date.getTime());
        } catch (ParseException ex) {
            LOG.error("Date cannot parse: {}", d);
        }

        return null;
    }
    
    private void sectionCodeField_keyTyped(){
        schedForLabel.setText("Schedule For: " + sectionCodeField.getText());
        sectionCodeField.setBackground(Color.WHITE);
    }
    
    /**
     * Resets the background color of the text field after typing something
     */
    private void maxRegStudField_keyTyped(){
        maxRegStudField.setBackground(Color.WHITE);
    }
    
    /**
     * Resets the background color of the text field after typing something
     */
    private void maxIrregStudField_keyTyped(){
        maxIrregStudField.setBackground(Color.WHITE);
    }
    
    private void addSubjButton_actionPerformed(){
        if(subjCombo.getModel().getSize() < 1) {
            TUtilities.setTip("Select a Subject on the Combo Box First.", tipLabel);
            return; //exit 
        }
        
        DefaultTableModel schedModel = (DefaultTableModel) schedTable.getModel();
        String selectedSubj = TUtilities.getComboValue(subjCombo);
        
        schedModel.addRow(new String[]{selectedSubj.substring(0, selectedSubj.indexOf("  ")), //get the code only
        selectedSubj.substring(selectedSubj.indexOf("  ") + 1)}); //get the subject description
        
        subjCombo.removeItemAt(subjCombo.getSelectedIndex()); //remove the added subject on table from the box
        TUtilities.setTip("", tipLabel);
    }
    
    private void removeSubjButton_actionPerformed(){
        if(schedTable.getSelectedRow() == -1) { //check if there's no selected row
            TUtilities.setTip("Select a Subject from the table first.", tipLabel);
            return; //exit
        }
        
        String subjCode = String.valueOf(schedTable.getModel().getValueAt(
                    schedTable.getSelectedRow(), 0)); //0 is the column index of subject code
        String subjDesc = String.valueOf(schedTable.getModel().getValueAt(
                    schedTable.getSelectedRow(), 1)); //1 is the column index of subject desc
        DefaultTableModel schedModel = (DefaultTableModel) schedTable.getModel();
        
        String subjInfo = subjCode + "  " + subjDesc;
        LOG.debug("SubjCode: {}, SubjDesc: {}", subjCode, subjDesc);
        
        schedModel.removeRow(schedTable.getSelectedRow()); //remove it from table
        
        subjCombo.addItem(subjInfo); //return it back to the combo box
        
        TUtilities.setTip("", tipLabel);
    }
    
    private void createSchedButton_actionPerformed(){
         boolean isError = false;
         
        if(sectionCodeField.getText().trim().equals("")) {
            sectionCodeField.setBackground(Color.RED);
            sectionCodeField.requestFocusInWindow();
            isError = true;

            TUtilities.setTip("You cannot leave section code blank.", tipLabel);
        } else {
            try {
                DB.open("Check Section Code and semester duplicate");
                DB.setQuery("SELECT SectionCode FROM SectionsTB WHERE SectionCode=? AND Semester=?");
                LOG.debug("Preparing Statement...");
                DB.ps = DB.conn.prepareStatement(DB.getQuery());

                DB.ps.setString(1, sectionCodeField.getText().trim().toUpperCase());
                DB.ps.setInt(2, getSemSched(TUtilities.getComboValue(semCombo)));

                LOG.debug("Executing Statement...");
                DB.rs = DB.ps.executeQuery();

                if (DB.rs.next()) { //if there's duplicate
                    sectionCodeField.setBackground(Color.RED);
                    sectionCodeField.requestFocusInWindow();
                    isError = true; //error because there's duplicate

                    TUtilities.setTip(DB.rs.getString("SectionCode") + " is already in use. Change Section Code", tipLabel);
                }

            } catch (ClassNotFoundException | SQLException ex) {
                LOG.error("Error Checking for duplicate section", ex);
            } finally {
                DB.close();
            }
        }
         
        if (maxRegStudField.getText().equals("0") && maxIrregStudField.getText().equals("0")) {
            maxRegStudField.setBackground(Color.RED);
            maxRegStudField.requestFocusInWindow();
            isError = true;

            TUtilities.setTip("You cannot put 0 Students.", tipLabel);
        }

        if (maxRegStudField.getText().equals("")){
            maxRegStudField.setBackground(Color.RED);
             maxRegStudField.requestFocusInWindow();
            isError = true;

            TUtilities.setTip("Fill up the Maximum Student Text Fields.", tipLabel);
        }
        
        if(maxIrregStudField.getText().equals("")){
            maxIrregStudField.setBackground(Color.RED);
            maxIrregStudField.requestFocusInWindow();
            isError = true;
            
            TUtilities.setTip("Fill up the Maximum Student Text Fields.", tipLabel);
        }
        
        if(schedTable.getModel().getRowCount() < 1) {
            isError = true;
            
            TUtilities.setTip("You cannot create section without subjects.", tipLabel);
        }
        
        if(isError) {
            return; //exit because there's error
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        createSchedButton.setEnabled(false);

        new SwingWorker() {

            @Override
            protected Void doInBackground() {
                TUtilities.setTip("Validating Table...", tipLabel);
                if (CONFLICT_SCHEDULE_CHECKER.isTableHasConflict(schedTable)) { //check table only
                    TUtilities.setTip("Conflict Schedule in the table found.", tipLabel);
                    createSchedButton.setEnabled(true);
                    return null; //exit immediately because conflict was found.
                }
                
                if(CONFLICT_SCHEDULE_CHECKER.isScheduleHasConflict(schedTable, 
                        getSemSched(TUtilities.getComboValue(semCombo)), "NoSectionToAvoid")){ //check table and the whole room schedules/section schedules.
                    TUtilities.setTip("Conflict has found on other section: " + CONFLICT_SCHEDULE_CHECKER.getConflictSection(), 
                            tipLabel);
                    createSchedButton.setEnabled(true);
                    return null; //exit immediately because conflict was found.
                }
                //=======================================//
                TUtilities.setTip("Preparing Database...", tipLabel);

                int row = schedTable.getModel().getRowCount();
                int col = schedTable.getModel().getColumnCount();

                try {
                    DB.open("Ready for batch: Section Schedule"); //Open database here before looping, for faster looping/performance
                    /**query = "INSERT INTO RoomSched"
                     * + "(RoomCode, Day, TimeStart, TimeEnd) "
                     * + "VALUES(?,?,?,?)";
                     * log.debug("Preparing statement for roomschedule...");
                     * roomSched = DB.conn.prepareStatement(query); //prepare it now for batch.
                     *///Depreciated
                    DB.setQuery("INSERT INTO SectionSubjTB"
                            + "(SectionCode, SubjCode, Semester, TimeStart, TimeEnd, Day,"
                            + "Room, ProfName)"
                            + "VALUES(?,?,?,?,?,?,?,?)");

                    LOG.debug("Preparing statement for section subject shedule...");
                    DB.ps = DB.conn.prepareStatement(DB.getQuery()); //prepare it now for batch.
                    DB.conn.setAutoCommit(false); //for batch query, (increase performance/speed)
                } catch (ClassNotFoundException | SQLException ex) {
                    LOG.error("Error preparing batch", ex);
                    DB.close();
                    return null;
                }
                //================A TIME FOR LOOP===========//
                TUtilities.setTip("Creating Schedule...", tipLabel);
                String[][] schedule = new String[1][col]; //1 row only, after looping columns, add to batch then put new values
                LOG.debug("Entering loop: schedule table...");
                for (int x = 0; x < row; x++) {
                    //isRoomSchedAvailable = true; depreciated
                    for (int y = 0; y < col; y++) {
                        if (schedTable.getModel().getValueAt(x, y) != null) {
                            schedule[0][y]
                                    = String.valueOf(schedTable.getModel().getValueAt(x, y)); //get values
                        } else {
                            schedule[0][y] = "";
                            /*if (y >= 2 && y <= 5) { //check only null for time start, end, day, room.
                                isRoomSchedAvailable = false; //Dont insert because incomplete.
                            }*/ //depreciated
                        }
                    }
                    //INSERT RECORD BATCH QUERY
                    try {
                        /**Check if room schedule can be record (depreciated)
                        if (isRoomSchedAvailable) {
                            log.debug("Complete Room Schedule found: setting up...");
                            roomSched.setString(1, schedule[0][5]); //RoomCode
                            roomSched.setString(2, schedule[0][4]); //Day
                            roomSched.setDate(3, getSQLDate(schedule[0][2])); //Time Start
                            roomSched.setDate(4, getSQLDate(schedule[0][3])); //Time End

                            log.debug("Adding to batch...");
                            roomSched.addBatch(); //add to batch for later commit
                            isRoomBatch = true; //to execute batch because a batch has been entered.
                        }
                        //============================================**/
                        LOG.debug("Setting up {} row...", x);
                        DB.ps.setString(1, sectionCodeField.getText().trim().toUpperCase());
                        DB.ps.setString(2, schedule[0][0]); //Subject Code
                        DB.ps.setInt(3, getSemSched(TUtilities.getComboValue(semCombo)));

                        if (!"".equals(schedule[0][2])) { //Time Start
                            DB.ps.setDate(4, getSQLDate(schedule[0][2]));
                        } else {
                            DB.ps.setNull(4, java.sql.Types.DATE);
                        }

                        if (!"".equals(schedule[0][3])) { //Time End
                            DB.ps.setDate(5, getSQLDate(schedule[0][3]));
                        } else {
                            DB.ps.setNull(5, java.sql.Types.DATE);
                        }

                        DB.ps.setString(6, schedule[0][4]); //Day
                        DB.ps.setString(7, schedule[0][5]); //RoomCode
                        DB.ps.setString(8, schedule[0][6]); //Professor

                        LOG.debug("Adding to batch...");
                        DB.ps.addBatch();
                    } catch (SQLException e) {
                        LOG.error("Error Preparing Datas for batch", e);
                        DB.close();
                        return null;
                    }
                }
                //=========UPDATE AND COMMIT TO DATABASE===================//
                try {
                    /*if (isRoomBatch) { //check if atleast one complete schedule is set.
                        log.debug("Executing Room Batch...");
                        roomSched.executeBatch();
                        isRoomBatch = false; //reset it.
                    }*///Depreciated

                    LOG.debug("Executing Section Subject Schedule Batch...");
                    DB.ps.executeBatch();
                    
                    LOG.debug("Commiting...");
                    DB.conn.commit();
                } catch (SQLException ex) {
                    LOG.error("Inserting Data on Section Schedule failed", ex);
                    try {
                        LOG.debug("Rolling back changes...");
                        DB.conn.rollback(); //undo all changes when error happen.
                    } catch (SQLException e) {
                        LOG.error("Rollback failed."); //MUST NOT HAPPEN
                    }
                    DB.close(); //close here because it will return and finally will not reached.
                    return null;
                } finally {
                    DB.close();
                }

                //========================================//
                TUtilities.setTip("Creating Section...", tipLabel);

                try {
                    DB.open("For section code and attribute");

                    DB.setQuery("INSERT INTO SectionsTB(SectionCode, Semester, RegEnrolled, "
                            + "IrregEnrolled, RegLimit"
                            + ", IrregLimit, SectionCourse, SectionYear, SectionLetter) "
                            + "VALUES(?,?,?,?,?,?,?,?,?)");

                    LOG.debug("Preparing statement...");
                    DB.ps = DB.conn.prepareStatement(DB.getQuery());

                    DB.ps.setString(1, sectionCodeField.getText().trim().toUpperCase());
                    DB.ps.setInt(2, getSemSched(TUtilities.getComboValue(semCombo)));
                    DB.ps.setInt(3, 0); //no regular enrolled default
                    DB.ps.setInt(4, 0); //no irregular enrolled default
                    DB.ps.setInt(5, Integer.parseInt(maxRegStudField.getText()));
                    DB.ps.setInt(6, Integer.parseInt(maxIrregStudField.getText()));
                    DB.ps.setString(7, "Custom"); //no course
                    DB.ps.setInt(8, 0); //no year level
                    DB.ps.setString(9, "Custom"); //no letter

                    LOG.debug("Executing Statement...");
                    DB.ps.executeUpdate();

                } catch (ClassNotFoundException | SQLException ex) {
                    LOG.error("Error inserting section attributes", ex);
                    DB.close(); //close here because finally wont be reached (return statement).
                    return null;
                } finally {
                    DB.close();
                }

                //=========HIGH FIVE FOR SUCCESSFUL UPDATE================//
                String sectionCodeTemp = sectionCodeField.getText().trim().toUpperCase();
                defaultSetup(); //reset all
                TUtilities.setTip("Done. " + sectionCodeTemp + 
                        " was successfully created.", tipLabel);
                
                LOG.info("Refreshing Section list on mainGUI...");
                MainForm mainForm = MainForm.getInstance(); //refresh section list.
                mainForm.getSectionList(); //refresh section list.

                return null;
            }

            @Override
            public void done() {
                setCursor(null);
                createSchedButton.setEnabled(true);
            }
        }.execute();
    }
}
