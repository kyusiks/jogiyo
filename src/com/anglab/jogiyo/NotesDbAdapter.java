package com.anglab.jogiyo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

@SuppressLint("NewApi")
public class NotesDbAdapter {
   public static final String KEY_TITLE = "title";
   public static final String KEY_BODY = "body";
   public static final String KEY_ROWID = "_id";

   private DatabaseHelper mDbHelper;
   private SQLiteDatabase mDb;

   /**
    * Database creation sql statement
    */
   private static final String DATABASE_CREATE = "create table notes (_id integer primary key autoincrement, "
                                               + "title text not null, body text not null);";
   private static final String gv_create_TB_LC000 /* 내구독목록_ */ = "CREATE TABLE TB_LC000 (SITE TEXT PRIMARY KEY, NAME TEXT, USE_YN TEXT, LST_UPD_DH TEXT);";
   private static final String gv_create_TB_LC001 /* 업종원장 */ = "CREATE TABLE TB_LC001 (SITE TEXT PRIMARY KEY, NAME TEXT, USE_YN TEXT, LST_UPD_DH TEXT);";
   private static final String gv_create_TB_LC002 /* 업체원장 */ = "CREATE TABLE TB_LC002 (CID TEXT, ID_SEQ TEXT, NAME TEXT, LOC_X TEXT, LOC_Y TEXT, FST_INS_DH TEXT, USE_YN TEXT, LST_UPD_DH TEXT, PRIMARY KEY(CID, ID_SEQ));";
   private static final String gv_create_TB_LC003 /* 업체상세 */ = "CREATE TABLE TB_LC003 (CID TEXT, ID_SEQ TEXT, SITE TEXT, FST_INS_DH TEXT, USE_YN TEXT, LST_UPD_DH TEXT, PRIMARY KEY(CID, ID_SEQ, SITE));";
   private static final String gv_create_TB_LC004 /* 주문원장 */ = "CREATE TABLE TB_LC004 (ORDER_SEQ TEXT PRIMARY KEY, STATUS TEXT, USER_ID TEXT, SITE TEXT, ORDER_CONTENTS TEXT, USER_X TEXT, USER_Y TEXT, CID TEXT, ID_SEQ TEXT, COMP_CONTENTS TEXT, FST_INS_DH TEXT, LST_UPD_DH TEXT);";
   private static final String gv_create_TB_LC005 /* 설정정보 */ = "CREATE TABLE TB_LC005 (SET_ID TEXT PRIMARY KEY, SET_NM TEXT NOT NULL, SEL_MODE TEXT NOT NULL, SET_VALUE TEXT, SET_CONT TEXT, SORT TEXT, USE_YN TEXT, LST_UPD_DH TEXT);";

   private static final String DATABASE_NAME = "data";
   private static final String DATABASE_TABLE = "notes";
   private static final int DATABASE_VERSION = 2;
   private final Context mCtx;

   private static class DatabaseHelper extends SQLiteOpenHelper {
       DatabaseHelper(Context context) {
           super(context, DATABASE_NAME, null, DATABASE_VERSION);
       }

       @Override
       public void onCreate(SQLiteDatabase db) {
           //db.execSQL(gv_create_TB_LC000);
           db.execSQL(gv_create_TB_LC001);
           db.execSQL(gv_create_TB_LC002);
           db.execSQL(gv_create_TB_LC003);
           db.execSQL(gv_create_TB_LC004);
           db.execSQL(gv_create_TB_LC005);
       }

       @Override
       public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
           Log.w("NotesDbAdapter", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

           
           db.execSQL("DROP TABLE IF EXISTS notes");
           db.execSQL("DROP TABLE IF EXISTS TB_LC000");
           db.execSQL("DROP TABLE IF EXISTS TB_LC001");
           db.execSQL("DROP TABLE IF EXISTS TB_LC002");
           db.execSQL("DROP TABLE IF EXISTS TB_LC003");
           db.execSQL("DROP TABLE IF EXISTS TB_LC004");
           db.execSQL("DROP TABLE IF EXISTS TB_LC005");
           onCreate(db); // 데이터 유실을 막아보고자..
           
       }
       public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
           Log.w("NotesDbAdapter", "Downgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
       }

   }

   public NotesDbAdapter(Context ctx) { this.mCtx = ctx; }

   public NotesDbAdapter open() throws SQLException {
       mDbHelper = new DatabaseHelper(mCtx);
       mDb = mDbHelper.getWritableDatabase();
       return this;
   }

   public void close() { mDbHelper.close(); }

   public long createNote(String title, String body) {
       ContentValues initialValues = new ContentValues();
       initialValues.put(KEY_TITLE, title);
       initialValues.put(KEY_BODY, body);
       return mDb.insert(DATABASE_TABLE, null, initialValues);
   }

   public boolean deleteNote(long rowId) {
       Log.i("Delete called", "value__" + rowId);
       return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
   }

   public Cursor fetchAllNotes() {
       return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_TITLE, KEY_BODY }, null, null, null, null, null);
   }

   public Cursor fetchNote(long rowId) throws SQLException {
       Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_TITLE, KEY_BODY }, KEY_ROWID
               + "=" + rowId, null, null, null, null, null);
       if (mCursor != null) {
           mCursor.moveToFirst();
       }
       return mCursor;
   }

   public boolean updateNote(long rowId, String title, String body) {
       ContentValues args = new ContentValues();
       args.put(KEY_TITLE, title);
       args.put(KEY_BODY, body);
       return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
   }

   public List<HashMap<String, String>> inqSql(String pMode, String pParam) {
	   String vTemp = "";
	   Log.d("inqSql", pMode);
	   if ( pMode == null || "".equals(pMode) ) {
	   } else if ( "J1".equals(pMode) ) { // 위치를 이용한 업종
		   vTemp = "SELECT LC001.SITE, LC001.NAME || '(' || COUNT(1) || ')' AS NAME, LC001.NAME AS SITE_NM "
				 + "  FROM TB_LC001 LC001, TB_LC002 LC002, TB_LC003 LC003 "
				 + " WHERE LC001.SITE = LC003.SITE "
				 + "   AND LC002.CID = LC003.CID "
				 + "   AND LC002.ID_SEQ = LC003.ID_SEQ "
				 + " GROUP BY LC001.SITE, LC001.NAME"
				 + " ORDER BY LC001.NAME ";
	   } else if ( "J4".equals(pMode) ) {
		   vTemp = "SELECT A.ORDER_SEQ, A.USER_ID, A.SITE, A.ORDER_CONTENTS, A.USER_X, A.USER_Y, A.CID "
				 + "     , A.ID_SEQ, A.COMP_CONTENTS, A.FST_INS_DH, A.LST_UPD_DH, A.STATUS "
				 + "     , CASE A.STATUS WHEN '1' THEN '요청' WHEN '2' THEN '접수(업체선정중)' WHEN '3' THEN '업체선정(곧 연락이 갑니다.)' WHEN '4' THEN '업체출장확정' WHEN '5' THEN '종료' ELSE '기타' END AS STATE_NM "
				 + "     , CASE A.STATUS WHEN ''  THEN '요청' WHEN '1' THEN '접수(업체선정중)' WHEN '2' THEN '업체선정(곧 연락이 갑니다.)' WHEN '3' THEN '업체출장확정' WHEN '4' THEN '종료' ELSE '기타' END AS NEXT_STEP "
				 + "     , SUBSTR(TRIM(REPLACE(A.ORDER_CONTENTS, '\n', ' ')), 1, 15) || '...' AS NAME "
				 + "     , B.NAME AS SITE_NM "
				 + "     , C.NAME AS CID_NM "
				 + "  FROM TB_LC004 A LEFT OUTER JOIN TB_LC002 C ON C.CID=A.CID AND C.ID_SEQ=A.ID_SEQ , TB_LC001 B"
				 + " WHERE A.SITE = B.SITE ORDER BY A.FST_INS_DH DESC";
	   } else if ( "J5".equals(pMode) ) {
		   vTemp = "SELECT SET_ID, SET_NM, SEL_MODE, SET_VALUE, SET_CONT, SORT, LST_UPD_DH "
				 + "  FROM TB_LC005 WHERE USE_YN != 'N' ";
	   } else if ( "J6".equals(pMode) ) {
		   return ((MainActivity) mCtx).list;
	   } else if ( "S_J1".equals(pMode) ) {
		   vTemp = "SELECT IFNULL(MAX(LST_UPD_DH), '-1') AS COL1 FROM TB_LC001 ";
	   } else if ( "S_J2".equals(pMode) ) {
		   vTemp = "SELECT IFNULL(MAX(LST_UPD_DH), '-1') AS COL1 FROM TB_LC002 ";
	   } else if ( "S_J3".equals(pMode) ) {
		   vTemp = "SELECT IFNULL(MAX(LST_UPD_DH), '-1') AS COL1 FROM TB_LC003 ";
	   } else if ( "S_J4".equals(pMode) ) {
		   vTemp = "SELECT IFNULL(MAX(LST_UPD_DH), '-1') AS COL1 FROM TB_LC004 ";
	   } else if ( "S_J5".equals(pMode) ) {
		   vTemp = "SELECT IFNULL(MAX(LST_UPD_DH), '-1') AS COL1 FROM TB_LC005 ";
	   } else if ( "".equals(pMode) ) {
   	   } else {
   		   return null;
	   }

	   Log.d("inqSq vTemp", vTemp);
	   Cursor result = mDb.rawQuery(vTemp, null);

	   List<HashMap<String, String>> vList = new ArrayList<>();
       if ( result.getCount() > 0 ) {
	       result.moveToFirst(); // 사이트를 키로 이미지뷰어, 썸네일 주소가 저장된다.
	       HashMap<String, String> data = new HashMap<String, String>();
	       while ( !result.isAfterLast() ) {
				data.clear();
				for ( int i = 0; i < result.getColumnCount(); i++ ) {
					if ( "NAME".equals(result.getColumnName(i))  // 특수문자 대치
					  && result.getString(i).indexOf("&") >= 0 ) {
						data.put(result.getColumnName(i), result.getString(i).replace("&#039;", "'").replace("&lt;", "<").replace("&gt;", ">"));
					} else {
						data.put(result.getColumnName(i), result.getString(i));	
					}
				}
				vList.add(new HashMap<String, String>(data));
				result.moveToNext();
	       }
       }
       result.close();
	   return vList;
   }

   // 웹툰리스트 버전 업 업데이트
   public void updList(String pMode, List<HashMap<String, String>> pList) {
	   Log.d("updList", "mode : " + pMode + " / " + pList.size() + " row update");
	   if ( pList.isEmpty() ) return;

	   String vSql = "";
	   if ( "J1".equals(pMode) ) {
		   vSql = "INSERT OR REPLACE INTO TB_LC001 (SITE, NAME, USE_YN, LST_UPD_DH) VALUES (?, ?, ?, ?)";
	   } else if ( "J2".equals(pMode) ) {
		   vSql = "INSERT OR REPLACE INTO TB_LC002 (CID, ID_SEQ, NAME, LOC_X, LOC_Y, FST_INS_DH, USE_YN, LST_UPD_DH) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	   } else if ( "J3".equals(pMode) ) {
		   vSql = "INSERT OR REPLACE INTO TB_LC003 (CID, ID_SEQ, SITE, FST_INS_DH, USE_YN, LST_UPD_DH) VALUES (?, ?, ?, ?, ?, ?)";
	   } else if ( "J4".equals(pMode) ) {
		   vSql = "INSERT OR REPLACE INTO TB_LC004 (ORDER_SEQ, STATUS, USER_ID, SITE, ORDER_CONTENTS, USER_X, USER_Y, CID, ID_SEQ, COMP_CONTENTS, FST_INS_DH, LST_UPD_DH) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	   } else if ( "J5".equals(pMode) ) {
		   vSql = "INSERT OR REPLACE INTO TB_LC005 (SET_ID, SET_NM, SEL_MODE, SET_VALUE, SET_CONT, SORT, USE_YN, LST_UPD_DH) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	   }

	   mDb.beginTransaction();
	   SQLiteStatement insert = mDb.compileStatement(vSql);

	   if ( "J1".equals(pMode) ) {
		   String vLstUpdDhMax = "";
		   for ( int i = 0; i < pList.size(); i++ ) {
			   if ( vLstUpdDhMax.compareTo(fn_getList(pList.get(i), "LST_UPD_DH")) < 0 ) vLstUpdDhMax = fn_getList(pList.get(i), "LST_UPD_DH");
			   insert.bindString(1, fn_getList(pList.get(i), "SITE"));
			   insert.bindString(2, fn_getList(pList.get(i), "NAME"));
			   insert.bindString(3, fn_getList(pList.get(i), "USE_YN"));
			   insert.bindString(4, fn_getList(pList.get(i), "LST_UPD_DH"));
			   insert.execute();
		   }
		   if ( !"".equals(vLstUpdDhMax) ) ((MainActivity) mCtx).updSettingValue("FST_INS_DH", vLstUpdDhMax);

	   } else if ( "J2".equals(pMode) ) {
		   for ( int i = 0; i < pList.size(); i++ ) {
			   insert.bindString(1, fn_getList(pList.get(i), "CID"));
			   insert.bindString(2, fn_getList(pList.get(i), "ID_SEQ"));
			   insert.bindString(3, fn_getList(pList.get(i), "NAME"));
			   insert.bindString(4, fn_getList(pList.get(i), "LOC_X"));
			   insert.bindString(5, fn_getList(pList.get(i), "LOC_Y"));
			   insert.bindString(6, fn_getList(pList.get(i), "FST_INS_DH"));
			   insert.bindString(7, fn_getList(pList.get(i), "USE_YN"));
			   insert.bindString(8, fn_getList(pList.get(i), "LST_UPD_DH"));
			   insert.execute();
		   }

	   } else if ( "J3".equals(pMode) ) {
		   for ( int i = 0; i < pList.size(); i++ ) {
			   insert.bindString(1, fn_getList(pList.get(i), "CID"));
			   insert.bindString(2, fn_getList(pList.get(i), "ID_SEQ"));
			   insert.bindString(3, fn_getList(pList.get(i), "SITE"));
			   insert.bindString(4, fn_getList(pList.get(i), "FST_INS_DH"));
			   insert.bindString(5, fn_getList(pList.get(i), "USE_YN"));
			   insert.bindString(6, fn_getList(pList.get(i), "LST_UPD_DH"));
			   insert.execute();
		   }

	   } else if ( "J4".equals(pMode) ) {
		   for ( int i = 0; i < pList.size(); i++ ) {
			   insert.bindString(1, fn_getList(pList.get(i), "ORDER_SEQ"));
			   insert.bindString(2, fn_getList(pList.get(i), "STATUS"));
			   insert.bindString(3, fn_getList(pList.get(i), "USER_ID"));
			   insert.bindString(4, fn_getList(pList.get(i), "SITE"));
			   insert.bindString(5, fn_getList(pList.get(i), "ORDER_CONTENTS"));
			   insert.bindString(6, fn_getList(pList.get(i), "USER_X"));
			   insert.bindString(7, fn_getList(pList.get(i), "USER_Y"));
			   insert.bindString(8, fn_getList(pList.get(i), "CID"));
			   insert.bindString(9, fn_getList(pList.get(i), "ID_SEQ"));
			   insert.bindString(10, fn_getList(pList.get(i), "COMP_CONTENTS"));
			   insert.bindString(11, fn_getList(pList.get(i), "FST_INS_DH"));
			   insert.bindString(12, fn_getList(pList.get(i), "LST_UPD_DH"));
			   insert.execute();
		   }

	   } else if ( "J5".equals(pMode) ) {
		   for ( int i = 0; i < pList.size(); i++ ) {
			   insert.bindString(1, fn_getList(pList.get(i), "SET_ID"));
			   insert.bindString(2, fn_getList(pList.get(i), "NAME"));
			   insert.bindString(3, fn_getList(pList.get(i), "SEL_MODE"));
			   insert.bindString(4, fn_getList(pList.get(i), "SET_VALUE"));
			   insert.bindString(5, fn_getList(pList.get(i), "SET_CONT"));
			   insert.bindString(6, fn_getList(pList.get(i), "SORT"));
			   insert.bindString(7, fn_getList(pList.get(i), "USE_YN"));
			   insert.bindString(8, fn_getList(pList.get(i), "LST_UPD_DH"));
			   insert.execute();
		   }
	   }

	   mDb.setTransactionSuccessful();
	   mDb.endTransaction();
   }

   public String fn_getList(HashMap<String, String> map, String pTagname) {
	   if ( map.containsKey(pTagname) ) {
		   return (String) map.get(pTagname);
	   } else {
		   return "";
	   }
   }

   /**** 설정관련 ****/
   public void fn_dbClear() {
	   // 초기화로직
	   mDb.execSQL("DROP TABLE IF EXISTS TB_LC001"); mDb.execSQL(gv_create_TB_LC001);
	   mDb.execSQL("DROP TABLE IF EXISTS TB_LC002"); mDb.execSQL(gv_create_TB_LC002);
	   mDb.execSQL("DROP TABLE IF EXISTS TB_LC003"); mDb.execSQL(gv_create_TB_LC003);
	   mDb.execSQL("DROP TABLE IF EXISTS TB_LC004"); mDb.execSQL(gv_create_TB_LC004);
	   mDb.execSQL("DROP TABLE IF EXISTS TB_LC005"); mDb.execSQL(gv_create_TB_LC005);
   }

   public void updSettingValue(String pSetId, String pSetValue) {
       ContentValues initialValues = new ContentValues();
       initialValues.put("SET_VALUE", pSetValue);
       int i = mDb.update("TB_LC005", initialValues, "SET_ID = '" + pSetId + "'", null);
       if ( i == 0 ) {
           initialValues.put("SET_ID", pSetId);
           initialValues.put("SET_NM", "");
           initialValues.put("SEL_MODE", "");
           initialValues.put("SET_CONT", "");
           initialValues.put("SORT", "");
           initialValues.put("USE_YN", "");
           initialValues.put("LST_UPD_DH", "");
           initialValues.put("SET_NM", "");

    	   mDb.insert("TB_LC005", null, initialValues);
       }
   }
   /********/
}