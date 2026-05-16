package com.birmarket.config;

import com.birmarket.entity.Category;
import com.birmarket.entity.Product;
import com.birmarket.entity.Role;
import com.birmarket.entity.User;
import com.birmarket.repository.CategoryRepository;
import com.birmarket.repository.ProductRepository;
import com.birmarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedCategories();
        seedProducts();
    }

    private void seedUsers() {

        if (userRepository.existsByEmail("admin@birmarket.az")) return;

        userRepository.save(User.builder()
                .email("admin@birmarket.az")
                .password(passwordEncoder.encode("Admin@123"))
                .fullName("Admin")
                .phone("+994501000000")
                .role(Role.ADMIN)
                .blocked(false)
                .enabled(true)
                .build());

        userRepository.save(User.builder()
                .email("leyla@birmarket.az")
                .password(passwordEncoder.encode("Test@1234"))
                .fullName("Leyla Əliyeva")
                .phone("+994502111222")
                .role(Role.SELLER)
                .blocked(false)
                .enabled(true)
                .build());

        userRepository.save(User.builder()
                .email("tural@birmarket.az")
                .password(passwordEncoder.encode("Test@1234"))
                .fullName("Tural Hüseynov")
                .phone("+994503222333")
                .role(Role.SELLER)
                .blocked(false)
                .enabled(true)
                .build());

        userRepository.save(User.builder()
                .email("nigar@gmail.com")
                .password(passwordEncoder.encode("Test@1234"))
                .fullName("Nigar Qasımova")
                .phone("+994504333444")
                .role(Role.CUSTOMER)
                .blocked(false)
                .enabled(true)
                .build());

        userRepository.save(User.builder()
                .email("rauf@gmail.com")
                .password(passwordEncoder.encode("Test@1234"))
                .fullName("Rauf Babayev")
                .phone("+994505444555")
                .role(Role.CUSTOMER)
                .blocked(false)
                .enabled(true)
                .build());
    }

    private void seedCategories() {

        if (categoryRepository.count() > 0) return;

        categoryRepository.save(Category.builder()
                .name("Elektronika")
                .description("Telefon, noutbuk və s.")
                .active(true)
                .build());

        categoryRepository.save(Category.builder()
                .name("Geyim")
                .description("Kişi və qadın geyimləri")
                .active(true)
                .build());

        categoryRepository.save(Category.builder()
                .name("Kitablar")
                .description("Bədii və elmi kitablar")
                .active(true)
                .build());

        categoryRepository.save(Category.builder()
                .name("Ev və Məişət")
                .description("Məişət məhsulları")
                .active(true)
                .build());

        categoryRepository.save(Category.builder()
                .name("İdman")
                .description("İdman məhsulları")
                .active(true)
                .build());

        categoryRepository.save(Category.builder()
                .name("Gözəllik")
                .description("Kosmetika və baxım")
                .active(true)
                .build());
    }

    private void seedProducts() {

        if (productRepository.count() > 0) return;

        User leyla = userRepository.findByEmail("leyla@birmarket.az").orElseThrow();
        User tural = userRepository.findByEmail("tural@birmarket.az").orElseThrow();

        Category elektronika = categoryRepository.findByName("Elektronika").orElseThrow();
        Category kitablar = categoryRepository.findByName("Kitablar").orElseThrow();
        Category idman = categoryRepository.findByName("İdman").orElseThrow();
        Category geyim = categoryRepository.findByName("Geyim").orElseThrow();
        Category ev = categoryRepository.findByName("Ev və Məişət").orElseThrow();

        productRepository.save(Product.builder()
                .name("Samsung Galaxy A55")
                .description("Smartphone")
                .price(BigDecimal.valueOf(699.99))
                .stockQuantity(25)
                .category(elektronika)
                .seller(leyla)
                .active(true)
                .build());

        productRepository.save(Product.builder()
                .name("AirPods Pro 2")
                .description("Apple headset")
                .price(BigDecimal.valueOf(349.00))
                .stockQuantity(15)
                .category(elektronika)
                .seller(leyla)
                .active(true).build());

        productRepository.save(Product.builder()
                .name("Lenovo IdeaPad Slim 5")
                .description("Laptop")
                .price(BigDecimal.valueOf(1199.00))
                .stockQuantity(8)
                .category(elektronika)
                .seller(leyla)
                .active(true).build());

        productRepository.save(Product.builder()
                .name("Azərbaycan tarixi")
                .description("Kitab")
                .price(BigDecimal.valueOf(45.00))
                .stockQuantity(100)
                .category(kitablar)
                .seller(leyla)
                .active(true).build());

        productRepository.save(Product.builder()
                .name("iPad Air M2")
                .description("Tablet")
                .price(BigDecimal.valueOf(899.00))
                .stockQuantity(0)
                .category(elektronika)
                .seller(leyla)
                .active(true).build());

        productRepository.save(Product.builder()
                .name("Nike Air Max 270")
                .description("Ayaqqabı")
                .price(BigDecimal.valueOf(189.99))
                .stockQuantity(40)
                .category(idman)
                .seller(tural)
                .active(true).build());

        productRepository.save(Product.builder()
                .name("Yoga Mat")
                .description("Fitness")
                .price(BigDecimal.valueOf(29.99))
                .stockQuantity(200)
                .category(idman)
                .seller(tural)
                .active(true).build());

        productRepository.save(Product.builder()
                .name("Adidas Hoodie")
                .description("Geyim")
                .price(BigDecimal.valueOf(79.99))
                .stockQuantity(60)
                .category(geyim)
                .seller(tural)
                .active(true).build());

        productRepository.save(Product.builder()
                .name("Tefal Fryer")
                .description("Mətbəx")
                .price(BigDecimal.valueOf(249.00))
                .stockQuantity(18)
                .category(ev)
                .seller(tural)
                .active(true).build());
    }
}