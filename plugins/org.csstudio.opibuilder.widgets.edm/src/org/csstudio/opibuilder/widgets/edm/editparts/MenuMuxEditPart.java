package org.csstudio.opibuilder.widgets.edm.editparts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.editparts.PVWidgetEditpartDelegate;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.edm.figures.MenuMuxFigure;
import org.csstudio.opibuilder.widgets.edm.model.MenuMuxModel;
import org.csstudio.opibuilder.widgets.edm.model.MenuMuxModel.MuxProperty;
import org.csstudio.simplepv.IPV;
import org.csstudio.simplepv.IPVListener;
import org.csstudio.simplepv.VTypeHelper;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.diirt.vtype.VType;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

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
    private volatile AtomicBoolean lastWriteAccess;
    private Map<IPV, IPVListener> targetPVsListenerMap = new HashMap<IPV, IPVListener>();
    private static final Logger LOGGER = Logger.getLogger(MenuMuxEditPart.class.getName());


    private final class WidgetPVListener extends IPVListener.Stub{

        @Override
        public void connectionChanged(IPV pv) {
            if(!pv.isConnected())
                lastWriteAccess = null;
            updateControlEnabling();
        }

        @Override
        public void writePermissionChanged(IPV pv) {
            updateControlEnabling();
        }
    }

    /**
     * Update the control widget enabling based on the writable
     * status of all attached PVs
     *
     * Control is disabled if ANY of the PVs (target + control) are
     * not writable. This ensures you cannot get into an inconsistent
     * state where some attached PVs update and other don't.
     */
    private void updateControlEnabling() {

        boolean allWritable = allPVsWritable();

        if(lastWriteAccess == null || lastWriteAccess.get() != allWritable){
            if(lastWriteAccess == null)
                lastWriteAccess= new AtomicBoolean();

            lastWriteAccess.set(allWritable);

            if(lastWriteAccess.get()){
                UIBundlingThread.getInstance().addRunnable(
                        getViewer().getControl().getDisplay(),new Runnable(){
                    @Override
                    public void run() {
                        setControlEnabled(true);
                    }
                });
            } else {
                UIBundlingThread.getInstance().addRunnable(
                        getViewer().getControl().getDisplay(),new Runnable(){
                    @Override
                    public void run() {
                        setControlEnabled(false);
                    }
                });
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IFigure doCreateFigure() {
        final MenuMuxModel model = getWidgetModel();
        if (model == null) {
            LOGGER.log(Level.SEVERE, "NULL model");
        }

        MenuMuxFigure comboFigure = new MenuMuxFigure(this);

        combo = comboFigure.getSWTWidget();
        if (combo == null) {
            LOGGER.log(Level.SEVERE, "NULL swt widget");
        }

        if(comboSelectionListener !=null)
            combo.removeSelectionListener(comboSelectionListener);

        comboSelectionListener = new MuxMenuSelectionListener();
        combo.addSelectionListener(comboSelectionListener);
        updateCombo(model.getItems());

        // Do NOT mark the PVName as a controlPV; this leads to a race condition between the
        // 'all PVs writable' implemented in this class and the WidgetPVListener in the
        // PVWidgetEditpartDelegate.

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
    protected void doActivate() {
        super.doActivate();

        createListeners();
    }

    /**
     * Create listeners on the target and control PVs responding
     * to connection and writePermission changes
     */
    private void createListeners() {
        if(getExecutionMode() == ExecutionMode.RUN_MODE){
            MenuMuxModel model = getWidgetModel();
            // control PV
            addListener(delegate.getPV());
            // target PVs
            for (int setIndex = 0; setIndex < model.getNumSets(); setIndex++) {
                String propId = MenuMuxModel.makePropId(MuxProperty.TARGET.propIDPre, setIndex);
                addListener(delegate.getPV(propId));
            }
        }
    }

    private void addListener(IPV pv) {
        if (pv != null) {
            WidgetPVListener pvListener = new WidgetPVListener();
            pv.addListener(pvListener);
            targetPVsListenerMap.put(pv, pvListener);
        }
    }

    @Override
    protected void doDeActivate() {
        super.doDeActivate();

        if(comboSelectionListener !=null) {
            combo.removeSelectionListener(comboSelectionListener);
        }

        targetPVsListenerMap.clear();
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
                    LOGGER.log(Level.WARNING, "Invalid initial state: " + initialState, ex);
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


    /** Check the writable status of ALL PVs associated with this widget:
     *          pvName
     *          targetN [N=0,..,model.MAX_SETS]
     *
     * @return TRUE if all pvs are writable, false otherwise
     */
    private boolean allPVsWritable() {
        boolean writable = true;

        IPV pv = getPV();
        if (pv != null && !pv.isWriteAllowed()) {
            writable = false;
        }
        else {
            MenuMuxModel model = getWidgetModel();
            PVWidgetEditpartDelegate delegate = getPVWidgetEditpartDelegate();
            for (int setIndex = 0; setIndex < model.getNumSets(); setIndex++) {
                pv = delegate.getPV(MenuMuxModel.makePropId(MuxProperty.TARGET.propIDPre, setIndex));

                if (pv != null && !pv.isWriteAllowed()) {
                    writable = false;
                    break;
                }
            }
        }
        return writable;
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

            if (e.stateMask == SWT.BUTTON1 || e.data != null) {
                /// On selected change put the selected PV name to the associated local pv (e.g. $d)
                int selectedIdx = combo.getSelectionIndex();
                oldSelectedIndex = selectedIdx;

                if (selectedIdx > -1) {
                    // Write the index to the control PV.
                    setPVValue(MenuMuxModel.PROP_PVNAME, selectedIdx);
                    // cache the selection to manage scroll-wheel use
                    updateTargetPVs(selectedIdx);
                }
                else {
                    LOGGER.log(Level.WARNING, "Target PVs not updated: no item selected");
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
     * Trigger updates of all target PVs to the correct
     * element of their value list
     *
     * @param selectedIdx
     */
    private void updateTargetPVs(final int selectedIdx) {

        MenuMuxModel model = getWidgetModel();

        for (int setIndex = 0; setIndex < model.getNumSets(); setIndex++) {
            List<String> values = model.getValues(setIndex);
            String propId = MenuMuxModel.makePropId(MuxProperty.TARGET.propIDPre, setIndex);
            if (selectedIdx < values.size()) {
                String value = values.get(selectedIdx);
                setPVValue(propId, value);
            }
            else {
                IPV pv = delegate.getPV(propId);
                if (pv != null) {
                    // only log a warning if there's a PV defined for this set
                    LOGGER.log(Level.WARNING, String.format(
                            "Target PV %s (%d) not updated: no value for selected index %d",
                            pv.getName(), setIndex, selectedIdx));
                }
            }

        }
    }

    /**
     * @param items
     */
    private void updateCombo(List<String> items) {

        if (items == null) {
            LOGGER.log(Level.SEVERE, "NULL combo items");
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

        // PV_Value
        IWidgetPropertyChangeHandler pvhandler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(final Object oldValue,
                    final Object newValue, final IFigure refreshableFigure) {
                if(newValue != null){
                    Number val = VTypeHelper.getNumber((VType)newValue);
                    if (val != null) {
                        setValue(val);
                    }
                }

                return true;
            }
        };
        setPropertyChangeHandler(MenuMuxModel.PROP_PVVALUE, pvhandler);
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
    public void setValue(final Object value) {
        int cache = oldSelectedIndex;
        if(value instanceof String) {
            //TODO: is this code block required?
            LOGGER.log(Level.INFO, "Updating menumux via String path " + getPVName());

            combo.setText((String) value);  // ignores value not in value list
            oldSelectedIndex = combo.getSelectionIndex();

            if (combo.getText() != value) {
                LOGGER.log(Level.WARNING, "Unrecognised value set on control PV: " + getPVName() + " (" + value + ")");
                combo.deselectAll();
                oldSelectedIndex = -1;
            }
        }
        else if (value instanceof Number) {
            int selectedIndex = ((Number)value).intValue();
            combo.select(selectedIndex);  // ignores out-of-range indices
            oldSelectedIndex = selectedIndex;

            // verify the combobox selected index matches the expected
            // value: if not it means the selection set in the PV is invalid
            if (oldSelectedIndex != combo.getSelectionIndex()) {
                LOGGER.log(Level.WARNING, "Unrecognised value set on control PV: " + getPVName() + " (" + value + ")");
                combo.deselectAll();
                oldSelectedIndex = -1;
            }
        }
        else {
            // this raises an exception
            super.setValue(value);
        }

        if (cache != oldSelectedIndex) {
            Event evt = new Event();
            evt.data = this; // pass some non-null data to trigger update
            combo.notifyListeners(SWT.Selection, evt);
        }
    }

}
