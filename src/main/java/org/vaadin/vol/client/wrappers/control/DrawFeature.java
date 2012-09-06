package org.vaadin.vol.client.wrappers.control;

import org.vaadin.vol.client.wrappers.GwtOlHandler;
import org.vaadin.vol.client.wrappers.handler.Handler;
import org.vaadin.vol.client.wrappers.layer.VectorLayer;

public class DrawFeature extends AbstractEventedFeature {
	protected DrawFeature(){}

	public native static DrawFeature create(VectorLayer targetLayer, Handler drawHandler)
	/*-{
		return new $wnd.OpenLayers.Control.DrawFeature(targetLayer, drawHandler);
	}-*/;


        public final void registerFeatureAddedListener(GwtOlHandler handler) {
            registerHandler("featureadded", handler);
        }
}
