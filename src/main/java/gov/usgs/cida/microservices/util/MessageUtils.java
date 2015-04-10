package gov.usgs.cida.microservices.util;

import java.util.Map;

public class MessageUtils {
	public static String getStringFromHeaders(Map<String, Object> params, String key) {
		return params.get(key) != null ? params.get(key).toString() : null;
	}
}
