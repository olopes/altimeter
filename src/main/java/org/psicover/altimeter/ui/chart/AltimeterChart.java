package org.psicover.altimeter.ui.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.DateRange;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.TextAnchor;
import org.psicover.altimeter.Preferences;
import org.psicover.altimeter.bean.AltimeterSample;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.bean.SampleRate;
import org.psicover.altimeter.ui.swing.AltimeterTimePeriod;

public class AltimeterChart extends JFreeChart {

	private static final long serialVersionUID = -2749476637301393526L;
	private final AltimeterSession session;
    private static final Color BG_GRAY = new Color(0xF4F4F4);
    private static final Color GRID_GRAY = new Color(0XDBDBDB);
    private static final Color GREEN = new Color(0x00F000);
    private static final Color BLUEVIOLET = new Color(0x8A2BE2);
    private static final Color ORANGE = new Color(0xFFA500);
    private static final Stroke DASH_LINE = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{3f}, 0f);
    private static final Stroke THIN_LINE = new BasicStroke(0.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Stroke BOLD_LINE = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	
	public static enum SeriesInfo {
		Altitude(0,0),
		SmoothedAltitude(0,1),
		Flight(0,2),
		Temperature(1,0),
		SmoothedTemperature(1,1);

		private final int rendererId, seriesId;
		private SeriesInfo(int rendererId, int seriesId) {
			this.rendererId=rendererId;
			this.seriesId=seriesId;
		}
		
		public int getRendererId() {
			return rendererId;
		}
		
		public int getSeriesId() {
			return seriesId;
		}
	}
	
	public AltimeterChart(AltimeterSession session, int sessionNum) {
		super(formatTitle(session, sessionNum), JFreeChart.DEFAULT_TITLE_FONT, createEmptyPlot(), true);
		this.session = session;
		loadChartData(sessionNum);
	}
	
	private static String formatTitle(AltimeterSession session, int sessionNum) {
		return "Session "+sessionNum+" - "+session.getSessionDuration();
	}
	
	private static XYPlot createEmptyPlot() {
        NumberAxis xAxis = new NumberAxis("Time (s)");
        xAxis.setAutoRangeIncludesZero(true);
        NumberAxis yAxis = new NumberAxis("Altitude (m)");
        yAxis.setAutoRangeIncludesZero(false);
        XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
        XYPlot plot = new XYPlot(new TimeSeriesCollection(), xAxis, yAxis, renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        return plot;
	}
	
	private static enum State {
		GROUND,FLIGHT
	}
	
	private void loadChartData(int sessionNum) {
		AltimeterSample [] samples = session.getData();
		SampleRate rate = session.getRate();
		// title = "Session "+sessionNum+" - "+session.getSessionDuration();
		
		TimeSeriesCollection dataset = (TimeSeriesCollection) getXYPlot().getDataset();
		TimeSeriesCollection tempDataSet = new TimeSeriesCollection();
		TimeSeries altSeries = new TimeSeries("Altitude (m)");
		TimeSeries altSSeries = new TimeSeries("Smoothed Altitude (m)");
		TimeSeries flightSeries = new TimeSeries("Flight");
		TimeSeries tempSeries = new TimeSeries("Temperature (C)");
		TimeSeries tempSSeries = new TimeSeries("Smoothed Temperature (C)");
		RegularTimePeriod tp=new AltimeterTimePeriod(rate);
		
		final Preferences prefs = Preferences.getInstance();
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
						XYTextAnnotation flightMark = new XYTextAnnotation("Duration: "+((end-start)/1000)+" s", start, initialLaunch);
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
		
		XYPlot plot = getXYPlot();
        plot.setBackgroundPaint(BG_GRAY);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(GRID_GRAY);
        plot.setDomainGridlineStroke(DASH_LINE);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(GRID_GRAY);
        plot.setRangeGridlineStroke(DASH_LINE);
        
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
        // renderer2.setPlotShapes(true);
        // renderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
        plot.setRenderer(1, renderer2);
				
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
        
		// setup dos botoes
        DateRange range = new DateRange(0,100000);
        domainAxis.setRange(range);
	}
	
	public boolean toggleSeriesVisible(SeriesInfo series) {
		return setSeriesVisible(series, !isSeriesVisible(series));
	}
	
	public boolean setSeriesVisible(SeriesInfo series, boolean visible) {
		XYItemRenderer renderer = getXYPlot().getRenderer(series.getRendererId());
		Boolean lastState=renderer.getSeriesVisible(series.getSeriesId());
		renderer.setSeriesVisible(series.getSeriesId(), visible, false);
		renderer.setSeriesVisibleInLegend(series.getSeriesId(), true, false);
		// meh... but it works...
		getXYPlot().rendererChanged(new RendererChangeEvent(renderer, true));
		return null==lastState?false:lastState.booleanValue();
	}
	
	public boolean isSeriesVisible(SeriesInfo series) {
		XYItemRenderer renderer = getXYPlot().getRenderer(series.getRendererId());
		Boolean lastState=renderer.getSeriesVisible(series.getSeriesId());
		return null==lastState?false:lastState.booleanValue();
	}
	
	public void setDomainRange(DateRange range) {
		getXYPlot().getDomainAxis().setRange(range);
	}
	
	public AltimeterSession getSession() {
		return session;
	}
}
