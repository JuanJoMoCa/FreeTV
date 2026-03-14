package com.freetv.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Scanner;

@Service
public class CanalService {

    @Autowired
    private CanalRepository canalRepository;

    private final String M3U_URL = "https://iptv-org.github.io/iptv/countries/mx.m3u";

    public void actualizarCanales() {
        RestTemplate restTemplate = new RestTemplate();
        String m3uContent = restTemplate.getForObject(M3U_URL, String.class);

        if (m3uContent != null) {
            canalRepository.deleteAll();
            Scanner scanner = new Scanner(m3uContent);
            String currentNombre = "Desconocido";
            String currentLogo = "";

            while (scanner.hasNextLine()) {
                String linea = scanner.nextLine();
                if (linea.startsWith("#EXTINF:-1")) {
                    if (linea.contains("tvg-logo=\"")) {
                        int start = linea.indexOf("tvg-logo=\"") + 10;
                        int end = linea.indexOf("\"", start);
                        if (start < end) currentLogo = linea.substring(start, end);
                    }
                    currentNombre = linea.substring(linea.lastIndexOf(",") + 1).trim();
                } else if (linea.startsWith("http")) {
                    Canal nuevoCanal = new Canal();
                    nuevoCanal.setNombre(currentNombre);
                    nuevoCanal.setLogoUrl(currentLogo);
                    nuevoCanal.setStreamUrl(linea);
                    canalRepository.save(nuevoCanal);
                }
            }
            scanner.close();
        }
    }
}