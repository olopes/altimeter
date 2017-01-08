package org.psicover.altimeter.ui.jfx;

import java.io.File;

import org.psicover.altimeter.bean.AltimeterFile;
import org.psicover.altimeter.bean.AltimeterSession;
import org.psicover.altimeter.io.AltimeterFileReader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AltimeterFX extends Application {
	
	TabPane tabPane;

	@Override
	public void start(Stage primaryStage) throws Exception {
        MenuBar menuBar = new MenuBar();
        
        Menu menuFile = new Menu("_File");
        
        menuBar.getMenus().add(menuFile);
        
        MenuItem open = new MenuItem("_Open");
        open.acceleratorProperty().setValue(KeyCombination.keyCombination("SHORTCUT+O"));
        open.setOnAction(this::doOpen);
        MenuItem exportData = new MenuItem("Export _Data");
        exportData.acceleratorProperty().setValue(KeyCombination.keyCombination("SHORTCUT+D"));
        exportData.setOnAction(this::doExportData);
        MenuItem exportChart = new MenuItem("_Export Chart");
        exportChart.acceleratorProperty().setValue(KeyCombination.keyCombination("SHORTCUT+E"));
        exportChart.setOnAction(this::doExportChart);
        MenuItem exit = new MenuItem("_Quit");
        exit.acceleratorProperty().setValue(KeyCombination.keyCombination("SHORTCUT+Q"));
        exit.setOnAction(this::doExit);
        
        menuFile.getItems().addAll(open, new SeparatorMenuItem(), exportData, exportChart, new SeparatorMenuItem(), exit);
        
        TabPane tabPane = new TabPane();
        
        BorderPane root = new BorderPane(tabPane);
        root.setTop(menuBar);
        
        AltimeterFile ff = AltimeterFileReader.readFile(new File("vvv.fda"));
        AltimeterSession [] sessions = ff.getSessions();
        for(int i = 0; i < sessions.length; i++) {
        	AltimeterChartPane chart = new AltimeterChartPane(sessions[i], i+1);
        	Tab tab = new Tab("Session "+i, chart);
        	tabPane.getTabs().add(tab);
        }
        
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
	
	private void doOpen(ActionEvent evt) {
		System.out.println("Open sesame");
	}
	
	private void doExportData(ActionEvent evt) {
		System.out.println("Export data");
	}
	
	private void doExportChart(ActionEvent evt) {
		System.out.println("Export chart");
	}
	
	private void doExit(ActionEvent evt) {
		System.out.println("Exit requested");
		Platform.exit();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
