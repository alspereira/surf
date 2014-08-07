package surf.demo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import surf.metadata.nilmmetadata.ApplianceMetadata;
import surf.metadata.nilmmetadata.BuildingMetadata;
import surf.metadata.nilmmetadata.DatasetMetadata;
import surf.metadata.nilmmetadata.ElecMeterMetadata;
import surf.metadata.nilmmetadata.GeoLocationMetadata;
import surf.metadata.nilmmetadata.IntervalMetadata;
import surf.metadata.nilmmetadata.RoomMetadata;
import surf.metadata.nilmmetadata.MeterDeviceMetadata;

public class NILMMetadata_BLUED {

	/*public static String getDatasetMetadataFromFile() throws FileNotFoundException {
		InputStream input = new FileInputStream( new File( "assets/dataset.yaml" ) );	
		return toYamlString(input);
	}
	
	public static String getBuildingMetadataFromFile() throws FileNotFoundException {
		InputStream input = new FileInputStream( new File( "assets/building1.yaml" ) );
		return toYamlString(input);
	}
	
	public static String getMeterDevicesMetadataFromFile() throws FileNotFoundException {
		InputStream input = new FileInputStream( new File( "assets/meter_devices.yaml" ) );
		return toYamlString(input);
	}
	
	private static String toYamlString(InputStream input) {
		Yaml yaml = new Yaml();
		Map<String, Object> object = (Map<String, Object>) yaml.load(input);
		return yaml.dump(object);
	}*/
	
	public static String getRawDatasetMetadataFromFile() throws IOException {
		return getRawDataFromFile("assets/dataset.yaml");
	}
	
	public static String getRawBuildingMetadataFromFile() throws IOException {
		return getRawDataFromFile("assets/building1.yaml");
	}
	
	public static String getRawMeterDevicesMetadataFromFile() throws IOException {
		return getRawDataFromFile("assets/meter_devices.yaml");
	}

	public static String getRawDataFromFile(String filePath) throws IOException {
		Path path = Paths.get(filePath);
		return new String( Files.readAllBytes(path), Charset.forName("UTF-8") );
	}
	
 	public static String getDatasetMetadata() {
		DatasetMetadata dsmeta = new DatasetMetadata();
		GeoLocationMetadata geolocmeta = new GeoLocationMetadata();
		IntervalMetadata intMeta = new IntervalMetadata();
		
		dsmeta.addProperty(DatasetMetadata.NAME, "BLUED");
		dsmeta.addProperty(DatasetMetadata.LONG_NAME, "Building Level fUly Labeled Electricity Disaggregation Dataset");
		dsmeta.addProperty(DatasetMetadata.CREATORS, new String[] {"Anderson, Kyle", "Ocneanu, Adrian", "Benítez, Diego",
				"Carlson, Derrick", "Rowe, Anthony", "Bergés, Mario"});
		dsmeta.addProperty(DatasetMetadata.PUBLICATION_DATE, 2012);
		dsmeta.addProperty(DatasetMetadata.INSTITUTION, "Carnegie Mellon University (CMU)");
		dsmeta.addProperty(DatasetMetadata.CONTACT, "marioberges@cmu.edu");
		dsmeta.addProperty(DatasetMetadata.DESCRIPTION, "One week of fully labeled power demand data from one domestic "
				+ "building in the United States");
		dsmeta.addProperty(DatasetMetadata.NUMBER_OF_BUILDINGS, 1);
		dsmeta.addProperty(DatasetMetadata.TIMEZONE, "US/Eastern");
		
		geolocmeta.addProperty(GeoLocationMetadata.LOCALITY, "Pennsylvania");
		geolocmeta.addProperty(GeoLocationMetadata.COUNTRY, "US");
		geolocmeta.addProperty(GeoLocationMetadata.LATITUDE, 40.442508);
		geolocmeta.addProperty(GeoLocationMetadata.LONGITUDE, -79.942564);
		
		dsmeta.addProperty(DatasetMetadata.GEO_LOCATION, geolocmeta.getProperties());
		
		String document = "K. Anderson, A. Ocneanu, D. Benitez, D. Carlson, A. Rowe, and M. Berges. "
				+ "BLUED: A Fully Labeled Public Dataset for Event-Based Non-Intrusive Load Monitoring Research. "
				+ "In Proceedings of the 2nd KDD Workshop on Data Mining Applications in Sustainability (SustKDD), "
				+ "Beijing, China, 2012.";
		dsmeta.addProperty(DatasetMetadata.RELATED_DOCUMENTS, new String[] {"http://nilm.cmu.bi.org", document} );
		dsmeta.addProperty(DatasetMetadata.SCHEMA, "https://github.com/nilmtk/nilm_metadata/tree/v0.2.0");
		
		intMeta.addProperty(IntervalMetadata.START, "2011-10-20");
		intMeta.addProperty(IntervalMetadata.END, "2011-10-27");
		
		dsmeta.addProperty(DatasetMetadata.TEMPORAL_COVERAGE, intMeta.getProperties());
		
		return dsmeta.toYAMLString();
	}
	
	public static String getBuildingMetadata() {
		BuildingMetadata buildMeta = new BuildingMetadata(1);
		IntervalMetadata intMeta = new IntervalMetadata();
		
		intMeta.addProperty(IntervalMetadata.START, "2011-10-20");
		intMeta.addProperty(IntervalMetadata.END, "2011-10-27");

		buildMeta.addProperty(BuildingMetadata.ORIGINAL_NAME, "BLUED House 1");
		buildMeta.addProperty(BuildingMetadata.ELEC_METERS, getElecMeterMetadata());
		buildMeta.addProperty(BuildingMetadata.APPLIANCES, getApplianceMetadata());
		buildMeta.addProperty(BuildingMetadata.DESCRIPTION, "Single family house in Pittsburgh, Pennsylvania. The house has aproximately 50 electical appliances.");
		buildMeta.addProperty(BuildingMetadata.ROOMS, getRoomsMetadata());
		buildMeta.addProperty(BuildingMetadata.NUM_OCCUPANTS, 2);
		buildMeta.addProperty(BuildingMetadata.TEMPORAL_COVERAGE, intMeta.getProperties());
		
		return buildMeta.toYAMLString();
	}
	
	public static String getMeterDevicesMetadata() {
		MeterDeviceMetadata mdMeta = new MeterDeviceMetadata();
		LinkedHashMap<String, Object> device;
		
		device = new LinkedHashMap<String, Object>();
		device.put(MeterDeviceMetadata.SAMPLE_PERIOD, 0.016667); // 60 Hz
		device.put(MeterDeviceMetadata.MAX_SAMPLE_PERIOD, 8.333333333e-05); // 12 kHz
		mdMeta.addProperty("BLUED_whole_house", device);
		
		device = new LinkedHashMap<String, Object>();
		device.put(MeterDeviceMetadata.MODEL, "Firefly plug-level");
		device.put(MeterDeviceMetadata.MODEL_URL, "http://www.ece.cmu.edu/firefly/");
		device.put(MeterDeviceMetadata.MANUFACTURER, "Carnegie Mellon University");
		device.put(MeterDeviceMetadata.SAMPLE_PERIOD, 1);
		device.put(MeterDeviceMetadata.MAX_SAMPLE_PERIOD, 0.001); // 1 kHz
		mdMeta.addProperty("firefly_plug_level", device);
		
		device = new LinkedHashMap<String, Object>();
		device.put(MeterDeviceMetadata.MODEL, "Firefly environmental");
		device.put(MeterDeviceMetadata.MODEL_URL, "http://www.ece.cmu.edu/firefly/");
		device.put(MeterDeviceMetadata.MANUFACTURER, "Carnegie Mellon University");
		mdMeta.addProperty("firefly_environmental", device);
		
		device = new LinkedHashMap<String, Object>();
		device.put(MeterDeviceMetadata.MANUFACTURER, "Carnegie Mellon University");
		device.put(MeterDeviceMetadata.SAMPLE_PERIOD, 0.05); // 20 Hz
		mdMeta.addProperty("BLUED_circuit_level", device);
		
		
		return mdMeta.toYAMLString();
	}
	
	private static List<LinkedHashMap<String, Object>> getRoomsMetadata() {
		List<LinkedHashMap<String, Object>> roomMeta = new ArrayList<LinkedHashMap<String, Object>>();
		RoomMetadata roomMetaAux;
		
		roomMetaAux = new RoomMetadata();
		roomMetaAux.addProperty(RoomMetadata.NAME, RoomMetadata.RoomName.BASEMENT.getName());
		roomMeta.add((LinkedHashMap<String, Object>) roomMetaAux.getProperties());	
		roomMetaAux = new RoomMetadata();
		roomMetaAux.addProperty(RoomMetadata.NAME, RoomMetadata.RoomName.BATHROOM.getName());
		roomMetaAux.addProperty(RoomMetadata.INSTANCE, 1);
		roomMetaAux.addProperty(RoomMetadata.FLOOR, 0);
		roomMeta.add((LinkedHashMap<String, Object>) roomMetaAux.getProperties());
		roomMetaAux = new RoomMetadata();
		roomMetaAux.addProperty(RoomMetadata.NAME, RoomMetadata.RoomName.KITCHEN.getName());
		roomMetaAux.addProperty(RoomMetadata.FLOOR, 0);
		roomMeta.add((LinkedHashMap<String, Object>) roomMetaAux.getProperties());
		roomMetaAux = new RoomMetadata();
		roomMetaAux.addProperty(RoomMetadata.NAME, RoomMetadata.RoomName.DINING_ROOM.getName());
		roomMetaAux.addProperty(RoomMetadata.FLOOR, 0);
		roomMeta.add((LinkedHashMap<String, Object>) roomMetaAux.getProperties());
		roomMetaAux = new RoomMetadata();
		roomMetaAux.addProperty(RoomMetadata.NAME, RoomMetadata.RoomName.LOUNGE.getName()); // living room
		roomMetaAux.addProperty(RoomMetadata.FLOOR, 0);
		roomMeta.add((LinkedHashMap<String, Object>) roomMetaAux.getProperties());
		roomMetaAux = new RoomMetadata();
		roomMetaAux.addProperty(RoomMetadata.NAME, RoomMetadata.RoomName.HALL.getName());
		roomMetaAux.addProperty(RoomMetadata.INSTANCE, 1);
		roomMetaAux.addProperty(RoomMetadata.FLOOR, 0);
		roomMeta.add((LinkedHashMap<String, Object>) roomMetaAux.getProperties());
		roomMetaAux = new RoomMetadata();
		roomMetaAux.addProperty(RoomMetadata.NAME, RoomMetadata.RoomName.GARAGE.getName());
		roomMetaAux.addProperty(RoomMetadata.FLOOR, 0);
		roomMeta.add((LinkedHashMap<String, Object>) roomMetaAux.getProperties());
		roomMetaAux = new RoomMetadata();
		roomMetaAux.addProperty(RoomMetadata.NAME, RoomMetadata.RoomName.OUTDOORS.getName()); // backyard
		roomMeta.add((LinkedHashMap<String, Object>) roomMetaAux.getProperties());
		// upstairs
		roomMetaAux = new RoomMetadata();
		roomMetaAux.addProperty(RoomMetadata.NAME, RoomMetadata.RoomName.BATHROOM.getName());
		roomMetaAux.addProperty(RoomMetadata.INSTANCE, 2);
		roomMetaAux.addProperty(RoomMetadata.FLOOR, 1);
		roomMeta.add((LinkedHashMap<String, Object>) roomMetaAux.getProperties());
		roomMetaAux = new RoomMetadata();
		roomMetaAux.addProperty(RoomMetadata.NAME, RoomMetadata.RoomName.BEDROOM.getName());
		roomMetaAux.addProperty(RoomMetadata.FLOOR, 1);
		roomMeta.add((LinkedHashMap<String, Object>) roomMetaAux.getProperties());
		roomMetaAux = new RoomMetadata();
		roomMetaAux.addProperty(RoomMetadata.NAME, RoomMetadata.RoomName.HALL.getName());
		roomMetaAux.addProperty(RoomMetadata.INSTANCE, 2);
		roomMetaAux.addProperty(RoomMetadata.FLOOR, 1);
		roomMeta.add((LinkedHashMap<String, Object>) roomMetaAux.getProperties());
		roomMetaAux = new RoomMetadata();
		roomMetaAux.addProperty(RoomMetadata.NAME, RoomMetadata.RoomName.STUDY.getName()); // office
		roomMetaAux.addProperty(RoomMetadata.FLOOR, 1);
		roomMeta.add((LinkedHashMap<String, Object>) roomMetaAux.getProperties());

		return roomMeta;
	}
	
	private static List<LinkedHashMap<String, Object>> getApplianceMetadata() {
		List<LinkedHashMap<String, Object>> appMeta = new ArrayList<LinkedHashMap<String, Object>>();
		ApplianceMetadata appMetaAux;
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 1);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 101);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Living Room Desk Lamp");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 30);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 26);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 2);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 102);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Living Room Tall Desk Lamp");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 30);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 25);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 3);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 103);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Garage Door");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 530);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 24);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 4);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 105);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Washing Machine");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, "130-700");
		//appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 0); // I need to understand this (should be 95)
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 5);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 107);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Kitchen Music");
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 6);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 108);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Kitchen Aid Chopper");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 1500);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 16);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 7);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 109);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Tea Kettle");
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 8);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 110);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Toaster Oven");
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 9);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 111);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Refrigerator");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 120);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 616);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 10);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 112);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "A/V Living Room");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 45);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 8);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 11);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 116);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Sub-woofer Living Room");
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 12);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 118);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Computer 1");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 60);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 45);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 13);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 120);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Laptop 1");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 40);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 14);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 14);
		//appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 109);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Dehumidifier");
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 15);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 122);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Vaccum Cleaner");
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 16);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 123);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Basement, Receiver/DVR/Blueray Player");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 55);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 34);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 17);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 124);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Sub-woofer Basement");
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 18);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 125);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Apple TV Basement");
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 19);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 127);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Air Compressor");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 1130);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 20);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 20);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 128);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "LCD Monitor 1");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 35);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 77);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 21);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 129);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "TV Basement");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 190);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 54);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 22);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 130);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Hard-drive");
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 23);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 131);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Printer");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 930);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 150);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 24);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 132);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Hair Dryer");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 1600);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 8);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 25);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 134);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "IRON");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 1400);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 40);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 26);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 135);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Empty Living Room Socket");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 60);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 2);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 27);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 136);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Empty Living Room Socket 2");
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 28);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 140);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Monitor 2");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 40);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 150);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 29);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 147);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Backyard Lights");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 60);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 16);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 30);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 148);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Washroom Light");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 110);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 6);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 31);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 149);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Office Lights");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 30);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 54);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 32);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 150);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Closet Lights");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 20);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 22);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 33);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 151);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Upstairs Hallway Light");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 25);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 17);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 34);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 152);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Hallway Stairs Light");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 110);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 58);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 35);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 153);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Kitchen Hallway Light");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 15);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 6);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 36);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 155);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Kitchen Overhead Light");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 65);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 56);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 37);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 156);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Bathroom Upstairs Light");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 65);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 98);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 38);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 157);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Dining Room Overhead Light");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 65);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 32);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 39);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 158);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Bedroom Lights");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 190);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 19);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 40);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 159);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Basement Lights");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 35);
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 39);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 41);
		//appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 136);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Microwave");
		appMetaAux.addProperty(ApplianceMetadata.AVERAGE_POWER_CONSUMPTION, 1550);
		//appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 0); // should be 70
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 42);
		//appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 136);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Air Conditioner");
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 43);
		//appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 136);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Dryer");
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 44);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 204);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Circuit 4");
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 46);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 45);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 207);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Circuit 7");
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 38);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 46);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 209);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Circuit 9");
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 46);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 47);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 210);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Circuit 10");
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 99);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		appMetaAux = new ApplianceMetadata();
		appMetaAux.addProperty(ApplianceMetadata.INSTANCE, 48);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_INSTANCE, 211);
		appMetaAux.addProperty(ApplianceMetadata.ORIGINAL_NAME, "Circuit 11");
		appMetaAux.addProperty(ApplianceMetadata.NUMBER_OF_EVENTS, 394);
		appMeta.add((LinkedHashMap<String, Object>) appMetaAux.getProperties());
		
		return appMeta;
	}
	
	private static LinkedHashMap<Integer,LinkedHashMap<String, Object>> getElecMeterMetadata() {
		LinkedHashMap<Integer,LinkedHashMap<String, Object>> emMeta = new LinkedHashMap<Integer,LinkedHashMap<String, Object>>();
		ElecMeterMetadata emMetaAux;
		
		emMetaAux = new ElecMeterMetadata();
		emMetaAux.addProperty(ElecMeterMetadata.SITE_METER, true);
		emMetaAux.addProperty(ElecMeterMetadata.DEVICE_MODEL, "BLUED_whole_house");
		emMeta.put(1, (LinkedHashMap<String, Object>) emMetaAux.getProperties());
		
		// now we should add the 28 plug level meters
		emMetaAux = new ElecMeterMetadata();
		emMetaAux.addProperty(ElecMeterMetadata.DEVICE_MODEL, "firefly_plug_level");
		emMeta.put(2, (LinkedHashMap<String, Object>) emMetaAux.getProperties());
		
		emMetaAux = new ElecMeterMetadata();
		emMetaAux.addProperty(ElecMeterMetadata.DEVICE_MODEL, "firefly_plug_level");
		emMeta.put(3, (LinkedHashMap<String, Object>) emMetaAux.getProperties());
		
		// . . . 
		emMetaAux = new ElecMeterMetadata();
		emMetaAux.addProperty(ElecMeterMetadata.DEVICE_MODEL, "firefly_plug_level");
		emMeta.put(29, (LinkedHashMap<String, Object>) emMetaAux.getProperties());
		
		// now we sould add the 12 environmental meters
		emMetaAux = new ElecMeterMetadata();
		emMetaAux.addProperty(ElecMeterMetadata.DEVICE_MODEL, "firefly_environmental");
		emMeta.put(30, (LinkedHashMap<String, Object>) emMetaAux.getProperties());
		
		// . . .
		emMetaAux = new ElecMeterMetadata();
		emMetaAux.addProperty(ElecMeterMetadata.DEVICE_MODEL, "firefly_environmental");
		emMeta.put(41, (LinkedHashMap<String, Object>) emMetaAux.getProperties());
		
		// now we should add the X circuit level meters
		emMetaAux = new ElecMeterMetadata();
		emMetaAux.addProperty(ElecMeterMetadata.DEVICE_MODEL, "BLUED_circuit_level");
		emMeta.put(42, (LinkedHashMap<String, Object>) emMetaAux.getProperties());
		
		return emMeta;
	}
}
