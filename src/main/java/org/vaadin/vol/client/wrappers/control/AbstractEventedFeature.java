package org.vaadin.vol.client.wrappers.control;

import org.vaadin.vol.client.wrappers.AbstractOpenLayersWrapper;
import org.vaadin.vol.client.wrappers.GwtOlHandler;

/**
 * Prototipo per feature che registrano eventi
 * 
 * @author michele franzin
 */
public abstract class AbstractEventedFeature extends Control {
 
    protected AbstractEventedFeature(){
    }

     /**
     * Cut & paste da AbstractOpenLayersWrapper#registerHandler
     * 
     * @see AbstractOpenLayersWrapper
     * @param eventName
     * @param handler 
     */
    protected native final void registerHandler(String eventName, GwtOlHandler handler) 
    /*-{
        var handlerCaller = function() {
            $entry(handler.@org.vaadin.vol.client.wrappers.GwtOlHandler::onEvent(Lcom/google/gwt/core/client/JsArray;)(arguments));
        };
        this.events.addEventType(eventName);
        this.events.register(eventName, this, handlerCaller);
    }-*/;
}
