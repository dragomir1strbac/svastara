package com.example.restfulwebserviceexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
//import java.net.URLEncoder;

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

import android.util.Log;


public class MainActivity extends ActionBarActivity {


    public static String android_id ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		android_id = Secure.getString(this.getContentResolver(),Secure.ANDROID_ID);
		Log.d("Android","Android ID : "+android_id);
		
		
		Button sendreceiveData = (Button) findViewById(R.id.sendreceiveservicedata);
		sendreceiveData.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String restURL = "http://serviszayesno.herokuapp.com/ws/submitQuestion";
				new RestOperation().execute(restURL);
			}
		});
		
		
	}

	private class RestOperation extends AsyncTask<String, Void, Void> {

		String content;
		String error;
		ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
		String data = "";
		TextView serverDataReceived = (TextView) findViewById(R.id.serverDataReceived);
		TextView showParsedJSON = (TextView) findViewById(R.id.showParsedJSON);
		EditText userinput = (EditText) findViewById(R.id.userinput);
		
		Long tsLong = System.currentTimeMillis()/1000;
		String id = tsLong.toString();
		
		String sender = MainActivity.android_id;// Secure.getString(getContext().getContentResolver(),Secure.ANDROID_ID); //should not be called every time
		
		
		


		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progressDialog.setTitle("Please wait ...");
			progressDialog.show();
		
		try {
			JSONObject jQuestion = new JSONObject();
			
			jQuestion.put("id",id);
			jQuestion.put("sender",sender);
			jQuestion.put("receiver","123"); //hard coded
			jQuestion.put("question",userinput.getText());
			
			data = jQuestion.toString();
			} catch (JSONException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

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
				serverDataReceived.setText("Error " + error);
				
			} else {
				userinput.setText(""); 
			//	serverDataReceived.setText(content);  //not to see raw data
				
				
				String output = "";
				JSONArray jsonArray;
				
				try {
					
					jsonArray = new JSONArray(content);
					
					for (int i = jsonArray.length() - 1; i >= 0; i--) {
						JSONObject child = jsonArray.getJSONObject(i);

						String id = child.getString("id");
						String question = child.getString("question");
						String answer = child.getString("answer");
					//	String time = child.getString("date_added");
						output += System.getProperty("line.separator");
						output += "Q:" +question + "..." +answer + System.getProperty("line.separator");
											
					}
					
					showParsedJSON.setText(output);
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}


	}


}
