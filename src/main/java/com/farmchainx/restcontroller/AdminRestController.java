package com.farmchainx.restcontroller;

import com.farmchainx.dto.UserDto;
import com.farmchainx.enums.UserRole;
import com.farmchainx.service.UserService;
import com.farmchainx.util.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {

    private final UserService userService;

    public AdminRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.findById(id)));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long id,
            @RequestBody UserDto dto) {
        return ResponseEntity.ok(ApiResponse.success("User updated", userService.update(id, dto)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> stats() {
        var stats = new java.util.LinkedHashMap<String, Object>();
        stats.put("totalFarmers",      userService.countByRole(UserRole.FARMER));
        stats.put("totalDistributors", userService.countByRole(UserRole.DISTRIBUTOR));
        stats.put("totalWarehouses",   userService.countByRole(UserRole.WAREHOUSE_MANAGER));
        stats.put("totalRetailers",    userService.countByRole(UserRole.RETAILER));
        stats.put("totalConsumers",    userService.countByRole(UserRole.CONSUMER));
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
