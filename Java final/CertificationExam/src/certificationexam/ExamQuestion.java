/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package certificationexam;
import javafx.scene.image.Image;

/**
 *
 * @author mak22
 */
public class ExamQuestion {
    int difficulty;
    String imgAddress;
    Image qImg;
    //String ansA, ansB, ansC, ansD, ansE;
    char correctAns;
    
    
    /*public ExamQuestion(){
        difficulty = -1;
        imgAddress = "";
        correctAns = '!';
        qImg = new Image(imgAddress);
    }*/
    
    public ExamQuestion(int dif, String imgAdr, char correct){
        difficulty = dif;
        imgAddress = imgAdr;
        correctAns = correct;
        qImg = new Image(imgAddress);
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getImgAddress() {
        return imgAddress;
    }

    public void setImgAddress(String imgAddress) {
        this.imgAddress = imgAddress;
    }
    
    public Image getImg() {
        return qImg;
    }

    public void setImg(Image image) {
        this.qImg = image;
    }

  /*  public String getAnsA() {
        return ansA;
    }

    public void setAnsA(String ansA) {
        this.ansA = ansA;
    }

    public String getAnsB() {
        return ansB;
    }

    public void setAnsB(String ansB) {
        this.ansB = ansB;
    }

    public String getAnsC() {
        return ansC;
    }

    public void setAnsC(String ansC) {
        this.ansC = ansC;
    }

    public String getAnsD() {
        return ansD;
    }

    public void setAnsD(String ansD) {
        this.ansD = ansD;
    }

    public String getAnsE() {
        return ansE;
    }

    public void setAnsE(String ansE) {
        this.ansE = ansE;
    }
*/
    public char getCorrectAns() {
        return correctAns;
    }

    public void setCorrectAns(char correctAns) {
        this.correctAns = correctAns;
    }
    
    
    
}
