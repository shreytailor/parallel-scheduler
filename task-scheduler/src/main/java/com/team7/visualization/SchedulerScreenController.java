package com.team7.visualization;

import com.team7.algorithm.Scheduler;
import com.team7.model.Schedule;
import com.team7.model.Task;
import com.team7.parsing.Config;
import com.team7.visualization.ganttchart.GanttProvider;
import com.team7.visualization.ganttchart.GanttProvider2;
import com.team7.visualization.realtime.ScheduleUpdater;
import com.team7.visualization.system.CPUUtilizationProvider;
import com.team7.visualization.system.RAMUtilizationProvider;
import com.team7.visualization.system.TimeProvider;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class SchedulerScreenController implements Initializable {

    private final Image SUN_IMAGE = new Image("/images/sun.png");
    private final Image MOON_IMAGE = new Image("/images/moon.png");
    private final Image DARK_MIN_IMAGE = new Image("/images/minimise-dark.png");
    private final Image DARK_CLOSE_IMAGE = new Image("/images/close-dark.png");
    private final Image DARK_LOAD_IMAGE = new Image("/images/loading-dark.png");
    private final Image DARK_TICK_IMAGE = new Image("/images/tick-dark.png");
    private final Image LIGHT_MIN_IMAGE = new Image("/images/minimise-light.png");
    private final Image LIGHT_CLOSE_IMAGE = new Image("/images/close-light.png");
    private final Image LIGHT_LOAD_IMAGE = new Image("/images/loading-light.png");
    private final Image LIGHT_TICK_IMAGE = new Image("/images/tick-light.png");
    private final String LightCss = getClass().getResource("/stylesheets/SplashLightMode.css").toExternalForm();
    private final String DarkCss = getClass().getResource("/stylesheets/SplashDarkMode.css").toExternalForm();
    private boolean isLightMode = true;
    private boolean isShowingUtilization = true;

    private final Config _config;
    private final Task[] _tasks;

    private ImageView inputGraphLight;
    private ImageView inputGraphDark;
    private final BorderPane inputGraphContainer = new BorderPane();
    private Scheduler scheduler;
    private Schedule _observedSchedule;

    public SchedulerScreenController(Task[] tasks, Config config, Scheduler s) {
        _config = config;
        _tasks = tasks;
        scheduler = s;

        try (InputStream dot = new FileInputStream(_config.getInputName())) {
            // parse the dot file and generate an image
            MutableGraph lg = new Parser().read(dot);
            lg.graphAttrs().add(Color.TRANSPARENT.background());
            MutableGraph dg = lg.copy();
            dg.linkAttrs().add(Color.WHITE);
            dg.nodeAttrs().add(Color.WHITE);
            dg.nodeAttrs().add(Color.WHITE.font());

            BufferedImage imBufferLight = Graphviz.fromGraph(lg).height(650).width(500).render(Format.SVG).toImage();
            BufferedImage imBufferDark = Graphviz.fromGraph(dg).height(650).width(500).render(Format.SVG).toImage();

            // convert the image to javafx component
            inputGraphLight = new ImageView(SwingFXUtils.toFXImage(imBufferLight, null));
            inputGraphDark = new ImageView(SwingFXUtils.toFXImage(imBufferDark, null));
            inputGraphContainer.setCenter(inputGraphLight);

        } catch (FileNotFoundException e) {
            viewToggleButton.setDisable(true);
            viewToggleButton.setTooltip(new Tooltip("Input file not found"));
        } catch (IOException e) {
            viewToggleButton.setDisable(true);
            viewToggleButton.setTooltip(new Tooltip("Some problems occurred in the input file"));
        }
    }

    @FXML
    private GridPane utilGraphContainer;

    @FXML
    private GridPane mainGrid;

    @FXML
    private BorderPane stateGraphContainer;

    @FXML
    private Label timerLabel;

    @FXML
    private Button viewToggleButton;

    @FXML
    private ImageView themeToggleIcon;

    @FXML
    private ImageView minimizeIcon;

    @FXML
    private ImageView closeIcon;

    @FXML
    private ImageView statusIconLoading;

    @FXML
    private ImageView statusIconTick;

    @FXML
    public LineChart<String, Number> cpuUsageChart;

    @FXML
    public LineChart<String, Number> ramUsageChart;

    private CPUUtilizationProvider cpuUtilizationProvider;
    private RAMUtilizationProvider ramUtilizationProvider;
    private GanttProvider2 ganttProvider;
    private Timeline _chartUpdaterTimeline;
    private TimeProvider _timeProvider;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupToolTips();
        setupSystemCharts();
    }

    /**
     * This method is used to set up the tooltips on the screen.
     */
    private void setupToolTips() {
        Tooltip.install(inputGraphContainer, new Tooltip("Input Graph"));
        Tooltip.install(themeToggleIcon, new Tooltip("Light/Dark mode"));
        Tooltip.install(utilGraphContainer, new Tooltip("Utilization Graphs"));
    }

    /**
     * This method is used to set up the providers, required for live CPU/RAM usage visualization.
     */
    private void setupSystemCharts() {
        _timeProvider = TimeProvider.getInstance();
        _timeProvider.registerLabel(timerLabel);

        cpuUtilizationProvider = new CPUUtilizationProvider(cpuUsageChart, "CPU Utilization", _timeProvider);
        cpuUtilizationProvider.startTracking();

        // Applying custom properties to the CPU chart.
        NumberAxis cpuYAxis = (NumberAxis) cpuUsageChart.getYAxis();
        cpuUsageChart.getXAxis().setLabel("Time (seconds)");
        cpuYAxis.setLabel("Usage (%)");
        cpuYAxis.setUpperBound(cpuUtilizationProvider.getUpperBound());

        ramUtilizationProvider = new RAMUtilizationProvider(ramUsageChart, "RAM Utilization", _timeProvider);
        ramUtilizationProvider.startTracking();

        // Applying custom properties to the RAM chart.
        NumberAxis ramYAxis = (NumberAxis) ramUsageChart.getYAxis();
        ramUsageChart.getXAxis().setLabel("Time (seconds)");
        ramYAxis.setLabel("Usage (%)");
        ramYAxis.setUpperBound(ramUtilizationProvider.getUpperBound());

        _observedSchedule = ScheduleUpdater.getInstance().getObservedSchedule();
//        _schedules = ScheduleUpdater.getInstance().getObservableList();
        ScheduleUpdater.getInstance().start();

        ganttProvider = new GanttProvider2(_tasks, _observedSchedule, _config);
        stateGraphContainer.setCenter(ganttProvider.getSchedule());

//        scheduler.getTrigger().addListener((observable, oldVal, newVal) -> {
//            Schedule s = scheduler.getCurrentBest();
//            ganttProvider.updateSchedule(s);
//        });

        EventHandler<ActionEvent> remakeGraph = event -> {
            ganttProvider.updateSchedule(ScheduleUpdater.getInstance().getObservedSchedule());
        };


        _chartUpdaterTimeline = new Timeline(new KeyFrame(Duration.seconds(1), remakeGraph));
        _chartUpdaterTimeline.setCycleCount(Timeline.INDEFINITE);
        _chartUpdaterTimeline.play();

        // For input graph
        mainGrid.add(inputGraphContainer, 0, 1, 1, 2);
        inputGraphContainer.setVisible(false);
    }

    @FXML
    private void handleToggleTheme() {
        ObservableList<String> sheets = themeToggleIcon.getScene().getRoot().getStylesheets();
        if (isLightMode) {
            themeToggleIcon.setImage(SUN_IMAGE);
            closeIcon.setImage(DARK_CLOSE_IMAGE);
            minimizeIcon.setImage(DARK_MIN_IMAGE);
            statusIconLoading.setImage(DARK_LOAD_IMAGE);
            statusIconTick.setImage(DARK_TICK_IMAGE);
            inputGraphContainer.setCenter(inputGraphDark);

            sheets.remove(LightCss);
            sheets.add(DarkCss);

            isLightMode = !isLightMode;
        } else {
            themeToggleIcon.setImage(MOON_IMAGE);
            closeIcon.setImage(LIGHT_CLOSE_IMAGE);
            minimizeIcon.setImage(LIGHT_MIN_IMAGE);
            statusIconLoading.setImage(LIGHT_LOAD_IMAGE);
            statusIconTick.setImage(LIGHT_TICK_IMAGE);
            inputGraphContainer.setCenter(inputGraphLight);

            sheets.remove(DarkCss);
            sheets.add(LightCss);

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
        System.exit(0);
    }

    @FXML
    public void handleViewToggleButton() {
        utilGraphContainer.setVisible(!isShowingUtilization);
        inputGraphContainer.setVisible(isShowingUtilization);
        isShowingUtilization = !isShowingUtilization;

        if (isShowingUtilization) {
            viewToggleButton.setText("Show Utilization");
        } else {
            viewToggleButton.setText("Show Input Graph");
        }
    }

    public void stop() {

        ScheduleUpdater.getInstance().stop();
//        _chartUpdaterTimeline.stop();
        ganttProvider.updateSchedule(ScheduleUpdater.getInstance().getObservedSchedule());
        _timeProvider.stopTimerLabel();
    }
}