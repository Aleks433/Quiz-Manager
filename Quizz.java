package com.example.project;
import java.io.*;
import java.nio.Buffer;
import java.nio.BufferOverflowException;

public class Quizz {
    public static int id=1;
    public User owner;
    public int[] questions;
    public String name;
    final public String status;
    final public String message;
    Quizz(User owner,String name, int[] questions) {
        this.owner=owner;
        this.name=name;
        this.questions=questions;
        if(owner.getUsername().equals("") || owner.getPassword().equals("")) {
            status="error";
            message="You need to be authenticated";
            return;
        }
        if(!User.login(owner)) {
            status="error";
            message="Login failed";
            return;
        }
        if(questions.length>10) {
            status="error";
            message="Quizz has more than 10 questions";
            return;
        }
        for(int i=0;i<questions.length;i++) {
            if(!Question.isQuestion(questions[i])) {
                status = "error";
                message = "Question ID for question " + (i+1) + " does not exist";
                return;
            }
        }
        if(!isUnique(name)){
            status="error";
            message="Quizz name already exists";
            return;
        }
        status="ok";
        message="Quizz added succesfully";
    }
    public static boolean isUnique(String name) {
        try (BufferedReader br = new BufferedReader(new FileReader("quizzes.csv"))) {
            String line;
            while((line=br.readLine())!=null) {
                String aux = line.split(",")[1];
                if(aux.equals(name)){
                    return false;
                }
            }
        } catch (IOException e) {
        }
        return true;
    }
    public static void getId() {
       int id=0;
       try (BufferedReader br = new BufferedReader(new FileReader("quizzes.csv"))) {
           String line;
           while ((line = br.readLine()) != null) {
               String aux = line.split(",")[0];
               if(id < Integer.parseInt(aux)) {
                   id=Integer.parseInt(aux);
               }
           }
       } catch (IOException e) {
       }
       Quizz.id=id+1;
   }

    public static void saveQuizz(Quizz toAdd) {
        getId();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("quizzes.csv", true))) {
            String toWrite= Quizz.id + "," + toAdd.name + "," + toAdd.owner.getUsername() + "," + toAdd.owner.getPassword();
            for(int i=0;i< toAdd.questions.length;i++) {
                toWrite += "," + toAdd.questions[i];
            }
            bw.write(toWrite + "\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static int getQuizz(String name) {
        try(BufferedReader br = new BufferedReader(new FileReader("quizzes.csv"))) {
           String line;
           while((line=br.readLine())!=null) {
               String[] aux=line.split(",");
               if(name.equals(aux[1])) {
                   return Integer.parseInt(aux[0]);
               }
           }
        }
        catch (IOException e) {

        }
        return -1;
    }
    public static String getQuizzes(User owner){
        String toRet="[";
        try(BufferedReader br = new BufferedReader(new FileReader("quizzes.csv"))) {
            String line;
            while((line=br.readLine())!=null) {
                String[] aux=line.split(",");
                if(owner.getUsername().equals(aux[2]) && owner.getPassword().equals(aux[3])) {
                    toRet+="{\"quizz_id\" : \"" + aux[0] + "\", \"quizz_name\" : \"" + aux[1] + "\", \"is_completed\" : \"False\"}, ";
                }
            }
        }
        catch (IOException e) {

        }
        return toRet.substring(0,toRet.length()-2) + "]";
    }
    public static String getQuizzDetails(int id) {
        String toRet="[";
        try(BufferedReader br = new BufferedReader(new FileReader("quizzes.csv"))) {
            String line;
            while((line=br.readLine())!=null) {
                String[] quizz = line.split(",");
                if(Integer.parseInt(quizz[0])==id) {
                    Question.answerId = 1;
                    for(int i=4;i<quizz.length;i++) {
                        Question aux = Question.getQuestion(Integer.parseInt(quizz[i]));
                        toRet += aux+ ", ";
                    }
                }
            }
        }
        catch (IOException e) {

        }
        return toRet.substring(0,toRet.length()-2) + "]";
    }
    public static String submit(String[] args) {
        int id;
        User owner = Tema1.getUser(args);
        if(owner.getUsername().equals("") || owner.getPassword().equals("")) {
            return "You need to be authenticated";
        }
        if(!User.login(owner)) {
            return "Login failed";
        }
        if(args.length <= 3){
            return "No quizz identifier was provided";
        }
        id=Integer.parseInt(args[3].substring(args[3].indexOf("\'")+1,args[3].length()-1));
        try{
            BufferedReader br = new BufferedReader(new FileReader("quizzes.csv"));
            String line;
            while((line=br.readLine())!=null) {
                String[] quizz=line.split(",");
                String quizzName = quizz[1];
                if(Integer.parseInt(quizz[0])==id) {
                    double score=0;
                    int idToQuestion=0;
                     for(int i=4;i<quizz.length;i++){
                        int questionId = Integer.parseInt(quizz[i]);
                        Question crtQuestion = Question.getQuestion(questionId);
                        int correctAnswers=crtQuestion.getCorrectAnswers();
                        int wrongAnswers=crtQuestion.answers.length - correctAnswers;
                        int correctSelected=0, wrongSelected=0;
                        int currentAnswer=4;
                        int answerId;
                        do {
                            answerId=Integer.parseInt(args[currentAnswer].substring(args[currentAnswer].indexOf("\'")+1,args[currentAnswer].length()-1));
                            if(answerId-idToQuestion <= crtQuestion.answers.length && answerId-idToQuestion > 0) {
                                if(crtQuestion.isCorrect[answerId-idToQuestion-1]) {
                                    correctSelected++;
                                }
                                else {
                                    wrongSelected++;
                                }
                            }
                            currentAnswer++;
                        }while(answerId-idToQuestion<crtQuestion.answers.length && currentAnswer<args.length);
                        idToQuestion+=crtQuestion.answers.length;
                        score+= ((((double)1/correctAnswers) * correctSelected) - (((double)1/wrongAnswers) * wrongSelected));
                     }
                     score=score/(quizz.length-4)*100;
                     if(score > Math.floor(score) + 0.5) {
                         score = Math.ceil(score);
                     }
                     else {
                         score = Math.floor(score);
                     }
                     if(score <0) {
                         score =0;
                     }
                     BufferedWriter bw = new BufferedWriter(new FileWriter("temp.csv"));
                     boolean found = false;
                     try {
                          br = new BufferedReader(new FileReader("solutions.csv"));
                          while ((line = br.readLine()) != null) {
                              String[] solution = line.split(",");
                              if (solution[0].equals(owner.getUsername()) && solution[0].equals(owner.getPassword())) {
                                  line += "," + id + "," + quizzName + "," + ((int)score);
                                  found = true;
                              }
                              bw.write(line);
                          }
                     }
                     catch (IOException e) {

                     }
                     bw.close();
                     if(found) {
                         bw=new BufferedWriter(new FileWriter("solutions.csv"));
                         br = new BufferedReader(new FileReader("temp.csv"));
                         while ((line = br.readLine()) != null) {
                             bw.write(line);
                         }
                     }
                     else {
                         bw=new BufferedWriter(new FileWriter("solutions.csv",true));
                         bw.write(owner.getUsername() + "," + owner.getPassword() +
                                 "," + id + "," + quizzName + "," + ((int)score));
                     }
                      bw.close();
                      return ((int)score) + " points";
                }
            }
        }
        catch (IOException e) {
        }
        return "No quiz was found";
    }
    public static String deleteQuizz(String[] args) {
        User owner = Tema1.getUser(args);
        int id;
        boolean found = false;
        if(owner.getUsername().equals("") || owner.getPassword().equals("")) {
            return "You need to be authenticated";
        }
        if(!User.login(owner)) {
            return "Login failed";
        }
        if(args.length <= 3){
            return "No quizz identifier was provided";
        }
        id=Integer.parseInt(args[3].substring(args[3].indexOf("\'")+1,args[3].length()-1));
        try{
            BufferedReader br = new BufferedReader(new FileReader("quizzes.csv"));
            BufferedWriter bw = new BufferedWriter(new FileWriter("temp.csv"));
            String line;
            while((line=br.readLine())!=null) {
                int quizzId = Integer.parseInt(line.split(",")[0]);
                if(quizzId==id) {
                    found=true;
                    continue;
                }
                bw.write(line);
            }
            bw.close();
            br = new BufferedReader(new FileReader("temp.csv"));
            bw = new BufferedWriter(new FileWriter("quizzes.csv"));
            while((line=br.readLine())!=null) {
                bw.write(line);
            }
            bw.close();
        }
        catch(IOException e) {

        }
        if(found) {
            return "Quizz deleted successfully";
        }
        return "No quiz was found";
    }
}
