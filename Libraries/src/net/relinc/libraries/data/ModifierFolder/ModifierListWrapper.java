package net.relinc.libraries.data.ModifierFolder;

import java.util.ArrayList;

import net.relinc.libraries.data.ModifierFolder.Modifier.ModifierEnum;

public class ModifierListWrapper extends ArrayList<Modifier> {
	
	private static final long serialVersionUID = 1L; //Gets rid of a warning

	public Modifier getModifier(ModifierEnum en){
		for(Modifier modifier : this){
			if(modifier.modifierEnum == en)
				return modifier;
		}
		return null;
	}
	
	public Modifier getZeroModifier(){
		for(Modifier mod : this)
			if(mod.modifierEnum == ModifierEnum.ZERO)
				return mod;
		return null;
	}
	
	public Modifier getPochammerModifier(){
		for(Modifier mod : this)
			if(mod.modifierEnum == ModifierEnum.POCHAMMER)
				return mod;
		return null;
	}
	
	public LowPass getLowPassModifier(){
		for(Modifier mod : this)
			if(mod instanceof LowPass)
				return (LowPass)mod;
		return null;
	}
	
	public Fitter getFitterModifier(){
		for(Modifier mod : this){
			if(mod instanceof Fitter)
				return (Fitter)mod;
		}
		return null;
	}

	public Resampler getResamplerModifier() {
		return this.stream().filter(m -> m instanceof Resampler).map(m -> (Resampler)m).findFirst().orElse(null);
	}

	public void setModifierFromLine(String line) {
		for(Modifier m : this){
			m.readModifierFromString(line);
			//m.setValuesFromLine(line);
		}
	}
}
