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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

public class QRCodeScanner extends Activity {
	/** Called when the activity is first created. */

	static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
	private DatabaseReference mDatabase;
	private DatabaseReference boothRef;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Write a message to the database
		mDatabase = FirebaseDatabase.getInstance().getReference();
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference myRef = database.getReference("Booth");
		ArrayList<Part> partArrayList = new ArrayList<>();
		ArrayList<Module> moduleArrayList = new ArrayList<>();
		boothRef = mDatabase.child("booth");

		Random rand = new Random();
		for(int i=0;i<10;i++) {
			int num = rand.nextInt(100000) + 9999;

			Part part = new Part("part"+num);
			partArrayList.add(part);

		}

		for(int i=0;i<10;i++) {
			int num = rand.nextInt(100000) + 9999;

			Module module1 = new Module("module"+num);
			moduleArrayList.add(module1);

		}
		Module module1 = new Module();
		module1.setPartsList(partArrayList);

		Booth booth = new Booth();
		booth.setModuleList(moduleArrayList);



		boothRef.setValue(booth);

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
					scanQR(null);
				}

				Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
				toast.show();
			}
		}
	}
}