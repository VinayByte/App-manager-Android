package com.egnize.appmanager.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.egnize.appmanager.R;
import com.egnize.chineseapps.utils.Response;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Utils {

    public static List<Response> readJson(Context context) {
        ArrayList<Response> fields = new ArrayList<Response>();

        try {
            //Load File
            BufferedReader jsonReader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.apps)));
            StringBuilder jsonBuilder = new StringBuilder();
            for (String line = null; (line = jsonReader.readLine()) != null; ) {
                jsonBuilder.append(line).append("\n");
            }

            //Parse Json
            JSONTokener tokener = new JSONTokener(jsonBuilder.toString());
            JSONArray jsonArray = new JSONArray(tokener);

            for (int index = 0; index < jsonArray.length(); index++) {
                //Set both values into the listview
                JSONObject jsonObject = jsonArray.getJSONObject(index);
                fields.add(new Response(jsonObject.getString("name"), jsonObject.getString("pkg")));
            }


            Map<String, Object> docData = new HashMap<>();
            docData.put("list", fields);

            FirebaseFirestore.getInstance().collection("data").document("app_list")
                    .set(docData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("@", "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("@", "Error writing document", e);
                        }
                    });


        } catch (FileNotFoundException e) {
            Log.e("jsonFile", "file not found");
        } catch (IOException e) {
            Log.e("jsonFile", "ioerror");
        } catch (JSONException e) {
            Log.e("jsonFile", "error while parsing json");
        }
        return fields;
    }
}

