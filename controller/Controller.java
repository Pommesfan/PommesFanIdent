package controller;

import model.Personal_ID;
import utils.Utils;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Date;
import java.util.Observable;

public class Controller extends Observable {
    public final String appDataLocation;
    public Controller(String appDataLocation) {
        this.appDataLocation = appDataLocation;
    }

    public void generateKeyPair(String profileName) throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator gpk = KeyPairGenerator.getInstance("RSA");
        gpk.initialize(2048);
        KeyPair keyPair = gpk.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        File f = Utils.createFileAndSubfolder(appDataLocation + "MyPublicProfiles/" + profileName);
        FileOutputStream fos = new FileOutputStream(f);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(data -> fos.write(data));
        sliceWriter.write(privateKey.getEncoded());
        sliceWriter.write(publicKey.getEncoded());
        fos.close();
    }

    private byte[] sign_id(byte[] personalIdB, String publicProfile) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        FileInputStream fis = new FileInputStream(appDataLocation + "MyPublicProfiles/" + publicProfile);
        Utils.SliceReader sliceReader = new Utils.SliceReader((data, length) -> fis.read(data, 0, length));
        byte[] privateKeyBytes = sliceReader.next();
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //sign message
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyFactory.generatePrivate(spec));
        signature.update(personalIdB);
        return signature.sign();
    }

    public void generateID(String publicProfile, String name, String surname, Date date, String address, File personalPicture, File handSignature) throws ParseException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        String ID_number = Utils.getAlphanumeric(8);
        System.out.println(ID_number);

        // get personal image
        String personalPictureFileName = personalPicture.getName();
        String internalPath1 = appDataLocation + "PersonalImages/" + personalPictureFileName;
        File imageDir1 = new File(appDataLocation + "PersonalImages/");
        imageDir1.mkdirs();
        Files.copy(Paths.get(personalPicture.toURI()), Paths.get(internalPath1));
        // get hand signature
        String handSignatureFileName = personalPicture.getName();
        String internalPath2 = appDataLocation + "HandSignatures/" + personalPictureFileName;
        File imageDir2 = new File(appDataLocation + "HandSignatures/");
        imageDir2.mkdirs();
        Files.copy(Paths.get(handSignature.toURI()), Paths.get(internalPath2));

        Personal_ID personalId = new Personal_ID(ID_number, publicProfile,  name, surname, date, address, personalPictureFileName, handSignatureFileName);
        byte[] personalId_b = personalId.toByte(true);

        //Create signature
        byte[] personalId_with_personal_image_b = Utils.concat_bytes(
                personalId_b, Files.readAllBytes(personalPicture.toPath()), Files.readAllBytes(handSignature.toPath()));
        byte[] signature_b = sign_id(personalId_with_personal_image_b, publicProfile);

        String distPath = appDataLocation + "CreatedPersonalIDs/" + ID_number;

        //Save ID
        File f = Utils.createFileAndSubfolder( distPath);
        FileOutputStream fos = new FileOutputStream(f);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(data -> fos.write(data));
        sliceWriter.write(personalId_b);
        sliceWriter.write(signature_b);
        fos.close();
    }

    private boolean validateSignature(byte[] personal_id_b, String publicProfile, byte[] signature_b) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        File publicKeyFile = new File(appDataLocation + "ImportedPublicProfiles/" + publicProfile);
        byte[] publicKey = Files.readAllBytes(publicKeyFile.toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(keyFactory.generatePublic(spec));
        publicSignature.update(personal_id_b);
        return publicSignature.verify(signature_b);
    }

    public void checkPersonalID(String id_number) throws Exception {
        String distPath = appDataLocation + "ImportedPersonalIDs/" + id_number.toUpperCase();

        //load personal id
        File f = new File(distPath);
        FileInputStream fis = new FileInputStream(f);
        Utils.SliceReader sliceReader = new Utils.SliceReader((data, length) -> fis.read(data, 0, length));
        byte[] personal_id_b = sliceReader.next();
        byte[] signature_b = sliceReader.next();
        fis.close();

        String[] personal_id_s = new String(personal_id_b).split("\n");

        Personal_ID personalId = new Personal_ID(personal_id_s);
        String personalImage = appDataLocation + "PersonalImages/" + personalId.personalImagePath;
        byte[] personalImage_b = Files.readAllBytes(Paths.get(personalImage));
        String handSignature = appDataLocation + "HandSignatures/" + personalId.personalImagePath;
        byte[] handSignature_b = Files.readAllBytes(Paths.get(handSignature));

        if (validateSignature(Utils.concat_bytes(personal_id_b, personalImage_b, handSignature_b), personal_id_s[1], signature_b)) {
            System.out.println("Ausweis ist korrekt\n");
            System.out.println(personalId);
        } else {
            System.out.println("Ausweis ist nicht korrekt\n");
        }
    }

    public void exportPublicProfile(String profileName, File destination) throws IOException {
        FileInputStream fis = new FileInputStream(appDataLocation + "MyPublicProfiles/" + profileName);
        Utils.SliceReader sliceReader = new Utils.SliceReader((data, length) -> fis.read(data, 0, length));
        byte[] privateKey_b = sliceReader.next();
        byte[] publicKey_b = sliceReader.next();

        FileOutputStream fos = new FileOutputStream(destination);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(data -> fos.write(data));
        sliceWriter.write(profileName.getBytes());
        sliceWriter.write(publicKey_b);
    }

    public void importPublicProfile(File publicProfile) throws IOException {
        FileInputStream fis = new FileInputStream(publicProfile);
        Utils.SliceReader sliceReader = new Utils.SliceReader((data, length) -> fis.read(data, 0, length));
        String public_profile_name = new String(sliceReader.next());
        byte public_profile_b[] = sliceReader.next();
        fis.close();

        String internalPath = appDataLocation + "ImportedPublicProfiles/" + public_profile_name;
        File destination = Utils.createFileAndSubfolder(internalPath);

        FileOutputStream fos = new FileOutputStream(destination);
        fos.write(public_profile_b);
        fos.close();
    }

    public void exportPersonalID(String personal_id, File destination) throws IOException {
        String distPath = appDataLocation + "CreatedPersonalIDs/" + personal_id.toUpperCase();

        //load personal id
        File f = new File(distPath);
        FileInputStream fis = new FileInputStream(f);
        Utils.SliceReader sliceReader = new Utils.SliceReader((data, length) -> fis.read(data, 0, length));
        byte[] personal_id_b = sliceReader.next();
        byte[] signature_b = sliceReader.next();
        fis.close();

        String[] personal_id_s = new String(personal_id_b).split("\n");
        // load personal image
        byte[] personalImage_b = Files.readAllBytes(Paths.get(appDataLocation + "PersonalImages/" + personal_id_s[8]));
        // load hand signature
        byte[] handSignature_b = Files.readAllBytes(Paths.get(appDataLocation + "HandSignatures/" + personal_id_s[8]));

        FileOutputStream fos = new FileOutputStream(destination);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(data -> fos.write(data));
        sliceWriter.write(personal_id_b);
        sliceWriter.write(signature_b);
        sliceWriter.write(personalImage_b);
        sliceWriter.write(handSignature_b);
        fos.close();
    }

    public void importPersonalID(File source) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        FileInputStream fis = new FileInputStream(source);
        Utils.SliceReader sliceReader = new Utils.SliceReader((data, length) -> fis.read(data, 0, length));
        // read personal id
        byte[] personal_id_b = sliceReader.next();
        // read signature
        byte[] signature_b = sliceReader.next();
        // read personal image and hand signature
        byte[] personalImage_b = sliceReader.next();
        byte[] handSignature_b = sliceReader.next();
        fis.close();

        // extract id number and image name
        String[] personal_id_s = new String(personal_id_b).split("\n");
        String id_number = personal_id_s[0];
        String publicProfile = personal_id_s[1];
        String imageName = personal_id_s[8];
        String handSignatureName = personal_id_s[9];

        if(!validateSignature(
                Utils.concat_bytes(personal_id_b, personalImage_b, handSignature_b),
                publicProfile, signature_b)) {
            System.err.println("Ausweis ungültig");
            return;
        }

        // save imported data
        String id_path = appDataLocation +  "ImportedPersonalIDs/" + id_number;
        File f_personal_id = Utils.createFileAndSubfolder(id_path);
        FileOutputStream fos1 = new FileOutputStream(f_personal_id);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(data -> fos1.write(data));
        sliceWriter.write(personal_id_b);
        sliceWriter.write(signature_b);
        fos1.close();


        File f_personal_image = Utils.createFileAndSubfolder(appDataLocation + "PersonalImages/" + imageName);
        FileOutputStream fos2 = new FileOutputStream(f_personal_image);
        fos2.write(personalImage_b);
        fos2.close();

        File f_hand_signature = Utils.createFileAndSubfolder(appDataLocation + "HandSignatures/" + handSignatureName);
        FileOutputStream fos3 = new FileOutputStream(f_hand_signature);
        fos3.write(handSignature_b);
        fos3.close();
    }

    public void checkPersonalIDFromRemote() throws Exception {
        System.out.println("Ip-Adresse:");
        System.out.println(InetAddress.getLocalHost().getHostAddress());
        ServerSocket serverSocket = new ServerSocket(0);
        System.out.println("Portnummer:");
        System.out.println(serverSocket.getLocalPort());
        Socket s = serverSocket.accept();
        InputStream inputStream = new BufferedInputStream(s.getInputStream());

        Utils.SliceReader sliceReader = new Utils.SliceReader((data, length) -> inputStream.read(data, 0, length));
        byte[] personal_id_b = sliceReader.next();
        byte[] personal_image_b = sliceReader.next();
        byte[] handSignature_b = sliceReader.next();
        byte[] signature_b = sliceReader.next();
        inputStream.close();
        serverSocket.close();
        String[] personal_id_s = new String(personal_id_b).split("\n");
        if (validateSignature(Utils.concat_bytes(personal_id_b, personal_image_b, handSignature_b), personal_id_s[1], signature_b)) {
            System.out.println(new Personal_ID(personal_id_s));
        } else {
            System.out.println("Ausweis nicht gültig");
        }
    }

    public void handInPersonalIDtoRemote(String id_number, String ip, int port) throws IOException {
        Socket s = new Socket(ip, port);
        //load personal id
        String distPath = appDataLocation + "ImportedPersonalIDs/" + id_number.toUpperCase();
        FileInputStream fis = new FileInputStream(distPath);
        Utils.SliceReader sliceReader = new Utils.SliceReader((data, length) -> fis.read(data, 0, length));
        byte[] personal_id_b = sliceReader.next();
        byte[] signature_b = sliceReader.next();
        // load personal image
        String[] personal_id_s = new String(personal_id_b).split("\n");
        byte[] personalImage_b = Files.readAllBytes(Paths.get(appDataLocation + "PersonalImages/" + personal_id_s[8]));
        byte[] handSignature_b = Files.readAllBytes(Paths.get(appDataLocation + "HandSignatures/" + personal_id_s[9]));
        fis.close();
        OutputStream outputStream = new BufferedOutputStream(s.getOutputStream());

        //hand in
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(data -> outputStream.write(data));
        sliceWriter.write(personal_id_b);
        sliceWriter.write(personalImage_b);
        sliceWriter.write(handSignature_b);
        sliceWriter.write(signature_b);
        outputStream.close();
    }
}
