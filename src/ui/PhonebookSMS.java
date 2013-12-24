package ui;


import java.util.ArrayList;
import java.util.List;

import tools.AppManager;
import tools.StringUtils;
import tools.UIHelper;

import bean.CardIntroEntity;
import bean.PhoneViewEntity;
import bean.SMSPersonList;

import com.vikaa.mycontact.R;

import config.CommonValue;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PhonebookSMS extends AppActivity implements OnItemClickListener{
	
	private ListView mListView;
	private List<CardIntroEntity> members;
	private ArrayList<String> smsMember;
	private SpaceAdapter mAdapter;
	private boolean allChosen;
	private Button rightBarButton;
	private Button nextButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phonebook_sms);
		initUI();
		initData();
	}
	
	private void initUI() {
		nextButton = (Button) findViewById(R.id.nextButton);
		rightBarButton = (Button)findViewById(R.id.rightBarButton);
		mListView = (ListView) findViewById(R.id.listView);
		mListView.setDividerHeight(0);
		members = new ArrayList<CardIntroEntity>();
		mAdapter = new SpaceAdapter(this, members);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}
	
	private void initData() {
		smsMember = new ArrayList<String>();
		SMSPersonList phonebook = (SMSPersonList) getIntent().getSerializableExtra(CommonValue.PhonebookViewIntentKeyValue.SMS);
		if (phonebook.members.size() > 0) {
			members.addAll(phonebook.members);
			mAdapter.notifyDataSetChanged();
		}
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.rightBarButton:
			chooseAll();
			break;
		case R.id.nextButton:
			showSMSBody();
			break;
		}
	}
	
	class SpaceAdapter extends BaseAdapter {
		private Context context;
		private List<CardIntroEntity> datas;
		private LayoutInflater listContainer;
		class CellHolder {
			ImageView isSelectedImageView;
			ImageView avatarImageView;
			TextView titleView;
			TextView roleView;
			TextView desView;
			TextView mobileView;
		}
		public SpaceAdapter(Context context, List<CardIntroEntity> datas) {
			this.context = context;
			this.datas = datas;
			this.listContainer = LayoutInflater.from(context);
		}
		
		public int getCount() {
			return datas.size();
		}

		public Object getItem(int arg0) {
			return datas.get(arg0);
		}

		public long getItemId(int arg0) {
			return datas.get(arg0).getId();
		}

		public View getView(int position, View convertView, ViewGroup arg2) {
			CellHolder cell = null;
			if (convertView == null) {
				cell = new CellHolder();
				convertView = listContainer.inflate(R.layout.phonebook_sms_cell, null);
				cell.isSelectedImageView = (ImageView) convertView.findViewById(R.id.isSelectedImageView);
				cell.avatarImageView = (ImageView) convertView.findViewById(R.id.avatarImageView);
				cell.titleView = (TextView) convertView.findViewById(R.id.title);
				cell.roleView = (TextView) convertView.findViewById(R.id.role);
				cell.desView = (TextView) convertView.findViewById(R.id.des);
				cell.mobileView = (TextView) convertView.findViewById(R.id.mobile);
				convertView.setTag(cell);
			}
			else 
				cell = (CellHolder) convertView.getTag();
			final CardIntroEntity model = datas.get(position);
			imageLoader.displayImage(model.headimgurl, cell.avatarImageView, CommonValue.DisplayOptions.default_options);
			cell.titleView.setText(String.format("%s(%s)", model.realname, model.nickname));
			cell.desView.setVisibility(View.GONE);
			if (!StringUtils.isEmpty(model.department)) {
				cell.desView.setVisibility(View.VISIBLE);
				cell.desView.setText(String.format("%s %s", model.department, model.position));
			}
			cell.mobileView.setText(model.phone);
			if (model.isChosen) {
				cell.isSelectedImageView.setBackgroundResource(R.drawable.friend_blue_select);
			} else {
				cell.isSelectedImageView.setBackgroundResource(R.drawable.friend_not_select);
			}
			return convertView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		CardIntroEntity model = members.get(position);
		if (model.isChosen) {
			smsMember.remove(model.phone);
		}
		else {
			smsMember.add(model.phone);
		}
		model.isChosen = !model.isChosen;
		mAdapter.notifyDataSetChanged();
	}
	
	private void chooseAll() {
		for (CardIntroEntity model : members) {
			model.isChosen = allChosen?false:true;
			if (!allChosen) {
				smsMember.add(model.phone);
			}
			else {
				smsMember.clear();
			}
		}
		mAdapter.notifyDataSetChanged();
		allChosen = !allChosen;
		rightBarButton.setText(allChosen?"取消全选":"全选");
	}
	
	private void showSMSBody() {
		if (smsMember.size() > 0) {
			Intent intent = new Intent(this, PhonebookSMSBody.class);
			intent.putStringArrayListExtra(CommonValue.PhonebookViewIntentKeyValue.SMSPersons, smsMember);
			startActivityForResult(intent, CommonValue.PhonebookViewIntentKeyValue.SMSPersonRequest);
		}
		else {
			UIHelper.ToastMessage(getApplicationContext(), "请选择成员", Toast.LENGTH_SHORT);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case CommonValue.PhonebookViewIntentKeyValue.SMSPersonRequest:
			finish();
			break;
		}
	}
}
