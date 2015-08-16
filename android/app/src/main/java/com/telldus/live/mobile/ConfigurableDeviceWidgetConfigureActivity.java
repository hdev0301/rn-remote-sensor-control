package com.telldus.live.mobile.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.telldus.live.mobile.interfaces.TelldusAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.BasicResponseHandler;
//import org.apache.http.impl.client.DefaultHttpClient;
//import java.io.BufferedInputStream;
//import java.io.BufferedWriter;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import okhttp3.Interceptor;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;

/**
 * The configuration screen for the {@link ConfigurableDeviceWidget ConfigurableDeviceWidget} AppWidget.
 */
public class ConfigurableDeviceWidgetConfigureActivity extends Activity {
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
//    private EditText etUrl;
    private Button btAdd;
    private View btSelectDevice;
    TextView deviceName, deviceHint, deviceOn, deviceOff;
    ImageView deviceState;
    private AppWidgetManager widgetManager;
    private RemoteViews views;

//    CharSequence deviceNameList[] = new CharSequence[]{"Light Dimmer", "Livingroom Lights", "Kitchen", "Outdoor Lights"};
    CharSequence[] deviceNameList = null;
    List<String> nameListItems = new ArrayList<String>();
    CharSequence[] deviceStateList = null;
    List<String> stateListItems = new ArrayList<String>();

    private String accessToken;
    private String expiresIn;
    private String tokenType;
    private String scope;
    private String refreshToken;
    private TelldusAPI telldusAPI;

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

                createDeviceApi();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setResult(RESULT_CANCELED);
        // activity stuffs
        setContentView(R.layout.activity_device_widget_configure);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/telldusicons.ttf");
//        etUrl = (EditText) findViewById(R.id.etUrl);
        widgetManager = AppWidgetManager.getInstance(this);
        views = new RemoteViews(this.getPackageName(), R.layout.configurable_device_widget);
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
        deviceName = (TextView) findViewById(R.id.txtDeviceName);
        deviceHint = (TextView) findViewById(R.id.txtDeviceHint);
        final String[] deviceStateVal = {"0"};
        btAdd = (Button) findViewById(R.id.btAdd);
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Gets user input
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(etUrl.getText().toString()));
//                PendingIntent pending = PendingIntent.getActivity(ConfigurableDeviceWidgetConfigureActivity.this, 0, intent, 0);
//                views.setOnClickPendingIntent(R.id.iconWidget, pending);
                views.setTextViewText(R.id.txtWidgetTitle, deviceName.getText());
                if (deviceStateVal[0] == "1") {
                    views.setImageViewResource(R.id.iconOn, R.drawable.on_light);
                    views.setImageViewResource(R.id.iconOff, R.drawable.off_dark);
                } else {
                    views.setImageViewResource(R.id.iconOn, R.drawable.on_dark);
                    views.setImageViewResource(R.id.iconOff, R.drawable.off_light);
                }
                widgetManager.updateAppWidget(mAppWidgetId, views);
                Intent resultValue = new Intent();
                // Set the results as expected from a 'configure activity'.
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
        btSelectDevice = (View) findViewById(R.id.btSelectDevice);
        btSelectDevice.setOnClickListener(new View.OnClickListener() {
            public int checkedItem;

            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ConfigurableDeviceWidgetConfigureActivity.this);
                builder.setTitle(R.string.pick_device)
                        .setSingleChoiceItems(deviceNameList, checkedItem, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deviceName.setText(deviceNameList[which]);
                                deviceHint.setText(null);
                                deviceStateVal[0] = (String) deviceStateList[which];
                            }
                        });
                builder.show();
            }
        });
    }

//    private static String getStringFromInputStream(InputStream is) {
//
//        BufferedReader br = null;
//        StringBuilder sb = new StringBuilder();
//
//        String line;
//        try {
//
//            br = new BufferedReader(new InputStreamReader(is));
//            while ((line = br.readLine()) != null) {
//                sb.append(line);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        return sb.toString();
//
//    }

    void createDeviceApi() {

//        String BASE_URL = "https://api.telldus.com/";
//
//        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
//            @Override
//            public okhttp3.Response intercept(Chain chain) throws IOException {
//                Request originalRequest = chain.request();
//                Request.Builder builder = originalRequest.newBuilder().addHeader("Content-Type", "application/json");
//                builder.addHeader("Accept", "application/json");
//                builder.addHeader("Authorization", "Bearer " + accessToken);
//
//                Request newRequest = builder.build();
//                return chain.proceed(newRequest);
//            }
//        }).build();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .client(okHttpClient)
//                .baseUrl(BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        telldusAPI = retrofit.create(TelldusAPI.class);
//        Call<DevicelistData> devicelistDataCall = telldusAPI.getDevicelists();
//        devicelistDataCall.enqueue(new Callback<DevicelistData>() {
//            @Override
//            public void onResponse(Call<DevicelistData> call, Response<DevicelistData> response) {
//                int statusCode = response.code();
//                DevicelistData devicelistData = response.body();
//                Log.d("Devicelists:", "onResponse: " + statusCode);
//                Log.d("Devicelists:", "onResponse: " + devicelistData);
//            }
//
//            @Override
//            public void onFailure(Call<DevicelistData> call, Throwable t) {
//                Log.d("Devicelists:", "onFailure: ");
//            }
//        });


        AndroidNetworking.post("https://api.telldus.com/oauth2/devices/list")
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Accpet", "application/json")
                .addHeaders("Authorization", "Bearer " + accessToken)
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject deviceData = new JSONObject(response.toString());
                            JSONArray deviceList = deviceData.getJSONArray("device");
                            for (int i = 0; i < deviceList.length(); i++) {
                                JSONObject curObj = deviceList.getJSONObject(i);
                                String name = curObj.getString("name");
                                Integer state = curObj.getInt("state");
                                nameListItems.add(name);
                                stateListItems.add(state.toString());
                            }
                            deviceNameList = nameListItems.toArray(new CharSequence[nameListItems.size()]);
                            deviceStateList = stateListItems.toArray(new CharSequence[stateListItems.size()]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        };
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
//        String serverUrl = "https://api.telldus.com/oauth2/devices/list";
//        new DeviceOperation().execute(serverUrl);
    }

//    private class DeviceOperation extends AsyncTask<String, Void, Void> {
//        @Override
//        protected Void doInBackground(String... urls) {
//            AndroidNetworking.post(urls[0])
//                .addHeaders("Content-Type", "application/json")
//                .addHeaders("Accpet", "application/json")
//                .addHeaders("Authorization", "Bearer " + accessToken)
//                .setPriority(Priority.LOW)
//                .build()
//                .getAsJSONObject(new JSONObjectRequestListener() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            JSONObject deviceData = new JSONObject(response.toString());
//                            JSONArray deviceNameList = deviceData.getJSONArray("device");
//                            nameListItems = new ArrayList<String>();
//                            for (int i = 0; i < deviceNameList.length(); i++) {
//                                JSONObject curObj = deviceNameList.getJSONObject(i);
//                                String name = curObj.getString("name");
//                                nameListItems.add(name);
//                                Log.d("************************************", name);
//                            }
//                            deviceList = nameListItems.toArray(new CharSequence[nameListItems.size()]);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        };
//                    }
//
//                    @Override
//                    public void onError(ANError anError) {
//
//                    }
//                });
//            return null;
//        }
//
//        protected void onPostExecute(String result) {
//            deviceList = nameListItems.toArray(new CharSequence[nameListItems.size()]);
//        }
//    }
}
