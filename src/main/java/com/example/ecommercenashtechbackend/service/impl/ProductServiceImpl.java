package com.example.ecommercenashtechbackend.service.impl;

import com.example.ecommercenashtechbackend.dto.request.ProductCreateRequestDto;
import com.example.ecommercenashtechbackend.dto.request.ProductUpdateRequestDto;
import com.example.ecommercenashtechbackend.dto.response.ProductResponseDto;
import com.example.ecommercenashtechbackend.entity.Category;
import com.example.ecommercenashtechbackend.entity.Product;
import com.example.ecommercenashtechbackend.exception.custom.ConflictException;
import com.example.ecommercenashtechbackend.repository.CategoryRepository;
import com.example.ecommercenashtechbackend.repository.ProductRepository;
import com.example.ecommercenashtechbackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public ProductResponseDto createProduct(ProductCreateRequestDto productCreateRequestDto) {
        Optional<Product> productOpt = productRepository.findByName(productCreateRequestDto.getName());
        if (!productOpt.isPresent()) {
            Product productSave = modelMapper.map(productCreateRequestDto, Product.class);
            return save(productSave, productCreateRequestDto.getCategoryId());
//            Optional<Category> categoryOpt = categoryRepository.findById(productCreateRequestDto.getCategoryId());
//            if (categoryOpt.isPresent()) {
//                Product productSave = modelMapper.map(productCreateRequestDto, Product.class);
//                productSave.setCategory(categoryOpt.get());
//                Product productSaved = productRepository.save(productSave);
//                return modelMapper.map(productSaved, ProductResponseDto.class);
//            }
//            throw new NotFoundException("Category not found");
        }
        throw new ConflictException("Product name already exits");
    }

    @Override
    public List<ProductResponseDto> getAllCategoriesPagination(int pageNumber, int pageSize, String sortField, String sortName, String keyword) {
        return null;
    }

    @Override
    public ProductResponseDto updateProduct(ProductUpdateRequestDto productUpdateRequestDto) {
        Optional<Product> productOldOpt = productRepository.findById(productUpdateRequestDto.getId());
        if (productOldOpt.isPresent()) {
            Product productOld = productOldOpt.get();
            Optional<Product> productExist = productRepository.findByName(productUpdateRequestDto.getName());
            if (!productExist.isPresent() || productExist.get().getId() != productOld.getId()) {
                Product productSave = modelMapper.map(productUpdateRequestDto, Product.class);
                return save(productSave, productUpdateRequestDto.getCategoryId());
//                Optional<Category> categoryOpt = categoryRepository.findById(productUpdateRequestDto.getCategoryId());
//                if (categoryOpt.isPresent()) {
//                    Product productSave = modelMapper.map(productUpdateRequestDto, Product.class);
//                    productSave.setCategory(categoryOpt.get());
//                    Product productSaved = productRepository.save(productSave);
//                    return modelMapper.map(productSaved, ProductResponseDto.class);
//                }
//                throw new NotFoundException("Category not found");
            }
            throw new ConflictException("Product name already exits");
        }
        throw new NotFoundException("Product not found");
    }

    ProductResponseDto save(Product productSave, Long categoryId) {
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isPresent()) {
            productSave.setCategory(categoryOpt.get());
            Product productSaved = productRepository.save(productSave);
            return modelMapper.map(productSaved, ProductResponseDto.class);
        }
        throw new NotFoundException("Category not found");
    }
}
