package org.csstudio.opibuilder.widgets.dawn.xygraph;

import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.ISample;
import org.eclipse.nebula.visualization.xygraph.dataprovider.Sample;
import org.eclipse.nebula.visualization.xygraph.linearscale.Range;

public class XYGraphDataProvider extends CircularBufferDataProvider
        implements IDataProvider {

    private boolean xAxisUsingDate;

    public XYGraphDataProvider(boolean chronological, boolean xAxisUsingDate) {
        super(chronological);
        this.xAxisUsingDate = xAxisUsingDate;
    }

    public void setXAxisUsingDate(boolean xAxisUsingDate) {
        this.xAxisUsingDate = xAxisUsingDate;
    }

    private boolean plotXIndex() {
        return chronological && !xAxisUsingDate;
    }

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

    @Override
    public void setChronological(boolean chronological) {
        super.setChronological(chronological);
    }

}
