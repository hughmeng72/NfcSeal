package com.freight_track.android.nfcseal.fragment;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.freight_track.android.nfcseal.R;
import com.freight_track.android.nfcseal.model.WsResultOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wayne on 4/23/2017.
 */

public class BaiduMapFragment extends Fragment {
    private static String TAG = "BaiduMapFragment";

    public static final String EXTRA_OPERATIONS = "com.freight_track.android.nfcseal.operations";

    private ArrayList<WsResultOperation> mOperationList;

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private BitmapDescriptor mbdCenter = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
    private BitmapDescriptor mbdGeneral = BitmapDescriptorFactory.fromResource(R.drawable.icon_mark);

    public static BaiduMapFragment newInstance(ArrayList<WsResultOperation> operations) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(EXTRA_OPERATIONS, operations);

        BaiduMapFragment fragment = new BaiduMapFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_freight_track_map, container, false);

        mOperationList = getArguments().getParcelableArrayList(EXTRA_OPERATIONS);

        mMapView = (MapView) view.findViewById(R.id.bmapView);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setupBaiduMap();
        clearLocationData();
        showBaiduMap();

        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * 设置百度地图属性
     */
    private void setupBaiduMap() {
        mMapView.removeViewAt(1); //移除百度地图Logo

        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16));
        mBaiduMap.getUiSettings().setOverlookingGesturesEnabled(false); //取消俯视手势
    }

    /**
     * 清除百度地图覆盖物
     */
    private void clearLocationData() {
        mBaiduMap.clear();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mMapView.getOverlay().clear();
        }
    }

    /**
     * 加载轨迹数据至百度地图
     */
    private void showBaiduMap() {
        if (mOperationList == null || mOperationList.size() == 0)
            return;

        List<LatLng> points = new ArrayList<>();

        for(WsResultOperation operation : mOperationList) {
            String[] location = operation.getCoordinate().split(",");
            LatLng lng = new LatLng(
                    Double.parseDouble(location[0]),
                    Double.parseDouble(location[1])
            );

            points.add(lng);

            MarkerOptions markerOptions;
            if (operation.isSelected()) {
                markerOptions = new MarkerOptions().icon(mbdCenter).position(lng).zIndex(1);

                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(lng, 14));
            }
            else {
                markerOptions = new MarkerOptions().icon(mbdGeneral).position(lng);
            }

            mBaiduMap.addOverlay(markerOptions);
        }

        if (points.size() > 1) {
            OverlayOptions options = new PolylineOptions()
                    .width(8)
                    .color(0xAAFF0000)
                    .points(points);

            mBaiduMap.addOverlay(options);
        }
    }

}
