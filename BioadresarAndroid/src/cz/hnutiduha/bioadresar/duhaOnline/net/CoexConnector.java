package cz.hnutiduha.bioadresar.duhaOnline.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

public class CoexConnector extends AsyncTask<Void, Void, String>
{
	public interface JSONReceiver {
		public void postFailed(Exception reason);
		public void readJSONResponse(JSONObject response);
	}

	private static String CONNECTOR_URL = "http://www.adresarfarmaru.cz/connector";
	JSONReceiver responseReceiver;
	List<NameValuePair> params;

	public CoexConnector(JSONReceiver responseReceiver, List<NameValuePair> params) {
		this.responseReceiver = responseReceiver;
		this.params = params;
		params.add(new BasicNameValuePair("client", "android"));
		params.add(new BasicNameValuePair("lang", "cs"));
	}

	private static String readResponse(HttpResponse response)
			throws ClientProtocolException, IOException, JSONException {
		
		StringBuilder builder = new StringBuilder();

		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode == 200) {
			HttpEntity entity = response.getEntity();
			InputStream content = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					content));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} else {
			throw new ClientProtocolException("Error status code: "
					+ statusCode);
		}
		
		return builder.toString();
	}

	public static String post(List<NameValuePair> args) throws IOException, ClientProtocolException, JSONException{
		// Create a new HttpClient and Post Header
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(CONNECTOR_URL);

		httpPost.setEntity(new UrlEncodedFormEntity(args, HTTP.UTF_8));

		// Execute HTTP Post Request
		HttpResponse response = httpClient.execute(httpPost);
		return readResponse(response);
	}
	
	protected Exception failure = null;

	@Override
	protected String doInBackground(Void ... unused) {
		try
		{
			return post(params);
		}
		catch (Exception ex)
		{
			this.failure = ex;
		}
		
		return null;
	}
	
	protected void onPostExecute(JSONObject response)
	{
		if (response != null)
		{
			responseReceiver.readJSONResponse(response);
		}
		else if (failure != null)
		{
			responseReceiver.postFailed(failure);
		}
		else
		{
			responseReceiver.postFailed(new Exception("Unknown error"));
		}
	}

}
