package org.csstudio.opibuilder.widgets.dawn.xygraph;

import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.ISample;
import org.eclipse.nebula.visualization.xygraph.dataprovider.Sample;
import org.eclipse.nebula.visualization.xygraph.linearscale.Range;

public class XYGraphDataProvider extends CircularBufferDataProvider
        implements IDataProvider {

    public XYGraphDataProvider(boolean chronological) {
        super(chronological);
    }

    private boolean plotXIndex() {
        return chronological && !xAxisDateEnabled;
    }

    /**
     * If plotting xIndex substitute the sample index for x-value, otherwise
     * return the indexed sample with its stored x-value
     */
    @Override
    public ISample getSample(int index) {
        ISample sample = super.getSample(index);
        if (sample != null && plotXIndex()) {
            sample = new Sample(index, sample.getYValue(),
                                sample.getYPlusError(), sample.getYMinusError(),
                                0, 0,
                                sample.getInfo());
        }
        return sample;
    }

    /**
     * If plotting xIndex return {0, n-1} otherwise calculate x-range as for
     * @see org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider
     *
     */
    @Override
    public synchronized Range getXDataMinMax() {
        Range xRange;
        if (plotXIndex() && getSize() > 1) {
            xRange = new Range(0, traceData.size() - 1);
        } else {
            xRange = super.getXDataMinMax();
        }
        return xRange;
    }

}
