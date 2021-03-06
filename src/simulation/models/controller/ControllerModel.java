package simulation.models.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.interfaces.EmbeddingComponentAccessI;
import fr.sorbonne_u.devs_simulation.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.AbstractSimulationReport;
import fr.sorbonne_u.utils.PlotterDescription;
import fr.sorbonne_u.utils.XYPlotter;
import simulation.events.controller.ResumeFridgeEvent;
import simulation.events.controller.StartEngineGeneratorEvent;
import simulation.events.controller.StartWashingMachineEvent;
import simulation.events.controller.StopEngineGeneratorEvent;
import simulation.events.controller.StopWashingMachineEvent;
import simulation.events.controller.SuspendFridgeEvent;
import simulation.events.electricmeter.ConsumptionEvent;
import simulation.events.enginegenerator.EngineGeneratorProductionEvent;
import simulation.events.windturbine.WindTurbineProductionEvent;
import simulation.tools.controller.Decision;
import simulation.tools.enginegenerator.EngineGeneratorState;
import simulation.tools.fridge.FridgeConsumption;
import simulation.tools.washingmachine.WashingMachineState;
import wattwatt.tools.URIS;

@ModelExternalEvents(imported = { ConsumptionEvent.class, 
								  EngineGeneratorProductionEvent.class,
								  WindTurbineProductionEvent.class },
					 exported = { StartEngineGeneratorEvent.class,
							 	  StopEngineGeneratorEvent.class, 
							 	  SuspendFridgeEvent.class, 
							 	  ResumeFridgeEvent.class,
							 	  StartWashingMachineEvent.class, 
							 	  StopWashingMachineEvent.class })
//-----------------------------------------------------------------------------
/**
* The class <code>ControllerModel</code> implements a simplified model of 
* a controller in the house
*
* <p><strong>Description</strong></p>
* 
* <p>
 * The controller model has two main variables: consumption and production. 
 * It imports multiple events providing the consumption and the production
 * of energy in the household. Given the received values, it triggers 
 * decisions that are either to the models (in MIL) or cause calls to
 * functions in the components (in SIL).
 * </p>
* 
* <p><strong>Invariant</strong></p>
* 
* <pre>
* invariant		true	// TODO
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
//-----------------------------------------------------------------------------
public class ControllerModel extends AtomicModel {
	// -------------------------------------------------------------------------
	// Inner classes
	// -------------------------------------------------------------------------

	/**
	 * The class <code>DecisionPiece</code> implements a piece in a piecewise
	 * representation of the observed decision function.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant		true	// TODO
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
	public static class DecisionPiece {
		public final double first;
		public final double last;
		public final Decision d;

		public DecisionPiece(double first, double last, Decision d) {
			super();
			this.first = first;
			this.last = last;
			this.d = d;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "(" + this.first + ", " + this.last + ", " + this.d + ")";
		}
	}

	/**
	 * The class <code>ControllerModelReport</code> implements the simulation
	 * report of the controller model.
	 *
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
	public static class ControllerModelReport extends AbstractSimulationReport {
		private static final long serialVersionUID = 1L;

		public ControllerModelReport(String modelURI) {
			super(modelURI);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "ControllerModel(" + this.getModelURI() + ")";
		}
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * URI used to create instances of the model; assumes a singleton, otherwise a
	 * different URI must be given to each instance.
	 */
	public static final String URI = URIS.CONTROLLER_MODEL_URI;

	private static final String PRODUCTION = "production";
	public static final String PRODUCTION_SERIES = "production-series";

	private static final String ENGINE_GENERATOR = "engine-generator";
	public static final String ENGINE_GENERATOR_SERIES = "engine-generator-series";

	private static final String FRIDGE = "frigde";
	public static final String FRIDGE_SERIES = "fridge-series";
	
	private static final String WASHING_MACHINE = "washing-machine";
	public static final String WASHING_MACHINE_SERIES = "washing-machine-series";

	private static final String CONTROLLER_STUB = "controller-stub";
	public static final String CONTROLLER_STUB_SERIES = "controller-stub-series";

	/**
	 * energy consumption (in Watt) retrieved from the electric meter
	 */
	protected double consumption;
	/**
	 * trigger to notify the controller model that a decision has to be sent
	 */
	protected boolean mustTransmitDecision;
	/**
	 * energy production (in Watt) provided by the engine generator
	 */
	protected double productionEngineGenerator;
	/**
	 * energy production (in Watt) provided by the wind turbine
	 */
	protected double productionWindTurbine;

	/**
	 * state of the engine generator
	 */
	protected EngineGeneratorState EGState;
	/**
	 * state of the fridge
	 */
	protected FridgeConsumption FridgeState;
	/**
	 * state of the washing machine
	 */
	protected WashingMachineState WMState;

	/**
	 * next decision to be sent to the engine generator
	 */
	protected Decision triggeredDecisionEngineGenerator;
	/**
	 * last decision sent to the engine generator
	 */
	protected Decision lastDecisionEngineGenerator;
	/**
	 * time of the last decision sent to the engine generator
	 */
	protected double lastDecisionTimeEngineGenerator;
	/**
	 * every decision sent to the engine generator are stored in this variable
	 */
	protected final Vector<DecisionPiece> decisionFunctionEngineGenerator;

	/**
	 * next decision to be sent to the fridge
	 */
	protected Decision triggeredDecisionFridge;
	/**
	 * last decision sent to the fridge
	 */
	protected Decision lastDecisionFridge;
	/**
	 * time of the last decision sent to the fridge
	 */
	protected double lastDecisionTimeFridge;
	/**
	 * every decision sent to the fridge are stored in this variable
	 */
	protected final Vector<DecisionPiece> decisionFunctionFridge;
	
	/**
	 * next decision to be sent to the washing machine
	 */
	protected Decision triggeredDecisionWashingMachine;
	/**
	 * last decision sent to the washing machine
	 */
	protected Decision lastDecisionWashingMachine;
	/**
	 * time of the last decision sent to the washing machine
	 */
	protected double lastDecisionTimeWashingMachine;
	/**
	 * every decision sent to the washing machine are stored in this variable
	 */
	protected final Vector<DecisionPiece> decisionFunctionWashingMachine;

	/**
	 * plotter for the production level over time
	 */
	protected XYPlotter productionPlotter;

	/**
	 * plotter for the decision on the components over time
	 */
	protected final Map<String, XYPlotter> modelsPlotter;

	/** reference on the object representing the component that holds the
	 *  model; enables the model to access the state of this component
	 */
	protected EmbeddingComponentAccessI componentRef;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an instance of controller model.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	simulatedTimeUnit != null
	 * pre	simulationEngine == null ||
	 * 		    	simulationEngine instanceof HIOA_AtomicEngine
	 * post	this.getURI() != null
	 * post	uri != null implies this.getURI().equals(uri)
	 * post	this.getSimulatedTimeUnit().equals(simulatedTimeUnit)
	 * post	simulationEngine != null implies
	 * 					this.getSimulationEngine().equals(simulationEngine)
	 * </pre>
	 *
	 * @param uri					unique identifier of the model.
	 * @param simulatedTimeUnit		time unit used for the simulation clock.
	 * @param simulationEngine		simulation engine enacting the model.
	 * @throws Exception			<i>todo.</i>
	 */
	public ControllerModel(String uri, TimeUnit simulatedTimeUnit, SimulatorI simulationEngine) throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);

		this.decisionFunctionEngineGenerator = new Vector<>();
		this.decisionFunctionFridge = new Vector<>();
		this.decisionFunctionWashingMachine = new Vector<>();
		this.modelsPlotter = new HashMap<String, XYPlotter>();

		// this.setLogger(new StandardLogger());
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.Model#setSimulationRunParameters(java.util.Map)
	 */
	@Override
	public void setSimulationRunParameters(Map<String, Object> simParams) throws Exception {
		String vname = this.getURI() + ":" + ControllerModel.PRODUCTION_SERIES + ":"
				+ PlotterDescription.PLOTTING_PARAM_NAME;
		PlotterDescription pd1 = (PlotterDescription) simParams.get(vname);
		this.productionPlotter = new XYPlotter(pd1);
		this.productionPlotter.createSeries(ControllerModel.PRODUCTION);

		vname = this.getURI() + ":" + ControllerModel.CONTROLLER_STUB_SERIES + ":"
				+ PlotterDescription.PLOTTING_PARAM_NAME;
		// if this key is in simParams, it's the MIL that's running
		if (simParams.containsKey(vname)) {
			PlotterDescription pd2 = (PlotterDescription) simParams.get(vname);
			this.modelsPlotter.put(ControllerModel.CONTROLLER_STUB, new XYPlotter(pd2));
			this.modelsPlotter.get(ControllerModel.CONTROLLER_STUB).createSeries(ControllerModel.CONTROLLER_STUB);
		} else {
			vname = this.getURI() + ":" + ControllerModel.ENGINE_GENERATOR_SERIES + ":"
					+ PlotterDescription.PLOTTING_PARAM_NAME;
			PlotterDescription pd2 = (PlotterDescription) simParams.get(vname);
			this.modelsPlotter.put(ControllerModel.ENGINE_GENERATOR, new XYPlotter(pd2));
			this.modelsPlotter.get(ControllerModel.ENGINE_GENERATOR).createSeries(ControllerModel.ENGINE_GENERATOR);

			vname = this.getURI() + ":" + ControllerModel.FRIDGE_SERIES + ":" + PlotterDescription.PLOTTING_PARAM_NAME;
			PlotterDescription pd3 = (PlotterDescription) simParams.get(vname);
			this.modelsPlotter.put(ControllerModel.FRIDGE, new XYPlotter(pd3));
			this.modelsPlotter.get(ControllerModel.FRIDGE).createSeries(ControllerModel.FRIDGE);
			
			vname = this.getURI() + ":" + ControllerModel.WASHING_MACHINE_SERIES + ":" + PlotterDescription.PLOTTING_PARAM_NAME;
			PlotterDescription pd4 = (PlotterDescription) simParams.get(vname);
			this.modelsPlotter.put(ControllerModel.WASHING_MACHINE, new XYPlotter(pd4));
			this.modelsPlotter.get(ControllerModel.WASHING_MACHINE).createSeries(ControllerModel.WASHING_MACHINE);
		}

		// The reference to the embedding component
		this.componentRef = (EmbeddingComponentAccessI) simParams.get(URIS.CONTROLLER_URI);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void initialiseState(Time initialTime) {
		super.initialiseState(initialTime);

		this.mustTransmitDecision = false;

		if (this.componentRef == null) {
			this.consumption = 0.0;
			this.productionEngineGenerator = 0.0;
			this.productionWindTurbine = 0.0;
			this.EGState = EngineGeneratorState.OFF;
			this.FridgeState = FridgeConsumption.RESUMED;
			this.WMState = WashingMachineState.OFF;
		} else {
			try {
				this.consumption = (double) this.componentRef.getEmbeddingComponentStateValue("consumption");
				this.productionEngineGenerator = (double) this.componentRef
						.getEmbeddingComponentStateValue("productionEG");
				this.productionWindTurbine = (double) this.componentRef.getEmbeddingComponentStateValue("productionWT");
				this.EGState = (EngineGeneratorState) this.componentRef.getEmbeddingComponentStateValue("stateEG");
				this.FridgeState = (FridgeConsumption) this.componentRef.getEmbeddingComponentStateValue("stateFridge");
				this.WMState = (WashingMachineState) this.componentRef.getEmbeddingComponentStateValue("stateWM");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		this.triggeredDecisionEngineGenerator = Decision.STOP_ENGINE;
		this.lastDecisionEngineGenerator = Decision.STOP_ENGINE;
		this.lastDecisionTimeEngineGenerator = initialTime.getSimulatedTime();
		this.decisionFunctionEngineGenerator.clear();

		this.triggeredDecisionFridge = Decision.RESUME_FRIDGE;
		this.lastDecisionFridge = Decision.RESUME_FRIDGE;
		this.lastDecisionTimeFridge = initialTime.getSimulatedTime();
		decisionFunctionFridge.clear();
		
		this.triggeredDecisionWashingMachine = Decision.STOP_WASHING;
		this.lastDecisionWashingMachine = Decision.STOP_WASHING;
		this.lastDecisionTimeWashingMachine = initialTime.getSimulatedTime();
		decisionFunctionWashingMachine.clear();

		if (this.productionPlotter != null) {
			this.productionPlotter.initialise();
			this.productionPlotter.showPlotter();
			this.productionPlotter.addData(ControllerModel.PRODUCTION, this.getCurrentStateTime().getSimulatedTime(),
					0.0);
		}

		for (Map.Entry<String, XYPlotter> elt : modelsPlotter.entrySet()) {
			String URI = elt.getKey();
			XYPlotter plotter = elt.getValue();
			if (plotter != null) {
				plotter.initialise();
				plotter.showPlotter();
				if (URI == ControllerModel.ENGINE_GENERATOR) {
					plotter.addData(URI, this.getCurrentStateTime().getSimulatedTime(),
							this.decisionToInteger(this.lastDecisionEngineGenerator));
				} else if (URI == ControllerModel.FRIDGE) {
					plotter.addData(URI, this.getCurrentStateTime().getSimulatedTime(),
							this.decisionToInteger(this.lastDecisionFridge));
				} 
				 else if (URI == ControllerModel.WASHING_MACHINE) {
						plotter.addData(URI, this.getCurrentStateTime().getSimulatedTime(),
								this.decisionToInteger(this.lastDecisionWashingMachine));
					}
				else {
					assert URI.equals(ControllerModel.CONTROLLER_STUB);
					plotter.addData(URI, this.getCurrentStateTime().getSimulatedTime(),
							this.decisionToInteger(this.lastDecisionEngineGenerator));
				}
			}
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI> output() {
		if (componentRef == null) {

			ArrayList<EventI> ret = null;
			ret = new ArrayList<EventI>(1);

			assert ret != null;

			if (this.triggeredDecisionEngineGenerator == Decision.START_ENGINE) {
				ret.add(new StartEngineGeneratorEvent(this.getCurrentStateTime()));
			} else if (this.triggeredDecisionEngineGenerator == Decision.STOP_ENGINE) {
				ret.add(new StopEngineGeneratorEvent(this.getCurrentStateTime()));
			} else if (this.triggeredDecisionFridge == Decision.SUSPEND_FRIDGE) {
				ret.add(new SuspendFridgeEvent(this.getCurrentStateTime()));
			} else if (this.triggeredDecisionFridge == Decision.RESUME_FRIDGE) {
				ret.add(new ResumeFridgeEvent(this.getCurrentStateTime()));
			}
			else if (this.triggeredDecisionWashingMachine == Decision.START_WASHING) {
				ret.add(new StartWashingMachineEvent(this.getCurrentStateTime()));
			} else if (this.triggeredDecisionWashingMachine == Decision.STOP_WASHING) {
				ret.add(new StopWashingMachineEvent(this.getCurrentStateTime()));
			}

			this.decisionFunctionEngineGenerator.add(new DecisionPiece(this.lastDecisionTimeEngineGenerator,
					this.getCurrentStateTime().getSimulatedTime(), this.lastDecisionEngineGenerator));

			this.decisionFunctionFridge.add(new DecisionPiece(this.lastDecisionTimeFridge,
					this.getCurrentStateTime().getSimulatedTime(), this.lastDecisionFridge));
			
			this.decisionFunctionWashingMachine.add(new DecisionPiece(this.lastDecisionTimeWashingMachine,
					this.getCurrentStateTime().getSimulatedTime(), this.lastDecisionWashingMachine));

			this.lastDecisionEngineGenerator = this.triggeredDecisionEngineGenerator;
			this.lastDecisionTimeEngineGenerator = this.getCurrentStateTime().getSimulatedTime();

			this.lastDecisionFridge = this.triggeredDecisionFridge;
			this.lastDecisionTimeFridge = this.getCurrentStateTime().getSimulatedTime();
			
			this.lastDecisionWashingMachine = this.triggeredDecisionWashingMachine;
			this.lastDecisionTimeWashingMachine = this.getCurrentStateTime().getSimulatedTime();

			this.mustTransmitDecision = false;
			return ret;
		} else {
			try {
				if (this.triggeredDecisionEngineGenerator != this.lastDecisionEngineGenerator) {
					if (this.triggeredDecisionEngineGenerator == Decision.START_ENGINE) {
						this.componentRef.setEmbeddingComponentStateValue("startEngine", null);
					} else if (this.triggeredDecisionEngineGenerator == Decision.STOP_ENGINE) {
						this.componentRef.setEmbeddingComponentStateValue("stopEngine", null);
					}
				} else if (this.triggeredDecisionFridge != this.lastDecisionFridge) {
					if (this.triggeredDecisionFridge == Decision.SUSPEND_FRIDGE) {
						this.componentRef.setEmbeddingComponentStateValue("suspendFridge", null);
					} else if (this.triggeredDecisionFridge == Decision.RESUME_FRIDGE) {
						this.componentRef.setEmbeddingComponentStateValue("resumeFridge", null);
					}
				}
				 else if (this.triggeredDecisionWashingMachine != this.lastDecisionWashingMachine) {
						if (this.triggeredDecisionWashingMachine == Decision.START_WASHING) {
							this.componentRef.setEmbeddingComponentStateValue("startWM", null);
						} else if (this.triggeredDecisionWashingMachine == Decision.STOP_WASHING) {
							this.componentRef.setEmbeddingComponentStateValue("stopWM", null);
						}
					}
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.decisionFunctionEngineGenerator.add(new DecisionPiece(this.lastDecisionTimeEngineGenerator,
					this.getCurrentStateTime().getSimulatedTime(), this.lastDecisionEngineGenerator));

			this.decisionFunctionFridge.add(new DecisionPiece(this.lastDecisionTimeFridge,
					this.getCurrentStateTime().getSimulatedTime(), this.lastDecisionFridge));
			
			this.decisionFunctionWashingMachine.add(new DecisionPiece(this.lastDecisionTimeWashingMachine,
					this.getCurrentStateTime().getSimulatedTime(), this.lastDecisionWashingMachine));

			this.lastDecisionEngineGenerator = this.triggeredDecisionEngineGenerator;
			this.lastDecisionTimeEngineGenerator = this.getCurrentStateTime().getSimulatedTime();

			this.lastDecisionFridge = this.triggeredDecisionFridge;
			this.lastDecisionTimeFridge = this.getCurrentStateTime().getSimulatedTime();
			
			this.lastDecisionWashingMachine = this.triggeredDecisionWashingMachine;
			this.lastDecisionTimeWashingMachine = this.getCurrentStateTime().getSimulatedTime();
			
			this.mustTransmitDecision = false;
			return null;
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration timeAdvance() {
		if (this.mustTransmitDecision) {
			return Duration.zero(this.getSimulatedTimeUnit());
		} else {
			return Duration.INFINITY;
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void userDefinedExternalTransition(Duration elapsedTime) {
		if (componentRef == null) {

			ArrayList<EventI> current = this.getStoredEventAndReset();
			
			Event ce = (Event) current.get(0);
			ce.executeOn(this);
			
			double production = this.productionEngineGenerator + this.productionWindTurbine;

			if (this.EGState == EngineGeneratorState.ON) {
				if (production > this.consumption) {
					// on l'eteint
					this.triggeredDecisionEngineGenerator = Decision.STOP_ENGINE;
					this.EGState = EngineGeneratorState.OFF;

					this.mustTransmitDecision = true;
				}
			} else {
				assert this.EGState == EngineGeneratorState.OFF;
				if (production <= this.consumption) {
					// on l'allume
					if (production <= this.consumption - 15) {
						this.triggeredDecisionEngineGenerator = Decision.START_ENGINE;
						this.EGState = EngineGeneratorState.ON;
						this.mustTransmitDecision = true;
					}

				}
			}
			if (this.FridgeState == FridgeConsumption.SUSPENDED) {
				if (production > this.consumption) {
					this.triggeredDecisionFridge = Decision.RESUME_FRIDGE;
					this.FridgeState = FridgeConsumption.RESUMED;

					this.mustTransmitDecision = true;
				}
			} else {
				assert this.FridgeState == FridgeConsumption.RESUMED;
				if (production <= this.consumption) {
					this.triggeredDecisionFridge = Decision.SUSPEND_FRIDGE;
					this.FridgeState = FridgeConsumption.SUSPENDED;
					this.mustTransmitDecision = true;
				}
			}
			
			if (this.WMState == WashingMachineState.ON) {
				if (production <= this.consumption) {
					this.triggeredDecisionWashingMachine = Decision.STOP_WASHING;
					this.WMState = WashingMachineState.OFF;

					this.mustTransmitDecision = true;
				}
			} else if(this.WMState == WashingMachineState.WORKING) {
				if (production <= this.consumption) {
					this.triggeredDecisionWashingMachine = Decision.STOP_WASHING;
					this.WMState = WashingMachineState.OFF;

					this.mustTransmitDecision = true;
				}
			}
			else {
				assert this.WMState == WashingMachineState.OFF;
				if (production > this.consumption + 20) {
					this.triggeredDecisionWashingMachine = Decision.START_WASHING;
					this.WMState = WashingMachineState.ON;

					this.mustTransmitDecision = true;
				}
			}


			this.productionPlotter.addData(PRODUCTION, this.getCurrentStateTime().getSimulatedTime(), production);
			this.productionPlotter.addData(PRODUCTION, this.getCurrentStateTime().getSimulatedTime(), production);

			for (Map.Entry<String, XYPlotter> elt : modelsPlotter.entrySet()) {
				String URI = elt.getKey();
				XYPlotter plotter = elt.getValue();
				if (plotter != null) {
					if (URI == ControllerModel.ENGINE_GENERATOR) {
						plotter.addData(URI, this.getCurrentStateTime().getSimulatedTime(),
								this.decisionToInteger(this.lastDecisionEngineGenerator));
					} else if (URI == ControllerModel.FRIDGE) {
						plotter.addData(URI, this.getCurrentStateTime().getSimulatedTime(),
								this.decisionToInteger(this.lastDecisionFridge));
					}else if (URI == ControllerModel.WASHING_MACHINE) {
						plotter.addData(URI, this.getCurrentStateTime().getSimulatedTime(),
								this.decisionToInteger(this.lastDecisionWashingMachine));
					}  
					else {
						assert URI.equals(ControllerModel.CONTROLLER_STUB);
						plotter.addData(URI, this.getCurrentStateTime().getSimulatedTime(),
								this.decisionToInteger(this.lastDecisionEngineGenerator));
					}
				}
			}

		} else {
			try {
				this.consumption = (double) this.componentRef.getEmbeddingComponentStateValue("consumption");
				this.productionEngineGenerator = (double) this.componentRef.getEmbeddingComponentStateValue("productionEG");
				this.productionWindTurbine = (double) this.componentRef.getEmbeddingComponentStateValue("productionWT");
				this.EGState = (EngineGeneratorState) this.componentRef.getEmbeddingComponentStateValue("stateEG");
				this.FridgeState = (FridgeConsumption) this.componentRef.getEmbeddingComponentStateValue("stateFridge");
				this.WMState = (WashingMachineState) this.componentRef.getEmbeddingComponentStateValue("stateWM");
			} catch (Exception e) {
				e.printStackTrace();
			}
			double production = this.productionEngineGenerator + this.productionWindTurbine;

			if (this.EGState == EngineGeneratorState.ON) {
				if (production > this.consumption) {
					// on l'eteint
					this.triggeredDecisionEngineGenerator = Decision.STOP_ENGINE;
					this.EGState = EngineGeneratorState.OFF;

					this.mustTransmitDecision = true;
				}
			} else {
				assert this.EGState == EngineGeneratorState.OFF;
				if (production <= this.consumption) {
					// on l'allume
					if (production <= this.consumption - 20) {
						this.triggeredDecisionEngineGenerator = Decision.START_ENGINE;
						this.EGState = EngineGeneratorState.ON;
						this.mustTransmitDecision = true;
					}

				}
			}
			if (this.FridgeState == FridgeConsumption.SUSPENDED) {
				if (production > this.consumption) {
					this.triggeredDecisionFridge = Decision.RESUME_FRIDGE;
					this.FridgeState = FridgeConsumption.RESUMED;
					this.mustTransmitDecision = true;
				}
			} else {
				assert this.FridgeState == FridgeConsumption.RESUMED;
				if (production <= this.consumption) {
					this.triggeredDecisionFridge = Decision.SUSPEND_FRIDGE;
					this.FridgeState = FridgeConsumption.SUSPENDED;
					this.mustTransmitDecision = true;
					
				}
			}
			if (this.WMState == WashingMachineState.ON) {
				if (production <= this.consumption) {
					this.triggeredDecisionWashingMachine = Decision.STOP_WASHING;
					this.WMState = WashingMachineState.OFF;

					this.mustTransmitDecision = true;
				}
			}else if(this.WMState == WashingMachineState.WORKING) {
				if (production <= this.consumption) {
					this.triggeredDecisionWashingMachine = Decision.STOP_WASHING;
					this.WMState = WashingMachineState.OFF;
					this.mustTransmitDecision = true;
				}
			} else {
				assert this.WMState == WashingMachineState.OFF;
				if (production > this.consumption + 20) {
					this.triggeredDecisionWashingMachine = Decision.START_WASHING;
					this.WMState = WashingMachineState.ON;

					this.mustTransmitDecision = true;
				}
			}

			this.productionPlotter.addData(PRODUCTION, this.getCurrentStateTime().getSimulatedTime(), production);
			this.productionPlotter.addData(PRODUCTION, this.getCurrentStateTime().getSimulatedTime(), production);

			for (Map.Entry<String, XYPlotter> elt : modelsPlotter.entrySet()) {
				String URI = elt.getKey();
				XYPlotter plotter = elt.getValue();
				if (plotter != null) {
					if (URI == ControllerModel.ENGINE_GENERATOR) {
						plotter.addData(URI, this.getCurrentStateTime().getSimulatedTime(),
								this.decisionToInteger(this.lastDecisionEngineGenerator));
					} else if (URI == ControllerModel.FRIDGE) {
						plotter.addData(URI, this.getCurrentStateTime().getSimulatedTime(),
								this.decisionToInteger(this.lastDecisionFridge));
					}else if (URI == ControllerModel.WASHING_MACHINE) {
						plotter.addData(URI, this.getCurrentStateTime().getSimulatedTime(),
								this.decisionToInteger(this.lastDecisionWashingMachine));
					}  
					else {
						assert URI.equals(ControllerModel.CONTROLLER_STUB);
						plotter.addData(URI, this.getCurrentStateTime().getSimulatedTime(),
								this.decisionToInteger(this.lastDecisionEngineGenerator));
					}
				}
			}
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void endSimulation(Time endTime) throws Exception {
		if (this.productionPlotter != null) {
			this.productionPlotter.addData(ControllerModel.PRODUCTION, this.getCurrentStateTime().getSimulatedTime(),
					this.productionEngineGenerator + this.productionWindTurbine);
		}

		for (Map.Entry<String, XYPlotter> elt : modelsPlotter.entrySet()) {
			String URI = elt.getKey();
			XYPlotter plotter = elt.getValue();
			if (plotter != null) {
				if (URI == ControllerModel.ENGINE_GENERATOR) {
					plotter.addData(URI, this.getCurrentStateTime().getSimulatedTime(),
							this.decisionToInteger(this.lastDecisionEngineGenerator));
				} else if (URI == ControllerModel.FRIDGE) {
					plotter.addData(URI, this.getCurrentStateTime().getSimulatedTime(),
							this.decisionToInteger(this.lastDecisionFridge));
				}else if (URI == ControllerModel.WASHING_MACHINE) {
					plotter.addData(URI, this.getCurrentStateTime().getSimulatedTime(),
							this.decisionToInteger(this.lastDecisionWashingMachine));
				}  
				else {
					assert URI.equals(ControllerModel.CONTROLLER_STUB);
					plotter.addData(URI, this.getCurrentStateTime().getSimulatedTime(),
							this.decisionToInteger(this.lastDecisionEngineGenerator));
				}
			}
		}
		super.endSimulation(endTime);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.Model#getFinalReport()
	 */
	@Override
	public SimulationReportI getFinalReport() throws Exception {
		return new ControllerModelReport(this.getURI());
	}
	
	// ------------------------------------------------------------------------
	// Model-specific methods
	// ------------------------------------------------------------------------

	/**
	 * return an integer representation to ease the plotting.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	s != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param d
	 *            a decision made by the controller.
	 * @return an integer representation to ease the plotting.
	 */
	protected int decisionToInteger(Decision d) {
		assert d != null;

		if (d == Decision.START_ENGINE) {
			return 1;
		} else if (d == Decision.STOP_ENGINE) {
			return 0;
		} else if (d == Decision.RESUME_FRIDGE) {
			return 1;
		} else if (d == Decision.SUSPEND_FRIDGE) {
			return 0;
		} 
		else if (d == Decision.START_WASHING) {
			return 1;
		} else if (d == Decision.STOP_WASHING) {
			return 0;
		}
		else {
			// Need to add other decisions
			return -1;
		}
	}

	public void setConsumption(double consumption) {
		this.consumption = consumption;
	}
	
	public void setProductionWindTurbine(double prod) {
		this.productionWindTurbine = prod;
	}
	
	public void setProductionEngineGenerator(double prod) {
		this.productionEngineGenerator = prod;
	}
}