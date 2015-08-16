package com.telldus.live.mobile.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The configuration screen for the {@link ConfigurableSensorWidget ConfigurableSensorWidget} AppWidget.
 */
public class ConfigurableSensorWidgetConfigureActivity extends Activity {
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    // private EditText etUrl;
    private Button btAdd;
    private View btSelectSensor, btSelectDisplayItem;
    private TextView sensorName, sensorHint, sensorDataName, sensorDataHint;
    private ImageView imgSensorType;
    private AppWidgetManager widgetManager;
    private RemoteViews views;

//    CharSequence sensorList[] = new CharSequence[] {"Outdoor Temp", "Indoor Temp", "Fridge", "Freezer"};
    CharSequence sensorDataList[] = new CharSequence[] {"Temperature", "Humidity", "Wind", "Rain"};
    CharSequence[] sensorNameList = null;
    List<String> nameListItems = new ArrayList<String>();
    CharSequence[] sensorIdList = null;
    List<String> idListItems = new ArrayList<String>();
    CharSequence[] sensorModelList = null;
    List<String> modelListItems = new ArrayList<String>();

    private String accessToken;
    private String expiresIn;
    private String tokenType;
    private String scope;
    private String refreshToken;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        //Get the text file
        File fileAuth = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/RNFS-BackedUp/auth.txt");
        if (fileAuth.exists()) {
            Log.d("File exists?", "Yes");

            //Read text from file
            StringBuilder text = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(fileAuth));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                JSONObject authInfo = new JSONObject(String.valueOf(text));
                accessToken = String.valueOf(authInfo.getString("access_token"));
                expiresIn = String.valueOf(authInfo.getString("expires_in"));
                tokenType = String.valueOf(authInfo.getString("token_type"));
                scope = String.valueOf(authInfo.getString("scope"));
                refreshToken = String.valueOf(authInfo.getString("refresh_token"));

                Log.d("Auth token", accessToken);
                Log.d("Expires in", expiresIn);
                Log.d("Token type", tokenType);
                Log.d("Scope", scope);
                Log.d("Refresh token", refreshToken);

                getSensorList();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        setResult(RESULT_CANCELED);
        // activity stuffs
        setContentView(R.layout.activity_sensor_widget_configure);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/telldusicons.ttf");
        // etUrl = (EditText) findViewById(R.id.etUrl);
        widgetManager = AppWidgetManager.getInstance(this);
        views = new RemoteViews(this.getPackageName(), R.layout.configurable_sensor_widget);
        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
        sensorName = (TextView) findViewById(R.id.txtSensorName);
        sensorHint = (TextView) findViewById(R.id.txtSensorHint);
        sensorDataName = (TextView) findViewById(R.id.txtSensorDataName);
        sensorDataHint = (TextView) findViewById(R.id.txtSensorDataHint);
        imgSensorType = (ImageView) findViewById(R.id.imgSensorType);
        btAdd = (Button) findViewById(R.id.btAdd);
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Gets user input
                // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(etUrl.getText().toString()));
                // PendingIntent pending = PendingIntent.getActivity(ConfigurableSensorWidgetConfigureActivity.this, 0, intent, 0);
//                views.setOnClickPendingIntent(R.id.iconWidget, pending);
                views.setTextViewText(R.id.txtSensorType, sensorName.getText());
                views.setImageViewResource(R.id.iconSensor, R.drawable.sensor);
                widgetManager.updateAppWidget(mAppWidgetId, views);
                Intent resultValue = new Intent();
                // Set the results as expected from a 'configure activity'.
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
        btSelectSensor = (View) findViewById(R.id.btSelectSensor);
        btSelectSensor.setOnClickListener(new View.OnClickListener() {
            public int checkedItem;

            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ConfigurableSensorWidgetConfigureActivity.this);
                builder.setTitle(R.string.pick_sensor)
                        .setSingleChoiceItems(sensorNameList, checkedItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sensorName.setText(sensorNameList[which]);
                                sensorHint.setText(null);
                                getSensorInfo(which);
                                dialog.dismiss();
                            }
                        });
                builder.show();
            }
        });
        btSelectDisplayItem = (View) findViewById(R.id.btSelectDisplayItem);
        btSelectDisplayItem.setOnClickListener(new View.OnClickListener() {
            public int checkedItem;

            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ConfigurableSensorWidgetConfigureActivity.this);
                builder.setTitle(R.string.pick_sensor_data)
                        .setSingleChoiceItems(sensorDataList, checkedItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                sensorDataName.setText(sensorDataList[i]);
                                sensorDataHint.setText(null);
                            }
                        });
                builder.show();
            }
        });
    }

    void getSensorList() {
        AndroidNetworking.post("https://api.telldus.com/oauth2/sensors/list")
            .addHeaders("Content-Type", "application/json")
            .addHeaders("Accept", "application/json")
            .addHeaders("Authorization", "Bearer " + accessToken)
            .setPriority(Priority.LOW)
            .build()
            .getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject sensorData = new JSONObject(response.toString());
                        JSONArray sensorList = sensorData.getJSONArray("sensor");
                        for (int i = 0; i < sensorList.length(); i++) {
                            JSONObject curObj = sensorList.getJSONObject(i);
                            String name = curObj.getString("name");
                            String id = curObj.getString("id");
                            String model = curObj.getString("model");

                            nameListItems.add(name);
                            idListItems.add(id);
                            modelListItems.add(model);
                        }
                        sensorNameList = nameListItems.toArray(new CharSequence[nameListItems.size()]);
                        sensorIdList = idListItems.toArray(new CharSequence[idListItems.size()]);
                        sensorModelList = modelListItems.toArray(new CharSequence[modelListItems.size()]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    };
                }

                @Override
                public void onError(ANError anError) {
                    Log.e("Error: ", "API pull data error");
                }
            });
    }

    void getSensorInfo(int sensorIndex) {
        JSONObject body = new JSONObject();
        String sensorId = String.valueOf(sensorIdList[sensorIndex]);
        Log.d("#################", sensorId);
        try {
            body.put("id", sensorId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AndroidNetworking.post("https://api.telldus.com/oauth2/sensor/info")
            .addHeaders("Content-Type", "application/json")
            .addHeaders("Accept", "application/json")
            .addHeaders("Authorization", "Bearer " + accessToken)
            .addJSONObjectBody(body)
            .setPriority(Priority.LOW)
            .build()
            .getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("%%%%%%%%%%%%%%%%%%%%%%%%%%%", String.valueOf(response));
                }

                @Override
                public void onError(ANError anError) {
                    Log.e("Error: ", "API pull data error");
                }
            });
    }
}

