package com.jrdev.whatsappplatform.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class SessionService {

    private final StringRedisTemplate redisTemplate;

    public SessionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Guarda el estado del usuario por 24 horas
    public void saveUserState(String instanceName, String phoneNumber, String state) {
        String key = "session:" + instanceName + ":" + phoneNumber;
        redisTemplate.opsForValue().set(key, state, 24, TimeUnit.HOURS);
    }

    // Obtiene el estado actual, si es nuevo devuelve "NUEVO_CHAT"
    public String getUserState(String instanceName, String phoneNumber) {
        String key = "session:" + instanceName + ":" + phoneNumber;
        String state = redisTemplate.opsForValue().get(key);
        return state != null ? state : "NUEVO_CHAT";
    }

    // Borra la sesión cuando el flujo termina
    public void clearSession(String instanceName, String phoneNumber) {
        String key = "session:" + instanceName + ":" + phoneNumber;
        redisTemplate.delete(key);
    }
}