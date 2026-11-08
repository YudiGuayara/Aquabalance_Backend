package com.AquaBalance.shared.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba unitaria pura del GlobalExceptionHandler.
 * No necesita MockMvc ni contexto de Spring: instanciamos el handler directamente
 * y verificamos que cada método retorna el ResponseEntity correcto.
 */
@DisplayName("GlobalExceptionHandler - Pruebas Unitarias")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ================================================================
    // ResourceNotFoundException → 404
    // ================================================================
    @Nested
    @DisplayName("handleNotFound() — ResourceNotFoundException")
    class HandleNotFound {

        @Test
        @DisplayName("Debe retornar status 404 NOT_FOUND")
        void debeRetornar404() {
            ResourceNotFoundException ex =
                    new ResourceNotFoundException("Recurso no encontrado con id: 99");

            ResponseEntity<ErrorResponse> respuesta =
                    handler.handleNotFound(ex);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("El cuerpo debe contener el código 404")
        void cuerpoDbeContenerCodigo404() {
            ResourceNotFoundException ex =
                    new ResourceNotFoundException("No encontrado");

            ErrorResponse body = handler.handleNotFound(ex).getBody();

            assertThat(body).isNotNull();
            assertThat(body.getStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("El cuerpo debe contener el mensaje original de la excepción")
        void cuerpoDbeContenerMensajeOriginal() {
            String mensaje = "Contaminante no encontrado con id: 5";
            ResourceNotFoundException ex = new ResourceNotFoundException(mensaje);

            ErrorResponse body = handler.handleNotFound(ex).getBody();

            assertThat(body).isNotNull();
            assertThat(body.getMessage()).isEqualTo(mensaje);
        }
    }

    // ================================================================
    // BusinessException → 400
    // ================================================================
    @Nested
    @DisplayName("handleBusiness() — BusinessException")
    class HandleBusiness {

        @Test
        @DisplayName("Debe retornar status 400 BAD_REQUEST")
        void debeRetornar400() {
            BusinessException ex = new BusinessException("El correo ya está registrado");

            ResponseEntity<ErrorResponse> respuesta =
                    handler.handleBusiness(ex);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("El cuerpo debe contener el código 400 y el mensaje de negocio")
        void cuerpoDbeContenerCodigo400YMensaje() {
            String mensaje = "El correo ya está registrado";
            BusinessException ex = new BusinessException(mensaje);

            ErrorResponse body = handler.handleBusiness(ex).getBody();

            assertThat(body).isNotNull();
            assertThat(body.getStatus()).isEqualTo(400);
            assertThat(body.getMessage()).isEqualTo(mensaje);
        }
    }

    // ================================================================
    // IllegalArgumentException → 400
    // ================================================================
    @Nested
    @DisplayName("handleIllegalArgument() — IllegalArgumentException")
    class HandleIllegalArgument {

        @Test
        @DisplayName("Debe retornar status 400 BAD_REQUEST")
        void debeRetornar400() {
            IllegalArgumentException ex =
                    new IllegalArgumentException("El pH debe estar entre 0 y 14");

            ResponseEntity<ErrorResponse> respuesta =
                    handler.handleIllegalArgument(ex);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("El cuerpo debe contener el código 400 y el mensaje de validación")
        void cuerpoDbeContenerCodigo400YMensajeDeValidacion() {
            String mensaje = "El pH debe estar entre 0 y 14";
            IllegalArgumentException ex = new IllegalArgumentException(mensaje);

            ErrorResponse body = handler.handleIllegalArgument(ex).getBody();

            assertThat(body).isNotNull();
            assertThat(body.getStatus()).isEqualTo(400);
            assertThat(body.getMessage()).isEqualTo(mensaje);
        }
    }

    // ================================================================
    // Exception genérica → 500
    // ================================================================
    @Nested
    @DisplayName("handleGeneral() — Exception genérica")
    class HandleGeneral {

        @Test
        @DisplayName("Debe retornar status 500 INTERNAL_SERVER_ERROR")
        void debeRetornar500() {
            Exception ex = new RuntimeException("Error inesperado en la base de datos");

            ResponseEntity<ErrorResponse> respuesta = handler.handleGeneral(ex);

            assertThat(respuesta.getStatusCode())
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("El cuerpo debe contener código 500 y mensaje genérico (no el mensaje original)")
        void cuerpoDbeContenerCodigo500YMensajeGenerico() {
            Exception ex = new RuntimeException("Detalle interno sensible");

            ErrorResponse body = handler.handleGeneral(ex).getBody();

            assertThat(body).isNotNull();
            assertThat(body.getStatus()).isEqualTo(500);
            // El mensaje no debe filtrar el detalle interno al cliente
            assertThat(body.getMessage())
                    .isEqualTo("Error interno del servidor")
                    .doesNotContain("Detalle interno sensible");
        }

        @Test
        @DisplayName("Debe manejar cualquier tipo de excepción genérica (NullPointerException)")
        void debeManejarNullPointerException() {
            Exception ex = new NullPointerException("Valor nulo inesperado");

            ResponseEntity<ErrorResponse> respuesta = handler.handleGeneral(ex);

            assertThat(respuesta.getStatusCode())
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("Debe manejar excepción con mensaje nulo sin lanzar otra excepción")
        void debeManejarExcepcionConMensajeNulo() {
            Exception ex = new RuntimeException((String) null);

            assertThat(handler.handleGeneral(ex).getStatusCode())
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // Consistencia de cuerpos
    // ================================================================
    @Nested
    @DisplayName("Consistencia del ErrorResponse")
    class ConsistenciaErrorResponse {

        @Test
        @DisplayName("El cuerpo nunca debe ser nulo para ningún tipo de excepción")
        void cuerpaNuncaDebeSerNulo() {
            assertThat(handler.handleNotFound(
                    new ResourceNotFoundException("x")).getBody()).isNotNull();
            assertThat(handler.handleBusiness(
                    new BusinessException("x")).getBody()).isNotNull();
            assertThat(handler.handleIllegalArgument(
                    new IllegalArgumentException("x")).getBody()).isNotNull();
            assertThat(handler.handleGeneral(
                    new RuntimeException("x")).getBody()).isNotNull();
        }

        @Test
        @DisplayName("Los códigos de status en el cuerpo deben coincidir con el status HTTP")
        void codigosDebenCoincidir() {
            assertThat(handler.handleNotFound(
                    new ResourceNotFoundException("x")).getBody().getStatus()).isEqualTo(404);
            assertThat(handler.handleBusiness(
                    new BusinessException("x")).getBody().getStatus()).isEqualTo(400);
            assertThat(handler.handleIllegalArgument(
                    new IllegalArgumentException("x")).getBody().getStatus()).isEqualTo(400);
            assertThat(handler.handleGeneral(
                    new RuntimeException("x")).getBody().getStatus()).isEqualTo(500);
        }
    }
}
