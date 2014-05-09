package ui.adapter;

import java.util.List;

import ui.Index;
import ui.Setting;

import com.vikaa.mycontact.R;

import config.CommonValue;
import bean.CardIntroEntity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class SettingCardAdapter extends BaseExpandableListAdapter{

	private ExpandableListView iphoneTreeView;
	
	private Context context;
	private LayoutInflater inflater;
	private List<List<CardIntroEntity>> cards;
	
	
	static class CellHolder {
		TextView titleView;
	}
	
	public SettingCardAdapter(ExpandableListView iphoneTreeView, Context context, List<List<CardIntroEntity>> cards) {
		this.iphoneTreeView = iphoneTreeView;
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.cards = cards;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return cards.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return cards.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return cards.get(groupPosition).get(0).cardSectionType;
	}

	@Override
	public int getGroupCount() {
		return cards.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}


	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.messagecenter_section, null);
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		CellHolder cell = null;
		if (convertView == null) {
			cell = new CellHolder();
			convertView = inflater.inflate(R.layout.more_cell, null);
			cell.titleView = (TextView) convertView.findViewById(R.id.title);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		final CardIntroEntity model = cards.get(groupPosition).get(childPosition);
		cell.titleView.setText(model.realname);
		if (model.cardSectionType.equals(CommonValue.CardSectionType.SettingsSectionType)) {
			if (childPosition == 0) {
				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						((Setting)context).showDialog(1);
					}
				});
			}
		}
		else {
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					((Setting)context).logout();
				}
			});
		}
		return convertView;
	}
}
