package com.mycontentprovider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class ProviderBase extends ContentProvider{

	private static final String TABLE_NAME = "myprovider";
	
	private static final String DATABASE_NAME = "tinnouifolder";
	private static final int DATABASE_VERSION = 1;
	
	private static final String COLUMNS_ID = "_id";
	private static final String COLUMNS_NAME = "name";
	private static final String COLUMNS_AGE = "age";
	
	private static final String AUTHORITY = "com.mycontentprovider.providerbase";
	
	private static Uri CURRENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
	
	private static final int PROVIDER_ALL = 1;
	private static final int PROVIDER_ITEM = 2;
	
	private static final UriMatcher mUrimatcher;
	
	private static HashMap<String,String> mProjection;
	
	static{
		mUrimatcher = new UriMatcher(UriMatcher.NO_MATCH);
		//提供访问整个表，以及表中某个记录的URI
		mUrimatcher.addURI(AUTHORITY, TABLE_NAME, MYPROVIDER);
		mUrimatcher.addURI(AUTHORITY, TABLE_NAME + "/#", MYPROVIDER_ITEM);
		
		mProjection = new HashMap<String, String>();
		mProjection.put(COLUMNS_ID, COLUMNS_ID);
		mProjection.put(COLUMNS_NAME, COLUMNS_NAME);
		mProjection.put(COLUMNS_AGE, COLUMNS_AGE);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper{
		
		DatabaseHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = "CREATE TABLE " + TABLE_NAME + " ("
						+ COLUMNS_ID + " INTEGER PRIMARY KEY,"
						+ COLUMNS_NAME + " TEXT,"
						+ COLUMNS_AGE + " INTEGER"
						+ " );";
			db.execSQL(sql);
			
			initSameValue(db);
		}
		
		private void initSameValue(SQLiteDatabase db) {
			String sql = "INSERT INTO " + TABLE_NAME + " ( " +
						COLUMNS_ID + "," + 
						COLUMNS_HOURE + "," + 
						COLUMNS_MINUTE + " ) " + 
						" VALUES " + 
						" (1, 23, 0 ) ";
			db.execSQL(sql);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			db.execSQL("DROP TABLE IF EXITS " + TABLE_NAME);
			onCreate(db);
		}
		
	}
	
	private DatabaseHelper mOpenHelper;
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count = 0;
		switch (mUrimatcher.match(uri)){
		case MYPROVIDER:
			count = db.delete(TABLE_NAME, null, null);
			break;
		case MYPROVIDER_ITEM:
			String index = uri.getPathSegments().get(1); 
			count = db.delete(TABLE_NAME, COLUMNS_ID + "=" + index 
					+ (!(TextUtils.isEmpty(where)) ? " AND (" + where + ")" : ""), whereArgs);
			
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		default:
			throw new SQLException("Unkown Uri" + uri);
		}
		
		return count;
	}

	@Override
	public String getType(Uri uri) {

		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		if (mUrimatcher.match(uri) != MYPROVIDER){
			throw new IllegalArgumentException("UnKonw URI" + uri);
		}
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(TABLE_NAME, null, values);//COLUMNS_NAME 第2个参数的作用：如果传过来的values是经过了这样的操作
		/*
		 * ContentValues vaules = new ContentValues();
		   getContentResolver.insert(uri, values);
		        如果 这里的insert的第2个参数是null，数据库将不会有变化
		        如果这里参数指定了某个列名，那么插入到数据库中的值是:{null}
		 */
		
		if (rowId > 0){
			Uri tempUri = ContentUris.withAppendedId(CURRENT_URI, rowId);
			getContext().getContentResolver().notifyChange(tempUri, null);
			return tempUri;
		}
		//return null;
		throw new SQLException("Failed to insert row into " + uri); //捕捉插入失败的异常
	}

	@Override
	public boolean onCreate() {
		
		mOpenHelper = new DatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_NAME);
		
		switch(mUrimatcher.match(uri)){
		case MYPROVIDER:
			qb.setProjectionMap(mProjection);//必须加设置一个projectionmap，否则查询会得到一个空值。
			break;
		case MYPROVIDER_ITEM:
			qb.setProjectionMap(mProjection);//必须加设置一个projectionmap，否则查询会得到一个空值。
			qb.appendWhere(COLUMNS_ID + "=" + uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		int count = 0;
		switch(mUrimatcher.match(uri)){
		case MYPROVIDER:
			count = db.update(TABLE_NAME, values, selection, selectionArgs);
			break;
		case MYPROVIDER_ITEM:
			String index = uri.getPathSegments().get(1);
			count = db.update(TABLE_NAME, values, COLUMNS_ID + "=" + index 
						+((!TextUtils.isEmpty(selection)) ? " AND (" + selection + ")" : "") , selectionArgs);
			break;
		default:
			throw new SQLException("Unkown uri :" + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}










