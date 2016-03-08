/*
 * Copyright © 2015 by Exequiel Egbert V. Ponce - All Rights Reserved.
 * This project or any portion thereof may not be reproduced
 * or used in any manner, without the prior permission 
 * from the owner.
 */
package tenten;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.slf4j.LoggerFactory;

/**
 * Custom JFrame. Undecorated, with custom background and buttons.
 * On the constructor after super(), call init() and defaultSetup()
 * for proper setup of the frame.
 *
 * @author tenten
 */
public abstract class TFrame extends JFrame {
    
    private static final Logger log = (Logger) LoggerFactory.getLogger(TFrame.class); //log purposes
    
    /**
     * Width of the border on left and right
     */
    public static final int SIDE_BORDER_WIDTH = 14;
    /**
     * Height of the title bar
     */
    public static final int TITLE_BAR_HEIGHT = 18;
    
    /**
     * Height of the panel of the buttons on the upper right corner 
     */
    public static final int TITLE_BUTTON_HEIGHT = 50;
    
    /**
     * Height of the border on bottom
     */
    public static final int BOTTOM_BORDER_HEIGHT = 14;
    
    private static final int TITLE_TOP_MARGIN = 5;
    
    private static final int TITLE_BUTTON_WIDTH = 82;
    
    private static final int ACTION_BUTTON_SIZE = 23; //close and minimize button size, they are square so it will only need one value for width and height
    private static final int ACTION_BUTTON_TOP_MARGIN = 10; //close and minimize button margin on top
    private static final int ACTION_BUTTON_MARGIN_LEFT = 10;
    
    private static final int TASK_BAR_Y_POSITION = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height - 3;
    
    private int xMouse; //for dragging
    private int yMouse; //for dragging
    private Robot robot; //for mouse position when its over the task bar
    
    private final JLabel title = new JLabel(); //duh, its the title
    private final JLabel titleButtons = new JLabel(); //min and close buttons
    private final JLabel header = new JLabel(); //label that will move the frame
    private final JLabel minButton = new JLabel(); //minimize button
    private final JLabel closeButton = new JLabel(); //close button
    public final JLabel borderLeft = new JLabel(); //border left, //public because side panel must overlap
    private final JLabel borderBottom = new JLabel(); //border bottom
    private final JLabel borderRight = new JLabel(); //border right
    private final JLabel titleBar = new JLabel(); //title bar 
    private final JLabel bg = new JLabel(); //container for background image of the frame   
    
    /**
     * constructor for custom jframe.
     * 
     * @param width width of the custom frame
     * @param height height of the custom frame
     * @param defaultOperation behavior when frame is close
     * @param bg custom background for the frame
     * @param title title of the frame
     */
    public TFrame(int width, int height, int defaultOperation, ImageIcon bg, String title) {
        setTitle(title);
        this.title.setText(title); //refers to the JLabel
        log.setLevel(Level.ALL);
        log.trace(">> setFrame()");
        setFrame(width, height, defaultOperation, bg);
        log.trace("<< setFrame()");
        log.trace(">> init()");
        try {
            init(); //initialize components
        } catch (NullPointerException e) {
            log.error("Error on initialization of components", e);
            System.exit(0);
        }
        log.trace("<< init()");
        log.trace(">> defaultSetup()");
        defaultSetup(); //default things to be shown on the frame
        log.trace("<< defaultSetup()");
    }

    /**
     * Sets up the custom frame. (must be called first before doing anything
     * on the frame!!!)
     * 
     * @param width width of the frame
     * @param height height of the frame
     * @param defaultOperation behavior when closed
     * @param bgImage background image of the frame
     */
    private void setFrame(int width, int height, int defaultOperation, ImageIcon bgImage) {
        setLayout(null); //null for animations
        setUndecorated(true); //no minimize, maximize or close button
        setDefaultCloseOperation(defaultOperation); //behavior when closed
        
        setSize(width, height); //set custom size
        setLocationRelativeTo(null); //centered frame

        /*Sets the frame movable*/
        try {
            robot = new Robot(); //
        } catch (AWTException ex) {
            log.warn("mouse position warning: ", ex);
        }
        header.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent evt) {
                if (evt.getYOnScreen() >= TASK_BAR_Y_POSITION) { //if the cursor is equal or greater than task bar y position
                    robot.mouseMove(evt.getXOnScreen(), TASK_BAR_Y_POSITION);  //Stop cursor moving lower because frame will not be accessible when its behind the task bar
                }                
                setLocation(evt.getXOnScreen() - xMouse, evt.getYOnScreen() - yMouse); //set location of the frame
            }
        });

        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                /*get position on frame to deduct on location so the frame will not teleport on the 0,0 position*/
                xMouse = evt.getX(); 
                yMouse = evt.getY();
            }
        });

        getContentPane().add(header);
        header.setBounds(0, 0, getWidth() - TITLE_BUTTON_WIDTH, TITLE_BAR_HEIGHT);
        
        /*Sets title*/
        title.setFont(new Font("Calibri", Font.BOLD, 10));
        title.setForeground(Color.WHITE);
        getContentPane().add(title);
        title.setBounds(SIDE_BORDER_WIDTH, TITLE_TOP_MARGIN, (int) title.getPreferredSize().getWidth(), 10);
        
        /*Sets minimize and close button*/
        minButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        minButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                setState(TFrame.ICONIFIED);
            }
        });
        closeButton.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent evt) {
                closeButton_mouseClicked();
            }
        });
        
        getContentPane().add(minButton);
        getContentPane().add(closeButton);
        
        closeButton.setBounds(getWidth() - ACTION_BUTTON_SIZE - ACTION_BUTTON_MARGIN_LEFT, ACTION_BUTTON_TOP_MARGIN, 
                ACTION_BUTTON_SIZE, ACTION_BUTTON_SIZE);
        minButton.setBounds(closeButton.getX() - ACTION_BUTTON_SIZE - 3, ACTION_BUTTON_TOP_MARGIN, ACTION_BUTTON_SIZE, ACTION_BUTTON_SIZE);
        
        /*sets the minimize and maximize button position on the frame*/
        titleButtons.setIcon(new ImageIcon(getClass().getResource("/IconsImages/titlebuttons.png")));
        getContentPane().add(titleButtons);
        titleButtons.setBounds(getWidth() - titleButtons.getIcon().getIconWidth(), 0, 
                TITLE_BUTTON_WIDTH, titleButtons.getIcon().getIconHeight());

        /*set the title bar width and position on the frame*/
        titleBar.setIcon(resizedIconWidth(getWidth(), new ImageIcon(getClass().getResource("/IconsImages/titlebar.png"))));
        getContentPane().add(titleBar);
        titleBar.setBounds(0, 0,
                getWidth(), new ImageIcon(getClass().getResource("/IconsImages/titlebar.png")).getIconHeight());
        
        /*set the height and the position of border on the left on the frame*/
        borderLeft.setIcon(resizedIconHeight(getHeight(), new ImageIcon(getClass().getResource("/IconsImages/borderLeft.png"))));
        getContentPane().add(borderLeft);
        borderLeft.setBounds(0, 0, new ImageIcon(getClass().getResource("/IconsImages/borderLeft.png")).getIconWidth(), getHeight());
        
        /*set the height and the position of border on the right on the frame*/
        borderRight.setIcon(resizedIconHeight(getHeight(), new ImageIcon(getClass().getResource("/IconsImages/borderRight.png"))));
        getContentPane().add(borderRight);
        borderRight.setBounds(getWidth() - new ImageIcon(getClass().getResource("/IconsImages/borderLeft.png")).getIconWidth(),
                0, new ImageIcon(getClass().getResource("/IconsImages/borderLeft.png")).getIconWidth(), getHeight());

        /*set the width and the position of the border on the bottom on the frame*/
        borderBottom.setIcon(resizedIconWidth(getWidth(), new ImageIcon(getClass().getResource("/IconsImages/borderBottom.png"))));
        getContentPane().add(borderBottom);
        borderBottom.setBounds(0, getHeight() - new ImageIcon(getClass().getResource("/IconsImages/borderBottom.png")).getIconHeight(),
                getWidth(), new ImageIcon(getClass().getResource("/IconsImages/borderBottom.png")).getIconHeight());
        
        /*set the background image on the frame*/
        bg.setIcon(resizedIcon(getWidth(), getHeight(), bgImage));
        getContentPane().add(bg);
        bg.setBounds(0, 0, getWidth(), getHeight());
    }
    
    /**
     * Put the components position and setup here
     */
    public abstract void init() throws NullPointerException;
    
    /**
     * Default setup of the frame
     */
    public abstract void defaultSetup();
    
    /**
     * Sets frame and properly Display it
     */
    public void displayFrame(){
        SwingUtilities.invokeLater(new Runnable() { //schedule displaying GUI on Event Dispatch Thread for safety of the thread
            @Override
            public void run(){
                setVisible(true);
            }
        });
    }
    
    /**
     * Action when close/x button is clicked.
     */
    public void closeButton_mouseClicked(){
        log.debug("Disposing {}", getTitle());
        dispose();
        log.trace("Disposed");
    }
    
    /**
     * Change the default icon of the frame
     * @param iconPath path of the icon from the package
     * example: "/yourPackage/sampleIcon.png"
     */
    public void setIcon(String iconPath) {
        try {
            URL url = ClassLoader.getSystemResource(iconPath);
            Toolkit kit = Toolkit.getDefaultToolkit();
            Image img = kit.createImage(url);
            setIconImage(img);
        } catch (Exception e) {
            log.warn("Error changing icon for {}", getTitle(), e);
        }
        
    }
    
    /**
     * Adds component on frame and set Z index for overlapping
     * the lower index component will be the one who will overlaps
     * on the higher index component.
     * 
     * @param component the component to be add
     */
    public void addComponent(Component component) {
        try {
            getContentPane().add(component); //add it on frame
            getContentPane().setComponentZOrder(component,
                    getContentPane().getComponentZOrder(bg)); //above the background
        }catch (IllegalArgumentException e){
            log.error("set the frame first");
            System.exit(0);
        }
        
    }
    
    /**
     * Adds component on frame and on top of another component
     * @param top component on top
     * @param below component to be covered by top component
     */
    public void addComponentOnTopOf(Component top, Component below){
        try{
            getContentPane().add(top); //add it on frame
            getContentPane().setComponentZOrder(top, getContentPane().getComponentZOrder(below)); //set Z index for the overlapping
        }catch (IllegalArgumentException e){
            log.error("set the frame first");
            System.exit(0);
        }
    }
    
    /**
     * Gets the frame height excluding border
     * @return height of the frame excluding border
     */
    public int getFrameHeight(){
        return getHeight() - (TITLE_BAR_HEIGHT + BOTTOM_BORDER_HEIGHT);
    }
    
    /**
     * Gets the frame width excluding the border
     * @return width of the frame excluding border
     */
    public int getFrameWidth(){
        return getWidth() - (SIDE_BORDER_WIDTH * 2); //times 2, left and right border
    }
    
    /**
     * Resize ImageIcon to desired size. (Method from TUtilities)
     *
     * @param newWidth desired width
     * @param newHeight desired height
     * @param origIcon icon to be resize
     * @return resized ImageIcon
     */
    public static ImageIcon resizedIcon(int newWidth, int newHeight, ImageIcon origIcon) {
        Image img = origIcon.getImage();
        BufferedImage bi = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.drawImage(img, 0, 0, newWidth, newHeight, null);
        return new ImageIcon(bi);
    }
    
    /**
     * Resize only the height of the image icon.
     * 
     * @param newHeight desired height
     * @param origIcon icon to be resize
     * @return resized ImageIcon
     */
    public static ImageIcon resizedIconHeight(int newHeight, ImageIcon origIcon) {
        Image img = origIcon.getImage();
        BufferedImage bi = new BufferedImage(origIcon.getIconWidth(), newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.drawImage(img, 0, 0, origIcon.getIconWidth(), newHeight, null);
        return new ImageIcon(bi);
    }
    
    /**
     * Resize only the width of the image icon.
     * 
     * @param newWidth desired width
     * @param origIcon icon to be resize
     * @return resized ImageIcon
     */
    public static ImageIcon resizedIconWidth(int newWidth, ImageIcon origIcon) {
        Image img = origIcon.getImage();
        BufferedImage bi = new BufferedImage(newWidth, origIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.drawImage(img, 0, 0, newWidth, origIcon.getIconHeight(), null);
        return new ImageIcon(bi);
    }
}
