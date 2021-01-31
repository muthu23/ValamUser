package com.delivery.app.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.delivery.app.Adapter.PlacesAutoCompleteAdapter;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.delivery.app.Helper.CustomDialog;
import com.delivery.app.Helper.LocaleHelper;
import com.delivery.app.Helper.SharedHelper;
import com.delivery.app.Models.PlacePredictions;
import com.delivery.app.R;
import com.delivery.app.Retrofit.ApiInterface;
import com.delivery.app.Retrofit.RetrofitClient;
import com.delivery.app.Utils.MyBoldTextView;
import com.delivery.app.Utils.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by Esack N on 9/28/2017.
 */

public class AddHomeWorkActivity  extends AppCompatActivity implements PlacesAutoCompleteAdapter.ClickListener {

    private RecyclerView rvRecentResults,rvLocation;
    private EditText txtLocation;
    private PlacePredictions predictions = new PlacePredictions();
    private static final int MY_PERMISSIONS_REQUEST_LOC = 30;
    private Utilities utils = new Utilities();
    private PlacePredictions placePredictions = new PlacePredictions();
    private GoogleApiClient mGoogleApiClient;
    private Handler handler;
    private Location mLastLocation;
    double latitude;
    double longitude;
    ImageView backArrow;
    private String GETPLACESHIT = "places_hit";
    private ApiInterface mApiInterface;
    private String strTag = "";
    private CustomDialog customDialog;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient placesClient;
    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;
    LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_work);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initializePlacesApiClient();

        strTag = getIntent().getExtras().getString("tag");
        init();
    }

    private void init() {
        rvRecentResults = (RecyclerView) findViewById(R.id.rvRecentResults);
        rvLocation = findViewById(R.id.locations_rv);
        txtLocation = (EditText) findViewById(R.id.txtLocation);
        backArrow = (ImageView) findViewById(R.id.backArrow);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        customDialog = new CustomDialog(AddHomeWorkActivity.this);

        getFavoriteLocations();

        //get permission for Android M
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getLastKnownLocation();
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOC);
            } else {
                getLastKnownLocation();
            }
        }
        //Add a text change listener to implement autocomplete functionality
        txtLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals("")) {
                    mAutoCompleteAdapter.getFilter().filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });
        initializeadapter();
    }

    private void initializePlacesApiClient() {
        // Initialize the SDK
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        // Create a new Places client instance
        placesClient = Places.createClient(this);
    }

    public String getPlaceAutoCompleteUrl(String input) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/autocomplete/json");
        urlString.append("?input=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&location=");
        urlString.append(latitude + "," + longitude); // append lat long of current location to show nearby results.
        urlString.append("&radius=500&language=en");
        urlString.append("&key=" + getResources().getString(R.string.google_maps_key));

        Log.d("FINAL URL:::   ", urlString.toString());
        return urlString.toString();
    }

    @Override
    public void place(Place place) {
        if (place!=null) {
            LatLng latLng = place.getLatLng();
            setGoogleAddress(place);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted!
                    getLastKnownLocation();
                } else {
                    // permission denied!
                    Toast.makeText(this, "Please grant permission for using this app!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    public void initializeadapter(){
        mAutoCompleteAdapter = new PlacesAutoCompleteAdapter(this,placesClient);
        mAutoCompleteAdapter.setClickListener(this);
        mLinearLayoutManager = new LinearLayoutManager(this);
        rvLocation.setLayoutManager(mLinearLayoutManager);
        rvLocation.setAdapter(mAutoCompleteAdapter);
    }
    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mLastLocation = location;
                            latitude = mLastLocation.getLatitude();
                            longitude = mLastLocation.getLongitude();
                        }
                    }
                });
    }

    void setAddress() {
        utils.hideKeypad(AddHomeWorkActivity.this, getCurrentFocus());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                if (placePredictions != null) {
                    setResult(RESULT_OK, intent);
                } else {
                    setResult(RESULT_CANCELED, intent);
                }
                finish();
            }
        }, 500);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private class RecentPlacesAdapter extends RecyclerView.Adapter<RecentPlacesAdapter.MyViewHolder> {
        JSONArray jsonArray;

        public RecentPlacesAdapter(JSONArray array) {
            this.jsonArray = array;
        }

        @Override
        public RecentPlacesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.autocomplete_row, parent, false);
            return new RecentPlacesAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecentPlacesAdapter.MyViewHolder holder, int position) {
            String[] name = jsonArray.optJSONObject(position).optString("address").split(",");
            if (name.length > 0) {
                holder.name.setText(name[0]);
            } else {
                holder.name.setText(jsonArray.optJSONObject(position).optString("address"));
            }
            holder.location.setText(jsonArray.optJSONObject(position).optString("address"));

            holder.imgRecent.setImageResource(R.drawable.recent_search);

            holder.lnrLocation.setTag(position);

            holder.lnrLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = Integer.parseInt(view.getTag().toString());
                    AddToHomeWork(strTag, jsonArray.optJSONObject(pos).optString("latitude"), jsonArray.optJSONObject(pos).optString("longitude"),
                            jsonArray.optJSONObject(pos).optString("address"));
                }
            });
        }

        @Override
        public int getItemCount() {
            return jsonArray.length();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            MyBoldTextView name, location;

            LinearLayout lnrLocation;

            ImageView imgRecent;

            public MyViewHolder(View itemView) {
                super(itemView);
                name = (MyBoldTextView) itemView.findViewById(R.id.place_name);
                location = (MyBoldTextView) itemView.findViewById(R.id.place_detail);
                lnrLocation = (LinearLayout) itemView.findViewById(R.id.lnrLocation);
                imgRecent = (ImageView) itemView.findViewById(R.id.imgRecent);
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void getFavoriteLocations() {
        mApiInterface = RetrofitClient.getLiveTrackingClient().create(ApiInterface.class);

        Call<ResponseBody> call = mApiInterface.getFavoriteLocations("XMLHttpRequest",
                SharedHelper.getKey(AddHomeWorkActivity.this, "token_type") + " " + SharedHelper.getKey(AddHomeWorkActivity.this, "access_token"));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.e("sUCESS","SUCESS"+response.body());
                if (response.body() != null){
                    try {
                        String bodyString = new String(response.body().bytes());
                        Log.e("sUCESS","bodyString"+bodyString);
                        try {
                            JSONObject jsonObj = new JSONObject(bodyString);
                            JSONArray homeArray = jsonObj.optJSONArray("home");
                            JSONArray workArray = jsonObj.optJSONArray("work");
                            JSONArray othersArray = jsonObj.optJSONArray("others");
                            JSONArray recentArray = jsonObj.optJSONArray("recent");
                            if (homeArray.length() > 0){
                                Log.v("Home Address", ""+homeArray);
                                SharedHelper.putKey(AddHomeWorkActivity.this, "home", homeArray.optJSONObject(0).optString("address"));
                                SharedHelper.putKey(AddHomeWorkActivity.this, "home_lat", homeArray.optJSONObject(0).optString("latitude"));
                                SharedHelper.putKey(AddHomeWorkActivity.this, "home_lng", homeArray.optJSONObject(0).optString("longitude"));
                                SharedHelper.putKey(AddHomeWorkActivity.this, "home_id", homeArray.optJSONObject(0).optString("id"));
                            }
                            if (workArray.length() > 0){
                                Log.v("Work Address", ""+workArray);
                                SharedHelper.putKey(AddHomeWorkActivity.this, "work", workArray.optJSONObject(0).optString("address"));
                                SharedHelper.putKey(AddHomeWorkActivity.this, "work_lat", workArray.optJSONObject(0).optString("latitude"));
                                SharedHelper.putKey(AddHomeWorkActivity.this, "work_lng", workArray.optJSONObject(0).optString("longitude"));
                                SharedHelper.putKey(AddHomeWorkActivity.this, "work_id", workArray.optJSONObject(0).optString("id"));
                            }
                            if (othersArray.length() > 0){
                                Log.v("Others Address", ""+othersArray);
                            }
                            if (recentArray.length() > 0){
                                Log.v("Recent Address", ""+recentArray);
                                rvRecentResults.setVisibility(View.VISIBLE);
                                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                                rvRecentResults.setLayoutManager(mLayoutManager);
                                rvRecentResults.setItemAnimator(new DefaultItemAnimator());
                                RecentPlacesAdapter recentPlacesAdapter = new RecentPlacesAdapter(recentArray);
                                rvRecentResults.setAdapter(recentPlacesAdapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("onFailure","onFailure"+call.request().url());
            }
        });
    }


    private void AddToHomeWork(String strType, String strLatitude, String strLongitude, String strAddress) {
        mApiInterface = RetrofitClient.getLiveTrackingClient().create(ApiInterface.class);

        customDialog.show();

        Call<ResponseBody> call = mApiInterface.updateFavoriteLocations("XMLHttpRequest", SharedHelper.getKey(AddHomeWorkActivity.this, "token_type") + " " + SharedHelper.getKey(AddHomeWorkActivity.this, "access_token")
        , strType, strLatitude, strLongitude, strAddress);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.e("sUCESS","SUCESS"+response.body());
                customDialog.dismiss();
                if (response.body() != null){
                    try {
                        String bodyString = new String(response.body().bytes());
                        Log.e("sUCESS","bodyString"+bodyString);
                        setAddress();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("onFailure","onFailure"+call.request().url());
                customDialog.dismiss();
            }
        });
    }

    private void setGoogleAddress(Place place) {
        placePredictions.strDestAddress = place.getAddress();
        if (place.getLatLng() != null) {
            placePredictions.strDestLatLng = place.getLatLng().toString();
            placePredictions.strDestLatitude = place.getLatLng().latitude + "";
            placePredictions.strDestLongitude = place.getLatLng().longitude + "";
            AddToHomeWork(strTag, placePredictions.strDestLatitude,
                    placePredictions.strDestLongitude,
                    placePredictions.strDestAddress);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


}
