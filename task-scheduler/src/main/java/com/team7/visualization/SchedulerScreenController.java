package com.team7.visualization;

import com.team7.model.Schedule;
import com.team7.model.Task;
import com.team7.parsing.Config;
import com.team7.visualization.ganttchart.GanttProvider;
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
    private final Image DARK_OUT_IMAGE = new Image("/images/zoom-out-dark.png");
    private final Image DARK_IN_IMAGE = new Image("/images/zoom-in-dark.png");

    private final Image LIGHT_MIN_IMAGE = new Image("/images/minimise-light.png");
    private final Image LIGHT_CLOSE_IMAGE = new Image("/images/close-light.png");
    private final Image LIGHT_OUT_IMAGE = new Image("/images/zoom-out-light.png");
    private final Image LIGHT_IN_IMAGE = new Image("/images/zoom-in-light.png");

    private final String LightCss = "/stylesheets/SplashLightMode.css";
    private final String DarkCss = "/stylesheets/SplashDarkMode.css";

    private boolean isLightMode = true;
    private boolean isShowingUtilization = true;

    private final Config _config;
    private final Task[] _tasks;
    private ObservableList<Schedule> _schedules;

    private ImageView inputGraphLight;
    private ImageView inputGraphDark;
    private final BorderPane inputGraphContainer = new BorderPane();

    // Input Graph
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

    public SchedulerScreenController(Task[] tasks, Config config) {

        _config = config;
        _tasks = tasks;

        try (InputStream dot = new FileInputStream(_config.getInputName())) {
            // parse the dot file and generate an image
            lightMutableGraph = new Parser().read(dot);
            lightMutableGraph.graphAttrs().add(Color.TRANSPARENT.background());
            darkMutableGraph = lightMutableGraph.copy();
            darkMutableGraph.linkAttrs().add(Color.WHITE);
            darkMutableGraph.nodeAttrs().add(Color.WHITE);
            darkMutableGraph.nodeAttrs().add(Color.WHITE.font());


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
    public ImageView zoomInIcon;

    @FXML
    public ImageView zoomOutIcon;

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
    private GanttProvider ganttProvider;
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
        _timeProvider = new TimeProvider();
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

        _schedules = ScheduleUpdater.getInstance().getObservableList();
        ScheduleUpdater.getInstance().start();

        ganttProvider = new GanttProvider(_tasks, _schedules.get(0), _config);
        stateGraphContainer.setCenter(ganttProvider.getSchedule());
        EventHandler<ActionEvent> remakeGraph = event -> {
            ganttProvider.updateSchedule(_schedules.get(0));
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
            zoomInIcon.setImage(DARK_IN_IMAGE);
            zoomOutIcon.setImage(DARK_OUT_IMAGE);
            themeToggleIcon.setImage(SUN_IMAGE);
            closeIcon.setImage(DARK_CLOSE_IMAGE);
            minimizeIcon.setImage(DARK_MIN_IMAGE);
            inputGraphContainer.setCenter(inputGraphDark);

            sheets.remove(LightCss);
            sheets.add(DarkCss);

            isLightMode = !isLightMode;
        } else {
            zoomInIcon.setImage(LIGHT_IN_IMAGE);
            zoomOutIcon.setImage(LIGHT_OUT_IMAGE);

            themeToggleIcon.setImage(MOON_IMAGE);
            closeIcon.setImage(LIGHT_CLOSE_IMAGE);
            minimizeIcon.setImage(LIGHT_MIN_IMAGE);
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
        if (isShowingUtilization) {
            utilGraphContainer.setVisible(false);
            inputGraphContainer.setVisible(true);
            viewToggleButton.setText("Show Utilization");
            isShowingUtilization = !isShowingUtilization;
        }
        else {
            inputGraphContainer.setVisible(false);
            utilGraphContainer.setVisible(true);
            viewToggleButton.setText("Show Input Graph");
            isShowingUtilization = !isShowingUtilization;
        }
    }

    public void stop() {

        ScheduleUpdater.getInstance().stop();
        _chartUpdaterTimeline.stop();
        ganttProvider.updateSchedule(_schedules.get(0));
        _timeProvider.stopTimerLabel();
    }
    @FXML
    public void handleZoomOutIcon() {

        // When the image size is smaller than the minimum width, return to save overhead
        if (inputGraphHeight <= INPUT_GRAPH_MIN_HEIGHT || inputGraphWidth <= INPUT_GRAPH_MIN_WIDTH) {
            return;
        }

        inputGraphHeight -= heightAdjustmentValue;
        inputGraphWidth -= unitAdjustmentValue;

        updateInputGraph(isLightMode,inputGraphHeight, inputGraphWidth);
    }

    @FXML
    public void handleZoomInIcon() {

        // When the image size is exceeding the minimum height, return to save overhead
        if (inputGraphHeight >= INPUT_GRAPH_MAX_HEIGHT || inputGraphWidth >= INPUT_GRAPH_MAX_WIDTH) {
            return;
        }

        inputGraphHeight += heightAdjustmentValue;
        inputGraphWidth += unitAdjustmentValue;

        updateInputGraph(isLightMode,inputGraphHeight, inputGraphWidth);
    }

    /***
     * A helper method that updates the input graph after resizing
     * @param isLightMode
     * @param inputGraphHeight
     * @param inputGraphWidth
     */
    private void updateInputGraph(boolean isLightMode, int inputGraphHeight, int inputGraphWidth){
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


}

