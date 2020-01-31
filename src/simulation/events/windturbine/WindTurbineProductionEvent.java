package simulation.events.windturbine;

import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.events.EventInformationI;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import simulation.models.controller.ControllerModel;

public class WindTurbineProductionEvent extends AbstractWindTurbineEvent {

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
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WindTurbineProductionEvent(Time timeOfOccurrence, EventInformationI content) {
		super(timeOfOccurrence, content);
	}
	
	public WindTurbineProductionEvent(Time timeOfOccurrence, double content) {
		super(timeOfOccurrence, new Reading(content));
	}

	@Override
	public String eventAsString() {
		return "WindTurbine(" + this.eventContentAsString() + ")";
	}

	@Override
	public String eventContentAsString() {
		return	"time = " + this.getTimeOfOccurrence() + ", " +
				"production = " + ((Reading)this.getEventInformation()).value
												+ " W";
	}
	
	@Override
	public void	executeOn(AtomicModel model)
	{
		assert	model instanceof ControllerModel ;

		ControllerModel m = (ControllerModel)model ;
		m.setProductionWindTurbine(((Reading)this.getEventInformation()).value);
	}
}
