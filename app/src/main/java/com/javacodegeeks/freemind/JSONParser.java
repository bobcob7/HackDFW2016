package com.javacodegeeks.freemind;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by anura on 2/25/2017.
 */

public class JSONParser {

    void parseAssignedBoothDetails() {

    }

    static String  createJSONForBooth(String boothCode, String userId) {
        JSONObject mainObject = new JSONObject();
        try {
            mainObject.put("boothCode", boothCode);
            mainObject.put("userId", userId);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
        }

        return mainObject.toString();
    }

    static String createFoundAck() {
        return null;
    }
}
