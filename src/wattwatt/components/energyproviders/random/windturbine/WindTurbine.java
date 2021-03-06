package wattwatt.components.energyproviders.random.windturbine;

import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.interfaces.EmbeddingComponentAccessI;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import simulation.models.windturbine.WindTurbineCoupledModel;
import simulation.plugins.WindTurbineSimulatorPlugin;
import simulation.tools.windturbine.WindTurbineState;
import wattwatt.interfaces.controller.IController;
import wattwatt.interfaces.energyproviders.random.windturbine.IWindTurbine;
import wattwatt.ports.energyproviders.random.windturbine.WindTurbineInPort;
import wattwatt.tools.windturbine.WindTurbineSetting;

//-----------------------------------------------------------------------------
/**
* The class <code>WindTurbine</code>
*
* <p>
* <strong>Description</strong>
* </p>
* 
* This class implements the wind turbine component. The wind turbine 
* requires the controller interface because he have to be
* connected to the controller to receive order from him.
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
//The next annotation requires that the referenced interface is added to
//the required interfaces of the component.
@OfferedInterfaces(offered = IWindTurbine.class)
@RequiredInterfaces(required = IController.class)
public class WindTurbine extends AbstractCyPhyComponent implements EmbeddingComponentAccessI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	/** The inbound port of the wind turbine */
	protected WindTurbineInPort eoin;

	/** The state of the wind turbine */
	protected boolean isOn;
	/** The energy production of the wind turbine */
	protected double production;
	
	
	/** the simulation plug-in holding the simulation models. */
	protected WindTurbineSimulatorPlugin asp;
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a wind turbine.
	 * 
	 *
	 * @param uri        URI of the component.
	 * @param eoIn 	inbound port URI of the wind turbine.
	 * @throws Exception <i>todo.</i>
	 */
	protected WindTurbine(String uri, String eoIn) throws Exception {
		super(uri, 2, 1);
		this.initialise();
		
		this.isOn = true;
		this.production = 0.0;

		this.eoin = new WindTurbineInPort(eoIn, this);
		this.eoin.publishPort();

		this.tracer.setRelativePosition(2, 0);
	}

	protected void initialise() throws Exception {
		// The coupled model has been made able to create the simulation
		// architecture description.
		Architecture localArchitecture = this.createLocalArchitecture(null);
		// Create the appropriate DEVS simulation plug-in.
		this.asp = new WindTurbineSimulatorPlugin();
		// Set the URI of the plug-in, using the URI of its associated
		// simulation model.
		this.asp.setPluginURI(localArchitecture.getRootModelURI());
		// Set the simulation architecture.
		this.asp.setSimulationArchitecture(localArchitecture);
		// Install the plug-in on the component, starting its own life-cycle.
		this.installPlugin(this.asp);

	}
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------
	
	
	@Override
	public void start() throws ComponentStartException {
		super.start();
		this.logMessage("Eolienne starting");
		try {
			Thread.sleep(10);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void execute() throws Exception {
		super.execute();
	}

	@Override
	public void shutdown() throws ComponentShutdownException {
		try {
			this.eoin.unpublishPort();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.shutdown();
	}

	@Override
	public Object getEmbeddingComponentStateValue(String name) throws Exception {
		if(name.equals("production")) {

			return new Double(this.production);
		}
		else {
			assert name.equals("state");
			return this.isOn()?WindTurbineState.ON:WindTurbineState.OFF;
				
		}
	}
	
	@Override
	public void setEmbeddingComponentStateValue(String name, Object value) throws Exception {
		if(name.equals("production")) {
			this.setProduction((double)value);
		}
		else if(name.equals("start")) {
			this.On();
		}
		else{
			assert name.equals("stop");
			this.Off();
		}
	}

	@Override
	protected Architecture createLocalArchitecture(String architectureURI) throws Exception {
		return WindTurbineCoupledModel.build();
	}
	
	public void behave() {
		// production should depend on the power of the wind
		if (this.isOn) {
			this.production += WindTurbineSetting.PROD_THR;
		} else {

			if (this.production - WindTurbineSetting.PROD_THR <= 0) {
				this.production = 0;
			} else {

				this.production -= WindTurbineSetting.PROD_THR;
			}

		}
	}

	public double getEnergie() {
		return this.production;
	}

	public void On() {
		this.isOn = true;
	}

	public void Off() {
		this.isOn = false;
	}

	public boolean isOn() {

		return this.isOn;
	}
	
	public double windDensity(double tempKelvin) {
		return 1.292 * (273.15 / tempKelvin);
	}
	
	public void setProduction(double windSpeed) {
		if(this.isOn()) {
//			this.production = 0.5 * (WindTurbineModel.BLADES_AREA
//					* windDensity(WindTurbineModel.KELVIN_TEMP)) * windSpeed * windSpeed ;
			// We tried to calculate realistic value but the production was much too
			// high compared to the consumption 
			this.production = windSpeed;
		}
		
		
	}


}
