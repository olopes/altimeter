package org.psicover.altimeter.ui.jfx;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.psicover.altimeter.bean.AltimeterFile;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.io.AltimeterFileReader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class AltimeterFX extends Application {
	
	private TabPane tabPane;
	private MenuItem exportData, exportChart;

	@Override
	public void start(Stage primaryStage) throws Exception {
		
        MenuBar menuBar = new MenuBar();
        
        Menu menuFile = new Menu("_File");
        
        menuBar.getMenus().add(menuFile);
        
        MenuItem open = new MenuItem("_Open");
        open.acceleratorProperty().setValue(KeyCombination.keyCombination("SHORTCUT+O"));
        open.setOnAction(e -> doOpen(primaryStage));
        exportData = new MenuItem("Export _Data");
        exportData.acceleratorProperty().setValue(KeyCombination.keyCombination("SHORTCUT+D"));
        exportData.setOnAction(e -> doExportData(primaryStage));
        exportData.setDisable(true);
        exportChart = new MenuItem("_Export Chart");
        exportChart.acceleratorProperty().setValue(KeyCombination.keyCombination("SHORTCUT+E"));
        exportChart.setOnAction(e -> doExportChart(primaryStage));
        exportChart.setDisable(true);
        MenuItem exit = new MenuItem("_Quit");
        exit.acceleratorProperty().setValue(KeyCombination.keyCombination("SHORTCUT+Q"));
        exit.setOnAction(e -> doExit());
        
        menuFile.getItems().addAll(open, new SeparatorMenuItem(), exportData, exportChart, new SeparatorMenuItem(), exit);
        
        tabPane = new TabPane();
        
        BorderPane root = new BorderPane(tabPane);
        root.setTop(menuBar);
        
        primaryStage.setScene(new Scene(root));
        primaryStage.setMaximized(true);
        primaryStage.setTitle("Altimeter Visualizer (JavaFX version)");
        primaryStage.show();
	}
	
	@Override
	public void stop() throws Exception {
		System.out.println("Graceful stop");
		super.stop();
	}
	
	private void doOpen(Stage stage) {
		System.out.println("Open sesame");
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Altimeter File");
		ExtensionFilter filter = new ExtensionFilter("Altimeter data (*.hka, *.fda)", "*.hka", "*.fda");
		fileChooser.getExtensionFilters().add(filter);
		fileChooser.setSelectedExtensionFilter(filter);
		
		File f = fileChooser.showOpenDialog(stage);
		if(f == null) return;

		try {
			AltimeterFile altFile = AltimeterFileReader.readFile(f);
			loadCharts(altFile);
			exportData.setDisable(false);
			exportChart.setDisable(false);
		} catch (Exception e) {
			showError("Error loading altimeter file: "+f.getName(), e);
		}
		
	}
	
	private void loadCharts(AltimeterFile altFile) {
		if(null == altFile) return;

		tabPane.getTabs().clear();
		AltimeterSession[] sessions = altFile.getSessions();
		for(int i = 0; i < sessions.length; i++) {
			AltimeterChartPane chartPane = new AltimeterChartPane(sessions[i], i+1);
			Tab tab = new Tab(chartPane.getTitle(), chartPane);
        	tabPane.getTabs().add(tab);
		}
		tabPane.getSelectionModel().select(0);

	}

	private void doExportData(Stage stage) {
		System.out.println("Export data");
	}
	
	private void doExportChart(Stage stage) {
		System.out.println("Export chart");
	}
	
	private void doExit() {
		System.out.println("Exit requested");
		Platform.exit();
	}
	
	private void showError(String msg, Exception ex) {
		// http://code.makery.ch/blog/javafx-dialogs-official/
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("An error has occurred");
		alert.setHeaderText(msg);
		alert.setContentText(ex.getMessage());

		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("The exception stacktrace was:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		// textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);

		alert.showAndWait();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
