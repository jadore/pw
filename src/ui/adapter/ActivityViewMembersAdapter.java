package ui.adapter;

import java.util.List;

import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.CardView;
import ui.adapter.PhonebookViewMembersAdapter.CellHolder;
import ui.adapter.PhonebookViewMembersAdapter.Operation;

import bean.ActivityViewEntity;
import bean.CardIntroEntity;
import bean.Entity;
import bean.Result;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.MyApplication;
import config.AppClient.ClientCallback;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import za.co.immedia.pinnedheaderlistview.SectionedBaseAdapter;

public class ActivityViewMembersAdapter extends SectionedBaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private List<List<CardIntroEntity>> cards;
	public ActivityViewEntity activity;
	public MyApplication appContext;
	private ProgressDialog loadingPd;
	
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
		TextView phoneView;
		TextView roleView;
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
			cell.phoneView = (TextView) convertView.findViewById(R.id.mobile);
			cell.roleView = (TextView) convertView.findViewById(R.id.role);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		final CardIntroEntity model = cards.get(section).get(position);
		ImageLoader.getInstance().displayImage(model.headimgurl, cell.avatarImageView, CommonValue.DisplayOptions.default_options);
		realnameEncode(cell, model);
		cell.desView.setVisibility(View.GONE);
		if (StringUtils.notEmpty(model.department)) {
			cell.desView.setVisibility(View.VISIBLE);
			cell.desView.setText(String.format("%s %s", model.department, model.position));
		}
		convertView.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getRole(model);
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
	
	private void realnameEncode(CellHolder cell, CardIntroEntity model) {
		String realName = null;
		String mobile = null;
		if (activity.openid.equals(appContext.getLoginUid())) {
			realName = String.format("%s(%s)", model.realname, model.nickname);
			mobile = model.phone;
		}
		else {
			realName = String.format("***(%s)", model.nickname);
			mobile = String.format("%s********", model.phone.subSequence(0, 3));
		}
		if (model.openid.equals(appContext.getLoginUid())) {
			realName = String.format("%s(%s)", model.realname, model.nickname);
			mobile = model.phone;
		}
		if (model.isfriend.equals(CommonValue.PhonebookLimitRight.Frined_Yes)) {
			realName = String.format("%s(%s)", model.realname, model.nickname);
			mobile = model.phone;
		}
		cell.phoneView.setText(mobile);
		cell.titleView.setText(realName);
		cell.titleView.setTextColor(
				model.openid.equals(activity.openid)? 
						context.getResources().getColor(R.color.nav_color) : 
							context.getResources().getColor(R.color.black));
		if (model.openid.equals(activity.openid)) { // 发起人
			cell.roleView.setText("(发起人)");
		}
		else { // 普通成员
			cell.roleView.setText("");
		}
	}
	
	private void getRole(CardIntroEntity model) {
		if (activity.openid.equals(appContext.getLoginUid())) { // 发起人
			RoleCreator(model);
		}
		else { // 普通成员
			RolePublic(model);
		}
	}
	
	private void RoleCreator(CardIntroEntity model) {
		String[] oprators;
		if (model.openid.equals(appContext.getLoginUid())) { // 自己 修改我的名片
			oprators = new String[] { Operation.EditCard };
			show1OptionsDialog(oprators, model);
		}
		else {
			oprators = new String[] { Operation.CallMobile, Operation.CheckCard };
			show2OptionsDialog(oprators, model);
		}
	}
	
	private void RolePublic(CardIntroEntity model) {
		String[] oprators;
		if (model.openid.equals(appContext.getLoginUid())) { // 自己 修改我的名片
			oprators = new String[] { Operation.EditCard };
			show1OptionsDialog(oprators, model);
		}
		else {
			if (model.isfriend.equals(CommonValue.PhonebookLimitRight.Friend_No)) {
				oprators = new String[] { Operation.ExchangeCard, Operation.CheckCard };
				show2OptionsDialog(oprators, model);
			}
			else if (model.isfriend.equals(CommonValue.PhonebookLimitRight.Friend_Wait)) {
				oprators = new String[] { Operation.Wait };
				show1OptionsDialog(oprators, model);
			}
			else {
				oprators = new String[] { Operation.CallMobile, Operation.CheckCard };
				show2OptionsDialog(oprators, model);
			}
		}
	}
	
	private void show2OptionsDialog(final String[] arg ,final CardIntroEntity model){
		new AlertDialog.Builder(context).setTitle(model.nickname).setItems(arg,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					if (arg[0].equals(Operation.CallMobile)) {
						callMobile(model.phone);
					}
					else if (arg[0].equals(Operation.ExchangeCard)) {
						exchangeCard(model);
					}
					break;
				case 1:
					if (arg[1].equals(Operation.CheckCard)) {
						showCardView(model);
					}
					break;
				}
			}
		}).show();
	}
	
	private void show1OptionsDialog(final String[] arg ,final CardIntroEntity model){
		new AlertDialog.Builder(context).setTitle(model.nickname).setItems(arg,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					if (arg[0].equals(Operation.EditCard)) {
						showCardView(model);
					}
					else if (arg[0].equals(Operation.CheckCard)) {
						showCardView(model);
					}
					else if (arg[0].equals(Operation.ExchangeCard)) {
						exchangeCard(model);
					}
					break;
				}
			}
		}).show();
	}
	
	private void showCardView(CardIntroEntity entity) {
		Intent intent = new Intent(context, CardView.class);
		intent.putExtra(CommonValue.CardViewIntentKeyValue.CardView, entity);
		context.startActivity(intent);
	}
	
	private void exchangeCard(final CardIntroEntity model) {
//		loadingPd = UIHelper.showProgress(context, null, null, true);
		AppClient.followCard(appContext, model.openid, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				switch (data.getError_code()) {
				case Result.RESULT_OK:
					Logger.i(model.isfriend);
					model.isfriend = CommonValue.PhonebookLimitRight.Friend_Wait;
					Logger.i(model.isfriend);
					notifyDataSetChanged();
					break;
				default:
					UIHelper.ToastMessage(context, data.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
				UIHelper.ToastMessage(context, message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
			}
		});
	}
	
	private void callMobile(String moblie) {
		Uri uri = null;
		uri = Uri.parse("tel:" + moblie);
		Intent it = new Intent(Intent.ACTION_CALL, uri);
		context.startActivity(it);
	}

}
