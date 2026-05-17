package com.citt.config;

import com.citt.persistence.entity.Despacho;
import com.citt.persistence.repository.DespachoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private DespachoRepository despachoRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (despachoRepository.count() == 0) {
            Despacho d1 = new Despacho();
            d1.setFechaDespacho(LocalDate.of(2024, 2, 3));
            d1.setPatenteCamion("ABCD12");
            d1.setIntento(1);
            d1.setIdCompra(1L);
            d1.setDireccionCompra("P Sherman Calle Wallabi 42 Sydney");
            d1.setValorCompra(22990L);
            d1.setDespachado(false);
            
            Despacho d2 = new Despacho();
            d2.setFechaDespacho(LocalDate.of(2024, 3, 6));
            d2.setPatenteCamion("EFGH34");
            d2.setIntento(1);
            d2.setIdCompra(2L);
            d2.setDireccionCompra("Avenida Siempre Viva 69");
            d2.setValorCompra(12590L);
            d2.setDespachado(false);
            
            despachoRepository.save(d1);
            despachoRepository.save(d2);
        }
    }
}