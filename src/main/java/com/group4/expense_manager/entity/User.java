package com.group4.expense_manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_role", columnList = "role")
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role = "client";

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // --- Mối quan hệ ---

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Category> categories;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Expense> expenses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Income> incomes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Budget> budgets;

    @OneToMany(mappedBy = "user") // ON DELETE SET NULL được xử lý ở mức DB hoặc @OnDelete
    private Set<ActivityLog> activityLogs;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Chuyển đổi role (String) của bạn thành GrantedAuthority
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.toUpperCase()));
    }

    @Override
    public String getPassword() {
        return this.passwordHash; // Trả về trường mật khẩu đã băm
    }

    @Override
    public String getUsername() {
        return this.email; // Dùng email làm username để đăng nhập
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Hoặc logic riêng nếu bạn có
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Hoặc logic riêng
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Hoặc logic riêng
    }

    @Override
    public boolean isEnabled() {
        return this.isActive; // Dùng trường isActive của bạn
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}