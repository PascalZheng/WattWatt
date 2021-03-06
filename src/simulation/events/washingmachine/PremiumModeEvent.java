package simulation.events.washingmachine;

import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import simulation.models.washingmachine.WashingMachineModel;

//-----------------------------------------------------------------------------
/**
* The class <code>PremiumModeEvent</code> defines the event which switches
* the consumption of the washing machine to high
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
public class PremiumModeEvent extends AbstractWashingMachineEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * create a PremiumModeEvent event.
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
	public PremiumModeEvent(Time timeOfOccurrence) {
		super(timeOfOccurrence, null);
	}
	
	@Override
	public String		eventAsString()
	{
		return "PremiumModeEvent(" + this.eventContentAsString() + ")" ;
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.es.events.ES_Event#hasPriorityOver(fr.sorbonne_u.devs_simulation.models.events.EventI)
	 */
	@Override
	public boolean		hasPriorityOver(EventI e)
	{
		return false;
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.events.Event#executeOn(fr.sorbonne_u.devs_simulation.models.AtomicModel)
	 */
	@Override
	public void executeOn(AtomicModel model) {
		assert model instanceof WashingMachineModel;

		WashingMachineModel m = (WashingMachineModel) model;
		m.premiumLavage();
	}

}
