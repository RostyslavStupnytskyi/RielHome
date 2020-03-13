package stupnytskiy.rostyslav.demo.tools;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Component
public class FileTool {
//    public static final String IMG_DIR =
//            System.getProperty("user.home") + File.separator +
//                    "dealer-images" + File.separator;//

    public static final String USER_DIR =
            System.getProperty("user.home") + File.separator +
                    "riel-home-images" + File.separator;
//            "D:\\Server\\Zepka\\users" + File.separator;

    public String saveRealtyImage(String img, String userDir) throws IOException {
        final String newDir = USER_DIR + userDir + File.separator + "realty-images" + File.separator;
        return saveFile(img,newDir);
    }

    public String saveUserAvatar(String img, String userDir) throws IOException {
        final String newDir = USER_DIR + userDir + File.separator;
        return saveFile(img,newDir);
    }

    private String saveFile(String img, String dir) throws IOException {
        createDir(dir);//create folder if not exists

        String[] data = img.split(",");
        String metaInfo = data[0];
        String base64File = data[1];

        String fileName = createFileName(null,
                getFileExtensionFromMetaInfo(metaInfo));

        Files.write(
                Paths.get(dir, fileName),
                Base64.getDecoder().decode(base64File.getBytes())
        );
        return fileName;
    }

    private String createFileName(String fileName, String fileExtension) {
        if (fileName == null) {
            fileName = UUID.randomUUID().toString();
        }
        return String.format("%s.%s", fileName, fileExtension);
    }

    //data:image/jpeg;base64
    private String getFileExtensionFromMetaInfo(String metaInfo) {
        return metaInfo.split("/")[1].split(";")[0];
    }

    private void createDir(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
}
