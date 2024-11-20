package model;

import controller.Controller;
import utils.OutputEvent;
import utils.Utils;

import java.io.*;

public class PublicProfile {
    public final String name;
    public final int sequence_number;
    public final String[] dynamicAttributes;
    public final byte[] publicKey;

    public PublicProfile(String name, int sequence_number, String[] dynamicAttributes, byte[] publicKey) {
        this.name = name;
        this.sequence_number = sequence_number;
        this.dynamicAttributes = dynamicAttributes;
        this.publicKey = publicKey;
    }

    public static PublicProfile loadInternal(Controller controller, String path, String profileName, int sequence_number) throws IOException {
        File f = new File(path + profileName + "/" + sequence_number);
        if(!f.exists()) {
            controller.notifyObservers(new OutputEvent.NoSuchPublicProfileEvent(profileName, sequence_number));
            return null;
        }
        FileInputStream fis = new FileInputStream(f);
        Utils.SliceReader sliceReader = new Utils.SliceReader((data, length) -> fis.read(data, 0, length));
        byte[] dynamicAttributes_b = sliceReader.next();
        byte[] publicKey = sliceReader.next();
        String[] dynamicAttributes = Utils.bytesToStringArray(dynamicAttributes_b);
        fis.close();
        return new PublicProfile(profileName, sequence_number, dynamicAttributes, publicKey);
    }

    public static PublicProfile fromExternal(File publicProfileFile) throws IOException {
        FileInputStream fis = new FileInputStream(publicProfileFile);
        Utils.SliceReader sliceReader = new Utils.SliceReader((data, length) -> fis.read(data, 0, length));
        String public_profile_name = new String(sliceReader.next());
        int sequence_number = Integer.parseInt(new String(sliceReader.next()));
        byte[] dynamic_attributes_b = sliceReader.next();
        byte[] public_profile_b = sliceReader.next();
        fis.close();
        return new PublicProfile(public_profile_name, sequence_number, Utils.bytesToStringArray(dynamic_attributes_b), public_profile_b);
    }

    public void saveExternal(File destination) throws IOException {
        FileOutputStream fos = new FileOutputStream(destination);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(data -> fos.write(data));
        sliceWriter.write(name.getBytes());
        sliceWriter.write(String.valueOf(sequence_number).getBytes());
        sliceWriter.write(Utils.stringArrayToLines(dynamicAttributes).getBytes());
        sliceWriter.write(publicKey);
    }

    public void saveInternal(String path) throws IOException {
        File destination = Utils.createFileAndSubfolder(path + name + "/" + sequence_number);
        FileOutputStream fos = new FileOutputStream(destination);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(data -> fos.write(data));
        sliceWriter.write(Utils.stringArrayToLines(dynamicAttributes).getBytes());
        sliceWriter.write(publicKey);
        fos.close();
    }
}
