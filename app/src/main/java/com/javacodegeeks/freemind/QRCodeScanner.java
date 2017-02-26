package com.javacodegeeks.freemind;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class QRCodeScanner extends Activity {
	/** Called when the activity is first created. */

	static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
	static final String BASE_URL = "http://192.168.2.8:3000";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	void connectionRequest() {

	}

	/**
	 * Given a URL, sets up a connection and gets the HTTP response body from the server.
	 * If the network request is successful, it returns the response body in String form. Otherwise,
	 * it will throw an IOException.
	 */
	private String downloadUrl(URL url) throws IOException {
		InputStream stream = null;
		HttpsURLConnection connection = null;
		String result = null;
		try {
			connection = (HttpsURLConnection) url.openConnection();
			// Timeout for reading InputStream arbitrarily set to 3000ms.
			connection.setReadTimeout(3000);
			// Timeout for connection.connect() arbitrarily set to 3000ms.
			connection.setConnectTimeout(3000);
			// For this use case, set HTTP method to GET.
			connection.setRequestMethod("GET");
			// Already true by default but setting just in case; needs to be true since this request
			// is carrying an input (response) body.
			connection.setDoInput(true);
			// Open communications link (network traffic occurs here).
			connection.connect();
			//publishProgress(DownloadCallback.Progress.CONNECT_SUCCESS);
			int responseCode = connection.getResponseCode();
			if (responseCode != HttpsURLConnection.HTTP_OK) {
				throw new IOException("HTTP error code: " + responseCode);
			}
			// Retrieve the response body as an InputStream.
			stream = connection.getInputStream();
			//publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);
			if (stream != null) {
				// Converts Stream to String with max length of 500.
				result = readStream(stream);
			}
		} finally {
			// Close Stream and disconnect HTTPS connection.
			if (stream != null) {
				stream.close();
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
		return result;
	}

	private String readStream(InputStream is) {
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			int i = is.read();
			while(i != -1) {
				bo.write(i);
				i = is.read();
			}
			return bo.toString();
		} catch (IOException e) {
			return "";
		}
	}

	public void scanBar(View v) {
		try {
			Intent intent = new Intent(ACTION_SCAN);
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			startActivityForResult(intent, 0);
		} catch (ActivityNotFoundException anfe) {
			showDialog(QRCodeScanner.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
		}
	}

	public void scanQR(View v) {
		try {
			Intent intent = new Intent(ACTION_SCAN);
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			startActivityForResult(intent, 0);
		} catch (ActivityNotFoundException anfe) {
			showDialog(QRCodeScanner.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
		}
	}

	private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
		AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
		downloadDialog.setTitle(title);
		downloadDialog.setMessage(message);
		downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				try {
					act.startActivity(intent);
				} catch (ActivityNotFoundException anfe) {

				}
			}
		});
		downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
			}
		});
		return downloadDialog.show();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				if(!contents.equals("bobcob")){
					scanBar(null);
				}

				Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
				toast.show();

				startActivity(new Intent(this,ItemActivity.class));
			}
		}
	}

	void sendBoothQRCode() {
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(BASE_URL + "/registerQRCode");
			urlConnection = (HttpURLConnection) url.openConnection();

			urlConnection.setDoOutput(true);
			urlConnection.setChunkedStreamingMode(0);

			OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
			//writeStream(out);

			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			readStream(in);
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		finally
		{
			if(urlConnection != null)
				urlConnection.disconnect();
		}
	}
}