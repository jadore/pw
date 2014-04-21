package im.bean;

import bean.Entity;

/**
 * 
 * 最近联系人显示的与某个的聊天记录
 * 
 */
public class Conversation extends Entity{
	public String roomId;
	public String content; 
	public String openId; 
	public String noticeTime; 
	public Integer noticeSum;
	public Integer noticeType;
	public Integer noticeStatus;
	public Integer noticeMediaType;
}
