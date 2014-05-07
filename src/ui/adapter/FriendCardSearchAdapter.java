package ui.adapter;

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
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendCardSearchAdapter extends BaseExpandableListAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<List<CardIntroEntity>> cards;
	
	static class CellHolder {
		TextView alpha;
		ImageView avatarImageView;
		TextView titleView;
		TextView desView;
		Button callButton;
	}
	
	static class SectionHolder {
		TextView titleView;
	}
	
	public FriendCardSearchAdapter(Context context, List<List<CardIntroEntity>> cards) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.cards = cards;
	}
	
	private void showCardView(CardIntroEntity entity) {
		Intent intent = new Intent(context, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.link);
		((WeFriendCard)context).startActivityForResult(intent, CommonValue.CardViewUrlRequest.editCard);
	}
	
	private void showMobileView(CardIntroEntity entity) {
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		Uri personUri = ContentUris.withAppendedId(uri, Integer.valueOf(entity.code));
		Intent intent2 = new Intent();
		intent2.setAction(Intent.ACTION_VIEW);
		intent2.setData(personUri);
		context.startActivity(intent2);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
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
		final CardIntroEntity model = cards.get(groupPosition).get(childPosition);
		ImageLoader.getInstance().displayImage(model.avatar, cell.avatarImageView, CommonValue.DisplayOptions.default_options);
		cell.titleView.setText(model.realname);
		if (model.cardSectionType.equals("mobile")) {
			cell.desView.setText("");
		}
		else {
			cell.desView.setText(String.format("%s %s", model.department, model.position));
		}
		cell.alpha.setVisibility(View.GONE);
		convertView.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View arg0) {
//				if (model.cardSectionType.equals("mobile")) {
//					showMobileView(model);
//				}
//				else {
//					showCardView(model);
//				}
			}
		});
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return cards.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return null;
	}

	@Override
	public int getGroupCount() {
		return cards.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		SectionHolder cell = null;
		if (convertView == null) {
			cell = new SectionHolder();
			convertView = inflater.inflate(R.layout.index_section, null);
			cell.titleView = (TextView) convertView.findViewById(R.id.titleView);
			convertView.setTag(cell);
		}
		else {
			cell = (SectionHolder) convertView.getTag();
		}
		if (cards.get(groupPosition).size()>0) {
			cell.titleView.setText(cards.get(groupPosition).get(0).cardSectionType);
		}
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
	
}
