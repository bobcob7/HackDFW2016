package com.javacodegeeks.freemind;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import static com.javacodegeeks.freemind.QRCodeScanner.ACTION_SCAN;

/**
 * Created by anura on 2/25/2017.
 */

public class ItemActivity extends Activity {

    private Button scanButton;
    private HashMap<Integer,String> qrMap;
    private TextView partNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        partNumber = (TextView) findViewById(R.id.pickUpItem);
        partNumber.setText("Part #1");

        qrMap = new HashMap<>();
        qrMap.put(1,"");
        qrMap.put(2,"");
        qrMap.put(3,"");
        qrMap.put(4,"");
        qrMap.put(5,"");

        scanButton = (Button) findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanQRCode();
            }
        });


    }

    private void scanQRCode() {
        Intent intent = new Intent(ACTION_SCAN);
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                    if (!contents.equals("#01,square.obj,0,0,0,0,0,0")) { //#02,square.obj,0,0,0,0,0,0"
                        scanQRCode();
                    }
                else {
                        Toast toast = Toast.makeText(this, "Item Found, Pick it up", Toast.LENGTH_LONG);
                        toast.show();
                    }


//                Intent itemIntent = new Intent(this, ItemActivity.class);
//                startActivity(itemIntent);
            }
        }
    }
}
