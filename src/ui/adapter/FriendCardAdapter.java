package ui.adapter;

import java.util.List;
import tools.StringUtils;
import ui.CardView;
import ui.CreateView;
import ui.FriendCards;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.vikaa.mycontact.R;

import config.CommonValue;

import bean.CardIntroEntity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
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
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		final CardIntroEntity model = cards.get(position);
		ImageLoader.getInstance().displayImage(model.headimgurl, cell.avatarImageView, CommonValue.DisplayOptions.default_options);
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
		Intent intent = new Intent(context, CreateView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/card/%s", CommonValue.BASE_URL, entity.code));
		((FriendCards)context).startActivityForResult(intent, CommonValue.CardViewUrlRequest.editCard);
	}
	
}
