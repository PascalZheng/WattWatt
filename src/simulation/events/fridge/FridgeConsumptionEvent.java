package simulation.events.fridge;

import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.events.EventInformationI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import simulation.models.electricmeter.ElectricMeterModel;

//----------------------------------------------------------------------------
/**
* The class <code>FridgeConsumptionEvent</code> define an event sent by the fridge to transmit
*  his energy consumption to the electric meter
*
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
public class FridgeConsumptionEvent  extends AbstractFridgeEvent {

	
	/**
	 * The class <code>Reading</code> implements the energy consumption value as an
	 * event content.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true
	 * </pre>
	 * 
	* <p>Created on : 2020-01-27</p>
	* 
	* @author	<p>Bah Thierno, Zheng Pascal</p>
	*/
	public static class		Reading
	implements EventInformationI
	{
		private static final long serialVersionUID = 1L;
		public final double	value ;

		public			Reading(double value)
		{
			super();
			this.value = value;
		}
	}
	

	private static final long serialVersionUID = 1L;
	
	/**
	 * Create an FridgeConsumptionEvent sent by the fridge
	 * 
	 *
	 * @param timeOfOccurrence	time of occurrence of the event.
	 * @param content			content of the event.
	 */
	public FridgeConsumptionEvent(Time timeOfOccurrence, EventInformationI content) {
		super(timeOfOccurrence, content);
	}
	
	/**
	 * Create an FridgeConsumptionEvent sent by the fridge
	 * 
	 *
	 * @param timeOfOccurrence	time of occurrence of the event.
	 */
	public FridgeConsumptionEvent(Time timeOfOccurrence, double content) {
		super(timeOfOccurrence, new Reading(content));
	}
	
	@Override
	public String eventAsString() {
		return "FridgeConsumption(" + this.eventContentAsString() + ")";
	}

	@Override
	public String eventContentAsString() {
		return	"time = " + this.getTimeOfOccurrence() + ", " +
				"quantity = " + ((Reading)this.getEventInformation()).value
												+ " mAh";
	}
	
	@Override
	public void	executeOn(AtomicModel model)
	{
		assert	model instanceof ElectricMeterModel ;

		ElectricMeterModel m = (ElectricMeterModel)model ;
		m.setFridgeConsumption(((Reading)this.getEventInformation()).value);
	}

}
