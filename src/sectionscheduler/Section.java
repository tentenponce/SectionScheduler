/*
 * Copyright © 2015 by Tenten Ponce - All Rights Reserved.
 * This project or any portion thereof may not be reproduced
 * or used in any manner, without the prior permission 
 * from the owner.
 */
package sectionscheduler;

/**
 * Object Class for Section.
 * 
 * @author Tenten Ponce
 * @version 1.0
 * @since October 2015
 */

public class Section{
    
    private String course;
    private int year;
    private int sem;
    private String letter;
    private int RegularEnrolled;
    private int IrregularEnrolled;
    private String code;
    private int RegLimit;
    private int IrregLimit;
    private static final String[] ALPHABET = {"A", "B", "C", "D", "E", "F", "G",
                            "H", "I", "J", "K", "L", "M", "N", "O",
                            "P", "Q", "R", "S", "T", "U", "V", "W",
                            "X", "Y", "Z"}; //for searching or incrementing letter.
    
    public Section(){}
    
    public Section(String course, int year, String letter){
        this.course = course;
        this.year = year;
        this.letter = letter;
        this.code = course + "-" + year + letter;
    }
            
    public Section(String course, int year, String letter, int sem, int regcount, int irregcount, 
                    int reglimit, int irreglimit){
        this.course = course;
        this.year = year;
        this.letter = letter;
        this.sem = sem;
        this.RegularEnrolled = regcount;
        this.IrregularEnrolled = irregcount; 
        this.code = course + "-" + year + letter;
        this.RegLimit = reglimit;
        this.IrregLimit = irreglimit;
    }
   
    /**
     * Identifies what letter is next on the current letter.
     * Loops through a array of alphabets and increments the index
     * to get the next letter.
     * @return next letter
     */
    public String getNextLetter(){
        int counter = 0;
        while(counter < ALPHABET.length){
            if(ALPHABET[counter].equals(letter)){ //checker if the letter has been found.
                if(letter.equals("Z")){ //will not return anything if limit reached (Z).
                    return "limit";
                }else{
                    return ALPHABET[counter + 1]; //increment index for the next letter.
                }
            }
            counter++;
        }
        
        return "A"; //return A if no section.
    }
    
    public String getCourse(){
        return course;
    }
    
    public int getYear(){
        return year;
    }
    
    public String getLetter(){
        return letter;
    }
    
    public int getSemester(){
        return sem;
    }
    
    public int getRegularEnrolled(){
        return RegularEnrolled;
    }
    
    public int getIrregularEnrolled(){
        return IrregularEnrolled;
    }
    
    public String getSectionCode(){
        return code;
    }
    
    public int getRegLimit(){
        return RegLimit;
    }
    
    public int getIrregLimit(){
        return IrregLimit;
    }
}
