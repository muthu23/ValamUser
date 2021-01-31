package com.delivery.app.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.delivery.app.Helper.AppHelper;
import com.delivery.app.Helper.ConnectionHelper;
import com.delivery.app.Helper.CustomDialog;
import com.delivery.app.Helper.SharedHelper;
import com.delivery.app.Helper.URLHelper;
import com.delivery.app.Helper.VolleyMultipartRequest;
import com.delivery.app.MyApplication;
import com.delivery.app.R;
import com.delivery.app.Utils.Utilities;
import com.rilixtech.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAGG = "RegisterDocument";
    private static final int SELECT_PHOTO = 100;
    public static int APP_REQUEST_CODE = 99;
    public static int deviceHeight;
    public static int deviceWidth;
    public Context context = RegisterActivity.this;
    public Activity activity = RegisterActivity.this;
    String TAG = "RegisterActivity";
    String strViewPager = "";
    String device_token, device_UDID;
    ImageView backArrow, iv_state, iv_medCard;
    FloatingActionButton nextICON;
    EditText email, first_name, last_name, mobile_no, password;
    CustomDialog customDialog;
    ConnectionHelper helper;
    Boolean isInternet;
    Boolean fromActivity = false;
    RadioGroup radio;
    RadioButton business_user_btn, normal_user_btn, senior_user_btn;
    String user_type = "";
    CountryCodePicker ccp;
    LinearLayout image_layout;
    Boolean isPermissionGivenAlready = false;
    int senior_citizen = 0;
    Bitmap stateBitmap = null;
    Bitmap medcalBitmap = null;
    private String mImageSrcType;

    private static Bitmap getBitmapFromUri(@NonNull Context context, @NonNull Uri uri) throws IOException {
        Log.e(TAGG, "getBitmapFromUri: Resize uri" + uri);
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        assert parcelFileDescriptor != null;
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        Log.e(TAGG, "getBitmapFromUri: Height" + deviceHeight);
        Log.e(TAGG, "getBitmapFromUri: width" + deviceWidth);
        int maxSize = Math.min(deviceHeight, deviceWidth);
        if (image != null) {
            Log.e(TAGG, "getBitmapFromUri: Width" + image.getWidth());
            Log.e(TAGG, "getBitmapFromUri: Height" + image.getHeight());
            int inWidth = image.getWidth();
            int inHeight = image.getHeight();
            int outWidth;
            int outHeight;
            if (inWidth > inHeight) {
                outWidth = maxSize;
                outHeight = (inHeight * maxSize) / inWidth;
            } else {
                outHeight = maxSize;
                outWidth = (inWidth * maxSize) / inHeight;
            }
            return Bitmap.createScaledBitmap(image, outWidth, outHeight, false);
        } else {
            Toast.makeText(context, context.getString(R.string.valid_image), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public static byte[] getFileDataFromDrawable(Drawable drawable) {
        try {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            return byteArrayOutputStream.toByteArray();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        try {
            Intent intent = getIntent();
            if (intent != null) {
                if (getIntent().getExtras().containsKey("viewpager")) {
                    strViewPager = getIntent().getExtras().getString("viewpager");
                }
                if (getIntent().getExtras().getBoolean("isFromMailActivity")) {
                    fromActivity = true;
                } else if (!getIntent().getExtras().getBoolean("isFromMailActivity")) {
                    fromActivity = false;
                } else {
                    fromActivity = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fromActivity = false;
        }
        findViewById();
        GetToken();


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;

        radio.setOnCheckedChangeListener((group, checkedId) -> {
            // Log.d("chk", "id" + checkedId);
            if (checkedId == R.id.normal_user_btn) {

                if (image_layout.getVisibility() == View.VISIBLE) {
                    image_layout.setVisibility(View.GONE);
                }
                user_type = "NORMAL";
            } else if (checkedId == R.id.business_user_btn) {
                user_type = "BUSINESSUSER";
                if (image_layout.getVisibility() == View.VISIBLE) {
                    image_layout.setVisibility(View.GONE);
                }
            } else if (checkedId == R.id.senior_user_btn) {
                user_type = "SENIORUSER";
                image_layout.setVisibility(View.VISIBLE);
                senior_citizen = 1;
            }

        });


        nextICON.setOnClickListener(view -> {
            Pattern ps = Pattern.compile(".*[0-9].*");
            Matcher firstName = ps.matcher(first_name.getText().toString());
            Matcher lastName = ps.matcher(last_name.getText().toString());
            if (email.getText().toString().equals("") || email.getText().toString().equalsIgnoreCase(getString(R.string.sample_mail_id))) {
                displayMessage(getString(R.string.email_validation));
            } else if (!Utilities.isValidEmail(email.getText().toString())) {
                displayMessage(getString(R.string.not_valid_email));
            } else if (first_name.getText().toString().equals("") || first_name.getText().toString().equalsIgnoreCase(getString(R.string.first_name))) {
                displayMessage(getString(R.string.first_name_empty));
            } else if (firstName.matches()) {
                displayMessage(getString(R.string.first_name_no_number));
            } else if (last_name.getText().toString().equals("") || last_name.getText().toString().equalsIgnoreCase(getString(R.string.last_name))) {
                displayMessage(getString(R.string.last_name_empty));
            } else if (lastName.matches()) {
                displayMessage(getString(R.string.last_name_no_number));
            } else if (mobile_no.getText().toString().equals("") || mobile_no.getText().toString().equalsIgnoreCase(getString(R.string.mobile_no))) {
                displayMessage(getString(R.string.mobile_number_empty));
            } else if (password.getText().toString().equals("") || password.getText().toString().equalsIgnoreCase(getString(R.string.password_txt))) {
                displayMessage(getString(R.string.password_validation));
            } else if (password.length() < 6) {
                displayMessage(getString(R.string.password_size));
            } else {

                if (senior_citizen == 1) {
                    if (stateBitmap == null) {
                        displayMessage(getString(R.string.stateImg_req));
                    } else if (medcalBitmap == null) {
                        displayMessage(getString(R.string.medicalImg_req));
                    } else {
                        if (isInternet) {
                            checkMailAlreadyExit();
                        } else {
                            displayMessage(getString(R.string.something_went_wrong_net));
                        }
                    }
                } else {
                    if (isInternet) {
                        checkMailAlreadyExit();
                    } else {
                        displayMessage(getString(R.string.something_went_wrong_net));
                    }
                }
            }
        });
        backArrow.setOnClickListener(view -> onBackPressed());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (customDialog != null && customDialog.isShowing())
            customDialog.dismiss();
    }

    public void findViewById() {
        email = findViewById(R.id.email);
        first_name = findViewById(R.id.first_name);
        last_name = findViewById(R.id.last_name);
        mobile_no = findViewById(R.id.mobile_no);
        password = findViewById(R.id.password);
        nextICON = findViewById(R.id.nextIcon);
        backArrow = findViewById(R.id.backArrow);
        helper = new ConnectionHelper(context);
        business_user_btn = findViewById(R.id.business_user_btn);
        normal_user_btn = findViewById(R.id.normal_user_btn);
        senior_user_btn = findViewById(R.id.senior_user_btn);
        image_layout = findViewById(R.id.image_layout);

        iv_medCard = findViewById(R.id.doc_medical_id);
        iv_state = findViewById(R.id.doc_state_id);
        iv_medCard.setOnClickListener(this);
        iv_state.setOnClickListener(this);

        radio = findViewById(R.id.radiogroup);
        ccp = findViewById(R.id.ccp);
        ccp.registerPhoneNumberTextView(mobile_no);
        isInternet = helper.isConnectingToInternet();
        if (!fromActivity) {
            email.setText(SharedHelper.getKey(context, "email"));
        }
    }

    public void checkMailAlreadyExit() {
        customDialog = new CustomDialog(RegisterActivity.this);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("email", email.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.CHECK_MAIL_ALREADY_REGISTERED,
                object, response -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            sendOTP();
        }, error -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            String json;
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                Utilities.print("MyTest", "" + error);
                Utilities.print("MyTestError", "" + error.networkResponse);
                Utilities.print("MyTestError1", "" + response.statusCode);
                try {
                    if (response.statusCode == 422) {
                        json = MyApplication.trimMessage(new String(response.data));
                        if (json != null && !json.equals("")) {
                            if (json.startsWith(getString(R.string.email_exist))) {
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
                    checkMailAlreadyExit();
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

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
                registerAPI();
            }
        }
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                //bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
                Bitmap resizeImg = getBitmapFromUri(this, uri);
                if (resizeImg != null && AppHelper.getPath(this, uri) != null) {
                    Bitmap reRotateImg = AppHelper.modifyOrientation(resizeImg, AppHelper.getPath(this, uri));

                    if (mImageSrcType.equals("STATEID")) {
                        iv_state.setImageBitmap(reRotateImg);
                        stateBitmap = reRotateImg;

                    } else {
                        iv_medCard.setImageBitmap(reRotateImg);
                        medcalBitmap = reRotateImg;

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendOTP() {
        customDialog = new CustomDialog(this);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("mobile", mobile_no.getText().toString());
            object.put("phoneonly", mobile_no.getText().toString());
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
                mainIntent.putExtra("mobile", mobile_no.getText().toString());
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

    private void registerAPI() {

        String stateIMG = "";
        String medicalIMG = "";
        if (senior_citizen == 1) {
            if (stateBitmap != null) {
                stateIMG = convertimageointostring(stateBitmap);
                if (medcalBitmap != null) {
                    medicalIMG = convertimageointostring(medcalBitmap);
                }
            }
        }


        customDialog = new CustomDialog(RegisterActivity.this);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("device_type", "android");
            object.put("device_id", device_UDID);
            object.put("device_token", "" + device_token);
            object.put("login_by", "manual");
            object.put("first_name", first_name.getText().toString());
            object.put("last_name", last_name.getText().toString());
            object.put("email", email.getText().toString());
            object.put("password", password.getText().toString());
            object.put("mobile", SharedHelper.getKey(RegisterActivity.this, "mobile"));
            object.put("picture", "");
            object.put("social_unique_id", "");

            if (user_type.equalsIgnoreCase("")) {
                object.put("user_type", "NORMAL");
            } else {
                object.put("user_type", user_type);
            }

            if (senior_citizen == 1) {
                object.put("state_id", stateIMG);
                object.put("medicare_id", medicalIMG);
            }

            Utilities.print("InputToRegisterAPI", "" + object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

       /* JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.register, object, response -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            Utilities.print("SignInResponse", response.toString());
            SharedHelper.putKey(RegisterActivity.this, "email", email.getText().toString());
            SharedHelper.putKey(RegisterActivity.this, "password", password.getText().toString());
            signIn();
        }, error -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            String json;
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                Utilities.print("MyTest", "" + error);
                Utilities.print("MyTestError", "" + error.networkResponse);
                Utilities.print("MyTestError1", "" + response.statusCode);
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
                        json = trimMessage(new String(response.data));
                        if (json != null && !json.equals("")) {
                            if (json.startsWith("The email has already been taken")) {
                                displayMessage(getString(R.string.email_exist));
                            } else {
                                displayMessage(getString(R.string.something_went_wrong));
                            }
                            //displayMessage(json);
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
                    registerAPI();
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
        MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);*/

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, URLHelper.register,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(new String(response.data));
                        //  Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                        Log.d(" Edit responce", obj.toString());
                        Utilities.print("SignInResponse", response.toString());
                        SharedHelper.putKey(RegisterActivity.this, "email", email.getText().toString());
                        SharedHelper.putKey(RegisterActivity.this, "password", password.getText().toString());
                        signIn();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if ((customDialog != null) && (customDialog.isShowing()))
                        customDialog.dismiss();
                    String json;
                    NetworkResponse response = error.networkResponse;
                    if (response != null && response.data != null) {
                        Utilities.print("MyTest", "" + error);
                        Utilities.print("MyTestError", "" + error.networkResponse);
                        Utilities.print("MyTestError1", "" + response.statusCode);
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
                                    //displayMessage(json);
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
                            registerAPI();
                        }
                    }
                }) {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("device_type", "android");
                params.put("device_id", device_UDID);
                params.put("device_token", "" + device_token);
                params.put("login_by", "manual");
                params.put("first_name", first_name.getText().toString());
                params.put("last_name", last_name.getText().toString());
                params.put("email", email.getText().toString());
                params.put("password", password.getText().toString());
                params.put("mobile", SharedHelper.getKey(RegisterActivity.this, "mobile"));
                params.put("picture", "");
                params.put("social_unique_id", "");

                if (user_type.equalsIgnoreCase("")) {
                    params.put("user_type", "NORMAL");
                } else {
                    params.put("user_type", user_type);
                }

                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                if (senior_citizen == 1) {
//                    params.put("state_id", stateIMG);
//                    params.put("medicare_id", medicalIMG);
                    params.put("state_id", new DataPart(imagename + ".png", getFileDataFromDrawable(iv_state.getDrawable()), "image/jpeg"));
                    params.put("medicare_id", new DataPart(imagename + ".png", getFileDataFromDrawable(iv_medCard.getDrawable()), "image/jpeg"));

                }
                return params;
            }
        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        //adding the request to volley
        MyApplication.getInstance().addToRequestQueue(volleyMultipartRequest);

    }

    private void GoToBeginActivity() {
        if (customDialog != null && customDialog.isShowing())
            customDialog.dismiss();
        Intent mainIntent = new Intent(RegisterActivity.this, WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        RegisterActivity.this.finish();
    }

    public void signIn() {
        if (isInternet) {
            customDialog = new CustomDialog(RegisterActivity.this);
            customDialog.setCancelable(false);
            if (customDialog != null)
                customDialog.show();
            JSONObject object = new JSONObject();
            try {
                object.put("grant_type", "password");
                object.put("client_id", URLHelper.client_id);
                object.put("client_secret", URLHelper.client_secret);
                object.put("username", SharedHelper.getKey(RegisterActivity.this, "email"));
                object.put("password", SharedHelper.getKey(RegisterActivity.this, "password"));
                object.put("scope", "");
                object.put("device_type", "android");
                object.put("device_id", device_UDID);
                object.put("device_token", device_token);
                Utilities.print("InputToLoginAPI", "" + object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.login, object, response -> {
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
                Utilities.print("SignUpResponse", response.toString());
                SharedHelper.putKey(context, "access_token", response.optString("access_token"));
                SharedHelper.putKey(context, "refresh_token", response.optString("refresh_token"));
                SharedHelper.putKey(context, "token_type", response.optString("token_type"));
                getProfile();
            }, error -> {
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
                String json = null;
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
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        signIn();
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
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

    public void getProfile() {
        if (isInternet) {
            customDialog = new CustomDialog(RegisterActivity.this);
            customDialog.setCancelable(false);
            if (customDialog != null)
                customDialog.show();
            JSONObject object = new JSONObject();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URLHelper.UserProfile + "?device_type=android&device_id=" + device_UDID + "&device_token=" + device_token, object, response -> {
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
                Utilities.print("GetProfile", response.toString());
                SharedHelper.putKey(RegisterActivity.this, "id", response.optString("id"));
                SharedHelper.putKey(RegisterActivity.this, "first_name", response.optString("first_name"));
                SharedHelper.putKey(RegisterActivity.this, "last_name", response.optString("last_name"));
                SharedHelper.putKey(RegisterActivity.this, "email", response.optString("email"));
                SharedHelper.putKey(RegisterActivity.this, "picture", URLHelper.base + "storage/" + response.optString("picture"));
                SharedHelper.putKey(RegisterActivity.this, "gender", response.optString("gender"));
                SharedHelper.putKey(RegisterActivity.this, "mobile", response.optString("mobile"));
                SharedHelper.putKey(RegisterActivity.this, "wallet_balance", response.optString("wallet_balance"));
                SharedHelper.putKey(RegisterActivity.this, "payment_mode", response.optString("payment_mode"));
                if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                    SharedHelper.putKey(context, "currency", response.optString("currency"));
                else
                    SharedHelper.putKey(context, "currency", "R");
                SharedHelper.putKey(context, "sos", response.optString("sos"));
                SharedHelper.putKey(RegisterActivity.this, "loggedIn", getString(R.string.True));
                //phoneLogin();
                GoToMainActivity();
               /* if (!SharedHelper.getKey(activity,"account_kit_token").equalsIgnoreCase("")) {

                }else {
                    GoToMainActivity();
                }*/
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
                                if (errorObj.optString("message").equalsIgnoreCase("invalid_token")) {
                                    refreshAccessToken();
                                } else {
                                    displayMessage(errorObj.optString("message"));
                                }
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
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        getProfile();
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "" + SharedHelper.getKey(RegisterActivity.this, "token_type") + " " + SharedHelper.getKey(RegisterActivity.this, "access_token"));
                    return headers;
                }
            };
            MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

    private void refreshAccessToken() {
        JSONObject object = new JSONObject();
        try {
            object.put("grant_type", "refresh_token");
            object.put("client_id", URLHelper.client_id);
            object.put("client_secret", URLHelper.client_secret);
            object.put("refresh_token", SharedHelper.getKey(context, "refresh_token"));
            object.put("scope", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.login, object, response -> {
            Log.v("SignUpResponse", response.toString());
            SharedHelper.putKey(context, "access_token", response.optString("access_token"));
            SharedHelper.putKey(context, "refresh_token", response.optString("refresh_token"));
            SharedHelper.putKey(context, "token_type", response.optString("token_type"));
            getProfile();
        }, error -> {
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                SharedHelper.putKey(context, "loggedIn", getString(R.string.False));
                GoToBeginActivity();
            } else {
                if (error instanceof NoConnectionError) {
                    displayMessage(getString(R.string.oops_connect_your_internet));
                } else if (error instanceof NetworkError) {
                    displayMessage(getString(R.string.oops_connect_your_internet));
                } else if (error instanceof TimeoutError) {
                    refreshAccessToken();
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

    public void GetToken() {
        try {
            if (!SharedHelper.getKey(context, "device_token").equals("") && SharedHelper.getKey(context, "device_token") != null) {
                device_token = SharedHelper.getKey(context, "device_token");
                Utilities.print(TAG, "GCM Registration Token: " + device_token);
            } else {
                device_token = "" + FirebaseInstanceId.getInstance().getToken();
                SharedHelper.putKey(context, "device_token", "" + FirebaseInstanceId.getInstance().getToken());
                Utilities.print(TAG, "Failed to complete token refresh: " + device_token);
            }
        } catch (Exception e) {
            device_token = "COULD NOT GET FCM TOKEN";
            Utilities.print(TAG, "Failed to complete token refresh");
        }
        try {
            device_UDID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            Utilities.print(TAG, "Device UDID:" + device_UDID);
        } catch (Exception e) {
            device_UDID = "COULD NOT GET UDID";
            e.printStackTrace();
            Utilities.print(TAG, "Failed to complete device UDID");
        }
    }

    public void GoToMainActivity() {
        if (customDialog != null && customDialog.isShowing())
            customDialog.dismiss();
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        RegisterActivity.this.finish();
    }

    public void displayMessage(String toastString) {
        Utilities.print("displayMessage", "" + toastString);
        try {
            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            try {
                Toast.makeText(context, "" + toastString, Toast.LENGTH_SHORT).show();
            } catch (Exception ee) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.doc_state_id) {
            mImageSrcType = "STATEID";
            if (checkStoragePermission())
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            else
                goToImageIntent();
        } else if (id == R.id.doc_medical_id) {
            mImageSrcType = "MEDICALID";
            if (checkStoragePermission())
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            else
                goToImageIntent();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    if (!isPermissionGivenAlready) {
                        goToImageIntent();
                    }
                }
            }
        }
    }

    public void goToImageIntent() {
        isPermissionGivenAlready = true;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PHOTO);
    }

    public String convertimageointostring(Bitmap bitmap) {


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

        return encodedImage;
    }


}