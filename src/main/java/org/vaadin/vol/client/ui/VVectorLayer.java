package org.vaadin.vol.client.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.vaadin.vol.client.Costants;
import org.vaadin.vol.client.wrappers.GwtOlHandler;
import org.vaadin.vol.client.wrappers.JsObject;
import org.vaadin.vol.client.wrappers.Map;
import org.vaadin.vol.client.wrappers.Projection;
import org.vaadin.vol.client.wrappers.SelectFeatureFactory;
import org.vaadin.vol.client.wrappers.Style;
import org.vaadin.vol.client.wrappers.StyleMap;
import org.vaadin.vol.client.wrappers.Vector;
import org.vaadin.vol.client.wrappers.control.Control;
import org.vaadin.vol.client.wrappers.control.DrawFeature;
import org.vaadin.vol.client.wrappers.control.ModifyFeature;
import org.vaadin.vol.client.wrappers.control.SelectFeature;
import org.vaadin.vol.client.wrappers.geometry.Geometry;
import org.vaadin.vol.client.wrappers.geometry.LineString;
import org.vaadin.vol.client.wrappers.geometry.Point;
import org.vaadin.vol.client.wrappers.handler.PathHandler;
import org.vaadin.vol.client.wrappers.handler.PointHandler;
import org.vaadin.vol.client.wrappers.handler.PolygonHandler;
import org.vaadin.vol.client.wrappers.handler.RegularPolygonHandler;
import org.vaadin.vol.client.wrappers.layer.VectorLayer;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ValueMap;
import org.vaadin.vol.client.wrappers.control.HighlightFeature;
import org.vaadin.vol.client.wrappers.control.TransformFeature;
import org.vaadin.vol.client.wrappers.handler.Handler;

public class VVectorLayer extends FlowPanel implements VLayer, Container {

    private VectorLayer vectors;
    private String drawingMode = "NONE";
    private Control df;
    private GwtOlHandler _fAddedListener;
    private boolean updating;
    protected ApplicationConnection client;
    protected String displayName;
    private GwtOlHandler _fModifiedListener;

    private boolean immediate;

    public VectorLayer getLayer() {
        if (vectors == null) {
            vectors = createLayer();
            registerHandlers(vectors);
        }
        return vectors;
    }

    protected VectorLayer createLayer() {
            return VectorLayer.create(displayName);
    }

    protected void registerHandlers(VectorLayer vectors){
            vectors.registerHandler("featureadded", getFeatureAddedListener());
            vectors.registerHandler("featuremodified",
                    getFeatureModifiedListener());
            vectors.registerHandler("afterfeaturemodified", new GwtOlHandler() {
                @SuppressWarnings("rawtypes")
                public void onEvent(JsArray arguments) {
                    if (updating) {
                        // ignore selections that happend during update, those
                        // should be already known and notified by the server
                        // side
                        return;
                    }
                    client.sendPendingVariableChanges();
                }
            });
            vectors.registerHandler("featureselected", new GwtOlHandler() {
                @SuppressWarnings("rawtypes")
                public void onEvent(JsArray arguments) {
                    if (updating) {
                        // ignore selections that happend during update, those
                        // should be already known and notified by the server
                        // side
                        return;
                    }
                    if (client.hasEventListeners(VVectorLayer.this, "vsel")) {
                        ValueMap javaScriptObject = arguments.get(0).cast();
                        Vector vector = javaScriptObject.getValueMap("feature")
                                .cast();
                        for (Widget w : getChildren()) {
                            VAbstractVector v = (VAbstractVector) w;
                            if (v.getVector() == vector) {
                                client.updateVariable(paintableId, "vsel", v,
                                        true);
                            }
                        }
                    }
                }
            });

            vectors.registerHandler("featureunselected", new GwtOlHandler() {
                @SuppressWarnings("rawtypes")
                public void onEvent(JsArray arguments) {
                    ValueMap javaScriptObject = arguments.get(0).cast();
                    Vector vector = javaScriptObject.getValueMap("feature")
                            .cast();
                    for (Widget w : getChildren()) {
                        VAbstractVector v = (VAbstractVector) w;
                        if (v.getVector() == vector) {
                            v.revertDefaultIntent();
                            // ignore selections that happend during update, those
                            // should be already known and notified by the server
                            // side
                            if (!updating && client.hasEventListeners(VVectorLayer.this,
                                    "vusel")) {
                                client.updateVariable(paintableId, "vusel", v,
                                        true);
                                break;
                            }
                        }
                    }
                }
            });

    }

    protected GwtOlHandler getFeatureModifiedListener() {
        if (_fModifiedListener == null) {
            _fModifiedListener = new GwtOlHandler() {

                @SuppressWarnings("rawtypes")
                public void onEvent(JsArray arguments) {
                    if (!updating && drawingMode != "NONE") {
                        // use vaadin js object helper to get the actual feature
                        // from ol event
                        // TODO improve typing of OL events
                        JsObject event = arguments.get(0).cast();
                        Vector feature = event.getFieldByName("feature").cast();
                        Geometry geometry = feature.getGeometry();

                        boolean isLineString = true; // TODO
                        if (isLineString) {
                            LineString ls = geometry.cast();
                            JsArray<Point> allVertices = ls.getVertices();
                            client.updateVariable(paintableId,
                                    "newVerticesProj", getMap().getProjection()
                                            .toString(), false);
                            String[] points = new String[allVertices.length()];
                            for (int i = 0; i < allVertices.length(); i++) {
                                Point point = allVertices.get(i);
                                point = point.nativeClone();
                                point.transform(getMap().getProjection(),
                                        getProjection());
                                points[i] = point.toShortString();
                            }
                            // VConsole.log("modified");
                            // communicate points to server and mark the
                            // new geometry to be removed on next update.
                            client.updateVariable(paintableId, "vertices",
                                    points, false);

                            Vector modifiedFeature = ((ModifyFeature) df.cast())
                                    .getModifiedFeature();
                            Iterator<Widget> iterator = iterator();
                            while (iterator.hasNext()) {
                                VAbstractVector next = (VAbstractVector) iterator
                                        .next();
                                Vector vector = next.getVector();
                                if (vector == modifiedFeature) {
                                    client.updateVariable(paintableId,
                                            "modifiedVector", next, immediate);
                                    break;
                                }
                            }

                            // client.sendPendingVariableChanges();
                            // lastNewDrawing = feature;
                        }
                    }
                }
            };
        }
        return _fModifiedListener;
    }

    protected String paintableId;
    private Vector lastNewDrawing;
    private boolean added = false;
    private String currentSelectionMode;
    private SelectFeature selectFeature;
    protected String selectionCtrlId;             // Common SelectFeature control identifier

    private GwtOlHandler getFeatureAddedListener() {
        if (_fAddedListener == null) {
            _fAddedListener = new GwtOlHandler() {

                @SuppressWarnings("rawtypes")
                public void onEvent(JsArray arguments) {
                    if (!updating && drawingMode != "NONE") {
                        // use vaadin js object helper to get the actual feature
                        // from ol event
                        // TODO improve typing of OL events
                        JsObject event = arguments.get(0).cast();
                        Vector feature = event.getFieldByName("feature").cast();
                        Geometry geometry = feature.getGeometry();

                        if (drawingMode == "AREA" || drawingMode == "LINE" || drawingMode == "RECTANGLE" || drawingMode == "CIRCLE") {
                            LineString ls = geometry.cast();
                            JsArray<Point> allVertices = ls.getVertices();
                            // TODO this can be removed??
                            client.updateVariable(paintableId,
                                    "newVerticesProj", getMap().getProjection()
                                            .toString(), false);
                            String[] points = new String[allVertices.length()];
                            for (int i = 0; i < allVertices.length(); i++) {
                                Point point = allVertices.get(i);
                                point.transform(getMap().getProjection(),
                                        getProjection());
                                points[i] = point.toShortString();
                            }
                            client.updateVariable(paintableId, "vertices",
                                    points, false);
                        } else if (drawingMode == "POINT") {
                            // point
                            Point point = geometry.cast();
                            point.transform(getMap().getProjection(),
                                    getProjection());
                            double x = point.getX();
                            double y = point.getY();
                            client.updateVariable(paintableId, "x", x, false);
                            client.updateVariable(paintableId, "y", y, false);
                        }
                        // VConsole.log("drawing done");
                        // communicate points to server and mark the
                        // new geometry to be removed on next update.
                        client.sendPendingVariableChanges();
                        if (drawingMode != "MODIFY" || drawingMode == "TRANSFORM") {
                            lastNewDrawing = feature;
                        }
                    }
                }
            };
        }
        return _fAddedListener;
    }

    public void updateFromUIDL(final UIDL layer, ApplicationConnection client) {
        if (client.updateComponent(this, layer, false)) {
            return;
        }
        this.client = client;
        paintableId = layer.getId();
        updating = true;
        displayName = layer.getStringAttribute("name");
        immediate = layer.getBooleanAttribute("immediate");
        if (!added) {
            getMap().addLayer(getLayer());
            added = true;
        }

        // Last new drawing only visible to next update. If used by server side
        // handler, we probably have it as a child component
        if (lastNewDrawing != null) {
            getLayer().removeFeature(lastNewDrawing);
            lastNewDrawing = null;
        }

        updateStyleMap(layer);
        setDrawingMode(layer);

        // Identifier for SelectFeature control to use ... layers specifying the
        // the same identifier can all listen for their own Select events on the map.
        selectionCtrlId = layer.getStringAttribute("selectionCtrlId");

        setHightlightMode(layer);
        setSelectionMode(layer);

        Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                arrangeCurrentSelectedFeature(layer);
            }
        });

        HashSet<Widget> orphaned = new HashSet<Widget>();
        for (Iterator<Widget> iterator = iterator(); iterator.hasNext();) {
            orphaned.add(iterator.next());
        }

        int childCount = layer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            UIDL childUIDL = layer.getChildUIDL(i);
            VAbstractVector vector = (VAbstractVector) client
                    .getPaintable(childUIDL);
            boolean isNew = !hasChildComponent(vector);
            if (isNew) {
                add(vector);
            }
            vector.updateFromUIDL(childUIDL, client);
            orphaned.remove(vector);
        }
        for (Widget widget : orphaned) {
            widget.removeFromParent();
        }
        updating = false;
    }

   private String currentHighlightMode;
   private HighlightFeature hoverFeature;

   private void setHightlightMode(UIDL layer) {
        String newHighlightMode = layer.getStringAttribute("hmode").intern();
        if (currentHighlightMode != newHighlightMode) {
            if (hoverFeature != null) {
                hoverFeature.deActivate();
                getMap().removeControl(hoverFeature);
                hoverFeature = null;
            }

            if ("NONE" != newHighlightMode) {
                hoverFeature = createHighlightFeature(vectors, newHighlightMode);
                getMap().addControl(hoverFeature);
                hoverFeature.activate();
            }

            currentHighlightMode = newHighlightMode;
        }
    }

    /*
     * NB: Si occupa anche di trasferire coerentemente la selezione corrente tra i controlli di
     * edit/transform e quello di selezione/highlight durante i cambi di modalità
     */
    private void setSelectionMode(UIDL layer) {
        // Vector unselect support
        if (currentSelectionMode != "NONE") {
            if (layer.hasAttribute("uvector")) {
                VAbstractVector unselectedVector = (VAbstractVector) layer
                        .getPaintableAttribute("uvector", client);
                if (unselectedVector != null) {
                    selectFeature.unselect(unselectedVector.getVector());
                }
            }
        }
        String newSelectionMode = layer.getStringAttribute("smode").intern();
        if (currentSelectionMode != newSelectionMode) {
            if (selectFeature != null) {
                // Versione ONE SELECT
//                selectFeature.deActivate();
//                getMap().removeControl(selectFeature);

                // Versione MULTI SELECT
                SelectFeatureFactory.getInst().removeLayer(selectFeature, selectionCtrlId, getMap(), vectors);
                selectFeature = null;
            }

            if (newSelectionMode != "NONE") {
                // VERSIONE ONE SELECT
//                selectFeature = SelectFeature.create(vectors);
//                getMap().addControl(selectFeature);
//                selectFeature.activate();

                // VERSIONE MULTI SELECT
                selectFeature = SelectFeatureFactory.getInst().getOrCreate(selectionCtrlId, getMap(), vectors);
            }

            currentSelectionMode = newSelectionMode;
        }
    }

    private void arrangeCurrentSelectedFeature(final UIDL layer) {
            if (layer.hasAttribute("svector")) {
            Vector selectedVector = ((VAbstractVector) layer
                    .getPaintableAttribute("svector", client)).getVector();
                if (selectedVector != null) {
                    // ensure selection
                if (drawingMode != "NONE") {
                    if (drawingMode == "MODIFY") {
                        ModifyFeature mf = (ModifyFeature) df.cast();
                        if(mf.getModifiedFeature() != null) {
                            mf.unselect(mf.getModifiedFeature());
                        }
                        mf.select(selectedVector);
                    } else if (drawingMode == "TRANSFORM") {
                        TransformFeature tf = df.cast();
                        if(tf.feature() != null) {
                            tf.unsetFeature();
                        }
                        tf.setFeature(selectedVector);
                    }
                    } else {
                    if (currentSelectionMode != "NONE") {
                        selectFeature.select(selectedVector);
                    }
                    }
                }
            } else {
            if (drawingMode != "NONE") {
                // remove selection
                if (drawingMode == "MODIFY") {
                    ModifyFeature modifyFeature = (ModifyFeature) df.cast();
                    if (modifyFeature.getModifiedFeature() != null) {
                        modifyFeature.unselect(modifyFeature
                                .getModifiedFeature());
                    }
                } else if (drawingMode == "TRANSFORM") {
                    TransformFeature tf = df.cast();
                    if (tf.feature() != null) {
                        tf.unsetFeature();
                    }
                    // HACK per nascondere il box di selezione visualizzato all'origine, ma
                    // senza disabilitare il controllo, in modo da poter editare le altre aree
                    tf.hideGhostUnderTheCarpet();
                }
                } else {
                if (currentSelectionMode != "NONE") {
                    try {
                        selectFeature.unselectAll();
                       
                    } catch (Exception e) {
                        // NOP, may throw exception if selected vector gets
                        // deleted
                    }
                }
            }
        }
    }

    private void setDrawingMode(UIDL layer) {
        String newDrawingMode = layer.getStringAttribute("dmode").intern();
        if (drawingMode != newDrawingMode) {
            if (drawingMode != "NONE") {
                // remove old drawing feature
                if (drawingMode == "MODIFY") {
                    ModifyFeature mf = df.cast();
                    if (mf.getModifiedFeature() != null) {
                        mf.unselect(mf.getModifiedFeature());
                    }
                } else if(drawingMode == "TRANSFORM") {
                    TransformFeature tf = df.cast();
                    if (tf.feature() != null) {
                        tf.unsetFeature();
                    }
                }
                df.deActivate();
                getMap().removeControl(df);
            }
            drawingMode = newDrawingMode;
            df = null;

            if (drawingMode == "AREA") {
                df = createDrawFeature(getLayer(), drawingMode);
            } else if (drawingMode == "LINE") {
                df = createDrawFeature(getLayer(), drawingMode);
            } else if (drawingMode == "MODIFY") {
                df = createModifyFeature(getLayer());
            } else if (drawingMode == "TRANSFORM") {
                df = createTransformFeature(getLayer());
            } else if (drawingMode == "POINT") {
                df = createDrawFeature(getLayer(), drawingMode);
            } else if (drawingMode == "RECTANGLE") {
                df = createDrawFeature(getLayer(), drawingMode);
            } else if (drawingMode == "CIRCLE") {
                df = createDrawFeature(getLayer(), drawingMode);
            }
            if (df != null) {
                getMap().addControl(df);
                df.activate();
            }

        }
    }

    private void updateStyleMap(UIDL childUIDL) {
        StyleMap sm = getStyleMap(childUIDL);
        if (sm == null) {
            sm = StyleMap.create();
        }

        getLayer().setStyleMap(sm);
    }

    public static StyleMap getStyleMap(UIDL childUIDL) {
        if (!childUIDL.hasAttribute("olStyleMap")) {
            return null;
        }
        String[] renderIntents = childUIDL
                .getStringArrayAttribute("olStyleMap");
        StyleMap sm;
        if (renderIntents.length == 1 && renderIntents[0].equals("default")) {
            sm = StyleMap.create();
            sm.setStyle(
                    "default",
                    Style.create(childUIDL.getMapAttribute(
                            "olStyle_" + renderIntents[0]).cast()));
        } else {
            sm = StyleMap.create();
            for (String intent : renderIntents) {
                if (intent.startsWith("__")) {
                    String specialAttribute = intent.replaceAll("__", "");
                    if (specialAttribute.equals("extendDefault")) {
                        sm.setExtendDefault(true);
                    }
                } else {
                    Style style = Style.create(childUIDL
                            .getMapAttribute("olStyle_" + intent));
                    sm.setStyle(intent, style);
                }
            }
        }

        if (childUIDL.hasAttribute(Costants.STYLEMAP_UNIQUEVALUERULES)) {
            addUniqueValueRules(sm, childUIDL);
        }
        return sm;
    }

    private static void addUniqueValueRules(StyleMap sm, UIDL childUIDL) {

        if (childUIDL.hasAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_KEYS)) {
            String[] uvrKeysArray = childUIDL
                    .getStringArrayAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_KEYS);

            for (String uvrkey : uvrKeysArray) {

                String property = childUIDL
                        .getStringAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX
                                + uvrkey
                                + Costants.STYLEMAP_UNIQUEVALUERULES_PROPERTY_SUFFIX);
                String intent = childUIDL
                        .getStringAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX
                                + uvrkey
                                + Costants.STYLEMAP_UNIQUEVALUERULES_INTENT_SUFFIX);
                // Object context =
                // childUIDL.getStringAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX+uvrkey+Costants.STYLEMAP_UNIQUEVALUERULES_CONTEXT_SUFFIX);

                if (childUIDL
                        .hasAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX
                                + uvrkey + "_lookupkeys")) {
                    String[] lookup_keys = childUIDL
                            .getStringArrayAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX
                                    + uvrkey
                                    + Costants.STYLEMAP_UNIQUEVALUERULES_LOOKUPKEYS_SUFFIX);

                    JsObject symbolizer_lookup_js = JsObject.createObject();

                    for (String key : lookup_keys) {

                        ValueMap symbolizer = childUIDL
                                .getMapAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX
                                        + uvrkey
                                        + Costants.STYLEMAP_UNIQUEVALUERULES_LOOKUPITEM_SUFFIX
                                        + key);
                        symbolizer_lookup_js
                                .setProperty(key, symbolizer.cast());
                    }

                    sm.addUniqueValueRules(intent, property,
                            symbolizer_lookup_js.cast(), null);
                }
            }

        }

        return;
    }

    @Override
    protected void onDetach() {
        if (added) {
            if (df != null) {
                getMap().removeControl(df);
            }
            if (selectFeature != null) {
                getMap().removeControl(selectFeature);
            }
            if (hoverFeature != null) {
                getMap().removeControl(hoverFeature);
            }
            getMap().removeLayer(getLayer());
        }
        added = false;
        super.onDetach();
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        if (!added && vectors != null) {
            getMap().addLayer(getLayer());
            added = true;
        }
    }

    protected Map getMap() {
        return getVMap().getMap();
    }

    private VOpenLayersMap getVMap() {
        return ((VOpenLayersMap) getParent().getParent());
    }

    protected Projection getProjection() {
        return getVMap().getProjection();
    }

    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        // TODO Auto-generated method stub

    }

    public boolean hasChildComponent(Widget component) {
        return getWidgetIndex(component) != -1;
    }

    public void updateCaption(Paintable component, UIDL uidl) {
        // TODO Auto-generated method stub

    }

    public boolean requestLayout(Set<Paintable> children) {
        // TODO Auto-generated method stub
        return false;
    }

    public RenderSpace getAllocatedSpace(Widget child) {
        // TODO Auto-generated method stub
        return null;
    }

    public void vectorUpdated(VAbstractVector vAbstractVector) {
        // redraw
        getLayer().drawFeature(vAbstractVector.getVector());
        if (df != null) {
            String id = df.getId();
            if (id.contains("ModifyFeature")) {
                ModifyFeature mf = df.cast();
                Vector modifiedFeature = mf.getModifiedFeature();
                if (modifiedFeature == vAbstractVector.getVector()) {
                    // VConsole.log("Whoops, modified on the server side " +
                    // "while currently being modified on the client side. " +
                    // "This may cause issues for OL unleass we notify " +
                    // "the ModifyFeature about the change.");
                    mf.resetVertices();
                }
            }
        }
    }

    protected SelectFeature createSelectFeature(VectorLayer layer, String selectMode) {
        return SelectFeature.create(layer);
    }

    protected DrawFeature createDrawFeature(VectorLayer layer, String mode) {
        if (mode == "AREA") {
            return DrawFeature.create(layer, PolygonHandler.get());
        } else if (mode == "LINE") {
            return DrawFeature.create(layer, PathHandler.get());
        } else if (mode == "POINT") {
            return DrawFeature.create(layer, PointHandler.get());
        } else if (mode == "RECTANGLE") {
            return DrawFeature.create(layer, RegularPolygonHandler.get(), RegularPolygonHandler.getRectangleOptions());
        } else if (mode == "CIRCLE") {
            return DrawFeature.create(layer, RegularPolygonHandler.get(), RegularPolygonHandler.getCircleOptions());
        }
        return null;
    }

    protected HighlightFeature createHighlightFeature(VectorLayer layer, String highlightMode) {
        return HighlightFeature.create(layer);
    }

    protected ModifyFeature createModifyFeature(VectorLayer layer) {
        return ModifyFeature.create(layer);
    }

    protected TransformFeature createTransformFeature(VectorLayer layer) {
        return TransformFeature.create(layer);
    }
}
