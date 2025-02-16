package view;

import controller.Controller;
import model.PublicProfile;
import utils.Observer;
import utils.OutputEvent;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Scanner;

public class TUI implements Observer<OutputEvent> {
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

    private void doGenerateKeyPair() throws NoSuchAlgorithmException, IOException, ParseException {
        System.out.println("Name für Öffentliches Profil:");
        String name = sc.next();
        System.out.println("Folgenummer:");
        int sequenceNumber = sc.nextInt();
        System.out.println("Dynamische Attribute:\nAnzahl eingeben, dann Attribute:");
        String[] dynamicAttributes = new String[sc.nextInt()];
        for (int i = 0; i < dynamicAttributes.length; i++) {
            dynamicAttributes[i] = sc.next();
        }
        System.out.println("Datum gültig ab:");
        String validFrom = sc.next();
        System.out.println("Datum gültig bis für Erstellung:");
        String validUntilForCreation = sc.next();
        System.out.println("Datum gültig bis für Erstellte:");
        String validUntilForCreated = sc.next();
        System.out.println("Tage für maximale Gültigkeit:");
        int maxValidDays = sc.nextInt();
        PublicProfile.ValidityPeriod validityPeriod = new PublicProfile.ValidityPeriod(validFrom, validUntilForCreation, validUntilForCreated, maxValidDays);
        controller.generateKeyPair(name, sequenceNumber, validityPeriod, dynamicAttributes);
    }

    private void doGenerateID() throws Exception {
        System.out.println("Öffentliches Profil auswählen");
        String publicProfile = sc.next();
        System.out.println("Folgenummer Profil:");
        int sequence_number = sc.nextInt();
        System.out.println("Gültig bis:");
        String valid_until = sc.next();
        System.out.println("Vorname");
        String name = sc.next();
        System.out.println("Nachname");
        String surname = sc.next();
        System.out.println("Geburtsdatum");
        String birthdate = sc.next();
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

        controller.generateID(controller, publicProfile, sequence_number, valid_until, name, surname, birthdate, address, dynamicAttributeValues, personalPicture, handSignature);
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
        controller.importPublicProfile(Files.newInputStream(publicProfile.toPath()));
    }

    private void doImportPersonalID() throws Exception {
        System.out.println("Ausweis aus Datei auswählen!");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
        File source = fileChooser.getSelectedFile();
        if(source == null) {
            return;
        }
        controller.importPersonalID(Files.newInputStream(source.toPath()));
    }

    private void doExportPublicProfile() throws IOException {
        System.out.println("Profilname:");
        String profileName = sc.next();
        System.out.println("Folgenummer Profil:");
        int sequence_number = sc.nextInt();
        System.out.println("Zielordner wählen!");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(profileName + "-" + sequence_number));
        int res = fileChooser.showSaveDialog(null);
        File destination = fileChooser.getSelectedFile();
        if(res != JFileChooser.APPROVE_OPTION) {
            return;
        }
        controller.exportPublicProfile(profileName, sequence_number, destination);
    }

    private void doExportPersonalID() throws Exception {
        System.out.println("Ausweisnummer angeben");
        String id_number = sc.next();
        System.out.println("Zielordner wählen!");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(id_number.toUpperCase()));
        int res = fileChooser.showSaveDialog(null);
        File destination = fileChooser.getSelectedFile();
        if(res != JFileChooser.APPROVE_OPTION) {
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
        System.out.println("Krypto-Passwort:");
        String password = sc.next();
        controller.handInPersonalIDtoRemote(id_number, ip, port, password);
    }

    private void doShowPublicProfile() throws IOException {
        System.out.println("Profilname:");
        String profile_name = sc.next();
        System.out.println("Folgenummer Profil:");
        int sequence = sc.nextInt();
        controller.showPublicProfile(profile_name, sequence);
    }

    @Override
    public void update(OutputEvent e) {
        if(e instanceof OutputEvent.PersonalIDValidEvent) {
            System.out.println("Ausweis ist korrekt\n");
            System.out.println(((OutputEvent.PersonalIDValidEvent) e).personalIDprintout);
        } else if(e instanceof OutputEvent.PersonalIDInvalidEvent){
            System.out.println("Ausweis ist nicht korrekt\n");
        } else if (e instanceof OutputEvent.ServerStartedEvent serverStartedEvent) {
            System.out.println("IP-Adresse:");
            System.out.println(serverStartedEvent.ip);
            System.out.println("Portnummer:");
            System.out.println(serverStartedEvent.port);
            System.out.println("Krypto-Passwort:");
            System.out.println(serverStartedEvent.password);
        } else if (e instanceof OutputEvent.NoSuchProfileEvent evt) {
            if(evt.namePresent)
                System.out.println("Profil mit der Sequenznummer " + evt.sequence_number + " nicht gespeichert, aber Profilname " + evt.name + " gespeichert");
            else {
                System.out.println("Profil mit dem Profilnamen " + evt.name + " nicht gespeichert");
            }
        } else if (e instanceof OutputEvent.DynamicAttributesDoesntFitEvent evt) {
            System.out.println("Anzahl dynamischer Attribute unpassend: Profil hat " + evt.nDynamicAttributes + " Attribute");
        } else if (e instanceof OutputEvent.ShowProfileEvent) {
            System.out.println(((OutputEvent.ShowProfileEvent) e).msg);
        } else if (e instanceof  OutputEvent.ProfileAlreadyExistsEvent) {
            System.out.println("Profil mit diesem Namen sowie Folgenummer bereits gespeichert");
        } else if (e instanceof OutputEvent.IDalreadyExistsEvent) {
            System.out.println("Ausweis mit dieser Ausweisnummer bereits gespeichert");
        } else if (e instanceof OutputEvent.InvalidDateEvent) {
            System.out.println("Fehlerhafte Datumsangabe");
        } else if (e instanceof OutputEvent.InvalidDateSequenceEvent) {
            System.out.println("Reihenfolge der Datumsangaben für Profil ungültig");
        } else if (e instanceof OutputEvent.PersonalIDoutOfValidityPeriod) {
            System.out.println("Gültigkeitsdatum von Ausweis passt nicht zu Profil");
        } else if (e instanceof OutputEvent.NoSuchPersonalIDevent evt) {
            System.out.println("Ausweis mit der Nummer: " + evt.idNumber + " nicht gespeichert");
        } else if (e instanceof OutputEvent.PersonalIDoutdatedEvent evt) {
            System.out.println("Ausweis mit der Nummer: " + evt.idNumber + " abgelaufen");
        }
    }
}
