package com.insurancecard.service;

import java.util.List;

import com.insurancecard.common.PageResponse;
import com.insurancecard.model.dto.ProductDtos.ProductRequest;
import com.insurancecard.model.dto.ProductDtos.ProductResponse;
import com.insurancecard.model.entity.InsuranceProduct;
import com.insurancecard.model.enums.ProductStatus;

public interface ProductService {

    List<ProductResponse> listActive();

    PageResponse<ProductResponse> search(String q, ProductStatus status, Integer page, Integer size);

    ProductResponse get(Long id);

    ProductResponse create(ProductRequest r);

    ProductResponse update(Long id, ProductRequest r);

    void delete(Long id);

    InsuranceProduct find(Long id);
}
