package org.csstudio.opibuilder.widgets.mjpg;


import java.io.IOException;
import java.net.URL;

import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.IRemoteDataset;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
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
import org.eclipse.ui.part.ViewPart;

public class MjpgView extends ViewPart {
    
    protected IRemoteDataset set;

    protected ILoaderService  service;
    protected IRemoteDatasetService dataService;
    protected IPlottingSystem<Composite> system;

    public MjpgView() {
        // A service for loading data from any data file format.
        service = Services.getCurrent().getLoaderService();
        
        final IPlottingService pservice = Services.getCurrent().getPlottingService();
        System.out.println("The plotting servivce " + service);
        try {
            dataService = Services.getCurrent().getRemoteDatasetService();
            this.system = pservice.createPlottingSystem();
            System.out.println("The plotting system: " + this.system);
        } catch (Exception ne) {
            ne.printStackTrace(); // Or your favourite logging.
        }
            
    }

    @Override
    public void createPartControl(Composite parent) {
        try {
            
            Composite c = new Composite(parent, SWT.NONE);
            c.setLayout(new GridLayout(1, false));
            system.createPlotPart(c, "XY Example", getViewSite().getActionBars(), PlotType.IMAGE, this);
            system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            System.out.println("The system " + system);
            
            String mjpgUrl = "http://bl11i-di-serv-01:8082/ALCAM2.MJPG.mjpg";
            URL url = null;
            // Plot some data
            try {
                url = new URL(mjpgUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Thread safe convenience method
            connect(url);
            
            
            
        } catch (Throwable ne) {
            ne.printStackTrace(); // Or your favourite logging.
        }
    }
    
    public void connect(URL url) {
        try {
            set = dataService.createMJPGDataset(url, 250, 10);

            //system.setKeepAspect(false);
            set.connect();
            ITrace trace = system.createPlot2D((IDataset)set, null, new NullProgressMonitor());
            System.out.println("The trace is " + trace);
            IImageTrace imageTrace = (IImageTrace) trace;
            System.out.println(imageTrace.getPaletteName());
            IPaletteService paletteService = Services.getCurrent().getPaletteService();
            for (String palette : paletteService.getColorSchemes()) {
                System.out.println(palette);
            }
            imageTrace.setPalette("NCD");
            System.out.println(imageTrace.getPaletteName());
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
            //system.setRescale(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

}
