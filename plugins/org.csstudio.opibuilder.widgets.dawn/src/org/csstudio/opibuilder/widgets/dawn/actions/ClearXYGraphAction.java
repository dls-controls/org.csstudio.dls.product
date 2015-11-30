/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.widgets.dawn.actions;

import org.csstudio.opibuilder.actions.AbstractWidgetTargetAction;
import org.csstudio.opibuilder.widgets.dawn.xygraph.DawnXYGraphEditPart;
import org.eclipse.jface.action.IAction;

/**Clear XY Graph.
 * @author Xihui Chen
 *
 */
public class ClearXYGraphAction extends AbstractWidgetTargetAction {

    public void run(IAction action) {
        getSelectedXYGraph().clearGraph();

    }

    /**
     * Gets the widget models of all currently selected EditParts.
     *
     * @return the currently selected DawnXYGraphEditPart
     */
    protected final DawnXYGraphEditPart getSelectedXYGraph() {
        return (DawnXYGraphEditPart)selection.getFirstElement();
    }
}
