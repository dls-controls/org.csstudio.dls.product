/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.widgets.mjpg;

import java.net.MalformedURLException;
import java.util.logging.Level;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.visualparts.BorderFactory;
import org.csstudio.opibuilder.visualparts.BorderStyle;
import org.eclipse.draw2d.IFigure;

/**
 * Editpart for the MJPG widget.
 *
 */
public class MjpgEditPart extends AbstractPVWidgetEditPart {;

    private MjpgFigure figure;

    @Override
    protected IFigure doCreateFigure() {
        MjpgModel model = getWidgetModel();

        figure = new MjpgFigure(this);
        figure.setURL(model.getMjpgUrl());

        try {
            figure.connect(model.getColorMap());
        } catch (MalformedURLException e) {
            Activator.getLogger().log(Level.WARNING, "URL " + model.getMjpgUrl() + " is not valid.", e);
        }
        figure.setTitle(model.getTitle());
        figure.setShowRamp(model.isShowRamp());
        figure.setShowToolbar(model.isShowToolbar());
        figure.setKeepAspectRatio(model.isKeepAspectRatio());

        return figure;
    }

    @Override
    public MjpgModel getWidgetModel() {
        return (MjpgModel)getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {

        getWidgetModel().getProperty(MjpgModel.PROP_TITLE).addPropertyChangeListener(
                event -> {
                        figure.setTitle((String)event.getNewValue());
                        figure.repaint();
                });

        getWidgetModel().getProperty(MjpgModel.PROP_BORDER_STYLE).removeAllPropertyChangeListeners();
        getWidgetModel().getProperty(MjpgModel.PROP_BORDER_STYLE).addPropertyChangeListener(
                event -> {
                        figure.setBorder(
                                BorderFactory.createBorder(BorderStyle.values()[(Integer)event.getNewValue()],
                                getWidgetModel().getBorderWidth(), getWidgetModel().getBorderColor(),
                                getWidgetModel().getName()));
                });

        getWidgetModel().getProperty(MjpgModel.PROP_BORDER_WIDTH).removeAllPropertyChangeListeners();
        getWidgetModel().getProperty(MjpgModel.PROP_BORDER_WIDTH).addPropertyChangeListener(
                event -> {
                        figure.setBorder(
                                BorderFactory.createBorder(getWidgetModel().getBorderStyle(),
                                (Integer)event.getNewValue(), getWidgetModel().getBorderColor(),
                                getWidgetModel().getName()));
                });

        getWidgetModel().getProperty(MjpgModel.PROP_SHOW_RAMP).addPropertyChangeListener(
                event -> ((MjpgFigure)getFigure()).setShowRamp((Boolean)event.getNewValue())
        );

        getWidgetModel().getProperty(MjpgModel.PROP_SHOW_TOOLBAR).addPropertyChangeListener(
                event -> ((MjpgFigure)getFigure()).setShowToolbar((Boolean)event.getNewValue())
        );

        getWidgetModel().getProperty(MjpgModel.PROP_KEEP_ASPECT_RATIO).addPropertyChangeListener(
                event -> ((MjpgFigure)getFigure()).setKeepAspectRatio((Boolean)event.getNewValue())
        );

    }

    @Override
    public void deactivate() {
        ((MjpgFigure)getFigure()).dispose();
        super.deactivate();
    }

}
