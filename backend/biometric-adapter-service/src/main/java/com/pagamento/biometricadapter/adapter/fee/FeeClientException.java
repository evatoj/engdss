package com.pagamento.biometricadapter.adapter.fee;

import org.springframework.http.HttpStatusCode;

/**
 * Exceção lançada pelo {@link FeeClient} em caso de falha na comunicação
 * com a Validra API (erro HTTP, timeout, desserialização, etc.).
 *
 * <p>O {@link com.pagamento.biometricadapter.service.BiometricService} captura
 * esta exceção e publica uma resposta com status {@code ERROR} de volta ao SAGA,
 * evitando que a mensagem vá para a DLQ por erro de processamento interno.
 */
public class FeeClientException extends RuntimeException {

    private final HttpStatusCode httpStatus;

    public FeeClientException(String message) {
        super(message);
        this.httpStatus = null;
    }

    public FeeClientException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = null;
    }

    public FeeClientException(String message, HttpStatusCode httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public FeeClientException(String message, HttpStatusCode httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    /**
     * HTTP status retornado pela Validra, ou {@code null} se o erro ocorreu
     * antes de receber resposta (ex: timeout de conexão).
     */
    public HttpStatusCode getHttpStatus() {
        return httpStatus;
    }

    public boolean isClientError() {
        return httpStatus != null && httpStatus.is4xxClientError();
    }

    public boolean isServerError() {
        return httpStatus != null && httpStatus.is5xxServerError();
    }
}
