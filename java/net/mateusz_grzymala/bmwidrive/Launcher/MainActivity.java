package net.mateusz_grzymala.bmwidrive.Launcher;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;

import net.mateusz_grzymala.bmwidrive.AppList.AppsListActivity;
import net.mateusz_grzymala.bmwidrive.R;
import net.mateusz_grzymala.bmwidrive.SettingsActivity;


public class MainActivity extends Activity implements OnMapReadyCallback {
    private ContentResolver cResolver;
    private Window window;
    private final Handler handler = new Handler();
    private boolean paused = false;
    private static final int UPDATE_FREQUENCY = 1000;
    private HorizontalScrollView hsv;
    private ImageButton leftArrow;
    private ImageButton rightArrow;
    private ImageView btnWifi;
    private ImageView btnBt;
    private ImageView btnSt;
    private ImageView btimg;
    private TextView bttxt;
    private TextView mViewTxt;
    private ImageView mViewImg;
    private LinearLayout mView;
    private GoogleMap gmap;
    private MapView map;
    private AppWidgetManager mAppWidgetManager;
    private AppWidgetHost mAppWidgetHost;
    private ObjectAnimator objectAnimator;
    private boolean PlayOnStart = false;
    private LatLng myloc=new LatLng(52,22);
    private boolean mapShow = false;
    private Marker mp;
    private LocationManager mLocationManager;
    private BroadcastReceiver btChangeReceiver;
    private BroadcastReceiver wifiChangeReceiver;
    private Bundle sbundle;
    private static final String MAP_VIEW_BUNDLE_KEY = "AIzaSyBAeNkkwoG8JEqODGK-t3LQ236LkZaABE0";

    /*------------------------------- Basic Actions -----------------------------------*/
    @Override
    protected void onPause() {
        super.onPause();
        if(map!=null) {
            map.onPause();
        }
        removeCallbacks();
        paused = true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(map!=null) {
            map.onResume();
        }
        updateMap();
        registerBrodcastRecievers();
        if (paused) {
            updatePosition();

            paused = false;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(map!=null) {
            map.onDestroy();
        }
        unregisterBrodcastRecievers();
        removeCallbacks();
    }
    @Override
    protected void onStart() {
        super.onStart();
        updatePosition();
        updateMap();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        checkswitchvalues(sharedPref);
        mAppWidgetHost.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAppWidgetHost.stopListening();
        if(map!=null) {
            map.onStop();
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitializeGuiElements();
        SharedPreferences sharedPref1 = PreferenceManager.getDefaultSharedPreferences(this);
        checkswitchvalues(sharedPref1);
        checkconnection(this,savedInstanceState);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        createSavedAudioWidget();
        createSavedOBCWidget();
        checkPermissions();
        sbundle=savedInstanceState;
        youDesirePermissionCode();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, mLocationListener);
        initializeButtons();
        initializeBrodcastRecievers();
        updatePosition();
        updateMap();

    }
    /*------------------------------- Popups -----------------------------------*/
    public void onButtonShowPopupWindowClick(View view) {
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popupaudio, null);
        int width = 250;
        int height = 280;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setAnimationStyle(R.style.popup_window_animation);
        TableRow media = (TableRow) findViewById(R.id.media_header);
        popupWindow.showAtLocation(media, Gravity.NO_GRAVITY, 96, 163);
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }
    public void onButtonShowPopupWindowClick1(View view) {
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popupnavi, null);
        int width = 250;
        int height = 280;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setAnimationStyle(R.style.popup_window_animation);
        TableRow navi = (TableRow) findViewById(R.id.navi_header);

        popupWindow.showAtLocation(navi, Gravity.NO_GRAVITY, 386, 163);
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }
    /*------------------------------- Permission -----------------------------------*/
    public void youDesirePermissionCode(){

            boolean flag=false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flag=Settings.System.canWrite(this);
            }else {
                flag = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED;
            }
        if (flag) {
            //do your code
        }  else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                this.startActivityForResult(intent, 111);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_SETTINGS},111);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults){
    }
    public void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                        , 10);
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                        , 10);
            }
        }
    }
    /*-----------------     Butons     -------------------------------------------*/
    public void checkswitchvalues(SharedPreferences s){

        PlayOnStart = s.getBoolean("PlayOnStart", false);
        if (PlayOnStart) {
            btnSt.setColorFilter(Color.parseColor("#af2519"));
        }
        else{
            btnSt.setColorFilter(Color.parseColor("#ffffff"));
        }
    }
    public void initializeButtons(){
        btnWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            btnWifi.setVisibility(View.INVISIBLE);
            btnWifi.setMaxWidth(0);
        }
        else {
            if (wifi.isWifiEnabled()) {
                btnWifi.setVisibility(View.VISIBLE);
            }
        }
    }
    /*-----------------     BroadcastRecievers ----------------------------------*/
    public void initializeBrodcastRecievers(){
        btChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
                changeBtBtn(state);
            }
        };
        wifiChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    boolean connected = info.isConnected();
                    changeWiFiBtn(connected);
                }
            }
        };
    }
    public void registerBrodcastRecievers(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        registerReceiver(btChangeReceiver, filter);
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(wifiChangeReceiver, intentFilter1);
    }
    public void unregisterBrodcastRecievers(){
        unregisterReceiver(btChangeReceiver);
        unregisterReceiver(wifiChangeReceiver);
    }
    /*-----------------     Widget ---------------------------------------------*/
    private void configureAudioWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("IdAudioWidget",appWidgetId);
        editor.commit();
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, R.id.REQUEST_CREATE_APPWIDGET);
        } else {
            createAudioWidget(data);
        }
    }
    private void createAudioWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

        AppWidgetHostView hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
        hostView.setAppWidget(appWidgetId, appWidgetInfo);
        LinearLayout l= (LinearLayout) findViewById(R.id.widgetAudio);
        l.removeAllViews();
        hostView.setLayoutParams(new ViewGroup.LayoutParams(270, 330));
        hostView.setPadding(-6,-6,-6,-6);
        hostView.requestLayout();
        hostView.updateAppWidgetSize(null,270,330,270,330);
        l.addView(hostView);
    }
    private void createSavedAudioWidget() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int i=sharedPref.getInt("IdAudioWidget",0);
        if (i!=0){
            LinearLayout l = (LinearLayout) findViewById(R.id.widgetAudio);
            l.removeAllViews();
            int appWidgetId = i;
            AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
            AppWidgetHostView hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
            hostView.setAppWidget(appWidgetId, appWidgetInfo);
            hostView.setPadding(-6,-6,-6,-6);
            hostView.setLayoutParams(new ViewGroup.LayoutParams(270, 330));
            hostView.requestLayout();
            hostView.updateAppWidgetSize(null, 270, 330, 270, 330);
            l.addView(hostView);
        }
    }
    public void selectWidget(View view) {
        int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        addEmptyData(pickIntent);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("WidgetType","Audio");
        editor.commit();
        startActivityForResult(pickIntent, R.id.REQUEST_PICK_APPWIDGET);
    }
    void addEmptyData(Intent pickIntent) {
        ArrayList customInfo = new ArrayList();
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
        ArrayList customExtras = new ArrayList();
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == R.id.REQUEST_PICK_APPWIDGET) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                String Type = sharedPref.getString("WidgetType","");

                if(Type=="Audio"){
                    configureAudioWidget(data);
                }
                else if(Type=="OBC"){
                    configureOBCWidget(data);
                }
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.remove("WidgetType");
                editor.commit();
            } else if (requestCode == R.id.REQUEST_CREATE_APPWIDGET) {
                if(data.getStringExtra("Type")=="Audio") {
                    createAudioWidget(data);
                }else if(data.getStringExtra("Type")=="OBC"){
                    createOBCWidget(data);
                }
            }
        } else if (resultCode == RESULT_CANCELED && data != null) {
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
    }
    /*-----------------    OBC Widget ---------------------------------------------*/
    private void configureOBCWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("IdOBCWidget",appWidgetId);
        editor.commit();
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, R.id.REQUEST_CREATE_APPWIDGET);
        } else {
            createOBCWidget(data);
        }
    }
    private void createOBCWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

        AppWidgetHostView hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
        hostView.setAppWidget(appWidgetId, appWidgetInfo);
        LinearLayout l= (LinearLayout) findViewById(R.id.widgetOBC);
        l.removeAllViews();
        hostView.setLayoutParams(new ViewGroup.LayoutParams(270, 330));
        hostView.setPadding(-6,-6,-6,-6);
        hostView.requestLayout();
        hostView.updateAppWidgetSize(null,270,330,270,330);
        l.addView(hostView);
    }
    private void createSavedOBCWidget() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int i=sharedPref.getInt("IdOBCWidget",0);
        if (i!=0){
            LinearLayout l = (LinearLayout) findViewById(R.id.widgetOBC);
            l.removeAllViews();
            int appWidgetId = i;
            AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
            AppWidgetHostView hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
            hostView.setAppWidget(appWidgetId, appWidgetInfo);
            hostView.setPadding(-6,-6,-6,-6);
            hostView.setLayoutParams(new ViewGroup.LayoutParams(270, 330));
            hostView.requestLayout();
            hostView.updateAppWidgetSize(null, 270, 330, 270, 330);
            l.addView(hostView);
        }
    }
    public void selectObcWidget(View view) {
        int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        addEmptyData(pickIntent);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("WidgetType","OBC");
        editor.commit();
        startActivityForResult(pickIntent, R.id.REQUEST_PICK_APPWIDGET);
    }

    /*--------------------------- GO TO APPS ---------------------------------------*/
    public void goApps(View view){
        startActivity(new Intent(this,AppsListActivity.class));
    }
    public void goSettings(View view){
        startActivity(new Intent(Settings.ACTION_SETTINGS));
    }
    public void goObd(View view){
        Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.e39.ak.e39ibus.app");
        if (intent != null) {
            startActivity(intent);
        } else {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.e39.ak.e39ibus.app")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.e39.ak.e39ibus.app")));
            }
        }
    }
    public void goBrowser(View view){
        Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.android.chrome");
        if (intent != null) {
            startActivity(intent);
        } else {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.android.chrome")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.android.chrome")));
            }
        }
    }
    public void goAssistent(View view){
        Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.google.android.googlequicksearchbox");
        if (intent != null) {
            startActivity(intent);
        } else {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.googlequicksearchbox")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.googlequicksearchbox")));
            }
        }
    }
    public void goVideo(View view){
        Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.tw.video");
        if (intent != null) {
            startActivity(intent);
        } else {
            intent = this.getPackageManager().getLaunchIntentForPackage("org.videolan.vlc");
            if (intent != null) {
                startActivity(intent);
            }
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.videolan.vlc")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.videolan.vlc")));
            }
        }
    }
    public void goDVR(View view){
        Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.tw.recorder");
        if (intent != null) {
            startActivity(intent);
        } else {
            intent = this.getPackageManager().getLaunchIntentForPackage("davidchristian.dvrdriving");
            if (intent != null) {
                startActivity(intent);
            }
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=davidchristian.dvrdriving")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=davidchristian.dvrdriving")));
            }
        }
    }
    public void goPhoneLink(View view){
        Intent intent = this.getPackageManager().getLaunchIntentForPackage("net.easyconn");
        if (intent != null) {
            startActivity(intent);
        } else {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=net.easyconn")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=net.easyconn")));
            }
        }
    }
    public void goBluetooth1(View view){
        Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.tw.bt");
        if (intent != null) {
            startActivity(intent);
        } else {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.tw.bt")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.tw.bt")));
            }
        }
    }
    public void goMusic(View view){
        Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.google.android.music");
        if (intent != null) {
            startActivity(intent);
        } else {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.music")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.music")));
            }
        }
    }
    public void goRadio(View view){
        Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.tw.radio");
        if (intent != null) {
            startActivity(intent);
        } else {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.tw.radio")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.tw.radio")));
            }
        }
    }
    public void runAutomapa(View view){
        Intent intent = this.getPackageManager().getLaunchIntentForPackage("pl.aqurat.automapa");
        if (intent != null) {
            startActivity(intent);
        } else {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=pl.aqurat.automapa")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=pl.aqurat.automapa")));
            }
        }
    }
    public void runYanosik(View view){
        Intent intent = this.getPackageManager().getLaunchIntentForPackage("pl.neptis.yanosik.mobi.android");
        if (intent != null) {
            startActivity(intent);
        } else {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=pl.neptis.yanosik.mobi.android")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=pl.neptis.yanosik.mobi.android")));
            }
        }
    }
    /*---------------------------- Update GUI -----------------------------------------*/
    private void changeWiFiBtn(boolean state) {
        if(state){
            btnWifi.setColorFilter(Color.parseColor("#af2519"));
            MapView m=new MapView(this);

        }else{
            btnWifi.setColorFilter(Color.parseColor("#ffffff"));
        }
    }
    private void changeBtBtn(int state){

        if(state==BluetoothAdapter.STATE_CONNECTED){
            btnBt.setColorFilter(Color.parseColor("#af2519"));
            BluetoothAdapter.getDefaultAdapter().getName();
            btimg.setImageResource(R.drawable.phbt);
            btimg.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            bttxt.setText(R.string.bluetooth_description_connected);
            bttxt.setGravity(Gravity.CENTER);
        }else{
            btnBt.setColorFilter(Color.parseColor("#ffffff"));
            btimg.setImageResource(R.drawable.icon_bluetooth);
            bttxt.setText(R.string.bluetooth_description);
            bttxt.setGravity(Gravity.LEFT);
        }
    }
    public void rightScroll(View v) {
        rightArrow.setVisibility(View.INVISIBLE);
        leftArrow.setVisibility(View.INVISIBLE);
        objectAnimator = ObjectAnimator.ofInt(hsv, "scrollX", hsv.getScrollX(), hsv.getScrollX()+1000).setDuration(1000);
        objectAnimator.start();
    }
    public void ScrollToBegin(View v) {
        rightArrow.setVisibility(View.INVISIBLE);
        leftArrow.setVisibility(View.INVISIBLE);
        objectAnimator = ObjectAnimator.ofInt(hsv, "scrollX", hsv.getScrollX(), 0).setDuration(1000);
        objectAnimator.start();
    }
    public void leftScroll(View v) {

        rightArrow.setVisibility(View.INVISIBLE);
        leftArrow.setVisibility(View.INVISIBLE);
        objectAnimator = ObjectAnimator.ofInt(hsv, "scrollX", hsv.getScrollX(), hsv.getScrollX()-1000).setDuration(1000);
        objectAnimator.start();
    }
    /*-----------------------------Settings-----------------------------------------------*/
    public void gotoAppSettings(View v) {
        startActivity(new Intent(this, SettingsActivity.class));
    }
    /*-----------------------------Maps-----------------------------------------------*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        gmap.setMinZoomPreference(12);
        LatLng ny = new LatLng(52.4103, 22);

        gmap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.stylemap));
        if(mp==null){
            MarkerOptions mo=new MarkerOptions();
            mo.position(ny).icon(BitmapDescriptorFactory.fromResource(R.drawable.locationdot));
            mp=gmap.addMarker(mo);;
        }
        gmap.moveCamera(CameraUpdateFactory.newLatLng(ny));
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mapShow!=false) {
            Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
            if (mapViewBundle == null) {
                mapViewBundle = new Bundle();
                outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
            }

            map.onSaveInstanceState(mapViewBundle);
        }
    }
    public void checkconnection(Context context,Bundle savedInstanceState){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if((cm.getActiveNetworkInfo() != null) && cm.getActiveNetworkInfo().isConnectedOrConnecting()&&sharedPref.getBoolean("HomeMapEnabled",false)){
             if(mapShow==false) {
                 Bundle mapViewBundle = null;
                 if (savedInstanceState != null) {
                     mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
                 }
                 mView.removeAllViews();
                 map = new MapView(this);
                 map.onCreate(mapViewBundle);
                 mView.addView(map);
                 map.getMapAsync(this);
                 mapShow = true;
                 map.onResume();
                 if(mp==null&&gmap!=null) {
                     MarkerOptions mo = new MarkerOptions();
                     mo.position(myloc).icon(BitmapDescriptorFactory.fromResource(R.drawable.locationdot));
                     mp = gmap.addMarker(mo);
                 }
             }
         }else{
             if(map!=null) {
                 map.onDestroy();
             }
             mView.removeAllViews();
             mView.addView(mViewTxt);
             mView.addView(mViewImg);
             mp=null;
             map=null;

             gmap=null;
             mapShow=false;
         }
    }
    /*----------------------------- initialize gui elements ---------------------------------------------------*/
    public void InitializeGuiElements(){
        hsv = findViewById(R.id.hsmain);
        leftArrow =  findViewById(R.id.arrow_left_scroll);
        rightArrow =  findViewById(R.id.arrow_right_scroll);
        btnSt =  findViewById(R.id.setbtn);
        btnWifi =  findViewById(R.id.wifibtn);
        btnBt =  findViewById(R.id.btbtn2);
        btimg = findViewById(R.id.btimg);
        bttxt = findViewById(R.id.bttxt);
        mView = findViewById(R.id.MapViews);
        mViewTxt = findViewById(R.id.textView23);
        mViewImg = findViewById(R.id.imageView10);
        cResolver = getContentResolver();
        window = getWindow();
        hsv.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetHost = new AppWidgetHost(this, R.id.APPWIDGET_HOST_ID);
    }
    /*--------------------Location-------------------------*/
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            myloc = new LatLng(location.getLatitude(),location.getLongitude());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
    private final Runnable updateMapRunnable = new Runnable() {
        public void run() {
            updateMap();
        }
    };
    private void updateMap() {
        handler.removeCallbacks(updateMapRunnable);
        handler.postDelayed(updateMapRunnable, 10000);
        checkconnection(this,sbundle);
        if (mp!=null&&mapShow!=false) {

            mp.setPosition(myloc);
            mp.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.locationdot));
            gmap.moveCamera(CameraUpdateFactory.newLatLng(myloc));
        }

    }
    private final Runnable updatePositionRunnable = new Runnable() {
        public void run() {
            updatePosition();
        }
    };
    private void updatePosition() {
        handler.removeCallbacks(updatePositionRunnable);
        if (objectAnimator != null && !objectAnimator.isRunning()) {
            if (hsv.getScrollX() == 0) {
                leftArrow.setVisibility(View.INVISIBLE);
            } else {
                leftArrow.setVisibility(View.VISIBLE);
            }
            if (hsv.getScrollX() > 1800) {
                rightArrow.setVisibility(View.INVISIBLE);
            } else {
                rightArrow.setVisibility(View.VISIBLE);
            }

        }
        handler.postDelayed(updatePositionRunnable, UPDATE_FREQUENCY);
    }
    private void removeCallbacks(){
        handler.removeCallbacks(updatePositionRunnable);
        handler.removeCallbacks(updateMapRunnable);
        mLocationManager.removeUpdates(mLocationListener);
    }
}