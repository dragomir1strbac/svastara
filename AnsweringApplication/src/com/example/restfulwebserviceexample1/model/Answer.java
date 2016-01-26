package com.example.restfulwebserviceexample1.model;

import java.io.Serializable;

public class Answer  implements Serializable 
{
	private String id;
	private String question;
	private String answer;
	
	
	public Answer () {}
	
	public String getId()
	{
		return id;
	}

	public String getAnswer()
	{	 
		return answer;
	}
	
	
	public String getQuestion()
	{	 
		return question;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public void setAnswer(String answer)
	{
		this.answer = answer;
	}
	
	
	public void setQuestion(String question)
	{
		this.question = question;
	}
	
}