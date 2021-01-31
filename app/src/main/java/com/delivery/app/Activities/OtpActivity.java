package com.delivery.app.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.delivery.app.BuildConfig;
import com.delivery.app.Helper.CustomDialog;
import com.delivery.app.Helper.SharedHelper;
import com.delivery.app.Helper.URLHelper;
import com.delivery.app.MyApplication;
import com.delivery.app.R;
import com.delivery.app.Utils.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.philio.pinentry.PinEntryView;

public class OtpActivity extends AppCompatActivity {

    PinEntryView pinEntry;
    TextView otpDescription, resendOtp, voiceCallOtp;
    Button submit;
    String mobile, countryCode, OTP, description = "";
    CustomDialog customDialog;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("otp")) {
                final String message = intent.getStringExtra("message");
                System.out.println("BroadcastReceiver" + message);
                pinEntry.setText(message);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        customDialog = new CustomDialog(this);
        customDialog.setCancelable(false);
        pinEntry = findViewById(R.id.pin_entry);
        otpDescription = findViewById(R.id.otp_description);
        submit = findViewById(R.id.submit);
        resendOtp = findViewById(R.id.resend_otp);
        voiceCallOtp = findViewById(R.id.voice_call_otp);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mobile = extras.getString("mobile", "");
            countryCode = extras.getString("country_code", "");
            OTP = extras.getString("otp", "");
            otpDescription.setText("OTP sent to your mobile number " + mobile);
        }
        if (BuildConfig.DEBUG) pinEntry.setText(OTP);
        submit.setOnClickListener(v -> {
            if (pinEntry.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.invalid_otp), Toast.LENGTH_SHORT).show();
                return;
            }
            if (pinEntry.getText().toString().equalsIgnoreCase(OTP)) {
                SharedHelper.putKey(this, "mobile", mobile);
                Intent intent = new Intent();
                intent.putExtra("otp", pinEntry.getText().toString());
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, R.string.wrong_otp, Toast.LENGTH_SHORT).show();
            }
        });
        resendOtp.setOnClickListener(v -> {
            description = "OTP resent to your mobile number " + mobile;
            sendOTP();
        });
        voiceCallOtp.setOnClickListener(v -> {
            description = "You will receive voice call to your mobile number " + mobile;
            sendVoiceOTP();
        });
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("otp"));
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    private void sendOTP() {
        customDialog = new CustomDialog(this);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("mobile", mobile);
            object.put("phoneonly", mobile);
            object.put("country_code", countryCode);
            Utilities.print("InputToOTPAPI", "" + object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.otp, object, response -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            pinEntry.setText("");
            OTP = String.valueOf(response.opt("otp"));
            otpDescription.setText(description);
            Utilities.print("OTPResponse", response.toString());
        }, error -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            String json;
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                try {
                    JSONObject errorObj = new JSONObject(new String(response.data));
                    if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                        try {
                            displayMessage(errorObj.optString("message"));
                        } catch (Exception e) {
                            displayMessage(getString(R.string.something_went_wrong));
                        }
                    } else if (response.statusCode == 401) {
                        try {
                            if (!errorObj.optString("message").equalsIgnoreCase("invalid_token")) {
                                displayMessage(errorObj.optString("message"));
                            }
                        } catch (Exception e) {
                            displayMessage(getString(R.string.something_went_wrong));
                        }
                    } else if (response.statusCode == 422) {
                        json = MyApplication.trimMessage(new String(response.data));
                        if (json != null && !json.equals("")) {
                            if (json.startsWith("The email has already been taken")) {
                                displayMessage(getString(R.string.email_exist));
                            } else {
                                displayMessage(getString(R.string.mobile_exist));
                            }
                        } else {
                            displayMessage(getString(R.string.please_try_again));
                        }
                    } else {
                        displayMessage(getString(R.string.please_try_again));
                    }
                } catch (Exception e) {
                    displayMessage(getString(R.string.something_went_wrong));
                }
            } else {
                if (error instanceof NoConnectionError) {
                    displayMessage(getString(R.string.oops_connect_your_internet));
                } else if (error instanceof NetworkError) {
                    displayMessage(getString(R.string.oops_connect_your_internet));
                } else if (error instanceof TimeoutError) {
                    sendOTP();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                return headers;
            }
        };
        MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void sendVoiceOTP() {
        customDialog = new CustomDialog(this);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("mobile", mobile);
            object.put("country_code", countryCode);
            Utilities.print("InputToVoiceOTPAPI", "" + object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.voiceOtp, object, response -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            pinEntry.setText("");
            OTP = String.valueOf(response.opt("otp"));
            otpDescription.setText(description);
            Utilities.print("VoiceOTPResponse", response.toString());
        }, error -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            String json;
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                try {
                    JSONObject errorObj = new JSONObject(new String(response.data));
                    if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                        try {
                            displayMessage(errorObj.optString("message"));
                        } catch (Exception e) {
                            displayMessage(getString(R.string.something_went_wrong));
                        }
                    } else if (response.statusCode == 401) {
                        try {
                            if (!errorObj.optString("message").equalsIgnoreCase("invalid_token")) {
                                displayMessage(errorObj.optString("message"));
                            }
                        } catch (Exception e) {
                            displayMessage(getString(R.string.something_went_wrong));
                        }
                    } else if (response.statusCode == 422) {
                        json = MyApplication.trimMessage(new String(response.data));
                        if (json != null && !json.equals("")) {
                            if (json.startsWith("The email has already been taken")) {
                                displayMessage(getString(R.string.email_exist));
                            } else {
                                displayMessage(getString(R.string.something_went_wrong));
                            }
                        } else {
                            displayMessage(getString(R.string.please_try_again));
                        }
                    } else {
                        displayMessage(getString(R.string.please_try_again));
                    }
                } catch (Exception e) {
                    displayMessage(getString(R.string.something_went_wrong));
                }
            } else {
                if (error instanceof NoConnectionError) {
                    displayMessage(getString(R.string.oops_connect_your_internet));
                } else if (error instanceof NetworkError) {
                    displayMessage(getString(R.string.oops_connect_your_internet));
                } else if (error instanceof TimeoutError) {
                    sendVoiceOTP();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                return headers;
            }
        };
        MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void displayMessage(String toastString) {
        Utilities.print("displayMessage", "" + toastString);
        try {
            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            try {
                Toast.makeText(this, "" + toastString, Toast.LENGTH_SHORT).show();
            } catch (Exception ee) {
                e.printStackTrace();
            }
        }
    }
}