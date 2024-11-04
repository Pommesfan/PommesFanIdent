import javax.swing.*;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Main {
    public static final Scanner sc = new Scanner(System.in);
    public static final String introMessage = "Aktion auswählen:\n1: Öffentliches Profil erstellen\n" +
            "2: Ausweis erstellen\n3: Ausweis prüfen\n4: Öffentliches Profil exportieren\n" +
            "5: Öffentliches Profil importieren\n6: Ausweis exportieren\n7: Ausweis importieren\n" +
            "8: Ausweis kontrollieren über Netzwerkverbindung\n9: Ausweis zeigen über Netzwerkverbindung";

    public static void main(String[] args) throws Exception {
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
        }
    }

    private static void doGenerateKeyPair() throws NoSuchAlgorithmException, IOException {
        System.out.println("Name für Öffentliches Profil:");
        Controller.generateKeyPair(sc.next());
    }

    private static void doGenerateID() throws ParseException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
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
        // get personalPicture
        System.out.println("Bild auswählen");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
        File personalPicture = fileChooser.getSelectedFile();
        if(personalPicture == null) {
            return;
        }
        Controller.generateID(publicProfile, name, surname, date, address, personalPicture);
    }

    private static void doCheckPersonalID() throws Exception {
        System.out.println("Ausweis auswählen");
        Controller.checkPersonalID(sc.next());
    }

    private static void doImportPublicProfile() throws IOException {
        System.out.println("Öffentliches Profil aus Datei auswählen!");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
        File publicProfile = fileChooser.getSelectedFile();
        if(publicProfile == null) {
            return;
        }
        Controller.importPublicProfile(publicProfile);
    }

    private static void doImportPersonalID() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        System.out.println("Ausweis aus Datei auswählen!");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
        File source = fileChooser.getSelectedFile();
        if(source == null) {
            return;
        }
        Controller.importPersonalID(source);
    }

    private static void doExportPublicProfile() throws IOException {
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
        Controller.exportPublicProfile(profileName, destination);
    }

    private static void doExportPersonalID() throws IOException {
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
        Controller.exportPersonalID(id_number, destination);
    }

    private static void doCheckPersonalIDFromRemote() throws Exception {
        Controller.checkPersonalIDFromRemote();
    }

    private static void doHandInPersonalIDtoRemote() throws IOException {
        System.out.println("Ausweisnummer angeben:");
        String id_number = sc.next();
        System.out.println("IP-Adresse angeben:");
        String ip = sc.next();
        System.out.println("Portnummer angeben:");
        int port = sc.nextInt();
        Controller.handInPersonalIDtoRemote(id_number, ip, port);
    }
}
