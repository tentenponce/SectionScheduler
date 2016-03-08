/*
 * Copyright © 2015 by Exequiel Egbert V. Ponce - All Rights Reserved.
 * This project or any portion thereof may not be reproduced
 * or used in any manner, without the prior permission 
 * from the owner.
 */
package tenten;

import ch.qos.logback.classic.Logger;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.slf4j.LoggerFactory;

/**
 *  Just a random methods that I commonly used.
 * @author tenten
 */
public final class TUtilities {
    
    public static final Logger log = (Logger) LoggerFactory.getLogger(TUtilities.class); //log purposes
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("hh:mm a"); // used for formatting and parsing
    public static final String[] DAYS = {"", "Sunday", "Monday", "Tuesday",
        "Wednesday", "Thursday", "Friday", "Saturday"};
    
    public static final int GAP = 10; //my gap when creating my GUIs
    
    /**
     * private constructor to avoid creating new instances
     */
    private TUtilities(){}
    
    /**
     * Converts util.Date to sql.Date. (for PreparedStatements)
     *
     * @param d util.Date to be converted
     * @return converted util.Date (sql.Date)
     */
    public java.sql.Date getSQLDate(String d) {
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
    
    /**
     * Returns preferred width of the component. (Like wrapping the text)
     * @param component preferred width of this component
     * @return preferred width of the component
     */
    public static int getPrefWidth(JComponent component){
        return (int) component.getPreferredSize().getWidth();
    }
    
    /**
     * Returns preferred height of the component.
     * @param component preferred width of this component
     * @return preferred height of the component
     */
    public static int getPrefHeight(JComponent component){
        return (int) component.getPreferredSize().getHeight();
    }
    
    /**
     * Returns the x point of the top of the component
     * @param cBelow the component below 
     * @param componentHeight the height of the component to be on top
     * @return x point of the top 
     */
    public static int onTopOf(JComponent cBelow, int componentHeight){
        return cBelow.getY() - componentHeight;
    }
    /**
     * Returns the x point of the bottom of the component
     * @param component bottom of the this component
     * @return x point of the bottom
     */
    public static int belowOf(JComponent component){
        return component.getY() + component.getHeight();
    }
    
    /**
     * Returns the x point of the right side of the component
     * @param component right side of this component
     * @return x point of the right side
     */
    public static int toRightOf(JComponent component){
        return component.getX() + component.getWidth();
    }
    
    /**
     * For Combo Boxes and text fields because combo box 
     * and text fields are bigger than label.
     * Returns the y point of JLabel minus its half height 
     * for the combo box/text field will be positioned center horizontal.
     * @param jl where we will align the combo box/text field
     * @param jcb combo box or text field
     * @return y point minus the half height of the label
     */
    public static int toMiddleOfLabel(JLabel jl, JComponent jcb){
        return jl.getY() - (jl.getHeight() / 2);
    }
    
    /**
     * Get the value of the selected item on a combo box and
     * convert it to string.
     * @param jcb The combo box where the selected item is
     * @return the string value of the selected item
     */
    public static String getComboValue(JComboBox jcb){
        return String.valueOf(jcb.getSelectedItem());
    }
    
    /**
     * sets message on a JLabel
     * @param msg message to be shown on the JLabel
     * @param jl Where the message will pop up
     */
    public static void setTip(String msg, JLabel jl){
        jl.setText(msg);
        if(!msg.equals("")){
            log.info(msg);
        }
    }
    
    /**
     * Sets the component to plain font
     * @param component component to set font
     * @param size size of the font
     */
    public static void setPlainFont(Component component, int size){
        component.setFont(new Font("Calibri", Font.PLAIN, size));
    }
    
    /**
     * Sets the component to bold font
     * @param component component to set font
     * @param size size of the font
     */
    public static void setBoldFont(Component component, int size){
        component.setFont(new Font("Calibri", Font.BOLD, size));
    }
    
    /**
     * Sets the border of the component to drop shadow
     * @param component JComponent that will have the drop shadow effect
     */
    public static void setDropShadow(JComponent component){
        DropShadowBorder dropShadow = new DropShadowBorder();
        component.setBorder(dropShadow);
    }
    
    /**
     * Sets the border of the component to drop shadow
     * and sets also where shadow will pop
     * @param component JComponent that will have the drop shadow effect
     * @param top true if there will be shadow on top
     * @param right true if there will be shadow on right
     * @param bottom true if there will be shadow on bottom
     * @param left true if there will be shadow on left
     */
    public static void setDropShadow(JComponent component, boolean top, boolean right,
            boolean bottom, boolean left){
        DropShadowBorder dropShadow = new DropShadowBorder();
        dropShadow.setShowTopShadow(top);
        dropShadow.setShowRightShadow(right);
        dropShadow.setShowBottomShadow(bottom);
        dropShadow.setShowLeftShadow(left);
        component.setBorder(dropShadow);
    }
    
    /**
     * Sets Line Border on the component
     * @param component component that will have line border
     * @param color color of the line border
     */
    public static void setLineBorder(JComponent component, Color color){
        component.setBorder(new LineBorder(color, 1));
    }
    
    /**
     * Resized ImageIcon to desired size.
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
     * Get Image from package/resource cleanly.
     * @param c The class where the code was
     * @param path Package of the image
     * @return the image
     */
    public static ImageIcon getIconFromResource(Class c, String path){
        return new ImageIcon(c.getResource(path));
    }
}
