/*******************************************************************************
 * Copyright (c) 2010 Oplugins/org.csstudio.opibuilder.widgets.dxy/src/org/csstudio/opibuilder/widgets/dxy/Services.javaak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.widgets.mjpg;

import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.ComboProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.eclipse.swt.graphics.RGB;

/**The model for intensity graph.
 * @author Xihui Chen
 *
 */
public class MjpgModel extends AbstractPVWidgetModel {

    /**
     *  URL of MJPG source.
     */
    public static final String PROP_MJPG_URL = "mjpg_url"; //$NON-NLS-1$

    /**
     *  Plot title.
     */
    public static final String PROP_TITLE = "title"; //$NON-NLS-1$

    /**
     * Color map of the graph.
     */
    public static final String PROP_COLOR_MAP = "color_map"; //$NON-NLS-1$

    /**
     * Show Ramp.
     */
    public static final String PROP_SHOW_RAMP = "show_ramp"; //$NON-NLS-1$

    public static final String PROP_COLOR_DEPTH = "color_depth"; //$NON-NLS-1$

    public static final String PROP_SHOW_TOOLBAR = "show_toolbar"; //$NON-NLS-1$

    public static final String PROP_KEEP_ASPECT_RATIO = "keep_aspect_ratio"; //$NON-NLS-1$

    /**
     * The ID of this widget model.
     */
    public static final String ID = "org.csstudio.opibuilder.widgets.mjpg"; //$NON-NLS-1$

    public MjpgModel() {
        setForegroundColor(new RGB(0,0,0));
        setSize(400, 240);
        setTooltip("$(mjpg_url)"); //$NON-NLS-1$
        setPropertyValue(PROP_BORDER_ALARMSENSITIVE, false);
        setScaleOptions(true, true, true);
    }

    @Override
    protected void configureProperties() {

        addProperty(new StringProperty(PROP_MJPG_URL, "MJPG URL",
                WidgetPropertyCategory.Display, ""), false);

        addProperty(new StringProperty(PROP_TITLE, "Title",
                WidgetPropertyCategory.Display, ""), true);

        String[] availablePalettes = Palettes.getCurrent().getPalettes().toArray(new String[0]);
        addProperty(new ComboProperty(PROP_COLOR_MAP, "Color Map",
                WidgetPropertyCategory.Display, availablePalettes, 0), true);

        addProperty(new BooleanProperty(PROP_SHOW_RAMP, "Show Ramp",
                WidgetPropertyCategory.Display, true),true);

        addProperty(new BooleanProperty(PROP_SHOW_TOOLBAR, "Show Toolbar",
                WidgetPropertyCategory.Display, true),true);

        addProperty(new BooleanProperty(PROP_KEEP_ASPECT_RATIO, "Keep Aspect Ratio",
                WidgetPropertyCategory.Display, true),true);

    }

    public static String makeAxisPropID(String axisID, String propIDPre){
        return axisID+ "_" + propIDPre; //$NON-NLS-1$
    }

    @Override
    public String getTypeID() {
        return ID;
    }

    /**
     * @return the MJPG URL
     */
    public String getMjpgUrl() {
        return (String) getCastedPropertyValue(PROP_MJPG_URL);
    }

    /**
     * @return the plot title
     */
    public String getTitle() {
        return (String) getCastedPropertyValue(PROP_TITLE);
    }

    /**
     * @return whether to show the color map
     */
    public int getColorMap() {
        return (int) getCastedPropertyValue(PROP_COLOR_MAP);
    }

    /**
     * @return whether to show the ramp
     */
    public Boolean isShowRamp(){
        return (Boolean) getCastedPropertyValue(PROP_SHOW_RAMP);
    }

    public boolean isShowToolbar() {
        return (Boolean) getCastedPropertyValue(PROP_SHOW_TOOLBAR);
    }

    public boolean isKeepAspectRatio() {
        return (Boolean) getCastedPropertyValue(PROP_KEEP_ASPECT_RATIO);
    }

}
