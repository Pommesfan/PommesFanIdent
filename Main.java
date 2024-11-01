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
        System.out.println("Aktion auswählen:\n1: Öffentliches Profil erstellen\n2: Ausweis erstellen\n3: Ausweis prüfen\n4: Öffentliches Profil importieren\n5: Ausweis importieren\n6: Öffentliches Profil exportieren\n7: Ausweis exportieren");
        int mode = sc.nextInt();
        switch (mode) {
            case 1: generateKeyPair(); break;
            case 2: generateID(); break;
            case 3: checkPersonalID(); break;
            case 4: importPublicProfile(); break;
            case 5: importPersonalID(); break;
            case 6: exportPublicProfile(); break;
            case 7: exportPersonalID(); break;
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

        File f = Utils.createFileAndSubfolder("MyPublicProfiles/" + profileName + "/private");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(privateKey.getEncoded());
        fos.close();

        f = Utils.createFileAndSubfolder("MyPublicProfiles/" + profileName + "/Hallo.jpg");
        fos = new FileOutputStream(f);
        fos.write(publicKey.getEncoded());
        fos.close();
    }

    private static void generateID() throws ParseException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        String ID_number = Utils.getAlphanumeric(8);

        System.out.println("Öffentliches Profil auswählen");
        String publicProfile = sc.next();
        File privateKeyFile = new File("MyPublicProfiles/" + publicProfile + "/private");

        System.out.println(ID_number);
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

    private static void checkPersonalID() throws Exception {
        System.out.println("Ausweis auswählen");
        String id_number = sc.next().toUpperCase();

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

    private static void importPublicProfile() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
        File publicProfile = fileChooser.getSelectedFile();
        if(publicProfile == null) {
            return;
        }

        String profileFileName = publicProfile.getName();
        String internalPath = "ImportedPublicProfiles/" + profileFileName;
        File distDir = new File("ImportedPublicProfiles/");
        distDir.mkdirs();
        Files.copy(Paths.get(publicProfile.toURI()), Paths.get(internalPath));
    }

    private static void importPersonalID() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
        File source = fileChooser.getSelectedFile();
        if(source == null) {
            return;
        }
        FileInputStream fis = new FileInputStream(source);

        // read len for personal id
        byte[] len_personal_id_b = new byte[4];
        fis.read(len_personal_id_b, 0, 4);
        int len_personal_id = Utils.bytes_to_int(len_personal_id_b);
        // read personal id
        byte[] personal_id_b = new byte[len_personal_id];
        fis.read(personal_id_b, 0, len_personal_id);

        // read len for signature
        byte[] len_signature_b = new byte[4];
        fis.read(len_signature_b, 0, 4);
        int len_signature = Utils.bytes_to_int(len_signature_b);
        // read signature
        byte[] signature_b = new byte[len_signature];
        fis.read(signature_b, 0, len_signature);

        // read len for personal image
        byte[] len_personal_image_b = new byte[4];
        fis.read(len_personal_image_b, 0, 4);
        int len_personal_image = Utils.bytes_to_int(len_personal_image_b);
        // read personal image
        byte[] personal_image_b = new byte[len_personal_image];
        fis.read(personal_image_b, 0, len_personal_image);
        fis.close();

        // extract id number and image name
        String[] personal_id_s = new String(personal_id_b).split("\n");
        String id_number = personal_id_s[0];
        String imageName = personal_id_s[8];

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

    private static void exportPersonalID() throws IOException {
        System.out.println("Ausweisnummer angeben");
        String personal_id = sc.next().toUpperCase();

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
        fos.write(Utils.int_to_bytes(personal_id_b.length));
        fos.write(personal_id_b);
        fos.write(Utils.int_to_bytes(signature_b.length));
        fos.write(signature_b);
        fos.write(Utils.int_to_bytes(personalImage_b.length));
        fos.write(personalImage_b);
        fos.close();
    }

    private static void exportPublicProfile() throws IOException {
        System.out.println("Profilname angeben");
        String profileName = sc.next();

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
