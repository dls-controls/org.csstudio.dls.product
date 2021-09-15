package org.csstudio.opibuilder.widgets.edm.figures;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.widgets.edm.Activator;
import org.csstudio.opibuilder.widgets.edm.model.EdmSymbolModel;
import org.csstudio.swt.widgets.util.AbstractInputStreamRunnable;
import org.csstudio.swt.widgets.util.IJobErrorHandler;
import org.csstudio.swt.widgets.util.ResourceUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.graphics.Image;

public class EdmSymbolFigure extends Figure {

    private int subImageWidth = 10;
    private int subImageSelection = 0;
    private Image image;
    private static Map<String, Image> imageCache;
    private EdmSymbolModel model;

    public EdmSymbolFigure() {
        this(null);
    }

    public EdmSymbolFigure(EdmSymbolModel model) {
        super();
        this.model = model;
        if(imageCache == null) imageCache = new HashMap<String, Image>();
        setImage(model.getFilename());
    }

    @Override
    protected void paintClientArea(Graphics graphics) {
        super.paintClientArea(graphics);
        if (image != null) {
            // If what were trying to draw is out of bounds, simply draw the full image to
            // fill the area
            if (subImageSelection * subImageWidth < 0 || getClientArea().x < 0 || getClientArea().y < 0
                    || getClientArea().width < 0 || getClientArea().height < 0) {
                graphics.fillRectangle(getClientArea().x, getClientArea().y, getClientArea().width,
                        getClientArea().height);
            }
            // If the subImage width is greater than the image width, just show entire
            // image.
            else if ((1 + subImageSelection) * subImageWidth >= image.getBounds().width) {
                graphics.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, getClientArea().x,
                        getClientArea().y, getClientArea().width, getClientArea().height);
                model.setSubImageWidth(image.getBounds().width);
            } else {
                graphics.drawImage(image, subImageSelection * subImageWidth, 0, subImageWidth, image.getBounds().height,
                        getClientArea().x, getClientArea().y, getClientArea().width, getClientArea().height);
            }
        }
    }

    public synchronized void setImage(final IPath path) {
        if(path != null && !path.isEmpty())
            image = imageCache.get(path.toString());
        if(image == null) {
            AbstractInputStreamRunnable uiTask = new AbstractInputStreamRunnable() {
                public void runWithInputStream(InputStream stream) {
                    synchronized (EdmSymbolFigure.this) {
                        image = new Image(null, stream);
                        imageCache.put(path.makeAbsolute().toString(), image);
                    }
                }
            };
            ResourceUtil.pathToInputStreamInJob(path, uiTask, "Loading Image...", new IJobErrorHandler() {
                public void handleError(Exception exception) {
                    System.out.println("Warning: " + exception);
                    if (model.getExecutionMode() == ExecutionMode.RUN_MODE) {
                        // Do not draw any image if in Runtime mode
                        image = null;
                    } else {
                        Activator activator = Activator.getDefault();
                        image = activator.getImageDescriptor("icon/symbol.png").createImage();
                        subImageWidth = image.getBounds().width;
                        model.setSubImageWidth(image.getBounds().width);
                    }
                }
            });
        }
        repaint();
    }

    public void setSubImageWidth(int width) {
        this.subImageWidth = width;
        repaint();
    }

    public void setSubImageSelection(int imageNum) {
        this.subImageSelection = imageNum;
        repaint();
    }
}
