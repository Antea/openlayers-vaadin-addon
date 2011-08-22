package org.vaadin.vol.client.wrappers.control;

import org.vaadin.vol.client.wrappers.Vector;
import org.vaadin.vol.client.wrappers.layer.Layer;

public class SelectFeature extends Control {
    protected SelectFeature() {
    }

    public native static SelectFeature create(Layer targetLayer,
            boolean multiple, boolean boxselection, boolean clickout,
            boolean toggle, boolean hover)
    /*-{
    	var o = {
           toggleKey: "ctrlKey",
           multipleKey: "shiftKey",
    	}
        o.multiple = multiple;
        o.box = boxselection;
        o.clickout = clickout;
        o.toggle = toggle;
        o.hover = hover;
        var h = new $wnd.OpenLayers.Control.SelectFeature(targetLayer, o);
        // TODO make this configurable, may disturb if there are multiple layers of vectors
        // Currently it is to enable panning while selection is on
        h.handlers.feature.stopDown = false;
    	return h;
    }-*/;

    public static SelectFeature create(Layer targetLayer) {
        return create(targetLayer, false, false, false, false, false);
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
