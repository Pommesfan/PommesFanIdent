import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, ParseException {
        System.out.println("Aktion auswählen:\n1: Öffentliches Profil erstellen\n2: Ausweis erstellen");
        int mode = sc.nextInt();
        switch (mode) {
            case 1: generateKeyPair(); break;
            case 2: generateID();
        }
    }

    public static void generateKeyPair() throws NoSuchAlgorithmException, IOException {
        System.out.println("Name für Öffentliches Profil:");
        String profileName = sc.next();
        KeyPairGenerator gpk = KeyPairGenerator.getInstance("RSA");
        gpk.initialize(2048);
        KeyPair keyPair = gpk.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        File f = new File("MyPublicProfiles/" + profileName + "/private");
        f.getParentFile().mkdirs();
        f.createNewFile();
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(privateKey.getEncoded());
        fos.close();

        f = new File("MyPublicProfiles/" + profileName + "/public");
        f.getParentFile().mkdirs();
        f.createNewFile();
        fos = new FileOutputStream(f);
        fos.write(publicKey.getEncoded());
        fos.close();
    }

    private static void generateID() throws ParseException {
        String ID_number = getAlphanumeric(8);
        System.out.println(ID_number);
        System.out.println("Vorname");
        String name = sc.next();
        System.out.println("Nachname");
        String surname = sc.next();
        System.out.println("Geburtsdatum");
        String birthdate = sc.next();
        Date date = new SimpleDateFormat("dd.mm.yyyy").parse(birthdate);
        System.out.println("Adresse");
        String address = sc.next();

        Personal_ID personalId = new Personal_ID(ID_number, name, surname, date, address);
    }

    private static String getAlphanumeric(int count) {
        Random r = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            int n = r.nextInt(36);
            char c;
            if(n < 10) {
                c = (char) (n + 48);
            } else {
                c = (char) (n + 55);
            }
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }
}
