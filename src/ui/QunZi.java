package ui;

import java.util.ArrayList;
import java.util.List;

import tools.AppException;
import tools.UIHelper;
import ui.adapter.QuanZiAdapter;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView;
import bean.Entity;
import bean.PhoneIntroEntity;
import bean.PhoneListEntity;
import bean.Result;

import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class QunZi extends AppActivity {
	
	private ProgressDialog loadingPd;
	private List<List<PhoneIntroEntity>> phones;
	private PinnedHeaderListView mPinedListView;
	private QuanZiAdapter mPhoneAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qunzi);
		initUI();
		getPhoneList();
	}
	
	private void initUI() {
		View header = (View) getLayoutInflater().inflate(R.layout.quzi_header, null);
		mPinedListView = (PinnedHeaderListView) findViewById(R.id.listView);
		mPinedListView.setDividerHeight(0);
		mPinedListView.addHeaderView(header, null, false);
		phones = new ArrayList<List<PhoneIntroEntity>>();
		mPhoneAdapter = new QuanZiAdapter(this, phones);
		mPinedListView.setAdapter(mPhoneAdapter);
	}
	
	private void getPhoneList() {
		loadingPd = UIHelper.showProgress(this, null, null, true);
		AppClient.getPhoneList(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				PhoneListEntity entity = (PhoneListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					if (entity.owned.size() > 0) {
						phones.add(entity.owned);
					}
					if (entity.joined.size() > 0) {
						phones.add(entity.joined);
					}
					mPhoneAdapter.notifyDataSetChanged();
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), entity.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
				((AppException)e).makeToast(getApplicationContext());
			}
		});
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.createPhonebook:
			showCreate(CommonValue.CreateViewUrlAndRequest.ContactCreateUrl, CommonValue.CreateViewUrlAndRequest.ContactCreat);
			break;

		case R.id.openQuanZi:
			
			break;
		}
	}
	
	public void showPhoneViewWeb(PhoneIntroEntity entity, int RequestCode) {
		Intent intent = new Intent(this, PhonebookViewWeb.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/book/%s", CommonValue.BASE_URL, entity.code));
	    startActivityForResult(intent, RequestCode);
	}
	
	public void showCreate(String url, int RequestCode) {
		Intent intent = new Intent(this,CreateView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, url);
        startActivityForResult(intent, RequestCode);
	}
}
