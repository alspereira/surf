package surf.metadata.nilmmetadata;

import java.util.Calendar;

public class BuildingMetadata extends NILMMetadata {
	
	private final String SOURCE_SCHEMA 				= 	"NILM Metadata :: Building";
	private final String SOURCE_FILE;

	public static String INSTANCE					= 	"instance";
	public static String ORIGINAL_NAME 				= 	"original_name";
	public static String ELEC_METERS				=	"elec_meters";
	public static String APPLIANCES					=	"appliances";
	public static String DESCRIPTION				=	"description";
	public static String ROOMS						=	"rooms";
	public static String NUM_OCCUPANTS				= 	"n_occupants";
	public static String PERIODS_UNOCCUPIED			=	"periods_unoccupied";
	
	// Inherited from Dataset
	public static String GEO_LOCATION				=	"geo_location";
	public static String TEMPORAL_COVERAGE			=	"temporal_coverage";
	public static String TIMEZONE					= 	"timezone";

	public BuildingMetadata(int building_seq) {
		super();
		this.SOURCE_FILE = "building<" + building_seq + ">.yaml";
		super.addProperty(NILMMetadata.SOURCE_SCHEMA, SOURCE_SCHEMA);
		super.addProperty(NILMMetadata.SOURCE_FILE, SOURCE_FILE);
		super.addProperty(NILMMetadata.LAST_UPDATE, NILMMetadata.getFormatedDate(
				Calendar.getInstance().getTime()));
		super.addProperty(INSTANCE, building_seq);
	}
	
}
