package org.psicover.altimeter.ui.jfx;

import org.jfree.data.time.DateRange;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.ui.chart.AltimeterChart;
import org.psicover.altimeter.ui.chart.AltimeterChart.SeriesInfo;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class AltimeterChartPane extends BorderPane {

	private final AltimeterSession session;
	private final AltimeterChart chart;

	public AltimeterChartPane(AltimeterSession session, int sessionNum) {
		this.session = session;
		chart = new AltimeterChart(session, sessionNum);		
		setupUI();
	}

	private void setupUI() {
		final ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
        stackPane.getChildren().add(canvas);  
        // Bind canvas size to stack pane size. 
        canvas.widthProperty().bind( stackPane.widthProperty()); 
        canvas.heightProperty().bind( stackPane.heightProperty());

		// TODO input to set zoomLevel
		final int stepSize = 100;
		
		// button setup
		final Slider slider = new Slider(0, (chart.getSession().getDuration()+1), 0);
		slider.setBlockIncrement(1);
		slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
	            int value = new_val.intValue();
	            DateRange range = new DateRange(value*1000,(value+stepSize)*1000);
	            chart.setDomainRange(range);
            }
        });

		setCenter(stackPane);
		setBottom(slider);
		
		final ContextMenu contextMenu = new ContextMenu();
		CheckMenuItem altitudeItem = new CheckMenuItem("Altitude");
		altitudeItem.setSelected(true);
		altitudeItem.setOnAction(e -> chart.setSeriesVisible(SeriesInfo.Altitude, altitudeItem.isSelected()));
		CheckMenuItem smoothAltitudeItem = new CheckMenuItem("Smoothed Altitude");
		smoothAltitudeItem.setSelected(true);
		smoothAltitudeItem.setOnAction(e -> chart.setSeriesVisible(SeriesInfo.SmoothedAltitude, smoothAltitudeItem.isSelected()));
		CheckMenuItem flightItem = new CheckMenuItem("Flight");
		flightItem.setSelected(true);
		flightItem.setOnAction(e -> chart.setSeriesVisible(SeriesInfo.Flight, flightItem.isSelected()));
		CheckMenuItem temperatureItem = new CheckMenuItem("Temperature");
		temperatureItem.setSelected(true);
		temperatureItem.setOnAction(e -> chart.setSeriesVisible(SeriesInfo.Temperature, temperatureItem.isSelected()));
		CheckMenuItem smoothTemperatureItem = new CheckMenuItem("Smoothed Temperature");
		smoothTemperatureItem.setSelected(true);
		smoothTemperatureItem.setOnAction(e -> chart.setSeriesVisible(SeriesInfo.SmoothedTemperature, smoothTemperatureItem.isSelected()));

		contextMenu.getItems().addAll(altitudeItem, smoothAltitudeItem, flightItem, temperatureItem, smoothTemperatureItem);
		
		canvas.setOnContextMenuRequested(e -> contextMenu.show(canvas, e.getX(), e.getY()));
	}

	public String getTitle() {
		return chart.getTitle().getText();
	}
	
	public AltimeterSession getSession() {
		return this.session;
	}

}
