/*
 * Copyright © 2015 by Tenten Ponce - All Rights Reserved.
 * This project or any portion thereof may not be reproduced
 * or used in any manner, without the prior permission 
 * from the owner.
 */
package sectionscheduler;

import ch.qos.logback.classic.Level;
import java.awt.Font;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import org.slf4j.LoggerFactory;

/**
 * Contains the main method. The first class to run when the system starts.
 *
 * @author Tenten Ponce
 */
public class MainMethod{

    private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(MainMethod.class);

    public static void main(String[] args) {
        log.setLevel(Level.ALL); //all levels
        //Nimbus feel
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            log.warn("Nimbus feel not found", ex);
        }
        
        UIManager.put("OptionPane.messageFont", new FontUIResource(new Font("Calibri", Font.PLAIN, 15)));

        log.info("Starting Section Scheduler...");
        MainForm mainForm = MainForm.getInstance(); //load main form behind
        try {
            Thread.sleep(5000); //for bootsplash
        } catch (InterruptedException ex) {}
        mainForm.displayFrame(); //Properly display frame with EDT
    }
}
