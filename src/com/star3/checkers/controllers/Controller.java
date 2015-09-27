package com.star3.checkers.controllers;

import com.star3.checkers.models.Color;
import com.star3.checkers.models.Edge;
import com.star3.checkers.models.Pawn;
import com.star3.checkers.services.ColorCalibration;
import com.star3.checkers.services.CountdownTimer;
import com.star3.checkers.services.FrameProcessing;
import com.star3.checkers.services.MoveValidator;
import com.star3.checkers.utils.MatrixToBufferedImageConverter;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;
import org.opencv.videoio.VideoCapture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Controller {

    @FXML
    public GridPane gridPane = new GridPane();

    @FXML
    public ImageView greenThreshold = new ImageView();

    @FXML
    public ImageView blueThreshold = new ImageView();

    @FXML
    public ImageView redThreshold = new ImageView();

    @FXML
    public ImageView cameraViewWithCircles = new ImageView();

    @FXML
    private Label timPlayer1 = new Label();

    @FXML
    private Label timPlayer2 = new Label();

    @FXML
    private Button submitPlayer1 = new Button();

    @FXML
    private Button submitPlayer2 = new Button();


    // Zmienne suwaków do kalibracji

    //GREEN
    @FXML
    private Slider hGreenH = new Slider();

    @FXML
    private Slider hGreenS = new Slider();

    @FXML
    private Slider hGreenV = new Slider();

    @FXML
    private Slider lGreenH = new Slider();

    @FXML
    private Slider lGreenS = new Slider();

    @FXML
    private Slider lGreenV = new Slider();

    //RED
    @FXML
    private Slider hRedH = new Slider();

    @FXML
    private Slider hRedS = new Slider();

    @FXML
    private Slider hRedV = new Slider();

    @FXML
    private Slider lRedH = new Slider();

    @FXML
    private Slider lRedS = new Slider();

    @FXML
    private Slider lRedV = new Slider();

    //BLUE
    @FXML
    private Slider hBlueH = new Slider();

    @FXML
    private Slider hBlueS = new Slider();

    @FXML
    private Slider hBlueV = new Slider();

    @FXML
    private Slider lBlueH = new Slider();

    @FXML
    private Slider lBlueS = new Slider();

    @FXML
    private Slider lBlueV = new Slider();

    //PARAMS
    @FXML
    private Slider param1 = new Slider();

    @FXML
    private Slider param2 = new Slider();

    @FXML
    private Slider param3 = new Slider();

    @FXML
    private Slider param4 = new Slider();

    //CONTRAST BRIGHTNESS

    @FXML
    private Slider alpha = new Slider();

    @FXML
    private Slider beta = new Slider();

    VideoCapture capture;

    BlockingQueue<GridPane> pawns = new ArrayBlockingQueue<>(10);

    Imgproc ip = new Imgproc();

    MatrixToBufferedImageConverter mbic = new MatrixToBufferedImageConverter();

    MoveValidator moveValidator = new MoveValidator();

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private Mat mainFrame = new Mat();
    private ArrayList<Pawn> greenPawns;
    private Collection<Pawn> redPawns;
    private Collection<Pawn> bluePawns;
    private FrameProcessing fp = new FrameProcessing();

    Scalar redLow = new Scalar(170, 110, 60);
    Scalar redHigh = new Scalar(179, 255, 255);

    Scalar blueLow = new Scalar(100, 100, 60);
    Scalar blueHigh = new Scalar(125, 255, 255);

    Scalar greenHigh = new Scalar(90, 255, 255);
    Scalar greenLow = new Scalar(40, 100, 40);

    Scalar redLowS = new Scalar(170, 110, 60);
    Scalar redHighS = new Scalar(179, 255, 255);

    Scalar blueLowS = new Scalar(100, 100, 60);
    Scalar blueHighS = new Scalar(125, 255, 255);

    Scalar greenHighS = new Scalar(90, 255, 255);
    Scalar greenLowS = new Scalar(40, 100, 40);

    ScheduledService<Void> timerTask;

    CountdownTimer timerPlayer1;
    CountdownTimer timerPlayer2;

    private static final Color PLAYER1 = Color.RED;
    private static final Color PLAYER2 = Color.BLUE;

    private Pawn[][] oldBoard = {
            {new Pawn(0, 0, PLAYER1), null, new Pawn(2, 0, PLAYER1), null, new Pawn(4, 0, PLAYER1), null, new Pawn(6, 0, PLAYER1), null},
            {null, new Pawn(1, 1, PLAYER1), null, new Pawn(3, 1, PLAYER1), null, new Pawn(5, 1, PLAYER1), null, new Pawn(7, 1, PLAYER1)},
            {new Pawn(0, 2, PLAYER1), null, new Pawn(2, 2, PLAYER1), null, new Pawn(4, 2, PLAYER1), null, new Pawn(6, 2, PLAYER1), null},
            {null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null},
            {null, new Pawn(1, 5, PLAYER2), null, new Pawn(3, 5, PLAYER2), null, new Pawn(5, 5, PLAYER2), null, new Pawn(7, 5, PLAYER2)},
            {new Pawn(0, 6, PLAYER2), null, new Pawn(2, 6, PLAYER2), null, new Pawn(4, 6, PLAYER2), null, new Pawn(6, 6, PLAYER2), null},
            {null, new Pawn(1, 7, PLAYER2), null, new Pawn(3, 7, PLAYER2), null, new Pawn(5, 7, PLAYER2), null, new Pawn(7, 7, PLAYER2)}
    };


    public enum GameState {
        PLAYER1_TURN, PLAYER2_TURN, PROCESSING, PLAYER1_WON, PLAYER2_WON, READY_TO_START, CALIBRATION_REQUIRED, GAME_STOPPED
    }

    GameState gameState = GameState.CALIBRATION_REQUIRED;

    @FXML
    private void initialize() {
        capture = new VideoCapture("http://192.168.43.1:8080/videofeed?dummyparam=dummy.mjpg");
        Thread readImage = new Thread(() -> {
            while (true) {
                Mat frame = getFrame(); //queueOfPawns();
                setMainFrame(frame);
            }

        });
        readImage.setDaemon(true);
        readImage.start();

        ScheduledService<Void> service = new ScheduledService<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    public Void call() {
                        try {
                            GridPane grid = pawns.take();
                            Platform.runLater(() -> {
                                gridPane.getChildren().clear();
                                gridPane.getChildren().addAll(grid.getChildren());
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
            }
        };
        service.setDelay(Duration.millis(1));
        service.setPeriod(Duration.millis(5));
        service.start();
    }

    static public Mat drawCircle(Mat original, Mat hsv) {

        Imgproc ip = new Imgproc();
        Mat colorMat = new Mat();
        Mat circle = new Mat();

        int blur = 5;

        int par1 = 5;
        int par2 = 20;
        int par3 = 15;
        int par4 = 60;

        ip.GaussianBlur(hsv, colorMat, new Size(blur, blur), blur, blur);
        ip.HoughCircles(colorMat, circle, ip.HOUGH_GRADIENT, 2.0, colorMat.rows() / 8, par1, par2, par3, par4);
        for (int i = 0; i < circle.cols(); i++) {

            double vCircle[] = circle.get(0, i);
            Point p = new Point(vCircle);
            double x = vCircle[0];
            int x0 = (int) Math.round(x);
            double y = vCircle[1];
            int y0 = (int) Math.round(y);
            double radius = vCircle[2];
            int radius0 = (int) Math.round(radius);

            ip.circle(original, p, 3, new Scalar(0, 255, 0), -1, 8, 0);
            // circle outline
            ip.circle(original, p, radius0, new Scalar(0, 0, 255), 3, 8, 0);
        }

        return original;
    }


    ArrayList<Pawn> edges = new ArrayList<Pawn>();

    public ArrayList<Pawn> getCircles(Mat thrMat, Color color, ArrayList<Double> params) {

        ArrayList<Pawn> pawns = new ArrayList<Pawn>();
        Mat circles = new Mat();
        double par0 = params.get(0).doubleValue();
        double par1 = params.get(1).doubleValue();
        int par2 = params.get(2).intValue();
        int par3 = params.get(3).intValue();


        ip.HoughCircles(thrMat, circles, ip.HOUGH_GRADIENT, 2.0, thrMat.rows() / 8, par0, par1, par2, par3);

        for (int i = 0; i < circles.cols(); i++) {

            double vCircle[] = circles.get(0, i);
            double x = vCircle[0];
            int x0 = (int) Math.round(x);
            double y = vCircle[1];
            int y0 = (int) Math.round(y);

            switch (color) {
                case GREEN:
                    pawns.add(new Edge(x0, y0, color));
                    break;
                case RED:
                    pawns.add(new Pawn(x0, y0, color));
                    break;
                case BLUE:
                    pawns.add(new Pawn(x0, y0, color));
                    break;
            }
        }

        return pawns;
    }


    public double getFieldDimension(int a, int b) {
        double value = 0;
        if (a < b) {
            value = b - a;
        } else {
            value = a - b;
        }

        return value;
    }

    private Pawn fields[][] = new Pawn[8][8];
    private double boardStartOffsetX = 0;
    private double boardStartOffsetY = 0;
    private double boardDimensionX = 0;
    private double boardDimensionY = 0;


    private GridPane getGrid(Collection<Pawn> red, Collection<Pawn> blue, ArrayList<Pawn> edges) {

        Collections.sort(edges, new Comparator<Pawn>() {

            public int compare(Pawn o1, Pawn o2) {
                return Integer.compare(o1.getX() + o1.getY(), o2.getX() + o2.getY());
            }
        });

        ImageView img;
        GridPane grid = new GridPane();


        grid.getChildren().clear();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                img = new ImageView();
                if (i % 2 == 0 && j % 2 == 0 || i % 2 == 1 && j % 2 == 1) {
                    img = new ImageView("black.jpg");
                } else {
                    img = new ImageView("white.jpg");
                }
                img.setFitHeight(65.0);
                img.setFitWidth(65.0);
                grid.add(img, i, j);
            }
        }

        if (edges.size() == 2) {
            Pawn last = edges.get(1);
            Pawn first = edges.get(0);

            boardStartOffsetX = first.getX();
            boardStartOffsetY = first.getY();
            boardDimensionX = getFieldDimension(first.getX(), last.getX());
            boardDimensionY = getFieldDimension(first.getY(), last.getY());
            int fieldX = 0;
            int fieldY = 0;

            for (Pawn tmpPawn : blue) {
                fieldX = ((Double) Math.ceil(((tmpPawn.getX() - boardStartOffsetX) / boardDimensionX) / 0.125)).intValue() - 1;
                fieldY = ((Double) Math.ceil(((tmpPawn.getY() - boardStartOffsetY) / boardDimensionY) / 0.125)).intValue() - 1;

                if ((fieldX <= 7 || fieldX >= 0) && (fieldY <= 7 || fieldY >= 0)) {
                    if (fieldY % 2 == 0 && fieldX % 2 == 0 || fieldY % 2 == 1 && fieldX % 2 == 1) {
                        img = new ImageView("blackBlue.jpg");
                    } else {
                        img = new ImageView("whiteBlue.jpg");
                    }

                    img.setFitHeight(65.0);
                    img.setFitWidth(65.0);
                    grid.add(img, fieldX, fieldY);
                }
            }

            for (Pawn tmpPawn : red) {
                fieldX = ((Double) Math.ceil(((tmpPawn.getX() - boardStartOffsetX) / boardDimensionX) / 0.125)).intValue() - 1;
                fieldY = ((Double) Math.ceil(((tmpPawn.getY() - boardStartOffsetY) / boardDimensionY) / 0.125)).intValue() - 1;

                if ((fieldX <= 7 || fieldX >= 0) && (fieldY <= 7 || fieldY >= 0)) {
                    if (fieldY % 2 == 0 && fieldX % 2 == 0 || fieldY % 2 == 1 && fieldX % 2 == 1) {
                        img = new ImageView("blackRed.jpg");
                    } else {
                        img = new ImageView("whiteRed.jpg");
                    }

                    img.setFitHeight(65.0);
                    img.setFitWidth(65.0);
                    grid.add(img, fieldX, fieldY);
                }
            }

        }


        return grid;

    }


    private Pawn[][] getBoard(Mat hsvMatrix, Collection<Pawn> red, Collection<Pawn> blue, ArrayList<Pawn> edges) {


        Pawn board[][] = new Pawn[8][8];

        if (!hsvMatrix.empty() && Edge.getDetected() == 4) {

            Collections.sort(edges, new Comparator<Pawn>() {

                public int compare(Pawn o1, Pawn o2) {
                    return Integer.compare(o1.getX() + o1.getY(), o2.getX() + o2.getY());
                }
            });


            Pawn last = edges.get(3);
            Pawn first = edges.get(0);

            boardStartOffsetX = first.getX();
            boardStartOffsetY = first.getY();
            boardDimensionX = getFieldDimension(first.getX(), last.getX());
            boardDimensionY = getFieldDimension(first.getY(), last.getY());
            int fieldX = 0;
            int fieldY = 0;

            for (Pawn tmpPawn : red) {
                fieldX = ((Double) Math.ceil(((tmpPawn.getX() - boardStartOffsetX) / boardDimensionX) / 0.125)).intValue() - 1;
                fieldY = ((Double) Math.ceil(((tmpPawn.getY() - boardStartOffsetY) / boardDimensionY) / 0.125)).intValue() - 1;
                board[fieldX][fieldY] = tmpPawn;
            }

            for (Pawn tmpPawn : blue) {
                fieldX = ((Double) Math.ceil(((tmpPawn.getX() - boardStartOffsetX) / boardDimensionX) / 0.125)).intValue() - 1;
                fieldY = ((Double) Math.ceil(((tmpPawn.getY() - boardStartOffsetY) / boardDimensionY) / 0.125)).intValue() - 1;
                board[fieldX][fieldY] = tmpPawn;
            }

        } else {
            System.out.println("No nic nie ma :(");
        }
        return board;
    }


    @FXML
    private void turnPlayer1() {
        if (gameState == GameState.PLAYER1_TURN) {
            takeAFrame();
        } else if (gameState == GameState.READY_TO_START) {
            submitPlayer1.setText("Zatwierdź ruch");
            submitPlayer2.setText("Zatwierdź ruch");

            gameState = GameState.PLAYER1_TURN;
        }
    }

    @FXML
    private void turnPlayer2() {
        if (gameState == GameState.PLAYER2_TURN) {
            takeAFrame();
        } else if (gameState == GameState.READY_TO_START) {
            submitPlayer1.setText("Zatwierdź ruch");
            submitPlayer2.setText("Zatwierdź ruch");

            gameState = GameState.PLAYER2_TURN;
        }
    }


    @FXML
    private void startNewGame() {
        GameState stateTmp = gameState;
        gameState = GameState.PROCESSING;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Rozpoczęcie gry");
        alert.setHeaderText("Czy chcesz rozpocząć nową grę?");
        alert.setContentText("Przed rozpoczęciem gry ustaw wszystkie pionki w konfiguracji startowej.\nKliknij 'Ok' gdy wszystkie pionki będą ustawione.");

        Optional<ButtonType> alertResult = alert.showAndWait();
        if (alertResult.get() == ButtonType.OK) {
            timerPlayer2 = new CountdownTimer(10, 0, 0);
            timerPlayer1 = new CountdownTimer(10, 0, 0);

            timPlayer1.setText(timerPlayer1.toString());
            timPlayer2.setText(timerPlayer2.toString());

            submitPlayer1.setText("Rozpocznij grę");
            submitPlayer2.setText("Rozpocznij grę");

            timerTask = new ScheduledService<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() {
                            if (gameState == GameState.PLAYER1_TURN) {
                                if (timerPlayer1.timedOut()) {
                                    gameState = GameState.PLAYER2_WON;
                                    winnerPopup("Gracz 1");
                                } else {
                                    timerPlayer1.countdown();
                                }
                            } else if (gameState == GameState.PLAYER2_TURN) {
                                if (timerPlayer2.timedOut()) {
                                    gameState = GameState.PLAYER1_WON;
                                    winnerPopup("Gracz 2");
                                } else {
                                    timerPlayer2.countdown();
                                }
                            }

                            Platform.runLater(() -> {
                                if (gameState == GameState.PLAYER1_TURN) {
                                    timPlayer1.setText(timerPlayer1.toString());
                                } else if (gameState == GameState.PLAYER2_TURN) {
                                    timPlayer2.setText(timerPlayer2.toString());
                                }
                            });
                            return null;
                        }
                    };
                }
            };
            timerTask.setDelay(Duration.millis(0));
            timerTask.setPeriod(Duration.millis(5));
            timerTask.start();

            gameState = GameState.READY_TO_START;


        } else {
            gameState = stateTmp;
        }
    }


    @FXML
    private void stopGame() {
        gameState = GameState.GAME_STOPPED;
    }

    private void winnerPopup(String playerName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Gra skończona");
        alert.setHeaderText(playerName + " zwyciężył!");

        gameState = GameState.GAME_STOPPED;
    }

    @FXML
    private void calibartionWizard() {
        if (gameState == GameState.CALIBRATION_REQUIRED || gameState == GameState.GAME_STOPPED) {
            try {
                boolean status = calibrationGreenColor();
                if (status) {
                    status = calibrationBlueColor();
                    if (status) {
                        status = calibrationRedColor();
                    }
                }

                gameState = status ? GameState.READY_TO_START : GameState.GAME_STOPPED;
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Błąd kalibracji!");
                alert.setHeaderText("Błąd!");
                alert.setContentText("Wystąpił błąd podczas kalibracji kamery. Spróbuj ponownie!");

                alert.showAndWait();
            }
        }
    }

    private boolean calibrationGreenColor() throws Exception {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Kalibarcja kamery");
        alert.setHeaderText("Przeprowadź kalibrację koloru zielonego");
        alert.setContentText("Ustaw 2 zielone pionki oraz po jednym niebieskim i czerwonym na planszy w dowolnych miejscach.\nKliknij 'Ok' aby kontynuować.");

        Optional<ButtonType> alertResult = alert.showAndWait();
        if (alertResult.get() == ButtonType.OK) {
            ColorCalibration greenCalibration = new ColorCalibration();
            Mat frame = Imgcodecs.imread("kalibracjaZielony.png"); // getFrame()
            ArrayList<Double> colors = greenCalibration.getColor(frame, 3, 80);

            greenHighS = new Scalar(colors.get(1), 255, 255);
            greenLowS = new Scalar(colors.get(0), 100, 60);
            return true;
        } else {
            return false;
        }
    }

    private boolean calibrationRedColor() throws Exception {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Kalibarcja kamery");
        alert.setHeaderText("Przeprowadź kalibrację koloru czerwonego");
        alert.setContentText("Ustaw 2 czerwone pionki oraz po jednym niebieskim i zieloneym na planszy w dowolnych miejscach.\nKliknij 'Ok' aby kontynuować.");

        Optional<ButtonType> alertResult = alert.showAndWait();
        if (alertResult.get() == ButtonType.OK) {
            ColorCalibration redCalibration = new ColorCalibration();
            Mat frame = getFrame();
            ArrayList<Double> colors = redCalibration.getColor(frame, 3, 165);
            redHighS = new Scalar(colors.get(1), 255, 255);
            redLowS = new Scalar(colors.get(0), 100, 60);
            return true;
        } else {
            return false;
        }
    }

    private boolean calibrationBlueColor() throws Exception {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Kalibarcja kamery");
        alert.setHeaderText("Przeprowadź kalibrację koloru niebieskiego");
        alert.setContentText("Ustaw 2 niebieskie pionki oraz po jednym zielonym i czerwonym na planszy w dowolnych miejscach.\nKliknij 'Ok' aby kontynuować.");

        Optional<ButtonType> alertResult = alert.showAndWait();
        if (alertResult.get() == ButtonType.OK) {
            ColorCalibration blueCalibration = new ColorCalibration();
            Mat frame = getFrame();
            ArrayList<Double> colors = blueCalibration.getColor(frame, 3, 111);
            blueHighS = new Scalar(colors.get(1), 255, 255);
            blueLowS = new Scalar(colors.get(0), 100, 60);
            return true;
        } else {
            return false;
        }
    }


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @FXML
    private void takeAFrame() {

        Color current = null;
        if (gameState.equals(GameState.PLAYER1_TURN)) {
            current = Color.BLUE;
        } else if (gameState.equals(GameState.PLAYER2_TURN)) {
            current = Color.RED;
        }

        gameState = GameState.PROCESSING;

        setColorRanges();

        Mat frame = getMainFrame(); //Imgcodecs.imread("contrastFrame.jpg");
        Mat denoise = new Mat();

        Mat contrastFrame = new Mat(frame.rows(), frame.cols(), frame.type());
        frame.convertTo(contrastFrame, -1, alpha.getValue(), beta.getValue());

        Photo.fastNlMeansDenoisingColored(contrastFrame, denoise);
//        saveFrameToFile(denoise, "testFrame");
//        saveFrameToFile(contrastFrame, "contrastFrame");
//        Mat contrastFrame = Imgcodecs.imread("contrastFrame.png");

        ArrayList<Double> params = getHoughParams();


        Mat greenThr = fp.getThresholdMat(denoise, greenHigh, greenLow);
        displayFrame(greenThr, greenThreshold);

        Mat blueThr = fp.getThresholdMat(denoise, blueHigh, blueLow);
        displayFrame(blueThr, blueThreshold);

        Mat redThr = fp.getThresholdMat(denoise, redHigh, redLow);
        displayFrame(redThr, redThreshold);

        denoise = fp.drawCircles(denoise, greenThr, params);
        denoise = fp.drawCircles(denoise, blueThr, params);
        denoise = fp.drawCircles(denoise, redThr, params);
        displayFrame(denoise, cameraViewWithCircles);
        greenPawns = getCircles(greenThr, Color.GREEN, params);
        redPawns = getCircles(redThr, Color.RED, params);
        bluePawns = getCircles(blueThr, Color.BLUE, params);
        GridPane gridPane = getGrid(redPawns, bluePawns, greenPawns);

        try {
            pawns.put(gridPane);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Pawn[][] newBoard = fp.translateCollectionToBoardMatrix(redPawns, bluePawns, greenPawns);

        if (current != null) {

            moveValidator.initialize(oldBoard, newBoard, current);

            boolean validationResult = moveValidator.validate();
            if (validationResult) {
                oldBoard = newBoard;
                gameState = Color.RED.equals(current) ? GameState.PLAYER2_TURN : GameState.PLAYER1_TURN;
                System.out.println("VALIDATION: TRUE");
            } else {
                System.out.println("VALIDATION: FALSE");
            }
        }
    }

    @FXML
    private void validateMove() {
        Color current = null;
        if (gameState.equals(GameState.PLAYER1_TURN)) {
            current = Color.BLUE;
        } else if (gameState.equals(GameState.PLAYER2_TURN)) {
            current = Color.RED;
        }

//        Pawn[][] oldBoard = new Pawn[8][8];
        Pawn[][] newBoard = new Pawn[8][8];
//
//        oldBoard[7][7] = new Pawn(0, 0, Color.RED);
//        oldBoard[5][5] = new Pawn(1, 1, Color.BLUE);
//        //oldBoard[5][1] = new Pawn(5,1, Color.BLUE);
//
//        newBoard[6][6] = new Pawn(0, 6, Color.RED);
//        newBoard[5][5] = new Pawn(1, 1, Color.BLUE);

        newBoard = fp.translateCollectionToBoardMatrix(redPawns, bluePawns, greenPawns);
        moveValidator.initialize(oldBoard, newBoard, current);

        boolean validationResult = moveValidator.validate();
        if (validationResult) {
            oldBoard = newBoard;
            System.out.println("VALIDATION: TRUE");
        } else {
            System.out.println("VALIDATION: FALSE");
        }
    }

    private Mat getFrame() {
        Mat capturedFrame = new Mat();
        if (capture.isOpened()) {
            capture.read(capturedFrame);
        }

        return capturedFrame;
    }

    private void displayFrame(Mat frame, ImageView imgView) {
        mbic.setMatrix(frame, ".png");
        imgView.setImage(SwingFXUtils.toFXImage(mbic.getImage(), null));
    }

    private void saveFrameToFile(Mat frame, String fileName) {
        mbic.setMatrix(frame, ".png");
        BufferedImage bufim = mbic.getImage();

        BufferedImage bImage = SwingFXUtils.fromFXImage(SwingFXUtils.toFXImage(bufim, null), null);
        File f = new File(fileName + ".png");
        try {
            ImageIO.write(bImage, "png", f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setColorRanges() {
        greenHigh = new Scalar(hGreenH.getValue(), hGreenS.getValue(), hGreenV.getValue()); //55,255,255
        greenLow = new Scalar(lGreenH.getValue(), lGreenS.getValue(), lGreenV.getValue()); // 42,94,97

        redHigh = new Scalar(hRedH.getValue(), hRedS.getValue(), hRedV.getValue()); // 180,255,255
        redLow = new Scalar(lRedH.getValue(), lRedS.getValue(), lRedV.getValue()); // 150,80,70

        blueHigh = new Scalar(hBlueH.getValue(), hBlueS.getValue(), hBlueV.getValue()); // 120,255,255
        blueLow = new Scalar(lBlueH.getValue(), lBlueS.getValue(), lBlueV.getValue()); // 110,90,168
    }

    private ArrayList<Double> getHoughParams() {
        ArrayList<Double> result = new ArrayList<>();

        result.add(param1.getValue());
        result.add(param2.getValue());
        result.add(param3.getValue());
        result.add(param4.getValue());

        return result;
    }

    synchronized public Mat getMainFrame() {
        return mainFrame;
    }

    synchronized public void setMainFrame(Mat mainFrame) {
        this.mainFrame = mainFrame;
    }
}
