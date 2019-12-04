package wattwattReborn.connecteurs;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import wattwattReborn.interfaces.appareils.suspensible.refrigerateur.IRefrigerateur;
import wattwattReborn.interfaces.compteur.ICompteur;
import wattwattReborn.interfaces.controleur.IControleur;

public class ControleurCompteurConnector extends AbstractConnector implements IControleur {

	@Override
	public int getAllConso() throws Exception {
		return ((ICompteur)this.offering).giveAllConso();
	}

	@Override
	public void refriOn() throws Exception {
		((IRefrigerateur)this.offering).On();
		
	}

	@Override
	public void refriOff() throws Exception {
		((IRefrigerateur)this.offering).Off();
		
	}

	@Override
	public void refriSuspend() throws Exception {
		((IRefrigerateur)this.offering).suspend();
		
	}

	@Override
	public void refriResume() throws Exception {
		((IRefrigerateur)this.offering).resume();
		
	}

	@Override
	public double refriTempH() throws Exception {
		return ((IRefrigerateur)this.offering).getTempHaut();
	}

	@Override
	public double refriTempB() throws Exception {
		return ((IRefrigerateur)this.offering).getTempBas();
	}

	@Override
	public int refriConso() throws Exception {
		return ((IRefrigerateur)this.offering).giveConsommation();
	}

}
