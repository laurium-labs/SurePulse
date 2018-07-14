package net.relinc.processor.controllers;


import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.relinc.libraries.application.BarSetup;
import net.relinc.libraries.data.DataFileListWrapper;
import net.relinc.libraries.data.DataInterpreter;
import net.relinc.libraries.data.DataModel;
import net.relinc.libraries.data.RawDataset;
import net.relinc.libraries.data.DataInterpreter.dataType;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.processor.controllers.BarCalibratorController.CalibrationMode;
import net.relinc.processor.controllers.SelectStrainGaugeController.Mode;

public class CategorizeDataController {
	@FXML LineChart<Number, Number> chart;
	@FXML RadioButton forceRadio;
	@FXML RadioButton engineeringStrainRadio;
	@FXML RadioButton trueStrainRadio;
	@FXML RadioButton lagrangianStrainRadio;
	@FXML RadioButton timeRadio;
	@FXML RadioButton incidentStrainGaugeRadio;
	@FXML RadioButton transmissionStrainGaugeRadio;
	@FXML RadioButton displacementRadioButton;
	@FXML RadioButton incidentBarStrainRadioButton;
	@FXML RadioButton transmissionBarStrainRadioButton;
	@FXML Label standardUnitsInstructionsLabel;
	@FXML TextField dataNameTF;
	@FXML Label nameLabel;
	@FXML TextField multiplierTF;
	XYChart.Series<Number, Number> currentSeries;
	@FXML HBox quickOptionsHbox;
	
	@FXML TableView<List<String>> tableView;
	@FXML AnchorPane tableColumnAnchorPane;
	
	RadioButton newtons = new RadioButton("N");
	RadioButton lbf = new RadioButton("lbf");
	
	Button s = new Button("s");
	Button ms = new Button("ms");
	Button us = new Button("\u03BCs");
	Button ns = new Button("ns");
	
	Button v = new Button("V");
	Button mv = new Button("mV");
	Button inchesButton = new Button("Inch");
	Button mmButton = new Button("mm");
	
	final ToggleGroup group = new ToggleGroup();
	final ToggleGroup forceUnits = new ToggleGroup();
	TableColumn<List<String>, String> tableColumn;
	
	
	RawDataset rawDataSet;
	DataModel model;
	//RawDataset timeDataSet;
	//public Stage stage;
	public BarSetup barSetup;
	//public Sample sample;
	//public DataSubsetListWrapper existingSampleData;
	public DataFileListWrapper existingSampleDataFiles;
	public boolean loadDisplacement;
	public CalibrationMode calibrationMode;
	@FXML
	public void initialize(){
		forceRadio.setToggleGroup(group);
		engineeringStrainRadio.setToggleGroup(group);
		trueStrainRadio.setToggleGroup(group);
		lagrangianStrainRadio.setToggleGroup(group);
		timeRadio.setToggleGroup(group);
		incidentStrainGaugeRadio.setToggleGroup(group);
		transmissionStrainGaugeRadio.setToggleGroup(group);
		displacementRadioButton.setToggleGroup(group);
		incidentBarStrainRadioButton.setToggleGroup(group);
		transmissionBarStrainRadioButton.setToggleGroup(group);
		newtons.setToggleGroup(forceUnits);
		lbf.setToggleGroup(forceUnits);
		
		//loadCellRadio.setToggleGroup(group);
		updateControls();
		quickOptionsHbox.setSpacing(15.0);
		quickOptionsHbox.setAlignment(Pos.CENTER);
		group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() 
		{
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				updateStandardUnitsDescriptionLabel();
				if(group.getSelectedToggle() == incidentStrainGaugeRadio){
					openSelectStrainGaugeDialog(true);
				}
				else if(group.getSelectedToggle() == transmissionStrainGaugeRadio){
					openSelectStrainGaugeDialog(false);
				}
				else if(group.getSelectedToggle() == incidentBarStrainRadioButton){
					openSelectStrainGaugeDialog(true);
				}
				else if(group.getSelectedToggle() == transmissionBarStrainRadioButton){
					openSelectStrainGaugeDialog(false);
				}
				
				if(currentSeries != null)
					currentSeries.setName(((RadioButton)group.getSelectedToggle()).getText());
				
				updateNameSuggestion();
			}
		});
		
		s.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				multiplierTF.setText("1");
			}
		});
		ms.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				multiplierTF.setText("1000");
			}
		});
		us.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				multiplierTF.setText("1000000");
			}
		});
		ns.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				multiplierTF.setText("1000000000");
			}
		});
		v.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				multiplierTF.setText("1");
			}
		});
		mv.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				multiplierTF.setText("1000");
			}
		});
		inchesButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				multiplierTF.setText("39.3701");
			}
			
		});
		mmButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				multiplierTF.setText("1000");
			}
			
		});
		
		newtons.selectedProperty().set(true);

	}


	public void doneButtonFired(){
		if(forceRadio.isSelected()){
			rawDataSet.interpreter.DataType = dataType.FORCE;
		}
		else if(engineeringStrainRadio.isSelected()){
			rawDataSet.interpreter.DataType = dataType.ENGINEERINGSTRAIN;
		}
		else if(trueStrainRadio.isSelected()){
			rawDataSet.interpreter.DataType = dataType.TRUESTRAIN;
		}
		else if(lagrangianStrainRadio.isSelected()){
			rawDataSet.interpreter.DataType = dataType.LAGRANGIANSTRAIN;
		}
		else if(timeRadio.isSelected()){
			rawDataSet.interpreter.DataType = dataType.TIME;		
		}
		else if(displacementRadioButton.isSelected()){
			rawDataSet.interpreter.DataType = dataType.DISPLACEMENT;
		}
		else if (incidentStrainGaugeRadio.isSelected()) {
			if (rawDataSet.interpreter.strainGauge != null) {
					rawDataSet.interpreter.DataType = dataType.INCIDENTSG;
			}
		} else if (transmissionStrainGaugeRadio.isSelected()) {
			if (rawDataSet.interpreter.strainGauge != null) {
					rawDataSet.interpreter.DataType = dataType.TRANSMISSIONSG;
			}
		}
		else if(incidentBarStrainRadioButton.isSelected()){
			if(rawDataSet.interpreter.strainGauge != null)
				rawDataSet.interpreter.DataType = dataType.INCIDENTBARSTRAIN;
		}
		else if(transmissionBarStrainRadioButton.isSelected()){
			if(rawDataSet.interpreter.strainGauge != null)
				rawDataSet.interpreter.DataType = dataType.TRANSMISSIONBARSTRAIN;
		}
		
		else{
			System.out.println("Unimplemented data type selected");
		}
		rawDataSet.interpreter.name = dataNameTF.getText(); //TODO: Check name validity
		double multiplierTemp = Double.parseDouble(multiplierTF.getText());
		
		//Account For units. Only force gets inverted. This is inconsistent.
		if(forceRadio.isSelected() && newtons.selectedProperty().get()){
			multiplierTemp= 1 / multiplierTemp * 1;
		}
		else if(forceRadio.isSelected()){
			multiplierTemp = 1 / (multiplierTemp * 4.44822);
		}
		
//		if(displacementRadioButton.isSelected())
//			multiplierTemp = multiplierTemp; //changed the autofill to 39.3701 instead of .0254.
		
		rawDataSet.interpreter.multiplier = multiplierTemp ;
		
		
		Stage stage = (Stage) forceRadio.getScene().getWindow();
	    // do what you have to do
	    stage.close();
	    //stage.close();
	}
	

	public void openSelectStrainGaugeDialog(boolean incident){
		Stage anotherStage = new Stage();
		try {
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/SelectStrainGauge.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
			Scene scene = new Scene(root1.load());
			//scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
			anotherStage.setScene(scene);
			anotherStage.getIcons().add(SPSettings.getRELLogo());
			anotherStage.setTitle("SURE-Pulse Data Processor");
			SelectStrainGaugeController c = root1.<SelectStrainGaugeController>getController();
			c.stage = anotherStage;
			
			
			if(incident){
				c.mode = Mode.INCIDENT;
				c.bar = barSetup.IncidentBar;
			}
			else {
				c.mode = Mode.TRANSMISSION;
				c.bar = barSetup.TransmissionBar;
			}
			c.rawDataset = rawDataSet;
			
			c.updateInterface();
			
			anotherStage.show();
			c.updateInterface();
		}
		catch(Exception e){
			
		}
	}

	
	
	public void updateControls() {
		tableView.getColumns().clear();
		if(tableColumn != null)
			tableView.getColumns().add(tableColumn);
		if (model != null) {
			if (model.hasTimeData()) {
				timeRadio.setDisable(true);
				
				if(barSetup == null || barSetup.IncidentBar == null || barSetup.IncidentBar.strainGauges.size() == 0){
					incidentStrainGaugeRadio.setText("Incident Strain Gauge Voltage (must select bar setup)");
					incidentBarStrainRadioButton.setText("Incident Bar Strain (must select bar setup)");
					incidentStrainGaugeRadio.setDisable(true);
					incidentBarStrainRadioButton.setDisable(true);
				}
				else{
					incidentStrainGaugeRadio.setText("Incident Strain Gauge Voltage");
					incidentBarStrainRadioButton.setText("Incident Bar Strain");
					incidentStrainGaugeRadio.setDisable(false);
					incidentBarStrainRadioButton.setDisable(false);
				}
				
				if(barSetup == null || barSetup.TransmissionBar == null || barSetup.TransmissionBar.strainGauges.size() == 0){
					transmissionStrainGaugeRadio.setText("Transmission Strain Gauge Voltage (must select bar setup)");
					transmissionBarStrainRadioButton.setText("Transmission Bar Strain (must select bar setup");
					transmissionStrainGaugeRadio.setDisable(true);
					transmissionBarStrainRadioButton.setDisable(true);
				}
				else{
					transmissionStrainGaugeRadio.setText("Transmission Strain Gauge Voltage");
					transmissionBarStrainRadioButton.setText("Transmission Bar Strain");
					transmissionStrainGaugeRadio.setDisable(false);
					transmissionBarStrainRadioButton.setDisable(false);
				}
				
				
				
				
				
			} else {
				// no time selected, must select time or enter collection rate
				timeRadio.setDisable(false);

				forceRadio.setDisable(true);
				trueStrainRadio.setDisable(true);
				engineeringStrainRadio.setDisable(true);
				lagrangianStrainRadio.setDisable(true);
				incidentStrainGaugeRadio.setDisable(true);
				transmissionStrainGaugeRadio.setDisable(true);
				displacementRadioButton.setDisable(true);
				incidentBarStrainRadioButton.setDisable(true);
				transmissionBarStrainRadioButton.setDisable(true);
				//loadCellRadio.setDisable(true);
				
				nameLabel.setDisable(true);
				dataNameTF.setDisable(true);

			}
			
			if(calibrationMode != null){
				if(calibrationMode == CalibrationMode.INCIDENT){
					forceRadio.setVisible(false);
					forceRadio.setManaged(false);
					trueStrainRadio.setVisible(false);
					trueStrainRadio.setManaged(false);
					engineeringStrainRadio.setVisible(false);
					engineeringStrainRadio.setManaged(false);
					displacementRadioButton.setVisible(false);
					displacementRadioButton.setManaged(false);
					transmissionStrainGaugeRadio.setVisible(false);
					transmissionStrainGaugeRadio.setManaged(false);
					incidentBarStrainRadioButton.setVisible(false);
					incidentBarStrainRadioButton.setManaged(false);
					transmissionBarStrainRadioButton.setVisible(false);
					transmissionBarStrainRadioButton.setManaged(false);
					lagrangianStrainRadio.setVisible(false);
					lagrangianStrainRadio.setManaged(false);
				}
				else if(calibrationMode == CalibrationMode.TRANSMISSION){
					forceRadio.setVisible(false);
					forceRadio.setManaged(false);
					trueStrainRadio.setVisible(false);
					trueStrainRadio.setManaged(false);
					engineeringStrainRadio.setVisible(false);
					engineeringStrainRadio.setManaged(false);
					displacementRadioButton.setVisible(false);
					displacementRadioButton.setManaged(false);
					incidentStrainGaugeRadio.setVisible(false);
					incidentStrainGaugeRadio.setManaged(false);
					incidentBarStrainRadioButton.setVisible(false);
					incidentBarStrainRadioButton.setManaged(false);
					transmissionBarStrainRadioButton.setVisible(false);
					transmissionBarStrainRadioButton.setManaged(false);
					lagrangianStrainRadio.setVisible(false);
					lagrangianStrainRadio.setManaged(false);
				}
				else{
					System.out.println("Calibration mode not implemented.");
				}
			}
		}
		
		if(loadDisplacement){
			trueStrainRadio.setDisable(true);
			engineeringStrainRadio.setDisable(true);
			incidentStrainGaugeRadio.setDisable(true);
			transmissionStrainGaugeRadio.setDisable(true);
		}
		
		
		if(rawDataSet == null)
			return;
		if(rawDataSet.interpreter.DataType != null){
			//set the toggled Radio
			if(rawDataSet.interpreter.DataType == dataType.TIME)
				timeRadio.setSelected(true);
			else if(rawDataSet.interpreter.DataType == dataType.FORCE)
				forceRadio.setSelected(true);
			else if(rawDataSet.interpreter.DataType == dataType.ENGINEERINGSTRAIN)
				engineeringStrainRadio.setSelected(true);
			else if(rawDataSet.interpreter.DataType == dataType.TRUESTRAIN)
				trueStrainRadio.setSelected(true);
			else if(rawDataSet.interpreter.DataType == dataType.LAGRANGIANSTRAIN)
				lagrangianStrainRadio.setSelected(true);
			else if(rawDataSet.interpreter.DataType == dataType.INCIDENTSG)
				incidentStrainGaugeRadio.setSelected(true);
			else if(rawDataSet.interpreter.DataType == dataType.TRANSMISSIONSG)
				transmissionStrainGaugeRadio.setSelected(true);
			else if(rawDataSet.interpreter.DataType == dataType.DISPLACEMENT)
				displacementRadioButton.setSelected(true);
			else if(rawDataSet.interpreter.DataType == dataType.INCIDENTBARSTRAIN)
				incidentBarStrainRadioButton.setSelected(true);
			else if(rawDataSet.interpreter.DataType == dataType.TRANSMISSIONBARSTRAIN)
				transmissionBarStrainRadioButton.setSelected(true);
			else {
				System.out.println("updating this toggle Not implemented");
			}
			
			multiplierTF.setText(Double.toString(rawDataSet.interpreter.multiplier));
		}
		
		
		
		updateStandardUnitsDescriptionLabel();
		
	}
	
	private void updateNameSuggestion() {
		String name = "";
		if(group.getSelectedToggle() == forceRadio){
			name = "Force";
			int count = model.countDataType(DataInterpreter.dataType.FORCE);
			count += existingSampleDataFiles.countDataType(dataType.FORCE);
			if(count > 0){
				//a force already exists, propose incremented name
				name = name + " #" + (count + 1); 
			}
			
		}
		else if(group.getSelectedToggle() == engineeringStrainRadio){
			name = "Engineering Strain";
			int count = model.countDataType(dataType.ENGINEERINGSTRAIN);
			count += existingSampleDataFiles.countDataType(dataType.ENGINEERINGSTRAIN);
			if(count > 0)
				name = name + " #" + (count + 1);
		}
		else if(group.getSelectedToggle() == trueStrainRadio){
			name = "True Strain";
			int count = model.countDataType(dataType.TRUESTRAIN);
			count += existingSampleDataFiles.countDataType(dataType.TRUESTRAIN);
			if(count > 0)
				name = name + " #" + (count + 1);
		}
		else if(group.getSelectedToggle() == lagrangianStrainRadio){
			name = "Lagrangian Strain";
			int count = model.countDataType(dataType.LAGRANGIANSTRAIN);
			count += existingSampleDataFiles.countDataType(dataType.LAGRANGIANSTRAIN);
			if(count > 0)
				name = name + " #" + (count + 1);
		}
		else if(group.getSelectedToggle() == displacementRadioButton){
			name = "Displacement";
			int count = model.countDataType(dataType.DISPLACEMENT);
			count += existingSampleDataFiles.countDataType(dataType.DISPLACEMENT);
			if(count > 0)
				name = name + " #" + (count + 1);
		}
		else if(group.getSelectedToggle() == incidentStrainGaugeRadio){
			name = "Incident Strain Gauge"; //maybe load up specific name of strain gauge?
			int count = model.countDataType(dataType.INCIDENTSG);
			count += existingSampleDataFiles.countDataType(dataType.INCIDENTSG);
			if(count > 0)
				name = name + " #" + (count + 1);
		}
		else if(group.getSelectedToggle() == transmissionStrainGaugeRadio){
			name = "Transmission Strain Gauge"; //again
			int count = model.countDataType(dataType.TRANSMISSIONSG);
			count += existingSampleDataFiles.countDataType(dataType.TRANSMISSIONSG);
			if(count > 0)
				name = name + " #" + (count + 1);
		}
		else if(group.getSelectedToggle() == incidentBarStrainRadioButton){
			name = "Incident Bar Strain";
			int count = model.countDataType(dataType.INCIDENTBARSTRAIN);
			count += existingSampleDataFiles.countDataType(dataType.INCIDENTBARSTRAIN);
			if(count > 0)
				name = name + " #" + (count + 1);
		}
		else if(group.getSelectedToggle() == transmissionBarStrainRadioButton){
			name = "Transmission Bar Strain";
			int count = model.countDataType(dataType.TRANSMISSIONBARSTRAIN);
			count += existingSampleDataFiles.countDataType(dataType.TRANSMISSIONBARSTRAIN);
			if(count > 0)
				name = name + " #" + (count + 1);
		}
		
		dataNameTF.setText(name);
			
	}
	

	
	public void updateStandardUnitsDescriptionLabel(){
		quickOptionsHbox.getChildren().clear();
		if (group.getSelectedToggle() != null) {

			standardUnitsInstructionsLabel.setVisible(true);
			if (group.getSelectedToggle() == timeRadio) {
				standardUnitsInstructionsLabel
						.setText("Please enter a factor to" + " convert the raw time data to seconds."
								+ " e.g. if the raw data is in milliseconds, enter 1000 below.");
				quickOptionsHbox.getChildren().add(s);
				quickOptionsHbox.getChildren().add(ms);
				quickOptionsHbox.getChildren().add(us);
				quickOptionsHbox.getChildren().add(ns);
			} else if (group.getSelectedToggle() == forceRadio) {
				standardUnitsInstructionsLabel
						.setText("Please enter a factor to" + " convert the raw force data to newtons or lbf."
								+ " e.g. if the raw data is in Volts and your lbf/V =1000, enter 1000.");
				quickOptionsHbox.getChildren().add(newtons);
				quickOptionsHbox.getChildren().add(lbf);
			} else if (group.getSelectedToggle() == incidentStrainGaugeRadio || 
					group.getSelectedToggle() == transmissionStrainGaugeRadio /*|| 
					group.getSelectedToggle() == loadCellRadio*/) {
				standardUnitsInstructionsLabel
						.setText("Please enter a factor to" + " convert the raw voltage data to volts."
								+ " e.g. if the raw data is in millivolts, enter 1000 below.");
				quickOptionsHbox.getChildren().add(v);
				quickOptionsHbox.getChildren().add(mv);
			}
			else if(group.getSelectedToggle() == engineeringStrainRadio || group.getSelectedToggle() == trueStrainRadio){
				standardUnitsInstructionsLabel
				.setText("Please enter a factor to" + " convert the raw strain data to strain."
						+ " e.g. if the raw data is in millistrain, enter 1000 below.");
			}
			else if(group.getSelectedToggle() == displacementRadioButton){
				standardUnitsInstructionsLabel.setText("Enter a factor to convert displacement data to meters.");
				quickOptionsHbox.getChildren().add(inchesButton);
				quickOptionsHbox.getChildren().add(mmButton);
			}
			else if(group.getSelectedToggle() == incidentBarStrainRadioButton){
				standardUnitsInstructionsLabel.setText("Enter a convert the data to strain, e.g. if the raw data is millistrain, enter 1000");
			}
		}
	}
	
	public void renderGraph(){

		
		XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
		currentSeries = series1;
		series1.setName("Data");
        chart.setCreateSymbols(false);

        for(int i = 0; i < rawDataSet.data.length; i++){
            series1.getData().add(new Data<Number, Number>(i, rawDataSet.data[i]));
            i += rawDataSet.data.length / 500;
        }
        
        chart.getData().add(series1);
	}
	
	
}
