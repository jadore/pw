package ui;

import com.vikaa.mycontact.R;

import android.os.Bundle;
import android.text.Html;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import tools.AppManager;
import tools.BaseActivity;
import tools.Logger;

public class LoginWechat extends BaseActivity{
	String b = "请发送【9】到我们<font color=\"#088ec1\">微信公众帐号</font><br>获取6位验证数字";
	
	String a = "恭喜，您在<a href=\"http://pb.wcl.m0.hk/book/54b92330a4a7\">维卡互动微信通讯录1</a>的查看申请已经批准";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_wechat);
		this.initUI();
	}
	
	private void initUI() {
		Button leftBarButton = (Button) findViewById(R.id.leftBarButton);
		accretionArea(leftBarButton);
		Button rightBarButton = (Button) findViewById(R.id.rightBarButton);
		accretionArea(rightBarButton);
		TextView textview = (TextView) findViewById(R.id.textview1);
		textview.setText(Html.fromHtml(b));
//		textview.setMovementMethod(LinkMovementMethod.getInstance());
//        CharSequence text = textview.getText();
//        if (text instanceof Spannable) {
//            int end = text.length();
//            Spannable sp = (Spannable) textview.getText();
//            URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
//            SpannableStringBuilder style = new SpannableStringBuilder(text);
//            style.clearSpans();
//            for (URLSpan url : urls) {
//            	NoLineClickSpan myURLSpan = new NoLineClickSpan(url.getURL());
//                style.setSpan(myURLSpan, sp.getSpanStart(url),
//                        sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            }
//            textview.setText(style);
//        }
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.rightBarButton:
			break;
		}
	}
	
	private class NoLineClickSpan extends ClickableSpan { 
	    String text;

	    public NoLineClickSpan(String text) {
	        super();
	        this.text = text;
	    }

	    @Override
	    public void updateDrawState(TextPaint ds) {
	        ds.setColor(getResources().getColor(R.color.nav_color));
	        ds.setUnderlineText(false);
	    }

		@Override
		public void onClick(View arg0) {
			Logger.i(text);
		}
	}
}	
