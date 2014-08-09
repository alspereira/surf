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

	public ArrayBlockingQueue<IPowerSample> samplesQueue;
	
	public PowerChart(int maxChartSamples) {
        this.chartSize = maxChartSamples;
        init();
	}
	
	private void init() {
		pTrace = new Trace2DLtd(chartSize);
    	qTrace = new Trace2DLtd(chartSize);

    	yAxis = new AxisLinear();
    	yAxis.getAxisTitle().setTitle("Power (W | VAR)");
    
    	pChart.setAxisYLeft(yAxis,1);

        AAxis xAxis = new AxisLinear();
        
        xAxis.setFormatter(new LabelFormatterDate(new SimpleDateFormat("dd-MM-y  HH:mm:ss")));
        
        pChart.setAxisXBottom(xAxis, 1);
        
		yAxis.setRangePolicy(new RangePolicyMinimumViewport(new Range(-1000, 2000)));
		
		pChart.addTrace(pTrace, xAxis, yAxis);
		pChart.addTrace(qTrace, xAxis, yAxis);
		
		qTrace.setColor(Color.RED);
		pTrace.setColor(Color.blue);
	
		pTrace.setName("Real Power (W)");
		qTrace.setName("Reactive Power (VAR)");

		this.setSize(400,300);
		this.setVisible(true);
	}
	
	public ArrayBlockingQueue<IPowerSample> getPowerSamplesQueue() {
		if(this.samplesQueue == null)
			this.samplesQueue = new ArrayBlockingQueue<IPowerSample>(50000);
		
		return this.samplesQueue;
	}
	
	public void start() {
		Thread s = new Thread(this);
		s.start();
	}
	

	@Override
	public void run() {
		IPowerSample ps;
		while(true) {
			try {
				ps = samplesQueue.take();
				pTrace.addPoint(ps.getTimestamp(), ps.getRealPower());
				pTrace.addPoint(ps.getTimestamp(), ps.getReactivePower());			
			} catch (InterruptedException e1) {
					e1.printStackTrace();
			}
		}
	}
}
