package persistence.service;

import com.citt.persistence.entity.Venta;
import com.citt.persistence.repository.VentaRepository;
import com.citt.persistence.services.VentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Venta venta;

    @BeforeEach
    public void setUp(){
        venta = Venta.builder()
                .direccionCompra("Calle Falsa 123")
                .valorCompra(1000)
                .fechaCompra(LocalDate.of(2025,4,14))
                .despachoGenerado(false)
                .build();
    }

    @Test
    @DisplayName("Cuando se guarda una venta válida, entonces se persiste correctamente")
    public void whenSavingValidVenta_thenItIsPersistedCorrectly(){
        //Prepara la simulación
        when(ventaRepository.save(any(Venta.class))).thenReturn(venta);

        //Llama al servicio
        Venta savedVenta = ventaService.saveVenta(venta);

        //Verifica el resultado
        verify(ventaRepository, times(1)).save(venta);

        //Verifica que la venta guardada es la misma que la venta original
        assertNotNull(savedVenta);
        assertEquals(venta.getDireccionCompra(), savedVenta.getDireccionCompra());
        assertEquals(venta.getValorCompra(), savedVenta.getValorCompra());
        assertEquals(venta.getFechaCompra(), savedVenta.getFechaCompra());
        assertEquals(venta.getDespachoGenerado(), savedVenta.getDespachoGenerado());
    }

    @Test
    @DisplayName("Cuando se guarda una venta, entonces se asigna un ID")
    public void whenVentaIsSavedthenIdIsAssigned(){
        // Preparar
        Venta ventaToSave = Venta.builder()
                .direccionCompra("Calle Falsa 123")
                .valorCompra(1000)
                .fechaCompra(LocalDate.of(2025,4,14))
                .despachoGenerado(false)
                .build();

        Venta ventaWithId = Venta.builder()
                .idVenta(1L)
                .direccionCompra("Calle Falsa 123")
                .valorCompra(1000)
                .fechaCompra(LocalDate.of(2025,4,14))
                .despachoGenerado(false)
                .build();

        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaWithId);

        // Ejecutar
        Venta result = ventaService.saveVenta(ventaToSave);

        // Verificar
        verify(ventaRepository).save(ventaToSave);
        assertNotNull(result);
        assertEquals(1L, result.getIdVenta());
        assertEquals(ventaToSave.getDireccionCompra(), result.getDireccionCompra());
    }

    @Test
    @DisplayName("Cuando se buscan todas las ventas, entonces se retorna la lista completa")
    public void whenFindAllVentas_thenReturnList() {
        Venta venta2 = Venta.builder()
                .idVenta(2L)
                .direccionCompra("Av. Providencia 456")
                .valorCompra(5000)
                .fechaCompra(LocalDate.of(2025, 3, 10))
                .despachoGenerado(false)
                .build();

        when(ventaRepository.findAll()).thenReturn(java.util.Arrays.asList(venta, venta2));

        java.util.List<Venta> resultado = ventaService.findAllVentas();

        assertEquals(2, resultado.size());
        verify(ventaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Cuando se busca una venta por ID existente, entonces se retorna la venta")
    public void whenFindById_withValidId_thenReturnVenta() throws com.citt.exceptions.VentaNotFoundException {
        venta = Venta.builder()
                .idVenta(1L)
                .direccionCompra("Calle Falsa 123")
                .valorCompra(1000)
                .fechaCompra(LocalDate.of(2025, 4, 14))
                .despachoGenerado(false)
                .build();

        when(ventaRepository.findById(1L)).thenReturn(java.util.Optional.of(venta));

        Venta resultado = ventaService.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdVenta());
        verify(ventaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Cuando se busca una venta por ID inexistente, entonces lanza VentaNotFoundException")
    public void whenFindById_withInvalidId_thenThrowException() {
        when(ventaRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThrows(com.citt.exceptions.VentaNotFoundException.class,
                () -> ventaService.findById(99L));
    }

    @Test
    @DisplayName("Cuando se elimina una venta existente, entonces se elimina correctamente")
    public void whenDeleteVenta_withValidId_thenDeleteSuccessfully() throws com.citt.exceptions.VentaNotFoundException {
        venta = Venta.builder()
                .idVenta(1L)
                .direccionCompra("Calle Falsa 123")
                .valorCompra(1000)
                .fechaCompra(LocalDate.of(2025, 4, 14))
                .despachoGenerado(false)
                .build();

        when(ventaRepository.findById(1L)).thenReturn(java.util.Optional.of(venta));
        doNothing().when(ventaRepository).deleteById(1L);

        ventaService.deleteVenta(1L);

        verify(ventaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Cuando se elimina una venta inexistente, entonces lanza VentaNotFoundException")
    public void whenDeleteVenta_withInvalidId_thenThrowException() {
        when(ventaRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThrows(com.citt.exceptions.VentaNotFoundException.class,
                () -> ventaService.deleteVenta(99L));
    }
}
