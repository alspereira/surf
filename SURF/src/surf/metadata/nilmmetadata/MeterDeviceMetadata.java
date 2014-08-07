package surf.metadata.nilmmetadata;

import java.util.Calendar;

public class MeterDeviceMetadata extends NILMMetadata {
	
	private final String SOURCE_SCHEMA 				= 	"NILM Metadata :: MeterDevice";
	private final String SOURCE_FILE				=	"meter_devices.yaml";

	public static String MODEL						= 	"model";
	public static String MODEL_URL	 				= 	"model_url";
	public static String MANUFACTURER				=	"manufacturer";
	public static String SAMPLE_PERIOD				=	"sample_period";
	public static String MAX_SAMPLE_PERIOD			=	"max_sample_period";
	public static String MEASUREMENTS				=	"measurements";
	//public static String MEASUREMENT_LIMITS			= 	"measurement_limits";
	public static String DESCRIPTION				=	"description";
	public static String WIRELESS					=	"wireless";
	public static String WIRELESS_BASE				=	"wireless_base";
	public static String DATA_LOGGER				=	"data_logger";

	public MeterDeviceMetadata() {
		super();
		super.addProperty(NILMMetadata.SOURCE_SCHEMA, SOURCE_SCHEMA);
		super.addProperty(NILMMetadata.SOURCE_FILE, SOURCE_FILE);
		super.addProperty(NILMMetadata.LAST_UPDATE, NILMMetadata.getFormatedDate(
				Calendar.getInstance().getTime()));
	}
	
}
