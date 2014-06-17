package cz.hnutiduha.bioadresar.duhaOnline.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.os.AsyncTask;

public class CoexConnector extends AsyncTask<Void, Void, String>
{
	public interface JSONReceiver {
		public void postFailed(Exception reason);
		public void readJSONResponse(JSONObject response);
	}
	
	public static final int METHOD_POST=1;
	public static final int METHOD_GET=2;

	private static String CONNECTOR_URL = "http://www.adresarfarmaru.cz/connector";
	JSONReceiver responseReceiver;
	List<NameValuePair> params;
	private int method;

	public CoexConnector(JSONReceiver responseReceiver, List<NameValuePair> params, int method) {
		this.responseReceiver = responseReceiver;
		this.params = params;
		params.add(new BasicNameValuePair("client", "android"));
		params.add(new BasicNameValuePair("lang", "cs"));
		this.method = method;
	}

	private static String readResponse(HttpResponse response)
			throws ClientProtocolException, IOException{
		
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

	public static String post(List<NameValuePair> args) throws IOException, ClientProtocolException {
		// Create a new HttpClient and Post Header
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(CONNECTOR_URL);

		httpPost.setEntity(new UrlEncodedFormEntity(args, HTTP.UTF_8));

		// Execute HTTP Post Request
		HttpResponse response = httpClient.execute(httpPost);
		return readResponse(response);
	}
	
	public static String get(List<NameValuePair> args)  throws IOException, ClientProtocolException, URISyntaxException {
        HttpGet request = new HttpGet();
        
        StringBuilder uri = new StringBuilder(CONNECTOR_URL);
        char separator = '?';
        for (NameValuePair arg: args)
        {
      	  uri.append(separator);
      	  uri.append(URLEncoder.encode(arg.getName(), HTTP.UTF_8));
      	  uri.append('=');
      	  uri.append(URLEncoder.encode(arg.getValue(), HTTP.UTF_8));
      	  
      	  separator = '&';
        }
        request.setURI(new URI(uri.toString()));
        
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);
        return readResponse(response);
	}
	
	protected Exception failure = null;

	@Override
	protected String doInBackground(Void ... unused) {
		try
		{
			switch (method)
			{
				case METHOD_POST:
					return post(params);
				case METHOD_GET:
					return get(params);
			}
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
