/**
 * QYdonal
 */
package ui;

import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import bean.PhoneIntroEntity;

import com.google.analytics.tracking.android.EasyTracker;
import com.vikaa.mycontact.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;

/**
 * QY
 *
 * @author donal
 *
 */
public class Register extends AppActivity implements OnFocusChangeListener{
	
	private ProgressDialog loadingPd;
	private InputMethodManager imm;
	private EditText nameET;
	private EditText phoneET;
	private EditText passwordET;
	private EditText orgET;
	private EditText posET;
	private EditText emailET;
	private ListView xlistView;
	
	private TextView t1;
	private TextView t2;
	private TextView t3;
	private TextView t4;
	private TextView t5;
	private TextView t6;
	
	@Override
	public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	}

	  @Override
	public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		initUI();
	}
	
	private void initUI() {
		xlistView = (ListView) findViewById(R.id.xlistview);
		View mHeaderView = getLayoutInflater().inflate(R.layout.register_header, null);
		nameET = (EditText) mHeaderView.findViewById(R.id.editTextName);
		phoneET = (EditText) mHeaderView.findViewById(R.id.editTextPhone);
		passwordET = (EditText) mHeaderView.findViewById(R.id.editTextPass);
		orgET = (EditText) mHeaderView.findViewById(R.id.editTextOrg);
		posET = (EditText) mHeaderView.findViewById(R.id.editTextPos);
		emailET = (EditText) mHeaderView.findViewById(R.id.editTextEmail);
		
		nameET.setOnFocusChangeListener(this);
		phoneET.setOnFocusChangeListener(this);
		passwordET.setOnFocusChangeListener(this);
		orgET.setOnFocusChangeListener(this);
		posET.setOnFocusChangeListener(this);
		emailET.setOnFocusChangeListener(this);
		
		t1 = (TextView) mHeaderView.findViewById(R.id.t1);
		t2 = (TextView) mHeaderView.findViewById(R.id.t2);
		t3 = (TextView) mHeaderView.findViewById(R.id.t3);
		t4 = (TextView) mHeaderView.findViewById(R.id.t4);
		t5 = (TextView) mHeaderView.findViewById(R.id.t5);
		t6 = (TextView) mHeaderView.findViewById(R.id.t6);
		
		xlistView.addHeaderView(mHeaderView);
		xlistView.setAdapter(new ArrayAdapter<PhoneIntroEntity>(this, R.layout.friend_card_cell));
//		xlistView.setOnScrollListener(new OnScrollListener() {
//			@Override
//			public void onScrollStateChanged(AbsListView arg0, int scollState) {
//				if (scollState == SCROLL_STATE_TOUCH_SCROLL) {
//					imm.hideSoftInputFromWindow(xlistView.getWindowToken(), 0);
//				}
//			}
//			
//			@Override
//			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
//				
//			}
//		});
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
		case R.id.editTextName:
			if (hasFocus) {
				xlistView.scrollTo(0, 0);
			}
			else {
				String name = nameET.getText().toString();
				if (StringUtils.notEmpty(name)) {
					if (!StringUtils.isChineseName(name)) {
						WarningDialog("请填写正确的姓名");
					}
					t1.setTextColor(getResources().getColor(R.color.black));
				}
				else {
					t1.setTextColor(getResources().getColor(R.color.red));
				}
			}
			break;
		case R.id.editTextPhone:
			if (hasFocus) {
				xlistView.scrollTo(0, 0);
			}
			else {
				String phone = phoneET.getText().toString();
				if (StringUtils.notEmpty(phone)) {
					if (!StringUtils.isMobileNO(phone)) {
						WarningDialog("请填写正确的手机");
					}
					t2.setTextColor(getResources().getColor(R.color.black));
				}
				else {
					t2.setTextColor(getResources().getColor(R.color.red));
				}
			}
			break;
		case R.id.editTextPass:
			if (hasFocus) {
				xlistView.scrollTo(0, 10);
			}
			else {
				String pass = passwordET.getText().toString();
				if (StringUtils.notEmpty(pass)) {
					if (!(pass.length() >= 6 && pass.length() <= 15)) {
						WarningDialog("请填写6-15位密码");
					}
					t3.setTextColor(getResources().getColor(R.color.black));
				}
				else {
					t3.setTextColor(getResources().getColor(R.color.red));
				}
			}
			break;
		case R.id.editTextOrg:
			if (hasFocus) {
				xlistView.scrollTo(0, 80);
			}
			else {
				String org = orgET.getText().toString();
				if (StringUtils.empty(org)) {
					t4.setTextColor(getResources().getColor(R.color.red));
				}
				else {
					t4.setTextColor(getResources().getColor(R.color.black));
				}
			}
			break;
		case R.id.editTextPos:
			if (hasFocus) {
				xlistView.scrollTo(0, 120);
			}
			else {
				String pos = posET.getText().toString();
				if (StringUtils.empty(pos)) {
					t5.setTextColor(getResources().getColor(R.color.red));
				}
				else {
					t5.setTextColor(getResources().getColor(R.color.black));
				}
			}
			break;
		case R.id.editTextEmail:
			if (hasFocus) {
				xlistView.scrollTo(0, 180);
			}
			else {
				String email = emailET.getText().toString();
				if (StringUtils.notEmpty(email)) {
					if (!StringUtils.isEmail(email)) {
						WarningDialog("请填写正确的邮箱");
					}
					t6.setTextColor(getResources().getColor(R.color.black));
				}
				else {
					t6.setTextColor(getResources().getColor(R.color.red));
				}
			}
			break;
		}
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.editTextName:
			xlistView.scrollTo(0, 0);
			break;
		case R.id.editTextPhone:
			xlistView.scrollTo(0, 0);
			break;
		case R.id.editTextPass:
			xlistView.scrollTo(0, 10);
			break;
		case R.id.editTextOrg:
			xlistView.scrollTo(0, 80);
			break;
		case R.id.editTextPos:
			xlistView.scrollTo(0, 120);
			break;
		case R.id.editTextEmail:
			xlistView.scrollTo(0, 180);
			break;
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.rightBarButton:
			break;
		}
	}
	
	protected void WarningDialog(String message) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

	   builder.create().show();
	}
	
}
