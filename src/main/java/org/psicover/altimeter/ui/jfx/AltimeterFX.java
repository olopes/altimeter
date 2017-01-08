package org.psicover.altimeter.ui.jfx;

import java.io.File;

import org.psicover.altimeter.bean.AltimeterFile;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.io.AltimeterFileReader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class AltimeterFX extends Application {
	
	private TabPane tabPane;

	@Override
	public void start(Stage primaryStage) throws Exception {
		
        MenuBar menuBar = new MenuBar();
        
        Menu menuFile = new Menu("_File");
        
        menuBar.getMenus().add(menuFile);
        
        MenuItem open = new MenuItem("_Open");
        open.acceleratorProperty().setValue(KeyCombination.keyCombination("SHORTCUT+O"));
        open.setOnAction(e -> doOpen(primaryStage));
        MenuItem exportData = new MenuItem("Export _Data");
        exportData.acceleratorProperty().setValue(KeyCombination.keyCombination("SHORTCUT+D"));
        exportData.setOnAction(e -> doExportData(primaryStage));
        MenuItem exportChart = new MenuItem("_Export Chart");
        exportChart.acceleratorProperty().setValue(KeyCombination.keyCombination("SHORTCUT+E"));
        exportChart.setOnAction(e -> doExportChart(primaryStage));
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
		} catch (Exception e) {
			e.printStackTrace();
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

	public static void main(String[] args) {
		launch(args);
	}

}
