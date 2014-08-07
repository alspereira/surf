import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.tokens.AnchorToken;


public class TestMetadata {
	
	static Map<Integer, Map<String, Map<String, Object>>> myTest = new LinkedHashMap<Integer, Map<String, Map<String, Object>>>();
	static Map<String, Object> props = new LinkedHashMap<String, Object>();
	static Map<String, Map<String, Object>> myAnchorMap = new LinkedHashMap<String, Map<String, Object>>();
	static Mark mark = new Mark("test1", 0, 0, 0, "*The first line.\nThe last line.", 0);
    static AnchorToken anchor = new AnchorToken("&id123", mark, mark);
    
    static DumperOptions options = new DumperOptions();
    
    public static void main(String[] args) {
    	props.put("prop_1", "value 1");
    	props.put("prop_1", "value 1");
    	myAnchorMap.put(anchor.getValue(), props);
    	myTest.put(1, myAnchorMap);
    	
    	options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    	
    	Yaml yaml = new Yaml( options );
    	System.out.println(yaml.dump( myTest ));
    	
    	System.out.println(anchor.getTokenId());
    	System.out.println(anchor.getValue());
    	
    }
	
	

}
