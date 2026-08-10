package com.jrdev.whatsappplatform.service;

import com.jrdev.whatsappplatform.model.Media;
import com.jrdev.whatsappplatform.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaService {
    private final MediaRepository mediaRepository;

    public List<Media> buscarTodas() {
        return mediaRepository.buscarTodo();
    }

    public Media buscarPorId(Long id) {
        return mediaRepository.buscarPorId(id).orElse(null);
    }

    public Media crear(Media media) {
        mediaRepository.crear(media);
        return mediaRepository
                .buscarTodo()
                .stream()
                .reduce((primero, segundo) -> segundo)
                .orElse(null);
    }

    public boolean actualizar(Long id, Media media) {
        return mediaRepository.actualizar(id, media) > 0;
    }

}
