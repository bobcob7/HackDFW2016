package com.javacodegeeks.freemind;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Stack;

import static com.javacodegeeks.freemind.QRCodeScanner.ACTION_SCAN;
import static com.javacodegeeks.freemind.R.id.imageView;

/**
 * Created by anura on 2/25/2017.
 */

public class ItemActivity extends Activity {

    private ImageView scanImageView;
    private HashMap<Integer,String> qrMap;
    private HashMap<Integer,String> palletMap;
    private TextView partNumber;
    private TextView palletNumber;
    private TextView itemsLeft;
    private static Stack<Integer> numStack;
    private static int selectedNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        partNumber = (TextView) findViewById(R.id.pickUpItem);
        palletNumber = (TextView) findViewById(R.id.locationText);
        itemsLeft = (TextView) findViewById(R.id.itemsLeftText);
        setupValues();

        if(!numStack.isEmpty()) {
            selectedNumber = numStack.pop();
            partNumber.setText("Part #"+selectedNumber);
            palletNumber.setText(palletMap.get(selectedNumber));
            itemsLeft.setText(String.valueOf(numStack.size()));
        }
        else {
            partNumber.setText("Finished");
            palletNumber.setText("No items");
            itemsLeft.setText("0");
        }


        scanImageView = (ImageView) findViewById(imageView);
        scanImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanQRCode();
            }
        });


    }

    void setupValues() {

        numStack = new Stack<>();
        numStack.push(1);
        numStack.push(3);
        numStack.push(5);
        numStack.push(4);
        numStack.push(2);

        qrMap = new HashMap<>();
        qrMap.put(1,"#01,square.obj,0,0,0,0,0,0");
        qrMap.put(2,"#02,square.obj,0,0,14.5,0,0,0");
        qrMap.put(3,"#03,square.obj,13,0,0,0,0,0");
        qrMap.put(4,"#04,square.obj,0,-13,0,0,0,0");
        qrMap.put(5,"#05,square.obj,0,0,28,0,0,0");

        palletMap = new HashMap<>();
        palletMap.put(1,"Pallet #3");
        palletMap.put(2,"Pallet #3");
        palletMap.put(3,"Pallet #6");
        palletMap.put(4,"Pallet #6");
        palletMap.put(5,"Pallet #9");

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
                    if (!contents.equals(qrMap.get(selectedNumber))) { //#02,square.obj,0,0,0,0,0,0"
                        scanQRCode();
                    }
                else {
                        Toast toast = Toast.makeText(this, "Item Found, Pick it up", Toast.LENGTH_LONG);
                        toast.show();

                        if(!numStack.isEmpty()) {

                            selectedNumber = numStack.pop();
                            partNumber.setText("Part #" + selectedNumber);
                            palletNumber.setText(palletMap.get(selectedNumber));
                            itemsLeft.setText(String.valueOf(numStack.size()));
                        } else {

                            this.finish();
                        }
                    }
            }
        }
    }
}
