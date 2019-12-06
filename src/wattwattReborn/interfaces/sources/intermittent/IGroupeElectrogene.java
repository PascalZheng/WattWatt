package wattwattReborn.interfaces.sources.intermittent;

import wattwattReborn.interfaces.sources.ISources;

public interface IGroupeElectrogene extends ISources {
	
	public boolean fuelIsEmpty() throws Exception;
	public boolean fuelIsFull() throws Exception;
	public int fuelQuantity() throws Exception;
	
	public void on() throws Exception; // use fuel to prod electricity
	public void off() throws Exception;

}