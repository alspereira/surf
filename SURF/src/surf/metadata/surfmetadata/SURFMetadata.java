package surf.metadata.surfmetadata;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONValue;

public class SURFMetadata {
	
	public static String SURF_ID = "surf_id";
	
	private Map<String, Object> properties;
	
	public SURFMetadata() {
		this.properties = new LinkedHashMap<String, Object>();
	}

	public String toJSONString() {
		return JSONValue.toJSONString(properties);
	}
	
	public void addProperty(String key, Object value) {
		properties.put(key, value);
	}
	
	public void removeProperty(String key) {
		properties.remove(key);
	}
	
	public Object getProperty(String key) {
		return properties.get(key);
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}
}
