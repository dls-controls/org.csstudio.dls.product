/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.widgets.dawn.actions;

import org.csstudio.opibuilder.commands.SetWidgetPropertyCommand;
import org.csstudio.opibuilder.widgets.dawn.xygraph.DawnXYGraphModel;
import org.csstudio.opibuilder.widgets.model.XYGraphModel;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IAction;


/**Show/Hide XYGraph Toolbar
 * @author Matthew Furseman
 *
 */
public class ShowXYGraphToolbarAction extends AbstractXYGraphTargetAction {

    public void run(IAction action) {
        DawnXYGraphModel model =  getSelectedXYGraph().getWidgetModel();
        Command command = new SetWidgetPropertyCommand(
                model, XYGraphModel.PROP_SHOW_TOOLBAR, !model.isShowToolbar());
        execute(command);
    }
}
