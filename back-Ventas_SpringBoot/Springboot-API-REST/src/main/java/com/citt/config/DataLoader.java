package com.citt.config;

import com.citt.persistence.entity.Venta;
import com.citt.persistence.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private VentaRepository ventaRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (ventaRepository.count() == 0) {
            // Venta 1
            Venta v1 = new Venta();
            v1.setDireccionCompra("P Sherman Calle Wallabi 42 Sydney");
            v1.setValorCompra(22990);
            v1.setFechaCompra(LocalDate.of(2024, 2, 3));
            v1.setDespachoGenerado(false);
            
            // Venta 2
            Venta v2 = new Venta();
            v2.setDireccionCompra("Avenida Siempre Viva 69");
            v2.setValorCompra(12590);
            v2.setFechaCompra(LocalDate.of(2024, 3, 6));
            v2.setDespachoGenerado(false);
            
            // Venta 3 
            Venta v3 = new Venta();
            v3.setDireccionCompra("Avenida Por Atras 1313");
            v3.setValorCompra(13990);
            v3.setFechaCompra(LocalDate.of(2024, 4, 20));
            v3.setDespachoGenerado(false);

            // Venta 4 
            Venta v4 = new Venta();
            v3.setDireccionCompra("Calle Presidente Kirby 8528");
            v3.setValorCompra(9990);
            v3.setFechaCompra(LocalDate.of(2024, 4, 15));
            v3.setDespachoGenerado(false);
            
            // Guardar todas las ventas
            ventaRepository.save(v1);
            ventaRepository.save(v2);
            ventaRepository.save(v3);
            ventaRepository.save(v4);
            
        }
    }
}