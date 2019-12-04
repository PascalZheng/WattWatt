package wattwattReborn.composants;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import wattwattReborn.interfaces.compteur.ICompteur;
import wattwattReborn.interfaces.controleur.IControleur;
import wattwattReborn.ports.compteur.CompteurInPort;

@OfferedInterfaces(offered = ICompteur.class)
@RequiredInterfaces(required = IControleur.class)
public class Compteur extends AbstractComponent {

	protected String COMPTEUR_URI;

	protected int consomation = 150;

	protected CompteurInPort cptin;

	public Compteur(String uri, String compteurIn) throws Exception {
		super(uri, 1, 1);
		COMPTEUR_URI = uri;

		cptin = new CompteurInPort(compteurIn, this);
		cptin.publishPort();

		this.tracer.setRelativePosition(0, 1);
	}

	public String getCOMPTEUR_URI() {
		return COMPTEUR_URI;
	}

	public int giveConso() throws Exception {
		return consomation;
	}

	public void majConso() {
		Random rand = new Random();
		this.consomation = 1000 + rand.nextInt(300);
	}

	@Override
	public void start() throws ComponentStartException {
		super.start();
		this.logMessage("Compteur starting");
		try {
			Thread.sleep(10);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void execute() throws Exception {
		super.execute();
		this.scheduleTask(new AbstractComponent.AbstractTask() {
			@Override
			public void run() {
				try {
					while (true) {
						
						((Compteur) this.getTaskOwner()).majConso();
						((Compteur) this.getTaskOwner()).logMessage("Compteur");
						Thread.sleep(1000);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}, 1000, TimeUnit.MILLISECONDS);
	}

	@Override
	public void shutdown() throws ComponentShutdownException {
		this.logMessage("Compteur shutdown");
		// unpublish les ports
		try {
			this.cptin.unpublishPort();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.shutdown();
	}

	@Override
	public void finalise() throws Exception {
		// unpublish les ports
		try {
			this.cptin.unpublishPort();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.finalise();
	}

}
