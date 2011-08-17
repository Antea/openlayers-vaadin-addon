package org.vaadin.vol.client.wrappers.layer;

import org.vaadin.vol.client.wrappers.StyleMap;
import org.vaadin.vol.client.wrappers.Vector;

import com.google.gwt.core.client.JavaScriptObject;

public class VectorLayer extends Layer {

    protected VectorLayer() {
    };

    /**
     * TODO name and styles
     * 
     * @return
     */
    public native final static VectorLayer create(String displayName)
    /*-{
        var defaultStyle = $wnd.OpenLayers.Util.extend({}, $wnd.OpenLayers.Feature.Vector.style['default']);
        defaultStyle.fillOpacity = 0.2;
        defaultStyle.graphicOpacity = 1;

        var selectStyle = $wnd.OpenLayers.Util.extend({}, $wnd.OpenLayers.Feature.Vector.style['select']);
        var temporaryStyle = $wnd.OpenLayers.Util.extend({}, $wnd.OpenLayers.Feature.Vector.style['temporary']);

        var styles = new $wnd.OpenLayers.StyleMap({
            "default" : defaultStyle,
            "select" : selectStyle,
            "temporary" : temporaryStyle
        });

    	return new $wnd.OpenLayers.Layer.Vector(displayName, {styleMap: styles});
    }-*/;

    public native final static VectorLayer create(String displayName,
            JavaScriptObject stylemap)
    /*-{
    	_myvector_layer = new $wnd.OpenLayers.Layer.Vector(displayName);
    	if(stylemap) 
    		_myvector_layer.styleMap = stylemap;
    	return _myvector_layer;
    	
    }-*/;

    public native final void removeFeature(Vector vector)
    /*-{
    	this.removeFeatures(vector);
    }-*/;
    
    public native final void eraseFeature(Vector vector)
    /*-{
    	this.eraseFeatures(vector);
    }-*/;

    public native final void addFeature(Vector vector)
    /*-{
    	this.addFeatures(vector);
    }-*/;

    public native final void drawFeature(Vector vector)
    /*-{
    	this.drawFeature(vector);
    }-*/;
    
    public native final void redraw()
    /*-{
        this.redraw();
    }-*/;

    public native final void setStyleMap(StyleMap style)
    /*-{
    	 this.styleMap = style;
    }-*/;

    public native final StyleMap getStyleMap(StyleMap style)
    /*-{
    	 return this.styleMap;
    }-*/;

    public native final void removeAllFeatures() 
    /*-{
        this.removeAllFeatures();
    }-*/;

}
