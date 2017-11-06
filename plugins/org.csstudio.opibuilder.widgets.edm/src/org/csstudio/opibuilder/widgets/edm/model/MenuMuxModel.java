package org.csstudio.opibuilder.widgets.edm.model;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.properties.IntegerProperty;
import org.csstudio.opibuilder.properties.NameDefinedCategory;
import org.csstudio.opibuilder.properties.PVNameProperty;
import org.csstudio.opibuilder.properties.PVValueProperty;
import org.csstudio.opibuilder.properties.StringListProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.widgets.model.ComboModel;
import org.eclipse.swt.graphics.RGB;

public class MenuMuxModel extends ComboModel {

    public static final int MAX_SETS = 8;

    public enum MuxProperty{

        VALUES("values", "Values"),//$NON-NLS-1$
        TARGET("target", "Target");//$NON-NLS-1$

        public String propIDPre;
        public String description;

        private MuxProperty(String propertyIDPrefix, String description) {
            this.propIDPre = propertyIDPrefix;
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public final String ID = "org.csstudio.opibuilder.widgets.edm.menumux";//$NON-NLS-1$
    /**
     * Items of the combo.
     *     items: the displayed text
     *  targets: the target (loc) PVs
     *  values: the PV names to forward to the target
     */
    public static final String PROP_ITEMS = "items";//$NON-NLS-1$
    public static final String PROP_INITIAL_STATE = "initial";//$NON-NLS-1$

    public static final String PROP_NUM_SETS = "num_sets";//$NON-NLS-1$

    public MenuMuxModel() {
        setBackgroundColor(new RGB(255,255,255));
        setForegroundColor(new RGB(0,0,0));
        setScaleOptions(true, false, false);
    }

    @Override
    protected void configureProperties() {
        addProperty(new StringListProperty(
                PROP_ITEMS, "Items", WidgetPropertyCategory.Misc, new ArrayList<String>()));

        addProperty(new IntegerProperty(
                PROP_NUM_SETS, "Number of value sets", WidgetPropertyCategory.Misc, 1, 0, MAX_SETS));

        addProperty(new StringProperty(
                PROP_INITIAL_STATE, "Initial State", WidgetPropertyCategory.Misc, ""));


        addMuxProperties();
    }

    private void addMuxProperties() {
        for (int set_index = 0; set_index < MAX_SETS; set_index++) {
            WidgetPropertyCategory category = new NameDefinedCategory("Set " + set_index);
            for(MuxProperty muxProperty : MuxProperty.values()){
                addMuxProperty(muxProperty, set_index, category);
            }
        }
    }

    protected void addMuxProperty(MuxProperty property, int set_index, WidgetPropertyCategory category) {
        String propId = makePropId(property.propIDPre, set_index);
        switch (property) {
        case TARGET:
            addPVProperty(new PVNameProperty(propId, property.description, category, ""),
                          new PVValueProperty(MenuMuxModel.makePvPropId(property.propIDPre, set_index), null));
            break;
        case VALUES:
            addProperty(new StringListProperty(propId, property.description, category, new ArrayList<String>()));
            break;
        }
    }

    static public String makePropId(String prefix, int index) {
        return prefix + index;
    }
    static protected String makePvPropId(String prefix, int index) {
        return prefix + index + "_pv";
    }

    @SuppressWarnings("unchecked")
    public List<String> getValues(int index){
        return (List<String>)getPropertyValue(MenuMuxModel.makePropId(MuxProperty.VALUES.propIDPre, index));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getItems(){
        return (List<String>)getPropertyValue(PROP_ITEMS);
    }

    public String getInitialState(){
        return (String)getPropertyValue(PROP_INITIAL_STATE);
    }

    @Override
    public String getTypeID() {
        return ID;
    }

    public int getNumSets() {
        return (int)getPropertyValue(PROP_NUM_SETS);
    }
}
