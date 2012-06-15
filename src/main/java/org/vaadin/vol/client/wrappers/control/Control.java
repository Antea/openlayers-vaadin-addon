package org.vaadin.vol.client.wrappers.control;

import com.google.gwt.core.client.JavaScriptObject;

public abstract class Control extends JavaScriptObject {

	protected Control () {}
	
	public native final void activate() 
	/*-{
		this.activate();
	}-*/;

	public native final void deActivate() 
	/*-{
		this.deactivate();
	}-*/;

	/**
	 * Helper to create a control by its name.
	 * 
	 * @param name
	 * @param map
	 * @return
	 */
	public native static final Control getControlByName(String name, String options)
	/*-{
		if($wnd.OpenLayers.Control[name]) {
                        var o = options ? eval("o = " +options+ ";") : {};
			return new $wnd.OpenLayers.Control[name](o);
		} else {
			return null;
		}
	}-*/;

	public native final String getId() 
	/*-{
		return this.id;
	}-*/;

}
