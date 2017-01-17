package org.psicover.altimeter.ui.chart;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.List;

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
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.DateRange;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.TextAnchor;
import org.psicover.altimeter.IPreferences;
import org.psicover.altimeter.Preferences;
import org.psicover.altimeter.bean.AltimeterSample;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.bean.SampleRate;
import org.psicover.altimeter.Messages;
import org.psicover.altimeter.ui.swing.AltimeterTimePeriod;

public class AltimeterChartHelper extends JPanel {

	private static final long serialVersionUID = -2749476637301393526L;
	private final AltimeterSession session;
	private String title;
    private XYPlot plot = null;
    private ChartPanel chartPanel;
    private static final Color BG_GRAY = new Color(0xF4F4F4);
    private static final Color GRID_GRAY = new Color(0XDBDBDB);
    private static final Color GREEN = new Color(0x00F000);
    private static final Color BLUEVIOLET = new Color(0x8A2BE2);
    private static final Color ORANGE = new Color(0xFFA500);
    private static final Stroke DASH_LINE = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{3f}, 0f);
    private static final Stroke THIN_LINE = new BasicStroke(0.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Stroke BOLD_LINE = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	
	public AltimeterChartHelper(AltimeterSession session, int sessionNum) {
		this.session=session;
		setupUI(sessionNum);
	}
	
	private static enum State {
		GROUND,FLIGHT
	}
	
	private void setupUI(int sessionNum) {
		setLayout(new BorderLayout());
		AltimeterSample [] samples = session.getData();
		SampleRate rate = session.getRate();
		title = Messages.getString("AltimeterChart.0", sessionNum, session.getSessionDuration()); //$NON-NLS-1$
		
		
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeriesCollection tempDataSet = new TimeSeriesCollection();
		TimeSeries altSeries = new TimeSeries(Messages.getString("AltimeterChart.2")); //$NON-NLS-1$
		TimeSeries altSSeries = new TimeSeries(Messages.getString("AltimeterChart.3")); //$NON-NLS-1$
		TimeSeries flightSeries = new TimeSeries(Messages.getString("AltimeterChart.4")); //$NON-NLS-1$
		TimeSeries tempSeries = new TimeSeries(Messages.getString("AltimeterChart.5")); //$NON-NLS-1$
		TimeSeries tempSSeries = new TimeSeries(Messages.getString("AltimeterChart.6")); //$NON-NLS-1$
		RegularTimePeriod tp=new AltimeterTimePeriod(rate);
		
		final IPreferences prefs = Preferences.getInstance();
		final int smthWindow=prefs.getSmoothWindowSize();
		final int sStart = smthWindow-1, sEnd = samples.length-smthWindow;
		
		// flight detection
		final double launchDelta = prefs.getLaunchDelta(); // 5 meters is launch
		final double landingDelta = prefs.getLandingDelta(); // 0.2 meters from initial launch height
		final int flightWindow = prefs.getFlightWindowSize()>0?prefs.getFlightWindowSize():rate.samplesPerSecond(); // changes in a second
		final int fEnd = samples.length-flightWindow;
		State state = State.GROUND;
		Double initialLaunch = null;
		RegularTimePeriod launchTime = null;
		List<XYAnnotation> markers = new ArrayList<>();
		
		for(int i = 0; i < samples.length; i++) {
			final AltimeterSample sample = samples[i];
			final double alt = sample.getAltitude();
			final double tem = sample.getTemperature();
			altSeries.add(tp, alt);
			tempSeries.add(tp, tem);
			
			// smooth curve
			if(i > sStart && i < sEnd) {
				int cnt = (2*smthWindow+1);
				double sumAlt = alt;
				double sumTemp = sample.getTemperature();
				for(int j = 1; j <= smthWindow; j++) {
					sumAlt += (samples[i-j].getAltitude()+samples[i+j].getAltitude());
					sumTemp += (samples[i-j].getTemperature()+samples[i+j].getTemperature());
				}
				altSSeries.add(tp, sumAlt/cnt);
				tempSSeries.add(tp, sumTemp/cnt);
			}
			
			// flight mode detection
			if(i < fEnd) {
				// TODO: smooth this? use smoothed data? look into the past?
				double sumAvg = 0.0;
				for(int j = 1; j <= flightWindow; j++)
					sumAvg += samples[i+j].getAltitude();
				sumAvg = sumAvg/flightWindow;
				
				if(state == State.GROUND) {
					// detect lauches
					if(sumAvg-alt > launchDelta) {
						state = State.FLIGHT;
						initialLaunch = alt;
						launchTime = tp;
					}
				} else {
					// detect landings
					if(sumAvg-initialLaunch < landingDelta) {
						state = State.GROUND;
						// set label
						long start = launchTime.getFirstMillisecond();
						long end = tp.getFirstMillisecond();
						XYTextAnnotation flightMark = new XYTextAnnotation(Messages.getString("AltimeterChart.7", (end-start)/1000), start, initialLaunch); //$NON-NLS-1$
						flightMark.setPaint(ORANGE);
						flightMark.setTextAnchor(TextAnchor.TOP_LEFT);
//				        final Marker flightMark = new IntervalMarker(start, end, Color.RED);
//				        //currentEnd.setPaint(Color.red);
//				        flightMark.setLabel("Duration: "+((end-start)/1000)+" s");
//				        flightMark.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
//				        flightMark.setLabelTextAnchor(TextAnchor.TOP_LEFT);
				        markers.add(flightMark);
						initialLaunch = null;
					}
				}
				flightSeries.add(tp, initialLaunch);
			}
			
			tp = tp.next();
		}
		
		dataset.addSeries(altSeries);
		dataset.addSeries(altSSeries);
		dataset.addSeries(flightSeries);
		tempDataSet.addSeries(tempSeries);
		tempDataSet.addSeries(tempSSeries);
		
		JFreeChart chart = ChartFactory.createXYLineChart(title,
	            Messages.getString("AltimeterChart.9"), Messages.getString("AltimeterChart.10"), dataset); //$NON-NLS-1$ //$NON-NLS-2$
		
        plot = chart.getXYPlot();
        plot.setBackgroundPaint(BG_GRAY);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(GRID_GRAY);
        plot.setDomainGridlineStroke(DASH_LINE);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(GRID_GRAY);
        plot.setRangeGridlineStroke(DASH_LINE);
        
        final NumberAxis axis2 = new NumberAxis(Messages.getString("AltimeterChart.11")); //$NON-NLS-1$
        axis2.setAutoRangeIncludesZero(true);
        plot.setRangeAxis(1, axis2);
        plot.setDataset(1, tempDataSet);
        plot.mapDatasetToRangeAxis(1, 1);
        ((NumberAxis)plot.getRangeAxis()).setAutoRangeIncludesZero(false);
        
        final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setNumberFormatOverride(new DecimalFormat("##0") { //$NON-NLS-1$
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
        // renderer2.setPlotShapes(true);
        // renderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
        plot.setRenderer(1, renderer2);
		
		// TODO adjust ticks, scales, zoom, etc...
        chartPanel = new ChartPanel(chart, true, false, false, true, true);
		JMenu toggleSeries = new JMenu(Messages.getString("AltimeterChart.13")); //$NON-NLS-1$
		JCheckBoxMenuItem toggAlt = new JCheckBoxMenuItem(Messages.getString("AltimeterChart.14"), true); //$NON-NLS-1$
		JCheckBoxMenuItem toggSAlt = new JCheckBoxMenuItem(Messages.getString("AltimeterChart.15"), true); //$NON-NLS-1$
		JCheckBoxMenuItem toggFlight = new JCheckBoxMenuItem(Messages.getString("AltimeterChart.16"), true); //$NON-NLS-1$
		JCheckBoxMenuItem toggTemp = new JCheckBoxMenuItem(Messages.getString("AltimeterChart.17"), true); //$NON-NLS-1$
		JCheckBoxMenuItem toggSTemp = new JCheckBoxMenuItem(Messages.getString("AltimeterChart.18"), true); //$NON-NLS-1$
		toggleSeries.add(toggAlt);
		toggAlt.addChangeListener(new ToggleSeriesChangeListener(renderer1, 0));
		toggleSeries.add(toggSAlt);
		toggSAlt.addChangeListener(new ToggleSeriesChangeListener(renderer1, 1));
		toggleSeries.add(toggFlight);
		toggFlight.addChangeListener(new ToggleSeriesChangeListener(renderer1, 2));
		toggleSeries.add(toggTemp);
		toggTemp.addChangeListener(new ToggleSeriesChangeListener(renderer2,0));
		toggleSeries.add(toggSTemp);
		toggSTemp.addChangeListener(new ToggleSeriesChangeListener(renderer2,1));
		
        renderer1.setSeriesPaint(0, GREEN);
        renderer1.setSeriesStroke(0,  THIN_LINE);
        renderer1.setSeriesPaint(1, GREEN);
        renderer1.setSeriesStroke(1,  BOLD_LINE);
        renderer1.setSeriesPaint(2, ORANGE);
        renderer1.setSeriesStroke(2,  BOLD_LINE);
        renderer2.setSeriesPaint(0, BLUEVIOLET);
        renderer2.setSeriesStroke(0,  THIN_LINE);
        renderer2.setSeriesPaint(1, BLUEVIOLET);
        renderer2.setSeriesStroke(1,  BOLD_LINE);

        for(XYAnnotation flightMark : markers)
        	plot.addAnnotation(flightMark);

        
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
