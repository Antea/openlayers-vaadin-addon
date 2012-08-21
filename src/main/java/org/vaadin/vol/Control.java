package org.vaadin.vol;

public enum Control {
    ArgParser, Navigation, LayerSwitcher, ScaleLine, PanZoomBar, PanZoom, NavToolbar, OverviewMap, MousePosition, Scale,
    /**
     * Supported from OpenLayers 2.11.
     */
    TouchNavigation, Attribution, ZoomPanel, ZoomToMaxExtent, Zoom,
    /**
     * BBox custom
     */
    LoadingPanel, ZoomToMaxExtentPanel
}
