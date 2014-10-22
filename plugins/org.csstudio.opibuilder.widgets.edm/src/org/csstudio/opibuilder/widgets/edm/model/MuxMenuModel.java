package org.csstudio.opibuilder.widgets.edm.model;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.StringListProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.widgets.model.ComboModel;
import org.eclipse.swt.graphics.RGB;

public class MuxMenuModel extends ComboModel {


	public final String ID = "org.csstudio.opibuilder.widgets.edm.muxmenu";//$NON-NLS-1$
	/**
	 * Items of the combo.
	 * 	items: the displayed text
	 *  targets: the target (loc) PVs
	 *  values: the PV names to forward to the target
	 */
	public static final String PROP_ITEMS = "items";//$NON-NLS-1$
	public static final String PROP_TARGETS = "targets";//$NON-NLS-1$
	public static final String PROP_VALUES = "values";//$NON-NLS-1$
	public static final String PROP_INITIAL_STATE = "initial";//$NON-NLS-1$

	public MuxMenuModel() {
		setBackgroundColor(new RGB(255,255,255));
		setForegroundColor(new RGB(0,0,0));
		setScaleOptions(true, false, false);
	}

	@Override
	protected void configureProperties() {
		addProperty(new StringListProperty(
				PROP_ITEMS, "Items", WidgetPropertyCategory.Behavior, new ArrayList<String>()));

		addProperty(new StringListProperty(
				PROP_VALUES, "Values", WidgetPropertyCategory.Behavior, new ArrayList<String>()));

		addProperty(new StringListProperty(
				PROP_TARGETS, "Targets", WidgetPropertyCategory.Behavior, new ArrayList<String>()));

		addProperty(new StringProperty(
				PROP_INITIAL_STATE, "Initial State", WidgetPropertyCategory.Behavior, ""));
	}

	@SuppressWarnings("unchecked")
	public List<String> getValues(){
		return (List<String>)getPropertyValue(PROP_VALUES);
	}

	@SuppressWarnings("unchecked")
	public List<String> getTargets(){
		return (List<String>)getPropertyValue(PROP_TARGETS);
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
}
