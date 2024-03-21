package com.example.project;

import java.io.*;

public class Question {
    public User owner;
    public String text;
    public String[] answers;
    public boolean[] isCorrect;
    public boolean type;
    public static int id = 1;
    public static int answerId=1;
    final public String status;
    final public String message;

    Question(String text, String[] answers, boolean[] isCorrect, boolean type) {
        this.owner = null;
        this.text = text;
        this.answers = answers;
        this.isCorrect = isCorrect;
        this.type = type;
        status="";
        message="";
    }

    Question(User owner, String text, String[] answers, boolean[] isCorrect, String type, boolean[] isCorrectInit) {
        this.owner = owner;
        this.text = text;
        this.answers = answers;
        this.isCorrect = isCorrect;
        this.type = type.equals("single");
        if (owner.getUsername().equals("") || owner.getPassword().equals("")) {
            status = "error";
            message = "You need to be authenticated";
            return;
        } else if (!User.login(owner)) {
            status = "error";
            message = "Login failed";
            return;
        }
        if (answers == null) {
            status = "error";
            message = "No answer provided";
            return;
        }
        if (answers.length > 5) {
            status = "error";
            message = "More than 5 answers were submitted";
            return;
        }
        if(answers.length == 1) {
            status = "error";
            message = "Only one answer provided";
            return;
        }
        if (text.equals("")) {
            status = "error";
            message = "No question text provided";
            return;
        }
        if (this.type) {
            int correctAnswers = 0;
            for (int i = 0; i < isCorrect.length; i++) {
                if (isCorrect[i]) {
                    correctAnswers++;
                }
            }
            if (correctAnswers > 1) {
                status = "error";
                message = "Single correct answer question has more than one correct answer";
                return;
            }
        }
        for (int i = 0; i < answers.length; i++) {
            if (answers[i] == null) {
                status = "error";
                message = "Answer " + (i+1) + " has no answer description";
                return;
            }
        }
        for (int i = 0; i < answers.length; i++) {
            if (!isCorrectInit[i]) {
                status = "error";
                message = "Answer " + (i + 1) + " has no answer correct flag";
                return;
            }
            for(int j=0;j<answers.length;j++) {
                if(i==j) {
                    continue;
                }
                if(answers[i].equals(answers[j])) {
                    status = "error";
                    message = "Same answer provided more than once";
                    return;
                }
            }
        }
        if (!Question.isUnique(text)) {
            status = "error";
            message = "Question already exists";
            return;
        }
        status = "ok";
        message = "Question added successfully";
    }

    public static boolean isUnique(String text) {
        try (BufferedReader br = new BufferedReader(new FileReader("questions.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String questionText = line.split(",")[1];
                if (questionText.equals(text)) {
                    return false;
                }
            }
        } catch (IOException e) {
        }
        return true;
    }

    public static void getId() {
        int id=0;
        try (BufferedReader br = new BufferedReader(new FileReader("questions.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String aux = line.split(",")[0];
                if(id < Integer.parseInt(aux)) {
                    id=Integer.parseInt(aux);
                }
            }
        } catch (IOException e) {
        }
        Question.id=id+1;
    }

    public static void saveQuestion(Question toAdd) {
        getId();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("questions.csv", true))) {
            String toWrite = Question.id + "," + toAdd.text + "," + toAdd.type + "," + toAdd.owner.getUsername() +
                    "," + toAdd.owner.getPassword();
            for (int i = 0; i < toAdd.answers.length; i++) {
                toWrite += "," + toAdd.answers[i] + "," + toAdd.isCorrect[i];
            }
            bw.write(toWrite + "\n");
            bw.close();
        } catch (IOException e) {
        }
    }

    public static int getQuestion(String text) {
        try (BufferedReader br = new BufferedReader(new FileReader("questions.csv"))) {
            String line;
            while((line=br.readLine())!=null) {
                String[] question = line.split(",");
                if(question[1].equals(text)) {
                    return Integer.parseInt(question[0]);
                }
            }
        }
         catch (IOException e) {
        }
        return -1;
    }
    public static String getQuestions(User owner) {
        String toReturn = "[";
        try(BufferedReader br = new BufferedReader(new FileReader("questions.csv"))) {
            String line;
            while((line=br.readLine())!=null) {
                String[] question = line.split(",");
                if(question[3].equals(owner.getUsername()) && question[4].equals(owner.getPassword())) {
                    toReturn+="{\"question_id\" : \"" + question[0] + "\", \"question_name\" : \"" + question[1] + "\"}, ";
                }
            }
        }
        catch (IOException e) {

        }
        toReturn=toReturn.substring(0,toReturn.length()-2);
        return toReturn + "]";
    }
    public static boolean isQuestion(int id) {
        try(BufferedReader br = new BufferedReader(new FileReader("questions.csv"))) {
            String line;
            while((line=br.readLine())!=null) {
                String aux = line.split(",")[0];
                if(Integer.parseInt(aux) == id) {
                    return true;
                }
            }
        }
        catch (IOException e) {

        }
        return false;
    }
    public static Question getQuestion(int id) {
        try(BufferedReader br = new BufferedReader(new FileReader("questions.csv"))) {
            String line;
            while((line=br.readLine())!=null) {
                String[] questionDetails = line.split(",");
                Question.id = Integer.parseInt(questionDetails[0]);
                if(Question.id == id) {
                    String text = questionDetails[1];
                    boolean type = Boolean.parseBoolean(questionDetails[2]);
                    int answersNo = (questionDetails.length - 5) / 2;
                    String[] answers = new String[answersNo];
                    boolean[] isCorrect = new boolean[answersNo];
                    for (int i = 5; i < questionDetails.length; i += 2) {
                        answers[answers.length - answersNo] = questionDetails[i];
                        isCorrect[isCorrect.length - answersNo] = Boolean.parseBoolean(questionDetails[i + 1]);
                        answersNo--;
                    }
                    return new Question(text, answers, isCorrect, type);
                }
            }
        }
        catch (IOException e) {

        }
        return null;
    }
    public int getCorrectAnswers() {
        int correctAnswers=0;
        for(int i=0;i<isCorrect.length;i++) {
            if(isCorrect[i]) {
                correctAnswers++;
            }
        }
        return correctAnswers;
    }
    public String toString() {
        String toRet="{\"question-name\":\""+ text + "\", \"question_index\":\"" + Question.id + "\", " +
                "\"question_type\":\"" + ((type) ? "single":"multiple") +
                "\", \"answers\":\"[";
        for(int i=0;i< answers.length;i++){
            toRet+="{\"answer_name\":\"" + answers[i] + "\", \"answer_id\":\"" + Question.answerId++ + "\"}, ";
        }
        return toRet.substring(0,toRet.length()-2) + "]\"}";
    }
}
