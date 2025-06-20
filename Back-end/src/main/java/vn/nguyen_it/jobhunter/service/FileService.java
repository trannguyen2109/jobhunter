package vn.nguyen_it.jobhunter.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// ý nghĩa của file này chủ yếu là liên quan đến việc tạo folder(dùng Files.createDirectory) và đường dẫn hệ thống(path)
// để lưu file do client gửi lên server và khi họ yêu cầu lấy một file nào đó thì có thể dùng InputStreamResource để đọc file và trả về file đó cho client

@Service
public class FileService {

    @Value("${nguyen_it.upload-file.base-uri}")
    private String baseURI;

    // đây là phương thức tạo ra một folder để lưu file khi client upload lên server
    public void createDirectory(String folder) throws URISyntaxException {
        // dùng URI để chuẩn hóa đường dẫn (ví dụ xử lý khi có khoảng trắng)
        URI uri = new URI(folder);

        // chuyển đường dẫn vừa chuẩn hóa bằng URI thành path(đường dẫn trong hệ thống
        // máy tính ví dụ C://folder/file/)
        Path path = Paths.get(uri);

        // có thể không cần chuyển sang file nhưng ở đây vẫn chuyển vì muốn có thể tương
        // thích với những API cũ
        File tmpDir = new File(path.toString());
        if (!tmpDir.isDirectory()) {
            try {
                // tạo ra 1 folder(nếu muốn tạo nhiều folder hơn thì dùng
                // Files.createDirectories)
                Files.createDirectory(tmpDir.toPath());
                System.out.println(">>> CREATE NEW DIRECTORY SUCCESSFUL, PATH = " + tmpDir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(">>> SKIP MAKING DIRECTORY, ALREADY EXISTS");
        }

    }

    // đây là phương thức tạo ra tên file cố định(tránh trường hợp trùng tên file)
    // và sau đó dùng folder đã
    // được tạo ở phương thức createDirectory (phương thức bên trên) để lưu lại file
    // client vừa upload lên server
    public String store(MultipartFile file, String folder) throws URISyntaxException, IOException {
        // create unique filename(tên file mặc định = thời gian upload tính bằng
        // milisecond + "-" + tên file client upload)
        String finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();

        // giống phương thức bên trên dùng URI để chuẩn hóa đường dẫn để lưu file client
        // tải lên
        URI uri = new URI(baseURI + folder + "/" + finalName);

        // chuyển uri thành path(đường dãn hệ thống)
        Path path = Paths.get(uri);
        try (InputStream inputStream = file.getInputStream()) {
            // lấy được file client gửi lên xong copy vào đường dẫn hẹ thống và kiểm trả nếu
            // nó có tồn tại thì ghi đè luôn StandardCopyOption.REPLACE_EXISTING
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        }
        return finalName;
    }

    // lấy độ dài của file(mục đích để xem file người dùng upload lên có vượt qus
    // dung lượng cho phép hay không)
    public long getFileLength(String fileName, String folder) throws URISyntaxException {
        URI uri = new URI(baseURI + folder + "/" + fileName);
        Path path = Paths.get(uri);

        File tmpDir = new File(path.toString());

        // file không tồn tại, hoặc file là 1 director => return 0
        if (!tmpDir.exists() || tmpDir.isDirectory())
            return 0;
        return tmpDir.length();
    }

    // dùng FileInputStream để đọc file và InputStreamResource sẽ bọc lại nội dung
    // của file đó và có thể trả về client nếu gọi hàm này
    public InputStreamResource getResource(String fileName, String folder)
            throws URISyntaxException, FileNotFoundException {
        URI uri = new URI(baseURI + folder + "/" + fileName);
        Path path = Paths.get(uri);

        File file = new File(path.toString());
        return new InputStreamResource(new FileInputStream(file));
    }
}