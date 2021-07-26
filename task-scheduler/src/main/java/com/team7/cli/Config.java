package com.team7.cli;

import java.util.Objects;

public class Config {
    private int numOfCores;
    private boolean isVisualised;
    private String outputName;
    private String inputName;

    /**
     * initialise with default values, as specified in the requirement
     */
    public Config(){
        this(1,false,"","");
    }

    public Config(int numOfCores, boolean isVisualised, String outputName, String inputName) {
        this.numOfCores = numOfCores;
        this.isVisualised = isVisualised;
        this.outputName = outputName;
        this.inputName = inputName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Config config = (Config) o;
        return numOfCores == config.numOfCores &&
                isVisualised == config.isVisualised &&
                Objects.equals(outputName, config.outputName) &&
                Objects.equals(inputName, config.inputName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numOfCores, isVisualised, outputName, inputName);
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
