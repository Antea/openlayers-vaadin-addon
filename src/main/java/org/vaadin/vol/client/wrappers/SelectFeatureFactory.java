package org.vaadin.vol.client.wrappers;

import java.util.HashMap;

import org.vaadin.vol.client.wrappers.control.SelectFeature;
import org.vaadin.vol.client.wrappers.layer.Layer;

import com.google.gwt.core.client.JsArray;
import java.util.Collection;
import java.util.HashSet;

/**
 * Factory to create new SelectFeature instances or deliver existing ones
 * @author eiko
 *
 */
public final class SelectFeatureFactory {

    private static SelectFeatureFactory inst = null;
    private java.util.Map<String, SelectFeatureContainer> selectFeatureControls =
            new HashMap<String, SelectFeatureContainer>();

    private SelectFeatureFactory() {
    }

    public static SelectFeatureFactory getInst() {
        if (inst == null) {
            inst = new SelectFeatureFactory();
        }
        return inst;
    }

    /**
     * return an existing SelectFeature instance for the specific id or create a new one
     * @param selectionCtrlId
     * @param map
     * @param targetLayer
     * @return
     */
    public SelectFeature getOrCreate(String selectionCtrlId, Map map, Layer targetLayer) {
        if (selectionCtrlId == null) {
            SelectFeature control = SelectFeature.create(targetLayer);
            map.addControl(control);
            control.activate();
            return control;
        } else {
            SelectFeatureContainer container = selectFeatureControls.get(selectionCtrlId);
            if (container == null) {
                container = new SelectFeatureContainer(targetLayer, map);
                selectFeatureControls.put(selectionCtrlId, container);
                return container.selectFeature;
            } else {
                container.addLayer(targetLayer);
                return container.selectFeature;
            }
        }
    }

    public void removeLayer(SelectFeature control, String selectionCtrlId, Map map,
            Layer targetLayer) {
        if (selectionCtrlId == null) {
            control.deActivate();
            map.removeControl(control);
        } else {
            SelectFeatureContainer cont = selectFeatureControls.get(selectionCtrlId);
            cont.removeLayer(targetLayer, map);
            if (cont.isEmpty()) {
                selectFeatureControls.remove(selectionCtrlId);
            }
        }
    }

    class SelectFeatureContainer {

        private Collection<Layer> layers = new HashSet<Layer>();
        private SelectFeature selectFeature;

        SelectFeatureContainer(Layer targetLayer, Map map) {
            layers.add(targetLayer);
            selectFeature = SelectFeature.create(targetLayer);
            map.addControl(selectFeature);
            selectFeature.activate();
        }

        void addLayer(Layer targetLayer) {
            layers.add(targetLayer);
            updateLayersInControl();
        }

        void removeLayer(Layer targetLayer, Map map) {
            if (layers.contains(targetLayer)) {
                layers.remove(targetLayer);
                updateLayersInControl();
                if (layers.isEmpty()) {
                    selectFeature.deActivate();
                    map.removeControl(selectFeature);
                    this.selectFeature = null;
                }
            }
        }

        boolean isEmpty() {
            return layers.isEmpty();
        }

        private void updateLayersInControl() {
            // OpenLayers documentation tells for this function: Attach a new layer to the control,
            // overriding any existing layers.
            // So reinit with all to this point registrated layers
            JsArray<Layer> layerArray = (JsArray<Layer>) JsArray.createArray();
            for (Layer layer : layers) {
                layerArray.push(layer);
            }
            selectFeature.setLayer(layerArray);
        }
    }
}
