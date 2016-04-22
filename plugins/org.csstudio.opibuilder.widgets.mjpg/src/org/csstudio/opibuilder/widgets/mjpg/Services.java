/*-
 *******************************************************************************
 * Copyright (c) 2011, 2014 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.csstudio.opibuilder.widgets.mjpg;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.osgi.service.component.ComponentContext;

/**
 * Class to inject OSGI services to.
 * @author fcp94556
 *
 */
public class Services {

    private ILoaderService loaderService;
    private IPlottingService plottingService;
    private IRemoteDatasetService dataService;
    private IPaletteService paletteService;

    public ILoaderService getLoaderService() {
        return loaderService;
    }

    public void setLoaderService(ILoaderService loaderService) {
        this.loaderService = loaderService;
    }

    public IPlottingService getPlottingService() {
        return plottingService;
    }

    public void setPlottingService(IPlottingService plottingService) {
        this.plottingService = plottingService;
    }

    public IRemoteDatasetService getRemoteDatasetService() {
        return dataService;
    }

    public void setRemoteDatasetService(IRemoteDatasetService dataService) {
        this.dataService = dataService;
    }

   public IPaletteService getPaletteService() {
        return paletteService;
    }

   public void setPaletteService(IPaletteService paletteService) {
        this.paletteService = paletteService;
    }

    private static Services current;

    public void start(ComponentContext context) {
        current = this;
    }

    public void stop() {
        current = null;
    }

    public static Services getCurrent() {
        return current;
    }
}
