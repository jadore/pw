package ui.adapter;

import im.bean.Conversation;
import im.ui.Chating;

import java.util.List;
import java.util.concurrent.ExecutionException;

import tools.Logger;
import tools.StringUtils;
import ui.CardView;
import ui.QYWebView;
import ui.WeFriendCard;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.vikaa.mycontact.R;

import config.CommonValue;
import bean.CardIntroEntity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ConversationAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<Conversation> conversation;
	
	static class CellHolder {
		TextView alpha;
		ImageView avatarImageView;
		TextView titleView;
		TextView desView;
		Button callButton;
	}
	
	public ConversationAdapter(Context context, List<Conversation> conversation) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.conversation = conversation;
	}
	
	@Override
	public int getCount() {
		return conversation.size();
	}

	@Override
	public Object getItem(int arg0) {
		return conversation.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return conversation.get(arg0).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		CellHolder cell = null;
		if (convertView == null) {
			cell = new CellHolder();
			convertView = inflater.inflate(R.layout.friend_card_cell, null);
			cell.alpha = (TextView) convertView.findViewById(R.id.alpha);
			cell.avatarImageView = (ImageView) convertView.findViewById(R.id.avatarImageView);
			cell.titleView = (TextView) convertView.findViewById(R.id.title);
			cell.desView = (TextView) convertView.findViewById(R.id.des);
			cell.callButton = (Button) convertView.findViewById(R.id.call);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		final Conversation con = conversation.get(position);
//		ImageLoader.getInstance().displayImage(model.avatar, cell.avatarImageView, CommonValue.DisplayOptions.default_options);
//		cell.titleView.setText(model.realname);
		cell.desView.setText(con.content);
		cell.alpha.setVisibility(View.GONE);
		cell.callButton.setVisibility(View.INVISIBLE);
		convertView.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			}
		});
		return convertView;
	}
}
