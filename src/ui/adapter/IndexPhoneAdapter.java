package ui.adapter;

import java.util.List;

import com.vikaa.mycontact.R;

import bean.PhoneIntroEntity;
import ui.FriendCards;
import ui.HomeContactActivity;
import ui.Index;
import ui.QunZi;
import za.co.immedia.pinnedheaderlistview.SectionedBaseAdapter;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class IndexPhoneAdapter extends SectionedBaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<List<PhoneIntroEntity>> phones;
	public IndexPhoneAdapter(Context context, List<List<PhoneIntroEntity>> phones) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.phones = phones;
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
		return phones.get(section).get(position);
	}

	@Override
	public long getItemId(int section, int position) {
		return position;
	}

	@Override
	public int getSectionCount() {
		return phones.size();
	}

	@Override
	public int getCountForSection(int section) {
		return phones.get(section).size();
	}

	@Override
	public View getItemView(int section, int position, View convertView,
			ViewGroup parent) {
		CellHolder cell = null;
		if (convertView == null) {
			cell = new CellHolder();
			convertView = inflater.inflate(R.layout.index_cell, null);
			cell.titleView = (TextView) convertView.findViewById(R.id.title);
			cell.desView = (TextView) convertView.findViewById(R.id.des);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		final PhoneIntroEntity model = phones.get(section).get(position);
		if (section > 0) {
			cell.desView.setText(String.format("人数:%s 点击数:%s", model.member, model.hits));
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					((Index)context).showPhoneView(model);
				}
			});
			convertView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View arg0) {
					((Index)context).showShareDialog(model);
					return false;
				}
			});
		}
		else {
			cell.desView.setText(model.content);
			if (position == 0) {
				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						((Index)context).showMobileView();
					}
				});
			} else {
				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						((Index)context).showFriendCardView();
					}
				});
			}
		}
		cell.titleView.setText(model.title);
		return convertView;
	}

	@Override
	public View getSectionHeaderView(int section, View convertView,
			ViewGroup parent) {
		SectionView sect = null;
		if (convertView == null) {
			sect = new SectionView();
			convertView = inflater.inflate(R.layout.index_section, null);
			sect.titleView = (TextView) convertView.findViewById(R.id.titleView);
			convertView.setTag(sect);
		}
		else {
			sect = (SectionView) convertView.getTag();
		}
		sect.titleView.setText(phones.get(section).get(0).phoneSectionType);
		return convertView;
	}

}
