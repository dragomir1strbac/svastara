package com.example.restfulwebserviceexample1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;

//import org.apache.http.client.HttpClient;
//import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.provider.Settings.Secure;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import android.util.Log;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;


import com.example.restfulwebserviceexample1.model.Answer;


public class MainActivity extends ActionBarActivity {

    private LinearLayout mLayout;
    private ArrayList<Answer> mAnswerList;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mLayout = (LinearLayout) findViewById(R.id.ll1);
		
		mAnswerList = new ArrayList<Answer>();
		
		Button sendreceiveData = (Button) findViewById(R.id.sendreceiveservicedata);
		
		sendreceiveData.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String restURL = "http://serviszayesno.herokuapp.com/ws/submitAnswer";
				new RestOperation().execute(restURL);
			}
		});
		
		
	}

	private class RestOperation extends AsyncTask<String, Void, Void> {

		String content;
		String error;
		ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
		String data = "";
		TextView errorView = (TextView) findViewById(R.id.error);
		
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			 //print array state 
			try 
			{
				JSONObject jsonAnswerMsg = new JSONObject();
				jsonAnswerMsg.put("sender","@1234");
				JSONArray jsonArray = new JSONArray();
				for (int i = 0; i<mAnswerList.size(); i++)		
				{
					String answer = mAnswerList.get(i).getAnswer();
					String id = mAnswerList.get(i).getId();
					
					if (!answer.equals("PENDING"))
					{
						JSONObject jsonAnswer = new JSONObject();
						jsonAnswer.put("id",id);
						jsonAnswer.put("answer",answer);
						jsonArray.put(jsonAnswer);			
					}	 	 
				}
				jsonAnswerMsg.put("answerList",jsonArray);
				data = jsonAnswerMsg.toString();
				Log.d("TAG",data);
			} catch (JSONException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
			}
			errorView.setText("");
			mLayout.removeAllViews();
			mAnswerList.clear();
			
			progressDialog.setTitle("Please wait ...");
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			BufferedReader br = null;

			URL url;
			try {
				url = new URL(params[0]);

				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				
				connection.setRequestMethod("POST");
				connection.setRequestProperty("USER-AGENT","Mozilla/5.0");
				connection.setRequestProperty("Content-Type","application/json");
				connection.setDoOutput(true);
				connection.setChunkedStreamingMode(0); //not to exhaust memory heap
				
				OutputStreamWriter outputStreamWr = new OutputStreamWriter(connection.getOutputStream());
				outputStreamWr.write(data);
				outputStreamWr.flush();
				
				br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line = null;
				
				while((line = br.readLine())!=null) {
					sb.append(line);
					sb.append(System.getProperty("line.separator"));
				}
				
				content = sb.toString();

			} catch (MalformedURLException e) {
				error = e.getMessage();
				e.printStackTrace();
			} catch (IOException e) {
				error = e.getMessage();
				e.printStackTrace();
			} finally {
				try {
					if(br != null)
					
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}


		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			progressDialog.dismiss();
			
			if(error!=null) {
				errorView.setText("Error: "+error);
			} else { 
			
				JSONArray jsonArray;
				
				try {
					
					jsonArray = new JSONArray(content);
					
					for (int i = jsonArray.length() - 1; i >= 0; i--) {
						JSONObject child = jsonArray.getJSONObject(i);

						String id = child.getString("id");
						String question = child.getString("question");
						String answer = child.getString("answer");
						
						Answer ansObj = new Answer();
						ansObj.setId(id);
						ansObj.setAnswer(answer);
						ansObj.setQuestion(question);
						
						createNewTextView(ansObj);
												
					}
					
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}


	}
	
	
	private TextView createNewTextView(Answer ansObj) {
		
		String question = System.getProperty("line.separator") + "Q:" + ansObj.getQuestion() + "..." + ansObj.getAnswer();
		final LayoutParams lparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		final TextView textView = new TextView(this);
		textView.setLayoutParams(lparams);		
		textView.setText(question);
		textView.setTag(ansObj);
		mAnswerList.add(ansObj);
		mLayout.addView(textView);
		textView.setOnClickListener(new HandleClick());
		return textView;
	}
	
	
 
    private class HandleClick implements OnClickListener{
        public void onClick(View arg0) 
		{
			TextView textView = (TextView)arg0;	//cast view to TextView
			Answer ansObj = (Answer)textView.getTag();
			String answer = ansObj.getAnswer();
			String question = ansObj.getQuestion();
			String text = System.getProperty("line.separator") + "Q:" + ansObj.getQuestion() + "...PENDING";
			if (answer.equals("PENDING")) 
			{
				textView.setBackgroundColor(Color.GREEN);
				textView.setText(text + "->YES");
				ansObj.setAnswer("YES");
			}
			else if (answer.equals("YES"))
			{
				textView.setBackgroundColor(Color.RED);
				textView.setText(text + "->NO");
				ansObj.setAnswer("NO");
			}
			else if (answer.equals("NO"))
			{
				textView.setBackgroundColor(Color.GRAY);
				textView.setText(text + "->IGNORE");
				ansObj.setAnswer("IGNORE");
			}
			else if (answer.equals("IGNORE"))
			{
				textView.setBackgroundColor(Color.WHITE);
				textView.setText(text);
				ansObj.setAnswer("PENDING");
			}
		}
    }
	
	
	

}
