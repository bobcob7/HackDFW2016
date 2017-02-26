package com.javacodegeeks.freemind;

import java.util.List;

/**
 * Created by anura on 2/26/2017.
 */

public class Module {
    private List<Part> partsList;
    private String moduleId;

    public Module() {

    }

    public Module(String moduleId) {
        this.moduleId = moduleId;

    }

    public List<Part> getPartsList() {
        return partsList;
    }

    public void setPartsList(List<Part> partsList) {
        this.partsList = partsList;
    }
}
