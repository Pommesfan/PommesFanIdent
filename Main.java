import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, ParseException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        System.out.println("Aktion auswählen:\n1: Öffentliches Profil erstellen\n2: Ausweis erstellen\n3: Ausweis prüfen");
        int mode = sc.nextInt();
        switch (mode) {
            case 1: generateKeyPair(); break;
            case 2: generateID(); break;
            case 3: checkPersonalID(); break;
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

    private static void generateID() throws ParseException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
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

        //Save ID
        Personal_ID personalId = new Personal_ID(ID_number, name, surname, date, address);
        byte[] personalId_b = personalId.toByte();
        File f = new File("PersonalIDs/" + ID_number + "/id");
        f.getParentFile().mkdirs();
        f.createNewFile();
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(personalId_b);

        //Create signature
        byte[] signatur_b = sign_id(personalId_b);
        f = new File("PersonalIDs/" + ID_number + "/signature");
        fos = new FileOutputStream(f);
        fos.write(signatur_b);
    }

    private static byte[] sign_id(byte[] personalIdB) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        System.out.println("Öffentliches Profil auswählen");

        //read out save key
        File privateKeyFile = new File("MyPublicProfiles/" + sc.next() + "/private");
        byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //sign message
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyFactory.generatePrivate(spec));
        signature.update(personalIdB);
        return signature.sign();
    }

    private static void checkPersonalID() throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException {
        System.out.println("Ausweis auswählen");
        String id_number = sc.next();

        //load personal id
        File f = new File("PersonalIDs/" + id_number + "/id");
        byte[] personal_id_b = Files.readAllBytes(f.toPath());

        //load signature
        f = new File("PersonalIDs/" + id_number + "/signature");
        byte[] signature_b = Files.readAllBytes(f.toPath());

        System.out.println("Öffentliches Profil auswählen");
        //load public key
        File privateKeyFile = new File("MyPublicProfiles/" + sc.next() + "/public");
        byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(keyFactory.generatePublic(spec));
        publicSignature.update(personal_id_b);
        boolean isCorrect = publicSignature.verify(signature_b);
        if (isCorrect) {
           System.out.println("Ausweis ist korrekt");
        } else {
            System.out.println("Ausweis ist nicht korrekt");
        }
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
