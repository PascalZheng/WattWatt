package simulTest.models.events;

import fr.sorbonne_u.cyphy.examples.sg.equipments.hairdryer.models.HairDryerModel;
import fr.sorbonne_u.cyphy.examples.sg.equipments.hairdryer.models.events.AbstractHairDryerEvent;
import fr.sorbonne_u.cyphy.examples.sg.equipments.hairdryer.models.events.SetLow;
import fr.sorbonne_u.cyphy.examples.sg.equipments.hairdryer.models.events.SwitchOn;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventInformationI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import simulTest.models.ControleurModel;

public class GetConsoEvent extends AbstractHairDryerEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GetConsoEvent(Time timeOfOccurrence, EventInformationI content) {
		super(timeOfOccurrence, null);
	}

	@Override
	public String eventAsString() {
		return "Controller::GetConso";
	}

	// @Override
	// public boolean hasPriorityOver(EventI e) {
	// if (e instanceof SwitchOn || e instanceof SetLow) {
	// return false;
	// } else {
	// return true;
	// }
	// }

	@Override
	public void executeOn(AtomicModel model) {
		assert model instanceof ControleurModel;

		ControleurModel m = (ControleurModel) model;
//		if (m.getState() == HairDryerModel.State.LOW) {
//			m.setState(HairDryerModel.State.HIGH);
//		}
	}
}