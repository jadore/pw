package ui.adapter;

import java.util.HashMap;
import java.util.List;

import tools.Logger;
import ui.Index;
import ui.adapter.IndexPhoneAdapter.CellHolder;
import widget.IphoneTreeView;
import widget.IphoneTreeView.IphoneTreeHeaderAdapter;

import com.google.android.gms.analytics.internal.Command;
import com.vikaa.mycontact.R;


import bean.PhoneIntroEntity;
import config.CommonValue;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

public class IphoneTreeViewAdapter extends BaseExpandableListAdapter{

	private ExpandableListView iphoneTreeView;
	
	private String[] groups = { 
			CommonValue.PhoneSectionType .OwnedSectionType,
			CommonValue.PhoneSectionType.JoinedSectionType,
			CommonValue.ActivitySectionType.OwnedSectionType,
			CommonValue.ActivitySectionType.JoinedSectionType,
			CommonValue.FamilySectionType.FamilySectionType,
			CommonValue.FamilySectionType.ClanSectionType
	};
	private Context context;
	private LayoutInflater inflater;
	private List<List<PhoneIntroEntity>> phones;
	public IphoneTreeViewAdapter(ExpandableListView iphoneTreeView, Context context, List<List<PhoneIntroEntity>> phones) {
		this.iphoneTreeView = iphoneTreeView;
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.phones = phones;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return phones.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (groupPosition>=0 && groupPosition <= 5) {
			return phones.get(groupPosition).size();
		}
		else if (groupPosition<0) {
			return phones.get(0).size();
		} 
		else {
			return phones.get(5).size();
		}
	}

	@Override
	public Object getGroup(int groupPosition) {
//		if (groupPosition>0) {
//			return groups[groupPosition];
//		}
//		return  groups[0];
		if (groupPosition>=0 && groupPosition <= 5) {
			return groups[groupPosition];
		}
		else if (groupPosition<0) {
			return  groups[0];
		} 
		else {
			return  groups[5];
		}
	}

	@Override
	public int getGroupCount() {
		return groups.length;
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
//		if (convertView == null) {
//			convertView = inflater.inflate(R.layout.list_group_view, null);
//		}
//		TextView groupName = (TextView) convertView
//				.findViewById(R.id.group_name);
//		groupName.setText(getGroup(groupPosition).toString());
//
//		ImageView indicator = (ImageView) convertView
//				.findViewById(R.id.group_indicator);
//		TextView onlineNum = (TextView) convertView
//				.findViewById(R.id.online_count);
//		onlineNum.setText(getChildrenCount(groupPosition) + "");
//		if (isExpanded) {
//			indicator.setImageResource(R.drawable.indicator_expanded);
//		} else {
//			indicator.setImageResource(R.drawable.indicator_unexpanded);
//		}
//		return convertView;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.index_group_divider, null);
		}
		return convertView;
	}

	static class CellHolder {
		TextView titleView;
		TextView desView;
	}
	
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
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
		final PhoneIntroEntity model = (PhoneIntroEntity) getChild(groupPosition, childPosition);
		if (groupPosition == 4 ) {
			cell.desView.setText(model.content);
			cell.desView.setText(String.format("%s氏   人数:%s", model.family_name, model.member));
		}
		else if (groupPosition == 5 ) {
			cell.desView.setText(model.content);
			cell.desView.setText(model.family_intro);
		}
		else if (groupPosition == 0 ) {
			cell.desView.setText(String.format("人数:%s   点击数:%s", model.member, model.hits));
		}
		else if (groupPosition == 1) {
			cell.desView.setText(String.format("人数:%s   发起人:%s", model.member, model.creator));
		}
		else if (groupPosition == 2 ) {//activity created
			cell.desView.setText(String.format("点击数:%s   参加人数:%s", model.hits, model.member));
		}
		else if (groupPosition == 3) {//activity took part
			cell.desView.setText(String.format("聚会时间:%s   参加人数:%s", model.begin_at, model.member));
		}
		cell.titleView.setText(model.title);
		convertView.setTag(R.id.title, groupPosition);
		convertView.setTag(R.id.des, childPosition);
		convertView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				((Index)context).showSharePre(false, null, model);
				return false;
			}
		});
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (model.phoneSectionType.equals(CommonValue.PhoneSectionType.OwnedSectionType) 
						|| model.phoneSectionType.equals(CommonValue.PhoneSectionType.JoinedSectionType)) {
					((Index)context).showPhoneViewWeb(model);
				}
				else {
					((Index)context).showActivityViewWeb(model);
				}
			}
		});
		return convertView;
		
	}
	
}
