package ui.adapter;

import java.util.List;
import java.util.concurrent.ExecutionException;

import tools.StringUtils;
import ui.CardView;
import ui.QYWebView;
import ui.FriendCards;

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

public class FriendCardAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<CardIntroEntity> cards;
	
	static class CellHolder {
		TextView alpha;
		ImageView avatarImageView;
		TextView titleView;
		TextView desView;
		Button callButton;
	}
	
	public FriendCardAdapter(Context context, List<CardIntroEntity> cards) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.cards = cards;
	}
	
	@Override
	public int getCount() {
		return cards.size();
	}

	@Override
	public Object getItem(int arg0) {
		return cards.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return cards.get(arg0).getId();
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
		final CardIntroEntity model = cards.get(position);
		ImageLoader.getInstance().displayImage(model.avatar, cell.avatarImageView, CommonValue.DisplayOptions.default_options);
		cell.titleView.setText(model.realname);
		cell.desView.setText(String.format("%s %s", model.department, model.position));
		String currentStr = StringUtils.getAlpha(model.pinyin);
		String previewStr = (position - 1) >= 0 ? StringUtils.getAlpha(cards.get(position - 1).pinyin) : " ";
		if (!previewStr.equals(currentStr)) { 
			cell.alpha.setVisibility(View.VISIBLE);
			cell.alpha.setText(currentStr);
		} else {
			cell.alpha.setVisibility(View.GONE);
		}
		if (model.phone_display.indexOf("*") != -1 || StringUtils.isEmpty(model.phone_display)) {
			cell.callButton.setVisibility(View.INVISIBLE);
		}
		else {
			cell.callButton.setVisibility(View.VISIBLE);
		}
		cell.callButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse("tel:" + model.phone);
				Intent it;
				try {
					it = new Intent(Intent.ACTION_VIEW, uri);
				} catch (Exception e) {
					it = new Intent(Intent.ACTION_DIAL, uri);
				}
				context.startActivity(it);
			}
		});
		convertView.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showCardView(model);
			}
		});
		return convertView;
	}
	
	private void showCardView(CardIntroEntity entity) {
//		Intent intent = new Intent(context, CardView.class);
//		intent.putExtra(CommonValue.CardViewIntentKeyValue.CardView, entity);
//		context.startActivity(intent);
		Intent intent = new Intent(context, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/card/%s", CommonValue.BASE_URL, entity.code));
		((FriendCards)context).startActivityForResult(intent, CommonValue.CardViewUrlRequest.editCard);
	}
	
}
