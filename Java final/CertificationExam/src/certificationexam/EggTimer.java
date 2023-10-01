/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package certificationexam;
import javafx.scene.control.Label;
import javafx.application.Platform;
/**
 *
 * @author mak22
 */
public class EggTimer implements Runnable {
    final int EXAM_TIME = 600;///6;
    
    int seconds;
    String displayTime;
    Thread my_thread;
    Label label;
    
    public EggTimer(Label la){
        this.seconds = EXAM_TIME;
        this.label = la;
        label.setText(getDisplayTime());
    }
    
    public void updateClock(){
        if(seconds > 0)
            this.seconds--;
       // getDisplayTime();
    }
    
    public void stopClock(){
        my_thread.stop();
        my_thread = null;
    }
    
    public void zeroClock(){
        //this.seconds = 0;
       // getDisplayTime();
    }
    
    public void resetClock(){
        this.seconds = EXAM_TIME;
       // getDisplayTime();
    }
    
    public String getDisplayTime(){
        return this.displayTime = String.format("      %02d:%02d", seconds/60, seconds%60);
    }
    
    public void start(){
    if(my_thread == null){
        my_thread = new Thread(this);
        my_thread.start();
    }
}
    
    
    @Override
    public void run(){
         while(my_thread != null && seconds > 0){
            try{
                //repaint();
                Thread.sleep(1000);
                updateClock();
                //label.setText(getDisplayTime());
                System.out.println("Running second thread: " + seconds + " seconds remaining.");
                
                Platform.runLater(new Runnable() {
                    public void run() {
                        if(seconds >0)
                            label.setText(getDisplayTime());
                        if(seconds == 0){
                            label.setText(" TIME IS UP!"); 
                            CertificationExam.INSTANCE.goToResults();
                        }
                    }
                });
            }
            catch(InterruptedException e){ 
            }
        }
    }
    

    
    
}
