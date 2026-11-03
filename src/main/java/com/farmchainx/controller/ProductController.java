package com.farmchainx.controller;

import com.farmchainx.dto.ProductDto;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.NotificationService;
import com.farmchainx.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
@PreAuthorize("hasAnyRole('ADMIN','FARMER')")
public class ProductController extends DashboardController {

    private final ProductService productService;

    public ProductController(ProductService productService,
                             NotificationService notificationService) {
        super(notificationService);
        this.productService = productService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0")  int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "")   String search,
                       @AuthenticationPrincipal CustomUserDetails principal,
                       Model model) {
        addCommonAttributes(model, principal);
        Page<ProductDto> products = search.isBlank()
                ? productService.findAll(PageRequest.of(page, size, Sort.by("productName")))
                : productService.search(search,  PageRequest.of(page, size, Sort.by("productName")));
        model.addAttribute("products",    products);
        model.addAttribute("search",      search);
        model.addAttribute("currentPage", page);
        return "product/list";
    }

    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        addCommonAttributes(model, principal);
        model.addAttribute("productDto", new ProductDto());
        model.addAttribute("editMode",   false);
        return "product/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("productDto") ProductDto dto,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails principal,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            addCommonAttributes(model, principal);
            model.addAttribute("editMode", false);
            return "product/form";
        }
        productService.create(dto);
        redirectAttributes.addFlashAttribute("successMsg", "Product added to catalogue.");
        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal CustomUserDetails principal, Model model) {
        addCommonAttributes(model, principal);
        model.addAttribute("productDto", productService.findById(id));
        model.addAttribute("editMode",   true);
        return "product/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("productDto") ProductDto dto,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails principal,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            addCommonAttributes(model, principal);
            model.addAttribute("editMode", true);
            return "product/form";
        }
        productService.update(id, dto);
        redirectAttributes.addFlashAttribute("successMsg", "Product updated.");
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("successMsg", "Product deleted.");
        return "redirect:/products";
    }
}
