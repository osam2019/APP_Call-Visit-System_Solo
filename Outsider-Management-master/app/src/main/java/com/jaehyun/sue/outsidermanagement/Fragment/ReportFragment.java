package com.jaehyun.sue.outsidermanagement.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.jaehyun.sue.outsidermanagement.Activity.MainActivity;
import com.jaehyun.sue.outsidermanagement.Adapter.ReportListAdapter;
import com.jaehyun.sue.outsidermanagement.R;
import com.jaehyun.sue.outsidermanagement.Utility.FireStoreCallbackListener;
import com.jaehyun.sue.outsidermanagement.Utility.FireStoreConnectionPool;
import com.jaehyun.sue.outsidermanagement.Utility.LoadingDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jaehyun.sue.outsidermanagement.R;


public class ReportFragment extends Fragment
{
    LinearLayout subtitleLinearLayout, bottomLinearLayout;
    ListView reportListView;
    EditText contentEditText;
    Button reportBtn;
    Button locateBtn;

    GoogleMap googleMap;
    Double latitude = 0.0;
    Double longitude =0.0;
    LocationManager manager;


    ArrayList<HashMap<String, Object>> reportList;
    private ReportListAdapter adapter;
    private FireStoreCallbackListener fireStoreCallbackListener;
    private LoadingDialog loadingDialog;

    public void setFireStoreCallbackListener(FireStoreCallbackListener listener)
    {
        this.fireStoreCallbackListener = listener;
    }
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        this.initReportList();

        this.view = inflater.inflate(R.layout.fragment_report, container, false);

        // Inflate the layout for this fragment
        return this.view;
    }

    private void initReportList()
    {
        Log.d("ReportFragment", "Initialize report list");

        if( loadingDialog == null )
            loadingDialog = new LoadingDialog(getContext());

        loadingDialog.show("Report Loading");

        this.setFireStoreCallbackListener(new FireStoreCallbackListener()
        {
            final int TASK_FAILURE = 1;

            @Override
            public void occurError(int errorCode)
            {
                switch (errorCode)
                {
                    case TASK_FAILURE:
                        Log.d("ReportFragment", "Task is not successful");
                        break;
                    default:
                        break;
                }

                if( loadingDialog.isShowing() )
                    loadingDialog.dismiss();
            }

            @Override
            public void doNext(boolean isSuccesful, Object obj)
            {
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();

                if (!isSuccesful)
                {
                    occurError(TASK_FAILURE);
                    return;
                }

                if (obj == null)
                {
                    Log.d("ReportFragment", "Report history is not found");
                    reportList = new ArrayList<>();
                }
                else if (obj != null && !(obj instanceof Boolean) && !(obj instanceof String))
                    reportList = (ArrayList<HashMap<String, Object>>) obj;

                Collections.sort(reportList, new Comparator<HashMap<String, Object>>()
                {
                    @Override
                    public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2)
                    {
                        return -o1.get("reportDate").toString().compareTo(o2.get("reportDate").toString());
                    }
                });
                startLocationService();
                bindUI(view);
            }
        });

        if( (boolean) MainActivity.myInfoMap.get("officer") )   // 간부이면
            FireStoreConnectionPool.getInstance().select(fireStoreCallbackListener,
                    "report", "supervisorId", MainActivity.myInfoMap.get("id").toString());
        else
            FireStoreConnectionPool.getInstance().select(fireStoreCallbackListener,
                    "report", "memberId", MainActivity.myInfoMap.get("id").toString());
    }


    //좌표 확인
    private void startLocationService()
    {
        manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        long minTime = 1000;
        float minDistance = 1;

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(getActivity(), "허가 받지 않았습니다", Toast.LENGTH_SHORT).show();
            return;
        }
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,minTime,minDistance,mLocationListener);
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,minTime,minDistance,mLocationListener);
    }

    private void stopLocationService()
    {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(getActivity(), "허가 받지 않았습니다", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            stopLocationService();
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








    private void bindUI(View view)
    {
        // Bind views
        this.subtitleLinearLayout = view.findViewById(R.id.subtitle_linear_layout);
        this.reportListView = view.findViewById(R.id.report_listView);
        this.adapter = new ReportListAdapter(getActivity(), R.layout.report_item, this.reportList);
        this.bottomLinearLayout = view.findViewById(R.id.bottom_linear_layout);
        this.contentEditText = view.findViewById(R.id.content_editText);
        this.reportBtn = view.findViewById(R.id.report_btn);

        this.locateBtn = view.findViewById(R.id.locate_btn);

        // Set attributes
        this.reportListView.setAdapter(this.adapter);

        if( (boolean) MainActivity.myInfoMap.get("officer") )    // 간부이면
        {
            this.subtitleLinearLayout.setVisibility(LinearLayout.VISIBLE);
            this.bottomLinearLayout.setVisibility(LinearLayout.GONE);
        }
        else
        {
            this.subtitleLinearLayout.setVisibility(LinearLayout.GONE);
            this.bottomLinearLayout.setVisibility(LinearLayout.VISIBLE);
        }

        // Add Events
        this.contentEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if( s.toString().trim().length() == 0 )
                    reportBtn.setEnabled(false);
                else
                    reportBtn.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });

        this.reportBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d("reportBtn", "Clicked : ");

                if ((boolean) MainActivity.myInfoMap.get("isOutsider"))
                {
                    final HashMap<String, Object> map = new HashMap<>();
                    String now = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());

                    map.put("outsiderType", MainActivity.myInfoMap.get("outsiderType"));
                    map.put("memberId", MainActivity.myInfoMap.get("id"));
                    map.put("supervisorId", MainActivity.myInfoMap.get("supervisorId"));
                    map.put("class", MainActivity.myInfoMap.get("class"));
                    map.put("name", MainActivity.myInfoMap.get("name"));
                    map.put("reportDate", now);
                    map.put("reportContent", contentEditText.getText().toString().trim());
                    map.put("tel", MainActivity.myInfoMap.get("tel"));

                    contentEditText.setText("");

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getWindow().getCurrentFocus().getWindowToken(), 0);

                    loadingDialog.show("Reporting now");

                    FireStoreConnectionPool.getInstance().insertNoID(fireStoreCallbackListener, map, "report");
                    FireStoreConnectionPool.getInstance().updateOne(fireStoreCallbackListener, "outsider",
                            "memberId", MainActivity.myInfoMap.get("id").toString(), "startDate", "endDate", "reportDate", now);

                    reportList.add(map);
                    adapter.notifyDataSetChanged();
                }
                else
                    Toast.makeText(getContext(), "현재 출타 중이지 않아서 보고를 할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        this.locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d("locateBtn","Clicked");
                if ((boolean) MainActivity.myInfoMap.get("isOutsider"))
                {
                    final HashMap<String, Object> map = new HashMap<>();
                    String now = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());

                    map.put("outsiderType", MainActivity.myInfoMap.get("outsiderType"));
                    map.put("memberId", MainActivity.myInfoMap.get("id"));
                    map.put("supervisorId", MainActivity.myInfoMap.get("supervisorId"));
                    map.put("class", MainActivity.myInfoMap.get("class"));
                    map.put("name", MainActivity.myInfoMap.get("name"));
                    map.put("reportDate", now);
                    map.put("reportContent", MainActivity.myInfoMap.get("name")+ " 위치보고 드리겠습니다. \n현재 위치하고 있는 곳의 위도는"+ latitude + "경도는" + longitude + "늦지 않게 복귀하겠습니다.");
                    map.put("tel", MainActivity.myInfoMap.get("tel"));

                    contentEditText.setText("");

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getWindow().getCurrentFocus().getWindowToken(), 0);

                    loadingDialog.show("Reporting now");

                    FireStoreConnectionPool.getInstance().insertNoID(fireStoreCallbackListener, map, "report");
                    FireStoreConnectionPool.getInstance().updateOne(fireStoreCallbackListener, "outsider",
                            "memberId", MainActivity.myInfoMap.get("id").toString(), "startDate", "endDate", "reportDate", now);

                    reportList.add(map);
                    adapter.notifyDataSetChanged();
                }
                else
                    Toast.makeText(getContext(), "현재 출타 중이지 않아서 보고를 할 수 없습니다.", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
