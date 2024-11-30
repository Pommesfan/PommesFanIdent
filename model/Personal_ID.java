package model;

import controller.Controller;
import utils.OutputEvent;
import utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Personal_ID {
    public final String ID_number;
    public final PublicProfile publicProfile;
    public final String created;
    public final String validUntil;
    public final String name;
    public final String surname;
    public final String birthdate;
    public final String address;
    public final String[] dynamicAttributesValues;
    public final String personalImagePath;
    public final String handSignaturePath;

    public Personal_ID(String pIDnumber, PublicProfile pPublicProfile, String pCreated, String pValidUntil, String pName, String pSurname, String pBirthDate,
                       String pAddress, String[] pDynamicAttributesValues, String pPersonalImagePath, String pHandSignaturePath) {
        ID_number = pIDnumber;
        publicProfile = pPublicProfile;
        created = pCreated;
        validUntil = pValidUntil;
        name = pName;
        surname = pSurname;
        birthdate = pBirthDate;
        address = pAddress;
        dynamicAttributesValues = pDynamicAttributesValues;
        personalImagePath = pPersonalImagePath;
        handSignaturePath = pHandSignaturePath;
    }

    public static Personal_ID fromString(Controller controller, int own_or_imported_profile, String[] attributes) throws Exception {
        String ID_number = attributes[0];
        PublicProfile publicProfile = null;
        final String profileName = attributes[1];
        final int sequence_number = Integer.parseInt(attributes[2]);
        if (own_or_imported_profile == Controller.LOAD_PROFILE_FROM_OWN) {
            publicProfile = PrivateProfile.loadInternal(controller, controller.appDataLocation + Controller.strMyPublicProfiles, profileName, sequence_number);
        } else if(own_or_imported_profile == Controller.LOAD_PROFILE_FROM_IMPORTED) {
            publicProfile = PublicProfile.loadInternal(controller, controller.appDataLocation + Controller.strImportedPublicProfiles, profileName, sequence_number);
        }
        if(publicProfile == null) {
            return null;
        }
        String created = attributes[3];
        String validUntil = attributes[4];
        String name = attributes[5];
        String surname = attributes[6];
        String birthdate = attributes[7];
        String address = attributes[8];

        int nDynamicAttributes = publicProfile.dynamicAttributes.length;
        if(attributes.length != 11 + nDynamicAttributes) {
            controller.notifyObservers(new OutputEvent.DynamicAttributesDoesntFitEvent(nDynamicAttributes));
            return null;
        }

        String[] dynamicAttributesValues = Utils.sliceStringArray(attributes, 9, 9 + nDynamicAttributes);

        String personalImagePath = attributes[9 + nDynamicAttributes];
        String handSignaturePath = attributes[10 + nDynamicAttributes];
        return new Personal_ID(ID_number, publicProfile, created, validUntil, name, surname, birthdate, address, dynamicAttributesValues, personalImagePath, handSignaturePath);
    }

    public byte[] toByte(boolean withPaths) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ID_number.getBytes());
        baos.write('\n');
        baos.write(publicProfile.name.getBytes());
        baos.write('\n');
        baos.write(Integer.toString(publicProfile.sequence_number).getBytes());
        baos.write('\n');
        baos.write(created.getBytes());
        baos.write('\n');
        baos.write(validUntil.getBytes());
        baos.write('\n');
        baos.write(name.getBytes());
        baos.write('\n');
        baos.write(surname.getBytes());
        baos.write('\n');
        baos.write(birthdate.getBytes());
        baos.write('\n');
        baos.write(address.getBytes());
        baos.write('\n');

        for (String attribute : dynamicAttributesValues) {
            baos.write(attribute.getBytes());
            baos.write('\n');
        }

        if(withPaths) {
            baos.write(personalImagePath.getBytes());
            baos.write('\n');
            baos.write(handSignaturePath.getBytes());
            baos.write('\n');
        }
        return baos.toByteArray();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ausweisnummer:\n");
        sb.append(ID_number);
        sb.append("\nÖffentliches Profil:\n");
        sb.append(publicProfile.name);
        sb.append("\nÖffentliches Profil Folgenummer:\n");
        sb.append(publicProfile.sequence_number);
        sb.append("\nErstellt:\n");
        sb.append(created);
        sb.append("\nGültig bis:\n");
        sb.append(validUntil);
        sb.append("\nVorname:\n");
        sb.append(name);
        sb.append("\nNachname:\n");
        sb.append(surname);
        sb.append("\nGeburtsdatum\n");
        sb.append(birthdate);
        sb.append("\nAdresse:\n");
        sb.append(address);

        for (int i = 0; i < publicProfile.dynamicAttributes.length; i++) {
            sb.append('\n');
            sb.append(publicProfile.dynamicAttributes[i]);
            sb.append(":\n");
            sb.append(dynamicAttributesValues[i]);
        }

        sb.append("\nPfad Passbild:\n");
        sb.append(personalImagePath);
        sb.append("\nPfad händische Signatur:\n");
        sb.append(handSignaturePath);
        sb.append('\n');
        return sb.toString();
    }
}
