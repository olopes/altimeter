package org.psicover.altimeter.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.DecimalFormat;
import java.text.FieldPosition;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.DateRange;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.psicover.altimeter.bean.AltimeterSample;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.bean.SampleRate;

public class AltimeterChartPanel extends JPanel {

	private static final long serialVersionUID = -2749476637301393526L;
	private final AltimeterSession session;
	private String title;
    private XYPlot plot = null;
    private ChartPanel chartPanel;
	
	public AltimeterChartPanel(AltimeterSession session, int sessionNum) {
		this.session=session;
		initComponents(sessionNum);
	}
	
	private void initComponents(int sessionNum) {
		setLayout(new BorderLayout());
		AltimeterSample [] samples = session.getData();
		SampleRate rate = session.getRate();
		title = "Session "+sessionNum+" - "+session.getSessionDuration();
		
		
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeriesCollection tempDataSet = new TimeSeriesCollection();
		TimeSeries altSeries = new TimeSeries("Altitude (m)");
		TimeSeries altSSeries = new TimeSeries("Smoothed Altitude (m)");
		TimeSeries tempSeries = new TimeSeries("Temperature (C)");
		TimeSeries tempSSeries = new TimeSeries("Smoothed Temperature (C)");
		RegularTimePeriod tp=new AltimeterTimePeriod(rate);
		
		double [] smoothAlt = new double[samples.length];
		double [] smoothTem = new double[samples.length];
		for(int i = 0; i < samples.length; i++) {
			AltimeterSample sample = samples[i];
			double alt = sample.getAltitude();
			double tem = sample.getTemperature();
			
			// smooth curve
			int cnt = 1;
			double sumAlt = alt;
			double sumTem = tem;
			if(i > 0) {
				cnt++;
				sumAlt += smoothAlt[i-1];
				sumTem += smoothTem[i-1];
			} 
			if (i+1 < samples.length) {
				cnt++;
				AltimeterSample next = samples[i+1];
				sumAlt += next.getAltitude();
				sumTem += next.getTemperature();
			}
			
			smoothAlt[i] = sumAlt/cnt;
			smoothTem[i] = sumTem/cnt;
			
			altSeries.add(tp, alt);
			altSSeries.add(tp, smoothAlt[i]);
			tempSeries.add(tp, tem);
			tempSSeries.add(tp, smoothTem[i]);
			tp = tp.next();
		}
		
		dataset.addSeries(altSeries);
		dataset.addSeries(altSSeries);
		tempDataSet.addSeries(tempSeries);
		tempDataSet.addSeries(tempSSeries);
		
		JFreeChart chart = ChartFactory.createXYLineChart(title,
	            "Time (s)", "Altitude (m)", dataset);
		
        plot = chart.getXYPlot();
        final NumberAxis axis2 = new NumberAxis("Temperature (C)");
        axis2.setAutoRangeIncludesZero(true);
        plot.setRangeAxis(1, axis2);
        plot.setDataset(1, tempDataSet);
        plot.mapDatasetToRangeAxis(1, 1);
        ((NumberAxis)plot.getRangeAxis()).setAutoRangeIncludesZero(false);
        
        final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setNumberFormatOverride(new DecimalFormat("##0") {
			private static final long serialVersionUID = 9200880572231520816L;
			@Override
        	public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
        		return super.format(number/1000, result, fieldPosition);
        	}
        	@Override
        	public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
        		return super.format(number/1000L, result, fieldPosition);
        	}
        });
//        
//		DateAxis axis = (DateAxis) plot.getDomainAxis();
//		axis.setDateFormatOverride(new SimpleDateFormat(dateFormat));
        
		final XYItemRenderer renderer1 = plot.getRenderer();
        final StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
        renderer2.setSeriesPaint(0, Color.BLUE);
        // renderer2.setPlotShapes(true);
        // renderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
        plot.setRenderer(1, renderer2);
		
		// TODO adjust ticks, scales, zoom, etc...
        chartPanel = new ChartPanel(chart, true, false, false, true, true);
		JMenu toggleSeries = new JMenu("Toggle Series");
		JCheckBoxMenuItem toggAlt = new JCheckBoxMenuItem("Altitude", true);
		JCheckBoxMenuItem toggSAlt = new JCheckBoxMenuItem("Smoothed Altitude", true);
		JCheckBoxMenuItem toggTemp = new JCheckBoxMenuItem("Temperature", true);
		JCheckBoxMenuItem toggSTemp = new JCheckBoxMenuItem("Smoothed Temperature", true);
		toggleSeries.add(toggAlt);
		toggAlt.addChangeListener(new ToggleSeriesChangeListener(renderer1, 0));
		toggleSeries.add(toggSAlt);
		toggSAlt.addChangeListener(new ToggleSeriesChangeListener(renderer1, 1));
		toggleSeries.add(toggTemp);
		toggTemp.addChangeListener(new ToggleSeriesChangeListener(renderer2,0));
		toggleSeries.add(toggSTemp);
		toggSTemp.addChangeListener(new ToggleSeriesChangeListener(renderer2,1));
		
		
		JPopupMenu contextMenu = chartPanel.getPopupMenu();
		contextMenu.addSeparator();
		contextMenu.add(toggleSeries);
		
		
		// TODO input to set zoomLevel
		final int stepSize = 100;
		
		// setup dos botoes
		final JSlider slider = new JSlider(0, (int)(tp.getFirstMillisecond()/1000L), 0);
		slider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
	            int value = slider.getValue();
	            DateRange range = new DateRange(value*1000,(value+stepSize)*1000);
	            domainAxis.setRange(range);
			}
		});
        DateRange range = new DateRange(0,stepSize*1000);
        domainAxis.setRange(range);

		add(chartPanel, BorderLayout.CENTER);
		add(slider, BorderLayout.SOUTH);
	}
	
	private class ToggleSeriesChangeListener implements ChangeListener {
		int index;
		XYItemRenderer renderer;
		
		ToggleSeriesChangeListener(XYItemRenderer rendered, int idx) {
			this.index=idx;
			this.renderer = rendered;
		}
		
		@Override
		public void stateChanged(ChangeEvent e) {
			JCheckBoxMenuItem cb = (JCheckBoxMenuItem)e.getSource();
			//set the renderer to hide the series
			renderer.setSeriesVisible(index, cb.isSelected(), false);
			renderer.setSeriesVisibleInLegend(index, true, false);
			// meh... but it works...
			plot.rendererChanged(new RendererChangeEvent(renderer, true));
		}
	}
	
	public AltimeterSession getSession() {
		return session;
	}
	
	public String getTitle() {
		return title;
	}
}
