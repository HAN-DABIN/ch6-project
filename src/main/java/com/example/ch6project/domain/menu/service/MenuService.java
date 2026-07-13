package com.example.ch6project.domain.menu.service;

import com.example.ch6project.domain.menu.dto.GetMenuResponse;
import com.example.ch6project.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    public List<GetMenuResponse> getMenus() {
        return menuRepository.findAll().stream()
                .map(GetMenuResponse::from)
                .toList();
    }
}
