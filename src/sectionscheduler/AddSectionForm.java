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
 * This class create sections properly, by loading appropriate subjects for the
 * chosen course, semester and year level. It also validates proper input for
 * time, day, and rooms. Look for TimeValidator.class, JTextFieldNumberValidator.class 
 * and ConflictScheduleChecker.class for more info.
 *
 * @author Tenten Ponce
 * @version 1.0
 * @since October 2015
 */
public class AddSectionForm extends TFrame{
    
    //<editor-fold defaultstate="collapsed" desc="Variables">
    private static final Logger LOG = (Logger) LoggerFactory.getLogger(AddSectionForm.class); //log purposes
    private static final DatabaseConnection DB = DatabaseConnection.getInstance(); //My Database
    private static final ConflictScheduleChecker CONFLICT_SCHEDULE_CHECKER = new ConflictScheduleChecker();
    private Section section; //section object
    
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
    private String sectionletter;
    String[][] subjectsTemp = {}; //temporary holder for subject name and code.
    
    private JLabel pickCourseLabel;
    private JLabel pickYearLabel;
    private JLabel pickSemLabel;
    private JLabel maxRegStudLabel;
    private JLabel maxIrregStudLabel;
    private JLabel schedForLabel;
    private JXLabel tipLabel;
    private JComboBox pickCourseCombo;
    private JComboBox pickYearCombo;
    private JComboBox pickSemCombo;
    private JTextField maxRegStudField;
    private JTextField maxIrregStudField;
    private JButton loadSubjButton;
    private JButton createSchedButton;
    private JXTable schedTable;
    private JScrollPane schedScroll;
    //</editor-fold>

    public AddSectionForm(int width, int height, int defaultOperation, ImageIcon bg, String title){
        super(width, height, defaultOperation, bg, title);
        LOG.setLevel(Level.ALL); //include trace level
        setIcon("IconsImages/application_add.png");
    }

    @Override
    public void init() {
        pickCourseLabel = new JLabel();
        pickYearLabel = new JLabel();
        pickSemLabel = new JLabel();
        maxRegStudLabel = new JLabel();
        maxIrregStudLabel = new JLabel();
        schedForLabel = new JLabel();
        tipLabel = new JXLabel();
        pickCourseCombo = new JComboBox();
        pickYearCombo = new JComboBox();
        pickSemCombo = new JComboBox();
        maxRegStudField = new JTextField();
        maxIrregStudField = new JTextField();
        loadSubjButton = new JButton();
        createSchedButton = new JButton();
        schedTable = new JXTable();
        schedScroll = new JScrollPane();
        //=========================//
        pickCourseLabel.setText("Pick Course:");
        pickCourseLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(pickCourseLabel, FONT_SIZE);

        addComponent(pickCourseLabel);
        pickCourseLabel.setBounds(SIDE_BORDER_WIDTH + TOP_LEFT_GAP, TITLE_BUTTON_HEIGHT + TUtilities.GAP, 
                TUtilities.getPrefWidth(pickCourseLabel), pickCourseLabel.getFont().getSize());
        //=========================//
        pickCourseCombo.setModel(new DefaultComboBoxModel(getCourses()));
        pickCourseCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                pickCourseCombo_ActionPerformed();
            }
        });
        TUtilities.setPlainFont(pickCourseCombo, FONT_SIZE);
        
        addComponent(pickCourseCombo);
        pickCourseCombo.setBounds(LEFT_GAP, TUtilities.toMiddleOfLabel(pickCourseLabel, pickCourseCombo), 
                getFrameWidth() - LEFT_GAP - TUtilities.GAP,
                (int) pickCourseCombo.getPreferredSize().getHeight());
        //=========================//
        pickYearLabel.setText("Pick Year Level:");
        pickYearLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(pickYearLabel, FONT_SIZE);
        
        addComponent(pickYearLabel);
        pickYearLabel.setBounds(pickCourseLabel.getX(), TUtilities.belowOf(pickCourseCombo) + TOP_LEFT_GAP, 
                TUtilities.getPrefWidth(pickYearLabel), pickYearLabel.getFont().getSize());
        //=========================//
        TUtilities.setPlainFont(pickYearCombo, FONT_SIZE);
        
        addComponent(pickYearCombo);
        pickYearCombo.setBounds(LEFT_GAP,
                TUtilities.toMiddleOfLabel(pickYearLabel, pickYearCombo),
                COMBO_WIDTH, (int) pickYearCombo.getPreferredSize().getHeight());
        //=========================//
        pickSemLabel.setText("Pick Semester:");
        pickSemLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(pickSemLabel, FONT_SIZE);
        
        addComponent(pickSemLabel);
        pickSemLabel.setBounds(pickCourseLabel.getX(), TUtilities.belowOf(pickYearCombo) + TOP_LEFT_GAP,
                TUtilities.getPrefWidth(pickSemLabel), pickSemLabel.getFont().getSize());
        //=========================//
        pickSemCombo.setModel(new DefaultComboBoxModel(new String[] {"1st Semester", "2nd Semester"}));
        TUtilities.setPlainFont(pickSemCombo, FONT_SIZE);
        
        addComponent(pickSemCombo);
        pickSemCombo.setBounds(LEFT_GAP,
                TUtilities.toMiddleOfLabel(pickSemLabel, pickSemCombo),
                COMBO_WIDTH, (int) pickSemCombo.getPreferredSize().getHeight());
        //=========================//
        maxRegStudLabel.setText("Max Regular Students:");
        maxRegStudLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(maxRegStudLabel, FONT_SIZE);
        
        addComponent(maxRegStudLabel);
        maxRegStudLabel.setBounds(pickCourseLabel.getX(), TUtilities.belowOf(pickSemCombo) + TOP_LEFT_GAP,
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
        maxIrregStudLabel.setBounds(pickCourseLabel.getX(), TUtilities.belowOf(maxRegStudField) + TOP_LEFT_GAP,
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
        loadSubjButton.setText("Load Appropriate Subjects");
        loadSubjButton.setIcon(TUtilities.getIconFromResource(getClass(), "/IconsImages/arrow_down.png"));
        loadSubjButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                loadSubjButton_actionPerformed();
            }
        });
        TUtilities.setBoldFont(loadSubjButton, FONT_SIZE);
        
        addComponent(loadSubjButton);
        loadSubjButton.setBounds(getWidth() - SIDE_BORDER_WIDTH - (BUTTON_WIDTH + BUTTON_WIDTH / 2) - TOP_LEFT_GAP,
                maxIrregStudField.getY(), (BUTTON_WIDTH + BUTTON_WIDTH / 2), BUTTON_HEIGHT);
        System.out.println(loadSubjButton.getHeight());
        //=========================//
        schedForLabel.setText("Schedule For:");
        schedForLabel.setForeground(Color.WHITE);
        TUtilities.setBoldFont(schedForLabel, FONT_SIZE);
        
        addComponent(schedForLabel);
        schedForLabel.setBounds(pickCourseLabel.getX(), TUtilities.belowOf(maxIrregStudField) + TOP_LEFT_GAP,
                getFrameWidth() - (SIDE_BORDER_WIDTH * 2) - (pickCourseLabel.getX() * 2), 
                schedForLabel.getFont().getSize());
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
        schedScroll.setBounds(pickCourseLabel.getX(), TUtilities.belowOf(schedForLabel) + TUtilities.GAP,
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
        tipLabel.setBounds(pickCourseLabel.getX(), createSchedButton.getY() + (createSchedButton.getHeight() / 2),
                getFrameWidth() - createSchedButton.getWidth() - TOP_LEFT_GAP
                - pickCourseLabel.getX(), tipLabel.getFont().getSize());
    }

    @Override
    public void defaultSetup() {
        if(pickCourseCombo.getModel().getSize() != 0){
            pickCourseCombo.setSelectedIndex(0); //set the first item on the list to be initially set
            pickCourseCombo_ActionPerformed(); //and get its year length
        }
        resetTable();
        maxRegStudField.setText("40"); //initially set 40 students to regular
        maxIrregStudField.setText("0"); //and 0 students on irregular
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
                        return false;
                    }
                });
            }
        });
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
            LOG.debug("Preparing normal statement...");
            DB.s = DB.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            LOG.debug("Executing statement...");
            DB.rs = DB.s.executeQuery("SELECT CourseCode FROM CourseTB");
            CourseArray = new String[DB.getRecordCount(DB.rs, "course list")];
            
            LOG.debug("Entering loop to get courses...");
            for(int counter = 0; DB.rs.next(); counter++) {
                CourseArray[counter] = DB.rs.getString(1);
            }
            LOG.debug("loop finished.");

        } catch (ClassNotFoundException | SQLException e) {
            LOG.error("Error in retreiving course list", e);
        } finally {
            DB.close();
        }
        
        LOG.debug("returning course list count: {}", CourseArray.length);
        return CourseArray;
    }
    
    /**
     * Converts the chosen year level for proper
     * and easy query. (1st Year = 1, 2nd Year = 2...)
     * (Limited to 6th Year ONLY.)
     * 
     * @param yearTxt 1st Year-6th Year (String)
     * @return number 1-5
     */
    private int getYearLvl(String yearTxt) {
        int yrlvl = 0;

        switch (yearTxt) {
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
                LOG.error("System is limited to log 6th year courses only.");
        }

        return yrlvl;
    }
    
    /**
     * Query the database to get the proper year length
     * of the selected course.
     * @param courseCode the course code of selected course
     * @return Array of year, maximum of 1st year-5th year.
     */
    public String[] getYearLength(String courseCode) {
        String[] YearLvl = {"Empty"}; //Temporary holder of year length from the selected course. (1st year - 5th year)</editor-fold>
        try {
            DB.open("get course length of: " + courseCode);
            DB.setQuery("SELECT CourseLength FROM CourseTB WHERE CourseCode=?");
            LOG.debug("Preparing statement...");
            DB.ps = DB.conn.prepareStatement(DB.getQuery());
            DB.ps.setString(1, courseCode);
            LOG.debug("Executing Statement...");
            DB.rs = DB.ps.executeQuery();

            if (DB.rs.next()) { //to check if the course has year length. (this cannot be false even once.)
                LOG.debug("Year length of {}: {}", courseCode, DB.rs.getInt(1));
                YearLvl = new String[DB.rs.getInt(1)]; //set the length depends on the course length.
                int counter = 0; //for index of array.
                LOG.debug("Entering loop...");
                while (counter < YearLvl.length) {
                    switch (counter) {
                        case 0:
                            YearLvl[counter] = "1st Year";
                            break;
                        case 1:
                            YearLvl[counter] = "2nd Year";
                            break;
                        case 2:
                            YearLvl[counter] = "3rd Year";
                            break;
                        case 3:
                            YearLvl[counter] = "4th Year";
                            break;
                        case 4:
                            YearLvl[counter] = "5th Year";
                            break;
                        case 5:
                            YearLvl[counter] = "6th Year";
                        default:
                            LOG.error("System is limited to log 6th year courses only.");
                    }
                    counter += 1;
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            LOG.error("Error retreiving length of course");
        } finally {
            DB.close();
        }
        
        LOG.debug("returning with a yearlength of: {}", YearLvl.length);
        return YearLvl;
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
     * Gets the last letter of the certain list of section 
     * from the database that has the same attributes with 
     * the chosen options. (Semester, Course, Year).
     * 
     * @return String letter (A-Z).
     */
    private String getSectionLastLetter() {
        String lastletter = ""; //so when it doesnt have last letter, it will return "";
        try {
            DB.open("get section last letter");
            DB.setQuery("SELECT SectionLetter FROM SectionsTB"
                    + " WHERE Semester=? AND SectionCourse=? AND"
                    + " SectionYear=? ORDER BY SectionLetter DESC");

            LOG.debug("Preparing statement...");
            DB.ps = DB.conn.prepareStatement(DB.getQuery());
            DB.ps.setInt(1, getSemSched(TUtilities.getComboValue(pickSemCombo)));
            DB.ps.setString(2, TUtilities.getComboValue(pickCourseCombo));
            DB.ps.setInt(3, getYearLvl(TUtilities.getComboValue(pickYearCombo)));
            LOG.debug("Executing statement...");
            DB.rs = DB.ps.executeQuery();

            if (DB.rs.next()) {
                lastletter = DB.rs.getString("SectionLetter");
                LOG.debug("Last letter found.");
            }

        } catch (ClassNotFoundException | SQLException ex) {
            LOG.error("Error retrieving last letter.", ex);
        } finally {
            DB.close();
        }

        LOG.debug("returning last letter: {}", lastletter);
        return lastletter;
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
    
    /**
     * Load the appropriate year level when course combo box is selected.
     */
    private void pickCourseCombo_ActionPerformed(){
        pickYearCombo.removeAllItems();
        pickYearCombo.setModel(new DefaultComboBoxModel(
                getYearLength(TUtilities.getComboValue(pickCourseCombo))));
    }
    
    /**
     * Gets all the subjects properly according to what the condition does
     * the user picked and load it on the schedule table. 
     */
    private void loadSubjButton_actionPerformed(){
        boolean isError = false;

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

        if (!isError) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            loadSubjButton.setEnabled(false);
            
            new SwingWorker() {

                @Override
                protected Void doInBackground() {
                    
                    TUtilities.setTip("Building Section...", tipLabel);
                    section = new Section(TUtilities.getComboValue(pickCourseCombo),
                            getYearLvl(TUtilities.getComboValue(pickYearCombo)), getSectionLastLetter());

                    sectionletter = section.getNextLetter();

                    section = new Section(TUtilities.getComboValue(pickCourseCombo),
                            getYearLvl(TUtilities.getComboValue(pickYearCombo)), sectionletter);

                    schedForLabel.setText("Schedule for: " + section.getSectionCode());
                    //===================================================//
                    TUtilities.setTip("Loading Appropriate Subjects...", tipLabel);
                    try {
                        LOG.info("get appropriate subjects for selected course: {}", section.getSectionCode());
                        DB.open();

                        DB.setQuery("SELECT SubjCourse.SubjCode, SubjName FROM SubjectsTB, SubjCourse "
                                + "WHERE SemSched=? AND YearLvl=? AND SubjectsTB.SubjCode=SubjCourse.SubjCode"
                                + " AND CourseCode=?"); //query on getting proper subjects

                        LOG.debug("Preparing statement...");
                        DB.ps = DB.conn.prepareStatement(DB.getQuery(), ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY); //so i can jump tru rows (like getting recordcount). prepstatement default is forward only.

                        DB.ps.setInt(1, getSemSched(TUtilities.getComboValue(pickSemCombo)));
                        DB.ps.setInt(2, getYearLvl(TUtilities.getComboValue(pickYearCombo)));
                        DB.ps.setString(3, TUtilities.getComboValue(pickCourseCombo));

                        LOG.debug("Executing Statement...");
                        DB.rs = DB.ps.executeQuery();

                        subjectsTemp = new String[DB.getRecordCount(DB.rs, "appropriate subjects")][2];

                        int counter = 0;
                        LOG.debug("Entering loop: put all subjects on temporary array");
                        while (DB.rs.next()) {
                            subjectsTemp[counter][0] = DB.rs.getString(1); //SubjCode index
                            subjectsTemp[counter][1] = DB.rs.getString(2); //SubjName index
                            counter++;
                        }
                        LOG.debug("loop finished.");
                    } catch (ClassNotFoundException | SQLException ex) {
                        LOG.error("Error creating or retreiving subjects", ex);
                    } finally {
                        DB.close();
                    }

                    LOG.debug("Scheduling to load subjects on schedule table");
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            schedTable.setModel(new DefaultTableModel(subjectsTemp, SCHEDULE_COL){
                                @Override
                                public boolean isCellEditable(int row, int column) {
                                    return !(column == 0 || column == 1); //subjcode and subjname not editable
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

                            if (schedTable.getRowCount() == 0) {
                                createSchedButton.setEnabled(false);
                                TUtilities.setTip("No Subjects Available.", tipLabel);
                            } else {
                                createSchedButton.setEnabled(true);
                                TUtilities.setTip("Done.", tipLabel);
                            }
                        }
                    });

                    return null;
                }

                @Override
                public void done() {
                    setCursor(null);
                    loadSubjButton.setEnabled(true);
                }
            }.execute();
        }
    }
    
    /**
     * Checks and adds the section and the schedule to the database.
     */
    private void createSchedButton_actionPerformed(){
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
                        getSemSched(TUtilities.getComboValue(pickSemCombo)), "NoSectionToAvoid")){ //check table and the whole room schedules/section schedules.
                    TUtilities.setTip("Conflict has found on other section: " + CONFLICT_SCHEDULE_CHECKER.getConflictSection(), 
                            tipLabel);
                    createSchedButton.setEnabled(true);
                    return null; //exit immediately because conflict was found.
                }
                //=======================================//
                TUtilities.setTip("Preparing Database...", tipLabel);

                int row = schedTable.getModel().getRowCount();
                int col = schedTable.getModel().getColumnCount();
                //boolean isRoomSchedAvailable; //true if row is completed. (time in, time out, day, room) (depreciated)

                section = new Section(TUtilities.getComboValue(pickCourseCombo),
                        getYearLvl(TUtilities.getComboValue(pickYearCombo)), 
                        sectionletter, getSemSched(TUtilities.getComboValue(pickSemCombo)), 0, 0,
                        Integer.parseInt(maxRegStudField.getText()),
                        Integer.parseInt(maxIrregStudField.getText())); //create object for easy getting

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
                        DB.ps.setString(1, section.getSectionCode());
                        DB.ps.setString(2, schedule[0][0]); //Subject Code
                        DB.ps.setInt(3, section.getSemester());

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

                    DB.ps.setString(1, section.getSectionCode());
                    DB.ps.setInt(2, section.getSemester());
                    DB.ps.setInt(3, section.getRegularEnrolled());
                    DB.ps.setInt(4, section.getIrregularEnrolled());
                    DB.ps.setInt(5, section.getRegLimit());
                    DB.ps.setInt(6, section.getIrregLimit());
                    DB.ps.setString(7, section.getCourse());
                    DB.ps.setInt(8, section.getYear());
                    DB.ps.setString(9, section.getLetter());

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
                TUtilities.setTip("Done. " + section.getSectionCode() + " was successfully created.", tipLabel);
                resetTable();
                
                LOG.info("Refreshing Section list on mainGUI...");
                MainForm mainForm = MainForm.getInstance(); //refresh section list.
                mainForm.getSectionList(); //refresh section list.

                return null;
            }

            @Override
            public void done() {
                setCursor(null);
            }
        }.execute();
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
}
