package org.longg.nh.kickstyleecommerce.app.api;

import com.eps.shared.models.exceptions.ResponseException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.contacts.ContactReplyRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.contacts.ContactRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.contacts.ContactResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.ContactStatus;
import org.longg.nh.kickstyleecommerce.domain.services.contacts.ContactService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
@Tag(name = "Contact Management", description = "API quản lý liên hệ khách hàng")
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    @Operation(summary = "Gửi liên hệ mới", description = "Khách hàng gửi liên hệ mới đến hệ thống")
    public ResponseEntity<ContactResponse> submitContact(@Valid @RequestBody ContactRequest request) {
        ContactResponse response = contactService.submitContact(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả liên hệ", description = "Admin lấy danh sách tất cả liên hệ với phân trang")
    public ResponseEntity<Page<ContactResponse>> getAllContacts(
            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sắp xếp theo trường (mặc định: createdAt)")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {

        String dbColumnName = mapToDbColumnName(sortBy);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(dbColumnName).descending() : Sort.by(dbColumnName).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ContactResponse> contacts = contactService.getAllContacts(pageable);
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm liên hệ", description = "Tìm kiếm liên hệ theo các tiêu chí")
    public ResponseEntity<Page<ContactResponse>> searchContacts(
            @Parameter(description = "Trạng thái liên hệ")
            @RequestParam(required = false) ContactStatus status,
            @Parameter(description = "Mức độ ưu tiên")
            @RequestParam(required = false) String priority,
            @Parameter(description = "ID admin được phân công")
            @RequestParam(required = false) Long assignedTo,
            @Parameter(description = "Email khách hàng")
            @RequestParam(required = false) String email,
            @Parameter(description = "Họ tên khách hàng")
            @RequestParam(required = false) String fullName,
            @Parameter(description = "Số trang")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sắp xếp theo")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp")
            @RequestParam(defaultValue = "desc") String sortDir) {

        // Map Java property names to database column names
        String dbColumnName = mapToDbColumnName(sortBy);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(dbColumnName).descending() : Sort.by(dbColumnName).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ContactResponse> contacts = contactService.searchContacts(
                status, priority, assignedTo, email, fullName, pageable);
        return ResponseEntity.ok(contacts);
    }

    private String mapToDbColumnName(String javaPropertyName) {
        switch (javaPropertyName) {
            case "createdAt":
                return "created_at";
            case "updatedAt":
                return "updated_at";
            case "resolvedAt":
                return "resolved_at";
            case "fullName":
                return "full_name";
            case "phoneNumber":
                return "phone_number";
            case "assignedTo":
                return "assigned_to";
            default:
                return javaPropertyName; // id, email, status, priority, etc.
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết liên hệ", description = "Admin lấy chi tiết một liên hệ cụ thể")
    public ResponseEntity<ContactResponse> getContactById(@PathVariable Long id) {
        try {
            ContactResponse contact = contactService.getById(null, id);
            return ResponseEntity.ok(contact);
        } catch (Exception e) {
            throw new ResponseException(HttpStatus.NOT_FOUND, "Liên hệ không tồn tại");
        }
    }

    @PostMapping("/{id}/reply")
    @Operation(summary = "Phản hồi liên hệ", description = "Admin phản hồi liên hệ của khách hàng")
    public ResponseEntity<ContactResponse> replyToContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactReplyRequest request,
            @Parameter(description = "ID của admin phản hồi")
            @RequestParam Long adminId) {
        
        ContactResponse response = contactService.replyToContact(id, request, adminId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Phân công liên hệ", description = "Phân công liên hệ cho admin xử lý")
    public ResponseEntity<ContactResponse> assignContact(
            @PathVariable Long id,
            @Parameter(description = "ID admin được phân công")
            @RequestParam Long adminId) {
        
        ContactResponse response = contactService.assignContact(id, adminId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(summary = "Lịch sử liên hệ", description = "Lấy lịch sử liên hệ của một email với phân trang")
    public ResponseEntity<Page<ContactResponse>> getContactHistory(
            @Parameter(description = "Email cần xem lịch sử")
            @RequestParam String email,
            @Parameter(description = "Số trang")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sắp xếp theo")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp")
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        String dbColumnName = mapToDbColumnName(sortBy);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(dbColumnName).descending() : Sort.by(dbColumnName).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ContactResponse> history = contactService.getContactHistory(email, pageable);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/by-status/{status}")
    @Operation(summary = "Lấy liên hệ theo trạng thái", description = "Lấy danh sách liên hệ theo trạng thái cụ thể")
    public ResponseEntity<Page<ContactResponse>> getContactsByStatus(
            @PathVariable ContactStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String dbColumnName = mapToDbColumnName(sortBy);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(dbColumnName).descending() : Sort.by(dbColumnName).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ContactResponse> contacts = contactService.getContactsByStatus(status, pageable);
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/unassigned")
    @Operation(summary = "Lấy liên hệ chưa được phân công", description = "Admin lấy danh sách liên hệ chưa được phân công")
    public ResponseEntity<Page<ContactResponse>> getUnassignedContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String dbColumnName = mapToDbColumnName(sortBy);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(dbColumnName).descending() : Sort.by(dbColumnName).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ContactResponse> contacts = contactService.getUnassignedContacts(pageable);
        return ResponseEntity.ok(contacts);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa liên hệ", description = "Admin xóa một liên hệ")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactService.deleteById(null, id);
        return ResponseEntity.noContent().build();
    }
} 