package com.effectivelife.polygonsample;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import jxl.Sheet;
import jxl.Workbook;
import timber.log.Timber;

/**
 * Created by com on 2015-06-09.
 */
public class MainActivity extends AppCompatActivity {

    private static final String IS_ALREADY_SYNC = "is_already_sync";

    @InjectView(R.id.et_search)
    EditText etSearch;
    @InjectView(R.id.bt_search)
    ImageButton btRun;
    @InjectView(R.id.rvArea)
    RecyclerView rvArea;

    private AreaAdapter areaAdapter;
    private CommercialAreaDBAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        dbAdapter = new CommercialAreaDBAdapter(this);

        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        if(!pref.getBoolean(IS_ALREADY_SYNC, false)) {
            new ExcelAsyncTask().execute();

        } else {
            drawList();
        }

        btRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etSearch.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
                } else {
                    getAreaPoint(etSearch.getText().toString());
                }
            }
        });

    }

    private void drawList() {
        Cursor cursor = dbAdapter.getAllPoints();
        List<AreaBean> areas = new ArrayList<AreaBean>();
        if(cursor != null) {
            while(cursor.moveToNext()) {
                AreaBean item = new AreaBean(cursor.getString(0), cursor.getString(1));
                areas.add(item);
            }
            cursor.close();
        }
        LinearLayoutManager llLayoutManager = new LinearLayoutManager(this);
        rvArea.setLayoutManager(llLayoutManager);
        areaAdapter = new AreaAdapter(this, areas);
        rvArea.setAdapter(areaAdapter);
    }

    private void getAreaPoint(String selection) {

        Cursor cursor = dbAdapter.query(selection);
        if(cursor != null) {
            List<AreaBean> areas = new ArrayList<AreaBean>();
            while (cursor.moveToNext()) {
                AreaBean item = new AreaBean(cursor.getString(0), cursor.getString(1));
                areas.add(item);
            }
            cursor.close();
            areaAdapter.updateItem(areas);

        }
    }

    private void syncExcelData() {
        Workbook workbook = null;
        try {
            dbAdapter.getWritableDatabase();
            InputStream is = getApplicationContext().getResources().getAssets().open("1200_areas.xls");
            workbook = Workbook.getWorkbook(is);

            if(workbook != null) {
                Sheet sheet = workbook.getSheet(0);
                if(sheet != null) {
                    for(int row=1; row<sheet.getRows(); row++) {
                        Timber.d("상권명 : %s, 좌표 : %s", sheet.getCell(1, row).getContents(), sheet.getCell(9, row).getContents());
                        dbAdapter.insert(sheet.getCell(0, row).getContents(), sheet.getCell(1, row).getContents(), sheet.getCell(9, row).getContents());
                    }
                    dbAdapter.close();
                    workbook.close();
                }
            }

        } catch (Exception e) {
            Timber.e(e!=null? (e.getMessage()!=null ? e.getMessage():String.valueOf(e)) : "syncExcelData error");
        } finally {
            if(dbAdapter != null) {
                dbAdapter.close();
            }
            if(workbook != null) {
                workbook.close();
            }
        }
    }

    private class ExcelAsyncTask extends AsyncTask<Void, Void, Void> {

        MaterialDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new MaterialDialog.Builder(MainActivity.this).content("엑셀 데이터를 싱크 중입니다.").progress(true, 0).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            syncExcelData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();

            SharedPreferences pref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(IS_ALREADY_SYNC, true);
            editor.apply();

            drawList();

            super.onPostExecute(aVoid);
        }
    }

}
