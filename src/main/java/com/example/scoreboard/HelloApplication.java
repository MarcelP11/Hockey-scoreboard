package com.example.scoreboard;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HelloApplication extends Application {
    private int homeScore = 0;
    private int awayScore = 0;
    private boolean timerRunning = false;
    private double timeSeconds = 0.0;
    private Timeline timeline;
    private Timeline timeoutTimeline;

    private Timeline intermissionTimeline;
    private int currentPeriod = 1;
    private final String[] periods = {"1.", "2.", "3.", "P", "N"};
    private List<Team> teams = new ArrayList<>();

    private int timeoutMinutes = 0; // Default timeout duration
    private int timeoutSeconds = 5; // Default timeout duration
    private boolean homeTimeoutUsed = false;
    private boolean awayTimeoutUsed = false;
    private int periodMinutes = 0; // Default period duration in minutes
    private int periodSeconds = 10;  // Default period duration in seconds
    private boolean intermissionEnabled = false;
    private int intermissionMinutes = 0;
    private int intermissionSeconds = 5;



    @Override
    public void start(Stage primaryStage) {
        // Labels for scores
        Label homeLabel = new Label("HOME");
        Label homeScoreLabel = new Label(String.valueOf(homeScore));
        Label awayLabel = new Label("AWAY");
        Label awayScoreLabel = new Label(String.valueOf(awayScore));

        // Buttons for score control
        Button homePlusButton = new Button("+");
        Button homeMinusButton = new Button("-");
        Button awayPlusButton = new Button("+");
        Button awayMinusButton = new Button("-");

        // Timeout buttons
        Button homeTimeoutButton = new Button("Timeout");
        Button awayTimeoutButton = new Button("Timeout");

        // Timer controls
        Label timerLabel = new Label(formatTime(timeSeconds));
        Button startStopButton = new Button("START");

        // Period controls
        Label periodTextLabel = new Label("Period");
        Label periodLabel = new Label(periods[currentPeriod - 1]);
        Button periodPlusButton = new Button("+");
        Button periodMinusButton = new Button("-");

        // Menu
        MenuBar menuBar = new MenuBar();
        Menu teamsMenu = new Menu("Teams");
        MenuItem addTeamMenuItem = new MenuItem("Add new team");
        MenuItem showTeamsMenuItem = new MenuItem("Show all teams");

        Menu settingsMenu = new Menu("Settings");
        MenuItem settingsMenuItem = new MenuItem("Settings");

        teamsMenu.getItems().addAll(addTeamMenuItem, new SeparatorMenuItem(), showTeamsMenuItem);
        settingsMenu.getItems().add(settingsMenuItem);
        menuBar.getMenus().addAll(teamsMenu, settingsMenu);

        addTeamMenuItem.setOnAction(e -> showAddTeamDialog(primaryStage));
        showTeamsMenuItem.setOnAction(e -> showAllTeamsDialog(primaryStage));
        settingsMenuItem.setOnAction(e -> showSettingsDialog(primaryStage));

        // Home score controls
        homePlusButton.setOnAction(e -> {
            homeScore++;
            homeScoreLabel.setText(String.valueOf(homeScore));
        });

        homeMinusButton.setOnAction(e -> {
            if (homeScore > 0) {
                homeScore--;
                homeScoreLabel.setText(String.valueOf(homeScore));
            }
        });

        // Away score controls
        awayPlusButton.setOnAction(e -> {
            awayScore++;
            awayScoreLabel.setText(String.valueOf(awayScore));
        });

        awayMinusButton.setOnAction(e -> {
            if (awayScore > 0) {
                awayScore--;
                awayScoreLabel.setText(String.valueOf(awayScore));
            }
        });

        // Timeout controls
        homeTimeoutButton.setOnAction(e -> startTimeout(timerLabel, startStopButton, homeTimeoutButton, awayTimeoutButton, true));
        awayTimeoutButton.setOnAction(e -> startTimeout(timerLabel, startStopButton, homeTimeoutButton, awayTimeoutButton, false));

        // Timer control
        startStopButton.setOnAction(e -> toggleTimer(startStopButton, timerLabel, homeTimeoutButton, awayTimeoutButton));

        // Period control
        periodPlusButton.setOnAction(e -> {
            if (currentPeriod < periods.length) {
                currentPeriod++;
                periodLabel.setText(periods[currentPeriod - 1]);
            }
        });

        periodMinusButton.setOnAction(e -> {
            if (currentPeriod > 1) {
                currentPeriod--;
                periodLabel.setText(periods[currentPeriod - 1]);
            }
        });

        // Layout for Home side
        VBox homeBox = new VBox(10, homeLabel, homeScoreLabel, homePlusButton, homeMinusButton, homeTimeoutButton);
        homeBox.setAlignment(Pos.CENTER);
        homeBox.setPadding(new Insets(10));
        homeBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));

        // Layout for Away side
        VBox awayBox = new VBox(10, awayLabel, awayScoreLabel, awayPlusButton, awayMinusButton, awayTimeoutButton);
        awayBox.setAlignment(Pos.CENTER);
        awayBox.setPadding(new Insets(10));
        awayBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));

        // Layout for Timer
        VBox periodBox = new VBox(5, periodTextLabel, periodLabel, new HBox(5, periodMinusButton, periodPlusButton));
        periodBox.setAlignment(Pos.CENTER);

        VBox timerBox = new VBox(10, periodBox, timerLabel, startStopButton);
        timerBox.setAlignment(Pos.CENTER);

        // Vertical Separators
        Separator homeSeparator = new Separator();
        homeSeparator.setOrientation(Orientation.VERTICAL);
        Separator awaySeparator = new Separator();
        awaySeparator.setOrientation(Orientation.VERTICAL);

        // Main layout
        HBox mainContent = new HBox(20, homeBox, homeSeparator, timerBox, awaySeparator, awayBox);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(20));

        VBox mainLayout = new VBox(menuBar, mainContent);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 600, 400);

        // Key event for spacebar
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                toggleTimer(startStopButton, timerLabel, homeTimeoutButton, awayTimeoutButton);
            }
        });

        primaryStage.setTitle("Scoreboard App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void toggleTimer(Button startStopButton, Label timerLabel, Button homeTimeoutButton, Button awayTimeoutButton) {
        if (timeoutTimeline != null && timeoutTimeline.getStatus() == Timeline.Status.RUNNING) {
            return; // Do nothing if timeout is running
        }
        timerRunning = !timerRunning;
        startStopButton.setText(timerRunning ? "STOP" : "START");
        if (timerRunning) {
            homeTimeoutButton.setDisable(true);
            awayTimeoutButton.setDisable(true);
            if (timeline == null) {
                timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
                    timeSeconds += 0.1;
                    timerLabel.setText(formatTime(timeSeconds));

                    // Check if the period time has been reached
                    double periodTotalSeconds = periodMinutes * 60 + periodSeconds;
                    if (timeSeconds >= periodTotalSeconds) {
                        timeline.pause();
                        timerRunning = false;
                        startStopButton.setText("START");

                        // Show the period end dialog after the timeline has paused
                        Platform.runLater(() -> {
                            showPeriodEndDialog();
                            if (intermissionEnabled) {
                                startIntermission(timerLabel, startStopButton, homeTimeoutButton, awayTimeoutButton);
                            } else {
                                timeSeconds = 0.0;
                                timerLabel.setText(formatTime(timeSeconds));
                                homeTimeoutButton.setDisable(true);
                                awayTimeoutButton.setDisable(true);
                            }
                        });
                    }
                }));
                timeline.setCycleCount(Timeline.INDEFINITE);
            }
            timeline.play();
        } else {
            homeTimeoutButton.setDisable(homeTimeoutUsed);
            awayTimeoutButton.setDisable(awayTimeoutUsed);
            if (timeline != null) {
                timeline.pause();
            }
        }
    }


    private void startTimeout(Label timerLabel, Button startStopButton, Button homeTimeoutButton, Button awayTimeoutButton, boolean isHomeTeam) {
        if (timeoutTimeline != null && timeoutTimeline.getStatus() == Timeline.Status.RUNNING) {
            return; // Do nothing if a timeout is already running
        }

        // Check if the timeout has already been used by the team
        if ((isHomeTeam && homeTimeoutUsed) || (!isHomeTeam && awayTimeoutUsed)) {
            return; // Do nothing if the team has already used their timeout
        }

        // Pause the main timer if it's running
        if (timeline != null) {
            timeline.pause();
        }
        timerRunning = false;
        startStopButton.setText("START");

        // Disable the timeout buttons
        homeTimeoutButton.setDisable(true);
        awayTimeoutButton.setDisable(true);
        startStopButton.setDisable(true);

        // Confirmation dialog
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Timeout Confirmation");
        confirmationDialog.setHeaderText(null);
        confirmationDialog.setContentText("Are you sure you want to start a timeout?");

        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        confirmationDialog.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = confirmationDialog.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            // User confirmed the timeout
            final double originalTime = timeSeconds;
            timeSeconds = timeoutMinutes * 60 + timeoutSeconds;

            // Mark the timeout as used for the team
            if (isHomeTeam) {
                homeTimeoutUsed = true;
            } else {
                awayTimeoutUsed = true;
            }
            timeoutTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
                timeSeconds -= 0.1;
                timerLabel.setText(formatTime(timeSeconds));
                if (timeSeconds <= 0) {
                    timeoutTimeline.stop();
                    timeSeconds = originalTime;
                    timerLabel.setText(formatTime(timeSeconds));
                    homeTimeoutButton.setDisable(homeTimeoutUsed);
                    awayTimeoutButton.setDisable(awayTimeoutUsed);
                    startStopButton.setDisable(false);
                }
            }));
            timeoutTimeline.setCycleCount(Timeline.INDEFINITE);
            timeoutTimeline.play();
        } else {
            // User canceled the timeout
            // Re-enable the timeout buttons
            homeTimeoutButton.setDisable(homeTimeoutUsed);
            awayTimeoutButton.setDisable(awayTimeoutUsed);
            startStopButton.setDisable(false);
        }
    }

    private void startIntermission(Label timerLabel, Button startStopButton, Button homeTimeoutButton, Button awayTimeoutButton) {
        if (intermissionTimeline != null && intermissionTimeline.getStatus() == Timeline.Status.RUNNING) {
            return; // Do nothing if a intermission is already running
        }

        // Pause the main timer if it's running
        if (timeline != null) {
            timeline.pause();
        }
        timerRunning = false;
        startStopButton.setText("START");

        // Disable the timeout buttons and start button
        homeTimeoutButton.setDisable(true);
        awayTimeoutButton.setDisable(true);
        startStopButton.setDisable(true);

            timeSeconds = intermissionMinutes * 60 + intermissionSeconds;


            timeoutTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
                timeSeconds -= 0.1;
                timerLabel.setText(formatTime(timeSeconds));
                if (timeSeconds <= 0) {
                    timeoutTimeline.stop();
                    timeSeconds = 0.0;
                    timerLabel.setText(formatTime(timeSeconds));
                    homeTimeoutButton.setDisable(true);
                    awayTimeoutButton.setDisable(true);
                    startStopButton.setDisable(false);
                }
            }));
            timeoutTimeline.setCycleCount(Timeline.INDEFINITE);
            timeoutTimeline.play();

    }


    private String formatTime(double timeSeconds) {
        int minutes = (int) timeSeconds / 60;
        int seconds = (int) timeSeconds % 60;
        int tenths = (int) ((timeSeconds * 10) % 10);
        return String.format("%02d:%02d.%d", minutes, seconds, tenths);
    }

    private void showAddTeamDialog(Stage primaryStage) {
        Dialog<Team> dialog = new Dialog<>();
        dialog.setTitle("Add New Team");
        dialog.setHeaderText("Add a new team and its players");

        ButtonType addButtonType = new ButtonType("Add Team", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField teamNameField = new TextField();
        teamNameField.setPromptText("Team name");

        TextField playerNumberField = new TextField();
        playerNumberField.setPromptText("No.");
        playerNumberField.setPrefWidth(50);
        TextField playerNameField = new TextField();
        playerNameField.setPromptText("Player name");
        ComboBox<String> playerPositionField = new ComboBox<>();
        playerPositionField.getItems().addAll("G", "F", "D");

        Button addPlayerButton = new Button("Add Player");
        Button resetPlayerButton = new Button("Reset");

        TableView<Player> playerTable = new TableView<>();
        TableColumn<Player, String> numberColumn = new TableColumn<>("No.");
        numberColumn.setCellValueFactory(data -> data.getValue().numberProperty());
        TableColumn<Player, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        TableColumn<Player, String> positionColumn = new TableColumn<>("Position");
        positionColumn.setCellValueFactory(data -> data.getValue().positionProperty());

        playerTable.getColumns().addAll(numberColumn, nameColumn, positionColumn);

        addPlayerButton.setOnAction(e -> {
            String number = playerNumberField.getText();
            String name = playerNameField.getText();
            String position = playerPositionField.getValue();

            if (!number.isEmpty() && !name.isEmpty() && position != null) {
                Player player = new Player(number, name, position);
                playerTable.getItems().add(player);
                playerNumberField.clear();
                playerNameField.clear();
                playerPositionField.getSelectionModel().clearSelection();
            }
        });

        resetPlayerButton.setOnAction(e -> {
            playerNumberField.clear();
            playerNameField.clear();
            playerPositionField.getSelectionModel().clearSelection();
        });

        grid.add(new Label("Team name:"), 0, 0);
        grid.add(teamNameField, 1, 0);
        grid.add(new Separator(), 0, 1, 2, 1);
        grid.add(new Label("No."), 0, 2);
        grid.add(playerNumberField, 1, 2);
        grid.add(new Label("Player name:"), 0, 3);
        grid.add(playerNameField, 1, 3);
        grid.add(new Label("Position:"), 0, 4);
        grid.add(playerPositionField, 1, 4);
        grid.add(new HBox(10, addPlayerButton, resetPlayerButton), 1, 5);
        grid.add(playerTable, 0, 6, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Team team = new Team(teamNameField.getText());
                team.getPlayers().addAll(playerTable.getItems());
                return team;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(team -> teams.add(team));
    }

    private void showAllTeamsDialog(Stage primaryStage) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("All Teams");
        dialog.setHeaderText("List of all teams");

        TableView<Team> teamTable = new TableView<>();
        TableColumn<Team, String> teamNameColumn = new TableColumn<>("Team Name");
        teamNameColumn.setCellValueFactory(data -> data.getValue().nameProperty());

        TableView<Player> playerTable = new TableView<>();
        TableColumn<Player, String> numberColumn = new TableColumn<>("No.");
        numberColumn.setCellValueFactory(data -> data.getValue().numberProperty());
        TableColumn<Player, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        TableColumn<Player, String> positionColumn = new TableColumn<>("Position");
        positionColumn.setCellValueFactory(data -> data.getValue().positionProperty());

        playerTable.getColumns().addAll(numberColumn, nameColumn, positionColumn);

        teamTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                playerTable.setItems(newSelection.getPlayers());
            }
        });

        teamTable.getColumns().add(teamNameColumn);
        teamTable.getItems().addAll(teams);

        VBox content = new VBox(10, teamTable, playerTable);
        content.setPadding(new Insets(20));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        dialog.showAndWait();
    }

    private void showPeriodEndDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Period Ended");
        alert.setHeaderText(null);
        alert.setContentText("The current period has ended.");
        alert.showAndWait();
    }


    private void showSettingsDialog(Stage primaryStage) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Settings");

        // Add settings dialog buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Settings grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Timeout duration spinners
        Spinner<Integer> timeoutMinutesSpinner = new Spinner<>(0, 59, timeoutMinutes);
        timeoutMinutesSpinner.setEditable(true);
        Spinner<Integer> timeoutSecondsSpinner = new Spinner<>(0, 59, timeoutSeconds);
        timeoutSecondsSpinner.setEditable(true);

        // Period duration spinners
        Spinner<Integer> periodMinutesSpinner = new Spinner<>(0, 59, periodMinutes);
        periodMinutesSpinner.setEditable(true);
        Spinner<Integer> periodSecondsSpinner = new Spinner<>(0, 59, periodSeconds);
        periodSecondsSpinner.setEditable(true);

        // Intermission controls
        Label intermissionLabel = new Label("Intermission between periods:");
        ToggleGroup intermissionToggleGroup = new ToggleGroup();
        RadioButton intermissionYesButton = new RadioButton("Yes");
        RadioButton intermissionNoButton = new RadioButton("No");
        intermissionYesButton.setToggleGroup(intermissionToggleGroup);
        intermissionNoButton.setToggleGroup(intermissionToggleGroup);
        intermissionNoButton.setSelected(!intermissionEnabled);
        intermissionYesButton.setSelected(intermissionEnabled);



        // Intermission duration spinners
        Spinner<Integer> intermissionMinutesSpinner = new Spinner<>(0, 59, intermissionMinutes);
        intermissionMinutesSpinner.setEditable(true);
        Spinner<Integer> intermissionSecondsSpinner = new Spinner<>(0, 59, intermissionSeconds);
        intermissionSecondsSpinner.setEditable(true);

        intermissionMinutesSpinner.setDisable(!intermissionEnabled);
        intermissionSecondsSpinner.setDisable(!intermissionEnabled);

        intermissionYesButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            intermissionMinutesSpinner.setDisable(!newValue);
            intermissionSecondsSpinner.setDisable(!newValue);
        });

        grid.add(new Label("Timeout duration (min):"), 0, 0);
        grid.add(timeoutMinutesSpinner, 1, 0);
        grid.add(new Label("Timeout duration (sec):"), 0, 1);
        grid.add(timeoutSecondsSpinner, 1, 1);

        grid.add(new Label("Period duration (min):"), 0, 2);
        grid.add(periodMinutesSpinner, 1, 2);
        grid.add(new Label("Period duration (sec):"), 0, 3);
        grid.add(periodSecondsSpinner, 1, 3);

        grid.add(intermissionLabel, 0, 4);
        grid.add(new HBox(10, intermissionYesButton, intermissionNoButton), 1, 4);

        grid.add(new Label("Intermission duration (min):"), 0, 5);
        grid.add(intermissionMinutesSpinner, 1, 5);
        grid.add(new Label("Intermission duration (sec):"), 0, 6);
        grid.add(intermissionSecondsSpinner, 1, 6);


        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                timeoutMinutes = timeoutMinutesSpinner.getValue();
                timeoutSeconds = timeoutSecondsSpinner.getValue();
                periodMinutes = periodMinutesSpinner.getValue();
                periodSeconds = periodSecondsSpinner.getValue();
                intermissionEnabled = intermissionYesButton.isSelected();
                intermissionMinutes = intermissionMinutesSpinner.getValue();
                intermissionSeconds = intermissionSecondsSpinner.getValue();
            }
            return null;
        });

        dialog.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class Team {
    private final SimpleStringProperty name;
    private final ObservableList<Player> players;

    public Team(String name) {
        this.name = new SimpleStringProperty(name);
        this.players = FXCollections.observableArrayList();
    }

    public String getName() {
        return name.get();
    }

    public ObservableList<Player> getPlayers() {
        return players;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }
}

class Player {
    private final SimpleStringProperty number;
    private final SimpleStringProperty name;
    private final SimpleStringProperty position;

    public Player(String number, String name, String position) {
        this.number = new SimpleStringProperty(number);
        this.name = new SimpleStringProperty(name);
        this.position = new SimpleStringProperty(position);
    }

    public String getNumber() {
        return number.get();
    }

    public String getName() {
        return name.get();
    }

    public String getPosition() {
        return position.get();
    }

    public SimpleStringProperty numberProperty() {
        return number;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty positionProperty() {
        return position;
    }
}