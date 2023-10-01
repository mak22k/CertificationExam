/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package certificationexam;

/**
 *
 * @author mak22
 */
public class ExamScore {
    //guessingPenalty = (numCorrect  - incorrect)/20;
    
    private double calculateScore(int numCorrect){
        double guessPenalty = (numCorrect)/20;
        double incorrectCoef = 1.0 - guessPenalty;
        double scoreReduction = (20 - (numCorrect)) * incorrectCoef;
        double baseScore = numCorrect * 5;
        double finalScore = baseScore - scoreReduction;
        return 0;
    }
    
    
     
    
    
    private double calcScore(int numCorrect){
        return (numCorrect*5.0) - (20.0-numCorrect)*(1.0 - (numCorrect/20.0));        
    }
    
    public String getTitle(double score){
        String title = score + ": ";
        if (score < 65)
           title += "Sorry, you did not pass- try again!";
        else if(score < 75 )//score >= 65 && score < 75)
            title += "Java Certified Programmer";
        else if(score < 85)//score >= 75 && score < 85)
            title += "Java Certified Developer";
        else //if(score >= 85)
            title += "Java Certified Architect";
        
        return title;
    }
    
}
