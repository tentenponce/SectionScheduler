/*
 * Copyright © 2015 by Tenten Ponce - All Rights Reserved.
 * This project or any portion thereof may not be reproduced
 * or used in any manner, without the prior permission 
 * from the owner.
 */
package sectionscheduler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import org.jdesktop.swingx.JXLabel;
import org.slf4j.LoggerFactory;

/**
 * Validates JTextField if it exceeds limit of text and checks
 * if number or letter.
 * @author Tenten Ponce
 */
public class JTextFieldNumberValidator extends PlainDocument {

    private int limit; //limit of character in the input
    private JTextField jt; //Textfield to validate
    private JXLabel tip_lbl; //to send tip and info to the user
    private static final Logger log = (Logger) LoggerFactory.getLogger(JTextFieldNumberValidator.class);

    JTextFieldNumberValidator(int limit, JTextField jt, JXLabel tip) {
        log.setLevel(Level.ALL); //all log levels
        this.limit = limit;
        this.jt = jt;
        this.tip_lbl = tip;
    }

    /*
    Validates input character, allows only number and prohibits the user
    to input character if he/she exceeds the limit.
    */
    @Override
    public void insertString(int offset, String str, AttributeSet attr) {
        int num = 0;
        setTip("");

        if ((getLength() + str.length()) <= limit) {
            try {
                super.insertString(offset, str, attr);
            } catch (BadLocationException ex) {
                log.error("Error happen when allowing text to be entered because limit still not reached.", ex);
            }
        }

        try {
            num = Integer.parseInt(str); //Number checker
        } catch (NumberFormatException e) { //if it was not a letter, catch.
            jt.setText(jt.getText().substring(0, jt.getText().length() - 1)); //remove the last character entered.
        }
    }

    public void setTip(String tip) {
        tip_lbl.setText(tip);
    }
}
