package org.psicover.altimeter.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartPanel;
import org.jfree.data.time.DateRange;
import org.psicover.altimeter.Messages;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.ui.chart.AltimeterChart;
import org.psicover.altimeter.ui.chart.AltimeterChart.SeriesInfo;

public class AltimeterChartPanel extends JPanel {

	private static final long serialVersionUID = -2749476637301393526L;
    private final AltimeterChart chart;

	
	public AltimeterChartPanel(AltimeterSession session, int sessionNum) {
		this.chart=new AltimeterChart(session, sessionNum);
		setupUI();
	}
	
	private void setupUI() {
		setLayout(new BorderLayout());
		
		// TODO adjust ticks, scales, zoom, etc...
        ChartPanel chartPanel = new ChartPanel(chart, true, false, false, true, true);
		// JMenu toggleSeries = new JMenu("Toggle Series");
		JPopupMenu toggleSeries = new JPopupMenu(Messages.getString("AltimeterChartPanel.0")); //$NON-NLS-1$
		JCheckBoxMenuItem toggAlt = new JCheckBoxMenuItem(Messages.getString("AltimeterChartPanel.1"), true); //$NON-NLS-1$
		JCheckBoxMenuItem toggSAlt = new JCheckBoxMenuItem(Messages.getString("AltimeterChartPanel.2"), true); //$NON-NLS-1$
		JCheckBoxMenuItem toggFlight = new JCheckBoxMenuItem(Messages.getString("AltimeterChartPanel.3"), true); //$NON-NLS-1$
		JCheckBoxMenuItem toggTemp = new JCheckBoxMenuItem(Messages.getString("AltimeterChartPanel.4"), true); //$NON-NLS-1$
		JCheckBoxMenuItem toggSTemp = new JCheckBoxMenuItem(Messages.getString("AltimeterChartPanel.5"), true); //$NON-NLS-1$
		toggleSeries.add(toggAlt);
		toggAlt.setForeground(Color.BLACK);
		toggAlt.addChangeListener(new ToggleSeriesChangeListener(SeriesInfo.Altitude));
		toggleSeries.add(toggSAlt);
		toggSAlt.setForeground(Color.BLACK);
		toggSAlt.addChangeListener(new ToggleSeriesChangeListener(SeriesInfo.SmoothedAltitude));
		toggleSeries.add(toggFlight);
		toggFlight.setForeground(Color.BLACK);
		toggFlight.addChangeListener(new ToggleSeriesChangeListener(SeriesInfo.Flight));
		toggleSeries.add(toggTemp);
		toggTemp.setForeground(Color.BLACK);
		toggTemp.addChangeListener(new ToggleSeriesChangeListener(SeriesInfo.Temperature));
		toggleSeries.add(toggSTemp);
		toggSTemp.setForeground(Color.BLACK);
		toggSTemp.addChangeListener(new ToggleSeriesChangeListener(SeriesInfo.SmoothedTemperature));

		chartPanel.setPopupMenu(toggleSeries);
//		JPopupMenu contextMenu = chartPanel.getPopupMenu();
//		contextMenu.addSeparator();
//		contextMenu.add(toggleSeries);
		
		
		// TODO input to set zoomLevel
		final int stepSize = 100;
		
		// button setup
		final JSlider slider = new JSlider(0, (int)(chart.getSession().getDuration()+1), 0);
		slider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
	            int value = slider.getValue();
	            DateRange range = new DateRange(value*1000,(value+stepSize)*1000);
	            chart.setDomainRange(range);
			}
		});

		add(chartPanel, BorderLayout.CENTER);
		add(slider, BorderLayout.SOUTH);
	}
	
	private class ToggleSeriesChangeListener implements ChangeListener {
		SeriesInfo series;
		
		ToggleSeriesChangeListener(SeriesInfo series) {
			this.series = series;
		}
		
		@Override
		public void stateChanged(ChangeEvent e) {
			JCheckBoxMenuItem cb = (JCheckBoxMenuItem)e.getSource();
			chart.setSeriesVisible(series, cb.isSelected());
		}
	}
	
	public AltimeterSession getSession() {
		return chart.getSession();
	}
	
	public String getTitle() {
		return chart.getTitle().getText();
	}
}
