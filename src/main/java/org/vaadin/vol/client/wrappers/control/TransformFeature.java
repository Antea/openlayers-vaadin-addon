package org.vaadin.vol.client.wrappers.control;

import org.vaadin.vol.client.wrappers.GwtOlHandler;
import org.vaadin.vol.client.wrappers.Vector;
import org.vaadin.vol.client.wrappers.layer.VectorLayer;

public class TransformFeature extends AbstractEventedFeature {
	
	protected TransformFeature(){}
	
	public native static TransformFeature create(VectorLayer targetLayer)
	/*-{
                return new $wnd.OpenLayers.Control.TransformFeature(targetLayer);
	}-*/;

        public final void registerBeforeTransformListener(GwtOlHandler transformHandler) {
            registerHandler("beforetransform", transformHandler);
        }

        public final void registerTransformListener(GwtOlHandler transformHandler) {
            registerHandler("transform", transformHandler);
        }

        public final void registerTransformCompleteListener(GwtOlHandler transformCompleteHandler) {
            registerHandler("transformcomplete", transformCompleteHandler);
        }        
}
