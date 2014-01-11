package bean;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import tools.AppException;
import tools.Logger;

public class CodeEntity extends Entity{
	public String info;
	
	public static CodeEntity parse(String res) throws IOException, AppException {
		CodeEntity data = new CodeEntity();
		try {
			JSONObject js = new JSONObject(res);
			if (js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				data.info = js.getString("info");
			}
			else {
				data.error_code = 11;
				data.message = js.getString("info");
			}
			
		} catch (JSONException e) {
			throw AppException.json(e);
		}
		return data;
	}
}
