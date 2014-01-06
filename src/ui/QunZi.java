package ui;

import java.util.ArrayList;
import java.util.List;

import tools.AppException;
import tools.UIHelper;
import ui.adapter.IndexPhoneAdapter;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView;
import bean.Entity;
import bean.PhoneIntroEntity;
import bean.PhoneListEntity;
import bean.Result;

import com.vikaa.mycontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class QunZi extends AppActivity {
	
	private ProgressDialog loadingPd;
	private List<List<PhoneIntroEntity>> phones;
	private PinnedHeaderListView mPinedListView1;
	private IndexPhoneAdapter mPhoneAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qunzi);
		initUI();
		getPhoneList();
	}
	
	private void initUI() {
		View header = (View) getLayoutInflater().inflate(R.layout.quzi_header, null);
		mPinedListView1 = (PinnedHeaderListView) findViewById(R.id.listView);
		mPinedListView1.setDividerHeight(0);
		mPinedListView1.addHeaderView(header, null, false);
		phones = new ArrayList<List<PhoneIntroEntity>>();
		mPhoneAdapter = new IndexPhoneAdapter(this, phones);
		mPinedListView1.setAdapter(mPhoneAdapter);
		
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
	
}
