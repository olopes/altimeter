package org.psicover.altimeter.ui.jfx;

import org.jfree.data.time.DateRange;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.ui.chart.AltimeterChart;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
	}

	public String getTitle() {
		return chart.getTitle().getText();
	}
	
	public AltimeterSession getSession() {
		return this.session;
	}

}
