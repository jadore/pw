package ui.adapter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tools.Logger;
import ui.CardView;
import ui.PhonebookViewMembers;
import ui.adapter.CardViewAdapter.CellHolder;
import bean.CardIntroEntity;
import bean.KeyValue;
import bean.MessageEntity;
import bean.PhoneIntroEntity;

import com.vikaa.mycontact.R;

import config.CommonValue;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MessageViewAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private List<MessageEntity> messages;
	
	static class CellHolder {
		TextView titleView;
		TextView desView;
	}
	
	public MessageViewAdapter(Context context, List<MessageEntity> messages) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.messages = messages;
	}
	
	@Override
	public int getCount() {
		return messages.size();
	}

	@Override
	public Object getItem(int arg0) {
		return messages.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return messages.get(arg0).getId();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		CellHolder cell = null;
		if (convertView == null) {
			cell = new CellHolder();
			convertView = inflater.inflate(R.layout.message_view_cell, null);
			cell.titleView = (TextView) convertView.findViewById(R.id.title);
			cell.desView = (TextView) convertView.findViewById(R.id.des);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		MessageEntity model = messages.get(position);
		cell.titleView.setText(Html.fromHtml(model.message));
		cell.titleView.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence text = cell.titleView.getText();
        if (text instanceof Spannable) {
            int end = text.length();
            Spannable sp = (Spannable) cell.titleView.getText();
            URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
            SpannableStringBuilder style = new SpannableStringBuilder(text);
            style.clearSpans();
            for (URLSpan url : urls) {
            	NoLineClickSpan myURLSpan = new NoLineClickSpan(url.getURL());
                style.setSpan(myURLSpan, sp.getSpanStart(url),
                        sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            cell.titleView.setText(style);
        }
        cell.desView.setText(model.created_at);
		return convertView;
	}
	
	private class NoLineClickSpan extends ClickableSpan { 
	    String text;

	    public NoLineClickSpan(String text) {
	        super();
	        this.text = text;
	    }

	    @Override
	    public void updateDrawState(TextPaint ds) {
	        ds.setColor(context.getResources().getColor(R.color.nav_color));
	        ds.setUnderlineText(true);
	    }

		@Override
		public void onClick(View arg0) {
			Logger.i(text);
			regexHtml(text, "");
		}
	}
	
	private void regexHtml(String target, String CONTENT) {
	    String result = "";
	    String cardRegex         = ".*\\/card\\/([0-9a-z]{6})$";
	    Pattern pattern = Pattern.compile(cardRegex);
		Matcher matcher = pattern.matcher(target);
	   	if (matcher.find()) {
	   		showCard(matcher.group(1));
	   		return;
		}
	    String bookRegex         = ".*\\/book\\/([0-9a-z]+)$";
	    pattern = Pattern.compile(bookRegex);
	    matcher = pattern.matcher(target);
	    if (matcher.find()) {
	    	showPhone(matcher.group(1));
	        return;
	    }
	    String activityRegex         = ".*\\/activity\\/([0-9a-z]+)$";
	    pattern = Pattern.compile(activityRegex);
	    matcher = pattern.matcher(target);
	    if (matcher.find()) {
	    	showPhone(matcher.group(1));
	        return;
	    }
	    String bRegex         = ".*\\/b\\/([0-9a-z]+)$";
	    pattern = Pattern.compile(bRegex);
	    matcher = pattern.matcher(target);
	    if (matcher.find()) {
	    	
	        return;
	    }
	}
	
	private void showPhone(String code) {
		Intent intent = new Intent(context, PhonebookViewMembers.class);
		PhoneIntroEntity entity = new PhoneIntroEntity();
		entity.code = code;
		entity.content = " ";
		entity.title = " ";
		intent.putExtra(CommonValue.IndexIntentKeyValue.PhoneView, entity);
		context.startActivity(intent);
	}
	
	private void showCard(String code) {
		Logger.i(code);
		Intent intent = new Intent(context, CardView.class);
		CardIntroEntity entity = new CardIntroEntity();
		entity.code = code;
		entity.department = "";
		entity.position = "";
		entity.willRefresh = true;
		intent.putExtra(CommonValue.CardViewIntentKeyValue.CardView, entity);
		context.startActivity(intent);
	}
}
