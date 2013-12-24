package ui.adapter;

import java.util.List;

import tools.Logger;
import ui.CardView;

import bean.ActivityViewEntity;
import bean.CardIntroEntity;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.vikaa.mycontact.R;

import config.CommonValue;
import config.MyApplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import za.co.immedia.pinnedheaderlistview.SectionedBaseAdapter;

public class ActivityViewMembersAdapter extends SectionedBaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private List<List<CardIntroEntity>> cards;
	public ActivityViewEntity activity;
	public MyApplication appContext;
	public ActivityViewMembersAdapter(Context context, List<List<CardIntroEntity>> cards, MyApplication appContext) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.cards = cards;
		this.appContext = appContext;
	}
	
	static class CellHolder {
		ImageView avatarImageView;
		TextView titleView;
		TextView desView;
	}
	
	static class SectionView {
		TextView titleView;
	}
	
	@Override
	public Object getItem(int section, int position) {
		return cards.get(section).get(position);
	}

	@Override
	public long getItemId(int section, int position) {
		return position;
	}

	@Override
	public int getSectionCount() {
		return cards.size();
	}

	@Override
	public int getCountForSection(int section) {
		return cards.get(section).size();
	}

	@Override
	public View getItemView(int section, int position, View convertView,
			ViewGroup parent) {
		CellHolder cell = null;
		if (convertView == null) {
			cell = new CellHolder();
			convertView = inflater.inflate(R.layout.view_members_cell, null);
			cell.avatarImageView = (ImageView) convertView.findViewById(R.id.avatarImageView);
			cell.titleView = (TextView) convertView.findViewById(R.id.title);
			cell.desView = (TextView) convertView.findViewById(R.id.des);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		final CardIntroEntity model = cards.get(section).get(position);
		ImageLoader.getInstance().displayImage(model.headimgurl, cell.avatarImageView, CommonValue.DisplayOptions.default_options);
		cell.titleView.setText(String.format("%s(%s)", model.realname, model.nickname));
		cell.desView.setText(String.format("%s %s", model.department, model.position));
		convertView.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (activity.openid.equals(appContext.getLoginUid())) {
					showCardView(model);
				}
			}
		});
		return convertView;
	}

	@Override
	public View getSectionHeaderView(int section, View convertView,
			ViewGroup parent) {
		SectionView sect = null;
		if (convertView == null) {
			sect = new SectionView();
			convertView = inflater.inflate(R.layout.view_members_section, null);
			sect.titleView = (TextView) convertView.findViewById(R.id.titleView);
			convertView.setTag(sect);
		}
		else {
			sect = (SectionView) convertView.getTag();
		}
		if (cards.size() > 0) {
			sect.titleView.setText(cards.get(section).get(0).cardSectionType);
		}
		return convertView;
	}
	
	private void showCardView(CardIntroEntity entity) {
		Intent intent = new Intent(context, CardView.class);
		intent.putExtra(CommonValue.CardViewIntentKeyValue.CardView, entity);
		context.startActivity(intent);
	}

}
