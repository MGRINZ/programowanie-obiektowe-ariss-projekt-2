package com.example.projectnr2javapython;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloController {

    private Stage stage;

    @FXML
    private ComboBox<String> wyborDnia;

    @FXML
    public ComboBox<String> wyborGodziny;

    @FXML
    private ListView<String> harmonogramList;

    @FXML
    private ListView<String> detaleList;

    @FXML
    private HBox pobieranieDanych;

    private int wybranyElement;
    private String dzien = "";
    private static JSONObject json = null;

    public void setJson(String text) {
        json = new JSONObject(text);
    }

    public JSONObject getJson() {
        return json;
    }

    public void setWybranyElement(int we) {
        this.wybranyElement = we;
    }

    public int getWybranyElement() {
        return wybranyElement;
    }

    public void setDzien(String dzien) {
        this.dzien = dzien;
    }

    public String getDzien() {
        return dzien;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        wyborDnia.getItems().add("Wszystkie dni");
        wyborDnia.getItems().add("Poniedziałek");
        wyborDnia.getItems().add("Wtorek");
        wyborDnia.getItems().add("Środa");
        wyborDnia.getItems().add("Czwartek");
        wyborDnia.getItems().add("Piątek");
        wyborDnia.getItems().add("Sobota");
        wyborDnia.getItems().add("Niedziela");

        wyborGodziny.getItems().add("Dowolna");
        for(int i = 0; i < 24; i++)
            wyborGodziny.getItems().add("" + i);

        onWczytajButtonClick();
    }

    @FXML
    private void onHelpButtonClicked(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("HelpScene.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage primaryStage = new Stage();
        primaryStage.setTitle("Pomoc");
        primaryStage.setScene(scene);
        primaryStage.initModality(Modality.WINDOW_MODAL);
        primaryStage.initOwner(((Button)actionEvent.getSource()).getScene().getWindow());

        primaryStage.setMinWidth(250);
        primaryStage.setMinHeight(300);

        primaryStage.sizeToScene();

        primaryStage.show();
    }

    @FXML
    public void dzienWybrany() {
        setWybranyElement(wyborDnia.getSelectionModel().getSelectedIndex());
        System.out.println(getWybranyElement());
        if(getWybranyElement() == 0)
            setDzien("");
        else
            setDzien(wyborDnia.getItems().get(getWybranyElement()));
        System.out.println("Wybrany dzień to " + getDzien());
    }
    
    public void drukujDetali(JSONObject json1) {
        detaleList.getItems().clear();
        int a = harmonogramList.getSelectionModel().getSelectedIndex();

        if(a == -1)
            return;

        JSONArray jsonGrafik = json1.getJSONArray("grafik");

        if(jsonGrafik.isEmpty())
            return;

        JSONObject jsonObject = jsonGrafik.getJSONObject(a);
        System.out.println(jsonObject.toString());
        detaleList.getItems().add("Instruktor: " + jsonObject.getString("instruktor"));
        detaleList.getItems().add("Poziom: " + jsonObject.getString("poziom"));
        detaleList.getItems().add("Zapisy: " + jsonObject.getString("zapisy"));
        JSONArray uwagi = jsonObject.getJSONArray("uwagi");

        ArrayList<String> uwagi_list = new ArrayList<>();
        for(Object uwaga : uwagi)
            uwagi_list.add(uwaga.toString());

        detaleList.getItems().add("Uwagi: " + String.join(", ", uwagi_list));
    }

    public String getTekstStrony() throws IOException {
        String adres = "http://127.0.0.1:5000/";
        ArrayList<String> parametry = new ArrayList<>();

        HashMap<String, String> parametryMap = new HashMap<>();
        parametryMap.put("dzien", getDzien());

        if(wyborGodziny.getSelectionModel().getSelectedIndex() > 0)
            parametryMap.put("godziny", wyborGodziny.getValue());

        for(Map.Entry<String, String> entry : parametryMap.entrySet())
            parametry.add(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));

        String adresZapytania = adres + "?" + String.join("&", parametry);
        System.out.println(adresZapytania);

        URL url = new URL(adresZapytania);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream inputConn = connection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputConn));

        StringBuilder text = new StringBuilder();
        String line;
        while((line = bufferedReader.readLine()) != null)
            text.append(line);

        bufferedReader.close();

        return text.toString();
    }

    public void pokazGrafik() {
        JSONArray jsonGrafik = getJson().getJSONArray("grafik");
        int liczbaZajec = jsonGrafik.length();
        System.out.println("liczba zajec = " + liczbaZajec);
        if(liczbaZajec != 0) {
            for (int a = 0; a < liczbaZajec; a++) {
                JSONObject jsonObject = jsonGrafik.getJSONObject(a);
                harmonogramList.getItems().add(jsonObject.getString("dzien") + "  " + jsonObject.getString("godziny") + "  " + jsonObject.getString("kurs"));
            }
        } else
            harmonogramList.getItems().add("Nie znaleziono zajęć spełniających podane kryteria.");
    }

    @FXML
    public void handleMouseClicked() {
        drukujDetali(getJson());
    }

    @FXML
    protected void onWczytajButtonClick() {
        harmonogramList.getItems().clear();
        detaleList.getItems().clear();
        pobieranieDanych.setVisible(true);

        ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        executorService.submit(() -> {
            try {
                String text = getTekstStrony();

                Platform.runLater(() -> {
                    setJson(text);

                    JSONObject json = getJson();
                    String status = json.getString("status");
                    if(status.equals("sukces"))
                        pokazGrafik();
                    else
                        pokazBlad(json.getString("info"));
                });
            } catch(ConnectException e) {
                Platform.runLater(() -> pokazBlad("Nie można nawiązać połączenia z robotem."));
            } catch (IOException e) {
                Platform.runLater(() -> pokazBlad(e.getMessage()));
            } finally {
                Platform.runLater(() -> pobieranieDanych.setVisible(false));
            }
        });
    }

    private void pokazBlad(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Błąd");
        alert.setTitle("Wystąpił błąd");
        alert.setContentText(text);
        alert.initOwner(stage);
        alert.showAndWait();
    }
}