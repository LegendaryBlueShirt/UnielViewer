package uniViewer.model;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class NameOverride {
	static Gson gson = new Gson();
	static Map<Integer, String> override;
	public static String getNameOverride(int id) {
		if(override == null) {
			return null;
		}
		if(override.containsKey(id)) {
			return override.get(id);
		} else {
			return null;
		}
	}
	
	public static void setOverrideData(String json) {
		java.lang.reflect.Type typeOfHashMap = new TypeToken<Map<Integer, String>>() { }.getType();
		override = gson.fromJson(json, typeOfHashMap); // This type must match TypeToken
	}
}
