package controller;

import model.Personal_ID;
import model.PrivateProfile;
import model.PublicProfile;
import utils.OutputEvent;
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
import java.util.Observable;

public class Controller extends Observable {
    public static final int LOAD_PROFILE_FROM_OWN = 1;
    public static final int LOAD_PROFILE_FROM_IMPORTED = 2;
    public static final String strCreatedProfiles = "CreatedProfiles/";
    public static final String strImportedPublicProfiles = "ImportedPublicProfiles/";
    public static final String strPersonalImages = "PersonalImages/";
    public static final String strHandSignatures = "HandSignatures/";
    public static final String strCreatedPersonalIDs = "CreatedPersonalIDs/";
    public static final String strImportedPersonalIDs = "ImportedPersonalIDs/";
    public static final String encryptionAlgorithm = "RSA";
    public static final String hashAllgorithm = "SHA256withRSA";
    public final String appDataLocation;
    public Controller(String appDataLocation) {
        this.appDataLocation = appDataLocation;
    }

    public void generateKeyPair(String profileName, int sequence_number, PublicProfile.ValidityPeriod validityPeriod, String[] dynamicAttributes) throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator gpk = KeyPairGenerator.getInstance(encryptionAlgorithm);
        gpk.initialize(2048);
        KeyPair keyPair = gpk.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        String created = Utils.today();

        PrivateProfile privateProfile = new PrivateProfile(
                profileName, sequence_number, created, validityPeriod, dynamicAttributes, publicKey.getEncoded(), privateKey.getEncoded());
        privateProfile.saveInternal(appDataLocation + strCreatedProfiles + profileName + "/" + sequence_number);
    }

    private byte[] sign_id(byte[] personalIdB, PrivateProfile privateProfile) throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateProfile.privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance(encryptionAlgorithm);
        //sign message
        Signature signature = Signature.getInstance(hashAllgorithm);
        signature.initSign(keyFactory.generatePrivate(spec));
        signature.update(personalIdB);
        return signature.sign();
    }

    public void generateID(Controller controller, String publicProfileName, int sequence_number, String validUntil, String name, String surname, String birthdate, String address, String[] dynamicAttributeValues, File personalPicture, File handSignature) throws Exception {
        //load public profile
        PrivateProfile privateProfile = PrivateProfile.fromInternalFile(
                this, appDataLocation + strCreatedProfiles, publicProfileName, sequence_number);
        if(privateProfile == null) {
            return;
        }
        int nDynamicAttributes = privateProfile.dynamicAttributes.length;
        if(nDynamicAttributes != dynamicAttributeValues.length) {
            controller.notifyObservers(new OutputEvent.DynamicAttributesDoesntFitEvent(nDynamicAttributes));
            return;
        }

        String ID_number = Utils.getAlphanumeric(8);
        // get personal image
        copy_attached_data(personalPicture, strPersonalImages);
        // get hand signature
        copy_attached_data(handSignature, strHandSignatures);

        String today = Utils.today();
        Personal_ID personalId = new Personal_ID(ID_number, privateProfile, today, validUntil, name, surname, birthdate,
                address, dynamicAttributeValues, personalPicture.getName(), handSignature.getName());
        byte[] personalId_b = personalId.toByte(true);

        //Create signature
        byte[] personalId_with_personal_image_b = Utils.concat_bytes(
                personalId_b, Files.readAllBytes(personalPicture.toPath()), Files.readAllBytes(handSignature.toPath()));
        byte[] signature_b = sign_id(personalId_with_personal_image_b, privateProfile);

        String distPath = appDataLocation + strCreatedPersonalIDs + ID_number;

        //Save ID
        File f = Utils.createFileAndSubfolder(distPath);
        FileOutputStream fos = new FileOutputStream(f);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(fos);
        sliceWriter.write(personalId_b);
        sliceWriter.write(signature_b);
        fos.close();
    }

    private boolean validateSignature(byte[] personal_id_b, byte[] publicKey, byte[] signature_b) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance(encryptionAlgorithm);

        Signature publicSignature = Signature.getInstance(hashAllgorithm);
        publicSignature.initVerify(keyFactory.generatePublic(spec));
        publicSignature.update(personal_id_b);
        return publicSignature.verify(signature_b);
    }

    public void checkPersonalID(String id_number) throws Exception {
        String distPath = appDataLocation + strImportedPersonalIDs + id_number.toUpperCase();

        //load personal id
        File f = new File(distPath);
        FileInputStream fis = new FileInputStream(f);
        Utils.SliceReader sliceReader = new Utils.SliceReader(fis);
        byte[] personal_id_b = sliceReader.next();
        byte[] signature_b = sliceReader.next();
        fis.close();
        String[] personal_id_s = Utils.bytesToStringArray(personal_id_b);

        Personal_ID personalId = Personal_ID.fromString(this, LOAD_PROFILE_FROM_IMPORTED, personal_id_s);
        if(personalId == null) {
            return;
        }
        String personalImage = appDataLocation + strPersonalImages + personalId.personalImagePath;
        byte[] personalImage_b = Files.readAllBytes(Paths.get(personalImage));
        String handSignature = appDataLocation + strHandSignatures + personalId.handSignaturePath;
        byte[] handSignature_b = Files.readAllBytes(Paths.get(handSignature));

        if (validateSignature(Utils.concat_bytes(personal_id_b, personalImage_b, handSignature_b), personalId.publicProfile.publicKey, signature_b)) {
            notifyObservers(new OutputEvent.PersonalIDValidEvent(personalId.toString()));
        } else {
            notifyObservers(new OutputEvent.PersonalIDInvalidEvent());
        }
    }

    public void exportPublicProfile(String profileName, int sequence_number, File destination) throws IOException {
        PrivateProfile privateProfile = PrivateProfile.fromInternalFile(
                this, appDataLocation + strCreatedProfiles, profileName, sequence_number);
        if(privateProfile == null)
            return;
        PublicProfile publicProfile = new PublicProfile(privateProfile.name, privateProfile.sequence_number,
                privateProfile.created, privateProfile.validityPeriod, privateProfile.dynamicAttributes, privateProfile.publicKey);
        publicProfile.saveExternal(destination);
    }

    public void importPublicProfile(InputStream inputStream) throws IOException {
        PublicProfile publicProfile = PublicProfile.fromExternal(inputStream);
        publicProfile.saveInternal(appDataLocation + strImportedPublicProfiles);
    }

    public void exportPersonalID(String personal_id, File destination) throws Exception {
        String distPath = appDataLocation + strCreatedPersonalIDs + personal_id.toUpperCase();

        //load personal id
        File f = new File(distPath);
        FileInputStream fis = new FileInputStream(f);
        Utils.SliceReader sliceReader = new Utils.SliceReader(fis);
        byte[] personal_id_b = sliceReader.next();
        byte[] signature_b = sliceReader.next();
        fis.close();

        String[] personal_id_s = Utils.bytesToStringArray(personal_id_b);

        Personal_ID personalId = Personal_ID.fromString(this, LOAD_PROFILE_FROM_OWN, personal_id_s);
        if (personalId == null) {
            return;
        }
        // load personal image
        byte[] personalImage_b = Files.readAllBytes(Paths.get(appDataLocation + strPersonalImages + personalId.personalImagePath));
        // load hand signature
        byte[] handSignature_b = Files.readAllBytes(Paths.get(appDataLocation + strHandSignatures + personalId.handSignaturePath));

        FileOutputStream fos = new FileOutputStream(destination);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(fos);
        sliceWriter.write(personal_id_b);
        sliceWriter.write(signature_b);
        sliceWriter.write(personalImage_b);
        sliceWriter.write(handSignature_b);
        fos.close();
    }

    public void importPersonalID(InputStream inputStream) throws Exception {
        Utils.SliceReader sliceReader = new Utils.SliceReader(inputStream);
        // read personal id
        byte[] personal_id_b = sliceReader.next();
        // read signature
        byte[] signature_b = sliceReader.next();
        // read personal image and hand signature
        byte[] personalImage_b = sliceReader.next();
        byte[] handSignature_b = sliceReader.next();
        inputStream.close();
        String[] personal_id_s = Utils.bytesToStringArray(personal_id_b);

        Personal_ID personalId = Personal_ID.fromString(this, LOAD_PROFILE_FROM_IMPORTED, personal_id_s);
        if (personalId == null) {
            return;
        }
        // extract id number and image name
        String id_number = personalId.ID_number;
        String imageName = personalId.personalImagePath;
        String handSignatureName = personalId.handSignaturePath;

        if(!validateSignature(
                Utils.concat_bytes(personal_id_b, personalImage_b, handSignature_b),
                personalId.publicProfile.publicKey, signature_b)) {
            notifyObservers(new OutputEvent.PersonalIDInvalidEvent());
            return;
        }

        // save imported data
        String id_path = appDataLocation + strImportedPersonalIDs + id_number;
        File f_personal_id = Utils.createFileAndSubfolder(id_path);
        FileOutputStream fos1 = new FileOutputStream(f_personal_id);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(fos1);
        sliceWriter.write(personal_id_b);
        sliceWriter.write(signature_b);
        fos1.close();


        saveAttachedData(appDataLocation + strPersonalImages + imageName, personalImage_b);
        saveAttachedData(appDataLocation + strHandSignatures + handSignatureName, handSignature_b);
    }

    public void checkPersonalIDFromRemote() throws Exception {
        String ip = InetAddress.getLocalHost().getHostAddress();
        ServerSocket serverSocket = new ServerSocket(0);
        notifyObservers(new OutputEvent.ServerStartedEvent(ip, serverSocket.getLocalPort()));
        Socket s = serverSocket.accept();
        InputStream inputStream = new BufferedInputStream(s.getInputStream());

        Utils.SliceReader sliceReader = new Utils.SliceReader(inputStream);
        byte[] personal_id_b = sliceReader.next();
        byte[] personal_image_b = sliceReader.next();
        byte[] handSignature_b = sliceReader.next();
        byte[] signature_b = sliceReader.next();
        inputStream.close();
        serverSocket.close();
        String[] personal_id_s = Utils.bytesToStringArray(personal_id_b);

        Personal_ID personalId = Personal_ID.fromString(this, LOAD_PROFILE_FROM_IMPORTED, personal_id_s);
        if (personalId == null) {
            return;
        }
        if (validateSignature(Utils.concat_bytes(personal_id_b, personal_image_b, handSignature_b), personalId.publicProfile.publicKey, signature_b)) {
            notifyObservers(new OutputEvent.PersonalIDValidEvent(personalId.toString()));
        } else {
            notifyObservers(new OutputEvent.PersonalIDInvalidEvent());
        }
    }

    public void handInPersonalIDtoRemote(String id_number, String ip, int port) throws Exception {
        Socket s = new Socket(ip, port);
        //load personal id
        String distPath = appDataLocation + strImportedPersonalIDs + id_number.toUpperCase();
        FileInputStream fis = new FileInputStream(distPath);
        Utils.SliceReader sliceReader = new Utils.SliceReader(fis);
        byte[] personal_id_b = sliceReader.next();
        byte[] signature_b = sliceReader.next();
        // load personal image
        String[] personal_id_s = Utils.bytesToStringArray(personal_id_b);

        Personal_ID personalId = Personal_ID.fromString(this, LOAD_PROFILE_FROM_IMPORTED, personal_id_s);
        if (personalId == null) {
            return;
        }
        byte[] personalImage_b = Files.readAllBytes(Paths.get(appDataLocation + strPersonalImages + personalId.personalImagePath));
        byte[] handSignature_b = Files.readAllBytes(Paths.get(appDataLocation + strHandSignatures + personalId.handSignaturePath));
        fis.close();
        OutputStream outputStream = new BufferedOutputStream(s.getOutputStream());

        //hand in
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(outputStream);
        sliceWriter.write(personal_id_b);
        sliceWriter.write(personalImage_b);
        sliceWriter.write(handSignature_b);
        sliceWriter.write(signature_b);
        outputStream.close();
    }

    public void showPublicProfile(String profileName, int sequence) throws IOException {
        PublicProfile profile = PublicProfile.loadInternal(this, appDataLocation + Controller.strImportedPublicProfiles, profileName, sequence);
        if(profile == null) {
            return;
        }
        notifyObservers(new OutputEvent.ShowProfileEvent(profile.toString()));
    }

    public void saveAttachedData(String url, byte[] data) throws IOException {
        File f = Utils.createFileAndSubfolder(url);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(data);
        fos.close();
    }

    private void copy_attached_data(File from, String to) throws IOException {
        String fileName = from.getName();
        String internalPath = appDataLocation + to + fileName;
        File imageDir2 = new File(appDataLocation + to);
        imageDir2.mkdirs();
        Files.copy(Paths.get(from.toURI()), Paths.get(internalPath));
    }

    @Override
    public void notifyObservers(Object o) {
        setChanged();
        super.notifyObservers(o);
    }
}
