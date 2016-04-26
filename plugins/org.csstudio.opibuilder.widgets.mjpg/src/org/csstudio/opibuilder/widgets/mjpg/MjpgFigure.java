package org.csstudio.opibuilder.widgets.mjpg;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.widgets.figures.AbstractSWTWidgetFigure;
import org.csstudio.ui.util.CustomMediaFactory;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.DataEvent;
import org.eclipse.dawnsci.analysis.api.dataset.IDataListener;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.IRemoteDataset;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.plotting.api.EmptyWorkbenchPart;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class MjpgFigure extends AbstractSWTWidgetFigure<Composite> implements IDataListener {

    protected ILoaderService  service;
    protected IPlottingSystem<Composite> system;
    protected IImageTrace imageTrace;
    protected IRemoteDatasetService dataService;
    protected IPaletteService paletteService;
    protected IRemoteDataset set;
    protected String urlString;
    protected Composite widgetComp;
    protected ActionBarWrapper wrapper;
    protected boolean grayScale;
    protected boolean autoscaled = false;

    private void getServices() {
        if (service == null) {
            service = Services.getCurrent().getLoaderService();
        }
        try {
            dataService = Services.getCurrent().getRemoteDatasetService();
            final IPlottingService pservice = Services.getCurrent().getPlottingService();
            this.system = pservice.createPlottingSystem();
            paletteService = Services.getCurrent().getPaletteService();
        } catch (Exception ne) {
            ne.printStackTrace(); // Or your favourite logging.
        }
    }

    public MjpgFigure(final AbstractBaseEditPart editpart) {
        super(editpart, SWT.NONE);
    }

    @Override
    protected Composite createSWTWidget(Composite parent, int style) {
        getServices();
        // Primary composite
        widgetComp = new Composite(parent, SWT.NONE);
        // Recommended by Matt Gerring
        widgetComp.setLayout(new GridLayout(1, false));
        widgetComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        widgetComp.layout();
        // Neither IActionBars nor IWorkbenchPart required.
        wrapper = ActionBarWrapper.createActionBars(widgetComp, null);

        system.createPlotPart(widgetComp, "XY Example", wrapper, PlotType.IMAGE, new EmptyWorkbenchPart<Composite>(system));
        // Recommended by Matt Gerring
        system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        return widgetComp;
    }

    public Collection<String> getAvailableColorMaps() {
        return paletteService.getColorSchemes();
    }

    public void setShowToolbar(boolean show) {
        wrapper.setVisible(show);
        widgetComp.layout();
    }

    public void setURL(String mjpgUrl) {
        this.urlString = mjpgUrl;
    }

    public void setKeepAspectRatio(boolean keep) {
        system.setKeepAspect(keep);
    }

    public void connect(int colorMapIndex) throws MalformedURLException {
        URL url = new URL(urlString);
        String colorMap = Palettes.getCurrent().getPalettes().get(colorMapIndex);
        grayScale = !colorMap.equals(Palettes.NONE);
        try {
            if (grayScale) {
                set = dataService.createGrayScaleMJPGDataset(url, 250, 10);
            } else {
                set = dataService.createMJPGDataset(url, 250, 10);
            }

            if (runmode) {
                set.connect();
            }
            ITrace trace = system.createPlot2D((IDataset)set, null, new NullProgressMonitor());
            imageTrace = (IImageTrace) trace;

            if (grayScale) {
                imageTrace.setPalette(colorMap);
            }
            // Don't show axes.
            for (IAxis axis : system.getAxes()) {
                axis.setVisible(false);
                axis.setShowMajorGrid(true);
                axis.setShowMinorGrid(true);
                Color black = CustomMediaFactory.getInstance().getColor(new RGB(0, 0, 0));
                axis.setMajorGridColor(black);
                axis.setForegroundColor(CustomMediaFactory.getInstance().getColor(new RGB(255, 0, 0)));
            }
            // Don't rescale when new data comes, otherwise we can't zoom.
            system.setRescale(false);
            // Allow zoom to change the aspect ratio.
            system.setKeepAspect(false);
            set.addDataListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setShowLegend(boolean visible) {
        system.setShowLegend(visible);
    }

    public void setShowRamp(boolean show) {
        system.setShowIntensity(show);
    }

    public void setTitle(String title) {
        system.setTitle(title);
    }

    @Override
    public void dispose() {
        wrapper.dispose();

        if (set != null) {
            try {
                set.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Work around limitation in IRemoteDataset.  The first
     * dataset is incomplete so plots as a single pixel.  We
     * need to autoscale when the full data arrives.
     * See http://jira.diamond.ac.uk/browse/SCI-5415
     */
    @Override
    public void dataChangePerformed(DataEvent evt) {
        if (!autoscaled) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    system.autoscaleAxes();
                }
            });
            autoscaled = true;
        }
    }

}
