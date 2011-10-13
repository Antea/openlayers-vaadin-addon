package org.vaadin.vol.client.wrappers.control;

import org.vaadin.vol.client.wrappers.GwtOlHandler;
import org.vaadin.vol.client.wrappers.layer.Layer;

public class HighlightFeature extends Control {
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
        return create(targetLayer, "temporary");
    }
 
    public final void registerBeforeHighlightListener(GwtOlHandler beforeHighlightHandler) {
        registerHandler("beforefeaturehighlighted", beforeHighlightHandler);
    }

    public final void registerHighlightListener(GwtOlHandler highlightHandler) {
        registerHandler("featurehighlighted", highlightHandler);
    }

    public final void registerUnhighlightListener(GwtOlHandler unhighlightHandler) {
        registerHandler("featureunhighlighted", unhighlightHandler);
    }

    /**
     * Cut & paste da AbstractOpenLayersWrapper#registerHandler
     * 
     * @see AbstractOpenLayersWrapper
     * @param eventName
     * @param handler 
     */
    private native final void registerHandler(String eventName, GwtOlHandler handler) 
    /*-{
        var handlerCaller = function() {
            $entry(handler.@org.vaadin.vol.client.wrappers.GwtOlHandler::onEvent(Lcom/google/gwt/core/client/JsArray;)(arguments));
        };
        this.events.addEventType(eventName);
        this.events.register(eventName, this, handlerCaller);
    }-*/;
}
