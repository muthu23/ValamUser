package com.delivery.app.Fragments;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.koushikdutta.ion.Ion;
import com.delivery.app.Activities.ChatActivity;
import com.delivery.app.Activities.CustomGooglePlacesSearch;
import com.delivery.app.Activities.HistoryActivity;
import com.delivery.app.Activities.LocationAndGoodsActivity;
import com.delivery.app.Activities.ShowProfile;
import com.delivery.app.Adapter.DropListAdapter;
import com.delivery.app.Adapter.StatusAdapter;
import com.delivery.app.Helper.ConnectionHelper;
import com.delivery.app.Helper.CustomDialog;
import com.delivery.app.Helper.DataParser;
import com.delivery.app.Helper.SharedHelper;
import com.delivery.app.Helper.URLHelper;
import com.delivery.app.Helper.VolleyMultipartRequest;
import com.delivery.app.Models.CardInfo;
import com.delivery.app.Models.Driver;
import com.delivery.app.Models.Locations;
import com.delivery.app.Models.TripStatus;
import com.delivery.app.MyApplication;
import com.delivery.app.R;
import com.delivery.app.Retrofit.ApiInterface;
import com.delivery.app.Retrofit.ResponseListener;
import com.delivery.app.Retrofit.RetrofitClient;
import com.delivery.app.Utils.MapAnimator;
import com.delivery.app.Utils.MyBoldTextView;
import com.delivery.app.Utils.MyButton;
import com.delivery.app.Utils.MyTextView;
import com.delivery.app.Utils.Utilities;
import com.squareup.picasso.Picasso;
import com.delivery.app.Activities.Payment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class HomeFragment extends Fragment implements OnMapReadyCallback, LocationListener,
        GoogleMap.OnMarkerDragListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResponseListener, GoogleMap.OnCameraMoveListener, StatusAdapter.StatuAdapterListner {

    private static final String TAG = "HomeFragment";
    private static final int REQUEST_LOCATION = 1450;
    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();
    private final int ADD_CARD_CODE = 435;
    private final int RC_WEB_PAYMENT = 500;
    public String PreviousStatus = "";
    public String CurrentStatus = "";
    StatusAdapter statusAdapter;
    Activity activity;
    Context context;
    View rootView;
    HomeFragmentListener listener;
    double wallet_balance;
    LayoutInflater inflater;
    AlertDialog reasonDialog;
    AlertDialog cancelRideDialog;
    String is_track = "";
    String strTimeTaken = "";
    ImageView backArrow, status_info;
    LinearLayout status;
    Button btnHome, btnWork;
    String isPaid = "", paymentMode = "";
    int totalRideAmount = 0, walletAmountDetected = 0, couponAmountDetected = 0;
    int flowValue = 0;
    DrawerLayout drawer;
    int NAV_DRAWER = 0;
    String reqStatus = "";
    int PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST = 18945;
    int LOCATION = 180;
    String feedBackRating;
    ArrayList<Locations> location_array = new ArrayList<>();
    double height;
    double width;
    Handler handleCheckStatus;
    String strPickLocation = "", strTag = "", strPickType = "";
    boolean once = true;
    int click = 1;
    boolean afterToday = false;
    boolean pick_first = true;
    Driver driver;
    //        <!-- Map frame -->
    LinearLayout mapLayout;
    SupportMapFragment mapFragment;
    GoogleMap mMap;
    int value;
    Marker marker;
    Double latitude, longitude;
    GoogleApiClient mGoogleApiClient;
    //        <!-- Source and Destination Layout-->
    LinearLayout sourceAndDestinationLayout;
    FrameLayout frmDestination;
    MyBoldTextView destination;
    ImageView imgMenu, mapfocus, imgBack, shadowBack;
    RecyclerView rvDesAddress;
    View tripLine;
    ImageView destinationBorderImg;
    TextView frmSource, frmDest, txtChange, source_address_txt;
    CardView srcDestLayout;
    LinearLayout lnrRequestProviders;
    RecyclerView rcvServiceTypes, recyclerView_tripstatus;
    ArrayList<TripStatus> statusArrayList = new ArrayList<>();
    ImageView imgPaymentType;
    JSONArray userdrop;
    JSONArray previousDrop = new JSONArray();
    //       <!--1. Request to providers -->
    ImageView imgSos;
    ImageView chat;
    ImageView imgShareRide;
    MyBoldTextView lblPaymentType, lblPaymentChange, booking_id;
    MyButton btnRequestRides;
    String scheduledDate = "";
    String scheduledTime = "";
    String cancalReason = "";
    LinearLayout lnrHidePopup, lnrProviderPopup, lnrPriceBase, lnrPricemin, lnrPricekm;
    RelativeLayout lnrSearchAnimation;
    ImageView imgProviderPopup;
    MyBoldTextView lblPriceMin, lblBasePricePopup, lblCapacity, lblServiceName, lblPriceKm, lblCalculationType, lblProviderDesc;
    //        <!--1. Driver Details-->
    MyButton btnDonePopup;
    LinearLayout lnrApproximate;
    MyButton btnRequestRideConfirm;
    MyButton imgSchedule;
    CheckBox chkWallet;
    //         <!--2. Approximate Rate ...-->
    MyBoldTextView lblEta;
    MyBoldTextView lblType;
    MyBoldTextView lblHelper;
    MyBoldTextView lblApproxAmount, surgeDiscount, surgeTxt;
    View lineView;
    LinearLayout ScheduleLayout;
    MyBoldTextView scheduleDate;
    MyBoldTextView scheduleTime;
    MyButton scheduleBtn;
    DatePickerDialog datePickerDialog;
    LocationRequest mLocationRequest;
    RelativeLayout lnrWaitingForProviders;
    MyBoldTextView lblNoMatch;
    ImageView imgCenter;
    MyButton btnCancelRide;
    //         <!--3. Waiting For Providers ...-->
    LinearLayout lnrProviderAccepted, lnrAfterAcceptedStatus, AfterAcceptButtonLayout;
    ImageView imgProvider, imgServiceRequested;
    MyBoldTextView lblProvider, lblStatus, lblServiceRequested, lblModelNumber, lblSurgePrice, otpTxt;
    RatingBar ratingProvider;
    MyButton btnCall, btnCancelTrip;
    LinearLayout lnrInvoice;
    MyBoldTextView lblBasePrice, lblDistanceCovered, lblExtraPrice, lblTimeTaken, lblDistancePrice,
            lblCommision, lblTaxPrice, lblTotalPrice, lblPaymentTypeInvoice, lblPaymentChangeInvoice,
            lblDiscountPrice, lblWalletPrice;
    //         <!--4. Driver Accepted ...-->
    ImageView imgPaymentTypeInvoice;
    MyButton btnPayNow;
    MyButton btnPaymentDoneBtn;
    LinearLayout discountDetectionLayout, walletDetectionLayout;
    LinearLayout bookingIDLayout;
    //          <!--5. Invoice Layout ...-->
    Locations location;
    LinearLayout lnrRateProvider;
    MyBoldTextView lblProviderNameRate;
    ImageView imgProviderRate;
    RatingBar ratingProviderRate;
    EditText txtCommentsRate;
    Button btnSubmitReview;
    //          <!--6. Rate provider Layout ...-->
    RelativeLayout rtlStaticMarker;
    ImageView imgDestination, imgGotoPhoto;
    MyButton btnDone;
    CameraPosition cmPosition;
    String current_lat = "", current_lng = "", current_address = "", source_lat = "", source_lng = "", source_address = "",
            dest_lat = "", dest_lng = "", dest_address = "", extend_dest_lat = "", extend_dest_lng = "";
    //Internet
    ConnectionHelper helper;
    Boolean isInternet;
    //            <!-- Static marker-->
    //RecylerView
    int currentPostion = 0;
    CustomDialog customDialog;
    //MArkers
    ArrayList<Marker> lstProviderMarkers = new ArrayList<>();
    AlertDialog alert;
    //Animation
    Animation slide_down, slide_up, slide_up_top, slide_up_down;
    ParserTask parserTask;
    FetchUrl fetchUrl;
    String notificationTxt;
    LinearLayout lnrHomeWork, lnrHome, lnrWork;
    private String choosedHour = "";
    private ArrayList<CardInfo> cardInfoArrayList = new ArrayList<>();
    private boolean mIsShowing;
    private boolean mIsHiding;
    private LatLng sourceLatLng;
    private LatLng destLatLng;
    private Marker sourceMarker;
    private Marker destinationMarker;
    private Marker providerMarker;
    private HashMap<Integer, Marker> mHashMap = new HashMap<>();
    private String userId, providerId;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCameraMove() {
        Utilities.print("Current marker", "Zoom Level " + mMap.getCameraPosition().zoom);
        cmPosition = mMap.getCameraPosition();
        if (marker != null) {
            if (!mMap.getProjection().getVisibleRegion().latLngBounds.contains(marker.getPosition())) {
                Utilities.print("Current marker", "Current Marker is not visible");
                if (mapfocus.getVisibility() == View.INVISIBLE) {
                    mapfocus.setVisibility(View.VISIBLE);
                }
            } else {
                Utilities.print("Current marker", "Current Marker is visible");
                if (mapfocus.getVisibility() == View.VISIBLE) {
                    mapfocus.setVisibility(View.INVISIBLE);
                }
                if (mMap.getCameraPosition().zoom < 14.0f) {
                    if (mapfocus.getVisibility() == View.INVISIBLE) {
                        mapfocus.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            notificationTxt = bundle.getString("Notification");
            Log.e("HomeFragment", "onCreate : Notification" + notificationTxt);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_home, container, false);
        }
        this.inflater = inflater;
        customDialog = new CustomDialog(context);
        customDialog.show();
        new Handler().postDelayed(() -> {
            init(rootView);
            //permission to access location
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Android M Permission check
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                initMap();
                MapsInitializer.initialize(getActivity());
            }
        }, 500);
        reqStatus = SharedHelper.getKey(context, "req_status");
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        try {
            listener = (HomeFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement HomeFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void init(View rootView) {
        helper = new ConnectionHelper(context);
        isInternet = helper.isConnectingToInternet();
        statusCheck();
//        <!-- Map frame -->
        mapLayout = rootView.findViewById(R.id.mapLayout);
        drawer = rootView.findViewById(R.id.drawer_layout);
        drawer = activity.findViewById(R.id.drawer_layout);
        backArrow = rootView.findViewById(R.id.backArrow);
        status_info = rootView.findViewById(R.id.status_info);
        status = rootView.findViewById(R.id.status);
//        <!-- Source and Destination Layout-->
        sourceAndDestinationLayout = rootView.findViewById(R.id.sourceAndDestinationLayout);
        srcDestLayout = rootView.findViewById(R.id.sourceDestLayout);
        frmSource = rootView.findViewById(R.id.frmSource);
        frmDest = rootView.findViewById(R.id.frmDest);
        source_address_txt = rootView.findViewById(R.id.source_address_txt);
        rvDesAddress = rootView.findViewById(R.id.rvDesAddress);
        txtChange = rootView.findViewById(R.id.txtChange);
        frmDestination = rootView.findViewById(R.id.frmDestination);
        destination = rootView.findViewById(R.id.destination);
        imgMenu = rootView.findViewById(R.id.imgMenu);
        imgSos = rootView.findViewById(R.id.imgSos);
        chat = rootView.findViewById(R.id.chat);
        imgShareRide = rootView.findViewById(R.id.imgShareRide);
        mapfocus = rootView.findViewById(R.id.mapfocus);
        imgBack = rootView.findViewById(R.id.imgBack);
        shadowBack = rootView.findViewById(R.id.shadowBack);
        tripLine = rootView.findViewById(R.id.trip_line);
        destinationBorderImg = rootView.findViewById(R.id.dest_border_img);
//        <!-- Request to providers-->
        lnrRequestProviders = rootView.findViewById(R.id.lnrRequestProviders);
        rcvServiceTypes = rootView.findViewById(R.id.rcvServiceTypes);
        recyclerView_tripstatus = rootView.findViewById(R.id.recyclerView);
        imgPaymentType = rootView.findViewById(R.id.imgPaymentType);
        lblPaymentType = rootView.findViewById(R.id.lblPaymentType);
        lblPaymentChange = rootView.findViewById(R.id.lblPaymentChange);
        booking_id = rootView.findViewById(R.id.booking_id);
        btnRequestRides = rootView.findViewById(R.id.btnRequestRides);
//        <!--  Driver and service type Details-->
        lnrSearchAnimation = rootView.findViewById(R.id.lnrSearch);
        lnrProviderPopup = rootView.findViewById(R.id.lnrProviderPopup);
        lnrPriceBase = rootView.findViewById(R.id.lnrPriceBase);
        lnrPricekm = rootView.findViewById(R.id.lnrPricekm);
        lnrPricemin = rootView.findViewById(R.id.lnrPricemin);
        lnrHidePopup = rootView.findViewById(R.id.lnrHidePopup);
        imgProviderPopup = rootView.findViewById(R.id.imgProviderPopup);
        lblServiceName = rootView.findViewById(R.id.lblServiceName);
        lblCapacity = rootView.findViewById(R.id.lblCapacity);
        lblPriceKm = rootView.findViewById(R.id.lblPriceKm);
        lblPriceMin = rootView.findViewById(R.id.lblPriceMin);
        lblCalculationType = rootView.findViewById(R.id.lblCalculationType);
        lblBasePricePopup = rootView.findViewById(R.id.lblBasePricePopup);
        lblDistanceCovered = rootView.findViewById(R.id.lblDistanceCovered);
        lblProviderDesc = rootView.findViewById(R.id.lblProviderDesc);
        btnDonePopup = rootView.findViewById(R.id.btnDonePopup);
//         <!--2. Approximate Rate ...-->
        lnrApproximate = rootView.findViewById(R.id.lnrApproximate);
        imgSchedule = rootView.findViewById(R.id.imgSchedule);
        chkWallet = rootView.findViewById(R.id.chkWallet);
        lblEta = rootView.findViewById(R.id.lblEta);
        lblType = rootView.findViewById(R.id.lblType);
        lblHelper = rootView.findViewById(R.id.lblHelper);
        lblApproxAmount = rootView.findViewById(R.id.lblApproxAmount);
        surgeDiscount = rootView.findViewById(R.id.surgeDiscount);
        surgeTxt = rootView.findViewById(R.id.surge_txt);
        btnRequestRideConfirm = rootView.findViewById(R.id.btnRequestRideConfirm);
        lineView = rootView.findViewById(R.id.lineView);
        //Schedule Layout
        ScheduleLayout = rootView.findViewById(R.id.ScheduleLayout);
        scheduleDate = rootView.findViewById(R.id.scheduleDate);
        scheduleTime = rootView.findViewById(R.id.scheduleTime);
        scheduleBtn = rootView.findViewById(R.id.scheduleBtn);
//         <!--3. Waiting For Providers ...-->
        lnrWaitingForProviders = rootView.findViewById(R.id.lnrWaitingForProviders);
        lblNoMatch = rootView.findViewById(R.id.lblNoMatch);
        //imgCenter = rootView.findViewById(R.id.imgCenter);
        btnCancelRide = rootView.findViewById(R.id.btnCancelRide);
//          <!--4. Driver Accepted ...-->
        lnrProviderAccepted = rootView.findViewById(R.id.lnrProviderAccepted);
        lnrAfterAcceptedStatus = rootView.findViewById(R.id.lnrAfterAcceptedStatus);
        AfterAcceptButtonLayout = rootView.findViewById(R.id.AfterAcceptButtonLayout);
        imgProvider = rootView.findViewById(R.id.imgProvider);
        imgServiceRequested = rootView.findViewById(R.id.imgServiceRequested);
        lblProvider = rootView.findViewById(R.id.lblProvider);
        lblStatus = rootView.findViewById(R.id.lblStatus);
        lblSurgePrice = rootView.findViewById(R.id.lblSurgePrice);
        lblServiceRequested = rootView.findViewById(R.id.lblServiceRequested);
        lblModelNumber = rootView.findViewById(R.id.lblModelNumber);
        otpTxt = rootView.findViewById(R.id.otp_txt);
        ratingProvider = rootView.findViewById(R.id.ratingProvider);
        btnCall = rootView.findViewById(R.id.btnCall);
        btnCancelTrip = rootView.findViewById(R.id.btnCancelTrip);
//           <!--5. Invoice Layout ...-->
        lnrInvoice = rootView.findViewById(R.id.lnrInvoice);
        lblBasePrice = rootView.findViewById(R.id.lblBasePrice);
        lblExtraPrice = rootView.findViewById(R.id.lblExtraPrice);
        lblDistancePrice = rootView.findViewById(R.id.lblDistancePrice);
        lblTimeTaken = rootView.findViewById(R.id.lblTimeTaken);
        //lblCommision = (MyBoldTextView) rootView.findViewById(R.id.lblCommision);
        lblTaxPrice = rootView.findViewById(R.id.lblTaxPrice);
        lblTotalPrice = rootView.findViewById(R.id.lblTotalPrice);
        lblPaymentTypeInvoice = rootView.findViewById(R.id.lblPaymentTypeInvoice);
        imgPaymentTypeInvoice = rootView.findViewById(R.id.imgPaymentTypeInvoice);
        btnPayNow = rootView.findViewById(R.id.btnPayNow);
        btnPaymentDoneBtn = rootView.findViewById(R.id.btnPaymentDoneBtn);
        bookingIDLayout = rootView.findViewById(R.id.bookingIDLayout);
        walletDetectionLayout = rootView.findViewById(R.id.walletDetectionLayout);
        discountDetectionLayout = rootView.findViewById(R.id.discountDetectionLayout);
        lblWalletPrice = rootView.findViewById(R.id.lblWalletPrice);
        lblDiscountPrice = rootView.findViewById(R.id.lblDiscountPrice);
//          <!--6. Rate provider Layout ...-->
        lnrHomeWork = rootView.findViewById(R.id.lnrHomeWork);
        lnrHome = rootView.findViewById(R.id.lnrHome);
        lnrWork = rootView.findViewById(R.id.lnrWork);
        lnrRateProvider = rootView.findViewById(R.id.lnrRateProvider);
        lblProviderNameRate = rootView.findViewById(R.id.lblProviderName);
        imgProviderRate = rootView.findViewById(R.id.imgProviderRate);
        txtCommentsRate = rootView.findViewById(R.id.txtComments);
        ratingProviderRate = rootView.findViewById(R.id.ratingProviderRate);
        btnSubmitReview = (MyButton) rootView.findViewById(R.id.btnSubmitReview);
//            <!--Static marker-->
        rtlStaticMarker = rootView.findViewById(R.id.rtlStaticMarker);
        imgDestination = rootView.findViewById(R.id.imgDestination);
        imgGotoPhoto = rootView.findViewById(R.id.imgGotoPhoto);
        btnDone = rootView.findViewById(R.id.btnDone);
        btnHome = rootView.findViewById(R.id.btnHome);
        btnWork = rootView.findViewById(R.id.btnWork);
        getCards();
        checkStatus();
        handleCheckStatus = new Handler();
        //check status every 3 sec
        handleCheckStatus.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (helper.isConnectingToInternet()) {
                    if (!isAdded()) {
                        return;
                    }
                    checkStatus();
                    Utilities.print("Handler", "Called");
                    if (alert != null && alert.isShowing()) {
                        alert.dismiss();
                        alert = null;
                    }
                } else {
                    showDialog();
                }
                handleCheckStatus.postDelayed(this, 3000);
            }
        }, 3000);
        backArrow.setOnClickListener(new OnClick());
        status_info.setOnClickListener(new OnClick());
        btnRequestRides.setOnClickListener(new OnClick());
        btnDonePopup.setOnClickListener(new OnClick());
        lnrHidePopup.setOnClickListener(new OnClick());
        btnRequestRideConfirm.setOnClickListener(new OnClick());
        btnCancelRide.setOnClickListener(new OnClick());
        btnCancelTrip.setOnClickListener(new OnClick());
        btnCall.setOnClickListener(new OnClick());
        btnPayNow.setOnClickListener(new OnClick());
        btnPaymentDoneBtn.setOnClickListener(new OnClick());
        btnSubmitReview.setOnClickListener(new OnClick());
        btnHome.setOnClickListener(new OnClick());
        btnWork.setOnClickListener(new OnClick());
        btnDone.setOnClickListener(new OnClick());
        frmDestination.setOnClickListener(new OnClick());
        frmDest.setOnClickListener(new OnClick());
        lblPaymentChange.setOnClickListener(new OnClick());
        frmSource.setOnClickListener(new OnClick());
        txtChange.setOnClickListener(new OnClick());
        imgMenu.setOnClickListener(new OnClick());
        mapfocus.setOnClickListener(new OnClick());
        imgSchedule.setOnClickListener(new OnClick());
        imgBack.setOnClickListener(new OnClick());
        scheduleBtn.setOnClickListener(new OnClick());
        scheduleDate.setOnClickListener(new OnClick());
        scheduleTime.setOnClickListener(new OnClick());
        imgProvider.setOnClickListener(new OnClick());
        imgProviderRate.setOnClickListener(new OnClick());
        imgSos.setOnClickListener(new OnClick());
        chat.setOnClickListener(new OnClick());
        imgShareRide.setOnClickListener(new OnClick());
        lnrRequestProviders.setOnClickListener(new OnClick());
        lnrProviderPopup.setOnClickListener(new OnClick());
        ScheduleLayout.setOnClickListener(new OnClick());
        lnrApproximate.setOnClickListener(new OnClick());
        lnrProviderAccepted.setOnClickListener(new OnClick());
        lnrInvoice.setOnClickListener(new OnClick());
        lnrRateProvider.setOnClickListener(new OnClick());
        lnrWaitingForProviders.setOnClickListener(new OnClick());
        imgGotoPhoto.setOnClickListener(new OnClick());
        flowValue = 0;
        layoutChanges();
        //Load animation
        slide_down = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
        slide_up = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
        slide_up_top = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up_top);
        slide_up_down = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up_down);
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN)
                return true;
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (!reqStatus.equalsIgnoreCase("SEARCHING")) {
                    Utilities.print("", "Back key pressed!");
                    if (lnrRequestProviders.getVisibility() == View.VISIBLE) {
                        flowValue = 0;
                        if (!current_lat.equalsIgnoreCase("") && !current_lng.equalsIgnoreCase("")) {
                            LatLng myLocation = new LatLng(Double.parseDouble(current_lat), Double.parseDouble(current_lng));
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(14).build();
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    } else if (lnrApproximate.getVisibility() == View.VISIBLE) {
                        flowValue = 1;
                    } else if (lnrWaitingForProviders.getVisibility() == View.VISIBLE) {
                        flowValue = 2;
                    } else if (ScheduleLayout.getVisibility() == View.VISIBLE) {
                        flowValue = 2;
                    } else if (status.getVisibility() == View.VISIBLE) {
                        status.startAnimation(slide_down);
                        slide_down.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                if (status.getVisibility() == View.VISIBLE)
                                    status.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        return true;
                    } else {
                        getActivity().finish();
                    }
                    layoutChanges();
                    return true;
                }
            }
            return false;
        });
    }

    @SuppressWarnings("MissingPermission")
    void initMap() {
        if (mMap == null) {
            FragmentManager fm = getChildFragmentManager();
            mapFragment = ((SupportMapFragment) fm.findFragmentById(R.id.provider_map));
            mapFragment.getMapAsync(this);
        }
        if (mMap != null) {
            setupMap();
        }
    }

    @SuppressWarnings("MissingPermission")
    void setupMap() {
        if (mMap != null) {
//            mMap.setMyLocationEnabled(true);
//            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.setBuildingsEnabled(true);
            mMap.setMyLocationEnabled(false);
            mMap.setOnMarkerDragListener(this);
            mMap.setOnCameraMoveListener(this);
            mMap.getUiSettings().setRotateGesturesEnabled(false);
            mMap.getUiSettings().setTiltGesturesEnabled(false);
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    // Getting view from the layout file infowindowlayout.xml
                    View v = activity.getLayoutInflater().inflate(R.layout.info_window, null);
                    TextView lblAddress = v.findViewById(R.id.lblAddress);
                    TextView lblTime = v.findViewById(R.id.txtTime);
                    lblAddress.setText(marker.getSnippet());
                    if (strTimeTaken.length() > 0) {
                        lblTime.setText(strTimeTaken);
                    }
                    if (marker.getTitle() == null) {
                        return null;
                    }
                    if (marker.getTitle().equalsIgnoreCase("source") || marker.getTitle().equalsIgnoreCase("destination")) {
                        return v;
                    } else {
                        return null;
                    }
                }

                @Override
                public View getInfoContents(Marker marker) {
                    return null;
                }
            });
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            if (marker != null) {
                marker.remove();
            }
            if (location != null && location.getLatitude() != 0 && location.getLongitude() != 0) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .anchor(0.5f, 0.75f)
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location));
                marker = mMap.addMarker(markerOptions);
                // mMap is GoogleMap object, latLng is the location on map from which ripple should start
                Log.e("MAP", "onLocationChanged: 1 " + location.getLatitude());
                Log.e("MAP", "onLocationChanged: 2 " + location.getLongitude());
                current_lat = "" + location.getLatitude();
                current_lng = "" + location.getLongitude();
                if (source_lat.equalsIgnoreCase("") || source_lat.length() < 0) {
                    source_lat = current_lat;
                }
                if (source_lng.equalsIgnoreCase("") || source_lng.length() < 0) {
                    source_lng = current_lng;
                }
                if (value == 0) {
                    LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(14).build();
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    mMap.setPadding(0, 0, 0, 0);
                    mMap.getUiSettings().setZoomControlsEnabled(false);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    mMap.getUiSettings().setMapToolbarEnabled(false);
                    mMap.getUiSettings().setCompassEnabled(false);
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    SharedHelper.putKey(context, "curr_lat", latitude + "");
                    SharedHelper.putKey(context, "curr_lng", longitude + "");
                    Utilities.getAddressUsingLatLng("source", frmSource, context, "" + latitude, "" + longitude);
//                    getAddressUsingLatLng(context, ""+latitude, ""+longitude);
                    source_lat = "" + latitude;
                    source_lng = "" + longitude;
//                    frmSource.setText(currentAddress);
                    value++;
                    if ((customDialog != null) && (customDialog.isShowing())) {
                        customDialog.dismiss();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void navigateToShareScreen(String shareUrl) {
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            String name = SharedHelper.getKey(context, "first_name") + " " + SharedHelper.getKey(context, "last_name");
            sendIntent.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.app_name) + "Mr/Mrs." + name + " would like to share a ride with you at " +
                    shareUrl + current_lat + "," + current_lng);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Share applications not found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSosPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(context.getResources().getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(context.getResources().getString(R.string.emaergeny_call))
                .setCancelable(false);
        builder.setPositiveButton(context.getResources().getString(R.string.yes), (dialog, which) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 3);
            } else {
                Intent intentCall = new Intent(Intent.ACTION_CALL);
                intentCall.setData(Uri.parse("tel:" + SharedHelper.getKey(context, "sos")));
                startActivity(intentCall);
            }
        });
        builder.setNegativeButton(context.getResources().getString(R.string.no), (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showCancelRideDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder.setTitle(context.getResources().getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(context.getResources().getString(R.string.cancel_ride_alert));
        builder.setCancelable(false);
        builder.setPositiveButton(context.getResources().getString(R.string.yes), (dialog, which) -> showreasonDialog());
        builder.setNegativeButton(context.getResources().getString(R.string.no), (dialog, which) -> dialog.dismiss());
        cancelRideDialog = builder.create();
        cancelRideDialog.show();
    }

    private void showreasonDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.cancel_dialog, null);
        final EditText reasonEtxt = view.findViewById(R.id.reason_etxt);
        Button submitBtn = view.findViewById(R.id.submit_btn);
        builder.setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setView(view)
                .setCancelable(true);
        reasonDialog = builder.create();
        submitBtn.setOnClickListener(v -> {
            cancalReason = reasonEtxt.getText().toString();
            cancelRequest();
            reasonDialog.dismiss();
        });
        reasonDialog.show();
    }

    void layoutChanges() {
        try {
            Utilities.hideKeypad(getActivity(), getActivity().getCurrentFocus());
            if (lnrApproximate.getVisibility() == View.VISIBLE) {
                lnrApproximate.startAnimation(slide_down);
            } else if (ScheduleLayout.getVisibility() == View.VISIBLE) {
                ScheduleLayout.startAnimation(slide_down);
            } else if (lnrRequestProviders.getVisibility() == View.VISIBLE) {
                lnrRequestProviders.startAnimation(slide_down);
            } else if (lnrProviderPopup.getVisibility() == View.VISIBLE) {
                lnrProviderPopup.startAnimation(slide_down);
                lnrSearchAnimation.startAnimation(slide_up_down);
                lnrSearchAnimation.setVisibility(View.VISIBLE);
            } else if (lnrInvoice.getVisibility() == View.VISIBLE) {
                lnrInvoice.startAnimation(slide_down);
            } else if (lnrRateProvider.getVisibility() == View.VISIBLE) {
                lnrRateProvider.startAnimation(slide_down);
            } else if (lnrInvoice.getVisibility() == View.VISIBLE) {
                lnrInvoice.startAnimation(slide_down);
            }
            lnrRequestProviders.setVisibility(View.GONE);
            lnrProviderPopup.setVisibility(View.GONE);
            lnrApproximate.setVisibility(View.GONE);
            lnrWaitingForProviders.setVisibility(View.GONE);
            lnrProviderAccepted.setVisibility(View.GONE);
            srcDestLayout.setVisibility(View.GONE);
            lnrInvoice.setVisibility(View.GONE);
            lnrRateProvider.setVisibility(View.GONE);
            lnrHomeWork.setVisibility(View.GONE);
            ScheduleLayout.setVisibility(View.GONE);
            rtlStaticMarker.setVisibility(View.GONE);
            frmDestination.setVisibility(View.GONE);
            imgBack.setVisibility(View.GONE);
            shadowBack.setVisibility(View.GONE);
            txtCommentsRate.setText("");
            scheduleDate.setText(context.getResources().getString(R.string.sample_date));
            scheduleTime.setText(context.getResources().getString(R.string.sample_time));
            if (flowValue == 0) {
                srcDestLayout.setVisibility(View.GONE);
                txtChange.setVisibility(View.GONE);
                frmSource.setOnClickListener(new OnClick());
                frmDest.setOnClickListener(new OnClick());
                srcDestLayout.setOnClickListener(null);
                if (mMap != null) {
                    mMap.clear();
                    stopAnim();
                    setupMap();
                }
                setCurrentAddress();
                if (!SharedHelper.getKey(context, "home").equalsIgnoreCase("")) {
                    lnrHome.setVisibility(View.VISIBLE);
                } else {
                    lnrHome.setVisibility(View.GONE);
                }
                if (!SharedHelper.getKey(context, "work").equalsIgnoreCase("")) {
                    lnrWork.setVisibility(View.VISIBLE);
                } else {
                    lnrWork.setVisibility(View.GONE);
                }
                if (lnrHome.getVisibility() == View.GONE && lnrWork.getVisibility() == View.GONE) {
                    lnrHomeWork.setVisibility(View.GONE);
                } else {
                    lnrHomeWork.setVisibility(View.VISIBLE);
                }
                frmDestination.setVisibility(View.VISIBLE);
                imgMenu.setVisibility(View.VISIBLE);
                destination.setText("");
                destination.setHint(context.getResources().getString(R.string.where_to_go));
                frmDest.setText("");
                dest_address = "";
                dest_lat = "";
                dest_lng = "";
                source_lat = "" + current_lat;
                source_lng = "" + current_lng;
                source_address = "" + current_address;
                sourceAndDestinationLayout.setVisibility(View.VISIBLE);
                getProvidersList("");
            } else if (flowValue == 1) {
                frmSource.setVisibility(View.VISIBLE);
                destinationBorderImg.setVisibility(View.GONE);
                frmDestination.setVisibility(View.VISIBLE);
                imgBack.setVisibility(View.VISIBLE);
                lnrRequestProviders.startAnimation(slide_up);
                lnrRequestProviders.setVisibility(View.VISIBLE);
                srcDestLayout.setVisibility(View.VISIBLE);
                imgMenu.setVisibility(View.GONE);
                if (!Double.isNaN(wallet_balance) && wallet_balance > 0) {
                    if (lineView != null && chkWallet != null) {
                        lineView.setVisibility(View.VISIBLE);
                        chkWallet.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (lineView != null && chkWallet != null) {
                        lineView.setVisibility(View.GONE);
                        chkWallet.setVisibility(View.GONE);
                    }
                }
                chkWallet.setChecked(false);
                if (sourceMarker != null && destinationMarker != null) {
                    sourceMarker.setDraggable(true);
                    destinationMarker.setDraggable(true);
                }
            } else if (flowValue == 2) {
                imgBack.setVisibility(View.VISIBLE);
                imgMenu.setVisibility(View.GONE);
                chkWallet.setChecked(false);
                lnrApproximate.startAnimation(slide_up);
                lnrApproximate.setVisibility(View.VISIBLE);
                if (sourceMarker != null && destinationMarker != null) {
                    sourceMarker.setDraggable(false);
                    destinationMarker.setDraggable(false);
                }
            } else if (flowValue == 3) {
                imgBack.setVisibility(View.VISIBLE);
                imgMenu.setVisibility(View.GONE);
                lnrWaitingForProviders.setVisibility(View.VISIBLE);
                srcDestLayout.setVisibility(View.GONE);
                //sourceAndDestinationLayout.setVisibility(View.GONE);
                if (sourceMarker != null && destinationMarker != null) {
                    sourceMarker.setDraggable(false);
                    destinationMarker.setDraggable(false);
                }
            } else if (flowValue == 4) {
                imgMenu.setVisibility(View.VISIBLE);
                setupRecyclerView();
                lnrProviderAccepted.startAnimation(slide_up);
                lnrProviderAccepted.setVisibility(View.VISIBLE);
                if (sourceMarker != null && destinationMarker != null) {
                    sourceMarker.setDraggable(false);
                    destinationMarker.setDraggable(false);
                }
            } else if (flowValue == 5) {
                imgMenu.setVisibility(View.VISIBLE);
                lnrInvoice.startAnimation(slide_up);
                lnrInvoice.setVisibility(View.VISIBLE);
                if (sourceMarker != null && destinationMarker != null) {
                    sourceMarker.setDraggable(false);
                    destinationMarker.setDraggable(false);
                }
            } else if (flowValue == 6) {
                imgMenu.setVisibility(View.VISIBLE);
                lnrRateProvider.startAnimation(slide_up);
                lnrRateProvider.setVisibility(View.VISIBLE);
                LayerDrawable drawable = (LayerDrawable) ratingProviderRate.getProgressDrawable();
                drawable.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
                drawable.getDrawable(1).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);
                drawable.getDrawable(2).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);
                ratingProviderRate.setRating(1.0f);
                feedBackRating = "1";
                ratingProviderRate.setOnRatingBarChangeListener((ratingBar, rating, b) -> {
                    if (rating < 1.0f) {
                        ratingProviderRate.setRating(1.0f);
                        feedBackRating = "1";
                    }
                    feedBackRating = String.valueOf((int) rating);
                });
                if (sourceMarker != null && destinationMarker != null) {
                    sourceMarker.setDraggable(false);
                    destinationMarker.setDraggable(false);
                }
            } else if (flowValue == 7) {
                imgBack.setVisibility(View.VISIBLE);
                ScheduleLayout.startAnimation(slide_up);
                ScheduleLayout.setVisibility(View.VISIBLE);
                if (sourceMarker != null && destinationMarker != null) {
                    sourceMarker.setDraggable(false);
                    destinationMarker.setDraggable(false);
                }
            } else if (flowValue == 8) {
                // clear all views
                shadowBack.setVisibility(View.GONE);
                if (sourceMarker != null && destinationMarker != null) {
                    sourceMarker.setDraggable(false);
                    destinationMarker.setDraggable(false);
                }
            } else if (flowValue == 9) {
                srcDestLayout.setVisibility(View.GONE);
                rtlStaticMarker.setVisibility(View.VISIBLE);
                shadowBack.setVisibility(View.GONE);
                frmDestination.setVisibility(View.GONE);
                if (sourceMarker != null && destinationMarker != null) {
                    sourceMarker.setDraggable(false);
                    destinationMarker.setDraggable(false);
                }
            } else if (flowValue == 10) {
                destination.setHint(context.getResources().getString(R.string.extend_trip));
                frmDestination.setVisibility(View.VISIBLE);
                imgMenu.setVisibility(View.VISIBLE);
                if (lnrProviderAccepted.getVisibility() == View.GONE) {
                    lnrProviderAccepted.startAnimation(slide_up);
                    lnrProviderAccepted.setVisibility(View.VISIBLE);
                    setupRecyclerView();
                }
                if (sourceMarker != null && destinationMarker != null) {
                    sourceMarker.setDraggable(false);
                    destinationMarker.setDraggable(true);
                }
                Utilities.getAddressUsingLatLng("destination", destination, context, "" + dest_lat,
                        "" + dest_lng);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            activity, R.raw.style_json));
            if (!success) {
                Utilities.print("Map:Style", "Style parsing failed.");
            } else {
                Utilities.print("Map:Style", "Style Applied.");
            }
        } catch (Resources.NotFoundException e) {
            Utilities.print("Map:Style", "Can't find style. Error: ");
        }
        mMap = googleMap;
        setupMap();
        customDialog.dismiss();
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
//                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
//            mMap.setMyLocationEnabled(true);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(context)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    1);
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        if (is_track.equalsIgnoreCase("YES") && CurrentStatus.equalsIgnoreCase("PICKEDUP")) {
            LatLng markerLocation = destinationMarker.getPosition();
            extend_dest_lat = "" + markerLocation.latitude;
            extend_dest_lng = "" + markerLocation.longitude;
            showTripExtendAlert(extend_dest_lat, extend_dest_lng);
        } else {
            String title;
            if (marker != null && marker.getTitle() != null) {
                title = marker.getTitle();
                if (sourceMarker != null && title.equalsIgnoreCase("Source")) {
                    LatLng markerLocation = sourceMarker.getPosition();
                    source_lat = markerLocation.latitude + "";
                    source_lng = markerLocation.longitude + "";
                    source_address = Utilities.getAddressUsingLatLng("source", frmSource, context, "" + source_lat,
                            "" + source_lng);
                } else if (destinationMarker != null && title.equalsIgnoreCase("Destination")) {
                    LatLng markerLocation = destinationMarker.getPosition();
                    dest_lat = "" + markerLocation.latitude;
                    dest_lng = "" + markerLocation.longitude;
                    dest_address = Utilities.getAddressUsingLatLng("destination", frmDest, context, "" + dest_lat,
                            "" + dest_lng);
                }
                mMap.clear();
                setValuesForSourceAndDestination();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    //Toast.makeText(SignInActivity.this, "PERMISSION_GRANTED", Toast.LENGTH_SHORT).show();
                    initMap();
                    MapsInitializer.initialize(getActivity());
                } /*else {
                    showPermissionReqDialog();
                }*/
                break;
            case 2:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    //Toast.makeText(SignInActivity.this, "PERMISSION_GRANTED", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + SharedHelper.getKey(context, "provider_mobile_no")));
                    startActivity(intent);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1);
                }
                break;
            case 3:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    //Toast.makeText(SignInActivity.this, "PERMISSION_GRANTED", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + SharedHelper.getKey(context, "sos")));
                    startActivity(intent);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 3);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showDialogForGPSIntent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("GPS is disabled in your device. Enable it?")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        (dialog, id) -> {
                            Intent callGPSSettingIntent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            activity.startActivity(callGPSSettingIntent);
                        });
        builder.setNegativeButton("Cancel",
                (dialog, id) -> dialog.cancel());
        AlertDialog alert1 = builder.create();
        alert1.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_WEB_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                flowValue = 6;
                layoutChanges();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(context, getString(R.string.payment_failed),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION) {
            if (parserTask != null) {
                parserTask = null;
            }
            if (fetchUrl != null) {
                fetchUrl = null;
            }
            if (resultCode == Activity.RESULT_OK) {
                if (marker != null) {
                    marker.remove();
                }
                location_array = new ArrayList<>();
                int location_size = (int) data.getSerializableExtra("Location size");
                Locations placePredictions = (Locations) data.getSerializableExtra("Location Address1");
                if (location_size >= 0) {
                    for (int i = 1; i <= location_size; i++) {
                        location_array.add((Locations) data.getSerializableExtra("Location Address" + i));
                    }
                    Log.e(TAG, "onActivityResult: " + location_array.toString());
                    strPickLocation = data.getExtras().getString("pick_location");
                    strPickType = data.getExtras().getString("type");
                    if (strPickLocation.equalsIgnoreCase("yes")) {
                        pick_first = true;
                        mMap.clear();
                        flowValue = 9;
                        layoutChanges();
                        float zoomLevel = 16.0f; //This goes up to 21
                        stopAnim();
                    } else {
                        if (placePredictions != null) {
                            if (is_track.equalsIgnoreCase("YES") && CurrentStatus.equalsIgnoreCase("PICKEDUP")) {
                                extend_dest_lat = "" + placePredictions.getdLatitude();
                                extend_dest_lng = "" + placePredictions.getdLongitude();
                                showTripExtendAlert(extend_dest_lat, extend_dest_lng);
                            } else {
                                if (placePredictions.getsAddress() != null && !placePredictions.getsAddress().equalsIgnoreCase("")) {
                                    try {
                                        source_lat = "" + placePredictions.getsLatitude();
                                        source_lng = "" + placePredictions.getsLongitude();
                                        source_address = Utilities.getAddressUsingLatLng("source", frmSource, context, "" + source_lat,
                                                "" + source_lng);
                                        frmSource.setText(source_address);
                                        if (!placePredictions.getsLatitude().equalsIgnoreCase("")
                                                && !placePredictions.getsLongitude().equalsIgnoreCase("")) {
                                            double latitude = Double.parseDouble(placePredictions.getsLatitude());
                                            double longitude = Double.parseDouble(placePredictions.getsLongitude());
                                            LatLng location = new LatLng(latitude, longitude);
                                            MarkerOptions markerOptions = new MarkerOptions()
                                                    .position(location)
                                                    .snippet(frmSource.getText().toString())
                                                    .title("source")
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker));
                                            marker = mMap.addMarker(markerOptions);
                                            sourceMarker = mMap.addMarker(markerOptions);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (!placePredictions.getdAddress().equalsIgnoreCase("")) {
                                    dest_lat = "" + placePredictions.getdLatitude();
                                    dest_lng = "" + placePredictions.getdLongitude();
                                    dest_address = Utilities.getAddressUsingLatLng("destination", frmDest, context, "" + dest_lat,
                                            "" + dest_lng);
                                    rvDesAddress.setAdapter(new DropListAdapter(location_array));
                                    frmDest.setText(dest_address);
                                    SharedHelper.putKey(context, "current_status", "2");
                                    if (source_lat != null && source_lng != null && !source_lng.equalsIgnoreCase("")
                                            && !source_lat.equalsIgnoreCase("")) {
                                        try {
                                            String url = getUrl(Double.parseDouble(source_lat), Double.parseDouble(source_lng)
                                                    , Double.parseDouble(dest_lat), Double.parseDouble(dest_lng));
                                            fetchUrl = new FetchUrl();
                                            fetchUrl.execute(url);
                                            LatLng location = new LatLng(Double.parseDouble(current_lat), Double.parseDouble(current_lng));

                                            if (sourceMarker != null)
                                                sourceMarker.remove();
                                            MarkerOptions markerOptions = new MarkerOptions()
                                                    .position(location)
                                                    .snippet(frmSource.getText().toString())
                                                    .title("source")
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker));
                                            marker = mMap.addMarker(markerOptions);
                                            sourceMarker = mMap.addMarker(markerOptions);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    for (int i = 0; i < location_array.size(); i++) {
                                        LatLng latLng = new LatLng(Double.parseDouble(location_array.get(i).getdLatitude()), Double.parseDouble(location_array.get(i).getdLongitude()));
                                        MarkerOptions destMarker = new MarkerOptions()
                                                .position(latLng).title("destination " + i).snippet(location_array.get(i).getdAddress())
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.provider_marker));
                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                        builder.include(mMap.addMarker(destMarker).getPosition());
                                    }
                                    /*if (!dest_lat.equalsIgnoreCase("") && !dest_lng.equalsIgnoreCase("")) {
                                        try {
                                            destLatLng = new LatLng(Double.parseDouble(dest_lat), Double.parseDouble(dest_lng));
                                            if (destinationMarker != null)
                                                destinationMarker.remove();
                                            MarkerOptions destMarker = new MarkerOptions()
                                                    .position(destLatLng).title("destination").snippet(frmDest.getText().toString())
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.provider_marker));
                                            destinationMarker = mMap.addMarker(destMarker);
                                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                            builder.include(sourceMarker.getPosition());
                                            builder.include(destinationMarker.getPosition());
                                            LatLngBounds bounds = builder.build();
                                            int padding = 150; // offset from edges of the map in pixels
                                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                            mMap.moveCamera(cu);

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }*/
                                }
                                if (dest_address.equalsIgnoreCase("")) {
                                    flowValue = 1;
//                            frmSource.setText(source_address);
                                    getServiceList();
                                } else {
                                    flowValue = 1;
                                    if (cardInfoArrayList.size() > 0) {
                                        getCardDetailsForPayment(cardInfoArrayList.get(0));
                                    }
                                    getServiceList();
                                }
                                layoutChanges();
                            }
                        }
                    }
                } else {
                    Log.i(TAG, "Locations are empty");
                }
            }
            if (requestCode == ADD_CARD_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    boolean result = data.getBooleanExtra("isAdded", false);
                    if (result) {
                        getCards();
                    }
                }
            }
            if (requestCode == REQUEST_LOCATION) {
                Log.e("GPS Result Status", "onActivityResult: " + requestCode);
                Log.e("GPS Result Status", "onActivityResult: " + data);
            } else {
                Log.e("GPS Result Status else", "onActivityResult: " + requestCode);
                Log.e("GPS Result Status else", "onActivityResult: " + data);
            }
        }
        if (requestCode == 5555) {
            if (resultCode == Activity.RESULT_OK) {
                CardInfo cardInfo = data.getParcelableExtra("card_info");
                getCardDetailsForPayment(cardInfo);
            }
        }
    }

    private void showTripExtendAlert(final String latitude, final String longitude) {
        Utilities.getAddressUsingLatLng("destination", frmDest, context, latitude, longitude);
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(getString(R.string.extend_trip_alert));
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            Utilities.getAddressUsingLatLng("destination", destination, context, latitude, longitude);
            extendTripAPI(latitude, longitude);
        });
        builder.setNegativeButton(R.string.no, (dialog, which) -> {
            //Reset to previous seletion menu in navigation
            dialog.dismiss();
        });
        builder.setCancelable(false);
        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.setOnShowListener(arg -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        });
        dialog.show();
    }

    private void extendTripAPI(final String latitude, final String longitude) {
        ApiInterface mApiInterface = RetrofitClient.getLiveTrackingClient().create(ApiInterface.class);
        Call<ResponseBody> call = mApiInterface.extendTrip("XMLHttpRequest", SharedHelper.getKey(context, "token_type") + " " + SharedHelper.getKey(context, "access_token"),
                SharedHelper.getKey(context, "request_id"), latitude, longitude, SharedHelper.getKey(context, "extend_address"));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.e("sUCESS", "SUCESS" + response.body());
                if (response.body() != null) {
                    try {
                        String bodyString = new String(response.body().bytes());
                        Log.e("sUCESS", "bodyString" + bodyString);
                        try {
                            JSONObject jsonObj = new JSONObject(bodyString);
                            Toast.makeText(context, jsonObj.optString("message"), Toast.LENGTH_SHORT).show();
                            dest_lat = latitude;
                            dest_lng = longitude;
                            dest_address = SharedHelper.getKey(context, "extend_address");
                            mMap.clear();
                            setValuesForSourceAndDestination();
                            flowValue = 10;
                            layoutChanges();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    void showProviderPopup(JSONObject jsonObject) {
        lnrSearchAnimation.startAnimation(slide_up_top);
        lnrSearchAnimation.setVisibility(View.GONE);
        lnrProviderPopup.setVisibility(View.VISIBLE);
        lnrRequestProviders.setVisibility(View.GONE);
        Glide.with(activity).load(jsonObject.optString("image")).placeholder(R.drawable.pickup_drop_icon).dontAnimate()
                .error(R.drawable.pickup_drop_icon).into(imgProviderPopup);
        lnrPriceBase.setVisibility(View.GONE);
        lnrPricemin.setVisibility(View.GONE);
        lnrPricekm.setVisibility(View.GONE);
        if (jsonObject.optString("calculator").equalsIgnoreCase("MIN")
                || jsonObject.optString("calculator").equalsIgnoreCase("HOUR")) {
            lnrPriceBase.setVisibility(View.VISIBLE);
            lnrPricemin.setVisibility(View.VISIBLE);
            if (jsonObject.optString("calculator").equalsIgnoreCase("MIN")) {
                lblCalculationType.setText("Minutes");
            } else {
                lblCalculationType.setText("Hours");
            }
        } else if (jsonObject.optString("calculator").equalsIgnoreCase("DISTANCE")) {
            lnrPriceBase.setVisibility(View.VISIBLE);
            lnrPricekm.setVisibility(View.VISIBLE);
            lblCalculationType.setText("Distance");
        } else if (jsonObject.optString("calculator").equalsIgnoreCase("DISTANCEMIN")
                || jsonObject.optString("calculator").equalsIgnoreCase("DISTANCEHOUR")) {
            lnrPriceBase.setVisibility(View.VISIBLE);
            lnrPricemin.setVisibility(View.VISIBLE);
            lnrPricekm.setVisibility(View.VISIBLE);
            if (jsonObject.optString("calculator").equalsIgnoreCase("DISTANCEMIN")) {
                lblCalculationType.setText("Distance and Minutes");
            } else {
                lblCalculationType.setText("Distance and Hours");
            }
        }
        if (!jsonObject.optString("capacity").equalsIgnoreCase("null")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                lblCapacity.setText(jsonObject.optString("capacity") + " Kgs");
            } else {
                lblCapacity.setText(jsonObject.optString("capacity") + " Kgs");
            }
        } else {
            lblCapacity.setVisibility(View.GONE);
        }
        lblServiceName.setText("" + jsonObject.optString("name"));
        lblBasePricePopup.setText(SharedHelper.getKey(context, "currency") + jsonObject.optString("fixed"));
        lblPriceKm.setText(SharedHelper.getKey(context, "currency") + jsonObject.optString("price"));
        lblPriceMin.setText(SharedHelper.getKey(context, "currency") + jsonObject.optString("minute"));
        if (jsonObject.optString("description").equalsIgnoreCase("null")) {
            lblProviderDesc.setVisibility(View.GONE);
        } else {
            lblProviderDesc.setVisibility(View.VISIBLE);
            lblProviderDesc.setText("" + jsonObject.optString("description"));
        }
    }

    public void setValuesForApproximateLayout() {
        if (isInternet) {
            String surge = SharedHelper.getKey(context, "surge");
            if (surge.equalsIgnoreCase("1")) {
                surgeDiscount.setVisibility(View.VISIBLE);
                surgeTxt.setVisibility(View.VISIBLE);
                surgeDiscount.setText(SharedHelper.getKey(context, "surge_value"));
            } else {
                surgeDiscount.setVisibility(View.GONE);
                surgeTxt.setVisibility(View.GONE);
            }
            lblApproxAmount.setText(SharedHelper.getKey(context, "currency") + "" + SharedHelper.getKey(context, "estimated_fare"));
            lblEta.setText(SharedHelper.getKey(context, "eta_time"));
            if (!SharedHelper.getKey(context, "name").equalsIgnoreCase("")
                    && !SharedHelper.getKey(context, "name").equalsIgnoreCase(null)
                    && !SharedHelper.getKey(context, "name").equalsIgnoreCase("null")) {
                lblType.setText(SharedHelper.getKey(context, "name"));
                lblHelper.setText(SharedHelper.getKey(context, "helper_count"));
            } else {
                lblType.setText("" + "Moving Truck");
            }
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
        }
    }

    private void getCards() {
        Ion.with(this)
                .load(URLHelper.CARD_PAYMENT_LIST)
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Authorization", SharedHelper.getKey(context, "token_type") + " " + SharedHelper.getKey(context, "access_token"))
                .asString()
                .withResponse()
                .setCallback((e, response) -> {
                    // response contains both the headers and the string result
                    try {
                        if (response.getHeaders().code() == 200) {
                            try {
                                JSONArray jsonArray = new JSONArray(response.getResult());
                                if (jsonArray.length() > 0) {
                                    CardInfo cardInfo = new CardInfo();
                                    cardInfo.setCardId("CASH");
                                    cardInfo.setCardType("CASH");
                                    cardInfo.setLastFour("CASH");
                                    cardInfoArrayList.add(cardInfo);
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject cardObj = jsonArray.getJSONObject(i);
                                        cardInfo = new CardInfo();
                                        cardInfo.setCardId(cardObj.optString("card_id"));
                                        cardInfo.setCardType(cardObj.optString("brand"));
                                        cardInfo.setLastFour(cardObj.optString("last_four"));
                                        cardInfoArrayList.add(cardInfo);
                                    }
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        CardInfo cardInfo = new CardInfo();
                        cardInfo.setCardId("CASH");
                        cardInfo.setCardType("CASH");
                        cardInfo.setLastFour("CASH");
                        cardInfoArrayList.add(cardInfo);
                    }
                });
    }

    public void getServiceList() {
        try {
            customDialog = new CustomDialog(context);
            customDialog.setCancelable(false);
            if (customDialog != null) {
                customDialog.show();
            }
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URLHelper.GET_SERVICE_LIST_API, response -> {
                JSONArray jsonArray = new JSONArray();
                try {
                    Utilities.print("GetServices", response.toString());
                    if (SharedHelper.getKey(context, "service_type").equalsIgnoreCase("")) {
                        SharedHelper.putKey(context, "service_type", "" + response.optJSONObject(0).optString("id"));
                    }
                    if ((customDialog != null) && (customDialog.isShowing()))
                        customDialog.dismiss();
                    if (response.length() > 0) {
                        currentPostion = 0;
                        for (int i = 0; i < response.length(); i++) {
                            if (SharedHelper.getKey(context, "user_type").equalsIgnoreCase(response.optJSONObject(i).optString("user_type"))) {
                                jsonArray.put(response.optJSONObject(i));
                            }
                        }
                        Log.e(TAG, "array: " + jsonArray.length());
                        ServiceListAdapter serviceListAdapter = new ServiceListAdapter(response);
                        rcvServiceTypes.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
                        rcvServiceTypes.setAdapter(serviceListAdapter);
                        getProvidersList(SharedHelper.getKey(context, "service_type"));
                    }
                    if (mMap != null) {
                        mMap.clear();
                    }
                    setValuesForSourceAndDestination();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, error -> {
                try {
                    if ((customDialog != null) && (customDialog.isShowing()))
                        customDialog.dismiss();
                    String json;
                    NetworkResponse response = error.networkResponse;
                    if (response != null && response.data != null) {
                        try {
                            JSONObject errorObj = new JSONObject(new String(response.data));
                            if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                                try {
                                    Utilities.displayMessage(getView(), context, errorObj.optString("message"));
                                } catch (Exception e) {
                                    Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.something_went_wrong));
                                }
                                flowValue = 1;
                                layoutChanges();
                            } else if (response.statusCode == 401) {
                                refreshAccessToken("SERVICE_LIST");
                            } else if (response.statusCode == 422) {
                                json = MyApplication.trimMessage(new String(response.data));
                                if (json != null && !json.equals("")) {
                                    Utilities.displayMessage(getView(), context, json);
                                } else {
                                    Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.please_try_again));
                                }
                                flowValue = 1;
                                layoutChanges();
                            } else if (response.statusCode == 503) {
                                Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.server_down));
                                flowValue = 1;
                                layoutChanges();
                            } else {
                                Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.please_try_again));
                                flowValue = 1;
                                layoutChanges();
                            }
                        } catch (Exception e) {
                            Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.something_went_wrong));
                            flowValue = 1;
                            layoutChanges();
                        }
                    } else {
                        Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.please_try_again));
                        flowValue = 1;
                        layoutChanges();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "" + SharedHelper.getKey(context, "token_type") + " "
                            + SharedHelper.getKey(context, "access_token"));
                    return headers;
                }
            };
            MyApplication.getInstance().addToRequestQueue(jsonArrayRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getApproximateFare() {
        try {
            customDialog = new CustomDialog(context);
            customDialog.setCancelable(false);
            if (customDialog != null)
                customDialog.show();
            String place = "";
            Log.e(TAG, "getApproximateFare:array " + location_array.size());
            for (int i = 1; i <= location_array.size(); i++) {
                if (i == 1) {
                    place = place + "s_latitude[" + i + "]=" + location_array.get(i - 1).getsLatitude()
                            + "&s_longitude[" + i + "]=" + location_array.get(i - 1).getsLongitude()
                            + "&d_latitude[" + i + "]=" + location_array.get(i - 1).getdLatitude()
                            + "&d_longitude[" + i + "]=" + location_array.get(i - 1).getdLongitude()
                            + "&service_items[1]=" + location_array.get(i - 1).getGoods()
                            + "&helper=" + location_array.get(0).getHelper_count();


                } else {
                    place = place + "&s_latitude[" + i + "]=" + location_array.get(i - 1).getsLatitude()
                            + "&s_longitude[" + i + "]=" + location_array.get(i - 1).getsLongitude()
                            + "&d_latitude[" + i + "]=" + location_array.get(i - 1).getdLatitude()
                            + "&d_longitude[" + i + "]=" + location_array.get(i - 1).getdLongitude()
                            + "&service_items[1]=" + location_array.get(i - 1).getGoods()
                            + "&helper=" + location_array.get(0).getHelper_count();
                }
            }
            Log.e(TAG, "getApproximateFare: " + place);
            //String place_all = place.substring(1);
//            int n = 1;
//            while( n <= location_array.size()){
//                System.out.println(location_array.getdAddress(n));
//                n++;
//            }
            JSONObject object = new JSONObject();
            String constructedURL = URLHelper.ESTIMATED_FARE_DETAILS_API + "" +
                    "?" + place
                    + "&service_type=" + SharedHelper.getKey(context, "service_type");
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, constructedURL, object, response -> {
                if (response != null) {
                    if (!response.optString("estimated_fare").equalsIgnoreCase("")) {
                        Utilities.print("ApproximateResponse", response.toString());
                        SharedHelper.putKey(context, "estimated_fare", response.optString("estimated_fare"));
                        SharedHelper.putKey(context, "distance", response.optString("distance"));
                        SharedHelper.putKey(context, "eta_time", response.optString("time"));
                        SharedHelper.putKey(context, "surge", response.optString("surge"));
                        SharedHelper.putKey(context, "surge_value", response.optString("surge_value"));
                        SharedHelper.putKey(context, "helper_count", response.optString("helper"));
                        setValuesForApproximateLayout();
                        double wallet_balance = response.optDouble("wallet_balance");
                        SharedHelper.putKey(context, "wallet_balance", "" + response.optDouble("wallet_balance"));
                        if (!Double.isNaN(wallet_balance) && wallet_balance > 0) {
                            lineView.setVisibility(View.VISIBLE);
                            chkWallet.setVisibility(View.VISIBLE);
                        } else {
                            lineView.setVisibility(View.GONE);
                            chkWallet.setVisibility(View.GONE);
                        }
                        flowValue = 2;
                        layoutChanges();
                    }
                }
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
                                if (errorObj.has("error")) {
                                    Utilities.showAlert(context, errorObj.optString("error"));
                                } else if (errorObj.has("message")) {
                                    Utilities.showAlert(context, errorObj.optString("message"));
                                } else {
                                    Utilities.showAlert(context, context.getResources().getString(R.string.something_went_wrong));
                                }
                            } catch (Exception e) {
                                Utilities.showAlert(context, context.getResources().getString(R.string.something_went_wrong));
                            }
                        } else if (response.statusCode == 401) {
                            refreshAccessToken("APPROXIMATE_RATE");
                        } else if (response.statusCode == 422) {
                            json = MyApplication.trimMessage(new String(response.data));
                            if (json != null && !json.equals("")) {
                                Utilities.showAlert(context, json);
                            } else {
                                Utilities.showAlert(context, context.getResources().getString(R.string.please_try_again));
                            }
                        } else if (response.statusCode == 503) {
                            Utilities.showAlert(context, context.getResources().getString(R.string.server_down));
                        } else {
                            Utilities.showAlert(context, context.getResources().getString(R.string.please_try_again));
                        }
                    } catch (Exception e) {
                        Utilities.showAlert(context, context.getResources().getString(R.string.something_went_wrong));
                    }
                } else {
                    Utilities.showAlert(context, context.getResources().getString(R.string.please_try_again));
                }

            }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "" + SharedHelper.getKey(context, "token_type") + " " + SharedHelper.getKey(context, "access_token"));
                    return headers;
                }
            };
            MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void getProvidersList(final String strTag) {
        String providers_request = URLHelper.GET_PROVIDERS_LIST_API + "?" +
                "latitude=" + current_lat +
                "&longitude=" + current_lng +
                "&service=" + strTag;
        Utilities.print("Get all providers", "" + providers_request);
        Utilities.print("service_type", "" + SharedHelper.getKey(context, "service_type"));
        for (int i = 0; i < lstProviderMarkers.size(); i++) {
            lstProviderMarkers.get(i).remove();
        }
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(providers_request, response -> {
            Utilities.print("GetProvidersList", response.toString());
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject jsonObj = response.getJSONObject(i);
                    Utilities.print("GetProvidersList", jsonObj.getString("latitude") + "," + jsonObj.getString("longitude"));
                    if (!jsonObj.getString("latitude").equalsIgnoreCase("") && !jsonObj.getString("longitude").equalsIgnoreCase("")) {
                        double proLat = Double.parseDouble(jsonObj.getString("latitude"));
                        double proLng = Double.parseDouble(jsonObj.getString("longitude"));
                        if (mHashMap.containsKey(jsonObj.optInt("id"))) {
                            Marker marker = mHashMap.get(jsonObj.optInt("id"));
                            LatLng startPosition = marker.getPosition();
                            LatLng newPos = new LatLng(proLat, proLng);

                            marker.setPosition(newPos);
                            marker.setRotation(getBearing(startPosition, newPos));
                        } else {
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .anchor(0.5f, 0.75f)
                                    .position(new LatLng(proLat, proLng))
                                    .rotation(0.0f)
                                    .snippet(jsonObj.getString("id"))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_marker));
                            lstProviderMarkers.add(mMap.addMarker(markerOptions));
//                                mHashMap.put(jsonObj.optInt("id"), mMap.addMarker(markerOptions));
                            builder.include(new LatLng(proLat, proLng));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> {
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                try {
                    JSONObject errorObj = new JSONObject(new String(response.data));
                    if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                        try {
                            Utilities.showAlert(context, errorObj.optString("message"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (response.statusCode == 401) {
                        refreshAccessToken("PROVIDERS_LIST");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };
        MyApplication.getInstance().addToRequestQueue(jsonArrayRequest);
    }

    public void sendRequest() {
        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                URLHelper.SEND_REQUEST_API, response -> {
            try {
                JSONObject resObj = new JSONObject(new String(response.data));
                if (response.statusCode == 200)
                    Toast.makeText(activity, resObj.optString("message"), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(activity, resObj.optString("error"), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onResponse: " + new Gson().toJson(response));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, error -> {
            if ((customDialog != null) && customDialog.isShowing())
                customDialog.dismiss();
            Log.e(TAG, "onErrorResponse: " + error.toString());
            Log.e(TAG, "onErrorResponse: " + error.networkResponse.statusCode);
            Log.e(TAG, "onErrorResponse: " + Arrays.toString(error.networkResponse.data));
            String json;
            NetworkResponse response = error.networkResponse;
            if (response.data != null) {
                try {
                    JSONObject errorObj = new JSONObject(new String(response.data));
                    if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                        try {
                            if (errorObj.has("error")) {
                                displayMessage(errorObj.optString("error"));
                            } else {
                                displayMessage(errorObj.optString("message"));
                            }
                        } catch (Exception e) {
                            displayMessage(context.getResources().getString(R.string.something_went_wrong));
                        }
                    } else if (response.statusCode == 401) {
                        refreshAccessToken("UPDATE_PROFILE_WITH_IMAGE");
                    } else if (response.statusCode == 422) {
                        json = MyApplication.trimMessage(new String(response.data));
                        if (json != null && !json.equalsIgnoreCase("")) {
                            displayMessage(json);
                        } else {
                            displayMessage(context.getResources().getString(R.string.please_try_again));
                        }
                    } else if (response.statusCode == 503) {
                        displayMessage(context.getResources().getString(R.string.server_down));
                    } else {
                        displayMessage(context.getResources().getString(R.string.please_try_again));
                    }
                } catch (Exception e) {
                    displayMessage(context.getResources().getString(R.string.something_went_wrong));
                }
            } else {
                if (error instanceof NoConnectionError) {
                    displayMessage(context.getResources().getString(R.string.oops_connect_your_internet));
                } else if (error instanceof NetworkError) {
                    displayMessage(context.getResources().getString(R.string.oops_connect_your_internet));
                } else if (error instanceof TimeoutError) {
                    sendRequest();
                }
            }
        }) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                try {
                    for (int i = 1; i <= location_array.size(); i++) {
                        params.put("s_latitude[" + i + "]", location_array.get(i - 1).getsLatitude());
                        params.put("s_longitude[" + i + "]", location_array.get(i - 1).getsLongitude());
                        params.put("d_latitude[" + i + "]", location_array.get(i - 1).getdLatitude());
                        params.put("d_longitude[" + i + "]", location_array.get(i - 1).getdLongitude());
                        params.put("service_items[" + i + "]", location_array.get(i - 1).getGoods());
                        params.put("d_address[" + i + "]", location_array.get(i - 1).getdAddress());
                        params.put("receiver_name[" + i + "]", location_array.get(i - 1).getReciver_name());
                        params.put("receiver_number[" + i + "]", location_array.get(i - 1).getReciver_number());
                        if(!location_array.get(0).getHelper_count().isEmpty())
                        params.put("helper", location_array.get(0).getHelper_count());
                        else params.put("helper", "0");
                    }
                    try {
                        for (int i = 0; i <= location_array.size() - 1; i++) {
                            params.put("s_address[" + (i + 1) + "]", SharedHelper.getKey(context, "source"));
                        }
                        //  params.put("s_address[1]", SharedHelper.getKey(context, "source"));
                        params.put("service_type", SharedHelper.getKey(context, "service_type"));
                        params.put("distance", SharedHelper.getKey(context, "distance"));
                        params.put("schedule_date", scheduledDate);
                        params.put("schedule_time", scheduledTime);
                        Log.e("Schedule Request", "sendRequest: " + params);
                        if (chkWallet.isChecked()) {
                            params.put("use_wallet", "1");
                        } else {
                            params.put("use_wallet", "0");
                        }
                        if (SharedHelper.getKey(context, "payment_mode").equals("CASH")) {
                            params.put("payment_mode", SharedHelper.getKey(context, "payment_mode"));
                        } else {
                            params.put("payment_mode", SharedHelper.getKey(context, "payment_mode"));
                            params.put("card_id", SharedHelper.getKey(context, "card_id"));
                        }
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "" + SharedHelper.getKey(context, "token_type") + " " + SharedHelper.getKey(context, "access_token"));
                Log.e(TAG, "getHeaders: " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };
        MyApplication.getInstance().addToRequestQueue(volleyMultipartRequest);
    }

    public void cancelRequest() {
        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("request_id", SharedHelper.getKey(context, "request_id"));
            object.put("cancel_reason", cancalReason);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.CANCEL_REQUEST_API, object, response -> {
            Utilities.print("CancelRequestResponse", response.toString());
            Toast.makeText(context, context.getResources().getString(R.string.request_cancel), Toast.LENGTH_SHORT).show();
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            mapClear();
            SharedHelper.putKey(context, "request_id", "");
            flowValue = 0;
            PreviousStatus = "";
            layoutChanges();
            setupMap();
        }, error -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            String json;
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                flowValue = 4;
                try {
                    JSONObject errorObj = new JSONObject(new String(response.data));
                    if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                        try {
                            Utilities.displayMessage(getView(), context, errorObj.optString("message"));
                        } catch (Exception e) {
                            Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.something_went_wrong));
                        }
                        layoutChanges();
                    } else if (response.statusCode == 401) {
                        refreshAccessToken("CANCEL_REQUEST");
                    } else if (response.statusCode == 422) {
                        json = MyApplication.trimMessage(new String(response.data));
                        if (json != null && !json.equals("")) {
                            Utilities.displayMessage(getView(), context, json);
                        } else {
                            Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.please_try_again));
                        }
                        layoutChanges();
                    } else if (response.statusCode == 503) {
                        Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.server_down));
                        layoutChanges();
                    } else {
                        Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.please_try_again));
                        layoutChanges();
                    }
                } catch (Exception e) {
                    Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.something_went_wrong));
                    layoutChanges();
                }
            } else {
                Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.please_try_again));
                layoutChanges();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "" + SharedHelper.getKey(context, "token_type") + " " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };
        MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void setValuesForSourceAndDestination() {
        if (isInternet) {
            if (!source_lat.equalsIgnoreCase("")) {
                if (!source_address.equalsIgnoreCase("")) {
//                    frmSource.setText(source_address);
                } else {
                    Utilities.getAddressUsingLatLng("source", frmSource, context, "" + source_lat, "" + source_lng);
                }
            } else {
                Utilities.getAddressUsingLatLng("source", frmSource, context, "" + current_lat, "" + current_lng);
            }
            if (!dest_lat.equalsIgnoreCase("")) {
                if (is_track.equalsIgnoreCase("YES") &&
                        (CurrentStatus.equalsIgnoreCase("STARTED") || CurrentStatus.equalsIgnoreCase("PICKEDUP")
                                || CurrentStatus.equalsIgnoreCase("ARRIVED"))) {
                    // Source Destination should not visible at the track
//                    destination.setText(SharedHelper.getKey(context, "extend_address"));
                } else {
                    destination.setText(SharedHelper.getKey(context, "destination"));
                    srcDestLayout.setVisibility(View.VISIBLE);
                }
            }
            if (!source_lat.equalsIgnoreCase("") && !source_lng.equalsIgnoreCase("")) {
                sourceLatLng = new LatLng(Double.parseDouble(source_lat), Double.parseDouble(source_lng));
            }
            if (!dest_lat.equalsIgnoreCase("") && !dest_lng.equalsIgnoreCase("")) {
                destLatLng = new LatLng(Double.parseDouble(dest_lat), Double.parseDouble(dest_lng));
            }
            if (sourceLatLng != null && destLatLng != null) {
                Utilities.print("LatLng", "Source:" + sourceLatLng + " Destination: " + destLatLng);
                String url = getUrl(sourceLatLng.latitude, sourceLatLng.longitude, destLatLng.latitude, destLatLng.longitude);
                fetchUrl = new FetchUrl();
                fetchUrl.execute(url);
            }
        }
    }

    private void refreshAccessToken(final String tag) {
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
            Utilities.print("SignUpResponse", response.toString());
            SharedHelper.putKey(context, "access_token", response.optString("access_token"));
            SharedHelper.putKey(context, "refresh_token", response.optString("refresh_token"));
            SharedHelper.putKey(context, "token_type", response.optString("token_type"));
            if (tag.equalsIgnoreCase("SERVICE_LIST")) {
                getServiceList();
            } else if (tag.equalsIgnoreCase("APPROXIMATE_RATE")) {
                getApproximateFare();
            } else if (tag.equalsIgnoreCase("SEND_REQUEST")) {
                sendRequest();
            } else if (tag.equalsIgnoreCase("CANCEL_REQUEST")) {
                cancelRequest();
            } else if (tag.equalsIgnoreCase("PROVIDERS_LIST")) {
                getProvidersList("");
            } else if (tag.equalsIgnoreCase("SUBMIT_REVIEW")) {
                submitReviewCall();
            } else if (tag.equalsIgnoreCase("PAY_NOW")) {
                payNow();
            }
        }, error -> {
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                SharedHelper.putKey(context, "loggedIn", context.getResources().getString(R.string.False));
                Utilities.GoToBeginActivity(getActivity());
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

    private void showChooser() {
        Intent intent = new Intent(getActivity(), Payment.class);
        startActivityForResult(intent, 5555);
    }

    private void getCardDetailsForPayment(CardInfo cardInfo) {
        if (cardInfo.getLastFour().equals("CASH")) {
            SharedHelper.putKey(context, "payment_mode", "CASH");
            imgPaymentType.setImageResource(R.drawable.money_icon);
            lblPaymentType.setText("CASH");
        } else {
            SharedHelper.putKey(context, "card_id", cardInfo.getCardId());
            SharedHelper.putKey(context, "payment_mode", "CARD");
            imgPaymentType.setImageResource(R.drawable.visa);
            lblPaymentType.setText("XXXX-XXXX-XXXX-" + cardInfo.getLastFour());
        }
    }

    public void payNow() {
        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("request_id", SharedHelper.getKey(context, "request_id"));
            object.put("payment_mode", paymentMode);
            object.put("is_paid", isPaid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.PAY_NOW_API, object, response -> {
            Utilities.print("PayNowRequestResponse", response.toString());
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            flowValue = 6;
            layoutChanges();
        }, error -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            String json = "";
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                try {
                    JSONObject errorObj = new JSONObject(new String(response.data));
                    if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                        try {
                            Utilities.displayMessage(getView(), context, errorObj.optString("message"));
                        } catch (Exception e) {
                            Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.something_went_wrong));
                        }
                    } else if (response.statusCode == 401) {
                        refreshAccessToken("PAY_NOW");
                    } else if (response.statusCode == 422) {
                        json = MyApplication.trimMessage(new String(response.data));
                        if (json != null && !json.equals("")) {
                            Utilities.displayMessage(getView(), context, json);
                        } else {
                            Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.please_try_again));
                        }
                    } else if (response.statusCode == 503) {
                        Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.server_down));
                    } else {
                        Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.please_try_again));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.something_went_wrong));
                }
            } else {
                Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.please_try_again));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "" + SharedHelper.getKey(context, "token_type") + " " + SharedHelper.getKey(context, "access_token"));
                headers.put("X-Requested-With", "XMLHttpRequest");
                return headers;
            }
        };
        MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void checkStatus() {
        try {
            Utilities.print("Handler", "Inside");
            if (isInternet) {
                final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                        URLHelper.REQUEST_STATUS_CHECK_API, null, response -> {
                    SharedHelper.putKey(context, "req_status", "");
                    paymentMode = SharedHelper.getKey(context, "payment_mode");
                    if (paymentMode.equals("CASH")) {
                        imgPaymentType.setImageResource(R.drawable.money_icon);
                    } else {
                        imgPaymentType.setImageResource(R.drawable.visa);
                    }
                    lblPaymentType.setText(paymentMode);
                    try {
                        if (customDialog != null && customDialog.isShowing()) {
                            customDialog.dismiss();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    reqStatus = "";
                    Utilities.print("Response", "" + response.toString());
                    try {
                        userdrop = response.getJSONArray("userdrop");
                        if (userdrop != null && !userdrop.toString().equals(previousDrop.toString())) {
                            previousDrop = userdrop;
                            Utilities.print("previousDrop", "" + previousDrop.toString());
                            Utilities.print("userDrop", "" + userdrop.toString());
                            statusArrayList = new ArrayList<>();
                            for (int i = 0; i < userdrop.length(); i++) {
                                TripStatus flows = new TripStatus();
                                flows.setdeliveryAddress(userdrop.getJSONObject(i).optString("d_address"));
                                flows.setcomments(userdrop.getJSONObject(i).optString("service_items"));
                                flows.setstatus(userdrop.getJSONObject(i).optString("status"));
                                flows.setD_lat(userdrop.getJSONObject(i).optString("d_latitude"));
                                flows.setD_long(userdrop.getJSONObject(i).optString("d_longitude"));
                                flows.setAfterImage(userdrop.getJSONObject(i).optString("after_image"));
                                statusArrayList.add(flows);
                            }
                            setupRecyclerView();
                            // Log.e(TAG, "userdrop: "+ userdrop);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (response.optJSONArray("data") != null && response.optJSONArray("data").length() > 0) {
                        Utilities.print("response", "not null");
                        try {
                            JSONArray requestStatusCheck = response.optJSONArray("data");
                            JSONObject requestStatusCheckObject = requestStatusCheck.getJSONObject(0);
                            Log.e(TAG, "requestStatusCheckObject: " + requestStatusCheckObject);
                            //Driver Detail
                            if (requestStatusCheckObject.optJSONObject("provider") != null) {
                                driver = new Driver();
                                driver.setFname(requestStatusCheckObject.optJSONObject("provider").optString("first_name"));
                                driver.setLname(requestStatusCheckObject.optJSONObject("provider").optString("last_name"));
                                driver.setEmail(requestStatusCheckObject.optJSONObject("provider").optString("email"));
                                driver.setMobile(requestStatusCheckObject.optJSONObject("provider").optString("mobile"));
                                driver.setImg(requestStatusCheckObject.optJSONObject("provider").optString("avatar"));
                                driver.setRating(requestStatusCheckObject.optJSONObject("provider").optString("rating"));
                            }
                            String status = requestStatusCheckObject.optString("status");
                            userId = requestStatusCheckObject.optString("user_id");
                            providerId = requestStatusCheckObject.optString("provider_id");
                            is_track = requestStatusCheckObject.optString("is_track");
                            SharedHelper.putKey(context, "track_status", is_track);
                            reqStatus = requestStatusCheckObject.optString("status");
                            SharedHelper.putKey(context, "req_status", requestStatusCheckObject.optString("status"));
                            String wallet = requestStatusCheckObject.optString("use_wallet");
                            source_lat = requestStatusCheckObject.optString("s_latitude");
                            source_lng = requestStatusCheckObject.optString("s_longitude");
                            dest_lat = requestStatusCheckObject.optString("d_latitude");
                            dest_lng = requestStatusCheckObject.optString("d_longitude");
                            if (!source_lat.equalsIgnoreCase("") && !source_lng.equalsIgnoreCase("")) {
                                LatLng myLocation = new LatLng(Double.parseDouble(source_lat), Double.parseDouble(source_lng));
                                CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(14).build();
                            }
                            // surge price
                            if (requestStatusCheckObject.optString("surge").equalsIgnoreCase("1")) {
                                lblSurgePrice.setVisibility(View.VISIBLE);
                            } else {
                                lblSurgePrice.setVisibility(View.GONE);
                            }
                            setTrackStatus();
                            Utilities.print("PreviousStatus", "" + PreviousStatus);
                            if (!PreviousStatus.equals(status)) {
                                mMap.clear();
                                PreviousStatus = status;
                                flowValue = 8;
                                layoutChanges();
                                SharedHelper.putKey(context, "request_id", "" + requestStatusCheckObject.optString("id"));
                                reCreateMap();
                                CurrentStatus = status;
                                Utilities.print("ResponseStatus", "SavedCurrentStatus: " + CurrentStatus + " Status: " + status);
                                switch (status) {
                                    case "SEARCHING":
                                        btnCancelRide.setVisibility(View.VISIBLE);
                                        if ((customDialog != null) && (customDialog.isShowing()))
                                            customDialog.dismiss();
                                        show(lnrWaitingForProviders);
                                        //rippleBackground.startRippleAnimation();
                                        strTag = "search_completed";
                                        if (!source_lat.equalsIgnoreCase("") && !source_lng.equalsIgnoreCase("")) {
                                            LatLng myLocation1 = new LatLng(Double.parseDouble(source_lat),
                                                    Double.parseDouble(source_lng));
                                            CameraPosition cameraPosition1 = new CameraPosition.Builder().target(myLocation1).zoom(14).build();
                                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition1));
                                        }
                                        break;
                                    case "CANCELLED":
                                        strTag = "";
                                        if (reasonDialog != null) {
                                            if (reasonDialog.isShowing()) {
                                                reasonDialog.dismiss();
                                            }
                                        }
                                        if (cancelRideDialog != null) {
                                            if (cancelRideDialog.isShowing()) {
                                                cancelRideDialog.dismiss();
                                            }
                                        }
                                        imgSos.setVisibility(View.GONE);
                                        break;
                                    case "ACCEPTED":
                                        setupRecyclerView();
                                        strTag = "ride_accepted";
                                        try {
                                            JSONObject provider = requestStatusCheckObject.getJSONObject("provider");
                                            JSONObject service_type = requestStatusCheckObject.getJSONObject("service_type");
                                            JSONObject provider_service = requestStatusCheckObject.getJSONObject("provider_service");
                                            SharedHelper.putKey(context, "provider_mobile_no", "" + provider.optString("mobile"));
                                            lblProvider.setText(provider.optString("first_name") + " " + provider.optString("last_name"));
                                            if (provider.optString("avatar").startsWith("http"))
                                                Picasso.with(context).load(provider.optString("avatar")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(imgProvider);
                                            else
                                                Picasso.with(context).load(URLHelper.base + "storage/" + provider.optString("avatar")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(imgProvider);
                                            lblServiceRequested.setText(service_type.optString("name"));
                                            lblModelNumber.setText(provider_service.optString("service_model") + "\n" + provider_service.optString("service_number"));
                                            Picasso.with(context).load(service_type.optString("image"))
                                                    .placeholder(R.drawable.car_select).error(R.drawable.car_select)
                                                    .into(imgServiceRequested);
                                            otpTxt.setVisibility(View.VISIBLE);
                                            otpTxt.setText(String.format(activity.getString(R.string.otp_cat), requestStatusCheckObject.optString("otp")));
                                            ratingProvider.setRating(Float.parseFloat(provider.optString("rating")));
                                            //lnrAfterAcceptedStatus.setVisibility(View.GONE);
                                            lblStatus.setText(context.getResources().getString(R.string.arriving));
                                            AfterAcceptButtonLayout.setVisibility(View.VISIBLE);
                                            btnCancelTrip.setText(context.getResources().getString(R.string.cancel_trip));
                                            btnCancelTrip.setVisibility(View.VISIBLE);
                                            show(lnrProviderAccepted);
                                            flowValue = 9;
                                            layoutChanges();
                                            if (is_track.equalsIgnoreCase("YES")) {
                                                flowValue = 10;
                                                txtChange.setVisibility(View.GONE);
                                            } else {
                                                flowValue = 4;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case "STARTED":
                                        setupRecyclerView();
                                        strTag = "ride_started";
                                        try {
                                            JSONObject provider = requestStatusCheckObject.getJSONObject("provider");
                                            JSONObject service_type = requestStatusCheckObject.getJSONObject("service_type");
                                            JSONObject provider_service = requestStatusCheckObject.getJSONObject("provider_service");
                                            SharedHelper.putKey(context, "provider_mobile_no", "" + provider.optString("mobile"));
                                            lblProvider.setText(provider.optString("first_name") + " " + provider.optString("last_name"));
                                            if (provider.optString("avatar").startsWith("http"))
                                                Picasso.with(context).load(provider.optString("avatar")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(imgProvider);
                                            else
                                                Picasso.with(context).load(URLHelper.base + "storage/" + provider.optString("avatar")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(imgProvider);
                                            lblServiceRequested.setText(service_type.optString("name"));
                                            lblModelNumber.setText(provider_service.optString("service_model") + "\n" + provider_service.optString("service_number"));
                                            Picasso.with(context).load(service_type.optString("image")).placeholder(R.drawable.car_select)
                                                    .error(R.drawable.car_select).into(imgServiceRequested);
                                            otpTxt.setVisibility(View.VISIBLE);
                                            otpTxt.setText(String.format(activity.getString(R.string.otp_cat), requestStatusCheckObject.optString("otp")));
                                            ratingProvider.setRating(Float.parseFloat(provider.optString("rating")));
                                            //lnrAfterAcceptedStatus.setVisibility(View.GONE);
                                            lblStatus.setText(context.getResources().getString(R.string.arriving));
                                            btnCancelTrip.setText(context.getResources().getString(R.string.cancel_trip));
                                            btnCancelTrip.setVisibility(View.VISIBLE);
                                            AfterAcceptButtonLayout.setVisibility(View.VISIBLE);
                                            if (is_track.equalsIgnoreCase("YES")) {
                                                flowValue = 10;
                                                txtChange.setVisibility(View.GONE);
                                            } else {
                                                flowValue = 4;
                                            }
                                            layoutChanges();
                                            if (!requestStatusCheckObject.optString("schedule_at").equalsIgnoreCase("null")) {
                                                SharedHelper.putKey(context, "current_status", "");
                                                Intent intent = new Intent(getActivity(), HistoryActivity.class);
                                                intent.putExtra("tag", "upcoming");
                                                startActivity(intent);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case "ARRIVED":
                                        setupRecyclerView();
                                        btnCancelRide.setVisibility(View.GONE);
                                        once = true;
                                        strTag = "ride_arrived";
                                        Utilities.print("MyTest", "ARRIVED");
                                        try {
                                            Utilities.print("MyTest", "ARRIVED TRY");
                                            JSONObject provider = requestStatusCheckObject.getJSONObject("provider");
                                            JSONObject service_type = requestStatusCheckObject.getJSONObject("service_type");
                                            JSONObject provider_service = requestStatusCheckObject.getJSONObject("provider_service");
                                            lblProvider.setText(provider.optString("first_name") + " " + provider.optString("last_name"));
                                            if (provider.optString("avatar").startsWith("http"))
                                                Picasso.with(context).load(provider.optString("avatar")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(imgProvider);
                                            else
                                                Picasso.with(context).load(URLHelper.base + "storage/" + provider.optString("avatar")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(imgProvider);
                                            lblServiceRequested.setText(service_type.optString("name"));
                                            lblModelNumber.setText(provider_service.optString("service_model") + "\n" + provider_service.optString("service_number"));
                                            Picasso.with(context).load(service_type.optString("image")).placeholder(R.drawable.car_select).error(R.drawable.car_select).into(imgServiceRequested);
                                            otpTxt.setVisibility(View.VISIBLE);
                                            otpTxt.setText(String.format(activity.getString(R.string.otp_cat), requestStatusCheckObject.optString("otp")));
                                            ratingProvider.setRating(Float.parseFloat(provider.optString("rating")));
                                            lnrAfterAcceptedStatus.setVisibility(View.VISIBLE);
                                            tripLine.setVisibility(View.VISIBLE);
                                            lblStatus.setText(context.getResources().getString(R.string.arrived));
                                            btnCancelTrip.setText(context.getResources().getString(R.string.share));
                                            btnCancelTrip.setVisibility(View.GONE);
                                            AfterAcceptButtonLayout.setVisibility(View.VISIBLE);
                                            if (is_track.equalsIgnoreCase("YES")) {
                                                flowValue = 10;
                                                txtChange.setVisibility(View.GONE);
                                            } else {
                                                flowValue = 4;
                                            }
                                            layoutChanges();
                                        } catch (Exception e) {
                                            Utilities.print("MyTest", "ARRIVED CATCH");
                                            e.printStackTrace();
                                        }
                                        break;
                                    case "PICKEDUP":
                                        setupRecyclerView();
                                        btnCancelRide.setVisibility(View.GONE);
                                        once = true;
                                        strTag = "ride_picked";
                                        try {
                                            JSONObject provider = requestStatusCheckObject.getJSONObject("provider");
                                            JSONObject service_type = requestStatusCheckObject.getJSONObject("service_type");
                                            JSONObject provider_service = requestStatusCheckObject.getJSONObject("provider_service");
                                            lblProvider.setText(provider.optString("first_name") + " " + provider.optString("last_name"));
                                            if (provider.optString("avatar").startsWith("http"))
                                                Picasso.with(context).load(provider.optString("avatar")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(imgProvider);
                                            else
                                                Picasso.with(context).load(URLHelper.base + "storage/" + provider.optString("avatar")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(imgProvider);
                                            lblServiceRequested.setText(service_type.optString("name"));
                                            lblModelNumber.setText(provider_service.optString("service_model") + "\n" + provider_service.optString("service_number"));
                                            Picasso.with(context).load(service_type.optString("image")).placeholder(R.drawable.car_select).error(R.drawable.car_select).into(imgServiceRequested);
                                            otpTxt.setVisibility(View.GONE);
                                            ratingProvider.setRating(Float.parseFloat(provider.optString("rating")));
                                            lnrAfterAcceptedStatus.setVisibility(View.VISIBLE);
                                            tripLine.setVisibility(View.VISIBLE);
                                            imgSos.setVisibility(View.VISIBLE);
                                            lblStatus.setText(context.getResources().getString(R.string.picked_up));
                                            btnCancelTrip.setText(context.getResources().getString(R.string.share));
                                            btnCancelTrip.setVisibility(View.GONE);
                                            AfterAcceptButtonLayout.setVisibility(View.VISIBLE);
                                            if (is_track.equalsIgnoreCase("YES")) {
                                                flowValue = 10;
                                                txtChange.setVisibility(View.VISIBLE);
                                            } else {
                                                flowValue = 4;
                                                txtChange.setVisibility(View.GONE);
                                            }
                                            layoutChanges();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case "DROPPED":
                                        setupRecyclerView();
                                        btnCancelRide.setVisibility(View.GONE);
                                        otpTxt.setVisibility(View.GONE);
                                        once = true;
                                        strTag = "";
                                        imgSos.setVisibility(View.VISIBLE);
                                        try {
                                            JSONObject provider = requestStatusCheckObject.optJSONObject("provider");
                                            if (requestStatusCheckObject.optJSONObject("payment") != null) {
                                                JSONObject payment = requestStatusCheckObject.optJSONObject("payment");
                                                isPaid = requestStatusCheckObject.optString("paid");
                                                totalRideAmount = payment.optInt("total");
                                                walletAmountDetected = payment.optInt("wallet");
                                                couponAmountDetected = payment.optInt("discount");
                                                paymentMode = requestStatusCheckObject.optString("payment_mode");
                                                lblDistanceCovered.setText(requestStatusCheckObject.optString("distance") + " KM");
                                                lblBasePrice.setText(SharedHelper.getKey(context, "currency") + "" + payment.optString("fixed"));
                                                lblTaxPrice.setText(SharedHelper.getKey(context, "currency") + "" + payment.optString("tax"));
                                                lblDistancePrice.setText(SharedHelper.getKey(context, "currency")
                                                        + "" + payment.optString("distance"));
                                                lblTimeTaken.setText(requestStatusCheckObject.optString("travel_time") + " mins");
                                                lblDiscountPrice.setText(SharedHelper.getKey(context, "currency") + "" + couponAmountDetected);
                                                lblWalletPrice.setText(SharedHelper.getKey(context, "currency") + "" + walletAmountDetected);
                                                //lblCommision.setText(SharedHelper.getKey(context, "currency") + "" + payment.optString("commision"));
                                                lblTotalPrice.setText(SharedHelper.getKey(context, "currency") + "" + payment.optString("total"));
                                                //Review values set
                                                lblProviderNameRate.setText(context.getResources().getString(R.string.rate_provider) + " " + provider.optString("first_name") + " " + provider.optString("last_name"));
                                                if (provider.optString("avatar").startsWith("http")) {
                                                    Picasso.with(context).load(provider.optString("avatar")).placeholder(R.drawable.loading).error(R.drawable.ic_dummy_user).into(imgProvider);
                                                } else {
                                                    Picasso.with(context).load(URLHelper.base + "storage/" + provider.optString("avatar")).placeholder(R.drawable.loading).error(R.drawable.ic_dummy_user).into(imgProvider);
                                                }
                                                if (requestStatusCheckObject.optString("booking_id") != null &&
                                                        !requestStatusCheckObject.optString("booking_id").equalsIgnoreCase("")) {
                                                    booking_id.setText(requestStatusCheckObject.optString("booking_id"));
                                                } else {
                                                    bookingIDLayout.setVisibility(View.GONE);
                                                    booking_id.setVisibility(View.GONE);
                                                }
                                                if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected > 0 && couponAmountDetected > 0 && totalRideAmount == 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.VISIBLE);
                                                    walletDetectionLayout.setVisibility(View.VISIBLE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.money_icon);
                                                    lblPaymentTypeInvoice.setText("CASH");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected > 0 && couponAmountDetected > 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.GONE);
                                                    walletDetectionLayout.setVisibility(View.VISIBLE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.money_icon);
                                                    lblPaymentTypeInvoice.setText("CASH");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CASH") && walletAmountDetected > 0
                                                        && couponAmountDetected == 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.GONE);
                                                    walletDetectionLayout.setVisibility(View.VISIBLE);
                                                    discountDetectionLayout.setVisibility(View.GONE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.money_icon);
                                                    lblPaymentTypeInvoice.setText("CASH");
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected > 0 && couponAmountDetected == 0 &&
                                                        totalRideAmount == 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.VISIBLE);
                                                    walletDetectionLayout.setVisibility(View.VISIBLE);
                                                    discountDetectionLayout.setVisibility(View.GONE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.money_icon);
                                                    lblPaymentTypeInvoice.setText("CASH");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected == 0 && couponAmountDetected > 0 &&
                                                        totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.GONE);
                                                    walletDetectionLayout.setVisibility(View.GONE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.money_icon);
                                                    lblPaymentTypeInvoice.setText("CASH");
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected == 0 && couponAmountDetected > 0 &&
                                                        totalRideAmount == 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.VISIBLE);
                                                    walletDetectionLayout.setVisibility(View.GONE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.money_icon);
                                                    lblPaymentTypeInvoice.setText("CASH");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected == 0 && couponAmountDetected == 0 &&
                                                        totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.GONE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.money_icon);
                                                    lblPaymentTypeInvoice.setText("CASH");
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected > 0 && couponAmountDetected > 0 && totalRideAmount == 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.VISIBLE);
                                                    walletDetectionLayout.setVisibility(View.VISIBLE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CARD");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected > 0 && couponAmountDetected > 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.VISIBLE);
                                                    btnPaymentDoneBtn.setVisibility(View.GONE);
                                                    walletDetectionLayout.setVisibility(View.VISIBLE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CARD");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected > 0 && couponAmountDetected == 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.VISIBLE);
                                                    btnPaymentDoneBtn.setVisibility(View.GONE);
                                                    walletDetectionLayout.setVisibility(View.VISIBLE);
                                                    discountDetectionLayout.setVisibility(View.GONE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CARD");
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CARD") && walletAmountDetected > 0 && couponAmountDetected == 0 && totalRideAmount == 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.VISIBLE);
                                                    walletDetectionLayout.setVisibility(View.VISIBLE);
                                                    discountDetectionLayout.setVisibility(View.GONE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CARD");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CARD") && walletAmountDetected == 0 && couponAmountDetected > 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.VISIBLE);
                                                    btnPaymentDoneBtn.setVisibility(View.GONE);
                                                    walletDetectionLayout.setVisibility(View.GONE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CARD");
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CARD") && walletAmountDetected == 0 && couponAmountDetected > 0 && totalRideAmount == 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.VISIBLE);
                                                    walletDetectionLayout.setVisibility(View.GONE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CARD");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected == 0 && couponAmountDetected == 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.VISIBLE);
                                                    btnPaymentDoneBtn.setVisibility(View.GONE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CARD");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected == 0 && couponAmountDetected > 0 && totalRideAmount == 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.VISIBLE);
                                                    walletDetectionLayout.setVisibility(View.GONE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CARD");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CASH") && walletAmountDetected == 0 && couponAmountDetected > 0 && totalRideAmount == 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.VISIBLE);
                                                    walletDetectionLayout.setVisibility(View.GONE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CASH");
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected > 0 && couponAmountDetected == 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    flowValue = 6;
                                                    layoutChanges();
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected == 0 && couponAmountDetected > 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    flowValue = 6;
                                                    layoutChanges();
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected == 0 && couponAmountDetected > 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    flowValue = 6;
                                                    layoutChanges();
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected > 0 && couponAmountDetected > 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    flowValue = 6;
                                                    layoutChanges();
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected > 0 && couponAmountDetected > 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    flowValue = 6;
                                                    layoutChanges();
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case "COMPLETED":
                                        setupRecyclerView();
                                        strTag = "";
                                        try {
                                            if (requestStatusCheckObject.optJSONObject("payment") != null) {
                                                JSONObject payment = requestStatusCheckObject.optJSONObject("payment");
                                                JSONObject provider = requestStatusCheckObject.optJSONObject("provider");
                                                isPaid = requestStatusCheckObject.optString("paid");
                                                paymentMode = requestStatusCheckObject.optString("payment_mode");
                                                imgSos.setVisibility(View.GONE);
                                                totalRideAmount = payment.optInt("total");
                                                walletAmountDetected = payment.optInt("wallet");
                                                couponAmountDetected = payment.optInt("discount");
                                                lblBasePrice.setText(SharedHelper.getKey(context, "currency") + ""
                                                        + payment.optString("fixed"));
                                                lblTaxPrice.setText(SharedHelper.getKey(context, "currency") + ""
                                                        + payment.optString("tax"));
                                                lblDistancePrice.setText(SharedHelper.getKey(context, "currency") + ""
                                                        + payment.optString("distance"));
                                                lblTotalPrice.setText(SharedHelper.getKey(context, "currency") + ""
                                                        + payment.optString("total"));
                                                lblDiscountPrice.setText(SharedHelper.getKey(context, "currency") + "" + couponAmountDetected);
                                                lblTimeTaken.setText(requestStatusCheckObject.optString("travel_time"));
                                                lblWalletPrice.setText(SharedHelper.getKey(context, "currency") + "" + walletAmountDetected);
                                                //Review values set
                                                lblProviderNameRate.setText(context.getResources().getString(R.string.rate_provider) + " " + provider.optString("first_name") + " " + provider.optString("last_name"));
                                                if (provider.optString("avatar").startsWith("http")) {
                                                    Picasso.with(context).load(provider.optString("avatar")).placeholder(R.drawable.loading).error(R.drawable.ic_dummy_user).into(imgProviderRate);
                                                } else {
                                                    Picasso.with(context).load(URLHelper.base + "storage/" + provider.optString("avatar")).placeholder(R.drawable.loading).error(R.drawable.ic_dummy_user).into(imgProviderRate);
                                                }
                                                if (requestStatusCheckObject.optString("booking_id") != null &&
                                                        !requestStatusCheckObject.optString("booking_id").equalsIgnoreCase("")) {
                                                    booking_id.setText(requestStatusCheckObject.optString("booking_id"));
                                                } else {
                                                    bookingIDLayout.setVisibility(View.GONE);
                                                    booking_id.setVisibility(View.GONE);
                                                }
                                                if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected > 0 && couponAmountDetected > 0
                                                        && totalRideAmount == 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.VISIBLE);
                                                    walletDetectionLayout.setVisibility(View.VISIBLE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.money_icon);
                                                    lblPaymentTypeInvoice.setText("CASH");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected > 0 && couponAmountDetected > 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.GONE);
                                                    walletDetectionLayout.setVisibility(View.VISIBLE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.money_icon);
                                                    lblPaymentTypeInvoice.setText("CASH");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected > 0 && couponAmountDetected == 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.GONE);
                                                    walletDetectionLayout.setVisibility(View.VISIBLE);
                                                    discountDetectionLayout.setVisibility(View.GONE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.money_icon);
                                                    lblPaymentTypeInvoice.setText("CASH");
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected > 0 && couponAmountDetected == 0 && totalRideAmount == 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.VISIBLE);
                                                    walletDetectionLayout.setVisibility(View.VISIBLE);
                                                    discountDetectionLayout.setVisibility(View.GONE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.money_icon);
                                                    lblPaymentTypeInvoice.setText("CASH");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected == 0 && couponAmountDetected > 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.GONE);
                                                    walletDetectionLayout.setVisibility(View.GONE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.money_icon);
                                                    lblPaymentTypeInvoice.setText("CASH");
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected == 0 && couponAmountDetected > 0 && totalRideAmount == 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.VISIBLE);
                                                    walletDetectionLayout.setVisibility(View.GONE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.money_icon);
                                                    lblPaymentTypeInvoice.setText("CASH");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected == 0 && couponAmountDetected == 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.GONE);
                                                    walletDetectionLayout.setVisibility(View.GONE);
                                                    discountDetectionLayout.setVisibility(View.GONE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.money_icon);
                                                    lblPaymentTypeInvoice.setText("CASH");
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected == 0 && couponAmountDetected == 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    flowValue = 6;
                                                    layoutChanges();
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected > 0 && couponAmountDetected > 0 && totalRideAmount == 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.VISIBLE);
                                                    walletDetectionLayout.setVisibility(View.VISIBLE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CARD");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected > 0 && couponAmountDetected > 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.VISIBLE);
                                                    btnPaymentDoneBtn.setVisibility(View.GONE);
                                                    walletDetectionLayout.setVisibility(View.VISIBLE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CARD");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected > 0 && couponAmountDetected == 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.VISIBLE);
                                                    btnPaymentDoneBtn.setVisibility(View.GONE);
                                                    walletDetectionLayout.setVisibility(View.VISIBLE);
                                                    discountDetectionLayout.setVisibility(View.GONE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CARD");
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected > 0 && couponAmountDetected == 0 && totalRideAmount == 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.VISIBLE);
                                                    walletDetectionLayout.setVisibility(View.VISIBLE);
                                                    discountDetectionLayout.setVisibility(View.GONE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CARD");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected == 0 && couponAmountDetected > 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.VISIBLE);
                                                    btnPaymentDoneBtn.setVisibility(View.GONE);
                                                    walletDetectionLayout.setVisibility(View.GONE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CARD");
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected == 0 && couponAmountDetected > 0 && totalRideAmount == 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.VISIBLE);
                                                    walletDetectionLayout.setVisibility(View.GONE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CARD");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CARD") && walletAmountDetected == 0 && couponAmountDetected == 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.VISIBLE);
                                                    btnPaymentDoneBtn.setVisibility(View.GONE);
                                                    walletDetectionLayout.setVisibility(View.GONE);
                                                    discountDetectionLayout.setVisibility(View.GONE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CARD");
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected == 0 && couponAmountDetected == 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    flowValue = 6;
                                                    layoutChanges();
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected == 0 && couponAmountDetected > 0 && totalRideAmount == 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.VISIBLE);
                                                    walletDetectionLayout.setVisibility(View.GONE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CARD");
                                                } else if (isPaid.equalsIgnoreCase("0") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected == 0 && couponAmountDetected > 0 && totalRideAmount == 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    btnPaymentDoneBtn.setVisibility(View.VISIBLE);
                                                    walletDetectionLayout.setVisibility(View.GONE);
                                                    discountDetectionLayout.setVisibility(View.VISIBLE);
                                                    flowValue = 5;
                                                    layoutChanges();
                                                    imgPaymentTypeInvoice.setImageResource(R.drawable.visa);
                                                    lblPaymentTypeInvoice.setText("CASH");
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected > 0 && couponAmountDetected == 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    flowValue = 6;
                                                    layoutChanges();
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected > 0 && couponAmountDetected == 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    flowValue = 6;
                                                    layoutChanges();
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected == 0 && couponAmountDetected > 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    flowValue = 6;
                                                    layoutChanges();
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected == 0 && couponAmountDetected > 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    flowValue = 6;
                                                    layoutChanges();
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CASH")
                                                        && walletAmountDetected > 0 && couponAmountDetected > 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    flowValue = 6;
                                                    layoutChanges();
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected > 0 && couponAmountDetected > 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    flowValue = 6;
                                                    layoutChanges();
                                                } else if (isPaid.equalsIgnoreCase("1") && paymentMode.equalsIgnoreCase("CARD")
                                                        && walletAmountDetected > 0 && couponAmountDetected == 0 && totalRideAmount > 0) {
                                                    btnPayNow.setVisibility(View.GONE);
                                                    flowValue = 6;
                                                    layoutChanges();
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                }
                            }
                            if ("ACCEPTED".equals(status) || "STARTED".equals(status) ||
                                    "ARRIVED".equals(status) || "PICKEDUP".equals(status) || "DROPPED".equals(status)) {
                                Utilities.print("Livenavigation", "" + status);
                                Utilities.print("Destination Current Lat", "" + requestStatusCheckObject.getJSONObject("provider").optString("latitude"));
                                Utilities.print("Destination Current Lng", "" + requestStatusCheckObject.getJSONObject("provider").optString("longitude"));
                                liveNavigation(status, requestStatusCheckObject.getJSONObject("provider").optString("latitude"),
                                        requestStatusCheckObject.getJSONObject("provider").optString("longitude"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.something_went_wrong));
                        }
                    } else if (PreviousStatus.equalsIgnoreCase("SEARCHING")) {
                        SharedHelper.putKey(context, "current_status", "");
                        if (scheduledDate == null || scheduledTime == null || scheduledDate.equalsIgnoreCase("")
                                || scheduledTime.equalsIgnoreCase("")) {
                            Toast.makeText(context, context.getResources().getString(R.string.no_drivers_found), Toast.LENGTH_SHORT).show();
                        }
                        strTag = "";
                        PreviousStatus = "";
                        flowValue = 0;
                        layoutChanges();
                        if (reasonDialog != null) {
                            if (reasonDialog.isShowing()) {
                                reasonDialog.dismiss();
                            }
                        }
                        if (cancelRideDialog != null) {
                            if (cancelRideDialog.isShowing()) {
                                cancelRideDialog.dismiss();
                            }
                        }
                        CurrentStatus = "";
                        mMap.clear();
                        mapClear();
                    } else if (PreviousStatus.equalsIgnoreCase("STARTED")) {
                        SharedHelper.putKey(context, "current_status", "");
                        Toast.makeText(context, context.getResources().getString(R.string.driver_busy), Toast.LENGTH_SHORT).show();
                        strTag = "";
                        PreviousStatus = "";
                        flowValue = 0;
                        layoutChanges();
                        if (reasonDialog != null) {
                            if (reasonDialog.isShowing()) {
                                reasonDialog.dismiss();
                            }
                        }
                        if (cancelRideDialog != null) {
                            if (cancelRideDialog.isShowing()) {
                                cancelRideDialog.dismiss();
                            }
                        }
                        CurrentStatus = "";
                        mMap.clear();
                        mapClear();
                    } else if (PreviousStatus.equalsIgnoreCase("ARRIVED")) {
                        setupRecyclerView();
                        btnCancelRide.setVisibility(View.GONE);
                        SharedHelper.putKey(context, "current_status", "");
                        Toast.makeText(context, context.getResources().getString(R.string.driver_busy), Toast.LENGTH_SHORT).show();
                        strTag = "";
                        PreviousStatus = "";
                        flowValue = 0;
                        layoutChanges();
                        if (reasonDialog != null) {
                            if (reasonDialog.isShowing()) {
                                reasonDialog.dismiss();
                            }
                        }
                        if (cancelRideDialog != null) {
                            if (cancelRideDialog.isShowing()) {
                                cancelRideDialog.dismiss();
                            }
                        }
                        CurrentStatus = "";
                        mMap.clear();
                        mapClear();
                    } else {
                        if (flowValue == 0) {
                            getProvidersList("");
                        } else if (flowValue == 1) {
                            getProvidersList(SharedHelper.getKey(context, "service_type"));
                        }
                        CurrentStatus = "";
                    }
                }, error -> {
                    Utilities.print("Error", error.toString());
                    try {
                        if (customDialog != null && customDialog.isShowing()) {
                            customDialog.dismiss();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    reqStatus = "";
                    SharedHelper.putKey(context, "req_status", "");
                }) {
                    @Override
                    public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("X-Requested-With", "XMLHttpRequest");
                        headers.put("Authorization", "" + SharedHelper.getKey(context, "token_type") + " " + SharedHelper.getKey(context, "access_token"));
                        return headers;
                    }
                };
                MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
            } else {
                Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.oops_connect_your_internet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTrackStatus() {

    }

    private void mapClear() {
        if (parserTask != null) {
            parserTask.cancel(true);
        }
        if (fetchUrl != null) {
            fetchUrl.cancel(true);
        }
        mMap.clear();
        dest_lat = "";
        dest_lng = "";
        if (!current_lat.equalsIgnoreCase("") && !current_lng.equalsIgnoreCase("")) {
            LatLng myLocation = new LatLng(Double.parseDouble(current_lat), Double.parseDouble(current_lng));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(14).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    public void reCreateMap() {
        if (mMap != null) {
            if (!source_lat.equalsIgnoreCase("") && !source_lng.equalsIgnoreCase("")) {
                sourceLatLng = new LatLng(Double.parseDouble(source_lat), Double.parseDouble(source_lng));
            }
            if (!dest_lat.equalsIgnoreCase("") && !dest_lng.equalsIgnoreCase("")) {
                destLatLng = new LatLng(Double.parseDouble(dest_lat), Double.parseDouble(dest_lng));
            }
            Utilities.print("LatLng", "Source:" + sourceLatLng + " Destination: " + destLatLng);
            //String url = getDirectionsUrl(sourceLatLng, destLatLng);
            String url = getUrl(sourceLatLng.latitude, sourceLatLng.longitude, destLatLng.latitude, destLatLng.longitude);
            fetchUrl = new FetchUrl();
            fetchUrl.execute(url);
           /* DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);*/
        }
    }

    private void show(final View view) {
        mIsShowing = true;
        ViewPropertyAnimator animator = view.animate()
                .translationY(0)
                .setInterpolator(INTERPOLATOR)
                .setDuration(500);
        animator.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mIsShowing = false;
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                // Canceling a show should hide the view
                mIsShowing = false;
                if (!mIsHiding) {
                    hide(view);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        animator.start();
    }

    private void hide(final View view) {
        mIsHiding = true;
        ViewPropertyAnimator animator = view.animate()
                .translationY(view.getHeight())
                .setInterpolator(INTERPOLATOR)
                .setDuration(200);
        animator.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                // Prevent drawing the View after it is gone
                mIsHiding = false;
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                // Canceling a hide should show the view
                mIsHiding = false;
                if (!mIsShowing) {
                    show(view);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        animator.start();
    }

    public void liveNavigation(String status, String lat, String lng) {
        Log.e("Livenavigation", "ProLat" + lat + " ProLng" + lng);
        if (!lat.equalsIgnoreCase("") && !lng.equalsIgnoreCase("")) {
            double proLat = Double.parseDouble(lat);
            double proLng = Double.parseDouble(lng);
            float rotation = 0.0f;
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(proLat, proLng))
                    .rotation(rotation)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_marker));
            if (providerMarker != null) {
                rotation = getBearing(providerMarker.getPosition(), markerOptions.getPosition());
                markerOptions.rotation(rotation * (180.0f / (float) Math.PI));
                providerMarker.remove();
            }
            providerMarker = mMap.addMarker(markerOptions);
        }
    }

    public float getBearing(LatLng oldPosition, LatLng newPosition) {
        double deltaLongitude = newPosition.longitude - oldPosition.longitude;
        double deltaLatitude = newPosition.latitude - oldPosition.latitude;
        double angle = (Math.PI * .5f) - Math.atan(deltaLatitude / deltaLongitude);
        if (deltaLongitude > 0) {
            return (float) angle;
        } else if (deltaLongitude < 0) {
            return (float) (angle + Math.PI);
        } else if (deltaLatitude < 0) {
            return (float) Math.PI;
        }
        return 0.0f;
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enableLoc();
        }
    }

    private void enableLoc() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d("Location error", "Connected");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        mGoogleApiClient.connect();
                    }
                })
                .addOnConnectionFailedListener(connectionResult -> Log.d("Location error", "Location error " + connectionResult.getErrorCode())).build();
        mGoogleApiClient.connect();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            Log.e("GPS Location", "onResult: " + result1);
            Log.e("GPS Location", "onResult Status: " + result1.getStatus());
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(getActivity(), REQUEST_LOCATION);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.CANCELED:
                    showDialogForGPSIntent();
                    break;
            }
        });
    }

    public void submitReviewCall() {
        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("request_id", SharedHelper.getKey(context, "request_id"));
            object.put("rating", feedBackRating);
            object.put("comment", "" + txtCommentsRate.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.RATE_PROVIDER_API, object, response -> {
            Utilities.print("SubmitRequestResponse", response.toString());
            Utilities.hideKeypad(context, activity.getCurrentFocus());
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            destination.setText("");
            frmDest.setText("");
            mapClear();
            flowValue = 0;
            layoutChanges();
            if (!current_lat.equalsIgnoreCase("") && !current_lng.equalsIgnoreCase("")) {
                LatLng myLocation = new LatLng(Double.parseDouble(current_lat), Double.parseDouble(current_lng));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(14).build();
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
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
                            Utilities.displayMessage(getView(), context, errorObj.optString("message"));
                        } catch (Exception e) {
                            Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.something_went_wrong));
                        }
                    } else if (response.statusCode == 401) {
                        refreshAccessToken("SUBMIT_REVIEW");
                    } else if (response.statusCode == 422) {
                        json = MyApplication.trimMessage(new String(response.data));
                        if (json != null && !json.equals("")) {
                            Utilities.displayMessage(getView(), context, json);
                        } else {
                            Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.please_try_again));
                        }
                    } else if (response.statusCode == 503) {
                        Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.server_down));
                    } else {
                        Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.please_try_again));
                    }
                } catch (Exception e) {
                    Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.something_went_wrong));
                }
            } else {
                Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.please_try_again));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "" + SharedHelper.getKey(context, "token_type") + " " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };
        MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void startAnim(ArrayList<LatLng> routeList) {
        if (mMap != null && routeList.size() > 1) {
            MapAnimator.getInstance().animateRoute(context, mMap, routeList);
        }
    }

    @Override
    public void onDestroy() {
        handleCheckStatus.removeCallbacksAndMessages(null);
//        if (mapRipple != null && mapRipple.isAnimationRunning()) {
//            mapRipple.stopRippleMapAnimation();
//        }
        super.onDestroy();
    }

    private void stopAnim() {
        if (mMap != null) {
            MapAnimator.getInstance().stopAnim();
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(context.getResources().getString(R.string.connect_to_network))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.connect_to_wifi), (dialog, id) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));
        if (alert == null) {
            alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("downloadUrl", data);
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private String getUrl(double source_latitude, double source_longitude, double dest_latitude, double dest_longitude) {
        // Origin of route
        String str_origin = "origin=" + source_latitude + "," + source_longitude;
        // Destination of route
        String str_dest = "";
        StringBuilder wayPoints = new StringBuilder();
        if (!statusArrayList.isEmpty()) {
            if (statusArrayList.size() > 1) {
                str_dest = "destination=" + statusArrayList.get(statusArrayList.size() - 1).getD_lat()
                        + "," + statusArrayList.get(statusArrayList.size() - 1).getD_long();
                wayPoints.append("waypoints=");
                for (int i = 0; i < statusArrayList.size(); i++) {
                    if (i != statusArrayList.size() - 1)
                        wayPoints.append("via=").append(statusArrayList.get(i).getdeliveryAddress());
                }
            } else {
                str_dest = "destination=" + statusArrayList.get(0).getD_lat() + "," + statusArrayList.get(0).getD_long();
            }
        } else if (!location_array.isEmpty()) {
            if (location_array.size() > 1) {
                str_dest = "destination=" + location_array.get(location_array.size() - 1).getdLatitude()
                        + "," + location_array.get(location_array.size() - 1).getdLongitude();
                wayPoints.append("waypoints=");
                for (int i = 0; i < location_array.size(); i++) {
                    if (i != location_array.size() - 1)
                        wayPoints.append("via=").append(location_array.get(i).getdAddress());
                }
            } else {
                str_dest = "destination=" + location_array.get(0).getdLatitude() + "," + location_array.get(0).getdLongitude();
            }
        }
        // Sensor enabled
//        String key = "key=" + getString(R.string.google_api_key);
        String key = "key=AIzaSyD39WhzpsRC_EOO1OfD4lyA55Ld2f7GhYk";
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + wayPoints + "&" + sensor + "&" + key;
        // Output format
        String output = "json";
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!SharedHelper.getKey(context, "wallet_balance").equalsIgnoreCase("")) {
            wallet_balance = Double.parseDouble(SharedHelper.getKey(context, "wallet_balance"));
        }
        if (lnrHome != null && lnrWork != null) {
            getFavoriteLocations();
        }
        if (!Double.isNaN(wallet_balance) && wallet_balance > 0) {
            if (lineView != null && chkWallet != null) {
                lineView.setVisibility(View.VISIBLE);
                chkWallet.setVisibility(View.VISIBLE);
            }
        } else {
            if (lineView != null && chkWallet != null) {
                lineView.setVisibility(View.GONE);
                chkWallet.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void getJSONArrayResult(String strTag, JSONArray response) {
        if (strTag.equalsIgnoreCase("Get Services")) {
            Utilities.print("GetServices", response.toString());
            if (SharedHelper.getKey(context, "service_type").equalsIgnoreCase("")) {
                SharedHelper.putKey(context, "service_type", "" + response.optJSONObject(0).optString("id"));
            }
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            if (response.length() > 0) {
                currentPostion = 0;
                ServiceListAdapter serviceListAdapter = new ServiceListAdapter(response);
                rcvServiceTypes.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
                rcvServiceTypes.setAdapter(serviceListAdapter);
                getProvidersList(SharedHelper.getKey(context, "service_type"));
            }
            if (mMap != null) {
                mMap.clear();
            }
            setValuesForSourceAndDestination();
        }
    }

    private void setCurrentAddress() {
        Utilities.getAddressUsingLatLng("source", frmSource, context, "" + current_lat, "" + current_lng);
    }

    public void setHomeWorkAddress(String strTag) {
        if (strTag.equalsIgnoreCase("home")) {
            dest_lat = "" + SharedHelper.getKey(activity, "home_lat");
            dest_lng = "" + SharedHelper.getKey(activity, "home_lng");
            dest_address = "" + SharedHelper.getKey(activity, "home");
        } else {
            dest_lat = "" + SharedHelper.getKey(activity, "work_lat");
            dest_lng = "" + SharedHelper.getKey(activity, "work_lng");
            dest_address = "" + SharedHelper.getKey(activity, "work");
        }
        String strSourceAdd = frmSource.getText().toString();
        String strDestAdd = frmDest.getText().toString();
        if (!strSourceAdd.equalsIgnoreCase(dest_address)) {
            frmDest.setText(dest_address);
            SharedHelper.putKey(context, "current_status", "2");
            if (source_lat != null && source_lng != null && !source_lng.equalsIgnoreCase("")
                    && !source_lat.equalsIgnoreCase("")) {
                try {
                    String url = getUrl(Double.parseDouble(source_lat), Double.parseDouble(source_lng)
                            , Double.parseDouble(dest_lat), Double.parseDouble(dest_lng));
                    fetchUrl = new FetchUrl();
                    fetchUrl.execute(url);
                    LatLng location = new LatLng(Double.parseDouble(current_lat), Double.parseDouble(current_lng));

                    if (sourceMarker != null)
                        sourceMarker.remove();
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(location).snippet(frmSource.getText().toString())
                            .title("source")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker));
                    marker = mMap.addMarker(markerOptions);
                    sourceMarker = mMap.addMarker(markerOptions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for (int i = 0; i < location_array.size(); i++) {
                LatLng latLng = new LatLng(Double.parseDouble(location_array.get(i).getdLatitude()), Double.parseDouble(location_array.get(i).getdLongitude()));
                MarkerOptions destMarker = new MarkerOptions()
                        .position(latLng).title("destination " + i).snippet(location_array.get(i).getdAddress())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.provider_marker));
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(mMap.addMarker(destMarker).getPosition());
            }
            /*if (!dest_lat.equalsIgnoreCase("") && !dest_lng.equalsIgnoreCase("")) {
                try {
                    destLatLng = new LatLng(Double.parseDouble(dest_lat), Double.parseDouble(dest_lng));
                    if (destinationMarker != null)
                        destinationMarker.remove();
                    MarkerOptions destMarker = new MarkerOptions()
                            .position(destLatLng).title("destination").snippet(frmDest.getText().toString())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.provider_marker));
                    destinationMarker = mMap.addMarker(destMarker);
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(sourceMarker.getPosition());
                    builder.include(destinationMarker.getPosition());
                    LatLngBounds bounds = builder.build();
                    int padding = 150; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.moveCamera(cu);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/
            if (dest_address.equalsIgnoreCase("")) {
                flowValue = 1;
//                            frmSource.setText(source_address);
                getServiceList();
            } else {
                flowValue = 1;
                if (cardInfoArrayList.size() > 0) {
                    getCardDetailsForPayment(cardInfoArrayList.get(0));
                }
                getServiceList();
            }
            layoutChanges();
        } else {
            Toast.makeText(context, activity.getResources().getString(R.string.source_dest_not_same), Toast.LENGTH_SHORT).show();
        }
    }

    public void gotoCurrentPosition() {
        if (!current_lat.equalsIgnoreCase("") && !current_lng.equalsIgnoreCase("")) {
            LatLng myLocation = new LatLng(Double.parseDouble(current_lat), Double.parseDouble(current_lng));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(14).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private void getFavoriteLocations() {
        ApiInterface mApiInterface = RetrofitClient.getLiveTrackingClient().create(ApiInterface.class);
        Call<ResponseBody> call = mApiInterface.getFavoriteLocations("XMLHttpRequest",
                SharedHelper.getKey(context, "token_type") + " " + SharedHelper.getKey(context, "access_token"));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.e("sUCESS", "SUCESS" + response.body());
                if (response.body() != null) {
                    try {
                        String bodyString = new String(response.body().bytes());
                        Log.e("sUCESS", "bodyString" + bodyString);
                        try {
                            JSONObject jsonObj = new JSONObject(bodyString);
                            JSONArray homeArray = jsonObj.optJSONArray("home");
                            JSONArray workArray = jsonObj.optJSONArray("work");
                            JSONArray othersArray = jsonObj.optJSONArray("others");
                            JSONArray recentArray = jsonObj.optJSONArray("recent");
                            if (homeArray.length() > 0) {
                                lnrHome.setVisibility(View.VISIBLE);
                                SharedHelper.putKey(context, "home", homeArray.optJSONObject(0).optString("address"));
                            } else {
                                lnrHome.setVisibility(View.GONE);
                            }
                            if (workArray.length() > 0) {
                                lnrWork.setVisibility(View.VISIBLE);
                                SharedHelper.putKey(context, "work", workArray.optJSONObject(0).optString("address"));
                            } else {
                                lnrWork.setVisibility(View.GONE);
                            }
                            if (flowValue == 0) {
                                if (lnrHome.getVisibility() == View.GONE && lnrWork.getVisibility() == View.GONE) {
                                    lnrHomeWork.setVisibility(View.GONE);
                                } else {
                                    lnrHomeWork.setVisibility(View.VISIBLE);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("onFailure", "onFailure" + call.request().url());
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView_tripstatus.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        if (statusAdapter == null) {
            statusAdapter = new StatusAdapter(statusArrayList, activity);
            recyclerView_tripstatus.setAdapter(statusAdapter);
        } else {
            refreshAdapter();
        }
        statusAdapter.setStatusadapterListener(this);
    }

    @Override
    public void onTrackbtn(TripStatus statusflow) {
        status.setVisibility(View.GONE);
        mapClear();
        String url = getUrl(Double.parseDouble(source_lat), Double.parseDouble(source_lng)
                , Double.parseDouble(statusflow.getD_lat()), Double.parseDouble(statusflow.getD_long()));
        fetchUrl = new FetchUrl();
        fetchUrl.execute(url);
    }

    public void displayMessage(String toastString) {
        Log.e("displayMessage", "" + toastString);
        try {
            Snackbar.make(getView(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            try {
                Toast.makeText(context, "" + toastString, Toast.LENGTH_SHORT).show();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    public void refreshAdapter() {
        statusAdapter.setListModels(statusArrayList);
        statusAdapter.notifyDataSetChanged();
    }

    private void openPhoto(String url) {
        androidx.appcompat.app.AlertDialog.Builder dialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_open_photo, null);
        dialogBuilder.setView(dialogView);
        final ImageView ivPhoto = dialogView.findViewById(R.id.ivPhoto);
        final TextView tvClose = dialogView.findViewById(R.id.tvClose);
        androidx.appcompat.app.AlertDialog alertDialog = dialogBuilder.create();
        Glide.with(activity).load(URLHelper.base + "/asset/home/" + url)
                .apply(new RequestOptions().placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)).into(ivPhoto);
        tvClose.setOnClickListener(v -> alertDialog.dismiss());
        alertDialog.show();
    }

    public interface HomeFragmentListener {
    }

    class OnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnHome:
                    if (!SharedHelper.getKey(context, "home").equalsIgnoreCase("")) {
                        setHomeWorkAddress("home");
                    }
                    break;
                case R.id.btnWork:
                    if (!SharedHelper.getKey(context, "home").equalsIgnoreCase("")) {
                        setHomeWorkAddress("work");
                    }
                    break;
                case R.id.frmSource:
                    Intent intent = new Intent(getActivity(), CustomGooglePlacesSearch.class);
                    intent.putExtra("cursor", "source");
                    intent.putExtra("s_address", frmSource.getText().toString());
                    intent.putExtra("d_address", frmDest.getText().toString());
                    //startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST);
                    break;
                case R.id.frmDestination:
                    if (CurrentStatus.equalsIgnoreCase("")) {
                        Intent intent2 = new Intent(getActivity(), LocationAndGoodsActivity.class);
                        intent2.putExtra("s_address", frmSource.getText().toString());
                        intent2.putExtra("d_address", frmDest.getText().toString());
                        startActivityForResult(intent2, LOCATION);
                    }
                    break;
                case R.id.txtChange:
                    Intent intent4 = new Intent(getActivity(), CustomGooglePlacesSearch.class);
                    intent4.putExtra("cursor", "destination");
                    intent4.putExtra("s_address", frmSource.getText().toString());
                    intent4.putExtra("d_address", frmDest.getText().toString());
                    //startActivityForResult(intent4, PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST);
                    break;
                case R.id.frmDest:
                    Intent intent3 = new Intent(getActivity(), CustomGooglePlacesSearch.class);
                    intent3.putExtra("cursor", "destination");
                    intent3.putExtra("s_address", frmSource.getText().toString());
                    intent3.putExtra("d_address", destination.getText().toString());
                    intent3.putExtra("d_address", frmDest.getText().toString());
                    //startActivityForResult(intent3, PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST);
                    break;
                case R.id.lblPaymentChange:
                    showChooser();
                    break;
                case R.id.btnRequestRides:
                    scheduledDate = "";
                    scheduledTime = "";
                    if (!frmSource.getText().toString().equalsIgnoreCase("") &&
                            !frmDest.getText().toString().equalsIgnoreCase("")) {
                        getApproximateFare();
                        source_address_txt.setText(frmSource.getText().toString());
                        frmDest.setOnClickListener(null);
                        frmSource.setOnClickListener(null);
                        srcDestLayout.setOnClickListener(new OnClick());
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.pickup_drop), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.backArrow:
                    status.setVisibility(View.GONE);
                    break;
                case R.id.status_info:
                    status.setVisibility(View.VISIBLE);
                    break;
                case R.id.btnRequestRideConfirm:
                    SharedHelper.putKey(context, "name", "");
                    scheduledDate = "";
                    scheduledTime = "";
                    sendRequest();
                    break;
                case R.id.btnPayNow:
                    payNow();
                    break;
                case R.id.btnPaymentDoneBtn:
                    btnPayNow.setVisibility(View.GONE);
                    btnPaymentDoneBtn.setVisibility(View.GONE);
                    flowValue = 6;
                    layoutChanges();
                    break;
                case R.id.btnSubmitReview:
                    submitReviewCall();
                    break;
                case R.id.lnrHidePopup:
                case R.id.btnDonePopup:
                    lnrHidePopup.setVisibility(View.GONE);
                    flowValue = 1;
                    layoutChanges();
                    click = 1;
                    break;
                case R.id.btnCancelRide:
                    showCancelRideDialog();
                    break;
                case R.id.btnCancelTrip:
                    if (btnCancelTrip.getText().toString().equals(context.getResources().getString(R.string.cancel_trip)))
                        showCancelRideDialog();
                    else {
                        String shareUrl = URLHelper.REDIRECT_SHARE_URL;
                        navigateToShareScreen(shareUrl);
                    }
                    break;
                case R.id.imgSos:
                    showSosPopUp();
                    break;
                case R.id.chat:
                    Intent i = new Intent(context, ChatActivity.class);
                    i.putExtra("request_id", SharedHelper.getKey(context, "request_id"));
                    i.putExtra("user_id", userId);
                    i.putExtra("provider_id", providerId);
                    startActivity(i);
                    break;
                case R.id.imgShareRide:
                    String url = "http://maps.google.com/maps?q=loc:";
                    navigateToShareScreen(url);
                    break;
                case R.id.imgProvider:
                    Intent intent1 = new Intent(activity, ShowProfile.class);
                    intent1.putExtra("driver", driver);
                    startActivity(intent1);
                    break;
                case R.id.imgProviderRate:
                    Intent intent5 = new Intent(activity, ShowProfile.class);
                    intent5.putExtra("driver", driver);
                    startActivity(intent5);
                    break;
                case R.id.btnCall:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 2);
                    } else {
                        Intent intentCall = new Intent(Intent.ACTION_CALL);
                        intentCall.setData(Uri.parse("tel:" + SharedHelper.getKey(context, "provider_mobile_no")));
                        startActivity(intentCall);
                    }
                    break;
                case R.id.btnDone:
                    if (is_track.equalsIgnoreCase("YES") &&
                            (CurrentStatus.equalsIgnoreCase("STARTED") || CurrentStatus.equalsIgnoreCase("PICKEDUP")
                                    || CurrentStatus.equalsIgnoreCase("ARRIVED"))) {
                        extend_dest_lat = "" + cmPosition.target.latitude;
                        extend_dest_lng = "" + cmPosition.target.longitude;
                        showTripExtendAlert(extend_dest_lat, extend_dest_lng);
                    } else {
                        pick_first = true;
                        try {
                            Utilities.print("centerLat", cmPosition.target.latitude + "");
                            Utilities.print("centerLong", cmPosition.target.longitude + "");
                            if (strPickType.equalsIgnoreCase("source")) {
                                source_address = Utilities.getAddressUsingLatLng("source", frmSource, context, "" + cmPosition.target.latitude, "" + cmPosition.target.longitude);
                                source_lat = "" + cmPosition.target.latitude;
                                source_lng = "" + cmPosition.target.longitude;
                                source_address_txt.setText(source_address);
                                if (dest_lat.equalsIgnoreCase("")) {
                                    Toast.makeText(context, "Select destination", Toast.LENGTH_SHORT).show();
                                    Intent intentDest = new Intent(getActivity(), CustomGooglePlacesSearch.class);
                                    intentDest.putExtra("cursor", "destination");
                                    intentDest.putExtra("s_address", source_address);
                                    startActivityForResult(intentDest, PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST);
                                } else {
                                    source_lat = "" + cmPosition.target.latitude;
                                    source_lng = "" + cmPosition.target.longitude;
                                    mMap.clear();
                                    flowValue = 1;
                                    layoutChanges();
                                    strPickLocation = "";
                                    strPickType = "";
                                    getServiceList();
                                    CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(cmPosition.target.latitude,
                                            cmPosition.target.longitude));
                                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(14);
                                    mMap.moveCamera(center);
                                    mMap.moveCamera(zoom);
                                }
                            } else {
                                dest_lat = "" + cmPosition.target.latitude;
                                if (dest_lat.equalsIgnoreCase(source_lat)) {
                                    Toast.makeText(context, activity.getResources().getString(R.string.source_dest_not_same), Toast.LENGTH_SHORT).show();
                                    Intent intentDest = new Intent(getActivity(), CustomGooglePlacesSearch.class);
                                    intentDest.putExtra("cursor", "destination");
                                    intentDest.putExtra("s_address", frmSource.getText().toString());
                                    //startActivityForResult(intentDest, PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST);
                                } else {
                                    dest_address = Utilities.getAddressUsingLatLng("destination", frmDest, context, "" + cmPosition.target.latitude, "" + cmPosition.target.longitude);
                                    dest_lat = "" + cmPosition.target.latitude;
                                    dest_lng = "" + cmPosition.target.longitude;
                                    mMap.clear();
                                    flowValue = 1;
                                    layoutChanges();
                                    strPickLocation = "";
                                    strPickType = "";
                                    getServiceList();
                                    CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(cmPosition.target.latitude,
                                            cmPosition.target.longitude));
                                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(14);
                                    mMap.moveCamera(center);
                                    mMap.moveCamera(zoom);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Can't able to get the address!.Please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case R.id.imgBack:
                    if (lnrRequestProviders.getVisibility() == View.VISIBLE) {
                        flowValue = 0;
                        srcDestLayout.setVisibility(View.GONE);
                        frmSource.setOnClickListener(new OnClick());
                        frmDest.setOnClickListener(new OnClick());
                        srcDestLayout.setOnClickListener(null);
                        if (!current_lat.equalsIgnoreCase("") && !current_lng.equalsIgnoreCase("")) {
                            destinationBorderImg.setVisibility(View.VISIBLE);
                            //verticalView.setVisibility(View.GONE);
                            LatLng myLocation = new LatLng(Double.parseDouble(current_lat), Double.parseDouble(current_lng));
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(14).build();
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            srcDestLayout.setVisibility(View.GONE);
                        }
                    } else if (lnrApproximate.getVisibility() == View.VISIBLE) {
                        frmSource.setOnClickListener(new OnClick());
                        frmDest.setOnClickListener(new OnClick());
                        srcDestLayout.setOnClickListener(null);
                        flowValue = 1;
                    } else if (lnrWaitingForProviders.getVisibility() == View.VISIBLE) {
                        flowValue = 2;
                    } else if (ScheduleLayout.getVisibility() == View.VISIBLE) {
                        flowValue = 2;
                    }
                    layoutChanges();
                    break;
                case R.id.imgMenu:
                    try {
                        if (NAV_DRAWER == 0) {
                            if (drawer != null)
                                drawer.openDrawer(GravityCompat.START);
                        } else {
                            NAV_DRAWER = 0;
                            if (drawer != null)
                                drawer.closeDrawers();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.mapfocus:
                    double crtLat, crtLng;
                    if (!current_lat.equalsIgnoreCase("") && !current_lng.equalsIgnoreCase("")) {
                        crtLat = Double.parseDouble(current_lat);
                        crtLng = Double.parseDouble(current_lng);
                        LatLng loc = new LatLng(crtLat, crtLng);
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(loc).zoom(14).build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        mapfocus.setVisibility(View.INVISIBLE);
                    }
                    break;
                case R.id.imgSchedule:
                    flowValue = 7;
                    layoutChanges();
                    break;
                case R.id.imgGotoPhoto:
                    if (statusArrayList.get(currentPostion).getAfterImage() != null &&
                            !statusArrayList.get(currentPostion).getAfterImage().equals(""))
                        openPhoto(statusArrayList.get(currentPostion).getAfterImage());
                    break;
                case R.id.scheduleBtn:
                    SharedHelper.putKey(context, "name", "");
                    if (!scheduledDate.equals("") && !scheduledTime.equals("")) {
                        Date date = null;
                        try {
                            date = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(scheduledDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        long milliseconds = date.getTime();
                        if (!DateUtils.isToday(milliseconds)) {
                            sendRequest();
                        } else {
                            if (Utilities.checktimings(scheduledTime)) {
                                sendRequest();
                            } else {
                                Toast.makeText(activity, context.getResources().getString(R.string.different_time), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(activity, context.getResources().getString(R.string.choose_date_time), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.scheduleDate:
                    // calender class's instance and get current date , month and year from calender
                    final Calendar c = Calendar.getInstance();
                    int mYear = c.get(Calendar.YEAR); // current year
                    int mMonth = c.get(Calendar.MONTH); // current month
                    int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                    // date picker dialog
                    datePickerDialog = new DatePickerDialog(activity,
                            (view, year, monthOfYear, dayOfMonth) -> {
                                // set day of month , month and year value in the edit text
                                String choosedMonth = "";
                                String choosedDate = "";
                                String choosedDateFormat = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                                scheduledDate = choosedDateFormat;
                                try {
                                    choosedMonth = Utilities.getMonth(choosedDateFormat);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if (dayOfMonth < 10) {
                                    choosedDate = "0" + dayOfMonth;
                                } else {
                                    choosedDate = "" + dayOfMonth;
                                }
                                afterToday = Utilities.isAfterToday(year, monthOfYear, dayOfMonth);
                                scheduleDate.setText(choosedDate + " " + choosedMonth + " " + year);
                            }, mYear, mMonth, mDay);
                    datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                    datePickerDialog.getDatePicker().setMaxDate((System.currentTimeMillis() - 1000) + (1000 * 60 * 60 * 24 * 7));
                    datePickerDialog.show();
                    break;
                case R.id.scheduleTime:
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {

                        int callCount = 0;   //To track number of calls to onTimeSet()

                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            if (callCount == 0) {
                                String choosedMinute = "";
                                String choosedTimeZone = "";
                                String choosedTime = "";
                                scheduledTime = selectedHour + ":" + selectedMinute;
                                if (selectedHour > 12) {
                                    choosedTimeZone = "PM";
                                    selectedHour = selectedHour - 12;
                                    if (selectedHour < 10) {
                                        choosedHour = "0" + selectedHour;
                                    } else {
                                        choosedHour = "" + selectedHour;
                                    }
                                } else {
                                    if (selectedHour >= 12) {
                                        choosedTimeZone = "PM";
                                    } else {
                                        choosedTimeZone = "AM";
                                    }
                                    if (selectedHour < 10) {
                                        choosedHour = "0" + selectedHour;
                                    } else {
                                        choosedHour = "" + selectedHour;
                                    }
                                }
                                if (selectedMinute < 10) {
                                    choosedMinute = "0" + selectedMinute;
                                } else {
                                    choosedMinute = "" + selectedMinute;
                                }
                                choosedTime = choosedHour + ":" + choosedMinute + " " + choosedTimeZone;
                                if (!scheduledDate.equals("") && !scheduledTime.equals("")) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd");
                                    Date now = new Date();
                                    String strTime = sdf.format(now);
                                    if (choosedHour.equalsIgnoreCase(strTime)) {
                                        Date date = null;
                                        try {
                                            date = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(scheduledDate);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        long milliseconds = date.getTime();
                                        if (!DateUtils.isToday(milliseconds)) {
                                            scheduleTime.setText(choosedTime);
                                        } else {
                                            if (Utilities.checktimings(scheduledTime)) {
                                                scheduleTime.setText(choosedTime);
                                            } else {
                                                Toast toast = new Toast(activity);
                                                toast.makeText(activity, context.getResources().getString(R.string.different_time), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } else {
                                        if (choosedHour.equalsIgnoreCase("00")) {
                                            scheduleTime.setText("12" + ":" + choosedMinute + " " + choosedTimeZone);
                                        } else {
                                            scheduleTime.setText(choosedTime);
                                        }
                                    }
                                } else {
                                    Toast.makeText(activity, context.getResources().getString(R.string.choose_date_time), Toast.LENGTH_SHORT).show();
                                }
                            }
                            callCount++;
                        }
                    }, hour, minute, false);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();
                    break;
            }
        }
    }

    private class ServiceListAdapter extends RecyclerView.Adapter<ServiceListAdapter.MyViewHolder> {

        JSONArray jsonArray;

        public ServiceListAdapter(JSONArray array) {
            this.jsonArray = array;
        }

        @Override
        public ServiceListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.service_type_list_item, null);
            return new ServiceListAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ServiceListAdapter.MyViewHolder holder, final int position) {
            Utilities.print("Title: ", "" + jsonArray.optJSONObject(position).optString("name")
                    + " Image: " + jsonArray.optJSONObject(position).optString("image") + " Grey_Image:" + jsonArray.optJSONObject(position).optString("grey_image"));
            holder.serviceTitle.setText(jsonArray.optJSONObject(position).optString("name"));
            if (position == currentPostion) {
                SharedHelper.putKey(context, "service_type", "" + jsonArray.optJSONObject(position).optString("id"));
                Glide.with(activity).load(jsonArray.optJSONObject(position).optString("image"))
                        .placeholder(R.drawable.car_select).dontAnimate().error(R.drawable.car_select).into(holder.serviceImg);
                SharedHelper.putKey(context, "name", "" + jsonArray.optJSONObject(currentPostion).optString("name"));
                holder.selector_background.setBackgroundResource(R.drawable.full_rounded_button_accent);
                holder.serviceTitle.setTextColor(ContextCompat.getColor(context, R.color.text_color_white));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
                        (Math.round(context.getResources().getDimension(R.dimen._50sdp)), Math.round(context.getResources().getDimension(R.dimen._50sdp)));
                holder.serviceImg.setLayoutParams(layoutParams);
            } else {
                Glide.with(activity).load(jsonArray.optJSONObject(position).optString("image"))
                        .placeholder(R.drawable.car_select).dontAnimate().error(R.drawable.car_select).into(holder.serviceImg);
                holder.selector_background.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
                holder.serviceTitle.setTextColor(ContextCompat.getColor(context, R.color.black_text_color));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
                        (Math.round(context.getResources().getDimension(R.dimen._40sdp)), Math.round(context.getResources().getDimension(R.dimen._40sdp)));
                holder.serviceImg.setLayoutParams(layoutParams);
            }
            holder.linearLayoutOfList.setTag(position);
            holder.linearLayoutOfList.setOnClickListener(view -> {
                if (position == currentPostion) {
                    try {
                        lnrHidePopup.setVisibility(View.VISIBLE);
                        showProviderPopup(jsonArray.getJSONObject(position));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                currentPostion = Integer.parseInt(view.getTag().toString());
                SharedHelper.putKey(context, "service_type", "" + jsonArray.optJSONObject(Integer.parseInt(view.getTag().toString())).optString("id"));
                SharedHelper.putKey(context, "name", "" + jsonArray.optJSONObject(currentPostion).optString("name"));
                notifyDataSetChanged();
                Utilities.print("service_type", "" + SharedHelper.getKey(context, "service_type"));
                Utilities.print("Service name", "" + SharedHelper.getKey(context, "name"));
                getProvidersList(SharedHelper.getKey(context, "service_type"));
            });
        }

        @Override
        public int getItemCount() {
            return jsonArray.length();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            MyTextView serviceTitle;
            ImageView serviceImg;
            LinearLayout linearLayoutOfList;
            FrameLayout selector_background;

            public MyViewHolder(View itemView) {
                super(itemView);
                serviceTitle = itemView.findViewById(R.id.serviceItem);
                serviceImg = itemView.findViewById(R.id.serviceImg);
                linearLayoutOfList = itemView.findViewById(R.id.LinearLayoutOfList);
                selector_background = itemView.findViewById(R.id.selector_background);
                height = itemView.getHeight();
                width = itemView.getWidth();
            }
        }
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObj = new JSONObject(result);
                if (!jsonObj.optString("status").equalsIgnoreCase("ZERO_RESULTS")) {
                    parserTask = new ParserTask();
                    // Invokes the thread for parsing the JSON data
                    parserTask.execute(result);
                } else {
                    mMap.clear();
                    stopAnim();
                    flowValue = 0;
                    layoutChanges();
                    gotoCurrentPosition();
//                    Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.no_service));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        DataParser parser;

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0]);
                parser = new DataParser();
                Log.d("ParserTask", parser.toString());
                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());
            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            if (result != null) {
                // Traversing through all the routes
                if (result.size() > 0) {
                    for (int i = 0; i < result.size(); i++) {
                        points = new ArrayList<>();
                        lineOptions = new PolylineOptions();
                        // Fetching i-th route
                        List<HashMap<String, String>> path = result.get(i);
                        // Fetching all the points in i-th route
                        for (int j = 0; j < path.size(); j++) {
                            HashMap<String, String> point = path.get(j);
                            double lat = Double.parseDouble(point.get("lat"));
                            double lng = Double.parseDouble(point.get("lng"));
                            LatLng position = new LatLng(lat, lng);
                            points.add(position);
                            if (j == 0) {
                                sourceLatLng = new LatLng(lat, lng);
                            }
                            if (j == path.size()) {
                                destLatLng = new LatLng(lat, lng);
                            }
                        }
                        // Adding all the points in the route to LineOptions
                        lineOptions.addAll(points);
                        lineOptions.width(5);
                        lineOptions.color(Color.BLACK);
                        Log.d("onPostExecute", "onPostExecute lineoptions decoded");
                    }
                    if (flowValue == 1) {
                        if (sourceMarker != null && destinationMarker != null) {
                            sourceMarker.setDraggable(true);
                            destinationMarker.setDraggable(true);
                        }
                    } else {
                        if (is_track.equalsIgnoreCase("YES") &&
                                (CurrentStatus.equalsIgnoreCase("STARTED") || CurrentStatus.equalsIgnoreCase("PICKEDUP")
                                        || CurrentStatus.equalsIgnoreCase("ARRIVED"))) {
                            if (sourceMarker != null && destinationMarker != null) {
                                sourceMarker.setDraggable(false);
                                destinationMarker.setDraggable(true);
                            }
                        } else {
                            if (sourceMarker != null && destinationMarker != null) {
                                sourceMarker.setDraggable(false);
                                destinationMarker.setDraggable(false);
                            }
                        }
                    }
                    if (flowValue != 0) {
                        if (!source_lat.equalsIgnoreCase("") && !source_lng.equalsIgnoreCase("")) {
                            LatLng location = new LatLng(Double.parseDouble(source_lat), Double.parseDouble(source_lng));
                            //mMap.clear();
                            if (sourceMarker != null)
                                sourceMarker.remove();

                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(location).snippet(frmSource.getText().toString())
                                    .title("source").draggable(true)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker));
                            marker = mMap.addMarker(markerOptions);
                            sourceMarker = mMap.addMarker(markerOptions);
                            //CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(18).build();
                            //mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                        for (int i = 0; i < location_array.size(); i++) {
                            LatLng latLng = new LatLng(Double.parseDouble(location_array.get(i).getdLatitude()), Double.parseDouble(location_array.get(i).getdLongitude()));
                            MarkerOptions destMarker = new MarkerOptions()
                                    .position(latLng).title("destination " + i).snippet(location_array.get(i).getdAddress()).draggable(true)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.provider_marker));
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(mMap.addMarker(destMarker).getPosition());
                        }
                        /*if (!dest_lat.equalsIgnoreCase("") && !dest_lng.equalsIgnoreCase("")) {
                            destLatLng = new LatLng(Double.parseDouble(dest_lat), Double.parseDouble(dest_lng));
                            if (destinationMarker != null)
                                destinationMarker.remove();

                            MarkerOptions destMarker = new MarkerOptions()
                                    .position(destLatLng).title("destination").snippet(frmDest.getText().toString()).draggable(true)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.provider_marker));
                            destinationMarker = mMap.addMarker(destMarker);
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(sourceMarker.getPosition());
                            builder.include(destinationMarker.getPosition());
                            LatLngBounds bounds = builder.build();
                            int padding = 320; // offset from edges of the map in pixels
                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                            mMap.moveCamera(cu);
                        }*/
                        strTimeTaken = parser.getEstimatedTime();
                    }
                } else {
                    mMap.clear();
                    flowValue = 0;
                    layoutChanges();
                    Utilities.displayMessage(getView(), context, context.getResources().getString(R.string.no_service));
                }
            }
            // Drawing polyline in the Google Map for the i-th route
            if (flowValue != 0) {
                if (lineOptions != null) {
                    //mMap.addPolyline(lineOptions);
                    startAnim(points);
                } else {
                    Log.d("onPostExecute", "without Polylines drawn");
                }
            }
        }
    }
}