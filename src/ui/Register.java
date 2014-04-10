/**
 * QYdonal
 */
package ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.CookieStore;

import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.LoginCode2.CountDown;
import bean.Entity;
import bean.PhoneIntroEntity;
import bean.RegUserEntity;
import bean.Result;
import bean.UserEntity;

import com.google.analytics.tracking.android.EasyTracker;
import com.loopj.android.http.PersistentCookieStore;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

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
	
	private String mobile;
	private boolean isJump;
	
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
		initData();
		initUI();
	}
	
	private void initData() {
		mobile = getIntent().getStringExtra("mobile");
		isJump = getIntent().getBooleanExtra("jump", false);
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
		
		phoneET.setText(mobile);
		
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
		xlistView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scollState) {
				if (scollState == SCROLL_STATE_TOUCH_SCROLL) {
					imm.hideSoftInputFromWindow(xlistView.getWindowToken(), 0);
					xlistView.requestFocus();
				}
			}
			
			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
				
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		logout();
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
		case R.id.rightBarButton:
			next();
			break;
		}
	}
	
	private void next() {
		String name = nameET.getText().toString();
		String phone = phoneET.getText().toString();
		String pass = passwordET.getText().toString();
		String org = orgET.getText().toString();
		String pos = posET.getText().toString();
		String email = emailET.getText().toString();
		if (StringUtils.empty(name) || !StringUtils.isChineseName(name)) {
			WarningDialog("请填写正确姓名");
			return;
		}
		if(StringUtils.empty(phone) || !StringUtils.isMobileNO(phone)) {
			WarningDialog("请填写正确手机号码");
			return;
		}
		if (pass.indexOf(" ")!= -1 || !(pass.length() >= 6 && pass.length() <= 15)) {
			WarningDialog("请填写6-15位密码,且没有空格");
			return;
		}
		if(StringUtils.empty(org)) {
			WarningDialog("请填写公司");
			return;
		}
		if(StringUtils.empty(pos)) {
			WarningDialog("请填写职位");
			return;
		}
		if(StringUtils.empty(email) || !StringUtils.isEmail(email)) {
			WarningDialog("请填写正确邮箱地址");
			return;
		}
		UIHelper.showProgress(this, null, null, true);
		AppClient.regUser(appContext, phone, pass, name, org, pos, email, new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				RegUserEntity entity = (RegUserEntity) data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					UIHelper.ToastMessage(Register.this, "保存成功", Toast.LENGTH_SHORT);
					if (isJump) {
						enterIndex();
					}
					setResult(RESULT_OK);
					AppManager.getAppManager().finishActivity(Register.this);
					break;
				default:
					UIHelper.ToastMessage(Register.this, entity.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
				UIHelper.ToastMessage(Register.this, message, Toast.LENGTH_SHORT);
			}
			
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
				UIHelper.ToastMessage(Register.this, e.getMessage(), Toast.LENGTH_SHORT);
			}
		});
	}
	
	private void enterIndex() {
		Intent intent = new Intent(this, Tabbar.class);
		startActivity(intent);
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
	
	public void logout() {
		new AlertDialog.Builder(this).setTitle("确定退出吗?")
		.setNeutralButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AppClient.Logout(appContext);
				CookieStore cookieStore = new PersistentCookieStore(Register.this);  
				cookieStore.clear();
				AppManager.getAppManager().finishAllActivity();
				appContext.setUserLogout();
				Intent intent = new Intent(Register.this, LoginCode1.class);
				startActivity(intent);
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		}).show();
	}
	
}
