import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Date;

public class Controller {
    public static String appDataLocation = "data/";

    public static void generateKeyPair(String profileName) throws NoSuchAlgorithmException, IOException {
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

    private static byte[] sign_id(byte[] personalIdB, File publicProfileFile) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        FileInputStream fis = new FileInputStream(publicProfileFile);
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

    public static void generateID(String publicProfile, String name, String surname, Date date, String address, File personalPicture) throws ParseException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        String ID_number = Utils.getAlphanumeric(8);
        System.out.println(ID_number);
        File publicProfileFile = new File(appDataLocation + "MyPublicProfiles/" + publicProfile);

        String personalPictureFileName = personalPicture.getName();
        String internalPath = appDataLocation + "PersonalImages/" + personalPictureFileName;
        File imageDir = new File(appDataLocation + "PersonalImages/");
        imageDir.mkdirs();
        Files.copy(Paths.get(personalPicture.toURI()), Paths.get(internalPath));

        Personal_ID personalId = new Personal_ID(ID_number, publicProfile,  name, surname, date, address, personalPictureFileName);
        byte[] personalId_b = personalId.toByte(true);

        //Create signature
        byte[] personalId_with_personal_image_b = Utils.concat_bytes(personalId_b, Files.readAllBytes(personalPicture.toPath()));
        byte[] signature_b = sign_id(personalId_with_personal_image_b, publicProfileFile);

        String distPath = appDataLocation + "CreatedPersonalIDs/" + ID_number;

        //Save ID
        File f = Utils.createFileAndSubfolder( distPath);
        FileOutputStream fos = new FileOutputStream(f);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(data -> fos.write(data));
        sliceWriter.write(personalId_b);
        sliceWriter.write(signature_b);
        fos.close();
    }

    private static boolean validateSignature(byte[] personal_id_b, String publicProfile, byte[] signature_b) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        File publicKeyFile = new File(appDataLocation + "ImportedPublicProfiles/" + publicProfile);
        byte[] publicKey = Files.readAllBytes(publicKeyFile.toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(keyFactory.generatePublic(spec));
        publicSignature.update(personal_id_b);
        return publicSignature.verify(signature_b);
    }

    public static void checkPersonalID(String id_number) throws Exception {
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

        if (validateSignature(Utils.concat_bytes(personal_id_b, personalImage_b), personal_id_s[1], signature_b)) {
            System.out.println("Ausweis ist korrekt\n");
            System.out.println(personalId);
        } else {
            System.out.println("Ausweis ist nicht korrekt\n");
        }
    }

    public static void exportPublicProfile(String profileName, File destination) throws IOException {
        FileInputStream fis = new FileInputStream(appDataLocation + "MyPublicProfiles/" + profileName);
        Utils.SliceReader sliceReader = new Utils.SliceReader((data, length) -> fis.read(data, 0, length));
        byte[] privateKey_b = sliceReader.next();
        byte[] publicKey_b = sliceReader.next();

        FileOutputStream fos = new FileOutputStream(destination);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(data -> fos.write(data));
        sliceWriter.write(profileName.getBytes());
        sliceWriter.write(publicKey_b);
    }

    public static void importPublicProfile(File publicProfile) throws IOException {
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

    public static void exportPersonalID(String personal_id, File destination) throws IOException {
        String distPath = appDataLocation + "CreatedPersonalIDs/" + personal_id.toUpperCase();

        //load personal id
        File f = new File(distPath);
        FileInputStream fis = new FileInputStream(f);
        Utils.SliceReader sliceReader = new Utils.SliceReader((data, length) -> fis.read(data, 0, length));
        byte[] personal_id_b = sliceReader.next();
        byte[] signature_b = sliceReader.next();
        fis.close();

        // load personal image
        String[] personal_id_s = new String(personal_id_b).split("\n");
        f = new File(appDataLocation + "PersonalImages/" + personal_id_s[8]);
        byte[] personalImage_b = Files.readAllBytes(f.toPath());

        FileOutputStream fos = new FileOutputStream(destination);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(data -> fos.write(data));
        sliceWriter.write(personal_id_b);
        sliceWriter.write(signature_b);
        sliceWriter.write(personalImage_b);
        fos.close();
    }

    public static void importPersonalID(File source) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        FileInputStream fis = new FileInputStream(source);
        Utils.SliceReader sliceReader = new Utils.SliceReader((data, length) -> fis.read(data, 0, length));
        // read personal id
        byte[] personal_id_b = sliceReader.next();
        // read signature
        byte[] signature_b = sliceReader.next();
        // read personal image
        byte[] personal_image_b = sliceReader.next();
        fis.close();

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
        String id_path = appDataLocation +  "ImportedPersonalIDs/" + id_number;
        File f_personal_id = Utils.createFileAndSubfolder(id_path);
        FileOutputStream fos1 = new FileOutputStream(f_personal_id);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(data -> fos1.write(data));
        sliceWriter.write(personal_id_b);
        sliceWriter.write(signature_b);
        fos1.close();


        File f_personal_image = Utils.createFileAndSubfolder(appDataLocation + "PersonalImages/" + imageName);
        FileOutputStream fos2 = new FileOutputStream(f_personal_image);
        fos2.write(personal_image_b);
        fos2.close();

        System.out.println();
    }
}
