package org.psicover.altimeter.ui.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.text.FieldPosition;

import org.jfree.chart.JFreeChart;
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
import org.psicover.altimeter.IPreferences;
import org.psicover.altimeter.Messages;
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
		return Messages.getString("AltimeterChart.0", sessionNum, session.getSessionDuration()); //$NON-NLS-1$
	}
	
	private static XYPlot createEmptyPlot() {
        NumberAxis xAxis = new NumberAxis(Messages.getString("AltimeterChart.9")); //$NON-NLS-1$
        xAxis.setAutoRangeIncludesZero(true);
        NumberAxis yAxis = new NumberAxis(Messages.getString("AltimeterChart.10")); //$NON-NLS-1$
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
		
		TimeSeriesCollection altiDataset = (TimeSeriesCollection) getXYPlot().getDataset();
		TimeSeriesCollection tempDataset = new TimeSeriesCollection();
		TimeSeries altiRSeries = new TimeSeries(Messages.getString("AltimeterChart.2")); //$NON-NLS-1$
		TimeSeries altiSSeries = new TimeSeries(Messages.getString("AltimeterChart.3")); //$NON-NLS-1$
		TimeSeries flightSeries = new TimeSeries(Messages.getString("AltimeterChart.4")); //$NON-NLS-1$
		TimeSeries tempRSeries = new TimeSeries(Messages.getString("AltimeterChart.5")); //$NON-NLS-1$
		TimeSeries tempSSeries = new TimeSeries(Messages.getString("AltimeterChart.6")); //$NON-NLS-1$
		altiDataset.addSeries(altiRSeries);
		altiDataset.addSeries(altiSSeries);
		altiDataset.addSeries(flightSeries);
		tempDataset.addSeries(tempRSeries);
		tempDataset.addSeries(tempSSeries);
		
		RegularTimePeriod tp=new AltimeterTimePeriod(rate);
		// create raw 
		for(int i = 0; i < samples.length; i++) {
			final AltimeterSample sample = samples[i];
			final double alt = sample.getAltitude();
			final double tem = sample.getTemperature();
			altiRSeries.add(tp, alt, false);
			// altSSeries.add(tp, null, false);
			// flightSeries.add(tp, null, false);
			tempRSeries.add(tp, tem, false);
			// tempSSeries.add(tp, null, false);
			tp = tp.next();
		}
		
        processChartData(altiDataset, tempDataset);
		
		XYPlot plot = getXYPlot();
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
        plot.setDataset(1, tempDataset);
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
	
	protected void processChartData(final TimeSeriesCollection altiDataSet, final TimeSeriesCollection tempDataSet) {
		XYPlot plot = getXYPlot();
        
		TimeSeries altiRSeries = altiDataSet.getSeries(0);
		TimeSeries altiSSeries = altiDataSet.getSeries(1);
		TimeSeries flightSeries = altiDataSet.getSeries(2);
		TimeSeries tempRSeries = tempDataSet.getSeries(0);
		TimeSeries tempSSeries = tempDataSet.getSeries(1);
        
		final int dataLength = altiRSeries.getItemCount();
		final IPreferences prefs = Preferences.getInstance();
		final int smthWindow=prefs.getSmoothWindowSize();
		final int sStart = smthWindow, sEnd = dataLength-smthWindow-1;
		
		// first pass: smooth curves
		for(int i = sStart; i < sEnd; i++) {
			final RegularTimePeriod tp = altiRSeries.getTimePeriod(i);
			final double alti = altiRSeries.getValue(i).doubleValue();
			final double temp = tempRSeries.getValue(i).doubleValue();
			
			// smooth curve
			int cnt = (2*smthWindow+1);
			double sumAlti = alti;
			double sumTemp = temp;
			for(int j = 1; j <= smthWindow; j++) {
				sumAlti += (altiRSeries.getValue(i-j).doubleValue()+altiRSeries.getValue(i+j).doubleValue());
				sumTemp += (tempRSeries.getValue(i-j).doubleValue()+tempRSeries.getValue(i+j).doubleValue());
			}
			altiSSeries.add(tp, sumAlti/cnt, false);
			tempSSeries.add(tp, sumTemp/cnt, false);
		}
		
		final TimeSeries altitudes = "smooth".equals(prefs.getFlightDetectionDataset())?altiSSeries:altiRSeries; //$NON-NLS-1$
		// flight detection
		final double launchDelta = prefs.getLaunchDelta(); // 5 meters is launch
		final double landingDelta = prefs.getLandingDelta(); // 0.2 meters from initial launch height
		final int flightWindow = prefs.getFlightWindowSize()>0?prefs.getFlightWindowSize():getSession().getRate().samplesPerSecond(); // changes in a second
		final int fStart = flightWindow, fEnd = dataLength-flightWindow-1;
		State state = State.GROUND;
		Double initialLaunch = null;
		RegularTimePeriod launchTime = null;
		
		// second pass: flight detection
		for(int i = fStart; i < fEnd; i++) {
			final RegularTimePeriod tp = altiRSeries.getTimePeriod(i);
			final double alti = altitudes.getValue(i).doubleValue();
			// flight mode detection
			// TODO: smooth this? use smoothed data? look into the past?
			double sumAvg = 0.0;
			for(int j = 1; j <= flightWindow; j++)
				sumAvg += altitudes.getValue(i+j).doubleValue();
			sumAvg = sumAvg/flightWindow;

			if(state == State.GROUND) {
				// detect lauches
				if(sumAvg-alti > launchDelta) {
					state = State.FLIGHT;
					initialLaunch = alti;
					launchTime = altitudes.getTimePeriod(i);
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
					// final Marker flightMark = new IntervalMarker(start, end, Color.RED);
					// //currentEnd.setPaint(Color.red);
					// flightMark.setLabel("Duration: "+((end-start)/1000)+" s");
					// flightMark.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
					// flightMark.setLabelTextAnchor(TextAnchor.TOP_LEFT);
					plot.addAnnotation(flightMark);
					initialLaunch = null;
				}
			}
			flightSeries.add(tp, initialLaunch, false);
		}
		


	}
}
