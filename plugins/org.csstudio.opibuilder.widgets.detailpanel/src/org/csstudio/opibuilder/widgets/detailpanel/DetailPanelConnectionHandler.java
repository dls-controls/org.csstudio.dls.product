package org.csstudio.opibuilder.widgets.detailpanel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.csstudio.simplepv.IPV;

/**
 * A modified version of the connection handler that does not mess with the widget border
 */
public class DetailPanelConnectionHandler {

	private Map<String, IPV> pvMap;
	
	protected DetailPanelEditpart editPart;

	
	/**
	 * @param editpart the widget editpart to be handled.
	 */
	public DetailPanelConnectionHandler(DetailPanelEditpart editpart) {
		editPart = editpart;
		pvMap = new ConcurrentHashMap<String, IPV>();
	}
	
	/**Add a PV to this handler, so its connection event can be handled.
	 * @param pvName name of the PV.
	 * @param pv the PV object.
	 */
	public void addPV(final String pvName, final IPV pv){
		pvMap.put(pvName, pv);
	}
	
	public void removePV(final String pvName){	
		if(pvMap == null){
			return;
		}		
		pvMap.remove(pvName);
	}
}
