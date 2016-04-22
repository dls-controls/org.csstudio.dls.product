package org.csstudio.opibuilder.widgets.mjpg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;

public class Palettes {
    
    public static final String NONE = "None";
    private List<String> palettes = new ArrayList<String>();
    private IPaletteService paletteService = null;
    private static Palettes instance = null;
    
    public Palettes() {
        paletteService = Services.getCurrent().getPaletteService();
    }

    public static Palettes getCurrent() {
        if (instance == null) {
            instance = new Palettes();
        }
        return instance;
    }

    public List<String> getPalettes() {
        if (palettes.isEmpty()) {
            palettes.add(NONE);
            Collection<String> paletteNames = paletteService.getColorSchemes();
            for (String palette : paletteNames) {
                palettes.add(palette);
            }
        }
        return palettes;
    }
}
