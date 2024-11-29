package model;

import controller.Controller;
import utils.OutputEvent;
import utils.Utils;

import java.io.*;

public class PublicProfile {
    public final String name;
    public final int sequence_number;
    public final String created;
    public final ValidityPeriod validityPeriod;
    public final String[] dynamicAttributes;
    public final byte[] publicKey;

    public PublicProfile(String name, int sequence_number, String created, ValidityPeriod validityPeriod, String[] dynamicAttributes, byte[] publicKey) {
        this.name = name;
        this.sequence_number = sequence_number;
        this.created = created;
        this.validityPeriod = validityPeriod;
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
        String[] profileParams = Utils.bytesToStringArray(sliceReader.next());

        String creationDate = profileParams[0];
        ValidityPeriod validityPeriod = ValidityPeriod.fromStringArray(profileParams, 1);
        String[] dynamicAttributes = Utils.sliceStringArray(profileParams, 5, profileParams.length);

        byte[] publicKey = sliceReader.next();
        fis.close();
        return new PublicProfile(profileName, sequence_number, creationDate, validityPeriod, dynamicAttributes, publicKey);
    }

    public static PublicProfile fromExternal(File publicProfileFile) throws IOException {
        FileInputStream fis = new FileInputStream(publicProfileFile);
        Utils.SliceReader sliceReader = new Utils.SliceReader((data, length) -> fis.read(data, 0, length));
        String[] profileParams = Utils.bytesToStringArray(sliceReader.next());
        String public_profile_name = profileParams[0];
        int sequence_number = Integer.parseInt(profileParams[1]);
        String creationDate = profileParams[2];
        ValidityPeriod validityPeriod = ValidityPeriod.fromStringArray(profileParams, 3);
        String[] dynamic_attributes = Utils.sliceStringArray(profileParams, 7, profileParams.length);
        byte[] public_profile_b = sliceReader.next();
        fis.close();
        return new PublicProfile(public_profile_name, sequence_number, creationDate, validityPeriod, dynamic_attributes, public_profile_b);
    }

    public void saveExternal(File destination) throws IOException {
        FileOutputStream fos = new FileOutputStream(destination);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(data -> fos.write(data));
        sliceWriter.write(profileToString(true).getBytes());
        sliceWriter.write(publicKey);
    }

    public void saveInternal(String path) throws IOException {
        File destination = Utils.createFileAndSubfolder(path + name + "/" + sequence_number);
        FileOutputStream fos = new FileOutputStream(destination);
        Utils.SliceWriter sliceWriter = new Utils.SliceWriter(data -> fos.write(data));
        sliceWriter.write(profileToString(false).getBytes());
        sliceWriter.write(publicKey);
        fos.close();
    }

    public String profileToString(boolean addNameAndSequence) {
        StringBuilder sb = new StringBuilder();
        if(addNameAndSequence) {
            sb.append(name);
            sb.append('\n');
            sb.append(sequence_number);
            sb.append('\n');
        }
        sb.append(created);
        sb.append('\n');
        sb.append(validityPeriod);
        sb.append(Utils.stringArrayToLines(dynamicAttributes));
        return sb.toString();
    }

    public static class ValidityPeriod {
        public final String validFrom;
        public final String validUntilForCreation;
        public final String validUntilForCreated;
        public final int maxValidDays;
        public ValidityPeriod(String validFrom, String validUntilForCreation, String validUntilForCreated, int maxValidDays) {
            this.validFrom = validFrom;
            this.validUntilForCreation = validUntilForCreation;
            this.validUntilForCreated =validUntilForCreated;
            this.maxValidDays = maxValidDays;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(validFrom);
            sb.append('\n');
            sb.append(validUntilForCreation);
            sb.append('\n');
            sb.append(validUntilForCreated);
            sb.append('\n');
            sb.append(maxValidDays);
            sb.append('\n');
            return sb.toString();
        }

        public static ValidityPeriod fromStringArray(String[]s, int start) {
            return new ValidityPeriod(s[start], s[1 + start], s[2 + start], Integer.parseInt(s[3 + start]));
        }
    }
}
