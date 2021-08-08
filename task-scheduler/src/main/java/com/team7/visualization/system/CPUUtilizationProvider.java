package com.team7.visualization.system;

import javafx.scene.chart.LineChart;

/**
 * This class extends the abstract UtilizationProvider class, and it supplies the CPU Utilization
 * chart with the required information.
 */
public class CPUUtilizationProvider extends UtilizationProvider {

    /**
     * The default constructor of this class which calls the constructor of the parent class.
     * @param chart the chart on which the usage information will be shown.
     */
    public CPUUtilizationProvider(LineChart<String, Number> chart) {
        super(chart);
    }

    /**
     * This method gets the current CPU load from this process.
     * @return double the percentage of CPU load.
     */
    @Override
    public double getData() {
        return this.bean.getProcessCpuLoad() * 100;
    }

    /**
     * This method gets the upper bound of the chart's Y axis.
     * @return double this number symbolises the upper bound of the chart's Y axis.
     */
    @Override
    public double getUpperBound() {
        return 100;
    }
}
