package surf.metadata.nilmmetadata;

import java.util.Calendar;

public class DatasetMetadata extends NILMMetadata {
	
	private final String SOURCE_SCHEMA 				= 	"NILM Metadata :: Dataset";
	private final String SOURCE_FILE				=	"dataset.yaml";

	public static String NAME 						= 	"name";
	public static String LONG_NAME 					= 	"long_name";
	public static String CREATORS					= 	"creators";
	public static String TIMEZONE					= 	"timezone";
	public static String PUBLICATION_DATE			=	"publication_date";
	public static String CONTACT					=	"contact";
	public static String INSTITUTION				=	"institution";
	public static String DESCRIPTION				=	"description";
	public static String NUMBER_OF_BUILDINGS		= 	"number_of_buildings";
	public static String IDENTIFIER					=	"identifier";
	public static String SUBJECT					=	"subject";
	public static String GEOSPATIAL_COVERAGE		=	"geospatial_coverage";
	public static String TEMPORAL_COVERAGE			=	"temporal_coverage";
	public static String FUNDING					=	"funding";
	public static String GEO_LOCATION				=	"geo_location";
	public static String RIGHTS_LIST				=	"rights_list";
	public static String DESCRIPTION_OF_SUBJECTS 	=	"description_of_subjects";
	public static String RELATED_DOCUMENTS			=	"related_documents";
	public static String SCHEMA						=	"schema";

	public DatasetMetadata() {
		super();
		super.addProperty(NILMMetadata.SOURCE_SCHEMA, SOURCE_SCHEMA);
		super.addProperty(NILMMetadata.SOURCE_FILE, SOURCE_FILE);
		super.addProperty(NILMMetadata.LAST_UPDATE, NILMMetadata.getFormatedDate(
				Calendar.getInstance().getTime()));
	}
	
}
