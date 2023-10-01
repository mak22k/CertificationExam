/*
 * CIT 285 Fall 2019 Final Exam
 * Create a test that mimics the Java certification exams
 * Use multithreading to display a countdown clock
 * 
 * By Marisha Kulseng
 * Last modified: 12/14/2019
 */
package certificationexam;
// DATABASE NAME: jcert

import static java.lang.Integer.parseInt;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import java.util.ArrayList;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Modality;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Collections;
import static javafx.scene.input.KeyCode.ENTER;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
/**
 *
 * @author mak22
 */
public class CertificationExam extends Application {
    public static CertificationExam INSTANCE;
    
    // mysql constants
    static final String DRIVER = "com.mysql.jdbc.Driver";
    static final String DATABASE_URL = "jdbc:mysql://localhost/jcert";
    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;
    PreparedStatement retrieveFromDB = null;
    PreparedStatement insertDB = null;
    PreparedStatement deleteFromDB = null;
    String retrieveStr = "SELECT * FROM test_results WHERE id = ?;";
    String insertStr = "INSERT INTO test_results VALUES (?, ?, ?, ?);";
    String deleteStr = "DELETE FROM test_results WHERE id = ? ;";

    
    int seconds, currentQNum, numCorrect, numIncorrect;
    String displayTime;
    Font timeFont = new Font("Tahoma", 60);
    Font instructionFont = new Font("Tahoma", 25);
    Font normalFont = new Font("Tahoma", 15);
    Font biggerFont = new Font("Tahoma", 20);
    Thread my_thread;
    UserData currentUser = new UserData();
    Label showTime = new Label("10:00");
    EggTimer clock = new EggTimer(showTime);
    HBox timeBox = new HBox();
    BorderPane root = new BorderPane();
    VBox resultsPage = new VBox(30);
    
    Label finishName = new Label(" Name: " );
    //Label finishScore = new Label(" Score: ");
    Label finishResult = new Label(" Results: ");
    
    RadioButton rbA = new RadioButton("Choice A");
    RadioButton rbB = new RadioButton("Choice B");
    RadioButton rbC = new RadioButton("Choice C");
    RadioButton rbD = new RadioButton("Choice D");
    RadioButton rbE = new RadioButton("Choice E");
    
    ArrayList<UserData> resultList = new ArrayList<>();
    ArrayList<ExamQuestion> lvl1 = new ArrayList<>(); // easiest Q's
    ArrayList<ExamQuestion> lvl2 = new ArrayList<>();
    ArrayList<ExamQuestion> lvl3 = new ArrayList<>();
    ArrayList<ExamQuestion> lvl4 = new ArrayList<>(); // hardest Q's
    
    ExamQuestion currentQuestion;// = new ExamQuestion();
    
    @Override
    public void start(Stage primaryStage) {
       // BorderPane root = new BorderPane();
       // HBox timeBox = new HBox();
        StackPane loginPage = new StackPane();
        VBox testPage = new VBox(15);
        //VBox resultsPage = new VBox(30);
        //Label showTime = new Label("10:00");
          showTime.setFont(timeFont);
          showTime.setTranslateX(450);
        //EggTimer clock = new EggTimer(showTime);
        //showTime.setText(clock.getDisplayTime());
        timeBox.getChildren().add(showTime);

        
        
         // User login page  //
        Label intro = new Label("Please log in. You will have 10 minutes to answer 20 questions. \n\t\t     Press ENTER to begin. Good luck!");
        intro.setFont(instructionFont);
        Label userName = new Label(" Name: ");
        Label idNum = new Label("    ID: ");
        userName.setFont(normalFont);   idNum.setFont(normalFont);

        TextField uNameInputField = new TextField();
        TextField idNumInputField = new TextField();
        Button btBegin = new Button("Begin!");
        
        loginPage.getChildren().addAll(intro, userName, idNum, uNameInputField, idNumInputField, btBegin);
        intro.setTranslateY(-200);
        userName.setTranslateY(-110);   userName.setTranslateX(-90);
        idNum.setTranslateY(-80);       idNum.setTranslateX(-90);
        uNameInputField.setTranslateY(-110);  uNameInputField.setTranslateX(50);    
        uNameInputField.setMaxWidth(200);
        idNumInputField.setTranslateY(-80);   idNumInputField.setTranslateX(50);     
        idNumInputField.setMaxWidth(200);
        ///////////////////login
        
        // Login popup - error // 
        VBox errorPane = new VBox(15);
        
        Stage errorPopup = new Stage();
         errorPopup.setTitle("Attention!");
         errorPopup.initOwner(primaryStage);
         errorPopup.initModality(Modality.WINDOW_MODAL);
         Scene errorScene = new Scene(errorPane, 400, 200); 
        Label errorLabel = new Label("You ran out of retake attempts!");
           errorLabel.setFont(biggerFont);
        Button btOK = new Button("OK");
           btOK.setOnAction(e-> {errorPopup.close();});   
        errorPane.getChildren().addAll(errorLabel, btOK);
        errorLabel.setTranslateY(20);  errorLabel.setTranslateX(50);
        btOK.setTranslateY(30);        btOK.setTranslateX(180);
        /////////////// login popup
        


        
        // Questions page //
        loadQuestionsLV1(); loadQuestionsLV2(); loadQuestionsLV3(); loadQuestionsLV4();
        // randomize question order
        Collections.shuffle(lvl1);      Collections.shuffle(lvl2);  
        Collections.shuffle(lvl3);      Collections.shuffle(lvl4); 

        // randomize first question's source
        currentQNum = 0; // currentQNum starts at 0, to reflect starting from 0th element
        int randNum = ((int)(Math.random() * 100) % 4) + 1; // 0+1, 1+1, 2+1, 3+1 for 1,2,3,4 difficulty
        getNextQuestion(currentQNum, randNum);
        //currentQuestion = lvl1.get(0); // start us off with the first question
        Label laQuestionNum = new Label("1. ");

        ImageView imgDisp = new ImageView(currentQuestion.getImg());

        Button btNext = new Button("Next question>>");
        Button btFinish = new Button("Finish and Submit");
        btFinish.setFont(instructionFont);
        btFinish.setTranslateY(-27);  btFinish.setTranslateX(400);
       
        rbA.setFont(normalFont); rbB.setFont(normalFont); rbC.setFont(normalFont);
        rbD.setFont(normalFont); rbE.setFont(normalFont);

        ToggleGroup questions = new ToggleGroup();
        rbA.setToggleGroup(questions);
        rbB.setToggleGroup(questions);
        rbC.setToggleGroup(questions);
        rbD.setToggleGroup(questions);
        rbE.setToggleGroup(questions);
        
        testPage.getChildren().addAll(imgDisp, rbA, rbB, rbC, rbD, rbE, 
                btNext, btFinish, laQuestionNum);
        testPage.setTranslateX(450);    testPage.setTranslateY(30);
        laQuestionNum.setTranslateX(-50); laQuestionNum.setTranslateY(-430);
        
        
        
        btNext.setOnAction(e->{
            int newDiff, currentDiff = currentQuestion.getDifficulty();
            // check responses and record result
            char correctAns = currentQuestion.getCorrectAns();//lvl1.get(currentQNum).getCorrectAns();
            if(gaveCorrectAns(correctAns)){
                numCorrect++; // user got answer correct
                // next ensure we are taking a (more difficult) question
                if(currentDiff < 4)
                    newDiff = currentDiff + 1;
                else //if(currentDiff ==4)
                    newDiff = currentDiff;
            }
            else {
                numIncorrect++;
                // next ensure we are taking a (less difficult) question
                if(currentDiff > 1)
                    newDiff = currentDiff - 1;
                else //(currentDiff == 1)
                    newDiff = currentDiff;
            }
            
           
            // clear responses and load next question
            //questions.getSelectedToggle().setSelected(false);
            //questions.selectToggle(null);
            rbA.setSelected(false);     rbB.setSelected(false);     rbC.setSelected(false);
            rbD.setSelected(false);     rbE.setSelected(false);
            currentQNum++; // on the next question
            getNextQuestion(currentQNum, newDiff);
            /*switch(newDiff){
                case 1: currentQuestion = lvl1.get(currentQNum); break;
                case 2: currentQuestion = lvl2.get(currentQNum); break;
                case 3: currentQuestion = lvl3.get(currentQNum); break;
                default: currentQuestion = lvl4.get(currentQNum); break;
            }*/
                            
            imgDisp.setImage(currentQuestion.getImg());
            laQuestionNum.setText((currentQNum + 1) + ". ");
            if(currentQNum == 19)
                btNext.setVisible(false);
            else 
                btNext.setVisible(true);
            if(currentQNum == 20){ // when we finish the 20th question, we end the test
                btFinish.fire();
            }
            
        });
        
        btFinish.setOnAction(e->{
            goToResults();
        });

        
        /////////////////// questions
        
        // Results Page //

        Button btReturn = new Button("Return to log in page");
        resultsPage.getChildren().addAll( finishResult, btReturn);
        resultsPage.setTranslateX(450);     resultsPage.setTranslateY(50);     
        btReturn.setOnAction(e->{
                clock.resetClock();
                timeBox.setVisible(true);
                showTime.setText(clock.getDisplayTime());
                //loginPage.setVisible(false);
                root.setCenter(loginPage);
                
                //set up for next test-taker
                rbA.setSelected(false);     rbB.setSelected(false);     rbC.setSelected(false);
                rbD.setSelected(false);     rbE.setSelected(false);
                currentUser = new UserData();
                currentQNum=0;
                btNext.setVisible(true);
                laQuestionNum.setText((currentQNum + 1) + ". ");                
                Collections.shuffle(lvl1);      Collections.shuffle(lvl2);  
                Collections.shuffle(lvl3);      Collections.shuffle(lvl4);
                int randNum1 = ((int)(Math.random() * 100) % 4) + 1; // 0+1, 1+1, 2+1, 3+1 for 1,2,3,4 difficulty
                getNextQuestion(currentQNum, randNum1);
                imgDisp.setImage(currentQuestion.getImg());
        });
        /////////////////// results
        
        
        loginPage.setOnKeyPressed((e)-> {
            if(e.getCode().equals(ENTER))
                btBegin.fire();}  );
        
        btBegin.setVisible(false);
        btBegin.setOnAction(e->{
            String testStr = idNumInputField.getText();
            boolean isDigits;
            try{
                int digit = parseInt(testStr);
                isDigits = true;
            }
            catch(Exception ex){
                isDigits = false;
            }
            
            // IF these fields are NULL, popup error
            if(uNameInputField.getText().isEmpty() || idNumInputField.getText().isEmpty()){
                errorLabel.setText("You cannot leave any field blank! \n\tEnter your name and ID.");
                errorPopup.setScene(errorScene); 
                errorPopup.show();
            }
            else if(testStr.length() != 4 || !isDigits){//idNumInputField.getText().length() != 4){ // IF THE ID FIELD HAS ID != 4 DIGITS, error
                errorLabel.setText("An ID must be " + (testStr.length() != 4 ? "FOUR " : "four ") 
                        + (isDigits? "digits" : "DIGITS") +".");
                errorPopup.setScene(errorScene); 
                errorPopup.show();
            }
            else{
            numCorrect = 0;  numIncorrect = 0; currentQNum=0;
            
            boolean canTakeExam;
            retrievePriorResults(idNumInputField.getText());
            System.out.println(currentUser.getTimesTaken());
            currentUser.setName(uNameInputField.getText());
            currentUser.setId(idNumInputField.getText());
            canTakeExam = (currentUser.getTimesTaken() < 2 ? true : false);
                
            /*int indexFound = -1;   // STORING USER WITH ARRAY LIST -- updated to sql
            for(UserData x : resultList){
                if(x.getId().equals(currentUser.getId()) ){
                    foundUser = true;
                    indexFound = resultList.indexOf(x);
                    if(x.getTimesTaken() < 2)
                        canTakeExam = true;
                    else
                        canTakeExam = false;
                    
                    System.out.println("Found user: " + foundUser + "\nCanTakeExam: " + canTakeExam);
                }
            }
            if(foundUser && canTakeExam){ // name, id, best score, times taken
                currentUser.setBestScore(resultList.get(indexFound).getBestScore());
                currentUser.setTimesTaken(resultList.get(indexFound).getTimesTaken());
                
                resultList.remove(indexFound); //remove the previous entry so we can re-add later
            }*/
                
            if(canTakeExam){
                clock.start();
                //resultList.add( new UserData(uNameInputField.getText(), 
                //        idNumInputField.getText()));
                //currentUser.setName(uNameInputField.getText());
                //currentUser.setId(idNumInputField.getText());
                //timeBox.setVisible(true);
                //loginPage.setVisible(false);
                root.setCenter(testPage);
            }
            else{
                errorLabel.setText("You ran out of retake attempts!");
                errorPopup.setScene(errorScene); 
                errorPopup.show();
            }
            }
        });
        
        

        //root.setCenter(btn);
        root.setCenter(loginPage);
        root.setTop(timeBox);
        
        Scene scene = new Scene(root, 1280, 720);
        
        primaryStage.setTitle("Java Certification Exam");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    public void goToResults(){
         Platform.runLater(new Runnable() {
             public void run() {
        
        // get final answer result         
        char correctAns = currentQuestion.getCorrectAns();//lvl1.get(currentQNum).getCorrectAns();
        if(gaveCorrectAns(correctAns))
            numCorrect++; // user got answer correct
        else 
            numIncorrect++;
        
        // stop the clock and get to results page
        clock.stopClock();
        timeBox.setVisible(true);
        //loginPage.setVisible(false);
        root.setCenter(resultsPage);
        
        currentUser.setTimesTaken(currentUser.getTimesTaken()+1);        
        finishName.setText(" Name: " + currentUser.getName());
        currentUser.saveBestScore(calcScore(numCorrect));
        finishResult.setText("Score: " + getTitle(calcScore(numCorrect))
                + "\n\nBest score: " + currentUser.getBestScore());
        updateDB(currentUser);
        System.out.println(currentUser.getBestScore());
             }
         });
    }
    
    public void init(){
        INSTANCE = this;
    }
    
    public boolean gaveCorrectAns(char correctAns){
            return ( (correctAns == 'a' && rbA.isSelected()) || 
                     (correctAns == 'b' && rbB.isSelected()) ||
                     (correctAns == 'c' && rbC.isSelected()) ||
                     (correctAns == 'd' && rbD.isSelected()) ||
                     (correctAns == 'e' && rbE.isSelected())  );
        }
    
    public double calcScore(int numCorrect){
        double score = (numCorrect*5.0) - ((20.0-numCorrect)*(1.0 - (numCorrect/20.0)));
        score *= 10;
        score = (int)score;
        score /=10.0;
        return (score >= 0? score : 0);
    }
    
    public String getTitle(double score){
        String title = score + "\n";
        if (score < 65)
            title += "Sorry, you did not pass-- try again!";
        else if(score < 75 )//score >= 65 && score < 75)
            title += "Java Certified Programmer";
        else if(score < 85)//score >= 75 && score < 85)
            title += "Java Certified Developer";
        else //if(score >= 85)
            title += "Java Certified Architect";
        
        return title;
    }
    
  
    private void retrievePriorResults (String userID) {  //look at result set stuff from class handouts
       String output = "";
       try{
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(DATABASE_URL,
                    "marisha", "password");
            
            //connection.setAutoCommit(false);
            retrieveFromDB = connection.prepareStatement(retrieveStr);
            retrieveFromDB.setString(1, userID);
            
            resultSet = retrieveFromDB.executeQuery();
          /* resultSet = statement.executeQuery(String.format("SELECT %s FROM %s WHERE %s='%s';", 
                   field, tableName, userInputType, userInput));*/
           if(resultSet.next()){
               //get the data...
              currentUser.setId(resultSet.getString("id"));
              currentUser.setName(resultSet.getString("name"));
              currentUser.setTimesTaken(resultSet.getInt("attempts"));
              currentUser.setBestScore(resultSet.getDouble("high_score"));
              // ... and delete the existing entry so it can be re-made if number of attempts <2
              if(currentUser.getTimesTaken() < 2){
                  deleteFromDB = connection.prepareStatement(deleteStr);
                  deleteFromDB.setString(1, userID);
                  deleteFromDB.executeUpdate();
              }
           }
           else{
               currentUser = new UserData();
           }
        }
        catch(SQLException sqlException){
            sqlException.printStackTrace();
            output = "ERROR: Data retrieval not successful";
        }
        catch(ClassNotFoundException classNotFound){
            classNotFound.printStackTrace();
            output = "ERROR: Data retrieval not successful";
        }
        finally{
        
            try{
                resultSet.close();
                retrieveFromDB.close();
                connection.close();
            }
            catch(Exception exception){
                exception.printStackTrace();
            }
        }
    }
    
    private void updateDB(UserData user) { 
        try{
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(DATABASE_URL,
                    "marisha", "password");
            
            // String insertStr = "INSERT INTO test_results VALUES ('?', '?', ?, ?)";
            //connection.setAutoCommit(false);
            insertDB = connection.prepareStatement(insertStr);
            insertDB.setString(1, user.getId());
            insertDB.setString(2, user.getName());
            insertDB.setInt(3, user.getTimesTaken());
            insertDB.setDouble(4, user.getBestScore());
            insertDB.executeUpdate();
            //statement = connection.createStatement();
            
            //statement.executeUpdate(queryString);
            
        }
        catch(SQLException sqlException){
            sqlException.printStackTrace();
        }
        catch(ClassNotFoundException classNotFound){
            classNotFound.printStackTrace();
        }
        finally{
        
            try{
                insertDB.close();
                connection.close();
            }
            catch(Exception exception){
                exception.printStackTrace();
            }
        }
    }  
    
    
    public void getNextQuestion(int currentQNum, int difficulty){
            switch(difficulty){ 
                case 1: currentQuestion = lvl1.get(currentQNum); break;
                case 2: currentQuestion = lvl2.get(currentQNum); break;
                case 3: currentQuestion = lvl3.get(currentQNum); break;
                default: currentQuestion = lvl4.get(currentQNum); break;
            }
        }
    
    public void loadQuestionsLV1(){
        int difficulty = 1;
        String baseAddress = "/certificationexam/lv1/1.";
        
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "1.png", 'c'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "2.png", 'd'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "3.png", 'c'));        
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "4.png", 'd'));        
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "5.png", 'a'));
               
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "6.png", 'e'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "7.png", 'b'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "8.png", 'b'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "9.png", 'b'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "10.png", 'd'));
        
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "11.png", 'd'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "12.png", 'c'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "13.png", 'b'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "14.png", 'a'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "15.png", 'b'));
        
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "16.png", 'b'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "17.png", 'a'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "18.png", 'c'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "19.png", 'c'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "20.png", 'b'));
        
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "21.png", 'd'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "22.png", 'd'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "23.png", 'b'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "24.png", 'd'));
        lvl1.add(new ExamQuestion(difficulty, baseAddress + "25.png", 'd'));
        
        
    }
    
    public void loadQuestionsLV2(){
        int difficulty = 2; 
        String baseAddress = "/certificationexam/lv2/2.";
        
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "1.png", 'd'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "2.png", 'e'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "3.png", 'b'));        
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "4.png", 'b'));        
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "5.png", 'a'));
               
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "6.png", 'c'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "7.png", 'a'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "8.png", 'b'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "9.png", 'e'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "10.png", 'b'));
        
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "11.png", 'a'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "12.png", 'c'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "13.png", 'b'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "14.png", 'd'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "15.png", 'd'));
        
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "16.png", 'a'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "17.png", 'c'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "18.png", 'c'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "19.png", 'c'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "20.png", 'd'));
        
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "21.png", 'b'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "22.png", 'd'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "23.png", 'b'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "24.png", 'd'));
        lvl2.add(new ExamQuestion(difficulty, baseAddress + "25.png", 'e'));
        
    }
     public void loadQuestionsLV3(){
        int difficulty = 3;
        String baseAddress = "/certificationexam/lv3/3.";
        
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "1.png", 'd'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "2.png", 'b'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "3.png", 'd'));        
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "4.png", 'b'));        
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "5.png", 'c'));
              
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "6.png", 'e'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "7.png", 'e'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "8.png", 'c'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "9.png", 'e'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "10.png", 'e'));
       
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "11.png", 'e'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "12.png", 'b'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "13.png", 'e'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "14.png", 'd'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "15.png", 'a'));
        
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "16.png", 'a'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "17.png", 'd'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "18.png", 'd'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "19.png", 'd'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "20.png", 'e'));
        
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "21.png", 'd'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "22.png", 'd'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "23.png", 'd'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "24.png", 'a'));
        lvl3.add(new ExamQuestion(difficulty, baseAddress + "25.png", 'e'));
        
    }
     
     public void loadQuestionsLV4(){
        int difficulty = 4;
        String baseAddress = "/certificationexam/lv4/4.";
       
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "1.png", 'a'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "2.png", 'b'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "3.png", 'a'));        
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "4.png", 'd'));        
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "5.png", 'd'));
              
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "6.png", 'e'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "7.png", 'b'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "8.png", 'b'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "9.png", 'a'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "10.png", 'b'));
       
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "11.png", 'c'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "12.png", 'd'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "13.png", 'b'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "14.png", 'a'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "15.png", 'a'));
        
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "16.png", 'c'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "17.png", 'b'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "18.png", 'd'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "19.png", 'c'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "20.png", 'd'));
        
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "21.png", 'e'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "22.png", 'a'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "23.png", 'b'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "24.png", 'c'));
        lvl4.add(new ExamQuestion(difficulty, baseAddress + "25.png", 'e'));
        
    }
     
    
    
    
}
