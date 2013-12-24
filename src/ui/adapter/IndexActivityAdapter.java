package ui.adapter;

import java.util.List;

import bean.ActivityIntroEntity;
import bean.PhoneIntroEntity;

import com.vikaa.mycontact.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ui.adapter.IndexPhoneAdapter.CellHolder;
import ui.adapter.IndexPhoneAdapter.SectionView;
import za.co.immedia.pinnedheaderlistview.SectionedBaseAdapter;

public class IndexActivityAdapter extends SectionedBaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private List<List<ActivityIntroEntity>> activities;
	public IndexActivityAdapter(Context context, List<List<ActivityIntroEntity>> activities) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.activities = activities;
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
		return activities.get(section).get(position);
	}

	@Override
	public long getItemId(int section, int position) {
		return position;
	}

	@Override
	public int getSectionCount() {
		return activities.size();
	}

	@Override
	public int getCountForSection(int section) {
		return activities.get(section).size();
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
		ActivityIntroEntity model = activities.get(section).get(position);
		cell.titleView.setText(model.title);
		cell.desView.setText(String.format("活动时间:%s 参加人数:%s", model.begin_at, model.member));
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
		sect.titleView.setText(activities.get(section).get(0).activitySectionType);
		return convertView;
	}

}
