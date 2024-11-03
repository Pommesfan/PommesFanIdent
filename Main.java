import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Main {
    public static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        System.out.println("Aktion auswählen:\n1: Öffentliches Profil erstellen\n2: Ausweis erstellen\n3: Ausweis prüfen\n4: Öffentliches Profil exportieren\n5: Öffentliches Profil importieren\n6: Ausweis exportieren\n7: Ausweis importieren");
        int mode = sc.nextInt();
        switch (mode) {
            case 1: doGenerateKeyPair(); break;
            case 2: doGenerateID(); break;
            case 3: doCheckPersonalID(); break;
            case 4: doExportPublicProfile(); break;
            case 5: doImportPublicProfile(); break;
            case 6: doExportPersonalID(); break;
            case 7: doImportPersonalID(); break;
        }
    }

    private static void doGenerateKeyPair() throws NoSuchAlgorithmException, IOException {
        System.out.println("Name für Öffentliches Profil:");
        generateKeyPair(sc.next());
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
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
        File personalPicture = fileChooser.getSelectedFile();
        if(personalPicture == null) {
            return;
        }
        generateID(publicProfile, name, surname, date, address, personalPicture);
    }

    private static void doCheckPersonalID() throws Exception {
        System.out.println("Ausweis auswählen");
        checkPersonalID(sc.next().toUpperCase());
    }

    private static void doImportPublicProfile() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
        File publicProfile = fileChooser.getSelectedFile();
        if(publicProfile == null) {
            return;
        }
        importPublicProfile(publicProfile);
    }

    private static void doImportPersonalID() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
        File source = fileChooser.getSelectedFile();
        if(source == null) {
            return;
        }
        importPersonalID(source);
    }

    private static void doExportPublicProfile() throws IOException {
        System.out.println("Profilname angeben");
        exportPublicProfile(sc.next());
    }

    private static void doExportPersonalID() throws IOException {
        System.out.println("Ausweisnummer angeben");
        exportPersonalID(sc.next());
    }

    public static void generateKeyPair(String profileName) throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator gpk = KeyPairGenerator.getInstance("RSA");
        gpk.initialize(2048);
        KeyPair keyPair = gpk.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        File f = Utils.createFileAndSubfolder("MyPublicProfiles/" + profileName + "/private");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(privateKey.getEncoded());
        fos.close();

        f = Utils.createFileAndSubfolder("MyPublicProfiles/" + profileName + "/public");
        fos = new FileOutputStream(f);
        fos.write(publicKey.getEncoded());
        fos.close();
    }

    private static void generateID(String publicProfile, String name, String surname, Date date, String address, File personalPicture) throws ParseException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        String ID_number = Utils.getAlphanumeric(8);
        System.out.println(ID_number);
        File privateKeyFile = new File("MyPublicProfiles/" + publicProfile + "/private");

        String personalPictureFileName = personalPicture.getName();
        String internalPath = "PersonalImages/" + personalPictureFileName;
        File imageDir = new File("PersonalImages/");
        imageDir.mkdirs();
        Files.copy(Paths.get(personalPicture.toURI()), Paths.get(internalPath));

        Personal_ID personalId = new Personal_ID(ID_number, publicProfile,  name, surname, date, address, personalPictureFileName);
        byte[] personalId_b = personalId.toByte(true);

        //Create signature
        byte[] personalId_with_personal_image_b = Utils.concat_bytes(personalId_b, Files.readAllBytes(personalPicture.toPath()));
        //ask for Hallo.jpg profile before, when quitting don't save anything
        byte[] signatur_b = sign_id(personalId_with_personal_image_b, privateKeyFile);

        String distPath = "CreatedPersonalIDs/" + ID_number;

        //Save ID
        File f = Utils.createFileAndSubfolder( distPath + "/id");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(personalId_b);

        f = new File(distPath + "/signature");
        fos = new FileOutputStream(f);
        fos.write(signatur_b);
    }

    private static byte[] sign_id(byte[] personalIdB, File privateKeyFile) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //sign message
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyFactory.generatePrivate(spec));
        signature.update(personalIdB);
        return signature.sign();
    }

    private static boolean validateSignature(byte[] personal_id_b, String publicProfile, byte[] signature_b) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        File publicKeyFile = new File("ImportedPublicProfiles/" + publicProfile);
        byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(keyFactory.generatePublic(spec));
        publicSignature.update(personal_id_b);
        return publicSignature.verify(signature_b);
    }

    private static void checkPersonalID(String id_number) throws Exception {
        String distPath = "ImportedPersonalIDs/" + id_number;

        //load personal id
        File f = new File(distPath + "/id");
        byte[] personal_id_b = Files.readAllBytes(f.toPath());

        //load signature
        f = new File(distPath + "/signature");
        byte[] signature_b = Files.readAllBytes(f.toPath());
        String[] personal_id_s = new String(personal_id_b).split("\n");

        Personal_ID personalId = new Personal_ID(personal_id_s);
        String personalImage = "PersonalImages/" + personalId.personalImagePath;
        byte[] personalImage_b = Files.readAllBytes(Paths.get(personalImage));

        if (validateSignature(Utils.concat_bytes(personal_id_b, personalImage_b), personal_id_s[1], signature_b)) {
           System.out.println("Ausweis ist korrekt\n");
           System.out.println(personalId);
        } else {
            System.out.println("Ausweis ist nicht korrekt\n");
        }
    }

    private static void importPublicProfile(File publicProfile) throws IOException {
        String profileFileName = publicProfile.getName();
        String internalPath = "ImportedPublicProfiles/" + profileFileName;
        File distDir = new File("ImportedPublicProfiles/");
        distDir.mkdirs();
        Files.copy(Paths.get(publicProfile.toURI()), Paths.get(internalPath));
    }

    private static void importPersonalID(File source) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        FileInputStream fis = new FileInputStream(source);
        Utils.SliceReader sliceReader = new Utils.SliceReader((data, length) -> fis.read(data, 0, length));

        // read personal id
        byte[] personal_id_b = sliceReader.next();
        // read signature
        byte[] signature_b = sliceReader.next();
        // read personal image
        byte[] personal_image_b = sliceReader.next();

        // extract id number and image name
        String[] personal_id_s = new String(personal_id_b).split("\n");
        String id_number = personal_id_s[0];
        String publicProfile = personal_id_s[1];
        String imageName = personal_id_s[8];

        if(!validateSignature(Utils.concat_bytes(personal_id_b, personal_image_b), publicProfile, signature_b)) {
            System.err.println("Ausweis ungültig");
            return;
        }

        // save imported data
        String id_path = "ImportedPersonalIDs/" + id_number;

        File f_personal_id = Utils.createFileAndSubfolder(id_path + "/id");
        FileOutputStream fos = new FileOutputStream(f_personal_id);
        fos.write(personal_id_b);
        fos.close();

        File f_signature = Utils.createFileAndSubfolder(id_path + "/signature");
        fos = new FileOutputStream(f_signature);
        fos.write(signature_b);
        fos.close();

        File f_personal_image = Utils.createFileAndSubfolder("PersonalImages/" + imageName);
        fos = new FileOutputStream(f_personal_image);
        fos.write(personal_image_b);
        fos.close();

        System.out.println();
    }

    private static void exportPersonalID(String personal_id) throws IOException {
        String distPath = "CreatedPersonalIDs/" + personal_id;

        //load personal id
        File f = new File(distPath + "/id");
        byte[] personal_id_b = Files.readAllBytes(f.toPath());

        //load signature
        f = new File(distPath + "/signature");
        byte[] signature_b = Files.readAllBytes(f.toPath());

        // load personal image
        String[] personal_id_s = new String(personal_id_b).split("\n");
        f = new File("PersonalImages/" + personal_id_s[8]);
        byte[] personalImage_b = Files.readAllBytes(f.toPath());

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(personal_id));
        fileChooser.showSaveDialog(null);
        File destination = fileChooser.getSelectedFile();
        if(destination == null) {
            return;
        }

        FileOutputStream fos = new FileOutputStream(destination);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(data -> fos.write(data));
        sliceWriter.write(personal_id_b);
        sliceWriter.write(signature_b);
        sliceWriter.write(personalImage_b);
    }

    private static void exportPublicProfile(String profileName) throws IOException {
        File publicProfile = new File("MyPublicProfiles/" + profileName + "/public");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(profileName));
        fileChooser.showSaveDialog(null);
        File destination = fileChooser.getSelectedFile();
        if(destination == null) {
            return;
        }
        Files.copy(publicProfile.toPath(), destination.toPath());
    }
}
