package com.effectivelife.polygonsample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;

/**
 * Created by com on 2015-06-10.
 */
public class CommercialAreaDBAdapter {
    // 데이터베이스명
    public static final String DATABASE_NAME = "commercial_area";
    // 테이블명
    public static final String TABLE_NAME = "coord_info";
    // 상권번호
    public static final String AREA_NUM = "area_num";
    // 상권명
    public static final String AREA_NAME = "area_name";
    // 포인트 (위경도는 ','로 구분 포인트는 '|'로 구분
    public static final String AREA_POINT = "area_point";
    // PK
    public static final String AREA_KEY = "_id";
    // VERSION
    public static final int DATABASE_VERSION = 1;

    private final Context context;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    private static final String CREATE_DATABASE = new StringBuilder("create table ").append(TABLE_NAME).append(" (")
            .append(AREA_KEY).append(" integer primary key autoincrement,")
            .append(AREA_NUM).append(" text not null,")
            .append(AREA_NAME).append(" text not null,")
            .append(AREA_POINT).append(" text not null);").toString();

    public CommercialAreaDBAdapter(Context context) {
        this.context = context;
        dbHelper = new DBHelper(context);
    }

    public CommercialAreaDBAdapter getWritableDatabase() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public long insert(String areaNum, String areaName, String areaPoint) {
        ContentValues values = new ContentValues();
        values.put(AREA_NUM, areaNum);
        values.put(AREA_NAME, areaName);
        values.put(AREA_POINT, areaPoint);
        return db.insert(TABLE_NAME, null, values);
    }

    public Cursor getAllPoints() {
        return db.query(TABLE_NAME, new String[]{AREA_KEY, AREA_NAME, AREA_POINT}, null, null, null, null, null);
    }

    public Cursor query(String selection) {
        db = dbHelper.getReadableDatabase();

        StringBuilder query = new StringBuilder("SELECT ").append(AREA_POINT).append(" FROM ").append(TABLE_NAME)
                .append(" WHERE ").append(AREA_NAME).append(" LIKE '%").append(selection).append("%';");

//        String[] args = new String[] {selection+"*"};
        Cursor cursor = db.rawQuery(query.toString(), null);
        if(cursor == null) {
            return null;
        } else if(!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }
    /*private Cursor query(String selection, String[] selectionArgs, String[] colums) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_NAME);

        Cursor cursor = builder.query(dbHelper.getReadableDatabase(), colums, selection, selectionArgs, null, null, null);
        if(cursor == null) {
            return null;
        } else if(!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    public Cursor getData(String query, String[] colums) {
        String selection = AREA_NAME + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query(selection, selectionArgs, colums);
    }*/

    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DATABASE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
            onCreate(db);
        }
    }

}