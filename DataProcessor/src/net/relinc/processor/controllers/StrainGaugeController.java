package net.relinc.processor.controllers;

import java.io.File;
import java.util.List;

import org.controlsfx.control.SegmentedButton;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.relinc.libraries.application.Bar;
import net.relinc.libraries.application.StrainGauge;
import net.relinc.libraries.application.StrainGaugeOnBar;
import net.relinc.libraries.fxControls.NumberTextField;
import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

public class StrainGaugeController {
	
	private File currentWorkingDirectory = SPSettings.Workspace;
	
	@FXML TreeView<String> treeView;
	@FXML TextField folderNameTF;
	@FXML TextField strainGaugeNameTF;
	NumberTextField resistanceTF;
	NumberTextField lengthTF;
	NumberTextField voltageCalibratedTF;
	NumberTextField gaugeFactorTF;
	NumberTextField shuntResistanceTF; 
	NumberTextField distanceToSampleTF;
	@FXML TextField specificNameTF;
	@FXML Button addStrainGaugeButton;
	@FXML Button addFolderButton;
	@FXML Button saveStrainGaugeButton;
	@FXML Button doneButton;
	
	@FXML GridPane strainGaugeGrid;
	@FXML VBox rightVBox;
	public Stage stage;
	
	private TreeItem<String> selectedTreeItem;
	public boolean incidentBarMode;
	public boolean transmissionBarMode;
	public Bar bar;
	
	@FXML
	public void initialize() {
		
		SegmentedButton b = new SegmentedButton();
		ToggleButton b1 = new ToggleButton("Workspace");
		ToggleButton b2 = new ToggleButton("Global");
		b.getButtons().addAll(b1,b2);
		b.getButtons().get(0).setSelected(true);
		rightVBox.getChildren().add(1, b);
		
		b1.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				if(b1.isSelected()) {
					currentWorkingDirectory = SPSettings.Workspace;
					updateTreeView();
				}
			}
		});
		
		b2.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				if(b2.isSelected()) {
					currentWorkingDirectory = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse");
					updateTreeView();
				}
			}
		});
		
		
		
		updateTreeView();
		treeView.getSelectionModel().selectedItemProperty()
	    .addListener(new ChangeListener<TreeItem<String>>() {

	        @Override
	        public void changed(
	                ObservableValue<? extends TreeItem<String>> observable,
	                TreeItem<String> old_val, TreeItem<String> new_val) {
	            TreeItem<String> selectedItem = new_val;
	            selectedTreeItem = selectedItem;
	            selectedItemChanged();
	        }

			

	    });
		
		doneButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Stage s = (Stage) doneButton.getScene().getWindow();
			    s.close();
			}
		});
		
		initializeDynamicTextFields();
	}
	
	private void initializeDynamicTextFields() {
		resistanceTF = new NumberTextField("Ohm", "Ohm");
		lengthTF = new NumberTextField("in", "mm");
		voltageCalibratedTF = new NumberTextField("v", "v");
		gaugeFactorTF = new NumberTextField("","");
		shuntResistanceTF = new NumberTextField("kOhm", "kOhm");
		distanceToSampleTF = new NumberTextField("in", "mm");
		if(SPSettings.metricMode.get())
			distanceToSampleTF.setPromptText("Distance to Sample (mm)");
		else
			distanceToSampleTF.setPromptText("Distance to Sample (in)");
		
		int j = 1;
		strainGaugeGrid.add(resistanceTF, 1, j++);
		strainGaugeGrid.add(resistanceTF.unitLabel, 1, j-1);
		strainGaugeGrid.add(lengthTF, 1, j++);
		strainGaugeGrid.add(lengthTF.unitLabel, 1, j-1);
		strainGaugeGrid.add(voltageCalibratedTF, 1, j++);
		strainGaugeGrid.add(voltageCalibratedTF.unitLabel, 1, j-1);
		strainGaugeGrid.add(gaugeFactorTF, 1, j++);
		strainGaugeGrid.add(gaugeFactorTF.unitLabel, 1, j-1);
		strainGaugeGrid.add(shuntResistanceTF, 1, j++);
		strainGaugeGrid.add(shuntResistanceTF.unitLabel, 1, j-1);
		
		rightVBox.getChildren().add(3,distanceToSampleTF);
	}
	
	public void addStrainGaugeFired(){
		try{
			Double.parseDouble(distanceToSampleTF.getText());
		}
		catch(Exception w){
			Dialogs.showAlert("Please enter the distance to sample",stage);
			return;
		}
		String path = getPathFromTreeViewItem(selectedTreeItem);
		File file = new File(currentWorkingDirectory.getPath() + "/" + path);
		if(file.isDirectory()){
			Dialogs.showAlert("Please select a strain gauge to add to the bar", stage);
			return;
		}
		//TODO: Check name validity. Cannot have repeat names on a bar. Either bar? pry could
		double dist = Converter.MeterFromInch(Double.parseDouble(distanceToSampleTF.getText()));
		if(SPSettings.metricMode.get())
		{
			dist = Converter.mFromMm(Double.parseDouble(distanceToSampleTF.getText()));
		}

		if( new File(file.getPath() + ".txt").exists() ) {
			bar.strainGauges.add(new StrainGaugeOnBar(file.getPath() + ".txt", dist, specificNameTF.getText()));
		} else {
			bar.strainGauges.add(new StrainGaugeOnBar(file.getPath() + ".json", dist, specificNameTF.getText()));
		}
		
		Stage stage = (Stage) addStrainGaugeButton.getScene().getWindow();
	    stage.close();

	}

	public void saveStrainGaugeFired(){
		if(!SPOperations.specialCharactersAreNotInTextField(strainGaugeNameTF)) {
			Dialogs.showInformationDialog("Save Strain Gauge","Invalid Character In Strain Gauge Name", "Only 0-9, a-z, A-Z, dash, space, and parenthesis are allowed",stage);
			return;
		}
		if(!checkStrainGaugeParameters()){
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Invalid parameters");
			alert.setHeaderText("Invalid strain gauge parameters.");
			alert.showAndWait();
			return;
		}
		String path = getPathFromTreeViewItem(selectedTreeItem);
		if(path == ""){
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("No directory selected");
			alert.setHeaderText("Please select a directory to save into.");
			alert.showAndWait();
			return;
		}
		File file = new File(currentWorkingDirectory.getPath() + "/" + path);
		if(!file.isDirectory()){
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("No directory selected");
			alert.setHeaderText("Please select a directory to save into.");
			alert.showAndWait();
			return;
		}
		File newFile = new File(file.getPath() + "/" + strainGaugeNameTF.getText() + ".json");
		if(newFile.exists()){
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Strain Gauge Already Exists");
			alert.setHeaderText("Please rename the strain gauge.");
			alert.showAndWait();
			return;
		}
		String SGName = strainGaugeNameTF.getText();
		double resistance = resistanceTF.getDouble();
		double length = Converter.MeterFromInch(lengthTF.getDouble());
		if(SPSettings.metricMode.getValue())
			length = Converter.mFromMm(lengthTF.getDouble());
		double voltageCalibrated = voltageCalibratedTF.getDouble();
		double gaugeFactor = gaugeFactorTF.getDouble();
		double shuntResistance = shuntResistanceTF.getDouble() * 1000; //kΩ -> Ω
		
		StrainGauge SG = new StrainGauge(SGName, gaugeFactor, resistance, shuntResistance, length, voltageCalibrated);
		SPOperations.writeStringToFile(SG.stringForFile(), newFile.getPath());
		updateTreeView();
	}

	public void newFolderFired(){
		if(!SPOperations.specialCharactersAreNotInTextField(folderNameTF)) {
			Dialogs.showInformationDialog("Add Strain Gauge Folder","Invalid Character In Strain Gauge Folder Name", "Only 0-9, a-z, A-Z, dash, space, and parenthesis are allowed",stage);
			return;
		}
		String path = getPathFromTreeViewItem(selectedTreeItem);
		System.out.println(path);
		if(path == null || path.trim().equals("")) {
			Dialogs.showInformationDialog("Failed to Create Folder", null, "Please select a directory",stage);
		    return;
		}
		if(folderNameTF.getText().trim().equals("")){
			Dialogs.showInformationDialog("Failed to Create Folder", null, "Please enter a folder name",stage);
			return;
		}
		File file = new File(currentWorkingDirectory.getPath() + "/" + path);
		if(!file.isDirectory()){
			System.out.println("Must be in directory");
			Dialogs.showInformationDialog("Failed to Create Folder", null, "You selected a file, please select a directory",stage);
			return;
		}
		File newDir = new File(file.getPath() + "/" + folderNameTF.getText());
		if(newDir.exists()){
			System.out.println("Folder already exists");
			Dialogs.showInformationDialog("Failed to Create Folder", null, "Folder already exists",stage);
			return;
		}
		newDir.mkdir();
		updateTreeView();
	}
	
	@FXML
	private void deleteButtonFired(){
		String path = getPathFromTreeViewItem(selectedTreeItem);
		File file = new File(currentWorkingDirectory.getPath() + "/" + path);
		if(file.isDirectory()){
			if(file.getName().equals("Strain Gauges")){
				Dialogs.showAlert("Cannot delete base directory", stage);
				return;
			}
			int sampleCount = 0;
			int folderCount = 0;
			List<Integer> contents = SPOperations.countContentsInFolder(file);
			sampleCount = contents.get(0);
			folderCount = contents.get(1);
			String message = "";
			if(sampleCount > 0 && folderCount > 0)
				message = "It contains " + folderCount + " folder(s) and " + sampleCount + " strain gauges(s).";
			else if(sampleCount > 0)
				message = "It contains " + sampleCount + " strain gauges(s).";
			else if(folderCount > 0)
				message = "It contains " + folderCount + " folder(s).";
			
			if(Dialogs.showConfirmationDialog("Deleting Folder", "Confirm", 
					"Are you sure you want to delete this folder?\n" + message, stage)){
				SPOperations.deleteFolder(file);
			}
			else{
				return;
			}
		}
		else{
			if(Dialogs.showConfirmationDialog("Deleting Strain Gauge", "Confirm", "Are you sure you want to delete this strain gauge?", stage))
				new File(file.getPath() + ".json").delete();
				new File(file.getPath() + ".txt").delete();
		}
		updateTreeView();
	}
	
	private void selectedItemChanged() {
		String path = getPathFromTreeViewItem(selectedTreeItem);
		File file = new File(currentWorkingDirectory + "/" + path);
		if(file.isDirectory()){
			System.out.println("Directory cannot be strain gauge file.");
			return;
		}
		File strainGaugeFile = new File(file.getPath() + ".json");

		if(! strainGaugeFile.exists()) {
			// legacy files have .txt extension, try that
			strainGaugeFile = new File(file.getPath() + ".txt");
		}

		if(!strainGaugeFile.exists()){
			System.out.println("Strain Gauge doesn't exist");
			return;
		}
		StrainGauge SG = new StrainGauge(strainGaugeFile.getPath());
		
		strainGaugeNameTF.setText(SG.genericName);
		resistanceTF.setNumberText(Double.toString(SG.resistance));
		voltageCalibratedTF.setNumberText(Double.toString(SG.voltageCalibrated));
		lengthTF.setNumberText(Double.toString(Converter.InchFromMeter(SG.length)));
		if(SPSettings.metricMode.getValue())
			lengthTF.setNumberText(Double.toString(Converter.mmFromM(SG.length)));
		gaugeFactorTF.setNumberText(Double.toString(SG.gaugeFactor));
		shuntResistanceTF.setNumberText(Double.toString(SG.shuntResistance / 1000));//convert to kilo
		
	}
	
	public void refresh(){
		updateTreeView();
		if(incidentBarMode || transmissionBarMode){
			folderNameTF.setVisible(false);
			folderNameTF.setManaged(false);
			
			strainGaugeNameTF.setDisable(true);
			resistanceTF.setDisable(true);
			voltageCalibratedTF.setDisable(true);
			lengthTF.setDisable(true);
			gaugeFactorTF.setDisable(true);
			shuntResistanceTF.setDisable(true);
			
			saveStrainGaugeButton.setVisible(false);
			saveStrainGaugeButton.setManaged(false);
			addFolderButton.setVisible(false);
			addFolderButton.setManaged(false);
			doneButton.setVisible(false);
			doneButton.setManaged(false);
		}
		
		
		if(incidentBarMode){
			addStrainGaugeButton.setText("Add Strain Gauge To Incident Bar");
			specificNameTF.setText("Incident SG #" + (bar.strainGauges.size() + 1));
		}
		else if(transmissionBarMode){
			addStrainGaugeButton.setText("Add Strain Gauge To Transmission Bar");
			specificNameTF.setText("Transmission SG #" + (bar.strainGauges.size() + 1));
		}
		else{
			//edit mode
			addStrainGaugeButton.setVisible(false);
			addStrainGaugeButton.setManaged(false);
			distanceToSampleTF.setVisible(false);
			distanceToSampleTF.setManaged(false);
			specificNameTF.setVisible(false);
			specificNameTF.setManaged(false);
		}
	}
	
	private String getPathFromTreeViewItem(TreeItem<String> item) {
		if(item == null)
		{
			System.out.println("cannot get path from null tree object.");
			return "";
		}
		String path = item.getValue();
		while(item.getParent() != null){
			item = item.getParent();
			path = item.getValue() + "/" + path;
		}
		return path;
	}

	private boolean checkStrainGaugeParameters() {
		try {
			resistanceTF.getDouble(); //testing for exceptions
			lengthTF.getDouble();
			voltageCalibratedTF.getDouble();
			gaugeFactorTF.getDouble();
			shuntResistanceTF.getDouble();
		}
		catch(Exception e){
			return false;
		}
		if(strainGaugeNameTF.getText().equals(""))
			return false;
		return true;
	}
	
	private void updateTreeView(){
		File home = new File(currentWorkingDirectory.getPath() + "/Strain Gauges");
		SPOperations.findFiles(home, null, treeView, SPOperations.folderImageLocation, SPOperations.strainGaugeImageLocation);
	}
	
}
