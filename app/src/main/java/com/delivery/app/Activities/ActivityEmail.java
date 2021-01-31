package com.delivery.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.delivery.app.Helper.SharedHelper;
import com.delivery.app.R;
import com.delivery.app.R;
import com.delivery.app.Utils.MyTextView;
import com.delivery.app.Utils.Utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jayakumar on 31/01/17.
 */

public class ActivityEmail extends AppCompatActivity {

    ImageView backArrow;
    FloatingActionButton nextICON;
    EditText email;
    MyTextView register, forgetPassword;
    LinearLayout lnrBegin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        email = findViewById(R.id.enter_ur_mailID);
        nextICON = findViewById(R.id.nextICON);
        backArrow = findViewById(R.id.backArrow);
        register = findViewById(R.id.register);
        forgetPassword = findViewById(R.id.forgetPassword);
        lnrBegin = findViewById(R.id.lnrBegin);
        nextICON.setOnClickListener(view -> {
            if (email.getText().toString().equals("") || email.getText().toString().equalsIgnoreCase(getString(R.string.sample_mail_id))) {
                displayMessage(getString(R.string.email_validation));
            } else {
                if ((!isValidEmail(email.getText().toString()))) {
                    displayMessage(getString(R.string.email_validation));
                } else {
                    Utilities.hideKeyboard(ActivityEmail.this);
                    SharedHelper.putKey(ActivityEmail.this, "email", email.getText().toString());
                    Intent mainIntent = new Intent(ActivityEmail.this, ActivityPassword.class);
                    startActivity(mainIntent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
            }
        });
        backArrow.setOnClickListener(view -> onBackPressed());
        register.setOnClickListener(view -> {
            SharedHelper.putKey(ActivityEmail.this, "password", "");
            Utilities.hideKeyboard(ActivityEmail.this);
            Intent mainIntent = new Intent(ActivityEmail.this, RegisterActivity.class);
            mainIntent.putExtra("isFromMailActivity", true);
            startActivity(mainIntent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        });
        forgetPassword.setOnClickListener(view -> {
            SharedHelper.putKey(ActivityEmail.this, "password", "");
            Utilities.hideKeyboard(ActivityEmail.this);
            Intent mainIntent = new Intent(ActivityEmail.this, ForgetPassword.class);
            mainIntent.putExtra("isFromMailActivity", true);
            startActivity(mainIntent);
        });
    }

    public void displayMessage(String toastString) {
        try {
            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, toastString, Toast.LENGTH_SHORT).show();
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
}