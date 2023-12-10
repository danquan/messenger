package com.example.messenger.client;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import animatefx.animation.FadeIn;
import animatefx.animation.FadeOut;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import static com.example.messenger.client.Controller.users;

public class Room extends Thread implements Initializable {
    @FXML
    public Label clientName;
    @FXML
    public Button chatBtn;
    @FXML
    public Pane chat;
    @FXML
    public TextField msgField;
    @FXML
    public VBox msgRoom;
    @FXML
    public Label online;
    @FXML
    public Label fullName;
    @FXML
    public Label email;
    @FXML
    public Label phoneNo;
    @FXML
    public Label gender;
    @FXML
    public Pane profile;
    @FXML
    public Button profileBtn;
    @FXML
    public TextField fileChoosePath;
    @FXML
    public ImageView proImage;
    @FXML
    public Circle showProPic;
    public ScrollPane scroll;
    private FileChooser fileChooser;
    private File filePath;
    public boolean toggleChat = false, toggleProfile = false;

    BufferedReader reader;
    PrintWriter writer;
    Socket socket;

    public void connectSocket() {
        try {
            socket = new Socket("localhost", 8889);
            System.out.println("Socket is connected with server!");
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String msg = reader.readLine();
                System.out.println(msg);
                String[] tokens = msg.split(" ");
                String cmd = tokens[0];
                String fulmsg = msg.substring(cmd.length() + 1);

                if (cmd.equalsIgnoreCase(Controller.username + ":")) {
                    continue;
                } else if(fulmsg.toString().equalsIgnoreCase("bye")) {
                    break;
                }

                HBox hbox = new HBox(10);
                hbox.setAlignment(Pos.TOP_LEFT);

                Text text = new Text(fulmsg.toString());
                text.setFill(Color.valueOf("#303030"));
                text.getStyleClass().add("msg");
                TextFlow message = new TextFlow(text);
                message.getStyleClass().add("msgReceiveBox");
                message.getStylesheets().add(getClass().getResource("/com/example/Messenger/styles/style.css").toExternalForm());
                hbox.getChildren().add(message);
                Platform.runLater(() -> {
                    msgRoom.getChildren().add(hbox);
                });
            }
            reader.close();
            writer.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void handleProfileBtn(ActionEvent event) {
        if (event.getSource().equals(profileBtn) && !toggleProfile) {
            new FadeIn(profile).play();
            profile.toFront();
            chat.toBack();
            toggleProfile = true;
            toggleChat = false;
            profileBtn.setText("Back");
            setProfile();
        } else if (event.getSource().equals(profileBtn) && toggleProfile) {
            new FadeIn(chat).play();
            chat.toFront();
            toggleProfile = false;
            toggleChat = false;
            profileBtn.setText("Profile");
        }
    }

    public void setProfile() {
        for (User user : users) {
            if (Controller.username.equalsIgnoreCase(user.name)) {
                fullName.setText(user.fullName);
                fullName.setOpacity(1);
                email.setText(user.email);
                email.setOpacity(1);
                phoneNo.setText(user.phoneNo);
                gender.setText(user.gender);
            }
        }
    }

    public void handleSendEvent(MouseEvent event) {
        send();
    }


    public void send() {
        String msg = msgField.getText();
        if (msg.isEmpty()) return;
        writer.println(Controller.username + ": " + msg);
        msgRoom.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.TOP_RIGHT);

        Text text = new Text(msg);
        text.setFill(Color.WHITE);
        text.getStyleClass().add("msg");
        TextFlow message = new TextFlow(text);
        message.getStyleClass().add("msgSendBox");
        message.getStylesheets().add(getClass().getResource("/com/example/Messenger/styles/style.css").toExternalForm());
        hbox.getChildren().add(message);

        msgRoom.getChildren().add(hbox);

        msgField.setText("");
        if(msg.equalsIgnoreCase("BYE") || (msg.equalsIgnoreCase("logout"))) {
            System.exit(0);
        }
    }

    public boolean saveControl = false;

    public void chooseImageButton(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");
        this.filePath = fileChooser.showOpenDialog(stage);
        fileChoosePath.setText(filePath.getPath());
        saveControl = true;
    }

    public void sendMessageByKey(KeyEvent event) {
        if (event.getCode().toString().equals("ENTER")) {
            send();
        }
    }

    public void saveImage() {
        /*
        if (saveControl) {
            try {
                BufferedImage bufferedImage = ImageIO.read(filePath);
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                proImage.setImage(image);
                showProPic.setFill(new ImagePattern(image));
                saveControl = false;
                fileChoosePath.setText("");
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
*/
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        msgRoom.heightProperty().addListener((observable, oldValue, newValue) -> {
            scroll.setVvalue((Double) newValue);
        });

        showProPic.setStroke(Color.valueOf("#90a4ae"));
        Image image;
        if(Controller.gender.equalsIgnoreCase("Male")) {
            image = new Image(getClass().getResource("/com/example/messenger/icons/user.png").toString(), false);
        } else {
            image = new Image(getClass().getResource("/com/example/messenger/icons/female.png").toString(), false);
        }
        proImage.setImage(image);
        showProPic.setFill(new ImagePattern(image));
        clientName.setText(Controller.username);
        connectSocket();
    }
}
