package surf.metadata.nilmmetadata;

import java.util.Calendar;

public class ElecMeterMetadata extends NILMMetadata {
	
	private final String SOURCE_SCHEMA 				= 	"NILM Metadata :: ElecMeter";		

	public static String DEVICE_MODEL				= 	"device_model";
	public static String SUBMETER_OF				= 	"submeter_of";
	public static String SUBMETER_OF_IS_UNCERTAIN	=	"submeter_of_is_uncertain";
	public static String UPSTREAM_METER_IN_BUILDING	=	"upstream_meter_in_building";
	public static String SITE_METER					=	"site_meter";
	public static String ROOM						=	"room";
	public static String FLOOR						= 	"floor";
	public static String DATA_LOCATION				=	"data_location";
	public static String PREPROCESSING_APPLIED		=	"preprocessing_applied";
	public static String STATISTICS					=	"statistics";
	
	public ElecMeterMetadata() {
		super();
		super.addProperty(NILMMetadata.SOURCE_SCHEMA, SOURCE_SCHEMA);
		super.addProperty(NILMMetadata.LAST_UPDATE, NILMMetadata.getFormatedDate(
				Calendar.getInstance().getTime()));
	}
	
}
