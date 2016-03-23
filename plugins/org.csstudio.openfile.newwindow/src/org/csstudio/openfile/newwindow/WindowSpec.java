package org.csstudio.openfile.newwindow;

import java.util.HashMap;
import java.util.Map;

public class WindowSpec {

    private String perspectiveId;
    private String perspectiveFile;
    private Map<String, String> links = new HashMap<>();

    public String getPerspectiveId() {
        return perspectiveId;
    }

    public void setPerspectiveId(String perspectiveId) {
        this.perspectiveId = perspectiveId;
    }

    public String getPerspectiveFile() {
        return perspectiveFile;
    }

    public void setPerspectiveFile(String perspectiveFile) {
        this.perspectiveFile = perspectiveFile;
    }

    public void addLink(String eclipsePath, String filePath) {
        links.put(eclipsePath, filePath);
    }

    public Map<String, String> getLinks() {
        return links;
    }

}
