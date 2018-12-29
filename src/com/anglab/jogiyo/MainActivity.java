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

// �����Ķ� 0057A3
// �ϴû� 009BD9
// ��Ⱦ�� F0520E


// �����׸� 33BB77 (�۾�, ������)
// ������� 333333 (�۾�)
// ������ FFFFFF
// ���1 F4F4F4 (����ȸ�����)
// ���2 E4E4E4 (����)

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnTouchListener,OnClickListener  {
	WebView mWebView;
	Spinner cbo_no;
    private NotesDbAdapter dbAdapter;
    static int gv_currentViewId = -1; // ���� ȭ�� ����

	List<HashMap<String, String>> list = new ArrayList<>();
	List<HashMap<String, String>> gv_backList = new ArrayList<>(); // �ڷΰ��� ��ư ����

	HashMap<String, String> gv_setting   = new HashMap<String, String>(); // ���α׷��������� VER���ù���, URL���α׷� URL,VIEW_FIRST_YNùȸ���� �����ΰ� ����ȸ���� �����ΰ�, THUMB_YN ������� ǥ���Ұ��ΰ�
	HashMap<String, String> gv_imgViewer = new HashMap<String, String>(); // ����Ʈ�� ���� ��ȸ �ּ�
	HashMap<String, String> gv_thumbComn = new HashMap<String, String>(); // ����Ʈ�� ����� ��ȸ �ּ�
	HashMap<String, int[]>  gv_buttons   = new HashMap<String, int[]>();  // ��ġ�� ��ư ���� �ٲ�����Ѱ��� ����
	static HashMap<String, String> gv_setView = new HashMap<String, String>(); // �������µ� �ʿ��� ����. CID,SITE,LST_VIEW_NO,MAX_NO,CBO_INDEX

	String vMode = "0"; // 0���������, 1����Ʈ��Ͽ�����Ŭ��, 2������Ͽ����� Ŭ��, 3ȸ��������ȸ(2�����Ͽ�����Ŭ�����Ľ���ȴ�)
	String gv_nav = ""; // ������̼� ���� ���� (���� ���� ����)
	String gv_isNewDate = ""; // ����Ͻú��� �� ����Ǹ� �� �ð� ������ �Խù��� NEW

	String gv_svrUrlOrg = "http://anglab.dothome.co.kr/jogiyo/"; // �⺻�߿� �⺻.
	String gv_pgmName = "a.php";
	String gv_svrUrl = gv_svrUrlOrg; // ���� �ּ�. �Ŀ� setting���� ��̷� �о���°ɷ� ��ü.
	ArrayList<String> gv_svrList = new ArrayList<String>(); // �������� ���α׷� �ּ�
	int gv_svrUrlArrayIndex = 0; // �������� �ּ� ��� ���� �ε���

	Context gv_this = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.loading);

        dbAdapter = new NotesDbAdapter(this);
        dbAdapter.open();

        /**********************************************/
        if ( !"����ǥ���̸鰳�߼�����".equals("no") ) { //TODO ���߼����� ������ true
            //dbAdapter.fn_dbClear(); // ��� �ʱ�ȭ. ONLY TEST!!
        	gv_svrUrlOrg = "http://anglab.dothome.co.kr/jogiyo/";
        	gv_pgmName = "d.php";

        	//gv_svrUrlOrg = "http://anglab.url.ph/toontaku/";
        	//gv_pgmName = "a.php";
        }
        /**********************************************/

	    (new initLoadings()).execute();
	}

    /**
     * �ý��� �ʱ�ȭ ks20141201
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

        	//Log.d("����", getSetting("APP_VER") + " / " + aUtil.getVersionName(gv_this));

    		// ���� �������� �Ͼ�°� "1".equals("1") || 
            if ( false && !getSetting("APP_VER").equals(aUtil.getVersionName(gv_this)) ) { // ���� �������� �Ǿ��ٸ�?
    			//if ( "-1".equals(data.get("VER004")) ) { // ���� ������ ����
    	        // ���� ���Ϸ� �Է� �� ������ ȣ���Ѵ�.
                publishProgress("1 / 3");
    			getXmldata("LC002", "-1"); // LC002����Ʈ����/��������
    	        getXmldata("LC001", "-1"); // LC001������� insert. ������ �߰��Ǵ� ��찡 ���� �ʾƼ� �� ����.X/��ü����
    	        getXmldata("LC005", "-1"); // LC005�����׸�
                publishProgress("2 / 3");
    			getXmldata("LC003", "-1"); // ȸ�����. �α��ִ�, �� ���� ������ �־�״�.
                publishProgress("3 / 3");

    			updSettingValue("FST_INS_DH", inqXmlParam("1-1", "")); // �� ��¥�� �������� ���� ���� �μ�Ʈ
    			updSettingValue("APP_VER", aUtil.getVersionName(gv_this)); // APP_VER ���ù���,  WEB_VER �ֽŹ���(�������� ������Ʈ�Ǿ��ִ�.)

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

            getXmldata("J1", ""); // ��������
            getXmldata("J2", ""); // ��������
            getXmldata("J3", ""); // ��������
            getXmldata("J4", ""); // ��������

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
        	//TODO ���⼭ GPS ������ �о�´�. ddd1();
    		fn_menu("J1", "");

        	alert(""); // �佺Ʈ �������.
    		CDialog.hideProgress();
        }
        protected void onCancelled() { }
    }

	/** �� �޴��� ������ȸ */
	public void fn_menu(String pMode, String pParam) {
	    (new BackgroundTask()).execute(new String[] {pMode, pParam});
	}

    class BackgroundTask extends AsyncTask<String, String , String[]> {
        protected void onPreExecute() {
    		CDialog.showProgress(gv_this);
        }
 
        // vMode 0������������ 1����Ʈ 2����Ʈ��������� 3����ȸ����� 4�˻� 5���� 6�α� 
        protected String[] doInBackground(String ... values) {
        	String vMode  = values[0];
        	String vParam = values[1];
        	List<HashMap<String, String>> vList = new ArrayList<>();

        	if ( "J4".equals(vMode) ) {
                publishProgress(getResources().getString(R.string.str_confirmUpdating));
  				vList = getXmldata(vMode, vParam);
      		} else {
      			//Log.d("������ �Ȱ�", "������ �Ȱ�");
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
    			fn_saveBack(vMode1, vParam); // �ڷΰ��� ��ư ���� ���� ��
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
				fn_saveBack(vMode1, vParam); // �ڷΰ��� ��ư ���� ���� ��
				fn_chgContentView(R.layout.activity_main);
    			fn_listAdapter(vMode1);

        	} else if ( "J4".equals(vMode1) ) {
				fn_saveBack(vMode1, vParam); // �ڷΰ��� ��ư ���� ���� ��
				fn_chgContentView(R.layout.activity_main);
    			fn_listAdapter(vMode1);

    		} else {
    			fn_saveBack(vMode1, vParam); // �ڷΰ��� ��ư ���� ���� ��
    			fn_listAdapter(vMode1);
    		}

    		vMode = vMode1;
    		if ( !gv_saveHist ) whenBackTouch(); // �ڷΰ��� ��ư���� ������ ���̶��
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
	    //logView.setText("GPS �� ������ ��ǥ�� ������");
		alert("GPS �� ������ ��ǥ�� ������");

	    // Acquire a reference to the system Location Manager
	    LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

	    // GPS ���ι��̴� ��밡�ɿ���
	    boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	    // ��Ʈ��ũ ���ι��̴� ��밡�ɿ���
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

	    // �������� ��ġ ���ϱ�
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

			if ( "-1".equals(pParam) // �ʱ� �����̿��� ���� ���� �д´�.
			  && ( "LC001".equals(pMode) || "LC002".equals(pMode) || "LC002".equals(pMode) || "LC005".equals(pMode) ) ) {
				in = getAssets().open("xml_list_" + pMode + ".xml");
			} else {

				vParam = pParam;
				// �Ķ���� ����
				if ( "J1".equals(pMode) || "J2".equals(pMode) || "J3".equals(pMode) 
				  || "J4".equals(pMode) || "J5".equals(pMode) ) {
					vLstUpdDh = inqXmlParam(pMode, vParam);
				} else {
					vLstUpdDh = vParam; // pParam�� ���� lst_upd_dh�� ������.
				}

				String vProgram = gv_pgmName;
				if ( "J1".equals(pMode) || "J2".equals(pMode) || "J3".equals(pMode)
				  || "J4".equals(pMode) || "J5".equals(pMode) ) {
				} else if ( "J6".equals(pMode) ) { // ������ ���� ������ �ܰ�
					String vValue = ((EditText) findViewById(R.id.edt_helpme)).getText().toString();
					vParam = "&ORDER_SEQ=" + getView("ORDER_SEQ") + "&SITE=" + getView("SITE");
					vParam += "&USER_X=" + getView("USER_X") + "&USER_Y=" + getView("USER_Y");
					vParam += "&ORDER_CONTENTS=" + URLEncoder.encode(vValue, "UTF-8");
				} else { // erró��
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
			String vFirstTagName = ""; // ���� �±׳����� �����Ѵ�. list�� ��Ȱ�� ������ ����.
			HashMap<String, String> data = new HashMap<String, String>();

			while ( eventType != XmlPullParser.END_DOCUMENT ) {
				if ( eventType == XmlPullParser.START_TAG ) {
					tagname = xpp.getName();
				} else if ( eventType == XmlPullParser.TEXT ) { // �±׺��� ����
					/**** LMultiData �������� �����ϱ����� �ļ�. ũ�� �Ű澵 �ʿ� ����. ****/
					if ( vFirstTagName.equals("") ) vFirstTagName = tagname;
					if ( vFirstTagName.equals(tagname) ) {
						if ( !data.isEmpty() ) {
							if ( !data.isEmpty() && "3".equals(pMode) && !data.containsKey("ID_SEQ") ) data.put("ID_SEQ", vParam);
							vList.add(new HashMap<String, String>(data));
							data.clear();
						}
					}
					/********/
					if ( "F".equals(tagname) && !"-1".equals(pParam) ) { // F:LST_UPD_DH �� ��� ���ڰ� ����Ǿ��ִ�. ���� Ǯ� �Է� ks20140416
						data.put(aUtil.sectionFind(tagname), Long.valueOf(xpp.getText()) + vLstUpdDhLong + ""); // �̰� �߿��ѰŴ�.
					} else {
						data.put(aUtil.sectionFind(tagname), xpp.getText()); // �̰� �߿��ѰŴ�.
					}
				} else if ( eventType == XmlPullParser.END_TAG ) {
				}
				eventType = xpp.next();
			}
			if ( !data.isEmpty() ) vList.add(new HashMap<String, String>(data));
//Log.d("list", vList.toString());
			if ( "J1".equals(pMode) || "J2".equals(pMode) || "J3".equals(pMode)
			  || "J4".equals(pMode) || "J5".equals(pMode) ) {
				dbAdapter.updList(pMode, vList);    // ����DB�� ������Ʈ�Ұ������� ����
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return vList;
	}

	// ������ �������� �ʿ��� 6���� ������ �����Ѵ�.
	// CID,ID_SEQ,SITE,LST_VIEW_NO,MAX_NO,CBO_INDEX
	public void fn_setViewSetting(String pTag, String pValue) {
		gv_setView.put(pTag, pValue); // gv_setView �� �ִ� ���� �� ���� ������ �ϰ� �̷��� ¥�ô�.
	}

	// pArrList �迭�� �Ѿ�� ���� ������ �����Ͽ� ����Ʈ�� �Ѵ�.
	// ������̼ǹٿ� pNav ���� �����Ѵ�. pTF �� true�� ���ξ��� �ƴϸ� �̾��
	public void fn_listAdapter(String pMode) {
		fn_chgContentView(R.layout.activity_main);
		gv_isNewDate = aUtil.getDataCal(); // ������Ʈ ���� ���� ����. ���� ������ 24�ð���.
		ListAdapterWithButton<String> adapter = new ListAdapterWithButton<String>(this, list);
		ListView listView = (ListView) findViewById(R.id.list);
		listView.setAdapter(adapter);
	}

	/*****************************
	 * OnClick ����
	 *****************************/
	// ListViewer �� Ŭ�� �̺�Ʈ���� �´�.
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

		} else if ( "5".equals(vMode) ) { // ����
			String vSelMode = getList(pPosition, "SEL_MODE");
			// vSelMode ���� LINK:���ͳ����������̵� COMBO:�޺� SWITCH:����ġ ���� �ִ�.
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
				if ( "DB_CLEAR".equals(getList(pPosition, "SET_ID")) ) { // DBŬ����
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
		} else { // ����
		}
	}

	@Override
    public boolean onTouch(View v, MotionEvent event) {
		if ( gv_buttons.containsKey( "" + v.getId() ) ) { // ��ư ���ϴ� �͵��̸�
			int[] vKeys = gv_buttons.get( "" + v.getId() );
	        if ( event.getAction() == MotionEvent.ACTION_DOWN ) { // ��ư�� ������ ���� ��
	        	findViewById(v.getId()).setBackgroundResource(vKeys[0]);
	        } else if ( event.getAction() == MotionEvent.ACTION_UP ) { //��ư���� ���� ������ �� 
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
        	fn_menu("J4", ""); // �ֹ����
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
	 * OnClick ��
	 *****************************/

	/*****************************
	 * UTIL ����
	 *****************************/
    String gv_searchWord = "";
	// ȭ���� �ٲ۴�.
	public void fn_chgContentView(int pLayoutResId) {
		if ( gv_currentViewId == pLayoutResId ) return; // ����ȭ��� �ٲ�ȭ���� ���ٸ� ����.

		// �������� �� â�� �����۾� ���� //
		String vNav = "";
		if ( gv_currentViewId == R.layout.activity_main ) {
			vNav = ((TextView)findViewById(R.id.txt_nav)).getText().toString();
		}
		// �������� �� â�� �����۾� �� //

		gv_currentViewId = pLayoutResId;
		setContentView(gv_currentViewId);

		// ���� ������ ȭ���� �����۾� ���� //
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
		// ���� ������ ȭ���� �����۾� �� //
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
	
	
	
	// pTagName �� ���� list �κ��� ArrayList�� �����Ѵ�.
	public ArrayList<String> fn_convArrayList(String pTagName) {
		ArrayList<String> vArrayList = new ArrayList<String>();
		for ( int i = 0; i < list.size(); i++ ) {
			vArrayList.add(getList(i, pTagName));
		}
		return vArrayList;
	}

	// list �� pTagName�� ���� pValue�� ���� ������ �ִ� row�� �����Ѵ�.
	public int fn_findListRow(String pTagName, String pValue) {
		if ( pTagName == null || pValue == null ) return -1;
		for ( int i = 0; i < list.size(); i++ ) {
			if ( pValue.equals(getList(i, pTagName)) ) {
				return i;
			}
		}
		return -1;
	}

	// thumbNail url�� ������´�.
	public String fn_getUrl(int pPosition) {
		String vSite = NVL(getList(pPosition, "SITE"), getView("SITE")); // list�� SITE�� ������ ���� ������ �۷ι��¿��� ������ ����.
		String vCode = NVL(getList(pPosition, "MAX_NO"), getList(pPosition, "LINK_CODE")); // "3".equals(vMode)? getList(pPosition, "LINK_CODE") : getList(pPosition, "MAX_NO"); // ȸ������. ȸ������Ʈ�� LINK_CODE��, ����������̸� MAX_NO�� �����´�.
		String vCid  = NVL(getList(pPosition, "CID"), getView("CID"));
		String vThumbUrl = (( getList(pPosition, "THUMB_NAIL").indexOf("http") == 0 )? "" : gv_thumbComn.get(vSite) ) + getList(pPosition, "THUMB_NAIL");
		return (vThumbUrl.replace("$cid", vCid).replace("$no", vCode));
	}

	// list�� pPosition ���� pTagName ���� ��ȯ
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

    // �ڷΰ��� ��ư ����
	boolean gv_saveHist = true; // �ڷΰ��� �̷��� ������ true
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if ( keyCode == KeyEvent.KEYCODE_BACK ) {
    		if ( gv_backList.isEmpty() || gv_backList.size() < 2 ) { // �� ���� �޴��϶� �ڷι�ư�� �ι� Ŭ���ϸ� ���� ����ȴ�.
    			if ( aUtil.chkTimer("BackButton", 2) ) { // 2�ʸ��� Ŭ���ϸ� �ý�������
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

    // �ڷΰ��� ��ư�� �����Ѵ�.
    public void fn_saveBack(String pMode, String pParam) {
    	if ( !gv_saveHist ) return;
		if ( "J4".equals(pMode) || "J1".equals(pMode) ) gv_backList.clear(); // ����������Ʈ �϶��� ���ư �ʱ�ȭ(���̻� �ڷΰ�������)

		if ( gv_backList.size() > 0 ) {
			HashMap<String, String> dataCompare = (HashMap<String, String>) gv_backList.get(gv_backList.size() - 1);
			if ( dataCompare.get("vMode").equals(pMode) && dataCompare.get("vParam").equals(pParam) ) return; // ���� �޴� �ι� Ŭ���� �н�
		}

		HashMap<String, String> data = new HashMap<String, String>();
		data.put("vMode" , pMode );
		data.put("vParam", pParam);
		data.put("vPos"  , ((ListView)findViewById(R.id.list)).getFirstVisiblePosition() + "");
		gv_backList.add(new HashMap<String, String>(data));
    }
    
	Toast mToast = null; // �޽��������� ���� �������� �佺Ʈ ����.
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
	 * UTIL ��
	 *****************************/

	/*****************************
	 * SQLite DB ��Ʈ�� ���� ����
	 *****************************/
	/** ���α׷� �⺻���� �ε� */
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

	/** ������ ���� */
	public void updSettingValue(String pSetId, String pSetValue) {
		dbAdapter.updSettingValue(pSetId, pSetValue);
		fn_setSetting();
	}

	/** DBŬ���� */
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
	 * SQLite DB ��Ʈ�� ���� ��
	 *****************************/
}