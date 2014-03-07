/**
 * QYdonal
 */
package bean;

import org.json.JSONException;
import org.json.JSONObject;

import tools.AppException;
import tools.Logger;

/**
 * QY
 *
 * @author donal
 *
 */
public class RegUserEntity extends Entity{
	public String info;
	public String url;
	
	public static RegUserEntity parse(String res) throws AppException {
		RegUserEntity data = new RegUserEntity();
		try {
			JSONObject js = new JSONObject(res);
			if (js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				data.info = js.getString("info");
				data.url = js.getString("url");
			}
			else {
				if (!js.isNull("error_code")) {
					data.error_code = js.getInt("error_code");
				}
				data.message = js.getString("info");
			}
		} catch (JSONException e) {
			Logger.i(res);
			throw AppException.json(e);
		}
		return data;
	}
}
