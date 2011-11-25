package org.vaadin.vol.client.wrappers.geometry;

import org.vaadin.vol.client.wrappers.Bounds;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;


public abstract class Geometry extends JavaScriptObject {

	protected Geometry(){};
	
	
	public native final Bounds getBounds() 
	/*-{
		return this.getBounds();
	}-*/;

	public native final JsArray<Point> getVertices() 
	/*-{
		return this.getVertices();
	}-*/;	
}
