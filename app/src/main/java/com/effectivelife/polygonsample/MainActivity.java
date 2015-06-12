package com.effectivelife.polygonsample;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.InputStream;

import jxl.Sheet;
import jxl.Workbook;
import timber.log.Timber;

/**
 * Created by com on 2015-06-09.
 */
public class MainActivity extends Activity {

    private static final String IS_ALREADY_SYNC = "is_already_sync";

    private EditText etSearch;
    private TextView tvResult;
    private Button btRun;
    private CommercialAreaDBAdapter dbAdapter;

    private MaterialDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbAdapter = new CommercialAreaDBAdapter(this);

        etSearch = (EditText) findViewById(R.id.et_search);
        tvResult = (TextView) findViewById(R.id.tv_result);
        tvResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(tvResult.getText().toString())) {
                    Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                    intent.putExtra(CommercialAreaDBAdapter.AREA_POINT, tvResult.getText().toString());
                    startActivity(intent);
                }
            }
        });
        btRun = (Button) findViewById(R.id.bt_search);
        btRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(etSearch.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
                } else {
                    getAreaPoint(etSearch.getText().toString());
                }
            }
        });

        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        if(!pref.getBoolean(IS_ALREADY_SYNC, false)) {
            dialog = new MaterialDialog.Builder(this).content("Doing DB Sync!!").progress(true, 0).show();
            syncExcelData();
        }

    }

    private void getAreaPoint(String selection) {

        Cursor cursor = dbAdapter.query(selection);
        if(cursor != null) {
            cursor.moveToFirst();
            tvResult.setText(cursor.getString(0));
        }
    }

    private void syncExcelData() {
        Workbook workbook = null;
        try {
            dbAdapter.getWritableDatabase();
            InputStream is = getApplicationContext().getResources().getAssets().open("areapoints.xls");
            workbook = Workbook.getWorkbook(is);

            if(workbook != null) {
                Sheet sheet = workbook.getSheet(0);
                if(sheet != null) {
                    for(int row=1; row<sheet.getRows(); row++) {
                        Timber.d("상권명 : %s, 좌표 : %s", sheet.getCell(1, row).getContents(), sheet.getCell(2, row).getContents());
                        dbAdapter.insert(sheet.getCell(0, row).getContents(), sheet.getCell(1, row).getContents(), sheet.getCell(2, row).getContents());
                    }
                    dbAdapter.close();
                    workbook.close();
                }
            }

            SharedPreferences pref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(IS_ALREADY_SYNC, true);
            editor.apply();
            if(dialog.isShowing()) dialog.dismiss();

        } catch (Exception e) {
            Timber.e(e!=null? (e.getMessage()!=null ? e.getMessage():String.valueOf(e)) : "syncExcelData error");
        } finally {
            if(dbAdapter != null) {
                dbAdapter.close();
            }
            if(workbook != null) {
                workbook.close();
            }
            if(dialog.isShowing()) dialog.dismiss();
        }
    }

}
