package org.csstudio.opibuilder.widgets.dawn.actions;

import org.csstudio.opibuilder.actions.AbstractWidgetTargetAction;
import org.csstudio.opibuilder.widgets.dawn.xygraph.DawnXYGraphEditPart;

public abstract class AbstractXYGraphTargetAction extends AbstractWidgetTargetAction {

    /**
     * Gets the widget models of all currently selected EditParts.
     *
     * @return the currently selected DawnXYGraphEditPart
     */
    protected final DawnXYGraphEditPart getSelectedXYGraph() {
        return (DawnXYGraphEditPart)selection.getFirstElement();
    }
}
