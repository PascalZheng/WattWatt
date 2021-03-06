package wattwatt.ports.energyproviders.random.windturbine;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import wattwatt.components.energyproviders.random.windturbine.WindTurbine;
import wattwatt.interfaces.energyproviders.random.windturbine.IWindTurbine;

/**
 * The class <code>WindTurbineInPort</code>
 *
 * <p><strong>Description</strong></p>
 *  The InBound port of the wind turbine component
 * 
 * <p>Created on : 2020-01-27</p>
 * 
 * @author	<p>Bah Thierno, Zheng Pascal</p>
 */
public class WindTurbineInPort extends AbstractInboundPort implements IWindTurbine {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WindTurbineInPort(String uri, ComponentI owner) throws Exception {
		super(uri, IWindTurbine.class, owner);
	}

	@Override
	public double getEnergy() throws Exception {
		return this.getOwner().handleRequestSync(owner -> ((WindTurbine) owner).getEnergie());
	}

	@Override
	public boolean isOn() throws Exception {
		return this.getOwner().handleRequestSync(owner -> ((WindTurbine) owner).isOn());
	}

	@Override
	public void On() throws Exception {
		this.owner.handleRequestAsync(new AbstractComponent.AbstractService<Void>() {
			@Override
			public Void call() throws Exception {
				((WindTurbine) this.getServiceOwner()).On();
				return null;
			}
		});

	}

	@Override
	public void Off() throws Exception {
		this.owner.handleRequestAsync(new AbstractComponent.AbstractService<Void>() {
			@Override
			public Void call() throws Exception {
				((WindTurbine) this.getServiceOwner()).Off();
				return null;
			}
		});

	}

}
