package surf.metadata.nilmmetadata;

public class RoomMetadata extends NILMMetadata {
	
	public static String NAME 			= "name";
	public static String INSTANCE		= "instance";
	public static String DESCRIPTION	= "description";
	public static String FLOOR			= "floor";
	
	public static enum RoomName {
		LOUNGE("Lounge"), KITCHEN("Kitchen"), BEDROOM("Bedroom"), UTILITY("Utility"), GARAGE("Garage"), BASEMENT("Basement"), BATHROOM("Bathroom"), STUDY("Study"), 
		NURSERY("Nursery"), HALL("Hall"), DINING_ROOM("Dinning Room"), OUTDOORS("Outdoors"); 
		
		private String value;
		
		private RoomName(String value) {
			this.value = value;
		}
		
		public String getName() {
			return value;
		}
	
	}
		
	public RoomMetadata() {
		super();
	}	

}
