package com.anglab.jogiyo;

import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ListViewer extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	static class ListAdapterWithButton<T> extends BaseAdapter {
		private final LayoutInflater mInflater;
		private final List<HashMap<String, String>> array;
	    private final Context mContext;

		public ListAdapterWithButton(final Context context, final List<HashMap<String, String>> array) {
			this.mContext = context;
			this.mInflater = LayoutInflater.from(context);
			this.array = array;
		}

		@Override
		public int getCount() { return array.size(); }

		@Override
		public String getItem(int position) { return (String)array.get(position).get("NAME"); }

		@Override
		public long getItemId(int position) { return position; }

		class ViewHolder {
			TextView label;
			ImageView img_new;
			RelativeLayout lay_mm;
			WebView web_thumb;

			TextView txt_fstInstDh;
			TextView txt_site;
			TextView txt_state;
			TextView txt_nextStep;
			TextView txt_miniTitle;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			final MainActivity vMainActivity = (MainActivity) mContext;

			if ( convertView == null ) {
				convertView = mInflater.inflate(R.layout.list_one_row, null);
				holder = new ViewHolder();
				holder.label     = (TextView) convertView.findViewById(R.id.txt_title);
				holder.img_new   = (ImageView) convertView.findViewById(R.id.img_new);
				holder.lay_mm    = (RelativeLayout) convertView.findViewById(R.id.lay_mm);
				holder.web_thumb = (WebView) convertView.findViewById(R.id.web_thumb);

				holder.txt_fstInstDh = (TextView) convertView.findViewById(R.id.txt_fstInstDh);
				holder.txt_site = (TextView) convertView.findViewById(R.id.txt_site);
				holder.txt_state = (TextView) convertView.findViewById(R.id.txt_state);
				holder.txt_nextStep = (TextView) convertView.findViewById(R.id.txt_nextStep);
				holder.txt_miniTitle = (TextView) convertView.findViewById(R.id.txt_miniTitle);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			String vName = getList(position, "NAME");
			holder.label.setText(vName); // ����Ʈ ����

			// �ڷᰡ ���� = �˻��� ����� �����ϴ�
			if ( vMainActivity.getResources().getString(R.string.str_noSearchData).equals(vName)
			  && array.size() == 1 ) {
				holder.lay_mm.setBackgroundColor(Color.WHITE);
				holder.label.setTextColor(0xFF333333);
				holder.img_new.setVisibility(View.GONE); 
				holder.web_thumb.setVisibility(View.GONE);
				return convertView;
			}

			final String vMode = vMainActivity.fn_getMode();

			/********** ��ĥó��, new ó�� **********/
			// vTF1:24�ð� �̳� ������Ʈ �Ǿ��°�, vTF2:���� �Ⱥ� �ֽ�ȭ�� �ִ°�
			String vLstUpdDh = getList(position, "LST_UPD_DH");

			boolean vTF1 = !"".equals(vLstUpdDh) && ( vMainActivity.gv_isNewDate.compareTo(vLstUpdDh) < 0 );
			boolean vTF2 = ( !"".equals(getList(position, "LST_VIEW_NO")) && !"".equals(getList(position, "MAX_NO")) )
					    && !getList(position, "MAX_NO").equals(getList(position, "LST_VIEW_NO"));

			if ( vTF2 ) {
				holder.lay_mm.setBackgroundColor(Color.WHITE);
				holder.label.setTextColor(0xFF33BB77);
			} else {
				holder.lay_mm.setBackgroundColor(0xFFF4F4F4);
				holder.label.setTextColor(0xFF333333);
			}
			holder.img_new.setVisibility(vTF1? View.VISIBLE : View.GONE); // new image icon visible
			/********************/


			/********** ��ư ������ ó�� **********/
			boolean vThumbTF = true;

			holder.web_thumb.setVisibility(View.VISIBLE);
			
			if ( "0".equals(vMode) ) { // ���������
			} else if ( "1".equals(vMode) ) { // ����Ʈ
				vThumbTF = false;
				holder.web_thumb.setVisibility(View.GONE);
			} else if ( "2".equals(vMode) || "4".equals(vMode) || "6".equals(vMode) ) { // ������� / �˻� / ��õ����
				if ( "Y".equals(getList(position, "MY_INQ_YN")) ) {
				} else { // �� ���� ����Ʈ�� ������ �߰� ��ư
				}
			} else if ( "J1".equals(vMode) ) { // ȸ������
				holder.txt_fstInstDh.setVisibility(View.GONE);
				holder.txt_site.setVisibility(View.GONE);
				holder.txt_state.setVisibility(View.GONE);
				holder.txt_nextStep.setVisibility(View.GONE);
				holder.txt_miniTitle.setVisibility(View.GONE);
				holder.label.setVisibility(View.VISIBLE);
			} else if ( "J4".equals(vMode) ) {
				holder.txt_fstInstDh.setVisibility(View.VISIBLE);
				holder.txt_site.setVisibility(View.VISIBLE);
				holder.txt_state.setVisibility(View.VISIBLE);
				holder.txt_nextStep.setVisibility(View.VISIBLE);
				holder.txt_miniTitle.setVisibility(View.VISIBLE);
				holder.label.setVisibility(View.GONE);

				holder.txt_fstInstDh.setText("��û�Ͻ� : " + getList(position, "FST_INS_DH"));
				holder.txt_site.setText("��û���� : " + getList(position, "SITE_NM"));
				holder.txt_state.setText("������� : " + getList(position, "STATE_NM"));
				holder.txt_nextStep.setText("�����۾� : " + getList(position, "NEXT_STEP"));
				holder.txt_miniTitle.setText("���� : " + getList(position, "NAME"));
			}
			/****************************************/

			/********** ����� ó�� **********/
			if ( "J1".equals(vMode) ) {
				String data = "<body leftmargin=0 topmargin=0 marginwidth=0 marginheight=0><img src='"+getList(position, "SITE")+".png' width=100% height=100%></body>";
				holder.web_thumb.loadDataWithBaseURL("file:///android_asset/",data , "text/html", "utf-8",null);
				holder.web_thumb.setBackgroundColor(0);
			} else {
				String data = "<body leftmargin=0 topmargin=0 marginwidth=0 marginheight=0><img src='ic_noimage.png' width=100% height=100%></body>";
				holder.web_thumb.loadDataWithBaseURL("file:///android_asset/",data , "text/html", "utf-8",null);
				holder.web_thumb.setBackgroundColor(0);
			}
			/********************/

			holder.lay_mm.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					vMainActivity.fn_listOnClick(position);
				}
			});

			return convertView;
		}

		public String getList(int pPosition, String pTag) {
			if ( getCount() <= pPosition && 0 > pPosition ) return "";
			if ( !array.get(pPosition).containsKey(pTag) ) return "";
			String vReturn = (String) array.get(pPosition).get(pTag);
			if ( vReturn == null || vReturn.trim().length() == 0 ) vReturn = "";
			return vReturn;
		}
	}
}