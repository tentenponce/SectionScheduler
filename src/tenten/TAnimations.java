/*
 * Copyright © 2015 by Tenten Ponce - All Rights Reserved.
 * This project or any portion thereof may not be reproduced
 * or used in any manner, without the prior permission 
 * from the owner.
 */
package tenten;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 * Class that makes components move horizontally and vertically. 
 * One TAnimations class per component to simultaneously animate
 * components.
 * @author tenten
 * @version 1.0
 * @since November 2015
 */
public class TAnimations {
    
    public static final int NO_DELAY = 0;
    public static final int MINI_DELAY = 5;
    public static final int SHORT_DELAY = 10;
    public static final int MEDIUM_DELAY = 100;
    public static final int LONG_DELAY = 500;

    private static boolean isLeftRunning; //true if component is moving left else false
    private static boolean isRightRunning; //true if component is moving right else false

    /**
     * Moves component to left. (component X position must be bigger than 
     * where the component X position will go to)
     * 
     * @param to Where your component is going to
     * @param delay delay every increment
     * @param inc number to be added on component X value
     * @param component component to be moved
     */
    public void componentLeft(int to, int delay, int inc, Component component) {
        isLeftRunning = true;
        new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(component.getX() > to && isLeftRunning){
                    component.setLocation(component.getX() - inc, component.getY());
                } else {
                    ((Timer) e.getSource()).stop();
                    if (isLeftRunning) {
                        component.setLocation(to, component.getY());
                    }
                }
            }
        }).start();
    }

    /**
     * Stops the component from moving to the left.
     */
    public void stopComponentLeft(){
        isLeftRunning = false;
    }
    
    /**
     * Moves component to right. (component X position must be smaller than 
     * where the component X position will go to)
     * 
     * @param to Where your component is going to
     * @param delay delay every increment
     * @param inc number to be added on component X value
     * @param component component to be moved
     */
    public void componentRight(int to, int delay, int inc, Component component) {
        isRightRunning = true;
        new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(component.getX() < to && isRightRunning){
                    component.setLocation(component.getX() + inc, component.getY());
                } else {
                    ((Timer) e.getSource()).stop();
                    if (isRightRunning) {
                        component.setLocation(to, component.getY());
                    }
                }
            }
        }).start();
    }
    
    /**
     * stops the component moving to the right.
     */
    public void stopComponentRight(){
        isRightRunning = false;
    }
}
