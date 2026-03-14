package com.freetv.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/canales")
public class CanalController {

    @Autowired
    private CanalRepository canalRepository;
    
    @Autowired
    private CanalService canalService;

    @GetMapping
    public List<Canal> obtenerCanales() {
        return canalRepository.findAll();
    }

    @GetMapping("/buscar")
    public List<Canal> buscarPorNombre(@RequestParam String nombre) {
        return canalRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @GetMapping("/actualizar")
    public String forzarActualizacion() {
        canalService.actualizarCanales();
        return "¡Canales actualizados correctamente en la base de datos!";
    }
}