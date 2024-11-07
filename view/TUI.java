package view;

import controller.Controller;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TUI implements Observer {
    public static final String introMessage = "Aktion auswählen:\n1: Öffentliches Profil erstellen\n" +
            "2: Ausweis erstellen\n3: Ausweis prüfen\n4: Öffentliches Profil exportieren\n" +
            "5: Öffentliches Profil importieren\n6: Ausweis exportieren\n7: Ausweis importieren\n" +
            "8: Ausweis kontrollieren über Netzwerkverbindung\n9: Ausweis zeigen über Netzwerkverbindung\n10: Öffentliches Profil anschauen";

    private final Scanner sc;
    private final Controller controller;

    public TUI(Controller controller) {
        this.controller = controller;
        sc = new Scanner(System.in);
        controller.addObserver(this);
    }

    public void processUserInput() throws Exception {
        System.out.println(introMessage);
        int mode = sc.nextInt();
        switch (mode) {
            case 1: doGenerateKeyPair(); break;
            case 2: doGenerateID(); break;
            case 3: doCheckPersonalID(); break;
            case 4: doExportPublicProfile(); break;
            case 5: doImportPublicProfile(); break;
            case 6: doExportPersonalID(); break;
            case 7: doImportPersonalID(); break;
            case 8: doCheckPersonalIDFromRemote(); break;
            case 9: doHandInPersonalIDtoRemote(); break;
            case 10: doShowPublicProfile(); break;
        }
    }

    private void doGenerateKeyPair() throws NoSuchAlgorithmException, IOException {
        System.out.println("Name für Öffentliches Profil:");
        String name = sc.next();
        System.out.println("Dynamische Attribute:\nAnzahl eingeben, dann Attribute:");
        String[] dynamicAttributes = new String[sc.nextInt()];
        for (int i = 0; i < dynamicAttributes.length; i++) {
            dynamicAttributes[i] = sc.next();
        }
        controller.generateKeyPair(name, dynamicAttributes);
    }

    private void doGenerateID() throws Exception {
        System.out.println("Öffentliches Profil auswählen");
        String publicProfile = sc.next();
        System.out.println("Vorname");
        String name = sc.next();
        System.out.println("Nachname");
        String surname = sc.next();
        System.out.println("Geburtsdatum");
        String birthdate = sc.next();
        Date date = new SimpleDateFormat("dd.MM.yyyy").parse(birthdate);
        System.out.println("Adresse");
        String address = sc.next();
        System.out.println("Dynamische Attribute:\nAnzahl eingeben, dann Attribute");
        String[] dynamicAttributeValues = new String[sc.nextInt()];
        for (int i = 0; i < dynamicAttributeValues.length; i++) {
            dynamicAttributeValues[i] = sc.next();
        }
        // get personalPicture
        System.out.println("Passbild auswählen:");

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
        File personalPicture = fileChooser.getSelectedFile();
        if(personalPicture == null) {
            return;
        }

        System.out.println("Bild von händischer Unterschrift auswählen:");
        fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
        File handSignature = fileChooser.getSelectedFile();
        if(handSignature == null) {
            return;
        }

        controller.generateID(publicProfile, name, surname, date, address, dynamicAttributeValues, personalPicture, handSignature);
    }

    private void doCheckPersonalID() throws Exception {
        System.out.println("Ausweis auswählen");
        controller.checkPersonalID(sc.next());
    }

    private void doImportPublicProfile() throws IOException {
        System.out.println("Öffentliches Profil aus Datei auswählen!");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
        File publicProfile = fileChooser.getSelectedFile();
        if(publicProfile == null) {
            return;
        }
        controller.importPublicProfile(publicProfile);
    }

    private void doImportPersonalID() throws Exception {
        System.out.println("Ausweis aus Datei auswählen!");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
        File source = fileChooser.getSelectedFile();
        if(source == null) {
            return;
        }
        controller.importPersonalID(source);
    }

    private void doExportPublicProfile() throws IOException {
        System.out.println("Profilname angeben");
        String profileName = sc.next();
        System.out.println("Zielordner wählen!");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(profileName));
        fileChooser.showSaveDialog(null);
        File destination = fileChooser.getSelectedFile();
        if(destination == null) {
            return;
        }
        controller.exportPublicProfile(profileName, destination);
    }

    private void doExportPersonalID() throws Exception {
        System.out.println("Ausweisnummer angeben");
        String id_number = sc.next();
        System.out.println("Zielordner wählen!");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(id_number.toUpperCase()));
        fileChooser.showSaveDialog(null);
        File destination = fileChooser.getSelectedFile();
        if(destination == null) {
            return;
        }
        controller.exportPersonalID(id_number, destination);
    }

    private void doCheckPersonalIDFromRemote() throws Exception {
        controller.checkPersonalIDFromRemote();
    }

    private void doHandInPersonalIDtoRemote() throws Exception {
        System.out.println("Ausweisnummer angeben:");
        String id_number = sc.next();
        System.out.println("IP-Adresse angeben:");
        String ip = sc.next();
        System.out.println("Portnummer angeben:");
        int port = sc.nextInt();
        controller.handInPersonalIDtoRemote(id_number, ip, port);
    }

    private void doShowPublicProfile() {
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
