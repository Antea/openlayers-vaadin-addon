package org.vaadin.vol.demo;

import java.util.Map;

import org.vaadin.vol.Bounds;
import org.vaadin.vol.OpenLayersMap;
import org.vaadin.vol.Point;
import org.vaadin.vol.Style;
import org.vaadin.vol.StyleMap;
import org.vaadin.vol.WebFeatureServiceLayer;
import org.vaadin.vol.WebMapServiceLayer;
import org.vaadin.vol.WellKnownTextLayer;

import com.vaadin.ui.Component;

/**
 * http://openlayers.org/dev/examples/wfs-states.js
 */
public class WellKnownTextTest extends AbstractVOLTest {

    @Override
    Component getMap() {
        OpenLayersMap openLayersMap = new OpenLayersMap();
        WebMapServiceLayer webMapServiceLayer = new WebMapServiceLayer();
        webMapServiceLayer.setUri("http://tilecache.osgeo.org/wms-c/Basic.py");
        webMapServiceLayer.setBaseLayer(true);
        webMapServiceLayer.setDisplayName("Base map");
        openLayersMap.addLayer(webMapServiceLayer);

        WellKnownTextLayer webFeatureServiceLayer = new WellKnownTextLayer();
        webFeatureServiceLayer.setWellKnownText("GEOMETRYCOLLECTION(LINESTRING(-103.30859375 45.705078125, -98.73828125 40.607421875, -102.95703125 36.388671875, -102.60546875 36.388671875),LINESTRING(-111.74609375 47.990234375, -108.23046875 47.990234375, -106.82421875 45.705078125, -106.6484375 43.068359375, -107 41.837890625, -106.296875 41.310546875, -105.41796875 41.310546875, -104.01171875 40.783203125, -106.47265625 40.080078125, -107 39.904296875, -107 37.794921875, -105.76953125 35.861328125, -105.76953125 33.751953125, -110.515625 33.048828125),POLYGON((-114.55859375 50.626953125, -113.85546875 30.939453125, -95.046875 32.345703125, -96.62890625 50.275390625, -114.55859375 50.626953125)))");
        
        /*
         * Style like a normal web feature server.
         */
        
        Style style = new Style();
        style.extendCoreStyle("default");
        style.setFillColor("green");
        style.setFillOpacity(0.5);
        StyleMap styleMap = new StyleMap(style);
        styleMap.setExtendDefault(true);
        webFeatureServiceLayer.setStyleMap(styleMap );

        openLayersMap.addLayer(webFeatureServiceLayer);

        Bounds bounds = new Bounds(new Point(-140.4, 25.1), new Point(-44.4,
                50.5));
        openLayersMap.zoomToExtent(bounds);

        openLayersMap.setSizeFull();

        return openLayersMap;
    }

}
