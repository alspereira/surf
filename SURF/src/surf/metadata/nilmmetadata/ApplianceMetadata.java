package surf.metadata.nilmmetadata;

import java.util.Calendar;

public class ApplianceMetadata extends NILMMetadata {
	
	private final String SOURCE_SCHEMA 				= 	"NILM Metadata :: Appliance";		

	public static String TYPE						= 	"type";
	public static String INSTANCE					= 	"instance";
	public static String METERS						=	"meters";
	public static String ON_POWER_THRESHOLD			=	"on_power_threshold";
	public static String MINIMUM_OFF_DURATION		=	"minimum_off_duration";
	public static String MINIMUM_ON_DURATION		=	"minimum_on_duration";
	public static String DOMINANT_APPLIANCE			= 	"dominant_appliance";
	public static String ROOM						=	"room";
	public static String MULTIPLE					=	"multiple";
	public static String COUNT						=	"count";
	public static String CONTROL					=	"control";
	public static String EFFICIENCY_RATING			=	"efficiency_rating";
	public static String NOMINAL_CONSUMPTION		=	"nominal_consumption";
	public static String COMPONENTS					=	"components";
	public static String MODEL						=	"model";
	public static String MANUFACTURER				=	"manufacturer";
	public static String ORIGINAL_NAME				=	"original_name";
	public static String DATES_ACTIVE				=	"dates_active";
	public static String YEAR_OF_PURCHASE			=	"year_of_purchase";
	public static String YEAR_OF_MANUFACTURE		=	"year_of_manufacture";
	public static String SUBTYPE					=	"subtype";
	public static String PART_NUMBER				=	"part_number";
	public static String GLOBAL_TRADE_ITEM_NUMBER	=	"gtin";
	public static String VERSION					=	"version";
	
	public static String ORIGINAL_INSTANCE			=	"original_instance";
	public static String AVERAGE_POWER_CONSUMPTION	=	"average_power_consumption";
	public static String NUMBER_OF_EVENTS			=	"number_of_events";
	
	public ApplianceMetadata() {
		super();
		super.addProperty(NILMMetadata.SOURCE_SCHEMA, SOURCE_SCHEMA);
		super.addProperty(NILMMetadata.LAST_UPDATE, NILMMetadata.getFormatedDate(
				Calendar.getInstance().getTime()));
	}
	
}
