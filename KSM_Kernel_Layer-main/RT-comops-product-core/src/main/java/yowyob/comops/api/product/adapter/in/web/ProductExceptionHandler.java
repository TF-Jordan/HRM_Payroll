package yowyob.comops.api.product.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.product.domain.DuplicateProductSkuException;
import yowyob.comops.api.product.domain.ProductNotFoundException;
import yowyob.comops.api.product.domain.ProductSearchUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ProductController.class)
public class ProductExceptionHandler {

    @ExceptionHandler(DuplicateProductSkuException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateSku(DuplicateProductSkuException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "PRODUCT_SKU_DUPLICATE"));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ProductNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(exception.getMessage(), "PRODUCT_NOT_FOUND"));
    }

    @ExceptionHandler(ProductSearchUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleSearchUnavailable(ProductSearchUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.failure(exception.getMessage(), "PRODUCT_SEARCH_UNAVAILABLE"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(exception.getMessage(), "PRODUCT_INVALID_REQUEST"));
    }
}
