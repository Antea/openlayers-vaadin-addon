package org.vaadin.vol.client.wrappers.control;

public class Scale extends Control {
	protected Scale() {};

	public static native Scale create()
	/*-{
		return new $wnd.OpenLayers.Control.Scale();
	}-*/;
}
