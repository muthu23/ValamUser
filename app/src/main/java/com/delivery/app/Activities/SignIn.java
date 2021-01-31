package com.delivery.app.Activities;

import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.delivery.app.Helper.ConnectionHelper;
import com.delivery.app.Helper.CustomDialog;
import com.delivery.app.Helper.SharedHelper;
import com.delivery.app.Helper.URLHelper;
import com.delivery.app.MyApplication;
import com.delivery.app.R;
import com.delivery.app.Utils.Utilities;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jayakumar on 22/06/17.
 */

public class SignIn extends AppCompatActivity {

    EditText txtemail, txtpassword;
    TextView lblforgotpassword;
    Button btnSignIn;

    AccessTokenTracker accessTokenTracker;
    CallbackManager callbackManager;
    Activity thisActivity;
    Boolean isInternet;
    ConnectionHelper helper;
    CustomDialog customDialog;
    LinearLayout lnrRegister;
    String TAG = "SignIn";
    public Context context = SignIn.this;

    String device_token, device_UDID;
    TextView connectTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        thisActivity = this;
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
        setContentView(R.layout.activity_begin_signin);
        helper = new ConnectionHelper(thisActivity);
        isInternet = helper.isConnectingToInternet();
        txtemail = findViewById(R.id.txtemail);
        lnrRegister = findViewById(R.id.lnrRegister);
        lblforgotpassword = findViewById(R.id.lblforgotpassword);
        txtpassword = findViewById(R.id.txtpassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        connectTxt = findViewById(R.id.connect_social_txt);
        connectTxt.setOnClickListener(v -> {
            Intent mainIntent = new Intent(SignIn.this, ActivitySocialLogin.class);
            startActivity(mainIntent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        });
        /*----------Facebook Login---------------*/
        callbackManager = CallbackManager.Factory.create();
        /*if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }*/
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException ignored) {

        }
        GetToken();
        lblforgotpassword.setOnClickListener(v -> {
            SharedHelper.putKey(thisActivity, "password", "");
            Intent mainIntent = new Intent(thisActivity, ForgetPassword.class);
            startActivity(mainIntent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        });
        lnrRegister.setOnClickListener(view -> {
            SharedHelper.putKey(getApplicationContext(), "from", "email");
            SharedHelper.putKey(getApplicationContext(), "email", "" + txtemail.getText().toString());
            Intent mainIntent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(mainIntent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        });
        btnSignIn.setOnClickListener(view -> {
            if (txtemail.getText().toString().equals("") || txtemail.getText().toString().equalsIgnoreCase(getString(R.string.sample_mail_id))) {
                displayMessage(getString(R.string.email_validation));
            } else if ((!isValidEmail(txtemail.getText().toString()))) {
                displayMessage(getString(R.string.not_valid_email));
            } else if (txtpassword.getText().toString().length() == 0) {
                displayMessage(getString(R.string.password_validation));
            } else if (txtpassword.length() < 6) {
                displayMessage(getString(R.string.password_size));
            } else {
                SharedHelper.putKey(thisActivity, "email", txtemail.getText().toString());
                SharedHelper.putKey(context, "password", txtpassword.getText().toString());
                signIn();
            }
        });
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
            }
        };
    }

    private void login(final String accesstoken, final String URL, final String Loginby) {
        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        final JsonObject json = new JsonObject();
        json.addProperty("device_type", "android");
        json.addProperty("device_token", device_token);
        json.addProperty("accessToken", accesstoken);
        json.addProperty("device_id", device_UDID);
        json.addProperty("login_by", Loginby);
//        json.addProperty("mobile",mobileNumber);
        Log.e(TAG, "login: Facebook" + json);
        Ion.with(SignIn.this)
                .load(URL)
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback((e, result) -> {
                    // do stuff with the result or error
                    if ((customDialog != null) && customDialog.isShowing())
                        customDialog.dismiss();
                    if (e != null) {
                        if (e instanceof NetworkErrorException) {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        } else if (e instanceof TimeoutException) {
                            login(accesstoken, URL, Loginby);
                        }
                        return;
                    }
                    if (result != null) {
                        Log.v(Loginby + "_Response", result.toString());
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());
                            String status = jsonObject.optString("status");
                            if (status.equalsIgnoreCase("true")) {
                                SharedHelper.putKey(SignIn.this, "token_type", jsonObject.optString("token_type"));
                                SharedHelper.putKey(SignIn.this, "access_token", jsonObject.optString("access_token"));
                                if (Loginby.equalsIgnoreCase("facebook"))
                                    SharedHelper.putKey(SignIn.this, "login_by", "facebook");
                                if (!jsonObject.optString("currency").equalsIgnoreCase("") && jsonObject.optString("currency") != null)
                                    SharedHelper.putKey(context, "currency", jsonObject.optString("currency"));
                                else
                                    SharedHelper.putKey(context, "currency", "R");
                                //phoneLogin();
                                getProfile();
                            } else {
                                GoToBeginActivity();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        displayMessage(getString(R.string.please_try_again));
                    }
                    // onBackPressed();
                });
    }

    private void signIn() {
        if (isInternet) {
            customDialog = new CustomDialog(thisActivity);
            customDialog.setCancelable(false);
            customDialog.show();
            JSONObject object = new JSONObject();
            try {
                object.put("grant_type", "password");
                object.put("client_id", URLHelper.client_id);
                object.put("client_secret", URLHelper.client_secret);
                object.put("username", SharedHelper.getKey(thisActivity, "email"));
                object.put("password", SharedHelper.getKey(thisActivity, "password"));
                object.put("scope", "");
                object.put("device_type", "android");
                object.put("device_id", device_UDID);
                object.put("device_token", device_token);
                Utilities.print("InputToLoginAPI", "" + object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.login, object, response -> {
                Utilities.print("SignUpResponse", response.toString());
                SharedHelper.putKey(thisActivity, "access_token", response.optString("access_token"));
                SharedHelper.putKey(thisActivity, "refresh_token", response.optString("refresh_token"));
                SharedHelper.putKey(thisActivity, "token_type", response.optString("token_type"));
                getProfile();
            }, error -> {
                customDialog.dismiss();
                String json;
                NetworkResponse response = error.networkResponse;
                Utilities.print("MyTest", "" + error);
                Utilities.print("MyTestError", "" + error.networkResponse);
                if (response != null && response.data != null) {
                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));
                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500 || response.statusCode == 401) {
                            try {
                                displayMessage(errorObj.optString("message"));
                            } catch (Exception e) {
                                displayMessage(getString(R.string.something_went_wrong));
                            }
                        } else if (response.statusCode == 422) {
                            json = MyApplication.trimMessage(new String(response.data));
                            if (json != null && !json.equals("")) {
                                displayMessage(json);
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
                    displayMessage(getString(R.string.please_try_again));
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
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

    public void getProfile() {
        if (isInternet) {
            JSONObject object = new JSONObject();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URLHelper.getUserProfileUrl + "?device_type=android&device_id=" + device_UDID + "&device_token=" + device_token, object, response -> {
                customDialog.dismiss();
                Utilities.print("GetProfile", response.toString());
                SharedHelper.putKey(thisActivity, "id", response.optString("id"));
                SharedHelper.putKey(thisActivity, "first_name", response.optString("first_name"));
                SharedHelper.putKey(thisActivity, "last_name", response.optString("last_name"));
                SharedHelper.putKey(thisActivity, "email", response.optString("email"));
                SharedHelper.putKey(context, "user_type", response.optString("user_type"));
                if (response.optString("picture").startsWith("http"))
                    SharedHelper.putKey(context, "picture", response.optString("picture"));
                else
                    SharedHelper.putKey(context, "picture", URLHelper.base + "storage/" + response.optString("picture"));
                SharedHelper.putKey(thisActivity, "gender", response.optString("gender"));
                SharedHelper.putKey(thisActivity, "mobile", response.optString("mobile"));
                SharedHelper.putKey(thisActivity, "wallet_balance", response.optString("wallet_balance"));
                SharedHelper.putKey(thisActivity, "payment_mode", response.optString("payment_mode"));
                if (response.optString("currency").equalsIgnoreCase("") || response.optString("currency") == null) {
                    SharedHelper.putKey(thisActivity, "currency", "R");
                } else {
                    SharedHelper.putKey(thisActivity, "currency", response.optString("currency"));
                }
                SharedHelper.putKey(thisActivity, "loggedIn", getString(R.string.True));
                GoToMainActivity();
            }, error -> {
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
                            refreshAccessToken();
                        } else if (response.statusCode == 422) {
                            json = MyApplication.trimMessage(new String(response.data));
                            if (json != null && !json.equals("")) {
                                displayMessage(json);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }
                        } else if (response.statusCode == 503) {
                            displayMessage(getString(R.string.server_down));
                        } else {
                            displayMessage(getString(R.string.please_try_again));
                        }
                    } catch (Exception e) {
                        displayMessage(getString(R.string.something_went_wrong));
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "" + SharedHelper.getKey(thisActivity, "token_type") + " "
                            + SharedHelper.getKey(thisActivity, "access_token"));
                    Utilities.print("authoization", "" + SharedHelper.getKey(thisActivity, "token_type") + " "
                            + SharedHelper.getKey(thisActivity, "access_token"));
                    return headers;
                }
            };
            MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

    public void GoToMainActivity() {
        Intent mainIntent = new Intent(thisActivity, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        thisActivity.finish();
    }

    private void refreshAccessToken() {
        if (isInternet) {
            customDialog = new CustomDialog(thisActivity);
            customDialog.setCancelable(false);
            customDialog.show();
            JSONObject object = new JSONObject();
            try {
                object.put("grant_type", "refresh_token");
                object.put("client_id", URLHelper.client_id);
                object.put("client_secret", URLHelper.client_secret);
                object.put("refresh_token", SharedHelper.getKey(thisActivity, "refresh_token"));
                object.put("scope", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.login, object, response -> {
                customDialog.dismiss();
                Utilities.print("SignUpResponse", response.toString());
                SharedHelper.putKey(thisActivity, "access_token", response.optString("access_token"));
                SharedHelper.putKey(thisActivity, "refresh_token", response.optString("refresh_token"));
                SharedHelper.putKey(thisActivity, "token_type", response.optString("token_type"));
                getProfile();
            }, error -> {
                customDialog.dismiss();
                NetworkResponse response = error.networkResponse;
                Utilities.print("MyTest", "" + error);
                Utilities.print("MyTestError", "" + error.networkResponse);
                Utilities.print("MyTestError1", "" + response.statusCode);
                if (response.data != null) {
                    SharedHelper.putKey(thisActivity, "loggedIn", getString(R.string.False));
                    GoToBeginActivity();
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
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

    public void GoToBeginActivity() {
        Intent mainIntent = new Intent(thisActivity, WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
        thisActivity.finish();
    }

    public void displayMessage(String toastString) {
        try {
            Snackbar snackbar = Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(this.getResources().getColor(R.color.black));
            TextView textView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setTextColor(this.getResources().getColor(R.color.white));
            snackbar.show();
        } catch (Exception e) {
            try {
                Toast.makeText(SignIn.this, "" + toastString, Toast.LENGTH_SHORT).show();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    public void GetToken() {
        try {
            if (!SharedHelper.getKey(thisActivity, "device_token").equals("") && SharedHelper.getKey(thisActivity, "device_token") != null) {
                device_token = SharedHelper.getKey(thisActivity, "device_token");
                Utilities.print(TAG, "GCM Registration Token: " + device_token);
            } else {
                device_token = "" + FirebaseInstanceId.getInstance().getToken();
                SharedHelper.putKey(thisActivity, "device_token", "" + FirebaseInstanceId.getInstance().getToken());
                Utilities.print(TAG, "Failed to complete token refresh: " + device_token);
            }
        } catch (Exception e) {
            device_token = "COULD NOT GET FCM TOKEN";
            Utilities.print(TAG, "Failed to complete token refresh");
        }
        try {
            device_UDID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            Utilities.print(TAG, "Device UDID:" + device_UDID);
        } catch (Exception e) {
            device_UDID = "COULD NOT GET UDID";
            e.printStackTrace();
            Utilities.print(TAG, "Failed to complete device UDID");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }*/
        if (resultCode == RESULT_OK && data != null) {
            /*Uri filePath = data.getData();
            Cursor cursor = null;
            try {
                String[] proj = {MediaStore.Images.Media.DATA};
                cursor = thisActivity.getContentResolver().query(filePath, proj, null, null, null);
                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
//                strImagePath = cursor.getString(column_index);
//                Log.e("path", "" + strImagePath);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
//                imgProfile.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }*/
            login(SharedHelper.getKey(context, "accessToken"), URLHelper.FACEBOOK_LOGIN, "facebook");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }
}