package org.csstudio.opibuilder.widgets.edm.editparts;

import java.util.logging.Logger;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.widgets.edm.figures.EdmSymbolFigure;
import org.csstudio.opibuilder.widgets.edm.model.EdmSymbolModel;
import org.diirt.vtype.Alarm;
import org.diirt.vtype.AlarmSeverity;
import org.diirt.vtype.VEnum;
import org.diirt.vtype.VNumber;
import org.eclipse.core.runtime.IPath;
import org.eclipse.draw2d.IFigure;


public class EdmSymbolEditpart extends AbstractPVWidgetEditPart {

    private static Logger log = Logger.getLogger(EdmSymbolEditpart.class.getName());

    @Override
    protected IFigure doCreateFigure() {
        EdmSymbolModel model = (EdmSymbolModel) getModel();
        EdmSymbolFigure figure = new EdmSymbolFigure(model.getFilename());
        figure.setSubImageSelection(0);
        figure.setSubImageWidth(model.getSubImageWidth());
        return figure;
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        // changes to the filename property
        IWidgetPropertyChangeHandler handler = new IWidgetPropertyChangeHandler() {
            public boolean handleChange(final Object oldValue, final Object newValue, final IFigure figure) {
                EdmSymbolFigure imageFigure = (EdmSymbolFigure) figure;
                IPath absolutePath = (IPath)newValue;
                if(!absolutePath.isAbsolute()) {
                    absolutePath = ResourceUtil.buildAbsolutePath(getWidgetModel(), absolutePath);
                }
                imageFigure.setImage(absolutePath);
                return false;
            }
        };
        setPropertyChangeHandler(EdmSymbolModel.PROP_EDM_IMAGE_FILE, handler);

        // changes to sub image width property
        handler = new IWidgetPropertyChangeHandler() {
            public boolean handleChange(final Object oldValue, final Object newValue, final IFigure figure) {
                EdmSymbolFigure imageFigure = (EdmSymbolFigure) figure;
                Double val = (Double) newValue;
                imageFigure.setSubImageWidth(val.intValue());
                return false;
            }
        };
        setPropertyChangeHandler(EdmSymbolModel.PROP_SUB_IMAGE_WIDTH, handler);

        // changes to PV value
        handler = new IWidgetPropertyChangeHandler() {
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                if(newValue == null) return false;
                int selection = 0;
                if(newValue instanceof Alarm) {
                    // If PV value is not valid leave index as 0 (typically invalid)
                    if(((Alarm) newValue).getAlarmSeverity() != AlarmSeverity.INVALID) {
                        if (newValue instanceof VNumber) {
                            selection = ((VNumber) newValue).getValue().intValue();
                        } else if (newValue instanceof VEnum) {
                            selection = ((VEnum) newValue).getIndex();
                        } else {
                            log.warning("VType " + newValue + " cannot be handled by EDM Symbol widget.");
                        }
                    }
                } else {
                    log.warning("Object " + newValue + " cannot be handled by EDM Symbol widget.");
                }
                EdmSymbolFigure edmFigure = (EdmSymbolFigure) figure;
                edmFigure.setSubImageSelection(selection);
                return false;
            }
        };
        setPropertyChangeHandler(AbstractPVWidgetModel.PROP_PVVALUE, handler);
    }

}
