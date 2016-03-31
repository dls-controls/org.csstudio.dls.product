package org.csstudio.openfile.newwindow;

import java.util.Map;
import java.util.StringJoiner;

import org.csstudio.utility.product.LinkedResourcesJob;
import org.eclipse.core.runtime.jobs.Job;

public class LinkUpdater {

    private Map<String, String> links;

    public LinkUpdater(Map<String, String> links) {
        this.links = links;
    }

    public void update() throws InterruptedException {
        // The logic for creating links is wrapped in this job.  However,
        // we want the job to finish before continuing.
        if (!links.isEmpty()) {
            final Job job = new LinkedResourcesJob(getLinksString());
            job.schedule();
            job.join();
        }
    }

    public String getLinksString() {
        StringJoiner joiner = new StringJoiner(",");
        for (String key : links.keySet()) {
            joiner.add(key + "=" + links.get(key));
        }
        return joiner.toString();
    }
}
