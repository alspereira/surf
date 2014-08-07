package surf.metadata.nilmmetadata;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONValue;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class NILMMetadata {
	
	public static String SOURCE_SCHEMA		= "source_schema";
	public static String SOURCE_FILE		= "source_file";
	public static String LAST_UPDATE 		= "last_update";
	
	private static final SimpleDateFormat dateFormat 	= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private Yaml yaml;
	private DumperOptions options = new DumperOptions();
	
	private Map<String, Object> properties;
	
	public NILMMetadata() {
		this.properties = new LinkedHashMap<String, Object>();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yaml = new Yaml(options);
	}
	
	public static String getFormatedDate(Date date) {
		return dateFormat.format(date);
	}
	
	public String toYAMLString() {
		return yaml.dump(properties);
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
