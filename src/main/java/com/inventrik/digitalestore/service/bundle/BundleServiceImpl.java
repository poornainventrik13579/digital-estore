package com.inventrik.digitalestore.service.bundle;

import com.inventrik.digitalestore.domain.bundle.BundleItem;
import com.inventrik.digitalestore.domain.bundle.ProductBundle;
import com.inventrik.digitalestore.domain.product.Product;
import com.inventrik.digitalestore.dto.request.BundleRequest;
import com.inventrik.digitalestore.dto.response.BundleResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.BundleItemRepository;
import com.inventrik.digitalestore.repository.ProductBundleRepository;
import com.inventrik.digitalestore.repository.ProductRepository;
import com.inventrik.digitalestore.repository.TenantRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import com.inventrik.digitalestore.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BundleServiceImpl implements BundleService {
    
    private final ProductBundleRepository bundleRepository;
    private final BundleItemRepository bundleItemRepository;
    private final ProductRepository productRepository;
    private final TenantRepository tenantRepository;
    private final IdGeneratorService idGeneratorService;
    private final UserService userService;
    
    @Override
    @Transactional(readOnly = true)
    public List<BundleResponse> getAllBundles(Integer tenantId, String status, String name, String productId) {
        if (productId != null) {
            List<BundleItem> bundleItems = bundleItemRepository.findBundlesContainingProduct(tenantId, productId);
            return bundleItems.stream()
                    .map(BundleItem::getProductBundle)
                    .distinct()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        if (name != null && !name.trim().isEmpty()) {
            return bundleRepository.findByBundleNameContaining(tenantId, name).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        if ("ACTIVE".equalsIgnoreCase(status)) {
            return bundleRepository.findActiveBundles(tenantId).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        return bundleRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public BundleResponse getBundle(Integer tenantId, String bundleId) {
        ProductBundle bundle = bundleRepository.findByTenantIdAndBundleId(tenantId, bundleId)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle not found with id: " + bundleId));
        return mapToResponse(bundle);
    }

    @Override
    public BundleResponse createBundle(Integer tenantId, BundleRequest bundleRequest, String username) {
        tenantRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        if (!validateBundleComposition(tenantId, bundleRequest.getBundleItems())) {
            throw new IllegalArgumentException("Invalid bundle composition");
        }

        String newBundleId = idGeneratorService.generateId(tenantId, "BUNDLE");
        
        ProductBundle bundle = new ProductBundle();
        bundle.setTenantId(tenantId);
        bundle.setBundleId(newBundleId);
        bundle.setBundleName(bundleRequest.getBundleName());
        bundle.setDescription(bundleRequest.getDescription());
        bundle.setBundlePrice(bundleRequest.getBundlePrice());
        bundle.setDiscountPercent(bundleRequest.getDiscountPercent());
        bundle.setCurrency(bundleRequest.getCurrency());
        bundle.setStatus("0");
        bundle.setCreatedBy(userService.getAuditCode(username));
        bundle.setUpdatedBy(userService.getAuditCode(username));
        bundle.setCreated(LocalDateTime.now());
        bundle.setUpdated(LocalDateTime.now());
        
        ProductBundle savedBundle = bundleRepository.save(bundle);
        
        for (BundleRequest.BundleItemRequest itemRequest : bundleRequest.getBundleItems()) {
            String newItemId = idGeneratorService.generateId(tenantId, "BUNDLE_ITEM");
            
            BundleItem bundleItem = new BundleItem();
            bundleItem.setTenantId(tenantId);
            bundleItem.setBundleItemId(newItemId);
            bundleItem.setBundleId(newBundleId);
            bundleItem.setProductId(itemRequest.getProductId());
            bundleItem.setQuantity(itemRequest.getQuantity());
            bundleItem.setStatus("0");
            bundleItem.setCreatedBy(userService.getAuditCode(username));
            bundleItem.setUpdatedBy(userService.getAuditCode(username));
            bundleItem.setCreated(LocalDateTime.now());
            bundleItem.setUpdated(LocalDateTime.now());
            
            bundleItemRepository.save(bundleItem);
        }
        
        return mapToResponse(savedBundle);
    }
    
    @Override
    public BundleResponse updateBundle(Integer tenantId, String bundleId, BundleRequest bundleRequest, String username) {
        ProductBundle bundle = bundleRepository.findByTenantIdAndBundleId(tenantId, bundleId)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle not found with id: " + bundleId));
        
        bundle.setBundleName(bundleRequest.getBundleName());
        bundle.setDescription(bundleRequest.getDescription());
        bundle.setBundlePrice(bundleRequest.getBundlePrice());
        bundle.setDiscountPercent(bundleRequest.getDiscountPercent());
        bundle.setCurrency(bundleRequest.getCurrency());
        bundle.setUpdatedBy(userService.getAuditCode(username));
        bundle.setUpdated(LocalDateTime.now());
        
        ProductBundle savedBundle = bundleRepository.save(bundle);
        return mapToResponse(savedBundle);
    }
    
    @Override
    public void deleteBundle(Integer tenantId, String bundleId, String username) {
        ProductBundle bundle = bundleRepository.findByTenantIdAndBundleId(tenantId, bundleId)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle not found with id: " + bundleId));
        
        bundle.setStatus("-1");
        bundle.setUpdatedBy(userService.getAuditCode(username));
        bundle.setUpdated(LocalDateTime.now());
        
        bundleRepository.save(bundle);
    }
    
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateBundlePrice(Integer tenantId, List<BundleRequest.BundleItemRequest> bundleItems) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        
        for (BundleRequest.BundleItemRequest item : bundleItems) {
            Product product = productRepository.findByTenantIdAndProductIdAndStatus(tenantId, item.getProductId(), "0")
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + item.getProductId()));
            
            BigDecimal itemTotal = product.getDefaultPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);
        }
        
        return totalPrice.setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateBundleComposition(Integer tenantId, List<BundleRequest.BundleItemRequest> bundleItems) {
        if (bundleItems == null || bundleItems.isEmpty()) {
            return false;
        }
        
        // Check if all products exist and are active
        for (BundleRequest.BundleItemRequest item : bundleItems) {
            boolean productExists = productRepository.findByTenantIdAndProductIdAndStatus(tenantId, item.getProductId(), "0")
                    .isPresent();
            if (!productExists) {
                log.warn("Product not found or inactive: productId={}, tenantId={}", item.getProductId(), tenantId);
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public BundleResponse addProductToBundle(Integer tenantId, String bundleId, String productId, Integer quantity, String username) {
        ProductBundle bundle = bundleRepository.findByTenantIdAndBundleId(tenantId, bundleId)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle not found with id: " + bundleId));
        
        // Check if product exists
        productRepository.findByTenantIdAndProductIdAndStatus(tenantId, productId, "0")
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // Create new bundle item
        BundleItem bundleItem = new BundleItem();
        bundleItem.setTenantId(tenantId);
        bundleItem.setBundleId(bundleId);
        bundleItem.setProductId(productId);
        bundleItem.setQuantity(quantity);
        bundleItem.setStatus("0");
        bundleItem.setCreatedBy("1");
        bundleItem.setUpdatedBy("1");
        
        bundleItemRepository.save(bundleItem);
        
        log.info("Product added to bundle: bundleId={}, productId={}, quantity={}, tenantId={}, username={}", 
                bundleId, productId, quantity, tenantId, username);
        
        return getBundle(tenantId, bundleId);
    }
    
    @Override
    public BundleResponse removeProductFromBundle(Integer tenantId, String bundleId, String productId, String username) {
        List<BundleItem> bundleItems = bundleItemRepository.findByTenantIdAndBundleIdAndStatus(tenantId, bundleId, "0");
        
        bundleItems.stream()
                .filter(item -> item.getProductId().equals(productId))
                .forEach(item -> {
                    item.setStatus("-1");
                    item.setUpdatedBy("1");
                    bundleItemRepository.save(item);
                });
        
        log.info("Product removed from bundle: bundleId={}, productId={}, tenantId={}, username={}", 
                bundleId, productId, tenantId, username);
        
        return getBundle(tenantId, bundleId);
    }
    
    @Override
    public BundleResponse updateProductQuantityInBundle(Integer tenantId, String bundleId, String productId, Integer quantity, String username) {
        List<BundleItem> bundleItems = bundleItemRepository.findByTenantIdAndBundleIdAndStatus(tenantId, bundleId, "0");
        
        bundleItems.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        item -> {
                            item.setQuantity(quantity);
                            item.setUpdatedBy("1");
                            bundleItemRepository.save(item);
                        },
                        () -> {
                            throw new ResourceNotFoundException("Product not found in bundle");
                        }
                );
        
        log.info("Product quantity updated in bundle: bundleId={}, productId={}, quantity={}, tenantId={}, username={}", 
                bundleId, productId, quantity, tenantId, username);
        
        return getBundle(tenantId, bundleId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer getBundleCount(Integer tenantId) {
        return bundleRepository.countActiveBundles(tenantId);
    }
    
    private BundleResponse mapToResponse(ProductBundle bundle) {
        List<BundleItem> bundleItems = bundleItemRepository.findActiveBundleItems(bundle.getTenantId(), bundle.getBundleId());
        
        List<BundleResponse.BundleItemResponse> itemResponses = new ArrayList<>();
        BigDecimal totalOriginalPrice = BigDecimal.ZERO;
        
        for (BundleItem item : bundleItems) {
            Product product = productRepository.findByTenantIdAndProductIdAndStatus(
                    item.getTenantId(), item.getProductId(), "0").orElse(null);
            
            if (product != null) {
                BigDecimal itemTotal = product.getDefaultPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                totalOriginalPrice = totalOriginalPrice.add(itemTotal);
                
                BundleResponse.BundleItemResponse itemResponse = new BundleResponse.BundleItemResponse();
                itemResponse.setBundleItemId(item.getBundleItemId());
                itemResponse.setProductId(product.getProductId());
                itemResponse.setProductName(product.getProductName());
                itemResponse.setProductDescription(product.getDescription());
                itemResponse.setProductPrice(product.getDefaultPrice());
                itemResponse.setProductCurrency(product.getDefaultCurrency());
                itemResponse.setQuantity(item.getQuantity());
                itemResponse.setItemTotal(itemTotal);
                itemResponse.setStatus(item.getStatus());
                
                itemResponses.add(itemResponse);
            }
        }
        
        BigDecimal totalSavings = totalOriginalPrice.subtract(bundle.getBundlePrice());
        
        BundleResponse response = new BundleResponse();
        response.setBundleId(bundle.getBundleId());
        response.setTenantId(bundle.getTenantId());
        response.setBundleName(bundle.getBundleName());
        response.setDescription(bundle.getDescription());
        response.setBundlePrice(bundle.getBundlePrice());
        response.setDiscountPercent(bundle.getDiscountPercent());
        response.setCurrency(bundle.getCurrency());
        response.setStatus(bundle.getStatus());
        response.setCreated(bundle.getCreated());
        response.setUpdated(bundle.getUpdated());
        response.setBundleItems(itemResponses);
        response.setTotalOriginalPrice(totalOriginalPrice);
        response.setTotalSavings(totalSavings);
        response.setTotalProducts(itemResponses.size());
        
        return response;
    }
} 