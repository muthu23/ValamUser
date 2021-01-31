package com.delivery.app.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.delivery.app.Adapter.LocationsAdapter;
import com.delivery.app.Helper.SharedHelper;
import com.delivery.app.Models.Locations;
import com.delivery.app.Models.PlacePredictions;
import com.delivery.app.R;

import java.util.ArrayList;

public class LocationAndGoodsActivity extends AppCompatActivity implements LocationsAdapter.LocationsListener {

    private static final String TAG = "LocationAndGoodsActivit";

    RecyclerView recyclerView;
    Button submitBtn;
    FloatingActionButton addFab;
    LocationsAdapter locationsAdapter;
    ArrayList<Locations> locationsArrayList = new ArrayList<>();
    ImageView backArrow;

    private Context context = LocationAndGoodsActivity.this;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST = 18945;

    Locations locations;
    public boolean isPickup;
    String dAddress;
    private String sAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_and_goods);
        findViewsById();
        sAddress = getIntent().getStringExtra("s_address");
        locationsArrayList.add(getLocation());
        setupRecyclerView();
        submitBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            for (int i = 0; i < locationsArrayList.size(); i++) {
                if (locationsArrayList.get(i).getGoods() == null) {
                    Toast.makeText(context, context.getResources().getString(R.string.empty_goods), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (locationsArrayList.get(i).getdAddress() == null) {
                    Toast.makeText(context, context.getResources().getString(R.string.empty_dest), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (locationsArrayList.get(0) != null) {
                int i = 1;
                for (Locations locations : locationsArrayList) {
                    intent.putExtra("Location Address" + i + "", locations);
                    i++;
                }
                intent.putExtra("Location size", locationsArrayList.size());
                Log.e(TAG, "onClick: ", locationsArrayList.get(0));
                intent.putExtra("pick_lo" +
                        "cation", "no");
                setResult(RESULT_OK, intent);
            } else {
                setResult(RESULT_CANCELED, intent);
            }
            finish();
        });
        backArrow.setOnClickListener(v -> finish());
        addFab.setOnClickListener(v -> {
            if (locationsAdapter.getItemCount() < 3) {
                locationsArrayList.add(getLocation());
                refreshAdapter();
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.can_not_add_loc), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void refreshAdapter() {
        locationsAdapter.setListModels(locationsArrayList);
        locationsAdapter.notifyDataSetChanged();
    }

    private Locations getLocation() {
        Locations locations = new Locations();
        locations.setsLatitude(SharedHelper.getKey(context, "curr_lat"));
        locations.setsLongitude(SharedHelper.getKey(context, "curr_lng"));
        locations.setsAddress(sAddress);
        locations.setdAddress(dAddress);
        locations.setdLatitude(null);
        locations.setdLongitude(null);
        return locations;
    }

    private void setupRecyclerView() {
        locationsAdapter = new LocationsAdapter(locationsArrayList, context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        locationsAdapter.setLocationsListener(this);
        recyclerView.setAdapter(locationsAdapter);
    }

    private void findViewsById() {
        recyclerView = findViewById(R.id.recyclerView);
        submitBtn = findViewById(R.id.submit_btn);
        addFab = findViewById(R.id.add_fab);
        backArrow = findViewById(R.id.backArrow);
    }

    @Override
    public void onCloseClick(Locations locations) {
        locationsArrayList.remove(locations);
        locationsAdapter.setListModels(locationsArrayList);
        locationsAdapter.notifyDataSetChanged();
    }

    public void goToSearch(String mvalue) {
        Intent intent = new Intent(this, CustomGooglePlacesSearch.class);
        intent.putExtra("cursor", mvalue);
        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST);
    }

    @Override
    public void onSrcClick(Locations locations) {
        isPickup = true;
        this.locations = locations;
        goToSearch("source");
    }

    @Override
    public void onDestClick(Locations locations) {
        isPickup = false;
        this.locations = locations;
        goToSearch("destination");
    }

    @Override
    public void onGoodsClick(Locations locations) {
        this.locations = locations;
        refreshAdapter();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode && data.getSerializableExtra("Location Address") != null) {
            PlacePredictions placePredictions = (PlacePredictions) data.getSerializableExtra("Location Address");
            Log.e(TAG, "onActivityResult: " + placePredictions.toString());
            if (placePredictions.strDestLatitude != null && placePredictions.strDestLongitude != null) {
                if (!placePredictions.strDestLongitude.equalsIgnoreCase(locations.getsLongitude()) &&
                        !placePredictions.strDestLatitude.equalsIgnoreCase(locations.getsLatitude())) {
                    showCurrentLocation(placePredictions);
                } else {
                   /* Toast.makeText(context, context.getString(R.string.src_and_dest_same_loc), Toast.LENGTH_SHORT).show();
                    goToSearch();*/
                    showCurrentLocation(placePredictions);
                }
            }
        }
    }

    private void showCurrentLocation(PlacePredictions placePredictions) {
        if (isPickup) {
            for (Locations loc : locationsArrayList) {
                loc.setsLatitude(placePredictions.strDestLatitude);
                loc.setsLongitude(placePredictions.strDestLongitude);
                loc.setsAddress(placePredictions.strDestAddress);
            }
            SharedHelper.putKey(context, "curr_lat", placePredictions.strDestLatitude + "");
            SharedHelper.putKey(context, "curr_lng", placePredictions.strDestLongitude + "");
            dAddress = placePredictions.strDestAddress;
            refreshAdapter();
        } else {
            locations.setdLatitude(placePredictions.strDestLatitude);
            locations.setdLongitude(placePredictions.strDestLongitude);
            locations.setdAddress(placePredictions.strDestAddress);
            refreshAdapter();
        }
    }
}