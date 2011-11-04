package org.vaadin.vol.client.wrappers.control;

import org.vaadin.vol.client.wrappers.GwtOlHandler;
import org.vaadin.vol.client.wrappers.layer.Layer;

public class HighlightFeature extends AbstractEventedFeature {
    protected HighlightFeature() {
    }

    public native static HighlightFeature create(Layer targetLayer, String renderIntent)
    /*-{
    	var options = {
           hover: true,
           highlightOnly: true,
           renderIntent: renderIntent
    	}
        
    	return new $wnd.OpenLayers.Control.SelectFeature(targetLayer, options);
    }-*/;

    public static HighlightFeature create(Layer targetLayer) {
        return HighlightFeature.create(targetLayer, "temporary");
    }
 
    public final void registerBeforeHighlightlistener(GwtOlHandler beforeHighlightHandler) {
        registerHandler("beforefeaturehighlighted", beforeHighlightHandler);
    }

    public final void registerHighlightlistener(GwtOlHandler highlightHandler) {
        registerHandler("featurehighlighted", highlightHandler);
    }

    public final void registerUnhighlightlistener(GwtOlHandler unhighlightHandler) {
        registerHandler("featureunhighlighted", unhighlightHandler);
    }
}
