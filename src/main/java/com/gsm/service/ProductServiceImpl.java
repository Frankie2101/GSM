package com.gsm.service;

import com.gsm.dto.ProductDto;
import com.gsm.dto.ProductVariantDto;
import com.gsm.exception.DuplicateResourceException;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.Product;
import com.gsm.model.ProductCategory;
import com.gsm.model.ProductVariant;
import com.gsm.model.Unit;
import com.gsm.repository.ProductCategoryRepository;
import com.gsm.repository.ProductRepository;
import com.gsm.repository.ProductVariantRepository;
import com.gsm.repository.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final UnitRepository unitRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              ProductVariantRepository productVariantRepository,
                              ProductCategoryRepository productCategoryRepository,
                              UnitRepository unitRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.unitRepository = unitRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return convertEntityToDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findAll() {
        AtomicInteger index = new AtomicInteger(1);
        return productRepository.findAll().stream()
                .map(product -> {
                    ProductDto dto = convertEntityToDtoSimple(product);
                    dto.setSequenceNumber((long) index.getAndIncrement());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * PHẦN SỬA LỖI CHÍNH NẰM Ở ĐÂY (Phiên bản 2)
     *
     * Luồng xử lý mới:
     * 1. Tiền xử lý danh sách variant DTO:
     * - Tạo một danh sách `processedVariants` mới.
     * - Duyệt qua từng variant DTO từ form.
     * - Tách chuỗi `size` (VD: "S, M, L").
     * - Size đầu tiên sẽ được coi là bản cập nhật, giữ nguyên `productVariantId` gốc.
     * - Các size tiếp theo sẽ được coi là bản ghi mới, `productVariantId` sẽ là `null`.
     * 2. Sử dụng danh sách `processedVariants` đã xử lý để thực hiện logic đồng bộ cũ dựa trên ID.
     */
    @Override
    @Transactional
    public ProductDto save(ProductDto dto) {
        // BƯỚC 1: Tiền xử lý danh sách variant DTO để tách chuỗi size
        List<ProductVariantDto> processedVariants = new ArrayList<>();
        if (dto.getVariants() != null) {
            for (ProductVariantDto variantDtoFromForm : dto.getVariants()) {
                if (variantDtoFromForm.getSize() == null || variantDtoFromForm.getSize().trim().isEmpty()) {
                    continue; // Bỏ qua nếu dòng không có size
                }

                String[] sizes = variantDtoFromForm.getSize().split("\\s*,\\s*");
                boolean isFirstSize = true;

                for (String size : sizes) {
                    if (size.trim().isEmpty()) continue;

                    ProductVariantDto processedDto = new ProductVariantDto();
                    processedDto.setColor(variantDtoFromForm.getColor());
                    processedDto.setPrice(variantDtoFromForm.getPrice());
                    processedDto.setCurrency(variantDtoFromForm.getCurrency());
                    processedDto.setSize(size.trim());

                    if (isFirstSize) {
                        // Size đầu tiên giữ lại ID gốc để thực hiện UPDATE
                        processedDto.setProductVariantId(variantDtoFromForm.getProductVariantId());
                        isFirstSize = false;
                    } else {
                        // Các size sau là bản ghi mới, không có ID
                        processedDto.setProductVariantId(null);
                    }
                    processedVariants.add(processedDto);
                }
            }
        }
        // Gán lại danh sách đã xử lý vào DTO chính
        dto.setVariants(processedVariants);

        // BƯỚC 2: Thực hiện logic đồng bộ như cũ, nhưng với danh sách variant đã được xử lý
        Product product = productRepository.findById(Optional.ofNullable(dto.getProductId()).orElse(0L))
                .orElseGet(Product::new);

        mapDtoToEntity(dto, product);

        Map<Long, ProductVariant> existingVariantsMap = product.getVariants().stream()
                .collect(Collectors.toMap(ProductVariant::getProductVariantId, v -> v));

        List<ProductVariant> variantsToSave = new ArrayList<>();
        if (dto.getVariants() != null) {
            for (ProductVariantDto variantDto : dto.getVariants()) {
                ProductVariant variant;
                Long variantId = variantDto.getProductVariantId();

                if (variantId != null) {
                    // Cập nhật variant đã có
                    variant = existingVariantsMap.get(variantId);
                    if (variant == null) {
                        throw new ResourceNotFoundException("ProductVariant not found with id: " + variantId);
                    }
                } else {
                    // Thêm variant mới
                    variant = new ProductVariant();
                    variant.setProduct(product);
                }

                variant.setColor(variantDto.getColor());
                variant.setSize(variantDto.getSize());
                variant.setPrice(variantDto.getPrice());
                variant.setCurrency(variantDto.getCurrency());
                variant.setSku(dto.getProductCode() + "-" + variantDto.getColor() + "-" + variantDto.getSize());

                variantsToSave.add(variant);
            }
        }

        // Đồng bộ với DB: Xóa hết và thêm lại list mới, orphanRemoval sẽ tự dọn dẹp
        product.getVariants().clear();
        product.getVariants().addAll(variantsToSave);

        Product savedProduct = productRepository.save(product);
        return convertEntityToDto(savedProduct);
    }

    // (Các phương thức helper khác giữ nguyên như file gốc)

    private void mapDtoToEntity(ProductDto dto, Product product) {
        ProductCategory category = productCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.getCategoryId()));
        Unit unit = unitRepository.findById(dto.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with ID: " + dto.getUnitId()));

        product.setProductCode(dto.getProductCode());
        product.setProductName(dto.getProductName());
        product.setProductCategory(category);
        product.setUnit(unit);
        product.setSeason(dto.getSeason());
        product.setStatus(dto.getStatus());
    }

    private void validateProductUniqueness(ProductDto dto) {
        productRepository.findByProductCode(dto.getProductCode()).ifPresent(existingProduct -> {
            if (dto.getProductId() == null || !existingProduct.getProductId().equals(dto.getProductId())) {
                throw new DuplicateResourceException("Product Code '" + dto.getProductCode() + "' already exists.");
            }
        });

        if (dto.getVariants() != null) {
            Set<String> skusInRequest = new HashSet<>();
            for (ProductVariantDto variant : dto.getVariants()) {
                // SKU được tạo thủ công để validate, logic này không thay đổi
                String sku = dto.getProductCode() + "-" + variant.getColor() + "-" + variant.getSize();
                if (!skusInRequest.add(sku)) {
                    throw new DuplicateResourceException("Duplicate SKU '" + sku + "' found in the request.");
                }
            }
        }
    }

    private ProductDto convertEntityToDto(Product product) {
        ProductDto dto = convertEntityToDtoSimple(product);
        dto.setSeason(product.getSeason());
        dto.setStatus(product.getStatus());

        if (product.getVariants() != null) {
            dto.setVariants(product.getVariants().stream()
                    .map(this::convertVariantEntityToDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private ProductDto convertEntityToDtoSimple(Product product) {
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setProductCode(product.getProductCode());
        dto.setProductName(product.getProductName());

        if (product.getProductCategory() != null) {
            dto.setCategoryId(product.getProductCategory().getCategoryId());
            dto.setCategoryName(product.getProductCategory().getCategoryName());
        }

        if (product.getUnit() != null) {
            dto.setUnitId(product.getUnit().getUnitId());
            dto.setUnitName(product.getUnit().getUnitName());
        }

        return dto;
    }

    private ProductVariantDto convertVariantEntityToDto(ProductVariant variant) {
        ProductVariantDto dto = new ProductVariantDto();
        dto.setProductVariantId(variant.getProductVariantId());
        dto.setColor(variant.getColor());
        dto.setSize(variant.getSize());
        dto.setSku(variant.getSku());
        dto.setPrice(variant.getPrice());
        dto.setCurrency(variant.getCurrency());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> search(String keyword, String categoryName) {
        String effectiveKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim().trim() : null;
        String effectiveCategory = (categoryName != null && !categoryName.trim().isEmpty()) ? categoryName.trim() : null;

        List<Product> products = productRepository.searchProducts(effectiveKeyword, effectiveCategory);

        AtomicInteger index = new AtomicInteger(1);
        return products.stream()
                .map(product -> {
                    ProductDto dto = convertEntityToDtoSimple(product);
                    dto.setSequenceNumber((long) index.getAndIncrement());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        for (Long id : ids) {
            if (productRepository.existsById(id)) {
                productVariantRepository.deleteByProduct_ProductId(id);
                productRepository.deleteById(id);
            }
        }
    }

    // Các phương thức helper không dùng đến vẫn được giữ lại để không thay đổi cấu trúc file
    private List<ProductVariantDto> processAndValidateVariants(ProductDto dto) {
        String productCode = dto.getProductCode();
        if (dto.getVariants() == null) {
            return Collections.emptyList();
        }

        return dto.getVariants().stream()
                .flatMap(variantDto -> {
                    String[] sizes = variantDto.getSize().split("\\s*,\\s*");
                    return Arrays.stream(sizes)
                            .filter(size -> !size.trim().isEmpty())
                            .map(size -> {
                                ProductVariantDto newVariantDto = new ProductVariantDto();
                                newVariantDto.setProductVariantId(variantDto.getProductVariantId());
                                newVariantDto.setColor(variantDto.getColor());
                                newVariantDto.setSize(size.trim());
                                newVariantDto.setPrice(variantDto.getPrice());
                                newVariantDto.setCurrency(variantDto.getCurrency());
                                newVariantDto.setSku(productCode + "-" + variantDto.getColor() + "-" + size.trim());
                                return newVariantDto;
                            });
                })
                .collect(Collectors.toList());
    }

    private Product findOrCreateAndSaveProduct(ProductDto dto) {
        Product product;
        if (dto.getProductId() != null) {
            product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cannot update. Product not found with id: " + dto.getProductId()));
        } else {
            product = new Product();
        }
        mapDtoToEntity(dto, product);
        return productRepository.save(product);
    }

    private void synchronizeVariants(ProductDto dto, Product savedProduct) {
        Set<ProductVariant> existingVariants = new HashSet<>(savedProduct.getVariants());
        Map<String, ProductVariant> existingVariantsMap = existingVariants.stream()
                .collect(Collectors.toMap(ProductVariant::getSku, variant -> variant));

        Set<String> updatedSkus = new HashSet<>();
        List<ProductVariant> variantsToSave = new ArrayList<>();

        if (dto.getVariants() != null) {
            for (ProductVariantDto variantDto : dto.getVariants()) {
                updatedSkus.add(variantDto.getSku());
                ProductVariant variant = existingVariantsMap.getOrDefault(variantDto.getSku(), new ProductVariant());

                variant.setProduct(savedProduct);
                variant.setColor(variantDto.getColor());
                variant.setSize(variantDto.getSize());
                variant.setSku(variantDto.getSku());
                variant.setPrice(variantDto.getPrice());
                variant.setCurrency(variantDto.getCurrency());

                variantsToSave.add(variant);
            }
        }

        Set<ProductVariant> variantsToRemove = existingVariants.stream()
                .filter(v -> !updatedSkus.contains(v.getSku()))
                .collect(Collectors.toSet());

        if (!variantsToRemove.isEmpty()) {
            productVariantRepository.deleteAll(variantsToRemove);
        }

        productVariantRepository.saveAll(variantsToSave);
    }
}