import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import surf.demo.NILMMetadata_BLUED;
import surf.metadata.nilmmetadata.DatasetMetadata;
import surf.metadata.nilmmetadata.GeoLocationMetadata;
import surf.metadata.nilmmetadata.RoomMetadata;


public class TestNILMMetadata {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//System.out.println(NILMMetadata_BLUED.getDatasetMetadata());
		
		System.out.println(NILMMetadata_BLUED.getBuildingMetadata());
		
		//System.out.println(NILMMetadata_BLUED.getMeterDevicesMetadata());
		
		//System.out.println(RoomMetadata.RoomName.DINING_ROOM.getName());
		
	}

	
}
