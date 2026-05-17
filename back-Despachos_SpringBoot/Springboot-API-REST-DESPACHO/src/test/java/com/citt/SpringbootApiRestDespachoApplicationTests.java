package com.citt;

import com.citt.exceptions.DespachoNotFoundException;
import com.citt.persistence.entity.Despacho;
import com.citt.persistence.repository.DespachoRepository;
import com.citt.persistence.services.DespachoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringbootApiRestDespachoApplicationTests {

    @Mock
    private DespachoRepository despachoRepository;

    @InjectMocks
    private DespachoServiceImpl despachoService;

    private Despacho despacho;

    @BeforeEach
    void setUp() {
        despacho = new Despacho();
        despacho.setIdDespacho(1L);
        despacho.setFechaDespacho(LocalDate.of(2024, 1, 15));
        despacho.setPatenteCamion("ABCD12");
        despacho.setIntento(1);
        despacho.setIdCompra(100L);
        despacho.setDireccionCompra("Av. Siempre Viva 742");
        despacho.setValorCompra(15000L);
        despacho.setDespachado(false);
    }

    @Test
    void findAllDespachos_debeRetornarListaDeDespachos() {
        // Arrange
        Despacho despacho2 = new Despacho();
        despacho2.setIdDespacho(2L);
        despacho2.setDireccionCompra("Calle Falsa 123");
        when(despachoRepository.findAll()).thenReturn(Arrays.asList(despacho, despacho2));

        // Act
        List<Despacho> resultado = despachoService.findAllDespachos();

        // Assert
        assertEquals(2, resultado.size());
        verify(despachoRepository, times(1)).findAll();
    }

    @Test
    void saveDespacho_debeGuardarYRetornarDespacho() {
        // Arrange
        when(despachoRepository.save(any(Despacho.class))).thenReturn(despacho);

        // Act
        Despacho resultado = despachoService.saveDespacho(despacho);

        // Assert
        assertNotNull(resultado);
        assertEquals("ABCD12", resultado.getPatenteCamion());
        assertEquals("Av. Siempre Viva 742", resultado.getDireccionCompra());
        verify(despachoRepository, times(1)).save(despacho);
    }

    @Test
    void findById_cuandoExiste_debeRetornarDespacho() throws DespachoNotFoundException {
        // Arrange
        when(despachoRepository.findById(1L)).thenReturn(Optional.of(despacho));

        // Act
        Despacho resultado = despachoService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdDespacho());
        assertEquals("ABCD12", resultado.getPatenteCamion());
    }

    @Test
    void findById_cuandoNoExiste_debeLanzarExcepcion() {
        // Arrange
        when(despachoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DespachoNotFoundException.class,
                () -> despachoService.findById(99L));
    }

    @Test
    void updateDespacho_cuandoExiste_debeActualizar() throws DespachoNotFoundException {
        // Arrange
        Despacho despachoActualizado = new Despacho();
        despachoActualizado.setFechaDespacho(LocalDate.of(2024, 6, 1));
        despachoActualizado.setPatenteCamion("XY9900");
        despachoActualizado.setIntento(2);
        despachoActualizado.setIdCompra(100L);
        despachoActualizado.setDireccionCompra("Nueva Dirección 999");
        despachoActualizado.setValorCompra(25000L);
        despachoActualizado.setDespachado(true);

        when(despachoRepository.findById(1L)).thenReturn(Optional.of(despacho));
        when(despachoRepository.save(any(Despacho.class))).thenReturn(despachoActualizado);

        // Act
        Despacho resultado = despachoService.updateDespacho(1L, despachoActualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals("XY9900", resultado.getPatenteCamion());
        verify(despachoRepository, times(1)).save(any(Despacho.class));
    }

    @Test
    void updateDespacho_cuandoNoExiste_debeLanzarExcepcion() {
        // Arrange
        when(despachoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DespachoNotFoundException.class,
                () -> despachoService.updateDespacho(99L, despacho));
    }

    @Test
    void deleteDespacho_cuandoExiste_debeEliminar() throws DespachoNotFoundException {
        // Arrange
        when(despachoRepository.findById(1L)).thenReturn(Optional.of(despacho));
        doNothing().when(despachoRepository).deleteById(1L);

        // Act
        despachoService.deleteDespacho(1L);

        // Assert
        verify(despachoRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteDespacho_cuandoNoExiste_debeLanzarExcepcion() {
        // Arrange
        when(despachoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DespachoNotFoundException.class,
                () -> despachoService.deleteDespacho(99L));
    }
}