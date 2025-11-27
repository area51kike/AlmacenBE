package sv.edu.ues.occ.ingenieria.prn335_2025.inventario.web.core.boundary.conversores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntegerToLongConverterTest {

    private IntegerToLongConverter converter;

    @BeforeEach
    void setUp() {
        converter = new IntegerToLongConverter();
    }

    // Tests para convertToDatabaseColumn (Long -> Integer)

    @Test
    void convertToDatabaseColumn_ConValorPositivo_DebeConvertirCorrectamente() {
        // Given
        Long valorLong = 100L;

        // When
        Integer resultado = converter.convertToDatabaseColumn(valorLong);

        // Then
        assertNotNull(resultado);
        assertEquals(100, resultado);
        assertEquals(Integer.class, resultado.getClass());
    }

    @Test
    void convertToDatabaseColumn_ConValorNegativo_DebeConvertirCorrectamente() {
        // Given
        Long valorLong = -50L;

        // When
        Integer resultado = converter.convertToDatabaseColumn(valorLong);

        // Then
        assertNotNull(resultado);
        assertEquals(-50, resultado);
    }

    @Test
    void convertToDatabaseColumn_ConCero_DebeRetornarCero() {
        // Given
        Long valorLong = 0L;

        // When
        Integer resultado = converter.convertToDatabaseColumn(valorLong);

        // Then
        assertNotNull(resultado);
        assertEquals(0, resultado);
    }

    @Test
    void convertToDatabaseColumn_ConNull_DebeRetornarNull() {
        // Given
        Long valorLong = null;

        // When
        Integer resultado = converter.convertToDatabaseColumn(valorLong);

        // Then
        assertNull(resultado);
    }

    @Test
    void convertToDatabaseColumn_ConVariosValoresPositivos_DebeConvertirCorrectamente() {
        // Given
        Long[] valores = {1L, 10L, 100L, 1000L, 999999L};

        // When & Then
        for (Long valor : valores) {
            Integer resultado = converter.convertToDatabaseColumn(valor);
            assertNotNull(resultado);
            assertEquals(valor.intValue(), resultado);
        }
    }

    @Test
    void convertToDatabaseColumn_ConValorMaximoInteger_DebeConvertirCorrectamente() {
        // Given
        Long valorLong = (long) Integer.MAX_VALUE;

        // When
        Integer resultado = converter.convertToDatabaseColumn(valorLong);

        // Then
        assertNotNull(resultado);
        assertEquals(Integer.MAX_VALUE, resultado);
    }

    @Test
    void convertToDatabaseColumn_ConValorMinimoInteger_DebeConvertirCorrectamente() {
        // Given
        Long valorLong = (long) Integer.MIN_VALUE;

        // When
        Integer resultado = converter.convertToDatabaseColumn(valorLong);

        // Then
        assertNotNull(resultado);
        assertEquals(Integer.MIN_VALUE, resultado);
    }

    @Test
    void convertToDatabaseColumn_ConValorFueraDeRangoInteger_DebeTruncarValor() {
        // Given - Valor que excede el rango de Integer
        Long valorLong = (long) Integer.MAX_VALUE + 1L;

        // When
        Integer resultado = converter.convertToDatabaseColumn(valorLong);

        // Then
        assertNotNull(resultado);
        // intValue() trunca el valor si está fuera del rango
        assertEquals(Integer.MIN_VALUE, resultado);
    }

    // Tests para convertToEntityAttribute (Integer -> Long)

    @Test
    void convertToEntityAttribute_ConValorPositivo_DebeConvertirCorrectamente() {
        // Given
        Integer valorInteger = 200;

        // When
        Long resultado = converter.convertToEntityAttribute(valorInteger);

        // Then
        assertNotNull(resultado);
        assertEquals(200L, resultado);
        assertEquals(Long.class, resultado.getClass());
    }

    @Test
    void convertToEntityAttribute_ConValorNegativo_DebeConvertirCorrectamente() {
        // Given
        Integer valorInteger = -75;

        // When
        Long resultado = converter.convertToEntityAttribute(valorInteger);

        // Then
        assertNotNull(resultado);
        assertEquals(-75L, resultado);
    }

    @Test
    void convertToEntityAttribute_ConCero_DebeRetornarCero() {
        // Given
        Integer valorInteger = 0;

        // When
        Long resultado = converter.convertToEntityAttribute(valorInteger);

        // Then
        assertNotNull(resultado);
        assertEquals(0L, resultado);
    }

    @Test
    void convertToEntityAttribute_ConNull_DebeRetornarNull() {
        // Given
        Integer valorInteger = null;

        // When
        Long resultado = converter.convertToEntityAttribute(valorInteger);

        // Then
        assertNull(resultado);
    }

    @Test
    void convertToEntityAttribute_ConVariosValoresPositivos_DebeConvertirCorrectamente() {
        // Given
        Integer[] valores = {1, 10, 100, 1000, 999999};

        // When & Then
        for (Integer valor : valores) {
            Long resultado = converter.convertToEntityAttribute(valor);
            assertNotNull(resultado);
            assertEquals(valor.longValue(), resultado);
        }
    }

    @Test
    void convertToEntityAttribute_ConValorMaximoInteger_DebeConvertirCorrectamente() {
        // Given
        Integer valorInteger = Integer.MAX_VALUE;

        // When
        Long resultado = converter.convertToEntityAttribute(valorInteger);

        // Then
        assertNotNull(resultado);
        assertEquals((long) Integer.MAX_VALUE, resultado);
    }

    @Test
    void convertToEntityAttribute_ConValorMinimoInteger_DebeConvertirCorrectamente() {
        // Given
        Integer valorInteger = Integer.MIN_VALUE;

        // When
        Long resultado = converter.convertToEntityAttribute(valorInteger);

        // Then
        assertNotNull(resultado);
        assertEquals((long) Integer.MIN_VALUE, resultado);
    }

    // Tests de integración (conversión bidireccional)

    @Test
    void conversionBidireccional_DebeSerReversible() {
        // Given
        Long valorOriginal = 12345L;

        // When
        Integer enDB = converter.convertToDatabaseColumn(valorOriginal);
        Long valorRecuperado = converter.convertToEntityAttribute(enDB);

        // Then
        assertEquals(valorOriginal, valorRecuperado);
    }

    @Test
    void conversionBidireccional_ConNull_DebeSerReversible() {
        // Given
        Long valorOriginal = null;

        // When
        Integer enDB = converter.convertToDatabaseColumn(valorOriginal);
        Long valorRecuperado = converter.convertToEntityAttribute(enDB);

        // Then
        assertNull(valorRecuperado);
    }

    @Test
    void conversionBidireccional_ConCero_DebeSerReversible() {
        // Given
        Long valorOriginal = 0L;

        // When
        Integer enDB = converter.convertToDatabaseColumn(valorOriginal);
        Long valorRecuperado = converter.convertToEntityAttribute(enDB);

        // Then
        assertEquals(valorOriginal, valorRecuperado);
    }

    @Test
    void convertToDatabaseColumn_YconvertToEntityAttribute_DebeSolucionarClassCastException() {
        // Given - Simula leer un Integer de la DB
        Integer valorDesdeBD = 500;

        // When - Convertir a Long para evitar ClassCastException
        Long valorEntity = converter.convertToEntityAttribute(valorDesdeBD);

        // Then
        assertNotNull(valorEntity);
        assertTrue(valorEntity instanceof Long);
        assertEquals(500L, valorEntity);
    }
}