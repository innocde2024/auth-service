package swp.group2.swpbe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Service
public class CloudinaryService {
  @Autowired
  private Cloudinary cloudinary;

  public Map upload(MultipartFile file) {
    try {
      return this.cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
          "resource_type", "auto"));
    } catch (IOException e) {
      return Collections.emptyMap();
    }

  }
}