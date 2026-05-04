package model;

import controller.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static controller.Controller.*;

public class AttachmentRelation {
    private String text; //
    public final String filePath;

    public AttachmentRelation(String filePath, String text) {
        this.filePath = filePath;
        this.text = text;
    }

    public void insertRelation(String imageFileName, String originalFileName, String idNumber) {
        text += "\n" + imageFileName + ":" + originalFileName + ":" + idNumber;
    }

    public String getImageFileName(String idNumber) {
        for(String line: text.split("\n")) {
            String[]attributes = line.split(":");
            if(attributes.length < 3)
                continue;
            if(attributes[2].equals(idNumber))
                return attributes[0];
        }
        throw new NoSuchElementException("idNumber not found");
    }

    public List<String> getSameFileNames(String originalFileName) {
        List<String> res = new LinkedList<>();
        for(String line: text.split("\n")) {
            String[]attributes = line.split(":");
            if(attributes.length < 3)
                continue;
            if(attributes[1].equals(originalFileName))
                res.add(attributes[0]);
        }
        return res;
    }

    public void save() throws IOException {
        Files.write(Paths.get(filePath), text.getBytes());
    }

    public static AttachmentRelation getRelation(Controller controller, int attachmentMode) throws IOException {
        String filePath;
        if(attachmentMode == ATTACHMENT_PERSONAL_IMAGE) {
            filePath = controller.appDataLocation + strPersonalImageRelations;
        } else if(attachmentMode == ATTACHMENT_HAND_SIGNATURE) {
            filePath = controller.appDataLocation + strHandSignaturesRelations;
        } else {
            throw new IllegalArgumentException("not such relation type");
        }
        return new AttachmentRelation(filePath, Files.readString(Paths.get(filePath)));
    }

    public static String attachmentPath(Controller controller, int attachmentMode) {
        if(attachmentMode == ATTACHMENT_PERSONAL_IMAGE)
            return controller.appDataLocation + strPersonalImages;
        else if(attachmentMode == ATTACHMENT_HAND_SIGNATURE)
            return controller.appDataLocation + strHandSignatures;
        else
            throw new IllegalArgumentException("not such relation type");
    }
}
