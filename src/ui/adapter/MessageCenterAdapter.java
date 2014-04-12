package ui.adapter;

import java.util.List;

import com.vikaa.mycontact.R;

import config.CommonValue;
import bean.CardIntroEntity;
import bean.MessageEntity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;
import tools.StringUtils;
import ui.Index;
import ui.MessageView;
import za.co.immedia.pinnedheaderlistview.SectionedBaseAdapter;

public class MessageCenterAdapter extends SectionedBaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private List<List<MessageEntity>> messages;
	public MessageCenterAdapter(Context context, List<List<MessageEntity>> messages) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.messages = messages;
	}
	
	static class CellHolder {
		TextView titleView;
		TextView desView;
	}
	
	static class SectionView {
		TextView titleView;
	}
	
	@Override
	public Object getItem(int section, int position) {
		return messages.get(section).get(position);
	}

	@Override
	public long getItemId(int section, int position) {
		return position;
	}

	@Override
	public int getSectionCount() {
		return messages.size();
	}

	@Override
	public int getCountForSection(int section) {
		return messages.get(section).size();
	}

	@Override
	public View getItemView(int section, int position, View convertView,
			ViewGroup parent) {
		CellHolder cell = null;
		if (convertView == null) {
			cell = new CellHolder();
			convertView = inflater.inflate(R.layout.messagecenter_cell, null);
			cell.titleView = (TextView) convertView.findViewById(R.id.title);
			cell.desView = (TextView) convertView.findViewById(R.id.messageView);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		MessageEntity message = messages.get(section).get(position);
		if (section == 0) {
			cell.titleView.setText("私信");
			if (StringUtils.notEmpty(message) && StringUtils.notEmpty(message.message) && !message.message.equals("0")) {
				cell.desView.setText(message.message);
				cell.desView.setVisibility(View.VISIBLE);
			}
			else {
				cell.desView.setVisibility(View.INVISIBLE);
			}
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					((MessageView)context).showConversation();
				}
			});
		}
		else if (section == 1) {
			cell.titleView.setText("名片交换请求");
			if (StringUtils.notEmpty(message) && StringUtils.notEmpty(message.message) && !message.message.equals("0")) {
				cell.desView.setText(message.message);
				cell.desView.setVisibility(View.VISIBLE);
			}
			else {
				cell.desView.setVisibility(View.INVISIBLE);
			}
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					((MessageView)context).showCard();
				}
			});
			
		}
		else if (section == 2) {
			cell.titleView.setText("通知");
			if (StringUtils.notEmpty(message) && StringUtils.notEmpty(message.message) && !message.message.equals("0")) {
				cell.desView.setText(message.message);
				cell.desView.setVisibility(View.VISIBLE);
			}
			else {
				cell.desView.setVisibility(View.INVISIBLE);
			}
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					((MessageView)context).showNotification();
				}
			});
			
		}
		return convertView;
	}

	@Override
	public View getSectionHeaderView(int section, View convertView,
			ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.messagecenter_section, null);
		}
		return convertView;
	}

}
