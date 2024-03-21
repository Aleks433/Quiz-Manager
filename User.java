package com.example.project;

import java.io.*;

public class User {
    private String username;
    private String password;
    final public String status;
    final public String message;
    User(String user, String password) {
        this.username = user;
        this.password = password;
        if(user.equals("")) {
            status = "error";
            message = "Please provide username";
        } else if (password.equals("")) {
            status = "error";
            message = "Please provide password";
        } else if (!User.isUnique(user)) {
           status = "error" ;
           message = "User already exists";
        }
        else {
            status = "ok";
            message = "User created successfully";
        }
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public static boolean isUnique(String username) {
        try(BufferedReader br = new BufferedReader(new FileReader("users.csv"))){
            String line;
            while((line = br.readLine()) != null) {
                String user = line.split(",")[0];
                if(user.equals(username) ) {
                    return false;
                }
            }
        }
        catch (IOException e){
        }
        return true;
    }
    public static boolean login(User toLogin) {
        try(BufferedReader br = new BufferedReader(new FileReader("users.csv"))) {
            String line;
            while((line = br.readLine()) != null) {
                String user = line.split(",")[0];
                String password = line.split(",")[1];
                if(user.equals(toLogin.getUsername()) && password.equals(toLogin.getPassword())) {
                    return true;
                }
            }
        }
        catch (IOException e) {
        }
        return false;
    }
    public static void saveUser(User toAdd) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("users.csv",true));
            bw.write(toAdd.getUsername() + "," + toAdd.getPassword() + "\n");
            bw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String getSolutions(String[] args) {
        String toRet = "[";
        User owner = Tema1.getUser(args);
        if(owner.getUsername().equals("") || owner.getPassword().equals("")) {
            return "You need to be authenticated";
        }
        if(!User.login(owner)) {
            return "Login failed";
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader("solutions.csv"));
            String line;
            while((line=br.readLine())!=null) {
                String[] solution = line.split(",");
                if(solution[0].equals(owner.getUsername()) && solution[1].equals(owner.getPassword()))  {
                    int indexInList=1;
                    for(int i=2;i<solution.length;i+=3) {
                        toRet+="{\"quiz-id\" : \"" + solution[i] + "\", \"quiz-name\" : \"" + solution[i+1] + "\", \"score\" : \"" +
                                solution[i+2] + "\", \"index_in_list\" : \"" + indexInList + "\"}, ";
                        indexInList++;
                    }
                }
            }
        }
        catch (IOException e) {

        }
        return toRet.substring(0, toRet.length()-2) + "]";
    }
}
