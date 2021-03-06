package simulation.mil;

import java.util.HashMap;
import java.util.Map;

import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;
import fr.sorbonne_u.utils.PlotterDescription;
import simulation.deployment.WattWattMain;
import simulation.models.controller.ControllerCoupledModel;
import simulation.models.controller.ControllerModel;
import simulation.tools.TimeScale;

//-----------------------------------------------------------------------------
/**
* The class <code>MIL_Controller</code> simply tests the simulation architecture
* defined by <code>ControllerCoupledModel</code> before attaching it to a
* component.
* The Coupled model use a Stub to represent the devices that should interact with the controller.
* <p><strong>Description</strong></p>
* 
* <p><strong>Invariant</strong></p>
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
public class MIL_Controller {
	public static void	main(String[] args)
	{
		SimulationEngine se ;

		try {
			Architecture localArchitecture = ControllerCoupledModel.build() ;
			se = localArchitecture.constructSimulator() ;
			Map<String, Object> simParams = new HashMap<String, Object>();
			
			simParams.put(					ControllerModel.URI + ":" + ControllerModel.PRODUCTION_SERIES + ":"
					+ PlotterDescription.PLOTTING_PARAM_NAME,
			new PlotterDescription("ControllerModel", "Time (sec)", "W", WattWattMain.ORIGIN_X,
					WattWattMain.ORIGIN_Y , WattWattMain.getPlotterWidth(),
					WattWattMain.getPlotterHeight()));
			simParams.put(					ControllerModel.URI + ":" + ControllerModel.CONTROLLER_STUB_SERIES + ":"
					+ PlotterDescription.PLOTTING_PARAM_NAME,
			new PlotterDescription("ControllerModel", "Time (sec)", "Decision", WattWattMain.ORIGIN_X,
					WattWattMain.ORIGIN_Y + WattWattMain.getPlotterHeight(), WattWattMain.getPlotterWidth(),
					WattWattMain.getPlotterHeight()));
			
			se.setSimulationRunParameters(simParams);
			se.setDebugLevel(0) ;
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L ;
			se.doStandAloneSimulation(0.0, TimeScale.DAY) ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
