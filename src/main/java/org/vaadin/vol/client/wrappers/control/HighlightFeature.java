package org.vaadin.vol.client.wrappers.control;

import org.vaadin.vol.client.wrappers.Vector;
import org.vaadin.vol.client.wrappers.layer.Layer;

public class HighlightFeature extends Control {
    protected HighlightFeature() {
    }

    public native static HighlightFeature create(Layer targetLayer, String renderIntent)
    /*-{
    	var o = {
           hover: true,
           highlightOnly: true,
           renderIntent: renderIntent
    	}
        
    	return new $wnd.OpenLayers.Control.SelectFeature(targetLayer, o);
    }-*/;

    public static HighlightFeature create(Layer targetLayer) {
        return create(targetLayer, "temporary");
    }

    public final native void unselectAll()
    /*-{
        this.unselectAll();
    }-*/;

    public final native void select(Vector vector)
    /*-{
        this.select(vector);
    }-*/;

    public final native void highlight(Vector vector)
    /*-{
        this.highlight(vector);
    }-*/;
}
