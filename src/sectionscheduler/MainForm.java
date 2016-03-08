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
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.slf4j.LoggerFactory;
import tenten.TAnimations;
import tenten.TFrame;
import tenten.TUtilities;

/**
 * This class displays information about the sections and their corresponding
 * schedules (finished or not) and also serves as the main form/root that will
 * be always visible that holds all of the other class to be showed.
 *
 * @author Tenten Ponce
 * @version 1.0
 * @since November 2015
 */
public final class MainForm extends TFrame{
    
    //<editor-fold defaultstate="collapsed" desc="Variables">
    private static MainForm INSTANCE; //singleton purposes

    private static final Logger LOG = (Logger) LoggerFactory.getLogger(MainForm.class); //log purposes
    private static final DatabaseConnection DB = DatabaseConnection.getInstance(); //My Database
    private final TAnimations ANIM = new TAnimations();

    private static final String[] SECTION_TABLE_COLUMN = {"SectionCode", "Semester",
        "Regular Enrolled", "Irregular Enrolled",
        "Regular Limit", "Irregular Limit"}; //Columns For section Table
    private static final String[] SCHED_TABLE_COLUMN = {"Subject Code", "Subject Name",
        "Time Start", "Time End", "Day", "Room", "Professor"}; //Columns for schedule table
    private int semester; //temporary holder for semester conversion of number

    private static final int FONT_SIZE = 15; //my default font size for this form
        
    private static final int REF_BUTTON_WIDTH = 180; //refresh button width
    private static final int REF_BUTTON_HEIGHT = 32; //refresh button height
    
    private static final int TREE_FONT_SIZE = 15; //font size of the side panel tree
    private static final int SIDE_PANEL_WIDTH = 230; //side panel (JPanel) width
    private static final int SIDE_PANEL_TREE_WIDTH = 225; //side panel (JXTree) width
    private static final int SIDE_PANEL_HIDE = 10; //width of the side panel when hidden on the left
    private static final int SIDE_PANEL_ROW_HEIGHT = 30; //row height of each node of the tree (side panel)

    private static final int SECTION_TABLE_SCROLL_HEIGHT = 190; //section table height (JScrollPane)

    private static final int SECTION_SCHED_LABEL_HEIGHT = 40; //section schedule label height
    private static final int SECTION_SCHED_LABEL_WIDTH = 170; //section schedule label width

    private static final int SECTION_SCHED_GAP = 10; // gap of section and section schedule label from left

    private static String[][] sectionTemp = {}; //Section Datas Variable
    private static String[][] schedTemp = {}; //Section Schedule Datas Variable

    private static AddSectionForm addsectionGUI;
    private static EditSectionForm editsectionGUI;
    private static CustomSectionForm customSectionGUI;
    private static AddSubjectForm addsubjectGUI;
    private static EditSubjectForm editsubjectGUI;
    private static DeleteSubjectForm deletesubjectGUI;
    private static RoomSchedForm roomSchedGUI;
    private static RoomAvailableForm roomAvailGUI;

    private JScrollPane sidepanelScroll;
    private JScrollPane sectionTableScroll;
    private JScrollPane sectionSchedTableScroll;
    private JXTree sidepanelTree;
    private JPanel sidePanel;
    private JLabel tipLabel;
    private JLabel sectionLabel; //just a label (with line border)
    private JLabel sectionScheduleLabel; //just a label (with line border)
    private JLabel sectionShowSchedLabel; //section that the schedule is being shown
    private JButton refButton;
    private JButton loadSchedButton;
    private JTable sectionTable;
    private JTable sectionSchedTable;
    //</editor-fold>   

    /**
     * private constructor so no one can get a new instance because this is a
     * singleton
     */
    private MainForm(int width, int height, int defaultOperation, ImageIcon bg, String title) {
        super(width, height, defaultOperation, bg, title); //sets the frame first before adding anything!
        LOG.setLevel(Level.ALL); //include trace level
        setIcon("IconsImages/application_side_boxes.png");
    }
    
    @Override
    public void init() {
        sidepanelScroll = new JScrollPane();
        sectionTableScroll = new JScrollPane();
        sectionSchedTableScroll = new JScrollPane();
        sidepanelTree = new JXTree();
        sidePanel = new JPanel();
        tipLabel = new JLabel();
        sectionLabel = new JLabel();
        sectionScheduleLabel = new JLabel();
        sectionShowSchedLabel = new JLabel();
        refButton = new JButton();
        loadSchedButton = new JButton();
        sectionTable = new JTable();
        sectionSchedTable = new JTable();
        //=========================//
        sidepanelTree.setRolloverEnabled(true); //for highlighting effect
        sidepanelTree.setRootVisible(false);
        sidepanelTree.setRowHeight(30);
        sidepanelTree.setToggleClickCount(1); //one click only to toggle action
        sidepanelTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                panelShowLabel_MouseClicked();
            }

            @Override
            public void mouseEntered(MouseEvent evt) {
                panelShowLabel_MouseEntered();
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                panelShowLabel_MouseExited();
            }
        });
        TUtilities.setPlainFont(sidepanelTree, 20);
        //=========================//
        sidepanelScroll.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                panelShowLabel_MouseClicked();
            }

            @Override
            public void mouseEntered(MouseEvent evt) {
                panelShowLabel_MouseEntered();
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                panelShowLabel_MouseExited();
            }
        });
        sidepanelScroll.setViewportView(sidepanelTree);
        TUtilities.setDropShadow(sidepanelScroll, false, true, false, false); //right shadow only
        //=========================//
        //=========================//
        sidePanel.setLayout(null); //for animation to work
        sidePanel.setOpaque(false); //transparent Panel

        sidePanel.add(sidepanelScroll); //add side panel scroll on a jpanel
        sidepanelScroll.setBounds(SIDE_PANEL_HIDE - SIDE_PANEL_TREE_WIDTH, 0, SIDE_PANEL_TREE_WIDTH, //set initial position hidden on the left of the frame
                getFrameHeight());

        addComponentOnTopOf(sidePanel, borderLeft);
        sidePanel.setBounds(TFrame.SIDE_BORDER_WIDTH, TFrame.TITLE_BAR_HEIGHT, SIDE_PANEL_WIDTH,
                getFrameHeight());
        //=========================//
        tipLabel.setForeground(Color.WHITE);
        tipLabel.setHorizontalAlignment(SwingConstants.RIGHT); //align right
        TUtilities.setPlainFont(tipLabel, FONT_SIZE);

        addComponent(tipLabel);
        tipLabel.setBounds(TFrame.SIDE_BORDER_WIDTH + TUtilities.GAP,
                getHeight() - TFrame.BOTTOM_BORDER_HEIGHT - tipLabel.getFont().getSize(),
                getFrameWidth() - (TUtilities.GAP * 2), //right and left gaps
                tipLabel.getFont().getSize());
        //=========================//
        refButton.setIcon(TUtilities.getIconFromResource(getClass(), "/IconsImages/Refresh.gif"));
        refButton.setText("Refresh Sections");
        refButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getSectionList();
            }
        });
        TUtilities.setPlainFont(refButton, FONT_SIZE);

        addComponent(refButton);
        refButton.setBounds(getFrameWidth() + TFrame.SIDE_BORDER_WIDTH - REF_BUTTON_WIDTH - TUtilities.GAP,
                TFrame.TITLE_BUTTON_HEIGHT, REF_BUTTON_WIDTH, REF_BUTTON_HEIGHT);
        //=========================//
        sectionLabel.setText("Section");
        sectionLabel.setForeground(Color.white);
        sectionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        TUtilities.setPlainFont(sectionLabel, 18);
        TUtilities.setLineBorder(sectionLabel, Color.white);

        addComponent(sectionLabel);
        sectionLabel.setBounds(TFrame.SIDE_BORDER_WIDTH + TUtilities.GAP + SECTION_SCHED_GAP,
                refButton.getY(), SECTION_SCHED_LABEL_WIDTH, SECTION_SCHED_LABEL_HEIGHT);
        //=========================//
        sectionTable.setRowHeight(30);
        sectionTable.getTableHeader().setResizingAllowed(false);
        sectionTable.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer centerTableHeader = (DefaultTableCellRenderer) sectionTable.getTableHeader().getDefaultRenderer();
        centerTableHeader.setHorizontalAlignment(JLabel.CENTER);
        TUtilities.setPlainFont(sectionTable, FONT_SIZE);
        TUtilities.setBoldFont(sectionTable.getTableHeader(), FONT_SIZE);
        //=========================//
        sectionTableScroll.setViewportView(sectionTable);
        addComponent(sectionTableScroll);
        sectionTableScroll.setBounds(TFrame.SIDE_BORDER_WIDTH + TUtilities.GAP, refButton.getY() + refButton.getHeight() + TUtilities.GAP,
                getFrameWidth() - (TUtilities.GAP * 2), SECTION_TABLE_SCROLL_HEIGHT);
        //=========================//
        sectionScheduleLabel.setText("Section Schedule");
        sectionScheduleLabel.setForeground(Color.white);
        sectionScheduleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        TUtilities.setPlainFont(sectionScheduleLabel, 18);
        TUtilities.setLineBorder(sectionScheduleLabel, Color.white);

        addComponent(sectionScheduleLabel);
        sectionScheduleLabel.setBounds(sectionLabel.getX(),
                TUtilities.belowOf(sectionTableScroll) + TUtilities.GAP,
                SECTION_SCHED_LABEL_WIDTH, SECTION_SCHED_LABEL_HEIGHT);
        //=========================//
        sectionShowSchedLabel.setForeground(Color.white);
        TUtilities.setPlainFont(sectionShowSchedLabel, 18);

        addComponent(sectionShowSchedLabel);
        sectionShowSchedLabel.setBounds(TUtilities.toRightOf(sectionScheduleLabel) + TUtilities.GAP,
                sectionScheduleLabel.getY(), SECTION_SCHED_LABEL_WIDTH, SECTION_SCHED_LABEL_HEIGHT);
        //=========================//
        sectionSchedTable.setRowHeight(SIDE_PANEL_ROW_HEIGHT);
        sectionSchedTable.setEnabled(false);
        centerTableHeader = (DefaultTableCellRenderer) sectionSchedTable.getTableHeader().getDefaultRenderer();
        centerTableHeader.setHorizontalAlignment(JLabel.CENTER);
        TUtilities.setPlainFont(sectionSchedTable, FONT_SIZE);
        TUtilities.setBoldFont(sectionSchedTable.getTableHeader(), FONT_SIZE);
        //=========================//
        sectionSchedTableScroll.setViewportView(sectionSchedTable);

        addComponent(sectionSchedTableScroll);
        sectionSchedTableScroll.setBounds(sectionTableScroll.getX(),
                sectionScheduleLabel.getY() + sectionScheduleLabel.getHeight() + TUtilities.GAP,
                sectionTableScroll.getWidth(),
                getFrameHeight() - (sectionScheduleLabel.getY() + sectionScheduleLabel.getHeight()) - (TUtilities.GAP * 2));

        //=====<editor-fold defaultstate="collapsed" desc="SETUP FOR HIDDEN ROOT">=======//
        //LEGACY : >> means that the node is inside of the node that was set before it.
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("hidden root");
        DefaultTreeModel defaultTreeModel = new DefaultTreeModel(root);

        sidepanelTree.setModel(defaultTreeModel);

        DefaultMutableTreeNode rootNode, sectionNode, studentNode, subjectNode, regStudNode, irregStudNode, roomNode, otherNode; //the parent
        DefaultMutableTreeNode node; //the child

        rootNode = (DefaultMutableTreeNode) defaultTreeModel.getRoot();
        sectionNode = new DefaultMutableTreeNode("Section");
        addNodeToDefaultTreeModel(defaultTreeModel, rootNode, sectionNode); //add to root

        /*>>*/node = new DefaultMutableTreeNode("Add Section");
        /*>>*/addNodeToDefaultTreeModel(defaultTreeModel, sectionNode, node); //add to section (as parent)
        /*>>*/node = new DefaultMutableTreeNode("Edit Section");
        /*>>*/addNodeToDefaultTreeModel(defaultTreeModel, sectionNode, node);
        /*>>*/node = new DefaultMutableTreeNode("Delete Section");
        /*>>*/addNodeToDefaultTreeModel(defaultTreeModel, sectionNode, node);
        /*>>*/node = new DefaultMutableTreeNode("Custom Section");
        /*>>*/addNodeToDefaultTreeModel(defaultTreeModel, sectionNode, node);

        studentNode = new DefaultMutableTreeNode("Student");
        addNodeToDefaultTreeModel(defaultTreeModel, root, studentNode);

        /*>>*/regStudNode = new DefaultMutableTreeNode("Regular Student");
        /*>>*/addNodeToDefaultTreeModel(defaultTreeModel, studentNode, regStudNode);

        /*>>*//*>>*/node = new DefaultMutableTreeNode("Add Student");
        /*>>*//*>>*/addNodeToDefaultTreeModel(defaultTreeModel, regStudNode, node);
        /*>>*//*>>*/node = new DefaultMutableTreeNode("Remove Student");
        /*>>*//*>>*/addNodeToDefaultTreeModel(defaultTreeModel, regStudNode, node);

        /*>>*/irregStudNode = new DefaultMutableTreeNode("Irregular Student");
        /*>>*/addNodeToDefaultTreeModel(defaultTreeModel, studentNode, irregStudNode);

        /*>>*//*>>*/node = new DefaultMutableTreeNode("Add Student");
        /*>>*//*>>*/addNodeToDefaultTreeModel(defaultTreeModel, irregStudNode, node);
        /*>>*//*>>*/node = new DefaultMutableTreeNode("Remove Student");
        /*>>*//*>>*/addNodeToDefaultTreeModel(defaultTreeModel, irregStudNode, node);

        subjectNode = new DefaultMutableTreeNode("Subject");
        addNodeToDefaultTreeModel(defaultTreeModel, rootNode, subjectNode);

        /*>>*/node = new DefaultMutableTreeNode("Add Subject");
        /*>>*/addNodeToDefaultTreeModel(defaultTreeModel, subjectNode, node);
        /*>>*/node = new DefaultMutableTreeNode("Edit Subject");
        /*>>*/addNodeToDefaultTreeModel(defaultTreeModel, subjectNode, node);
        /*>>*/node = new DefaultMutableTreeNode("Delete Subject");
        /*>>*/addNodeToDefaultTreeModel(defaultTreeModel, subjectNode, node);

        roomNode = new DefaultMutableTreeNode("Room");
        addNodeToDefaultTreeModel(defaultTreeModel, rootNode, roomNode);

        /*>>*/node = new DefaultMutableTreeNode("Add Room");
        /*>>*/addNodeToDefaultTreeModel(defaultTreeModel, roomNode, node);
        /*>>*/node = new DefaultMutableTreeNode("Edit Room");
        /*>>*/addNodeToDefaultTreeModel(defaultTreeModel, roomNode, node);
        /*>>*/node = new DefaultMutableTreeNode("Room Schedule");
        /*>>*/addNodeToDefaultTreeModel(defaultTreeModel, roomNode, node);
        /*>>*/node = new DefaultMutableTreeNode("Room Availability");
        /*>>*/addNodeToDefaultTreeModel(defaultTreeModel, roomNode, node);
        
        otherNode = new DefaultMutableTreeNode("Others");
        addNodeToDefaultTreeModel(defaultTreeModel, rootNode, otherNode);
        
        /*>>*/node = new DefaultMutableTreeNode("Settings");
        /*>>*/addNodeToDefaultTreeModel(defaultTreeModel, otherNode, node);
        /*>>*/node = new DefaultMutableTreeNode("About");
        /*>>*/addNodeToDefaultTreeModel(defaultTreeModel, otherNode, node);

        sidepanelTree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                    boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
                        hasFocus);

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                String nodeLabel = node.toString();

                switch (nodeLabel) {
                    case "Section":
                        setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/application_cascade.png"));
                        setFont(new Font("Calibri", Font.BOLD, TREE_FONT_SIZE));
                        break;
                    case "Add Section":
                        setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/application_add.png"));
                        setFont(new Font("Calibri", Font.PLAIN, TREE_FONT_SIZE));
                        break;
                    case "Edit Section":
                        setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/application_edit.png"));
                        setFont(new Font("Calibri", Font.PLAIN, TREE_FONT_SIZE));
                        break;
                    case "Delete Section":
                        setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/application_delete.png"));
                        setFont(new Font("Calibri", Font.PLAIN, TREE_FONT_SIZE));
                        break;
                    case "Custom Section":
                        setIcon(TUtilities.getIconFromResource(getClass(), "/IconsImages/application_xp_terminal.png"));
                        TUtilities.setPlainFont(this, TREE_FONT_SIZE);
                        break;
                    case "Student":
                        setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/user.png"));
                        setFont(new Font("Calibri", Font.BOLD, TREE_FONT_SIZE));
                        break;
                    case "Regular Student":
                        setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/user_suit.png"));
                        setFont(new Font("Calibri", Font.BOLD, TREE_FONT_SIZE));
                        break;
                    case "Irregular Student":
                        setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/user_gray.png"));
                        setFont(new Font("Calibri", Font.BOLD, TREE_FONT_SIZE));
                        break;
                    case "Add Student":
                        setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/user_add.png"));
                        setFont(new Font("Calibri", Font.PLAIN, TREE_FONT_SIZE));
                        break;
                    case "Remove Student":
                        setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/user_delete.png"));
                        setFont(new Font("Calibri", Font.PLAIN, TREE_FONT_SIZE));
                        break;
                    case "Subject":
                        setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/book.png"));
                        setFont(new Font("Calibri", Font.BOLD, TREE_FONT_SIZE));
                        break;
                    case "Add Subject":
                        setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/book_add.png"));
                        setFont(new Font("Calibri", Font.PLAIN, TREE_FONT_SIZE));
                        break;
                    case "Edit Subject":
                        setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/book_edit.png"));
                        setFont(new Font("Calibri", Font.PLAIN, TREE_FONT_SIZE));
                        break;
                    case "Delete Subject":
                        setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/book_delete.png"));
                        setFont(new Font("Calibri", Font.PLAIN, TREE_FONT_SIZE));
                        break;
                    case "Room":
                        setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/door.png"));
                        setFont(new Font("Calibri", Font.BOLD, TREE_FONT_SIZE));
                        break;
                    case "Add Room":
                        setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/add.png"));
                        setFont(new Font("Calibri", Font.PLAIN, TREE_FONT_SIZE));
                        setEnabled(false);
                        break;
                    case "Edit Room":
                        setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/pencil.png"));
                        setFont(new Font("Calibri", Font.PLAIN, TREE_FONT_SIZE));
                        setEnabled(false);
                        break;
                    case "Room Schedule":
                        setIcon(TUtilities.getIconFromResource(getClass(), "/IconsImages/calendar.png"));
                        TUtilities.setPlainFont(this, TREE_FONT_SIZE);
                        break;
                    case "Room Availability":
                        setIcon(TUtilities.getIconFromResource(getClass(), "/IconsImages/calendar.png"));
                        TUtilities.setPlainFont(this, TREE_FONT_SIZE);
                        break;
                    case "Others":
                        TUtilities.setBoldFont(this, TREE_FONT_SIZE);
                        setEnabled(false);
                        break;
                    case "Settings":
                        TUtilities.setPlainFont(this, TREE_FONT_SIZE);
                        setEnabled(false);
                        break;
                    case "About":
                        TUtilities.setPlainFont(this, TREE_FONT_SIZE);
                        setEnabled(false);
                        break;
                }

                return this;
            }
        }); //Custom Icon setup

        sidepanelTree.addHighlighter(new ColorHighlighter(HighlightPredicate.ROLLOVER_ROW,
                null, Color.GRAY));
        //======</editor-fold>
        //=========================//
        loadSchedButton.setIcon(TUtilities.getIconFromResource(getClass(), "/IconsImages/arrow_down.png"));
        loadSchedButton.setText("Load Schedule");
        loadSchedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sectionTable.getModel().getRowCount() != 0) { //check if theres atleast 1 section
                    if (sectionTable.getSelectedRows().length < 1) {// check if there's selected section from section table
                        TUtilities.setTip("No Section is Selected.", tipLabel);
                    } else {
                        loadSchedule(String.valueOf(sectionTable.getValueAt(sectionTable.getSelectedRow(), 0))); //get section code of the selected row                                       
                    }
                } else {
                    TUtilities.setTip("No Sections Registered.", tipLabel);
                }
            }
        });
        TUtilities.setPlainFont(loadSchedButton, FONT_SIZE);

        addComponent(loadSchedButton);
        loadSchedButton.setBounds(refButton.getX(), 
                TUtilities.onTopOf(sectionSchedTableScroll, REF_BUTTON_HEIGHT) - TUtilities.GAP,
                REF_BUTTON_WIDTH, REF_BUTTON_HEIGHT);
    }
    
    @Override
    public void defaultSetup(){
        getSectionList(); //initially load all the sections (of course, duh)
        expandAll(sidepanelTree); //expand it on load
    }
    
    @Override
    public void closeButton_mouseClicked(){
        LOG.trace(">> Exit System");
        System.exit(0);
    }

    /**
     * Singleton Class, restricts the instantiation of class to one object.
     *
     * @return instance of the class
     */
    public static MainForm getInstance() {
        synchronized (MainForm.class) {
            if (INSTANCE == null) {
                LOG.debug("Get First and Last Instance of Main Form...");
                INSTANCE = new MainForm(900, 700, JFrame.EXIT_ON_CLOSE, 
                        new ImageIcon(MainForm.class.getResource("/IconsImages/bg.png")), "Main Form");
            }
        }

        return INSTANCE;
    }

    /**
     * The magic behind the hidden root of JTree.
     *
     * @param treeModel Tree Model to be use as platform of nodes
     * @param parentNode Root/Parent Node of the Child Node
     * @param node Child Node (Original link of this method:
     * https://www.daniweb.com/programming/software-development/threads/65014/display-jtree-without-root-node)
     */
    private static void addNodeToDefaultTreeModel(DefaultTreeModel treeModel, DefaultMutableTreeNode parentNode, DefaultMutableTreeNode node) {
        treeModel.insertNodeInto(node, parentNode, parentNode.getChildCount());
        if (parentNode == treeModel.getRoot()) {
            treeModel.nodeStructureChanged((TreeNode) treeModel.getRoot());
        }
    }

    /**
     * Converts the chosen semester to integer for proper and easy query. (1st
     * Semester = 1, 2nd Semester = 2)
     *
     * @param semtext 1st Semester or 2nd Semester.
     * @return 1 or 2
     */
    private int getSemSched(String semtext) {
        int sem;

        if (semtext.equals("1st Semester")) {
            sem = 1;
        } else {
            sem = 2;
        }

        return sem;
    }

    /**
     * Populates the section table with the list of sections and their
     * information. Loads the table with the sections came from database.
     */
    public void getSectionList() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        refButton.setEnabled(false);
        loadSchedButton.setEnabled(false);
        TUtilities.setTip("Loading Sections...", tipLabel);
        new SwingWorker() {
            @Override
            protected Void doInBackground() {
                try {
                    DB.open("for section list");
                    LOG.debug("Preparing normal statement...");
                    DB.s = DB.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    LOG.debug("Executing statement...");
                    DB.rs = DB.s.executeQuery("SELECT * FROM SectionsTB"); //Query the sections.

                    sectionTemp = new String[DB.getRecordCount(DB.rs, "section list")][SECTION_TABLE_COLUMN.length]; //Temporary Holder for data

                    int row = 0;

                    LOG.debug("Entering loop...");
                    while (DB.rs.next()) {
                        //Fill up temporary secionTemp array to fill the table.
                        for (int column = 0; column < SECTION_TABLE_COLUMN.length; column++) {
                            //Try to get AS Integer, if failed, catch with String.
                            try {
                                if (column == 1) { //Semester column is 1 index.
                                    if (DB.rs.getInt("Semester") == 1) {
                                        sectionTemp[row][column] = "1st Semester";
                                    } else {
                                        sectionTemp[row][column] = "2nd Semester";
                                    }
                                } else { //Get if not semester and add Students for proper viewing, catch string.
                                    sectionTemp[row][column] = Integer.toString(DB.rs.getInt(column + 1)) + " Student(s)";
                                }
                            } catch (Exception e) {
                                sectionTemp[row][column] = DB.rs.getString(column + 1);
                            }
                        }
                        row++;
                    }

                    LOG.debug("Loop Finished.");
                    LOG.debug("Scheduling for setting up model of section list...");
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            TUtilities.setTip("Displaying sections...", tipLabel);
                            sectionTable.setModel(new DefaultTableModel(sectionTemp, SECTION_TABLE_COLUMN) {
                                @Override
                                public boolean isCellEditable(int row, int column) {
                                    return false; //Cell will not be editable.
                                }
                            }); // Set Model/Data PROPERLY

                            DefaultTableCellRenderer cellIconRenderer = new DefaultTableCellRenderer() {
                                @Override
                                public Component getTableCellRendererComponent(JTable table,
                                        Object value,
                                        boolean isSelected,
                                        boolean hasFocus,
                                        int row,
                                        int column) {
                                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                    switch (column) {
                                        case 0: //Section
                                            setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/application.png"));
                                            setHorizontalAlignment(JLabel.CENTER);
                                            break;
                                        case 2: //Regular Enrolled
                                            setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/user_suit.png"));
                                            setHorizontalAlignment(JLabel.CENTER);
                                            break;
                                        case 3: //Irregular Enrolled
                                            setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/user_gray.png"));
                                            setHorizontalAlignment(JLabel.CENTER);
                                            break;
                                        default:
                                            setIcon(null);
                                    }
                                    return this;
                                }
                            };

                            //Set Icons beside the text of the table
                            for (int x = 0; x < sectionTable.getColumnCount(); x++) {
                                sectionTable.getColumnModel().getColumn(x).setCellRenderer(cellIconRenderer);
                            }
                            
                            if (sectionTable.getRowCount() != 0) { //initially selects row if the section list is not 0.
                                sectionTable.addRowSelectionInterval(0, 0);
                                sectionTable.requestFocusInWindow(); //request for focus
                            }
                            
                            sectionSchedTable.setModel(new DefaultTableModel(new Object[][]{}, //clear the schedule table
                                    SCHED_TABLE_COLUMN) {
                                        @Override
                                        public boolean isCellEditable(int row, int column) {
                                            return false; //Cell will not be editable.
                                        }
                                    }); // Set Model/Data PROPERLY

                            TUtilities.setTip("", tipLabel);
                        }
                    });

                } catch (ClassNotFoundException | SQLException ex) {
                    LOG.error("Error loading sections", ex);
                } finally {
                    DB.close();
                }

                return null;
            }

            @Override
            public void done() {
                refButton.setEnabled(true);
                loadSchedButton.setEnabled(true);
                setCursor(null);
                sectionShowSchedLabel.setText("");
            }
        }.execute();

    }

    /**
     * Properly load schedule of a section
     * @param sectionCode code of the section to be loaded
     */
    private void loadSchedule(String sectionCode) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        TUtilities.setTip("Loading Schedule...", tipLabel);
        new SwingWorker() {
            @Override
            protected Void doInBackground() {
                sectionShowSchedLabel.setText(sectionCode);

                if (sectionTable.getValueAt(sectionTable.getSelectedRow(), 1).equals("1st Semester")) { //converts the semester of selected course for querying.
                    semester = 1;
                } else {
                    semester = 2;
                }

                try {
                    DB.open("for schedule of the selected section"); //msg
                    DB.setQuery("SELECT SubjectsTB.SubjCode, SubjName, TimeStart, TimeEnd, Day, Room, ProfName"
                            + " FROM SectionSubjTB, SubjectsTB "
                            + "WHERE SectionCode=? AND Semester=? AND SectionSubjTB.SubjCode=SubjectsTB.SubjCode");

                    LOG.debug("preparing statement...");
                    DB.ps = DB.conn.prepareStatement(DB.getQuery(), ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_READ_ONLY);  //so i can jump tru rows (like getting recordcount). prepstatement default is forward only.

                    DB.ps.setString(1, sectionCode);
                    DB.ps.setInt(2, semester);

                    LOG.debug("Executing Statement...");
                    DB.rs = DB.ps.executeQuery();

                    schedTemp = new String[DB.getRecordCount(DB.rs, "proper schedule of selected section")][7]; //As Query column count

                    LOG.debug("Entering Loop...");
                    for (int row = 0; DB.rs.next(); row++) { //loop tru the recordset and put datas on a temporary array.
                        //shortcut for if else, check each value if null to avoid displaying null word.
                        schedTemp[row][0] = (DB.rs.getString("SubjCode") != null) ? DB.rs.getString("SubjCode") : null;
                        schedTemp[row][1] = (DB.rs.getString("SubjName") != null) ? DB.rs.getString("SubjName") : null;
                        schedTemp[row][2] = (DB.rs.getTimestamp("TimeStart") != null) ? TUtilities.FORMAT.format((DB.rs.getTimestamp("TimeStart"))) : null;
                        schedTemp[row][3] = (DB.rs.getTimestamp("TimeEnd") != null) ? TUtilities.FORMAT.format(DB.rs.getTimestamp("TimeEnd")) : null;
                        schedTemp[row][4] = (DB.rs.getString("Day") != null) ? DB.rs.getString("Day") : null;
                        schedTemp[row][5] = (DB.rs.getString("Room") != null) ? DB.rs.getString("Room") : null;
                        schedTemp[row][6] = (DB.rs.getString("ProfName") != null) ? DB.rs.getString("ProfName") : null;
                    }
                    LOG.debug("Loop finished");
                    LOG.debug("scheduling for setting up model of section schedule table...");

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            TUtilities.setTip("Displaying Schedule...", tipLabel);
                            sectionSchedTable.setModel(new DefaultTableModel(schedTemp, SCHED_TABLE_COLUMN) {
                                @Override
                                public boolean isCellEditable(int row, int column) {
                                    return false; //Cell will not be editable.
                                }
                            }); // Set Model/Data PROPERLY
                            TUtilities.setTip("", tipLabel);

                            DefaultTableCellRenderer cellIconRenderer = new DefaultTableCellRenderer() {
                                @Override
                                public Component getTableCellRendererComponent(JTable table, Object value,
                                        boolean isSelected, boolean hasFocus, int row, int column) {
                                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                    switch (column) {
                                        case 0:
                                            setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/book.png"));
                                            break;
                                        case 2:
                                            if (sectionSchedTable.getModel().getValueAt(row, 2) != null) {
                                                setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/time.png"));
                                            } else {
                                                setIcon(null);
                                            }
                                            break;
                                        case 3:
                                            if (sectionSchedTable.getModel().getValueAt(row, 3) != null) {
                                                setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/time.png"));
                                            } else {
                                                setIcon(null);
                                            }
                                            break;
                                        case 4:
                                            if (!sectionSchedTable.getModel().getValueAt(row, 4).equals("") &&
                                                    sectionSchedTable.getModel().getValueAt(row, 4) != null) {
                                                setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/calendar_view_week.png"));
                                            } else {
                                                setIcon(null);
                                            }
                                            break;
                                        case 5:
                                            if (!sectionSchedTable.getModel().getValueAt(row, 5).equals("") && 
                                                    sectionSchedTable.getModel().getValueAt(row, 5) != null) {
                                                setIcon(TUtilities.getIconFromResource(getClass(),"/IconsImages/door.png"));
                                            } else {
                                                setIcon(null);
                                            }
                                            break;                                            
                                        default:
                                            setIcon(null);
                                    }
                                    
                                    return this;
                                }
                            };

                            //set icon for for section schedule table cell
                            for (int x = 0; x < sectionSchedTable.getColumnCount(); x++) {
                                sectionSchedTable.getColumnModel().getColumn(x).setCellRenderer(cellIconRenderer);
                            }
                        }
                    });

                } catch (ClassNotFoundException | SQLException ex) {
                    LOG.warn("Cannot load proper schedule: ", ex);
                } finally {
                    DB.close();
                }
                return null;
            }

            @Override
            public void done() {
                TUtilities.setTip("", tipLabel);
                setCursor(null);
            }

        }.execute();
    }

    /**
     * Expands all in JTree. Loops through the JTree and expands each row.
     *
     * @param tree JTree to be expanded.
     */
    private void expandAll(JXTree tree) {
        int row = 0;
        while (row < tree.getRowCount()) {
            tree.expandRow(row);
            row++;
        }
    }

    /**
     * Animates/Slides side panel to left (show)
     */
    private void panelShowLabel_MouseEntered() {
        ANIM.stopComponentLeft();
        ANIM.componentRight(0, TAnimations.MINI_DELAY,
                5, sidepanelScroll);
    }

    /**
     * Animates/Slides side panel to right (hide)
     */
    private void panelShowLabel_MouseExited() {
        ANIM.stopComponentRight();
        ANIM.componentLeft(SIDE_PANEL_HIDE - sidepanelScroll.getWidth(), TAnimations.MINI_DELAY,
                5, sidepanelScroll);
    }

    /**
     * Handles and execute appropriate action when the side panel(JTree) is
     * clicked.
     */
    private void panelShowLabel_MouseClicked() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) sidepanelTree.getLastSelectedPathComponent(); //gets the selected node.

        if (node == null) {
            return; //do nothing if null.
        }

        String nodeLabel = node.toString(); //get the name of the node clicked.
        String nodeParent = node.getParent().toString(); //get the parent of the node clicked.
                
        switch (nodeLabel) {
            case "Add Section":
                TUtilities.setTip("Loading Add Section Form...", tipLabel);
                if (addsectionGUI == null) { //for first time initialization.
                    LOG.debug("First instance of add section gui...");
                    addsectionGUI = new AddSectionForm(700, 680, DISPOSE_ON_CLOSE, new ImageIcon(getClass().getResource("/IconsImages/bg.png")), "Add Section");
                }

                LOG.debug("disposing old add section gui...");
                addsectionGUI.dispose(); //dispose to completely destroy.
                LOG.debug("creating new instance...");
                addsectionGUI = new AddSectionForm(700, 680, DISPOSE_ON_CLOSE, new ImageIcon(getClass().getResource("/IconsImages/bg.png")), "Add Section"); //for restarting
                addsectionGUI.displayFrame();
                TUtilities.setTip("", tipLabel);
                break;
            case "Edit Section":
                if(sectionTable.getModel().getRowCount() == 0){ //check if there are no sections
                    TUtilities.setTip("No Sections Registered.", tipLabel);
                    break;
                }
                
                if(sectionSchedTable.getModel().getRowCount() == 0){ //check if schedule is loaded
                    TUtilities.setTip("Load the Schedule First.", tipLabel);
                    break;
                }
                
                String sectionCode = String.valueOf(sectionTable.getValueAt(sectionTable.getSelectedRow(), 0));
                if (sectionCode.equals("")) { //must not happen
                    TUtilities.setTip("No Section Selected.", tipLabel);
                    break;
                }
                
                TUtilities.setTip("Loading Edit Section Form...", tipLabel);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (editsectionGUI == null) { //for first time initialization.
                            LOG.debug("First instance of edit section gui...");
                            editsectionGUI = new EditSectionForm(700, 600, DISPOSE_ON_CLOSE, new ImageIcon(getClass().getResource("/IconsImages/bg.png")),
                                    "Edit Section",
                                    (DefaultTableModel) sectionSchedTable.getModel(), sectionShowSchedLabel.getText(),
                                    getSemSched(String.valueOf(sectionTable.getValueAt(sectionTable.getSelectedRow(), 1))));
                        }

                        LOG.debug("disposing old edit section gui...");
                        editsectionGUI.dispose(); //dispose to completely destroy.
                        LOG.debug("creating new instance...");
                        editsectionGUI = new EditSectionForm(700, 600, DISPOSE_ON_CLOSE, new ImageIcon(getClass().getResource("/IconsImages/bg.png")),
                                    "Edit Section",
                                    (DefaultTableModel) sectionSchedTable.getModel(), sectionShowSchedLabel.getText(),
                                    getSemSched(String.valueOf(sectionTable.getValueAt(sectionTable.getSelectedRow(), 1)))); //for restarting
                        editsectionGUI.displayFrame();
                        TUtilities.setTip("", tipLabel);
                    }
                });
                break;

            case "Delete Section":
                if(sectionTable.getModel().getRowCount() == 0){ //check if there are no sections
                    TUtilities.setTip("No Sections Registered.", tipLabel);
                    break;
                }
                
                sectionCode = String.valueOf(sectionTable.getValueAt(sectionTable.getSelectedRow(), 0));
                if (sectionCode.equals("")) { //must not happen
                    TUtilities.setTip("No Section Selected.", tipLabel);
                    break;
                }
                
                TUtilities.setTip("Deleting Section...", tipLabel);
                int result = JOptionPane.showConfirmDialog(getRootPane(),
                        "Are you sure you want to delete " + sectionCode + "?",
                        "Delete Section", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE, null);

                if (result == JOptionPane.YES_OPTION) {
                    if (sectionTable.getValueAt(sectionTable.getSelectedRow(), 1).equals("1st Semester")) { //converts the semester of selected course for querying.
                            semester = 1;
                        } else {
                            semester = 2;
                        }
                    
                    //DELETE FROM SectionSubjTB
                    try {
                        DB.open("Delete Section: " + sectionCode + ", Semester: " + semester);
                        DB.setQuery("DELETE FROM SectionSubjTB WHERE "
                                + "SectionCode='" + sectionCode + "' AND Semester=" + semester);
                        LOG.debug("Preparing Statement...");
                        DB.s = DB.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY);

                        LOG.debug("Executing Statement..");
                        DB.s.executeUpdate(DB.getQuery());
                    } catch (SQLException | ClassNotFoundException ex) {
                        LOG.error("Error deleting section {}", sectionCode, ex);
                    } finally {
                        DB.close();
                    }

                    //DELETE FROM SectionsTB
                    try {
                        DB.open("Delete Section: " + sectionCode + ", Semester: " + semester);
                        DB.setQuery("DELETE FROM SectionsTB WHERE "
                                + "SectionCode='" + sectionCode + "' AND Semester=" + semester);
                        LOG.debug("Preparing Statement...");
                        DB.s = DB.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY);

                        LOG.debug("Executing Statement..");
                        DB.s.executeUpdate(DB.getQuery());
                    } catch (SQLException | ClassNotFoundException ex) {
                        LOG.error("Error deleting section {}", sectionCode, ex);
                    } finally {
                        DB.close();
                    }

                    getSectionList(); //refresh
                } else {
                    tipLabel.setText("");
                }
                break;
            case "Custom Section":
                TUtilities.setTip("Loading Add Section Form...", tipLabel);
                if (customSectionGUI == null) { //for first time initialization.
                    LOG.debug("First instance of add section gui...");
                    customSectionGUI = new CustomSectionForm(700, 680, DISPOSE_ON_CLOSE, 
                            new ImageIcon(getClass().getResource("/IconsImages/bg.png")), "Custom Section");
                }

                LOG.debug("disposing old add section gui...");
                customSectionGUI.dispose(); //dispose to completely destroy.
                LOG.debug("creating new instance...");
                customSectionGUI = new CustomSectionForm(700, 680, DISPOSE_ON_CLOSE, 
                        new ImageIcon(getClass().getResource("/IconsImages/bg.png")), "Custom Section"); //for restarting
                customSectionGUI.displayFrame();
                TUtilities.setTip("", tipLabel);
                
                break;
            case "Add Student":
                if(sectionTable.getModel().getRowCount() == 0){ //check if there are no sections
                    TUtilities.setTip("No Sections Registered.", tipLabel);
                    break;
                }
                
                sectionCode = String.valueOf(sectionTable.getValueAt(sectionTable.getSelectedRow(), 0));                
                if (sectionCode.equals("")) { //must not happen
                    TUtilities.setTip("No Section Selected.", tipLabel);
                    break;
                }
                String regStud = String.valueOf(sectionTable.getValueAt(sectionTable.getSelectedRow(), 2)); //reg column
                String irregStud = String.valueOf(sectionTable.getValueAt(sectionTable.getSelectedRow(), 3)); //irreg column
                 
                String maxReg = String.valueOf(sectionTable.getValueAt(sectionTable.getSelectedRow(), 4)); //max reg column
                String maxIrreg = String.valueOf(sectionTable.getValueAt(sectionTable.getSelectedRow(), 5)); //max iireg column

                int regStudCount = Integer.parseInt(regStud.substring(0, regStud.indexOf(" "))); //get the number only
                int irregStudCount = Integer.parseInt(irregStud.substring(0, irregStud.indexOf(" "))); //get the number only

                int maxRegStudCount = Integer.parseInt(maxReg.substring(0, maxReg.indexOf(" "))); //get the number only
                int maxIrregStudCount = Integer.parseInt(maxIrreg.substring(0, maxIrreg.indexOf(" "))); //get the number only

                if(nodeParent.equals("Regular Student")) {
                    if(regStudCount >= maxRegStudCount) { //check if limit reach
                        JOptionPane.showMessageDialog(getRootPane(), 
                                "Maximum Regular Student Reached.", 
                                "Limit Reached", JOptionPane.WARNING_MESSAGE);
                        break; //exit
                    }
                } else {
                    if(irregStudCount >= maxIrregStudCount) { //check if limit reach
                        JOptionPane.showMessageDialog(getRootPane(), 
                                "Maximum Irregular Student Reached.", 
                                "Limit Reached", JOptionPane.WARNING_MESSAGE);
                        break; //exit
                    }
                }
                
                result = JOptionPane.showConfirmDialog(getRootPane(),
                        "Add " + nodeParent + " to " + sectionCode + "?",
                        "Adding " + nodeParent, JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE, null);
                
                if(result == JOptionPane.NO_OPTION) {
                    break;
                }
                
                int numOfStud;
                 
                if (sectionTable.getValueAt(sectionTable.getSelectedRow(), 1).equals("1st Semester")) { //converts the semester of selected course for querying.
                    semester = 1;
                } else {
                    semester = 2;
                }
                
                try {
                    DB.open("Add number of regular students to section: " + sectionCode);

                    if (nodeParent.equals("Regular Student")) {
                        numOfStud = Integer.parseInt(regStud.substring(0, regStud.indexOf(" ")));
                        DB.setQuery("UPDATE SectionsTB SET RegEnrolled=? WHERE SectionCode=? AND Semester=?");
                    } else {
                        numOfStud = Integer.parseInt(irregStud.substring(0, irregStud.indexOf(" ")));
                        DB.setQuery("UPDATE SectionsTB SET IrregEnrolled=? WHERE SectionCode=? AND Semester=?");
                    }

                    LOG.debug("Preparing Statement...");
                    DB.ps = DB.conn.prepareStatement(DB.getQuery());

                    DB.ps.setInt(1, numOfStud + 1);
                    DB.ps.setString(2, sectionCode);
                    DB.ps.setInt(3, semester);
                    
                    LOG.debug("Executing Update Statement...");
                    DB.ps.executeUpdate();
                } catch (SQLException | ClassNotFoundException ex) {
                    LOG.error("Error updating number of students", ex);
                } finally {
                    DB.close();
                }
                
                refButton.doClick(); //refresh
                break;
            case "Remove Student":
                if(sectionTable.getModel().getRowCount() == 0){ //check if there are no sections
                    TUtilities.setTip("No Sections Registered.", tipLabel);
                    break;
                }
                
                sectionCode = String.valueOf(sectionTable.getValueAt(sectionTable.getSelectedRow(), 0));                
                if (sectionCode.equals("")) { //must not happen
                    TUtilities.setTip("No Section Selected.", tipLabel);
                    break;
                }
                regStud = String.valueOf(sectionTable.getValueAt(sectionTable.getSelectedRow(), 2)); //reg column
                irregStud = String.valueOf(sectionTable.getValueAt(sectionTable.getSelectedRow(), 3)); //irreg column
                 
                regStudCount = Integer.parseInt(regStud.substring(0, regStud.indexOf(" "))); //get the number only
                irregStudCount = Integer.parseInt(irregStud.substring(0, irregStud.indexOf(" "))); //get the number only

                if(nodeParent.equals("Regular Student")) {
                    if(regStudCount <= 0) { //check if limit reach
                        JOptionPane.showMessageDialog(getRootPane(), 
                                "There were no student in this section.", 
                                "Empty Section", JOptionPane.INFORMATION_MESSAGE);
                        break; //exit
                    }
                } else {
                    if(irregStudCount <= 0) { //check if limit reach
                        JOptionPane.showMessageDialog(getRootPane(), 
                                "There were no student in this section.", 
                                "Empty Section", JOptionPane.INFORMATION_MESSAGE);
                        break; //exit
                    }
                }
                
                result = JOptionPane.showConfirmDialog(getRootPane(),
                        "Remove " + nodeParent + " to " + sectionCode + "?",
                        "Removing " + nodeParent, JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE, null);
                
                if(result == JOptionPane.NO_OPTION) {
                    break;
                }
                 
                if (sectionTable.getValueAt(sectionTable.getSelectedRow(), 1).equals("1st Semester")) { //converts the semester of selected course for querying.
                    semester = 1;
                } else {
                    semester = 2;
                }
                
                try {
                    DB.open("Add number of regular students to section: " + sectionCode);

                    if (nodeParent.equals("Regular Student")) {
                        numOfStud = Integer.parseInt(regStud.substring(0, regStud.indexOf(" ")));
                        DB.setQuery("UPDATE SectionsTB SET RegEnrolled=? WHERE SectionCode=? AND Semester=?");
                    } else {
                        numOfStud = Integer.parseInt(irregStud.substring(0, irregStud.indexOf(" ")));
                        DB.setQuery("UPDATE SectionsTB SET IrregEnrolled=? WHERE SectionCode=? AND Semester=?");
                    }

                    LOG.debug("Preparing Statement...");
                    DB.ps = DB.conn.prepareStatement(DB.getQuery());

                    DB.ps.setInt(1, numOfStud - 1);
                    DB.ps.setString(2, sectionCode);
                    DB.ps.setInt(3, semester);
                    
                    LOG.debug("Executing Update Statement...");
                    DB.ps.executeUpdate();
                } catch (SQLException | ClassNotFoundException ex) {
                    LOG.error("Error updating number of students", ex);
                } finally {
                    DB.close();
                }
                
                refButton.doClick(); //refresh
                break;
            case "Add Subject":
                TUtilities.setTip("Loading Add Subject Form...", tipLabel);

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (addsubjectGUI == null) { //for first time initialization.
                            LOG.debug("First instance of add section gui...");
                            addsubjectGUI = new AddSubjectForm(700, 650, DISPOSE_ON_CLOSE, 
                                    TUtilities.getIconFromResource(getClass(), "/IconsImages/bg.png"),
                                    "Add Subject Form");
                        }

                        LOG.debug("disposing old addsubjectGUI gui...");
                        addsubjectGUI.dispose(); //dispose to completely destroy.
                        LOG.debug("creating new instance...");
                        addsubjectGUI = new AddSubjectForm(700, 650, DISPOSE_ON_CLOSE, 
                                    TUtilities.getIconFromResource(getClass(), "/IconsImages/bg.png"),
                                    "Add Subject Form"); //for restarting
                        addsubjectGUI.setVisible(true);
                        TUtilities.setTip("", tipLabel);
                    }
                });
                break;

            case "Edit Subject":
                TUtilities.setTip("Loading Edit Subject Form...", tipLabel);

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (editsubjectGUI == null) { //for first time initialization.
                            LOG.debug("First instance of edit section gui...");
                            editsubjectGUI = new EditSubjectForm(700, 680, DISPOSE_ON_CLOSE, 
                                    TUtilities.getIconFromResource(getClass(), "/IconsImages/bg.png"),
                                    "Edit Subject Form");
                        }

                        LOG.debug("disposing old editsubjectGUI gui...");
                        editsubjectGUI.dispose(); //dispose to completely destroy.
                        LOG.debug("creating new instance...");
                        editsubjectGUI = new EditSubjectForm(700, 680, DISPOSE_ON_CLOSE, 
                                    TUtilities.getIconFromResource(getClass(), "/IconsImages/bg.png"),
                                    "Edit Subject Form"); //for restarting
                        editsubjectGUI.setVisible(true);
                        TUtilities.setTip("", tipLabel);
                    }
                });
                break;

            case "Delete Subject":
                TUtilities.setTip("Loading Delete Subject Form...", tipLabel);

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (deletesubjectGUI == null) { //for first time initialization.
                            LOG.debug("First instance of delete section gui...");
                            deletesubjectGUI = new DeleteSubjectForm(700, 500, DISPOSE_ON_CLOSE, 
                                    TUtilities.getIconFromResource(getClass(), "/IconsImages/bg.png"),
                                    "Delete Subject Form");
                        }

                        LOG.debug("disposing old deletesubjectGUI gui...");
                        deletesubjectGUI.dispose(); //dispose to completely destroy.
                        LOG.debug("creating new instance...");
                        deletesubjectGUI = new DeleteSubjectForm(700, 500, DISPOSE_ON_CLOSE, 
                                    TUtilities.getIconFromResource(getClass(), "/IconsImages/bg.png"),
                                    "Delete Subject Form"); //for restarting
                        deletesubjectGUI.setVisible(true);
                        TUtilities.setTip("", tipLabel);
                    }
                });
                break;
            
            case "Room Schedule":
                TUtilities.setTip("Loading Room Schedule...", tipLabel);
                
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (roomSchedGUI == null) { //for first time initialization.
                            LOG.debug("First instance of room schedule gui...");
                            roomSchedGUI = new RoomSchedForm(700, 500, DISPOSE_ON_CLOSE, 
                                    TUtilities.getIconFromResource(getClass(), "/IconsImages/bg.png"),
                                    "Room Schedule Form");
                        }

                        LOG.debug("disposing old deletesubjectGUI gui...");
                        roomSchedGUI.dispose(); //dispose to completely destroy.
                        LOG.debug("creating new instance...");
                        roomSchedGUI = new RoomSchedForm(700, 500, DISPOSE_ON_CLOSE, 
                                    TUtilities.getIconFromResource(getClass(), "/IconsImages/bg.png"),
                                    "Room Schedule Form"); //for restarting
                        roomSchedGUI.setVisible(true);
                        TUtilities.setTip("", tipLabel);
                    }
                });
                break;
            
            case "Room Availability":
                TUtilities.setTip("Loading Room Availability Form...", tipLabel);
                
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (roomAvailGUI == null) { //for first time initialization.
                            LOG.debug("First instance of room schedule gui...");
                            roomAvailGUI = new RoomAvailableForm(
                                    (RoomAvailableForm.LIST_WIDTH * 7) + (RoomAvailableForm.TOP_LEFT_GAP * 2) + (SIDE_BORDER_WIDTH * 2), 
                                    500,
                                    DISPOSE_ON_CLOSE, 
                                    TUtilities.getIconFromResource(getClass(), "/IconsImages/bg.png"),
                                    "Room Availability Form");
                        }

                        LOG.debug("disposing old deletesubjectGUI gui...");
                        roomAvailGUI.dispose(); //dispose to completely destroy.
                        LOG.debug("creating new instance...");
                        roomAvailGUI = new RoomAvailableForm(
                                (RoomAvailableForm.LIST_WIDTH * 7) + (RoomAvailableForm.TOP_LEFT_GAP * 2) + (SIDE_BORDER_WIDTH * 2), 
                                500,
                                DISPOSE_ON_CLOSE, 
                                    TUtilities.getIconFromResource(getClass(), "/IconsImages/bg.png"),
                                    "Room Availability Form"); //for restarting
                        roomAvailGUI.setVisible(true);
                        TUtilities.setTip("", tipLabel);
                    }
                });
                break;
        }
    }
}
