package com.delivery.app.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.delivery.app.Helper.CustomDialog;
import com.delivery.app.Helper.URLHelper;
import com.delivery.app.MyApplication;
import com.delivery.app.R;
import com.delivery.app.Utils.Utilities;
import com.rilixtech.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PhoneActivity extends AppCompatActivity {

    ImageView ivBack;
    FloatingActionButton fabNext;
    EditText etPhoneNumber;
    CountryCodePicker ccp;
    CustomDialog customDialog;
    public static int APP_REQUEST_CODE = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        customDialog = new CustomDialog(this);
        customDialog.setCancelable(false);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        ccp = findViewById(R.id.ccp);
        ccp.registerPhoneNumberTextView(etPhoneNumber);
        fabNext = findViewById(R.id.fabNext);
        ivBack = findViewById(R.id.ivBack);
        fabNext.setOnClickListener(view -> {
            if (etPhoneNumber.getText().toString().equals("")) {
                Snackbar.make(getCurrentFocus(), getString(R.string.phone_validation),
                        Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            } else {
                Utilities.hideKeyboard(PhoneActivity.this);
                sendOTP();
            }
        });
        ivBack.setOnClickListener(view -> onBackPressed());
    }

    private void sendOTP() {
        customDialog = new CustomDialog(this);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("mobile", etPhoneNumber.getText().toString());
            object.put("phoneonly", etPhoneNumber.getText().toString());
            object.put("country_code", ccp.getSelectedCountryCodeWithPlus());
            Utilities.print("InputToOTPAPI", "" + object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.otp, object, response -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            if (response.opt("otp") != null) {
                Intent mainIntent = new Intent(this, OtpActivity.class);
                mainIntent.putExtra("mobile", etPhoneNumber.getText().toString());
                mainIntent.putExtra("country_code", ccp.getSelectedCountryCodeWithPlus());
                mainIntent.putExtra("otp", String.valueOf(response.opt("otp")));
                startActivityForResult(mainIntent, APP_REQUEST_CODE);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            } else {
                displayMessage(String.valueOf(response.opt("status")));
            }
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

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request\
                Intent intent = new Intent();
                intent.putExtra("mobile", etPhoneNumber.getText().toString());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }
}