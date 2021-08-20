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

    // Image Resources
    private final Image SUN_IMAGE = new Image("/images/sun.png");
    private final Image MOON_IMAGE = new Image("/images/moon.png");

    private final Image DARK_MIN_IMAGE = new Image("/images/minimise-dark.png");
    private final Image DARK_CLOSE_IMAGE = new Image("/images/close-dark.png");
    private final Image DARK_LOAD_IMAGE = new Image("/images/loading-dark.png");
    private final Image DARK_TICK_IMAGE = new Image("/images/tick-dark.png");
    private final Image DARK_OUT_IMAGE = new Image("/images/zoom-out-dark.png");
    private final Image DARK_IN_IMAGE = new Image("/images/zoom-in-dark.png");

    private final Image LIGHT_MIN_IMAGE = new Image("/images/minimise-light.png");
    private final Image LIGHT_CLOSE_IMAGE = new Image("/images/close-light.png");
    private final Image LIGHT_LOAD_IMAGE = new Image("/images/loading-light.png");
    private final Image LIGHT_TICK_IMAGE = new Image("/images/tick-light.png");
    private final Image LIGHT_OUT_IMAGE = new Image("/images/zoom-out-light.png");
    private final Image LIGHT_IN_IMAGE = new Image("/images/zoom-in-light.png");

    // Style Sheets
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

    // Input Graph variables
    private BufferedImage lightBufferedImage;
    private BufferedImage darkBufferedImage;
    private MutableGraph lightMutableGraph;
    private MutableGraph darkMutableGraph;
    private final int INPUT_GRAPH_MAX_HEIGHT = 650;
    private final int INPUT_GRAPH_MAX_WIDTH = 500;
    private final int INPUT_GRAPH_MIN_HEIGHT = 50;
    private final int INPUT_GRAPH_MIN_WIDTH = 50;
    private int inputGraphHeight;
    private int inputGraphWidth;
    private int unitAdjustmentValue = 20;
    private double heightAdjustmentRatio; //ratio value that used to resize the input graph
    private int heightAdjustmentValue;

    public SchedulerScreenController(Task[] tasks, Config config, Scheduler s) {
        _config = config;
        _tasks = tasks;
        scheduler = s;

        try (InputStream dot = new FileInputStream(_config.getInputName())) {
            // parse the dot file and generate an image
            lightMutableGraph = new Parser().read(dot);
            lightMutableGraph.graphAttrs().add(Color.TRANSPARENT.background());
            darkMutableGraph = lightMutableGraph.copy();
            darkMutableGraph.linkAttrs().add(Color.WHITE);
            darkMutableGraph.nodeAttrs().add(Color.WHITE);
            darkMutableGraph.nodeAttrs().add(Color.WHITE.font());

            //render an image into buffer
            lightBufferedImage = Graphviz.fromGraph(lightMutableGraph).render(Format.SVG).toImage();
            darkBufferedImage = Graphviz.fromGraph(darkMutableGraph).render(Format.SVG).toImage();

            // finds a suitable adjustment height value for every zoom in/zoom out click
            inputGraphHeight = lightBufferedImage.getHeight();
            inputGraphWidth = lightBufferedImage.getWidth();
            heightAdjustmentRatio = (double) inputGraphHeight / (double) inputGraphWidth;
            heightAdjustmentValue = (int) (unitAdjustmentValue * heightAdjustmentRatio);

            // convert the image to javafx component
            inputGraphLight = new ImageView(SwingFXUtils.toFXImage(lightBufferedImage, null));
            inputGraphDark = new ImageView(SwingFXUtils.toFXImage(darkBufferedImage, null));
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
    public ImageView zoomInIcon;

    @FXML
    public ImageView zoomOutIcon;


    @FXML
    private ImageView statusIconLoading;

    @FXML
    private ImageView statusIconTick;

    @FXML
    private Label labelOpenedStates;

    @FXML
    private Label labelClosedStates;

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

        EventHandler<ActionEvent> rerenderStatistics = event -> {
            ScheduleUpdater scheduleUpdater = ScheduleUpdater.getInstance();
            ganttProvider.updateSchedule(scheduleUpdater.getObservedSchedule());
            labelOpenedStates.setText(String.valueOf(scheduleUpdater.getOpenedStates()));
            labelClosedStates.setText(String.valueOf(scheduleUpdater.getClosedStates()));
        };

        _chartUpdaterTimeline = new Timeline(new KeyFrame(Duration.seconds(1), rerenderStatistics));
        _chartUpdaterTimeline.setCycleCount(Timeline.INDEFINITE);
        _chartUpdaterTimeline.play();

        // For input graph
        mainGrid.add(inputGraphContainer, 0, 1, 1, 2);
        inputGraphContainer.setVisible(false);
        _timeProvider.registerTimeline(_chartUpdaterTimeline);
    }

    @FXML
    private void handleToggleTheme() {
        ObservableList<String> sheets = themeToggleIcon.getScene().getRoot().getStylesheets();
        if (isLightMode) {
            updateTheme(sheets, DARK_IN_IMAGE, DARK_OUT_IMAGE, SUN_IMAGE, DARK_CLOSE_IMAGE, DARK_MIN_IMAGE, inputGraphDark, LightCss, DarkCss);
        } else {
            updateTheme(sheets, LIGHT_IN_IMAGE, LIGHT_OUT_IMAGE, MOON_IMAGE, LIGHT_CLOSE_IMAGE, LIGHT_MIN_IMAGE, inputGraphLight, DarkCss, LightCss);
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

    @FXML
    public void handleZoomOutIcon() {

        // When the image size is smaller than the minimum width, return to save overhead
        if (inputGraphHeight <= INPUT_GRAPH_MIN_HEIGHT || inputGraphWidth <= INPUT_GRAPH_MIN_WIDTH) {
            return;
        }

        inputGraphHeight -= heightAdjustmentValue;
        inputGraphWidth -= unitAdjustmentValue;

        updateInputGraph(isLightMode, inputGraphHeight, inputGraphWidth);
    }

    @FXML
    public void handleZoomInIcon() {

        // When the image size is exceeding the minimum height, return to save overhead
        if (inputGraphHeight >= INPUT_GRAPH_MAX_HEIGHT || inputGraphWidth >= INPUT_GRAPH_MAX_WIDTH) {
            return;
        }

        inputGraphHeight += heightAdjustmentValue;
        inputGraphWidth += unitAdjustmentValue;

        updateInputGraph(isLightMode, inputGraphHeight, inputGraphWidth);
    }

    public void stop() {

        ScheduleUpdater.getInstance().stop();
//        _chartUpdaterTimeline.stop();
        ganttProvider.updateSchedule(ScheduleUpdater.getInstance().getObservedSchedule());
        _timeProvider.stopTimerLabel();
    }

    /***
     * A helper method that updates the input graph after resizing
     * @param isLightMode
     * @param inputGraphHeight
     * @param inputGraphWidth
     */
    private void updateInputGraph(boolean isLightMode, int inputGraphHeight, int inputGraphWidth) {
        if (isLightMode) {
            lightBufferedImage = Graphviz.fromGraph(lightMutableGraph).height(inputGraphHeight).width(inputGraphWidth).render(Format.SVG).toImage();
            inputGraphLight = new ImageView(SwingFXUtils.toFXImage(lightBufferedImage, null));
            inputGraphContainer.setCenter(inputGraphLight);
        } else {
            darkBufferedImage = Graphviz.fromGraph(darkMutableGraph).height(inputGraphHeight).width(inputGraphWidth).render(Format.SVG).toImage();
            inputGraphDark = new ImageView(SwingFXUtils.toFXImage(darkBufferedImage, null));
            inputGraphContainer.setCenter(inputGraphDark);
        }
    }

    /***
     * A helper method that updates the components and stylesheet after changing theme
     * @param sheets
     * @param in_image
     * @param out_image
     * @param planet_image
     * @param close_image
     * @param min_image
     * @param inputGraph
     * @param removingCss
     * @param addingCss
     */
    private void updateTheme(ObservableList<String> sheets, Image in_image, Image out_image, Image planet_image, Image close_image, Image min_image, ImageView inputGraph, String removingCss, String addingCss) {
        zoomInIcon.setImage(in_image);
        zoomOutIcon.setImage(out_image);
        themeToggleIcon.setImage(planet_image);
        closeIcon.setImage(close_image);
        minimizeIcon.setImage(min_image);
        inputGraphContainer.setCenter(inputGraph);

        sheets.remove(removingCss);
        sheets.add(addingCss);

        isLightMode = !isLightMode;

        // Updating the input graph, otherwise size will differ in two different themes
        updateInputGraph(isLightMode, inputGraphHeight, inputGraphWidth);
    }
}