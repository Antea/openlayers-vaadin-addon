package org.vaadin.vol.client.wrappers.control;


public class LoadingPanel extends Control {
	protected LoadingPanel() {};

	public static native LoadingPanel create()
	/*-{
		return new $wnd.OpenLayers.Control.LoadingPanel();
	}-*/;
}
