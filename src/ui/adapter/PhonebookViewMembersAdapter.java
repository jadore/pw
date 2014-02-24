package ui.adapter;

import java.util.List;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.CardView;
import bean.CardIntroEntity;
import bean.CodeEntity;
import bean.Entity;
import bean.PhoneViewEntity;
import bean.Result;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;
import config.CommonValue;
import config.MyApplication;

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

public class PhonebookViewMembersAdapter extends SectionedBaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private List<List<CardIntroEntity>> cards;
	private MyApplication appContext;
	public PhoneViewEntity phonebook;
	private ProgressDialog loadingPd;
	
	public PhonebookViewMembersAdapter(Context context, List<List<CardIntroEntity>> cards, MyApplication appContext) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.cards = cards;
		this.appContext = appContext;
	}
	
	static class CellHolder {
		ImageView avatarImageView;
		TextView titleView;
		TextView roleView;
		TextView desView;
		TextView mobileView;
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
			cell.roleView = (TextView) convertView.findViewById(R.id.role);
			cell.desView = (TextView) convertView.findViewById(R.id.des);
			cell.mobileView = (TextView) convertView.findViewById(R.id.mobile);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		final CardIntroEntity model = cards.get(section).get(position);
		ImageLoader.getInstance().displayImage(model.headimgurl, cell.avatarImageView, CommonValue.DisplayOptions.default_options);
		realnameEncode(cell, model);
		cell.desView.setVisibility(View.GONE);
		if (!StringUtils.isEmpty(model.department)) {
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
	
	private void realnameEncode(CellHolder cell, CardIntroEntity model) {
		String realName = null;
		String mobile = null;
		if (phonebook.wechat_id.equals(appContext.getLoginUid())) {
			realName = String.format("%s(%s)", model.realname, model.nickname);
			mobile = model.phone;
		}
		else if (phonebook.readable.equals(CommonValue.PhonebookLimitRight.PBreadable_Yes)) {
			if (phonebook.wechat_id.equals(model.openid)) {
				realName = String.format("%s(%s)", model.realname, model.nickname);
				mobile = model.phone;
			}
			else if (model.readable.equals(CommonValue.PhonebookLimitRight.Memreadable_Yes)) {
				realName = String.format("%s(%s)", model.realname, model.nickname);
				mobile = model.phone;
			}
			else {
				realName = String.format("***(%s)", model.nickname);
				mobile = String.format("%s********", model.phone.subSequence(0, 3));
			}
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
		cell.mobileView.setText(mobile);
		cell.titleView.setText(realName);
		cell.titleView.getPaint().setFlags(0);
		cell.titleView.setTextColor(
					model.role.equals(CommonValue.PhonebookLimitRight.RolePublic)? 
						context.getResources().getColor(R.color.black) : 
							context.getResources().getColor(R.color.nav_color));
		if (model.openid.equals(phonebook.wechat_id)) { // 发起人
			cell.roleView.setText("(发起人)");
		}
		else if (model.role.equals(CommonValue.PhonebookLimitRight.RoleAdmin)) { // 管理员
			cell.roleView.setText("(管理员)");
		}
		else if (model.role.equals(CommonValue.PhonebookLimitRight.RoleNone)) { // 管理员
			cell.roleView.setText("(被移除)");
			cell.titleView.setTextColor(context.getResources().getColor(R.color.black));
			cell.titleView.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG|Paint.ANTI_ALIAS_FLAG);
		}
		else { // 普通成员
			cell.roleView.setText("");
		}
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
		if (cards.size() > 0 && cards.get(section).size() >0) {
			sect.titleView.setText(cards.get(section).get(0).cardSectionType);
		}
		return convertView;
	}
	
	private void showCardView(CardIntroEntity entity) {
		Intent intent = new Intent(context, CardView.class);
		intent.putExtra(CommonValue.CardViewIntentKeyValue.CardView, entity);
		context.startActivity(intent);
	}
	
	private void show5OptionsDialog(final String[] arg ,final CardIntroEntity model){
		new AlertDialog.Builder(context).setTitle(model.nickname).setItems(arg,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					if (arg[0].equals(Operation.CallMobile)) {
						callMobile(model.phone);
					}
					break;
				case 1:
					if (arg[1].equals(Operation.CheckCard)) {
						showCardView(model);
					}
					break;
				case 2:
					if (arg[2].equals(Operation.SetAdmin)) {
						setRole(model, CommonValue.PhonebookLimitRight.RoleAdmin);
					}
					break;
				case 3:
					if (arg[3].equals(Operation.DeleteCard)) {
						setRole(model, CommonValue.PhonebookLimitRight.RoleNone);
					}
					break;
				case 4:
					if (arg[4].equals(Operation.CancelPass)) {
						setPass(model, CommonValue.PhonebookLimitRight.QunReadable_No);
					}
					else if (arg[4].equals(Operation.SetPass)) {
						setPass(model, CommonValue.PhonebookLimitRight.QunReadable_Yes);
					}
					break;
				}
			}
		}).show();
	}
	
	private void show4OptionsDialog(final String[] arg ,final CardIntroEntity model){
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
				case 2:
					if (arg[2].equals(Operation.DeleteCard)) {
						setRole(model, CommonValue.PhonebookLimitRight.RoleNone);
					}
					break;
				case 3:
					if (arg[3].equals(Operation.CancelAdmin)) {
						setRole(model, CommonValue.PhonebookLimitRight.RolePublic);
					}
					else if (arg[3].equals(Operation.SetPass)) {
						setPass(model, CommonValue.PhonebookLimitRight.QunReadable_Yes);
					}
					else if (arg[3].equals(Operation.CancelPass)) {
						setPass(model, CommonValue.PhonebookLimitRight.QunReadable_No);
					}
					break;
				}
			}
		}).show();
	}
	
	private void show3OptionsDialog(final String[] arg ,final CardIntroEntity model){
		new AlertDialog.Builder(context).setTitle(model.nickname).setItems(arg,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					if (arg[0].equals(Operation.CheckCard)) {
						showCardView(model);
					}
					else if (arg[0].equals(Operation.ExchangeCard)) {
						exchangeCard(model);
					}
					break;
				case 1:
					if (arg[1].equals(Operation.DeleteCard)) {
						setRole(model, CommonValue.PhonebookLimitRight.RoleNone);
					}
					break;
				case 2:
					if (arg[2].equals(Operation.SetPass)) {
						setPass(model, CommonValue.PhonebookLimitRight.QunReadable_Yes);
					}
					else if (arg[2].equals(Operation.CancelPass)) {
						setPass(model, CommonValue.PhonebookLimitRight.QunReadable_No);
					}
					else if (arg[2].equals(Operation.CancelAdmin)) {
						setRole(model, CommonValue.PhonebookLimitRight.RolePublic);
					}
					break;
				}
			}
		}).show();
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
					else if (arg[0].equals(Operation.AddCard)) {
						setRole(model, CommonValue.PhonebookLimitRight.RolePublic);
					}
					break;
				}
			}
		}).show();
	}
	
	interface Operation {
		String EditCard = "查看名片";
		String ExchangeCard = "交换名片";
		String Wait = "查看名片";
		String CheckCard = "查看名片";
		
		String DeleteCard = "移除名片";
		String AddCard = "恢复名片";
		
		String SetAdmin = "设为管理员";
		String CancelAdmin = "取消管理员";
		
		String SetPass = "批准TA查看通讯录";
		String CancelPass = "禁止TA查看通讯录";
		
		String CallMobile = "拨打电话";
	}
	
	private void getRole(CardIntroEntity model) {
		if (phonebook.wechat_id.equals(appContext.getLoginUid())) { // 发起人
			RoleCreator(model);
		}
		else if (phonebook.is_admin.equals(CommonValue.PhonebookLimitRight.Admin_Yes)) { // 管理员
			Logger.i("admin");
			RoleAdmin(model);
		}
		else { // 普通成员
			RolePublic(model);
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
	
	private void RoleAdmin(CardIntroEntity model) {
		String[] oprators;
		if (model.openid.equals(appContext.getLoginUid())) { // 自己 修改我的名片
			oprators = new String[] { Operation.EditCard };
			show1OptionsDialog(oprators, model);
		}
		else {
			if (model.role.equals(CommonValue.PhonebookLimitRight.RolePublic)) { //普通成员
				if (model.isfriend.equals(CommonValue.PhonebookLimitRight.Frined_Yes)) {
					if (model.qun_readable.equals(CommonValue.PhonebookLimitRight.QunReadable_Yes)) {
						oprators = new String[] { Operation.CallMobile, Operation.CheckCard, Operation.DeleteCard, Operation.CancelPass };
						show4OptionsDialog(oprators, model);
					} 
					else {
						oprators = new String[] { Operation.CallMobile, Operation.CheckCard, Operation.DeleteCard, Operation.SetPass };
						show4OptionsDialog(oprators, model);
					}
				}
				else if (model.isfriend.equals(CommonValue.PhonebookLimitRight.Friend_No)) {
					if (model.qun_readable.equals(CommonValue.PhonebookLimitRight.QunReadable_Yes)) {
						oprators = new String[] { Operation.ExchangeCard, Operation.CheckCard, Operation.DeleteCard, Operation.CancelPass };
						show4OptionsDialog(oprators, model);
					} 
					else {
						oprators = new String[] { Operation.ExchangeCard, Operation.CheckCard, Operation.DeleteCard, Operation.SetPass };
						show4OptionsDialog(oprators, model);
					}
				}	
				else if (model.isfriend.equals(CommonValue.PhonebookLimitRight.Friend_Wait)) {
					if (model.qun_readable.equals(CommonValue.PhonebookLimitRight.QunReadable_Yes)) {
						oprators = new String[] { Operation.CheckCard, Operation.DeleteCard, Operation.CancelPass };
						show3OptionsDialog(oprators, model);
					} 
					else {
						oprators = new String[] { Operation.CheckCard, Operation.DeleteCard, Operation.SetPass };
						show3OptionsDialog(oprators, model);
					}
				}
			}
			else if (model.role.equals(CommonValue.PhonebookLimitRight.RoleNone)) {
				oprators = new String[] { Operation.AddCard };
				show1OptionsDialog(oprators, model);
			}
			else { // 管理员及发起人
				if (model.isfriend.equals(CommonValue.PhonebookLimitRight.Frined_Yes)) {
					oprators = new String[] { Operation.CallMobile, Operation.CheckCard };
					show2OptionsDialog(oprators, model);
				}
				else if (model.isfriend.equals(CommonValue.PhonebookLimitRight.Friend_No)) {
					oprators = new String[] { Operation.ExchangeCard, Operation.CheckCard };
					show2OptionsDialog(oprators, model);
				}	
				else if (model.isfriend.equals(CommonValue.PhonebookLimitRight.Friend_Wait)) {
					oprators = new String[] { Operation.CheckCard };
					show1OptionsDialog(oprators, model);
				}
			}
		}
	}
	
	private void RoleCreator(CardIntroEntity model) {
		String[] oprators;
		if (model.openid.equals(appContext.getLoginUid())) { // 自己 修改我的名片
			oprators = new String[] { Operation.EditCard };
			show1OptionsDialog(oprators, model);
		}
		else {
			if (model.role.equals(CommonValue.PhonebookLimitRight.RoleAdmin)) { //管理员
					oprators = new String[] { Operation.CallMobile, Operation.CheckCard, Operation.DeleteCard, Operation.CancelAdmin };
					show4OptionsDialog(oprators, model);
			}
			else if (model.role.equals(CommonValue.PhonebookLimitRight.RoleNone)) {
				oprators = new String[] { Operation.AddCard };
				show1OptionsDialog(oprators, model);
			}
			else {
					if (model.qun_readable.equals(CommonValue.PhonebookLimitRight.QunReadable_Yes)) {
						oprators = new String[] { Operation.CallMobile, Operation.CheckCard, Operation.SetAdmin, Operation.DeleteCard, Operation.CancelPass };
						show5OptionsDialog(oprators, model);
					}
					else {
						oprators = new String[] { Operation.CallMobile, Operation.CheckCard, Operation.SetAdmin, Operation.DeleteCard, Operation.SetPass };
						show5OptionsDialog(oprators, model);
					}
			}
		}
	}
	
	private void callMobile(String moblie) {
		Uri uri = null;
		uri = Uri.parse("tel:" + moblie);
		Intent it = new Intent(Intent.ACTION_CALL, uri);
		context.startActivity(it);
	}
	
	private void setRole(final CardIntroEntity model, final String role) {
//		loadingPd = UIHelper.showProgress(context, null, null, true);
		AppClient.setPhonebookRole(appContext, phonebook.code, model.openid, role, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				switch (data.getError_code()) {
				case Result.RESULT_OK:
					Logger.i(model.role);
					model.role = role;
					Logger.i(model.role);
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
	
	private void setPass(final CardIntroEntity model, final String state) {
//		loadingPd = UIHelper.showProgress(context, null, null, true);
		AppClient.setPhonebookPassmember(appContext, phonebook.code, model.openid, state, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				switch (data.getError_code()) {
				case Result.RESULT_OK:
					Logger.i(model.qun_readable);
					model.qun_readable = state;
					Logger.i(model.qun_readable);
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

}
