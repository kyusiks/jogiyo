package com.anglab.jogiyo;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.anglab.jogiyo.ListViewer.ListAdapterWithButton;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

// 진한파랑 0057A3
// 하늘색 009BD9
// 주횡색 F0520E


// 메인테마 33BB77 (글씨, 아이콘)
// 가장검정 333333 (글씨)
// 흰색배경 FFFFFF
// 흰색1 F4F4F4 (연한회색배경)
// 흰색2 E4E4E4 (선색)

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnTouchListener,OnClickListener  {
	WebView mWebView;
	Spinner cbo_no;
    private NotesDbAdapter dbAdapter;
    static int gv_currentViewId = -1; // 현재 화면 저장

	List<HashMap<String, String>> list = new ArrayList<>();
	List<HashMap<String, String>> gv_backList = new ArrayList<>(); // 뒤로가기 버튼 저장

	HashMap<String, String> gv_setting   = new HashMap<String, String>(); // 프로그램세팅정보 VER세팅버전, URL프로그램 URL,VIEW_FIRST_YN첫회부터 볼것인가 최종회부터 볼것인가, THUMB_YN 썸네일을 표시할것인가
	HashMap<String, String> gv_imgViewer = new HashMap<String, String>(); // 사이트별 웹툰 조회 주소
	HashMap<String, String> gv_thumbComn = new HashMap<String, String>(); // 사이트별 썸네일 조회 주소
	HashMap<String, int[]>  gv_buttons   = new HashMap<String, int[]>();  // 터치시 버튼 색을 바꿍기위한값을 저장
	static HashMap<String, String> gv_setView = new HashMap<String, String>(); // 웹툰보는데 필요한 정보. CID,SITE,LST_VIEW_NO,MAX_NO,CBO_INDEX

	String vMode = "0"; // 0내구독목록, 1사이트목록에서의클릭, 2웹툰목록에서의 클릭, 3회차정보조회(2웹톤목록에서의클릭직후실행된다)
	String gv_nav = ""; // 내비게이션 제목 저장 (보통 웹툰 제목)
	String gv_isNewDate = ""; // 년월일시분초 가 저장되며 이 시간 이후의 게시물은 NEW

	String gv_svrUrlOrg = "http://anglab.dothome.co.kr/jogiyo/"; // 기본중에 기본.
	String gv_pgmName = "a.php";
	String gv_svrUrl = gv_svrUrlOrg; // 서버 주소. 후에 setting에서 어레이로 읽어오는걸로 대체.
	ArrayList<String> gv_svrList = new ArrayList<String>(); // 서버연동 프로그램 주소
	int gv_svrUrlArrayIndex = 0; // 서버연동 주소 어레이 선택 인덱스

	Context gv_this = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.loading);

        dbAdapter = new NotesDbAdapter(this);
        dbAdapter.open();

        /**********************************************/
        if ( !"느낌표붙이면개발서버로".equals("no") ) { //TODO 개발서버를 돌릴때 true
            //dbAdapter.fn_dbClear(); // 디비 초기화. ONLY TEST!!
        	gv_svrUrlOrg = "http://anglab.dothome.co.kr/jogiyo/";
        	gv_pgmName = "d.php";

        	//gv_svrUrlOrg = "http://anglab.url.ph/toontaku/";
        	//gv_pgmName = "a.php";
        }
        /**********************************************/

	    (new initLoadings()).execute();
	}

    /**
     * 시스템 초기화 ks20141201
     */
    class initLoadings extends AsyncTask<String, String , String> {
        protected void onPreExecute() {
    		if ( android.os.Build.VERSION.SDK_INT > 9 ) {
    			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    			StrictMode.setThreadPolicy(policy);
    		}
    		fn_setSetting();
        }
 
        protected String doInBackground(String ... values) {
            gv_buttons.put(""+ R.id.btn_search, new int[]{R.drawable.ic_mn_add, R.drawable.ic_mn_add_b});

        	//Log.d("버전", getSetting("APP_VER") + " / " + aUtil.getVersionName(gv_this));

    		// 앱의 버전업이 일어났는가 "1".equals("1") || 
            if ( false && !getSetting("APP_VER").equals(aUtil.getVersionName(gv_this)) ) { // 어플 버전업이 되었다면?
    			//if ( "-1".equals(data.get("VER004")) ) { // 정말 쌩으로 최초
    	        // 로컬 파일로 입력 후 서버를 호출한다.
                publishProgress("1 / 3");
    			getXmldata("LC002", "-1"); // LC002사이트정보/업종정보
    	        getXmldata("LC001", "-1"); // LC001웹툰목록 insert. 웹툰이 추가되는 경우가 많지 않아서 잘 없다.X/업체정보
    	        getXmldata("LC005", "-1"); // LC005세팅항목
                publishProgress("2 / 3");
    			getXmldata("LC003", "-1"); // 회차목록. 인기있는, 양 많은 웹툰만 넣어뒀다.
                publishProgress("3 / 3");

    			updSettingValue("FST_INS_DH", inqXmlParam("1-1", "")); // 이 날짜를 기준으로 이후 웹툰 인서트
    			updSettingValue("APP_VER", aUtil.getVersionName(gv_this)); // APP_VER 로컬버전,  WEB_VER 최신버전(웹서버에 업데이트되어있다.)

            } else {
            	try {
					Thread.sleep(500);
	                publishProgress("1");
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}

            publishProgress("2");
        	List<HashMap<String, String>> vList = getXmldata("J5", "");
        	if ( !vList.isEmpty() ) fn_setSetting();

            getXmldata("J1", ""); // 업종정보
            getXmldata("J2", ""); // 업종정보
            getXmldata("J3", ""); // 업종정보
            getXmldata("J4", ""); // 업종정보

    		return list.size() + "";
        }
 
        protected void onProgressUpdate(String ... values) {
        	if ( "1".equals(values[0]) ) {
            	alert("Hi! " + getSetting("MY_NM") + "!"); //alert(R.string.app_name);
        	} else if ( "2".equals(values[0]) ) {
	    		CDialog.showProgress(gv_this);
        	} else {
	    		CDialog.showProgress(gv_this, getResources().getText(R.string.str_firstLoading) + "\n" + values[0]);
        	}
    	}

        protected void onPostExecute(String values) {
        	//TODO 여기서 GPS 정보를 읽어온다. ddd1();
    		fn_menu("J1", "");

        	alert(""); // 토스트 지우려고.
    		CDialog.hideProgress();
        }
        protected void onCancelled() { }
    }

	/** 각 메뉴별 쿼리조회 */
	public void fn_menu(String pMode, String pParam) {
	    (new BackgroundTask()).execute(new String[] {pMode, pParam});
	}

    class BackgroundTask extends AsyncTask<String, String , String[]> {
        protected void onPreExecute() {
    		CDialog.showProgress(gv_this);
        }
 
        // vMode 0내가보는웹툰 1사이트 2사이트별웹툰목록 3웹툰회차목록 4검색 5세팅 6인기 
        protected String[] doInBackground(String ... values) {
        	String vMode  = values[0];
        	String vParam = values[1];
        	List<HashMap<String, String>> vList = new ArrayList<>();

        	if ( "J4".equals(vMode) ) {
                publishProgress(getResources().getString(R.string.str_confirmUpdating));
  				vList = getXmldata(vMode, vParam);
      		} else {
      			//Log.d("서버에 안감", "서버에 안감");
      		}

            publishProgress("");
    		list = dbAdapter.inqSql(vMode, vParam);
        	return values;
        }

        protected void onPostExecute(String[] values) {
			if ( gv_currentViewId != R.layout.activity_main ) fn_chgContentView(R.layout.activity_main);

        	String vMode1 = values[0];
        	String vParam = values[1];

			Log.d("vMode", vMode);
        	if ( "J6".equals(vMode1) ) {
    			fn_saveBack(vMode1, vParam); // 뒤로가기 버튼 정보 저장 끝
    			fn_chgContentView(R.layout.order);

				((TextView)findViewById(R.id.txt_siteNm)).setText(getView("SITE_NM"));
				((TextView)findViewById(R.id.txt_userId)).setText(getView("USER_ID"));

    			if ( !"".equals(getView("ORDER_SEQ"))) {
    				((TextView)findViewById(R.id.txt_orderSeq)).setText(getView("ORDER_SEQ"));
    				((EditText)findViewById(R.id.edt_helpme)).setText(getView("ORDER_CONTENTS"));
    				((TextView)findViewById(R.id.txt_cidNm)).setText(getView("CID_NM"));
    				((EditText)findViewById(R.id.edt_compContents)).setText(getView("COMP_CONTENTS"));

    				((Button)findViewById(R.id.btn_img)).setVisibility(View.GONE);
    				((Button)findViewById(R.id.btn_confirm)).setVisibility(View.GONE);
    				
    			} else {
    				((TableRow)findViewById(R.id.tableRow5)).setVisibility(View.GONE);
    				((TableRow)findViewById(R.id.tableRow6)).setVisibility(View.GONE);
    			}

        	} else if ( "J1".equals(vMode1) ) {
				fn_saveBack(vMode1, vParam); // 뒤로가기 버튼 정보 저장 끝
				fn_chgContentView(R.layout.activity_main);
    			fn_listAdapter(vMode1);

        	} else if ( "J4".equals(vMode1) ) {
				fn_saveBack(vMode1, vParam); // 뒤로가기 버튼 정보 저장 끝
				fn_chgContentView(R.layout.activity_main);
    			fn_listAdapter(vMode1);

    		} else {
    			fn_saveBack(vMode1, vParam); // 뒤로가기 버튼 정보 저장 끝
    			fn_listAdapter(vMode1);
    		}

    		vMode = vMode1;
    		if ( !gv_saveHist ) whenBackTouch(); // 뒤로가기 버튼으로 동작한 것이라면
    		CDialog.hideProgress();
        }

        protected void onProgressUpdate(String ... values) {
        	if ( "".equals(values[0]) ) {
        		CDialog.showProgress(gv_this);
        	} else {
        		CDialog.showProgress(gv_this, values[0]);
        	}
    	}
        protected void onCancelled() { }
    }

	public void ddd1() { // TODO

		//final TextView logView = (TextView)findViewById(R.id.txt_nav);
	    //logView.setText("GPS 가 잡혀야 좌표가 구해짐");
		alert("GPS 가 잡혀야 좌표가 구해짐");

	    // Acquire a reference to the system Location Manager
	    LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

	    // GPS 프로바이더 사용가능여부
	    boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	    // 네트워크 프로바이더 사용가능여부
	    boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

	    Log.d("Main", "isGPSEnabled="+ isGPSEnabled);
	    Log.d("Main", "isNetworkEnabled="+ isNetworkEnabled);

	    LocationListener locationListener = new LocationListener() {
	        public void onLocationChanged(Location location) {
	            double lat = location.getLatitude();
	            double lng = location.getLongitude();

	            //logView.setText("latitude: "+ lat +", longitude: "+ lng);
	    		alert("latitude: "+ lat +", longitude: "+ lng);
	        }

	        public void onStatusChanged(String provider, int status, Bundle extras) {
	            //logView.setText("onStatusChanged");
	    		alert("onStatusChanged");
	        }

	        public void onProviderEnabled(String provider) {
	            //logView.setText("onProviderEnabled");
	    		alert("onProviderEnabled");
	        }

	        public void onProviderDisabled(String provider) {
	            //logView.setText("onProviderDisabled");
	    		alert("onProviderDisabled");
	        }
	    };

	    // Register the listener with the Location Manager to receive location updates
	    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

	    // 수동으로 위치 구하기
	    String locationProvider = LocationManager.GPS_PROVIDER;
	    Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
	    if (lastKnownLocation != null) {
	        double lng = lastKnownLocation.getLatitude();
	        double lat = lastKnownLocation.getLatitude();
	        Log.d("Main", "longtitude=" + lng + ", latitude=" + lat);
	    }
	}

	private List<HashMap<String, String>> getXmldata(String pMode, String pParam) {
		List<HashMap<String, String>> vList = new ArrayList<>();
		try {
			String vParam = "";
			String vLstUpdDh = "";
			Long vLstUpdDhLong = (long)0;
			InputStream in = null;

			if ( "-1".equals(pParam) // 초기 실행이여서 로컬 파일 읽는다.
			  && ( "LC001".equals(pMode) || "LC002".equals(pMode) || "LC002".equals(pMode) || "LC005".equals(pMode) ) ) {
				in = getAssets().open("xml_list_" + pMode + ".xml");
			} else {

				vParam = pParam;
				// 파라메터 세팅
				if ( "J1".equals(pMode) || "J2".equals(pMode) || "J3".equals(pMode) 
				  || "J4".equals(pMode) || "J5".equals(pMode) ) {
					vLstUpdDh = inqXmlParam(pMode, vParam);
				} else {
					vLstUpdDh = vParam; // pParam은 보통 lst_upd_dh를 가진다.
				}

				String vProgram = gv_pgmName;
				if ( "J1".equals(pMode) || "J2".equals(pMode) || "J3".equals(pMode)
				  || "J4".equals(pMode) || "J5".equals(pMode) ) {
				} else if ( "J6".equals(pMode) ) { // 서버로 정보 던지는 단계
					String vValue = ((EditText) findViewById(R.id.edt_helpme)).getText().toString();
					vParam = "&ORDER_SEQ=" + getView("ORDER_SEQ") + "&SITE=" + getView("SITE");
					vParam += "&USER_X=" + getView("USER_X") + "&USER_Y=" + getView("USER_Y");
					vParam += "&ORDER_CONTENTS=" + URLEncoder.encode(vValue, "UTF-8");
				} else { // err처리
					return vList;
				}
				URL url = new URL(gv_svrUrl + vProgram + "?pMode=" + pMode + "&pMyNm="+ getSetting("MY_NM") + "&pLstUpdDh=" + vLstUpdDh + "&pParam=" + vParam);
				Log.d("url", "url : " +url);
				in = url.openStream();
			}

			XmlPullParserFactory fatorry = XmlPullParserFactory.newInstance();
			fatorry.setNamespaceAware(true);
			XmlPullParser xpp = fatorry.newPullParser();
			xpp.setInput(in, "utf-8"); // xpp.setInput(in, "euc-kr");

			int eventType = xpp.getEventType();
			String tagname = "";
			String vFirstTagName = ""; // 최초 태그네임을 저장한다. list의 원활한 저장을 위해.
			HashMap<String, String> data = new HashMap<String, String>();

			while ( eventType != XmlPullParser.END_DOCUMENT ) {
				if ( eventType == XmlPullParser.START_TAG ) {
					tagname = xpp.getName();
				} else if ( eventType == XmlPullParser.TEXT ) { // 태그별로 저장
					/**** LMultiData 형식으로 저장하기위한 꼼수. 크게 신경쓸 필요 없다. ****/
					if ( vFirstTagName.equals("") ) vFirstTagName = tagname;
					if ( vFirstTagName.equals(tagname) ) {
						if ( !data.isEmpty() ) {
							if ( !data.isEmpty() && "3".equals(pMode) && !data.containsKey("ID_SEQ") ) data.put("ID_SEQ", vParam);
							vList.add(new HashMap<String, String>(data));
							data.clear();
						}
					}
					/********/
					if ( "F".equals(tagname) && !"-1".equals(pParam) ) { // F:LST_UPD_DH 인 경우 숫자가 압축되어있다. 압축 풀어서 입력 ks20140416
						data.put(aUtil.sectionFind(tagname), Long.valueOf(xpp.getText()) + vLstUpdDhLong + ""); // 이게 중요한거다.
					} else {
						data.put(aUtil.sectionFind(tagname), xpp.getText()); // 이게 중요한거다.
					}
				} else if ( eventType == XmlPullParser.END_TAG ) {
				}
				eventType = xpp.next();
			}
			if ( !data.isEmpty() ) vList.add(new HashMap<String, String>(data));
//Log.d("list", vList.toString());
			if ( "J1".equals(pMode) || "J2".equals(pMode) || "J3".equals(pMode)
			  || "J4".equals(pMode) || "J5".equals(pMode) ) {
				dbAdapter.updList(pMode, vList);    // 로컬DB에 업데이트할게있으면 업뎃
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return vList;
	}

	// 웹툰을 보기위해 필요한 6가지 정보를 세팅한다.
	// CID,ID_SEQ,SITE,LST_VIEW_NO,MAX_NO,CBO_INDEX
	public void fn_setViewSetting(String pTag, String pValue) {
		gv_setView.put(pTag, pValue); // gv_setView 에 넣는 값을 좀 보기 좋을까 하고 이렇께 짜봤다.
	}

	// pArrList 배열로 넘어온 값을 적당히 가공하여 리스트업 한다.
	// 내비게이션바에 pNav 으로 세팅한다. pTF 가 true면 새로쓰기 아니면 이어쓰기
	public void fn_listAdapter(String pMode) {
		fn_chgContentView(R.layout.activity_main);
		gv_isNewDate = aUtil.getDataCal(); // 업데이트 기준 일자 세팅. 보통 현재의 24시간전.
		ListAdapterWithButton<String> adapter = new ListAdapterWithButton<String>(this, list);
		ListView listView = (ListView) findViewById(R.id.list);
		listView.setAdapter(adapter);
	}

	/*****************************
	 * OnClick 시작
	 *****************************/
	// ListViewer 의 클릭 이벤트에서 온다.
	public void fn_listOnClick(int pPosition) {
		gv_nav = ((TextView) findViewById(R.id.txt_nav)).getText() + " > " + getList(pPosition, "NAME");
		if ( "J1".equals(vMode) ) { 
			gv_setView.clear();
			fn_setViewSetting("SITE_NM", getList(pPosition, "SITE_NM"));
			fn_setViewSetting("USER_ID", getSetting("MY_NM"));
			fn_setViewSetting("SITE"   , getList(pPosition, "SITE"   ));
			fn_setViewSetting("USER_X" , getList(pPosition, "USER_X" ));
			fn_setViewSetting("USER_Y" , getList(pPosition, "USER_Y" ));
			fn_setViewSetting("ORDER_CONTENTS", getList(pPosition, "ORDER_CONTENTS"));
			
			fn_menu("J6", getView("SITE"));

		} else if ( "J4".equals(vMode) ) { 
			gv_setView.clear();
			fn_setViewSetting("SITE"     , getList(pPosition, "SITE"     ));
			fn_setViewSetting("SITE_NM"  , getList(pPosition, "SITE_NM"  ));
			fn_setViewSetting("USER_ID"  , getList(pPosition, "USER_ID"  ));
			fn_setViewSetting("ORDER_SEQ", getList(pPosition, "ORDER_SEQ"));
			fn_setViewSetting("ORDER_CONTENTS", getList(pPosition, "ORDER_CONTENTS"));


			fn_setViewSetting("CID_NM", getList(pPosition, "CID_NM"));
			fn_setViewSetting("COMP_CONTENTS", getList(pPosition, "COMP_CONTENTS"));
			
			
			fn_menu("J6", getView("ORDER_SEQ"));

		} else if ( "5".equals(vMode) ) { // 설정
			String vSelMode = getList(pPosition, "SEL_MODE");
			// vSelMode 에는 LINK:인터넷페이지로이동 COMBO:콤보 SWITCH:스위치 등이 있다.
			if ( "LINK".equals(vSelMode) ) {
				if ( "REVIEW".equals(getList(pPosition, "SET_ID")) ) {
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.anglab.toontaku"));
					startActivity(i);
				} else {
					//fn_viewerSetting(getList(pPosition, "SET_VALUE"));
				}
			} else if ( "intent".equals(vSelMode) ) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getList(pPosition, "SET_VALUE")));
				startActivity(i);
			} else if ( "donation".equals(vSelMode) ) {
				//fn_donation(getList(pPosition, "SET_CONT"));
			} else if ( "function".equals(vSelMode) ) {
				if ( "DB_CLEAR".equals(getList(pPosition, "SET_ID")) ) { // DB클리어
					fn_dbClear();
				}
			} else if ( "CheckBox".equals(vSelMode) ) {
				if ( !"".equals(getList(pPosition, "SET_CONT")) ) {
				    AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
				    alt_bld.setMessage(getList(pPosition, "SET_CONT"))
							.setCancelable(false)
							.setPositiveButton(R.string.str_yes,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
					AlertDialog alert = alt_bld.create();
					alert.setTitle(R.string.str_alert);
					alert.show();
				}
			} else {
			}
		} else { // 에러
		}
	}

	@Override
    public boolean onTouch(View v, MotionEvent event) {
		if ( gv_buttons.containsKey( "" + v.getId() ) ) { // 버튼 변하는 것들이면
			int[] vKeys = gv_buttons.get( "" + v.getId() );
	        if ( event.getAction() == MotionEvent.ACTION_DOWN ) { // 버튼을 누르고 있을 때
	        	findViewById(v.getId()).setBackgroundResource(vKeys[0]);
	        } else if ( event.getAction() == MotionEvent.ACTION_UP ) { //버튼에서 손을 떼었을 때 
	        	findViewById(v.getId()).setBackgroundResource(vKeys[1]);
	        }
		}
		return false;
	}

    @Override
	public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_confirm:
        	getXmldata("J6", "");
        	fn_menu("J4", ""); // 주문목록
            break;

        case R.id.btn_myList:
        	fn_menu("J1", "");
            break;

        case R.id.btn_search:
        	fn_menu("J4", "");
            break;
	    }
    }
	/*****************************
	 * OnClick 끝
	 *****************************/

	/*****************************
	 * UTIL 시작
	 *****************************/
    String gv_searchWord = "";
	// 화면을 바꾼다.
	public void fn_chgContentView(int pLayoutResId) {
		if ( gv_currentViewId == pLayoutResId ) return; // 현재화면과 바뀔화면이 같다면 리턴.

		// 없어저야 할 창의 사전작업 시작 //
		String vNav = "";
		if ( gv_currentViewId == R.layout.activity_main ) {
			vNav = ((TextView)findViewById(R.id.txt_nav)).getText().toString();
		}
		// 없어저야 할 창의 사전작업 끝 //

		gv_currentViewId = pLayoutResId;
		setContentView(gv_currentViewId);

		// 새로 생성된 화면의 사전작업 시작 //
		if ( gv_currentViewId == R.layout.activity_main ) {
			findViewById(R.id.btn_myList).setOnTouchListener(this);
			findViewById(R.id.btn_search).setOnTouchListener(this);
			findViewById(R.id.btn_myList).setOnClickListener(this);
			findViewById(R.id.btn_search).setOnClickListener(this);

		} else if ( gv_currentViewId == R.layout.order ) {
	        findViewById(R.id.btn_confirm ).setOnTouchListener(this);
	        findViewById(R.id.btn_confirm ).setOnClickListener(this);
			findViewById(R.id.btn_myList).setOnTouchListener(this);
			findViewById(R.id.btn_search).setOnTouchListener(this);
			findViewById(R.id.btn_myList).setOnClickListener(this);
			findViewById(R.id.btn_search).setOnClickListener(this);
		} else if ( gv_currentViewId == R.layout.loading ) {
		}
		// 새로 생성된 화면의 사전작업 끝 //
	}



	private ValueCallback<Uri> mUploadMessage;
	private final static int FILECHOOSER_RESULTCODE = 1;
	@Override
	protected  void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == FILECHOOSER_RESULTCODE) {
			if (null == mUploadMessage)
				return;
			Uri result = intent == null || resultCode != RESULT_OK ? null
					: intent.getData();
			mUploadMessage.onReceiveValue(result);
			mUploadMessage = null;
		}
	}
	
	
	
	// pTagName 을 가진 list 부분을 ArrayList로 리턴한다.
	public ArrayList<String> fn_convArrayList(String pTagName) {
		ArrayList<String> vArrayList = new ArrayList<String>();
		for ( int i = 0; i < list.size(); i++ ) {
			vArrayList.add(getList(i, pTagName));
		}
		return vArrayList;
	}

	// list 의 pTagName의 값중 pValue의 값을 가지고 있는 row를 리턴한다.
	public int fn_findListRow(String pTagName, String pValue) {
		if ( pTagName == null || pValue == null ) return -1;
		for ( int i = 0; i < list.size(); i++ ) {
			if ( pValue.equals(getList(i, pTagName)) ) {
				return i;
			}
		}
		return -1;
	}

	// thumbNail url을 가지고온다.
	public String fn_getUrl(int pPosition) {
		String vSite = NVL(getList(pPosition, "SITE"), getView("SITE")); // list에 SITE가 있으면 쓰고 없으면 글로벌셋에서 가져다 쓴다.
		String vCode = NVL(getList(pPosition, "MAX_NO"), getList(pPosition, "LINK_CODE")); // "3".equals(vMode)? getList(pPosition, "LINK_CODE") : getList(pPosition, "MAX_NO"); // 회차정보. 회차리스트면 LINK_CODE로, 내구독목록이면 MAX_NO를 가져온다.
		String vCid  = NVL(getList(pPosition, "CID"), getView("CID"));
		String vThumbUrl = (( getList(pPosition, "THUMB_NAIL").indexOf("http") == 0 )? "" : gv_thumbComn.get(vSite) ) + getList(pPosition, "THUMB_NAIL");
		return (vThumbUrl.replace("$cid", vCid).replace("$no", vCode));
	}

	// list의 pPosition 줄의 pTagName 값을 반환
	public String getList(int pPosition, String pTagName) {
		return NVL((String) list.get(pPosition).get(pTagName));
	}
	public String NVL(String pStr1) { return NVL(pStr1, ""); }
	public String NVL(String pStr1, String pStr2) {
		if ( pStr1 == null || pStr1.trim().length() == 0 ) {
			if ( pStr2 == null || pStr2.trim().length() == 0 ) pStr2 = "";
			return pStr2;
		}
		return pStr1;
	}

	public String fn_getMode() { return vMode; }
	public String getSetting(String pParam) { return NVL(gv_setting.get(pParam), ""); }
	public String getView(String pParam) { return NVL(gv_setView.get(pParam), ""); }

    // 뒤로가기 버튼 동작
	boolean gv_saveHist = true; // 뒤로가기 이력을 쌓으면 true
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if ( keyCode == KeyEvent.KEYCODE_BACK ) {
    		if ( gv_backList.isEmpty() || gv_backList.size() < 2 ) { // 최 상위 메뉴일때 뒤로버튼을 두번 클릭하면 앱이 종료된다.
    			if ( aUtil.chkTimer("BackButton", 2) ) { // 2초만에 클릭하면 시스템종료
    		        finish();
    		        System.exit(0);
    		        android.os.Process.killProcess(android.os.Process.myPid());
    		    } else {
    		        Toast.makeText(this, R.string.str_oneMoreFinish, Toast.LENGTH_SHORT).show();
    		    }
    		} else {
    			HashMap<String, String> data = (HashMap<String, String>) gv_backList.get(gv_backList.size() - 2);
    			gv_saveHist = false;
    			fn_menu(NVL(data.get("vMode")), NVL(data.get("vParam")));
    		}
    	}
		return false;
	}

    public void whenBackTouch() {
		gv_saveHist = true;
		HashMap<String, String> data = (HashMap<String, String>) gv_backList.get(gv_backList.size() - 1); // vPos, vNav
		gv_backList.remove(gv_backList.size() - 1);

		int vPos = Integer.valueOf(data.get("vPos"));
		if ( vPos > -1 && list.size() > vPos ) ((ListView)findViewById(R.id.list)).setSelection(vPos);
		((TextView)findViewById(R.id.txt_nav)).setText(data.get("vNav"));
    }

    // 뒤로가기 버튼을 저장한다.
    public void fn_saveBack(String pMode, String pParam) {
    	if ( !gv_saveHist ) return;
		if ( "J4".equals(pMode) || "J1".equals(pMode) ) gv_backList.clear(); // 내구독리스트 일때는 백버튼 초기화(더이상 뒤로갈수없음)

		if ( gv_backList.size() > 0 ) {
			HashMap<String, String> dataCompare = (HashMap<String, String>) gv_backList.get(gv_backList.size() - 1);
			if ( dataCompare.get("vMode").equals(pMode) && dataCompare.get("vParam").equals(pParam) ) return; // 같은 메뉴 두번 클릭은 패스
		}

		HashMap<String, String> data = new HashMap<String, String>();
		data.put("vMode" , pMode );
		data.put("vParam", pParam);
		data.put("vPos"  , ((ListView)findViewById(R.id.list)).getFirstVisiblePosition() + "");
		gv_backList.add(new HashMap<String, String>(data));
    }
    
	Toast mToast = null; // 메시지변경을 위해 전역으로 토스트 설정.
    public void alert(String pMsg) {
    	if ( "".equals(pMsg) ) {
    		if ( mToast != null ) mToast.cancel();
    	} else {
	    	if ( mToast != null ) {
				mToast.setText(pMsg);
			} else {
				mToast = Toast.makeText(gv_this, pMsg, Toast.LENGTH_SHORT);
			}
			mToast.show();
    	}
    }
    public void alert(int pMsg) {
		if ( mToast != null ) {
			mToast.setText(pMsg);
		} else {
			mToast = Toast.makeText(gv_this, pMsg, Toast.LENGTH_SHORT);
		}
		mToast.show();    	
    }
	/*****************************
	 * UTIL 끝
	 *****************************/

	/*****************************
	 * SQLite DB 컨트롤 영역 시작
	 *****************************/
	/** 프로그램 기본정보 로드 */
	public void fn_setSetting() {
        List<HashMap<String, String>> vList = dbAdapter.inqSql("J5", "");
        for ( HashMap<String, String> data : vList ) {
    		gv_setting.put(data.get("SET_ID"), data.get("SET_VALUE"));
        }
        Log.d("MY_NM", getSetting("MY_NM"));

	}

	public String inqXmlParam(String pMode, String pParam) {
		List<HashMap<String, String>> vList = dbAdapter.inqSql("S_" + pMode, pParam);
		if ( vList.isEmpty() ) return "";
		return vList.get(0).get("COL1");
	}

	/** 설정값 저장 */
	public void updSettingValue(String pSetId, String pSetValue) {
		dbAdapter.updSettingValue(pSetId, pSetValue);
		fn_setSetting();
	}

	/** DB클리어 */
	private void fn_dbClear() {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(R.string.str_dbClear)
				.setCancelable(false)
				.setPositiveButton(R.string.str_yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								//dbAdapter.delLC000All();
							}
						})
				.setNegativeButton(R.string.str_no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = alt_bld.create();
		alert.setTitle(R.string.str_rusure);
		alert.show();
	}
	/*****************************
	 * SQLite DB 컨트롤 영역 끝
	 *****************************/
}