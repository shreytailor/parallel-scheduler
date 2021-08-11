package com.team7.visualization;

import com.team7.model.Schedule;
import com.team7.parsing.Config;
import com.team7.visualization.ganttchart.GanttProvider;
import com.team7.visualization.system.CPUUtilizationProvider;
import com.team7.visualization.system.RAMUtilizationProvider;
import com.team7.visualization.system.TimeProvider;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SchedulerScreenController implements Initializable {

    private final Image SUN_IMAGE = new Image("/images/sun.png");
    private final Image MOON_IMAGE = new Image("/images/moon.png");
    private final Image DARK_MIN_IMAGE = new Image("/images/minimise-dark.png");
    private final Image DARK_CLOSE_IMAGE = new Image("/images/close-dark.png");
    private final Image LIGHT_MIN_IMAGE = new Image("/images/minimise-light.png");
    private final Image LIGHT_CLOSE_IMAGE = new Image("/images/close-light.png");

    private boolean isLightMode = true;

    private Config _config;
    private Schedule _schedule;

    public SchedulerScreenController(Schedule schedule, Config config) {
        _config = config;
        _schedule = schedule;
    }

    @FXML
    private BorderPane stateGraphContainer;

    @FXML
    private Label timerLabel;

    @FXML
    public Button viewToggleButton;

    @FXML
    public ImageView themeToggleIcon;

    @FXML
    public ImageView minimizeIcon;

    @FXML
    public ImageView closeIcon;

    @FXML
    public LineChart<String, Number> cpuUsageChart;

    @FXML
    public LineChart<String, Number> ramUsageChart;

    private CPUUtilizationProvider cpuUtilizationProvider;
    private RAMUtilizationProvider ramUtilizationProvider;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TimeProvider timeProvider = new TimeProvider();
        timeProvider.registerLabel(timerLabel);

        cpuUtilizationProvider = new CPUUtilizationProvider(cpuUsageChart, "CPU Utilization", timeProvider);
        cpuUtilizationProvider.startTracking();

        NumberAxis cpuYAxis = (NumberAxis) cpuUsageChart.getYAxis();
        cpuUsageChart.getXAxis().setLabel("Time (seconds)");
        cpuYAxis.setLabel("Usage (%)");
        cpuYAxis.setUpperBound(cpuUtilizationProvider.getUpperBound());

        ramUtilizationProvider = new RAMUtilizationProvider(ramUsageChart, "RAM Utilization", timeProvider);
        ramUtilizationProvider.startTracking();

        NumberAxis ramYAxis = (NumberAxis) ramUsageChart.getYAxis();
        ramUsageChart.getXAxis().setLabel("Time (seconds)");
        ramYAxis.setLabel("Usage (%)");
        ramYAxis.setUpperBound(ramUtilizationProvider.getUpperBound());

        GanttProvider scheduleProvider = new GanttProvider(_schedule, _config);
        stateGraphContainer.setCenter(scheduleProvider.getSchedule());
    }

    @FXML
    private void handleToggleTheme() {
        ObservableList<String> sheets = themeToggleIcon.getScene().getRoot().getStylesheets();
        
        if (isLightMode) {
            themeToggleIcon.setImage(SUN_IMAGE);
            closeIcon.setImage(DARK_CLOSE_IMAGE);
            minimizeIcon.setImage(DARK_MIN_IMAGE);

            sheets.clear();
            sheets.add("/stylesheets/SplashDarkMode.css");

            isLightMode = !isLightMode;
        }
        else {
            themeToggleIcon.setImage(MOON_IMAGE);
            closeIcon.setImage(LIGHT_CLOSE_IMAGE);
            minimizeIcon.setImage(LIGHT_MIN_IMAGE);

            sheets.clear();
            sheets.add("/stylesheets/SplashLightMode.css");

            isLightMode = !isLightMode;
        }
    }

    @FXML
    private void handleMinimize() {
        Stage stage = (Stage) minimizeIcon.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleClose() {
        Platform.exit();
    }
}

