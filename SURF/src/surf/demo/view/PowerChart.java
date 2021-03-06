/**
 * 
 */
package surf.demo.view;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.concurrent.ArrayBlockingQueue;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.axis.AAxis;
import info.monitorenter.gui.chart.axis.AxisLinear;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterDate;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.util.Range;

import javax.swing.JFrame;
import javax.swing.JLabel;

import surf.demo.model.IPowerSample;



/**
 * @author lucaspereira
 *
 */
public class PowerChart extends JFrame implements Runnable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int chartSize;

	private Chart2D pChart = new Chart2D();

	private ITrace2D pTrace;
	private ITrace2D qTrace;
	
	AAxis yAxis;
	AAxis xAxis;
	
	public ArrayBlockingQueue<IPowerSample> samplesQueue;
	
	public PowerChart(int maxChartSamples) {
        this.chartSize = maxChartSamples;
        init();
	}
	
	public void setPowerSamplesQueue(ArrayBlockingQueue<IPowerSample> powerSamplesQueue) {
		this.samplesQueue = powerSamplesQueue;
	}
	
	private void init() {
		pTrace = new Trace2DLtd(chartSize);
    	qTrace = new Trace2DLtd(chartSize);

    	yAxis = new AxisLinear();
    	 xAxis = new AxisLinear();
    	//yAxis.getAxisTitle().setTitle("Power (W | VAR)");
    	pChart.setAxisYLeft(yAxis);
    	
    	xAxis.setFormatter(new LabelFormatterDate(new SimpleDateFormat("dd-MM-y HH:mm:ss")));
        //xAxis.getAxisTitle().setTitle("Time");
        pChart.setAxisXBottom(xAxis);
        
		yAxis.setRangePolicy(new RangePolicyMinimumViewport(new Range(-1000, 2000)));
		
		pChart.addTrace(pTrace, xAxis, yAxis);
		pChart.addTrace(qTrace, xAxis, yAxis);
		
		qTrace.setColor(Color.RED);
		pTrace.setColor(Color.blue);
	
		pTrace.setName("Real Power (W)");
		qTrace.setName("Reactive Power (VAR)");
		
		this.getContentPane().add(pChart);

		this.setSize(800,300);
		this.setVisible(true);
	}
	
	public ArrayBlockingQueue<IPowerSample> getPowerSamplesQueue() {
		if(this.samplesQueue == null)
			this.samplesQueue = new ArrayBlockingQueue<IPowerSample>(50000);
		
		return this.samplesQueue;
	}

	@Override
	public void run() {
		IPowerSample ps;
		while(true) {
			try {
				ps = samplesQueue.take();
				pTrace.addPoint(ps.getTimestamp(), ps.getRealPower());
				qTrace.addPoint(ps.getTimestamp(), ps.getReactivePower());	
			} catch (InterruptedException e1) {
					e1.printStackTrace();
			}
		}
	}
}
