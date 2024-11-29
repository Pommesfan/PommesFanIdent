package model;

import controller.Controller;
import utils.OutputEvent;
import utils.Utils;

import java.io.*;

public class PrivateProfile extends PublicProfile{
    public final byte[] privateKey;
    public PrivateProfile(String name, int sequence_number, String created, ValidityPeriod validityPeriod, String[] dynamicAttributes, byte[] publicKey, byte[] privateKey) {
        super(name, sequence_number, created, validityPeriod, dynamicAttributes, publicKey);
        this.privateKey = privateKey;
    }

    public void saveInternal(String url) throws IOException {
        File f = Utils.createFileAndSubfolder(url);
        FileOutputStream fos = new FileOutputStream(f);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(data -> fos.write(data));
        sliceWriter.write(profileToString(false).getBytes());
        sliceWriter.write(privateKey);
        sliceWriter.write(publicKey);
        fos.close();
    }

    public static PrivateProfile fromInternalFile(Controller controller, String path, String profileName, int sequence_number) throws IOException {
        File f = new File(path + profileName + "/" + sequence_number);
        if(!f.exists()) {
            controller.notifyObservers(new OutputEvent.NoSuchPublicProfileEvent(profileName, sequence_number));
            return null;
        }
        FileInputStream fis = new FileInputStream(f);
        Utils.SliceReader sliceReader = new Utils.SliceReader((data, length) -> fis.read(data, 0, length));
        String[] profileParams = Utils.bytesToStringArray(sliceReader.next());
        String created = profileParams[0];
        ValidityPeriod validityPeriod = ValidityPeriod.fromStringArray(profileParams, 1);
        String[] dynamicAttributes = Utils.sliceStringArray(profileParams, 5, profileParams.length);
        byte[] privateKey = sliceReader.next();
        byte[] publicKey = sliceReader.next();
        fis.close();
        return new PrivateProfile(profileName, sequence_number, created, validityPeriod, dynamicAttributes, publicKey, privateKey);
    }
}
