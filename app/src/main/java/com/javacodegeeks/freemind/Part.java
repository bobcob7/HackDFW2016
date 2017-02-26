package com.javacodegeeks.freemind;

/**
 * Created by anura on 2/26/2017.
 */

public class Part {
    private String partId;
    private boolean isAssigned;
    private boolean isFound;
    private boolean isPlaced;
    private boolean isAvailable;

    public Part() {

    }
    public Part(String partId) {
        this.partId = partId;
    }

    public boolean isAssigned() {
        return isAssigned;
    }

    public void setAssigned(boolean assigned) {
        isAssigned = assigned;
    }

    public boolean isFound() {
        return isFound;
    }

    public void setFound(boolean found) {
        isFound = found;
    }

    public boolean isPlaced() {
        return isPlaced;
    }

    public void setPlaced(boolean placed) {
        isPlaced = placed;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
