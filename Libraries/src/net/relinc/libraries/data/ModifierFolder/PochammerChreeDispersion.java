package net.relinc.libraries.data.ModifierFolder;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.staticClasses.SPSettings;

public class PochammerChreeDispersion extends Modifier {
	private String pochammerChreeDescriptor = "Pochammer-Chree";
	
	public PochammerChreeDispersion() {
		modifierEnum = ModifierEnum.POCHAMMER;
		checkBox = new CheckBox("Enable Pochammer-Chree Dispersion");
		checkBox.selectedProperty().bindBidirectional(activated);
		checkBox.disableProperty().bind(enabled.not());
	}

	@Override
	public String toString() {
		return "Pochammer-Chree Dispersion BETA";
	}

	@Override
	public List<Node> getTrimDataHBoxControls() {
		return new ArrayList<Node>();
	}

	@Override
	public String getStringForFileWriting() {
		return enabled.get() ? pochammerChreeDescriptor + ":true" + SPSettings.lineSeperator : "";
	}

	@Override
	public void setValuesFromDescriptorValue(String descrip, String val) {
		if(descrip.equals(pochammerChreeDescriptor)){
			enabled.set(true);
			activated.set(true);
		}
	}

	@Override
	public ModifierResult applyModifier(double[] x, double[] y, DataSubset data) {
		return new ModifierResult(x, y, 1.0);
	}

	@Override
	public void configureModifier(DataSubset sub) {
		//nothing to do
	}
	
	@Override
	public void readModifierFromString(String line) {
		setValuesFromLine(line);
	}
}
