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
public class UserData {
    private String name, id;
    private double bestScore;
    private int timesTaken;
    
    public UserData(){
        name = "";
        id = "";
        bestScore = 0;
        timesTaken = 0;
    }
    
    public UserData(String n, String i){
        name = n;
        id = i;
        bestScore = 0;
        timesTaken = 0;
    }
    
    public void saveBestScore(double newScore) {
        this.bestScore = ( bestScore > newScore ? bestScore : newScore);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getBestScore() {
        return bestScore;
    }

    public void setBestScore(double bestScore) {
        this.bestScore = bestScore;
    }

    public int getTimesTaken() {
        return timesTaken;
    }

    public void setTimesTaken(int timesTaken) {
        this.timesTaken = timesTaken;
    }
    
  
}
