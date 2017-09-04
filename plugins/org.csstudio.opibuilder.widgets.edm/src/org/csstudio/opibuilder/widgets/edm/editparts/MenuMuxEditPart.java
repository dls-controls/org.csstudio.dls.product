package org.csstudio.opibuilder.widgets.edm.editparts;

import java.util.List;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.edm.figures.MenuMuxFigure;
import org.csstudio.opibuilder.widgets.edm.model.MenuMuxModel;
import org.csstudio.opibuilder.widgets.edm.model.MenuMuxModel.MuxProperty;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;

/**The editpart of a muxMenu.
 *
 * This is based on ComboEditPart by Xihui Chen
 * @author Nick Battam
 *
 */
public final class MenuMuxEditPart extends AbstractPVWidgetEditPart {

    private int oldSelectedIndex;
    private Combo combo;
    private SelectionListener comboSelectionListener;

    /**
     * {@inheritDoc}
     */
    @Override
    protected IFigure doCreateFigure() {
        final MenuMuxModel model = getWidgetModel();
        if (model == null) {
            System.err.println("NULL model");
        }

        MenuMuxFigure comboFigure = new MenuMuxFigure(this);

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
    public void activate() {
        super.activate();

        // Delay setting initialisation until local PVs have started.
        // It would be preferable to be able to queue this on the same
        // thread that starts the PVs, but I'm not sure this is possible.
        int DELAY = 100; // milliseconds
        Display.getCurrent().timerExec(DELAY, new Runnable() {
            @Override
            public void run() {
                setInitialSelection();
            }
        });
    }

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
                    oldSelectedIndex = selectedIndex;
                }
                catch (NumberFormatException ex) {
                    System.err.println("Invalid initial state: " + initialState);
                }
            }
            else
            {
                // Default selection is the first element
                combo.select(0);
                oldSelectedIndex = 0;
            }
            // force a selection change event to set the associated loc:// pv
            comboSelectionListener.widgetSelected(null);
        }
    }

    private class MuxMenuSelectionListener extends SelectionAdapter {
        /// Selection change handler for the MenuMux Combobox

        @Override
        public void widgetSelected(SelectionEvent e) {
            /// Only react if the selection was accomplished by clicking on an item
            /// See https://github.com/ControlSystemStudio/cs-studio/issues/2276
            /// for equivalent change to ComboBox widget.
            if (e == null)
                return;

            if (e.stateMask == SWT.BUTTON1) {
                /// On selected change put the selected PV name to the associated local pv (e.g. $d)
                MenuMuxModel model = getWidgetModel();

                int selectedIdx = combo.getSelectionIndex();
                // Write the index to the control PV.
                setPVValue(MenuMuxModel.PROP_PVNAME, selectedIdx);
                // cache the selection to manage scroll-wheel use
                oldSelectedIndex = selectedIdx;

                for (int set_index = 0; set_index < model.getNumSets(); set_index++) {
                    List<String> values = model.getValues(set_index);

                    if (selectedIdx < values.size()) {
                        String value = values.get(selectedIdx);
                        setPVValue(MenuMuxModel.makePropId(MuxProperty.TARGET.propIDPre, set_index), value);
                    }
                }
            }
            else {
                // Ignore selections from mouse wheel (stateMask == 0).
                // Unfortunately this also ignores selections via keyboard.

                // Restore current value to UI.
                combo.select(oldSelectedIndex);
                // block further process of this selection event
                e.doit = false;
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
    public MenuMuxModel getWidgetModel() {
        return (MenuMuxModel)getModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerPropertyChangeHandlers() {
        autoSizeWidget((MenuMuxFigure) getFigure());

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
        setPropertyChangeHandler(MenuMuxModel.PROP_ITEMS, itemsHandler);

        //size change handlers--always apply the default height
        IWidgetPropertyChangeHandler handle = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(final Object oldValue, final Object newValue,
                    final IFigure figure) {
                autoSizeWidget((MenuMuxFigure)figure);
                return true;
            }
        };
        setPropertyChangeHandler(AbstractWidgetModel.PROP_WIDTH, handle);
        setPropertyChangeHandler(AbstractWidgetModel.PROP_HEIGHT, handle);
        setPropertyChangeHandler(AbstractWidgetModel.PROP_BORDER_STYLE, handle);
        setPropertyChangeHandler(AbstractWidgetModel.PROP_BORDER_WIDTH, handle);
        setPropertyChangeHandler(MenuMuxModel.PROP_FONT, handle);
    }

    private void autoSizeWidget(MenuMuxFigure comboFigure) {
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
        if(value instanceof String) {
            combo.setText((String) value);
            oldSelectedIndex = combo.getSelectionIndex();
        }
        else if (value instanceof Number) {
            int selectedIndex = ((Number)value).intValue();
            combo.select(selectedIndex);
            oldSelectedIndex = selectedIndex;
        }
        else {
            super.setValue(value);
        }
    }

}
