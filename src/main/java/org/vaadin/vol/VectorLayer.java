/**
 * 
 */
package org.vaadin.vol;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.tools.ReflectTools;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;

@ClientWidget(org.vaadin.vol.client.ui.VVectorLayer.class)
public class VectorLayer extends AbstractComponentContainer implements Layer {

    private StyleMap stylemap;

    public enum SelectionMode {
        NONE, SIMPLE
        // MULTI, MULTI_WITH_AREA_SELECTION etc
    }

    private Vector selectedVector;
    private Vector unselectedVector = null;

    public enum HighlightMode {
        NONE, DEFAULT
    }

    private HighlightMode highlightMode = HighlightMode.NONE;

    private String displayName = "Vector layer";

    private List<Vector> vectors = new LinkedList<Vector>();

    private SelectionMode selectionMode = SelectionMode.NONE;

    public enum DrawingMode {
        NONE, LINE, AREA, POINT, MODIFY, TRANSFORM
    }

    private DrawingMode drawindMode = DrawingMode.NONE;

    public void addVector(Vector m) {
        addComponent(m);
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        target.addAttribute("name", displayName);
        target.addAttribute("dmode", drawindMode.toString());
        target.addAttribute("smode", selectionMode.toString());
        target.addAttribute("hmode", highlightMode.toString());
        if (selectedVector != null) {
            target.addAttribute("svector", selectedVector);
        }
        if (unselectedVector != null) {
            target.addAttribute("uvector", unselectedVector);
            unselectedVector = null;
        }

        if (stylemap != null) {
            stylemap.paint(target);
        }

        for (Vector m : vectors) {
            m.paint(target);
        }

    }

    public void replaceComponent(Component oldComponent, Component newComponent) {
        throw new UnsupportedOperationException();
    }

    public Iterator<Component> getComponentIterator() {
        LinkedList<Component> list = new LinkedList<Component>(vectors);
        return list.iterator();
    }

    @Override
    public void addComponent(Component c) {
        if (c instanceof Vector) {
            vectors.add((Vector) c);
            super.addComponent(c);
        } else {
            throw new IllegalArgumentException(
                    "VectorLayer supports only Vectors");
        }
    }

    @Override
    public void removeComponent(Component c) {
        vectors.remove(c);
        super.removeComponent(c);
        if (selectedVector == c) {
            unselectedVector = selectedVector;
            selectedVector = null;
            fireEvent(new VectorUnSelectedEvent(this, unselectedVector));
        }
        requestRepaint();
    }

    public void setDrawindMode(DrawingMode drawindMode) {
        this.drawindMode = drawindMode;
        requestRepaint();
    }

    public DrawingMode getDrawindMode() {
        return drawindMode;
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        // support other drawing modes than area
        // TODO make events fired when new object is drawn/edited
        if (variables.containsKey("vertices")) {
            String[] object = (String[]) variables.get("vertices");
            Point[] points = new Point[object.length];
            for (int i = 0; i < points.length; i++) {
                points[i] = Point.valueOf(object[i]);
            }

            if (drawindMode == DrawingMode.LINE) {
                PolyLine polyline = new PolyLine();
                polyline.setPoints(points);
                newVectorPainted(polyline);
            } else if (drawindMode == DrawingMode.AREA) {
                Area area = new Area();
                area.setPoints(points);
                newVectorPainted(area);
            } else if (drawindMode == DrawingMode.MODIFY) {
                Vector vector = (Vector) variables.get("modifiedVector");
                if (vector != null) {
                    vector.setPointsWithoutRepaint(points);
                    vectorModified(vector);
                } else {
                    Logger.getLogger(getClass().getName())
                            .severe("Vector modified event didn't provide related vector!?");
                }
            }
        }
        if (drawindMode == DrawingMode.TRANSFORM) {
            Vector vector = (Vector) variables.get("transformedVector");
            if (vector != null) {
                vector.changeVariables(source, variables);
                VectorTransformedEvent vectorTransformedEvent = new VectorTransformedEvent(this,
                        vector);
                if (variables.containsKey("scale")) {
                    vectorTransformedEvent.setScale(Double.parseDouble(variables.get("scale").toString()));
                }
                if (variables.containsKey("ratio")) {
                    vectorTransformedEvent.setRatio(Double.parseDouble(variables.get("ratio").toString()));
                }
                if (variables.containsKey("rotation")) {
                    vectorTransformedEvent.setRotation(Double.parseDouble(variables.get("rotation").toString()));
                }
                // if rect/circle
                Bounds bounds = new Bounds(Double.parseDouble(variables.get("bound_top").toString()),
                        Double.parseDouble(variables.get("bound_left").toString()),
                        Double.parseDouble(variables.get("bound_bottom").toString()),
                        Double.parseDouble(variables.get("bound_right").toString()));
                vectorTransformedEvent.setBounds(bounds);
                fireEvent(vectorTransformedEvent);
            } else {
                Logger.getLogger(getClass().getName())
                        .severe("Vector transformed event didn't provide related vector!?");
            }
        }
        if (drawindMode == DrawingMode.POINT && variables.containsKey("x")) {
            Double x = (Double) variables.get("x");
            Double y = (Double) variables.get("y");
            PointVector point = new PointVector(x, y);
            newVectorPainted(point);
        }
        if (variables.containsKey("vusel")) {
            Vector object = (Vector) variables.get("vusel");
            if (selectedVector == object) {
                selectedVector = null;
            }
            VectorUnSelectedEvent vectorUnselectedEvent = new VectorUnSelectedEvent(
                    this, object);
            fireEvent(vectorUnselectedEvent);
        }
        if (variables.containsKey("vsel")) {
            Vector object = (Vector) variables.get("vsel");
            selectedVector = object;
            VectorSelectedEvent vectorSelectedEvent = new VectorSelectedEvent(
                    this, object);
            fireEvent(vectorSelectedEvent);
        }
    }

    private void vectorModified(Vector object2) {
        VectorModifiedEvent vectorModifiedEvent = new VectorModifiedEvent(this,
                object2);
        fireEvent(vectorModifiedEvent);
    }

    protected void newVectorPainted(Vector vector) {
        VectorDrawnEvent vectorDrawnEvent = new VectorDrawnEvent(this, vector);
        fireEvent(vectorDrawnEvent);
        requestRepaint();
    }

    public interface VectorDrawnListener {

        public final Method method = ReflectTools.findMethod(
                VectorDrawnListener.class, "vectorDrawn",
                VectorDrawnEvent.class);

        public void vectorDrawn(VectorDrawnEvent event);

    }

    public void addListener(VectorDrawnListener listener) {
        addListener(VectorDrawnEvent.class, listener,
                VectorDrawnListener.method);
    }

    public void removeListener(VectorDrawnListener listener) {
        removeListener(VectorDrawnEvent.class, listener,
                VectorDrawnListener.method);
    }

    public interface VectorModifiedListener {

        public final Method method = ReflectTools.findMethod(
                VectorModifiedListener.class, "vectorModified",
                VectorModifiedEvent.class);

        public void vectorModified(VectorModifiedEvent event);

    }

    public void addListener(VectorModifiedListener listener) {
        addListener(VectorModifiedEvent.class, listener,
                VectorModifiedListener.method);
    }

    public void removeListener(VectorModifiedListener listener) {
        removeListener(VectorModifiedEvent.class, listener,
                VectorModifiedListener.method);
    }

    public interface VectorTransformedListener {

        public final Method method = ReflectTools.findMethod(
                VectorTransformedListener.class, "vectorTransformed",
                VectorTransformedEvent.class);

        public void vectorTransformed(VectorTransformedEvent event);

    }

    public void addListener(VectorTransformedListener listener) {
        addListener(VectorTransformedEvent.class, listener,
                VectorTransformedListener.method);
    }

    public void removeListener(VectorTransformedListener listener) {
        removeListener(VectorTransformedEvent.class, listener,
                VectorTransformedListener.method);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return the stylemap
     */
    public StyleMap getStyleMap() {
        return stylemap;
    }

    /**
     * @param stylemap
     *            the stylemap to set
     */
    public void setStyleMap(StyleMap stylemap) {
        this.stylemap = stylemap;
        requestRepaint();
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
        requestRepaint();
    }

    public void setHighlightMode(HighlightMode highlightMode) {
        this.highlightMode = highlightMode;
        requestRepaint();
    }

    public SelectionMode getSelectionMode() {
        return selectionMode;
    }

    private abstract class VectorEvent extends Event {
        private Vector vector;

        public VectorEvent(Component source, Vector vector) {
            super(source);
            setVector(vector);
        }

        private void setVector(Vector vector) {
            this.vector = vector;
        }

        public Vector getVector() {
            return vector;
        }
}
    public class VectorDrawnEvent extends VectorEvent {

        public VectorDrawnEvent(Component source, Vector vector) {
            super(source,vector);
        }
    }

    public class VectorModifiedEvent extends VectorEvent {

        public VectorModifiedEvent(Component source, Vector vector) {
            super(source,vector);
        }
    }

    public class VectorTransformedEvent extends VectorEvent {

        private double scale;
        private double ratio;
        private double rotation;
        private Bounds bounds;

        public VectorTransformedEvent(Component source, Vector vector) {
            super(source,vector);
        }

        public double getRatio() {
            return ratio;
        }

        public void setRatio(double ratio) {
            this.ratio = ratio;
        }

        public double getRotation() {
            return rotation;
        }

        public void setRotation(double rotation) {
            this.rotation = rotation;
        }

        public double getScale() {
            return scale;
        }

        public void setScale(double scale) {
            this.scale = scale;
        }

        public void setBounds(Bounds bounds) {
            this.bounds = bounds;
        }

        public Bounds getBounds() {
            return bounds;
        }
    }

    public interface VectorSelectedListener {

        public final String EVENT_ID = "vsel";

        public final Method method = ReflectTools.findMethod(
                VectorSelectedListener.class, "vectorSelected",
                VectorSelectedEvent.class);

        public void vectorSelected(VectorSelectedEvent event);

    }

    public void addListener(VectorSelectedListener listener) {
        addListener(VectorSelectedListener.EVENT_ID, VectorSelectedEvent.class,
                listener, VectorSelectedListener.method);
    }

    public void removeListener(VectorSelectedListener listener) {
        removeListener(VectorSelectedListener.EVENT_ID,
                VectorSelectedEvent.class, listener);
    }

    public class VectorSelectedEvent extends VectorEvent {

        public VectorSelectedEvent(Component source, Vector vector) {
            super(source, vector);
        }
    }

    public interface VectorUnSelectedListener {

        public final String EVENT_ID = "vusel";

        public final Method method = ReflectTools.findMethod(
                VectorUnSelectedListener.class, "vectorUnSelected",
                VectorUnSelectedEvent.class);

        public void vectorUnSelected(VectorUnSelectedEvent event);

    }

    public void addListener(VectorUnSelectedListener listener) {
        addListener(VectorUnSelectedListener.EVENT_ID,
                VectorUnSelectedEvent.class, listener,
                VectorUnSelectedListener.method);
    }

    public void removeListener(VectorUnSelectedListener listener) {
        removeListener(VectorUnSelectedListener.EVENT_ID,
                VectorUnSelectedEvent.class, listener);
    }

    public Vector getSelectedVector() {
        return selectedVector;
    }

    public void setSelectedVector(Vector selectedVector) {
        if (this.selectedVector != selectedVector) {
            this.unselectedVector = this.selectedVector;
            if (this.unselectedVector != null) {
                fireEvent(new VectorUnSelectedEvent(this, this.unselectedVector));
            }
            this.selectedVector = selectedVector;
            if (this.selectedVector != null) {
                fireEvent(new VectorSelectedEvent(this, this.selectedVector));
            }
            requestRepaint();
        }
    }

    public class VectorUnSelectedEvent extends VectorEvent {

        public VectorUnSelectedEvent(Component source, Vector vector) {
            super(source,vector);
        }
    }

}