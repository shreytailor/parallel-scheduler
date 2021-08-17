package com.team7.visualization;

import com.team7.model.Schedule;
import com.team7.model.Task;
import com.team7.parsing.Config;
import com.team7.visualization.ganttchart.GanttProvider;
import com.team7.visualization.system.CPUUtilizationProvider;
import com.team7.visualization.system.RAMUtilizationProvider;
import com.team7.visualization.system.TimeProvider;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
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
    private final Image LIGHT_MIN_IMAGE = new Image("/images/minimise-light.png");
    private final Image LIGHT_CLOSE_IMAGE = new Image("/images/close-light.png");
    private final String LightCss = "/stylesheets/SplashLightMode.css";
    private final String DarkCss = "/stylesheets/SplashDarkMode.css";
    private boolean isLightMode = true;
    private boolean isShowingUtilization = true;

    private Config _config;
    private Schedule _schedule;
    private final Task[] _tasks;

    private ImageView inputGraphLight;
    private ImageView inputGraphDark;
    private BorderPane inputGraphContainer = new BorderPane();

    public SchedulerScreenController(Task[] tasks, Schedule schedule, Config config) {
        _config = config;
        _schedule = schedule;
        _tasks = tasks;

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
        setupToolTips();

        TimeProvider timeProvider = new TimeProvider();
        timeProvider.registerLabel(timerLabel);

        cpuUtilizationProvider = new CPUUtilizationProvider(cpuUsageChart, "CPU Utilization", timeProvider);
        cpuUtilizationProvider.startTracking();

        // Applying custom properties to the CPU chart.
        NumberAxis cpuYAxis = (NumberAxis) cpuUsageChart.getYAxis();
        cpuUsageChart.getXAxis().setLabel("Time (seconds)");
        cpuYAxis.setLabel("Usage (%)");
        cpuYAxis.setUpperBound(cpuUtilizationProvider.getUpperBound());

        ramUtilizationProvider = new RAMUtilizationProvider(ramUsageChart, "RAM Utilization", timeProvider);
        ramUtilizationProvider.startTracking();

        // Applying custom properties to the RAM chart.
        NumberAxis ramYAxis = (NumberAxis) ramUsageChart.getYAxis();
        ramUsageChart.getXAxis().setLabel("Time (seconds)");
        ramYAxis.setLabel("Usage (%)");
        ramYAxis.setUpperBound(ramUtilizationProvider.getUpperBound());

        GanttProvider scheduleProvider = new GanttProvider(_tasks, _schedule, _config);
        stateGraphContainer.setCenter(scheduleProvider.getSchedule());

        // For input graph
        mainGrid.add(inputGraphContainer, 0, 1, 1, 2);
        inputGraphContainer.setVisible(false);
    }

    private void setupToolTips() {
        Tooltip.install(inputGraphContainer, new Tooltip("Input Graph"));
        Tooltip.install(themeToggleIcon, new Tooltip("Light/Dark mode"));
        Tooltip.install(utilGraphContainer, new Tooltip("Utilization Graphs"));
    }

    @FXML
    private void handleToggleTheme() {
        ObservableList<String> sheets = themeToggleIcon.getScene().getRoot().getStylesheets();
        
        if (isLightMode) {
            themeToggleIcon.setImage(SUN_IMAGE);
            closeIcon.setImage(DARK_CLOSE_IMAGE);
            minimizeIcon.setImage(DARK_MIN_IMAGE);
            inputGraphContainer.setCenter(inputGraphDark);

            sheets.remove(LightCss);
            sheets.add(DarkCss);

            isLightMode = !isLightMode;
        }
        else {
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
}

