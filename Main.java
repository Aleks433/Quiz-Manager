package com.example.project;
import java.io.*;
import java.util.Arrays;

public class Main {

	public static void clean() {
		new File("./users.csv").delete();
		new File("./questions.csv").delete();
		new File("./quizzes.csv").delete();
		new File("./solutions.csv").delete();
		new File("./temp.csv").delete();
	}
	public static User getUser(String[] args) {
		String user="";
		String password="";
		for(int i=1;i<args.length;i++) {

			if(!user.equals("") && !password.equals(""))
				break;

			if(args[i].contains("-u")) {
				int start=args[i].indexOf('\'');
				user=args[i].substring(start + 1, args[i].length()-1);
			} else if (args[i].contains("-p")) {
				int start=args[i].indexOf('\'');
				password=args[i].substring(start + 1, args[i].length()-1);
			}
		}
		return new User(user, password);
	}
	public static Question getQuestion(String[] args) {
		User owner = getUser(args);
		String text="";
		String[] answers=null;
		boolean[] isCorrect=null;
		boolean[] isCorrectInit=null;
		String type="";
		for(int i=1;i<args.length;i++) {

			if(args[i].contains("-text")) {
				int start=args[i].indexOf("\'");
				text=args[i].substring(start+1, args[i].length()-1);
			}
			else if(args[i].contains("-type")) {
				int start=args[i].indexOf("\'");
				String aux=args[i].substring(start+1, args[i].length()-1);
				type=aux;
			}
			else if(args[i].contains("-answer")) {
				int answerIndex = Integer.parseInt(args[i].substring(8, 9));
				if(args[i].contains("is-correct")) {
					//answer value
					int value = Integer.parseInt(args[i].substring(args[i].length()-2,args[i].length()-1));
					if(isCorrect==null) {
						isCorrect = new boolean[answerIndex];
						isCorrectInit = new boolean[answerIndex];
					}
					else if(answerIndex >= isCorrect.length) {
						isCorrect = Arrays.copyOf(isCorrect, answerIndex);
						isCorrectInit = Arrays.copyOf(isCorrectInit, answerIndex);
					}
					isCorrect[answerIndex-1]=value==1;
					isCorrectInit[answerIndex-1]=true;
				}
				else {
					//answer text
					int start = args[i].indexOf("\'");
					String aux = args[i].substring(start + 1, args[i].length() - 1);
					if(answers==null) {
						answers=new String[answerIndex];
					}
					else if(answerIndex >= answers.length) {
						answers = Arrays.copyOf(answers, answerIndex);
					}
					answers[answerIndex-1] = aux;
				}
			}
		}
		return new Question(owner,text,answers,isCorrect,type, isCorrectInit);
	}
	public static String getQuestionId(String[] args) {
		User owner = getUser(args);
		if(owner.getUsername().equals("") || owner.getPassword().equals("")) {
			return "You need to be authenticated";
		}
		boolean isLoggedIn=User.login(owner);
		if(!isLoggedIn) {
			return "Login failed";
		}
		String questionText = args[3].substring(args[3].indexOf("\'")+1, args[3].length()-1);
		int id=Question.getQuestion(questionText);
		if(id>0) {
			return "" + id;
		}
		else {
			return "Question does not exist";
		}
	}
	public static Quizz getQuizz(String[] args)	 {
		User owner = getUser(args);
		String name="";
		int[] questions=null;
		for(int i=1;i<args.length;i++) {
			if(args[i].contains("-name")) {
				int start = args[i].indexOf("\'");
				String aux = args[i].substring(start + 1, args[i].length()-1);
				name =aux;
			}
			if(args[i].contains("question")) {
				int questionNumber = Integer.parseInt(args[i].substring(10,11));
				int start = args[i].indexOf("'");
				int id=Integer.parseInt(args[i].substring(start+1, args[i].length()-1));
				if(questions==null)	{
					questions=new int[questionNumber];
				}
				else if(questionNumber > questions.length) {
					questions=Arrays.copyOf(questions, questionNumber);
				}
				questions[questionNumber-1] = id;
			}
		}
		return new Quizz(owner, name, questions);
	}

	public static String getAllQuestions(String[] args) {
		User owner = getUser(args);
		if(owner.getUsername().equals("") || owner.getPassword().equals("")) {
			return "You need to be authenticated";
		}
		boolean isLoggedIn=User.login(owner);
		if(!isLoggedIn) {
			return "Login failed";
		}
		return Question.getQuestions(owner);
	}
	public static String getQuizzId(String[] args) {
		User owner = getUser(args);
		if(owner.getUsername().equals("") || owner.getPassword().equals("")) {
			return "You need to be authenticated";
		}
		boolean isLoggedIn=User.login(owner);
		if(!isLoggedIn) {
			return "Login failed";
		}
		String quizzName = args[3].substring(args[3].indexOf("\'")+1, args[3].length()-1);
		int id=Quizz.getQuizz(quizzName);
		if(id>0) {
			return "" + id;
		}
		else {
			return "Quizz does not exist";
		}
	}
	public static String getAllQuizzes(String[] args) {
		User owner = getUser(args);
		if(owner.getUsername().equals("") || owner.getPassword().equals("")) {
			return "You need to be authenticated";
		}
		boolean isLoggedIn=User.login(owner);
		if(!isLoggedIn) {
			return "Login failed";
		}
		return Quizz.getQuizzes(owner);
	}

	public static String getQuizzById(String[] args) {
		User owner = getUser(args);
		if(owner.getUsername().equals("") || owner.getPassword().equals("")) {
			return "You need to be authenticated";
		}
		if(!User.login(owner)) {
			return "Login failed";
		}
		int id = Integer.parseInt(args[3].substring(5,args[3].length()-1));
		return Quizz.getQuizzDetails(id);
	}
	public static void main(final String[] args) {
		if(args == null) {
			System.out.print("Hello world!");
			return;
		}
		String status="", message="";
		if(args.length == 0 )
		{
			return;
		}
		switch(args[0]) {
			case "-create-user": {
				User currentUser=null;
				currentUser=getUser(args);
				message = currentUser.message;
				status = currentUser.status;
				if(status.equals("ok")) {
					User.saveUser(currentUser);
				}
				break;
			}
			case "-create-question" : {
				Question currentQuestion = null;
				currentQuestion = getQuestion(args);
				message = currentQuestion.message;
				status = currentQuestion.status;
				if(status.equals("ok")) {
					Question.saveQuestion(currentQuestion);
				}
				break;
			}
			case "-get-question-id-by-text": {
				String id=getQuestionId(args);
				status = "ok";
				message = id;
				try {
					Integer.parseInt(id);
				} catch(Exception e){
					status = "error";
				}
				break;
			}
			case "-get-all-questions" : {
				String aux = getAllQuestions(args);
				status = "ok";
				message=aux;
				if(aux.equals("You need to be authenticated") || aux.equals("Login failed")) {
					status = "error";
				}
				break;
			}
			case "-create-quizz" : {
				Quizz currentQuizz = null;
				currentQuizz = getQuizz(args);
				status= currentQuizz.status;
				message= currentQuizz.message;
				if(status.equals("ok")) {
					Quizz.saveQuizz(currentQuizz);
				}
				break;
			}
			case "-get-quizz-by-name" : {
				String result = getQuizzId(args);
				message=result;
				status="ok";
				try {
					Integer.parseInt(result);
				}
				catch (Exception e) {
					status="error";
				}
				break;
			}
			case "-get-all-quizzes" : {
				String result = getAllQuizzes(args);
				status = "ok";
				message = result;
				if(result.equals("You need to be authenticated") || result.equals("Login failed")) {
					status = "error";
				}
				break;
			}
			case "-get-quizz-details-by-id" : {
				String result = getQuizzById(args);
				status = "ok";
				message = result;
				if(result.equals("You need to be authenticated") || result.equals("Login failed")) {
					status = "error";
				}
				break;
			}
			case "-submit-quizz" : {
				String result=Quizz.submit(args);
				message=result;
				if(result.contains("points")) {
					status="ok";
				}
				else {
					status="error";
				}
				break;
			}
			case "-delete-quizz-by-id" : {
				String result = Quizz.deleteQuizz(args);
				message = result;
				status = "error";
				if(result.equals("Quizz deleted successfully")) {
					status = "ok";
				}
				break;
			}
			case "-get-my-solutions" : {
				String result = User.getSolutions(args);
				status="ok";
				message=result;
				if(result.equals("You need to be authenticated") || result.equals("Login failed")) {
					status = "error";
				}
				break;
			}
			case "-cleanup-all" : {
				clean();
				status= "ok";
				message = "Cleanup finished successfully";
				break;
			}
		}
		System.out.print("{'status':'" + status + "','message':'" + message + "'}");
	}
}
