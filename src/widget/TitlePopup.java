package widget;

import java.util.ArrayList;

import com.vikaa.mycontact.R;

import tools.ImageUtils;
import tools.Logger;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class TitlePopup extends PopupWindow {
	private Context mContext;

	protected final int LIST_PADDING = 10;
	
	private Rect mRect = new Rect();
	
	private final int[] mLocation = new int[2];
	
	private int mScreenWidth,mScreenHeight;

	private boolean mIsDirty;
	
	private int popupGravity = Gravity.NO_GRAVITY;	
	
	private OnItemOnClickListener mItemOnClickListener;
	
	private ListView mListView;
	
	private ArrayList<ActionItem> mActionItems = new ArrayList<ActionItem>();			
	
	public TitlePopup(Context context){
		this(context, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}
	
	public TitlePopup(Context context, int width, int height){
		this.mContext = context;
		
		setFocusable(true);
		setTouchable(true);	
		setOutsideTouchable(true);
		
		mScreenWidth = ImageUtils.getDisplayWidth(context);
		mScreenHeight = ImageUtils.getDisplayHeighth(context);
		
		setWidth(width);
		setHeight(height);
		
		setBackgroundDrawable(new BitmapDrawable());
		
		setContentView(LayoutInflater.from(mContext).inflate(R.layout.timeline_popup, null));
		
		initUI();
	}
		
	private void initUI(){
		
		mListView = (ListView) getContentView().findViewById(R.id.title_list);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,long arg3) {
				dismiss();
				
				if(mItemOnClickListener != null)
					mItemOnClickListener.onItemClick(mActionItems.get(index), index);
			}
		}); 
	}
	
	public void show(View view){
		view.getLocationOnScreen(mLocation);
		
		mRect.set(mLocation[0], mLocation[1], mLocation[0] + view.getWidth(), mLocation[1] + view.getHeight());
		
		if(mIsDirty){
			populateActions();
		}
		showAtLocation(view, popupGravity, mScreenWidth - LIST_PADDING - (getWidth()/2)-20, ImageUtils.dip2px(mContext, 44)+getStatusBarHeight());
	}
	public int getStatusBarHeight() {
		  int result = 0;
		  int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
		  if (resourceId > 0) {
		      result = mContext.getResources().getDimensionPixelSize(resourceId);
		  }
		  return result;
	}
	
	static class CellHolder {
		TextView titleView;
		ImageView iconView;
	}
	
	private void populateActions(){
		mIsDirty = false;
		
		mListView.setAdapter(new BaseAdapter() {			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				CellHolder cell = null;
				if (convertView == null) {
					cell = new CellHolder();
					convertView = LayoutInflater.from(mContext).inflate(R.layout.more_cell, null);
					cell.titleView = (TextView) convertView.findViewById(R.id.title);
					cell.iconView = (ImageView) convertView.findViewById(R.id.icon);
					convertView.setTag(cell);
				}
				else {
					cell = (CellHolder) convertView.getTag();
				}
				ActionItem item = mActionItems.get(position);
				cell.titleView.setText(item.mTitle);
				cell.titleView.setTextColor(mContext.getResources().getColor(R.color.white));
				cell.iconView.setImageDrawable(item.mDrawable);
				convertView.setBackgroundColor(mContext.getResources().getColor(R.color.nav_color));
				return convertView;
			}
			
			@Override
			public long getItemId(int position) {
				return position;
			}
			
			@Override
			public Object getItem(int position) {
				return mActionItems.get(position);
			}
			
			@Override
			public int getCount() {
				return mActionItems.size();
			}
		}) ;
	}
	
	public void addAction(ActionItem action){
		if(action != null){
			mActionItems.add(action);
			mIsDirty = true;
		}
	}
	
	public void cleanAction(){
		if(mActionItems.isEmpty()){
			mActionItems.clear();
			mIsDirty = true;
		}
	}
	
	public ActionItem getAction(int position){
		if(position < 0 || position > mActionItems.size())
			return null;
		return mActionItems.get(position);
	}			
	
	public void setItemOnClickListener(OnItemOnClickListener onItemOnClickListener){
		this.mItemOnClickListener = onItemOnClickListener;
	}
	
	public static interface OnItemOnClickListener{
		public void onItemClick(ActionItem item , int position);
	}
}
