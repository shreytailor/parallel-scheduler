package com.team7.cli;

public class Config {
    private int numOfCores;
    private boolean isVisualised;
    private String outputName;
    private String inputName;

    /**
     * initialise with default values, as specified in the requirement
     */
    private Config(){
        numOfCores = 1;
        isVisualised = false;
    }


    public int getNumOfCores() {
        return numOfCores;
    }

    public void setNumOfCores(int numOfCores) {
        this.numOfCores = numOfCores;
    }

    public boolean isVisualised() {
        return isVisualised;
    }

    public void setVisualised(boolean visualised) {
        isVisualised = visualised;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }
}
