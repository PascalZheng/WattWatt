package simulation.events.hairdryer;

import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import simulation.models.hairdryer.HairDryerModel;

//-----------------------------------------------------------------------------
/**
* The class <code>SwitchModeEvent</code> defines the event which changes
* the mode in which the hair dryer is in
*
* <p>
* <strong>Description</strong>
* </p>
* 
* <p>
* <strong>Invariant</strong>
* </p>
* 
* <pre>
* invariant		true
* </pre>
* 
* <p>
* Created on : 2020-01-27
* </p>
* 
* @author
*         <p>
*         Bah Thierno, Zheng Pascal
*         </p>
*/
public class SwitchModeEvent extends AbstractHairDryerEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * create a SwitchModeEvent event.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	timeOfOccurrence != null 
	 * post	this.getTimeOfOccurrence().equals(timeOfOccurrence)
	 * </pre>
	 *
	 * @param timeOfOccurrence time of occurrence of the event.
	 */
	public SwitchModeEvent(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}

	@Override
	public String eventAsString() {
		return "HairDryer::SwitchMode";
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.es.events.ES_Event#hasPriorityOver(fr.sorbonne_u.devs_simulation.models.events.EventI)
	 */
	@Override
	public boolean hasPriorityOver(EventI e) {
		if (e instanceof SwitchOnEvent) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.events.Event#executeOn(fr.sorbonne_u.devs_simulation.models.AtomicModel)
	 */
	@Override
	public void executeOn(AtomicModel model) {
		assert model instanceof HairDryerModel;
		
		HairDryerModel m = (HairDryerModel) model;
		m.switchMode();
	}
}
