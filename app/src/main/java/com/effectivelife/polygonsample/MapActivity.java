package com.effectivelife.polygonsample;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.effectivelife.cokcoksupport.api.ApiInfo;
import com.effectivelife.cokcoksupport.api.ApiResult;
import com.effectivelife.cokcoksupport.net.ReqQueueManager;
import com.effectivelife.cokcoksupport.net.SimpleGsonRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import timber.log.Timber;

/**
 * Created by com on 2015-06-10.
 */
public class MapActivity extends Activity implements OnMapReadyCallback {

    private static final ApiInfo URL_TRANSCOORD = new ApiInfo("https://apis.daum.net", "/local/geo/transcoord", ParsedPoint.class);
    private static final String FROM_COORD = "KTM";
    private static final String TO_COORD = "WGS84";
    private static final String RESPONSE_OUTPUT = "json";

    private GoogleMap map;
    private LatLngBounds.Builder builder;

    private ArrayList<ArrayList<String>> tmCoords = new ArrayList<>();
    private List<LatLng> wgs84Coords = new ArrayList<LatLng>();

    private static final LatLng DEFAULT_LATLNG = new LatLng( 37.56, 126.97);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        map = mapFragment.getMap();

    }

    private void convertPoint(String areas) {
        StringTokenizer tokenizer = new StringTokenizer(areas, "|");
        while (tokenizer.hasMoreTokens()) {
            String point = tokenizer.nextToken();
            StringTokenizer splitPoint = new StringTokenizer(point, ",");
            ArrayList<String> area = new ArrayList<String>();
            while (splitPoint.hasMoreTokens()) {
                area.add(splitPoint.nextToken());
            }
            tmCoords.add(area);
        }
        makeOrderedLatLng();
    }

    private void makeOrderedLatLng() {
        if(tmCoords.isEmpty()) {
            drawBoundary();
            return;
        }

        final SimpleGsonRequest<ParsedPoint> req = new SimpleGsonRequest<>(URL_TRANSCOORD,
                new Response.Listener<ParsedPoint>() {
                    @Override
                    public void onResponse(ParsedPoint response) {
                        wgs84Coords.add(new LatLng(response.getY(), response.getX()));
                        tmCoords.remove(0);
                        makeOrderedLatLng();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Timber.e(error!=null ? (error.getMessage()!=null ? error.getMessage():String.valueOf(error)): "makeOrderedLatLng error");
                    }
                }
        );
        req.addParam("apikey", BuildConfig.APIKEY_DAUM_CONVERTCOORD);
        req.addParam("fromCoord", FROM_COORD);
        req.addParam("toCoord", TO_COORD);
        req.addParam("output", RESPONSE_OUTPUT);
        ArrayList<String> area = tmCoords.get(0);
        req.addParam("x", area.get(0));
        req.addParam("y", area.get(1));

        ReqQueueManager.getInstance(getApplicationContext()).addToRequestQueue(req);

    }

    private void drawBoundary() {
        PolygonOptions polygonOptions = new PolygonOptions().fillColor(Color.parseColor("#80F52887")).strokeColor(Color.parseColor("#F52887"));
        builder = new LatLngBounds.Builder();
        for(LatLng point : wgs84Coords) {
            polygonOptions.add(point);
            builder.include(point);
        }
        map.addPolygon(polygonOptions);
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 10));
        map.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LATLNG, 15));
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        if(getIntent() != null && getIntent().hasExtra(CommercialAreaDBAdapter.AREA_POINT)) {
            convertPoint(getIntent().getStringExtra(CommercialAreaDBAdapter.AREA_POINT));
        }
        /*Polygon polygon = map.addPolygon(new PolygonOptions().add(new LatLng(37.48390852585939, 126.89874115446943),
                        new LatLng(37.48368192623689, 126.89859748547514), new LatLng(37.48383888135317, 126.89706873646234),
                        new LatLng(37.484561815214306, 126.89536205269874),
                        new LatLng(37.4858053678302, 126.8963388086726),
                        new LatLng(37.484699234577874, 126.89850341929704),
                        new LatLng(37.48415188087892, 126.89874889388838),
                        new LatLng(37.48390852585939, 126.89874115446943)).fillColor(Color.parseColor("#80F52887")).strokeColor(Color.parseColor("#F52887"))
        );

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

                LatLngBounds bounds = new LatLngBounds.Builder().include(new LatLng(37.48390852585939, 126.89874115446943)).include(new LatLng(37.48368192623689, 126.89859748547514))
                        .include(new LatLng(37.48383888135317, 126.89706873646234)).include(new LatLng(37.484561815214306, 126.89536205269874))
                        .include(new LatLng(37.4858053678302, 126.8963388086726))
                        .include(new LatLng(37.484699234577874, 126.89850341929704))
                        .include(new LatLng(37.48415188087892, 126.89874889388838))
                        .include(new LatLng(37.48390852585939, 126.89874115446943)).build();
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
                map.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
                map.setOnCameraChangeListener(null);
            }
        });*/

    }

    private static class ParsedPoint extends ApiResult {
        @SerializedName("x")
        private double x;
        @SerializedName("y")
        private double y;

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }
}
