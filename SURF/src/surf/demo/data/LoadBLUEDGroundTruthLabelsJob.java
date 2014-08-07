package surf.demo.data;

import java.util.ArrayList;
import java.util.List;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteStatement;

public class LoadBLUEDGroundTruthLabelsJob<T> extends SQLiteJob<BLUEDGroundTruthLabelDTO[]> {
	
	private String query = "SELECT * FROM blued_labels";
	private BLUEDGroundTruthLabelDTO groundTruthLabel;
	
	public LoadBLUEDGroundTruthLabelsJob(String where_clause) {
		query = query + " WHERE " + where_clause;
	}
	
	public LoadBLUEDGroundTruthLabelsJob() {
	}

	@Override
	protected BLUEDGroundTruthLabelDTO[] job(SQLiteConnection connection) throws Throwable {
		List<BLUEDGroundTruthLabelDTO> labelsList = new ArrayList<BLUEDGroundTruthLabelDTO>();
		BLUEDGroundTruthLabelDTO[] result = new BLUEDGroundTruthLabelDTO[0];
		
		SQLiteStatement st = connection.prepare(query);
		while(st.step()) {
			groundTruthLabel = new BLUEDGroundTruthLabelDTO();
			groundTruthLabel.id = st.columnInt(0);
			groundTruthLabel.appliance_id = st.columnInt(1);
			groundTruthLabel.appliance_label = st.columnString(2);
			groundTruthLabel.position = st.columnLong(3);
			groundTruthLabel.matlab_timestamp = st.columnDouble(4);
			groundTruthLabel.timestamp = st.columnString(5);
			groundTruthLabel.phase = st.columnString(6);
			groundTruthLabel.delta_P = (float) st.columnDouble(7);
			groundTruthLabel.delta_Q = (float) st.columnDouble(8);		
			
			labelsList.add(groundTruthLabel);			
		}
		return labelsList.toArray(result);
	}
}