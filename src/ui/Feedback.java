package ui;

import tools.AppManager;
import tools.StringUtils;
import tools.UIHelper;

import com.vikaa.mycontact.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Feedback extends AppActivity{
	private ProgressDialog loadingPd;
	private FrameLayout mForm;
	private TextView msgView;
	private EditText mContent;
	private LinearLayout mClearwords;
	private TextView mNumberwords;
	
	private InputMethodManager imm;
	
	public static LinearLayout mMessage;
	public static Context mContext;
	
	private static final int MAX_TEXT_LENGTH = 160;//最大输入字数
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		mContext = this;
		//软键盘管理类
		imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		initUI();
		initData();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initUI() {    	
    	mForm = (FrameLayout)findViewById(R.id.tweet_pub_form);
    	msgView = (TextView) findViewById(R.id.msg_view);
    	mMessage = (LinearLayout)findViewById(R.id.tweet_pub_message);
    	mContent = (EditText)findViewById(R.id.tweet_pub_content);
    	mClearwords = (LinearLayout)findViewById(R.id.tweet_pub_clearwords);
    	mNumberwords = (TextView)findViewById(R.id.tweet_pub_numberwords);
    	
    	mClearwords.setOnClickListener(clearwordsClickListener);
    	
    	//编辑器添加文本监听
    	mContent.addTextChangedListener(new TextWatcher() {		
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//显示剩余可输入的字数
				mNumberwords.setText((s.length()) + "");
			}		
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}		
			public void afterTextChanged(Editable s) {}
		});
    	//编辑器点击事件
    	mContent.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//显示软键盘
				showIMM();
			}
		});
    	//设置最大输入字数
    	InputFilter[] filters = new InputFilter[1];  
    	filters[0] = new InputFilter.LengthFilter(MAX_TEXT_LENGTH);
    	mContent.setFilters(filters);
    }
    
    private void initData() {
    	
    }
    
    
    private void showIMM() {
    	imm.showSoftInput(mContent, 0);
    }
	
    private View.OnClickListener clearwordsClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			String content = mContent.getText().toString();
			if(!StringUtils.isEmpty(content)){
				UIHelper.showClearWordsDialog(v.getContext(), mContent, mNumberwords);
			}
		}
	};
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			overridePendingTransition(R.anim.exit_in_from_left, R.anim.exit_out_to_right);
			break;
		case R.id.rightBarButton:
			break;
		}
	}
	
}
