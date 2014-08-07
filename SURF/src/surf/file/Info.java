package surf.file;

public class Info {
	/**
	 *  IARL: archival location. where the file is originally stored. value class = String
	 */
	public String archival_location = "";		// 'IARL' archival location
	/**
	 *  IART: file creator. who created this file. value class = String
	 */
	public String file_creator = "";			// 'IART' artist -> file creator
	public String commissioner = "";			// 'ICMS' commissioner
	public String comments = "";				// 'ICMT' comments
	public String copyright = "";				// 'ICOP' copyright
	public String creation_date = "";			// 'ICRD' creation date
	public String keywords = "";				// 'IKEY' keywords
	public String name = "";					// 'INAM' subject
	public String product = ""; 				// 'IPRD' product -> original propose of the file
	public String subject = "";					// 'ISBJ' subject -> contents of the file
	public String software = "";				// 'ISFT' software -> software package used to create the file
	public String source = "";					// 'ISRC' source -> original (person / organization) source of the file
	public String source_form = "";				// 'ISRF' source form -> original form of material (.ZIP / .TXT / etc)
} 