package com.prj.learnvocabularybe.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file, String folder, String publicId) throws Exception {
        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "image",
                        "folder", folder,
                        "public_id", publicId
                )
        );
        return (String) result.get("secure_url");
    }

    public String uploadAudioMp3(byte[] mp3Bytes, String folder, String publicId) throws Exception {
        Map<?, ?> result = cloudinary.uploader().upload(
                mp3Bytes,
                ObjectUtils.asMap(
                        "resource_type", "video",
                        "folder", folder,
                        "public_id", publicId,
                        "format", "mp3"
                )
        );
        return (String) result.get("secure_url");
    }
}