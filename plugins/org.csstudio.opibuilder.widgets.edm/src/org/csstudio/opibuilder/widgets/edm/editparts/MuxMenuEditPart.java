package org.csstudio.opibuilder.widgets.edm.editparts;

import java.util.List;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.edm.figures.MuxMenuFigure;
import org.csstudio.opibuilder.widgets.edm.model.MuxMenuModel;
import org.csstudio.opibuilder.widgets.edm.model.MuxMenuModel.MuxProperty;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;

/**The editpart of a muxMenu.
 *
 * This is based on ComboEditPart by Xihui Chen
 * @author Nick Battam
 *
 */
public final class MuxMenuEditPart extends AbstractPVWidgetEditPart {

	private Combo combo;
	private SelectionListener comboSelectionListener;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IFigure doCreateFigure() {
		final MuxMenuModel model = getWidgetModel();
		if (model == null) {
			System.err.println("NULL model");
		}

		MuxMenuFigure comboFigure = new MuxMenuFigure(this);

		combo = comboFigure.getSWTWidget();
		if (combo == null) {
			System.err.println("NULL COMBO");
		}

		if(comboSelectionListener !=null)
			combo.removeSelectionListener(comboSelectionListener);
		
		comboSelectionListener = new MuxMenuSelectionListener();
		combo.addSelectionListener(comboSelectionListener);

		updateCombo(model.getItems());

		return comboFigure;
	}
	
	@Override
	protected void doActivate() {
		super.doActivate();
		
		setInitialSelection();
	};
	
	@Override
	protected void doDeActivate() {
		super.doDeActivate();

		if(comboSelectionListener !=null)
			combo.removeSelectionListener(comboSelectionListener);
	}

	private void setInitialSelection() {
		if (combo.getItemCount() > 0) {
			String initialState = getWidgetModel().getInitialState();

			if (initialState != null && !initialState.isEmpty()) {
				try {
					int selectedIndex = Integer.parseInt(initialState);
					combo.select(selectedIndex);
				}
				catch (NumberFormatException ex) {
					System.err.println("Invalid initial state: " + initialState);
				}
			}
			else
			{
				// Default selection is the first element
				combo.select(0);
			}
			// force a selection change event to set the associated loc:// pv
			comboSelectionListener.widgetSelected(null);
		}
	}

	private class MuxMenuSelectionListener extends SelectionAdapter {
		/// Selection change handler for the MenuMux Combobox

		@Override
		public void widgetSelected(SelectionEvent e) {
			/// On selected change put the selected PV name to the associated local pv (e.g. $d)
			MuxMenuModel model = getWidgetModel();

			int selectedIdx = combo.getSelectionIndex();
			
			for (int set_index = 0; set_index < model.getNumSets(); set_index++) {
				List<String> values = model.getValues(set_index);

				if (selectedIdx < values.size()) {
					String value = values.get(selectedIdx);
					setPVValue(MuxMenuModel.makePropId(MuxProperty.TARGET.propIDPre, set_index), value);
				}
			}
		}
	}

	/**
	 * @param items
	 */
	private void updateCombo(List<String> items) {

		if (items == null) {
			System.err.println("NULL ITEMS");			
		}
		else if(getExecutionMode() == ExecutionMode.RUN_MODE) {
			combo.removeAll();

			for(String item : items){
				combo.add(item);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MuxMenuModel getWidgetModel() {
		return (MuxMenuModel)getModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void registerPropertyChangeHandlers() {
		autoSizeWidget((MuxMenuFigure) getFigure());

		// Items
		IWidgetPropertyChangeHandler itemsHandler = new IWidgetPropertyChangeHandler() {
			@Override
			@SuppressWarnings("unchecked")
			public boolean handleChange(final Object oldValue,
					final Object newValue, final IFigure refreshableFigure) {
				if(newValue != null && newValue instanceof List){
					updateCombo((List<String>)newValue);
				}
				return true;
			}
		};
		setPropertyChangeHandler(MuxMenuModel.PROP_ITEMS, itemsHandler);

		//size change handlers--always apply the default height
		IWidgetPropertyChangeHandler handle = new IWidgetPropertyChangeHandler() {
			@Override
			public boolean handleChange(final Object oldValue, final Object newValue,
					final IFigure figure) {
				autoSizeWidget((MuxMenuFigure)figure);
				return true;
			}
		};
		setPropertyChangeHandler(AbstractWidgetModel.PROP_WIDTH, handle);
		setPropertyChangeHandler(AbstractWidgetModel.PROP_HEIGHT, handle);
		setPropertyChangeHandler(AbstractWidgetModel.PROP_BORDER_STYLE, handle);
		setPropertyChangeHandler(AbstractWidgetModel.PROP_BORDER_WIDTH, handle);
		setPropertyChangeHandler(MuxMenuModel.PROP_FONT, handle);
	}

	private void autoSizeWidget(MuxMenuFigure comboFigure) {
		Dimension d = comboFigure.getAutoSizeDimension();
		getWidgetModel().setSize(getWidgetModel().getWidth(), d.height);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getValue() {
		return combo.getText();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(Object value) {
		if(value instanceof String)
			combo.setText((String) value);
		else if (value instanceof Number)
			combo.select(((Number)value).intValue());
		else
			super.setValue(value);
	}
	
}
