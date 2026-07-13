package com.example.ch6project.domain.menu.controller;

import com.example.ch6project.common.response.CommonApiResponse;
import com.example.ch6project.domain.menu.dto.GetMenuResponse;
import com.example.ch6project.domain.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<CommonApiResponse<List<GetMenuResponse>>> getMenus() {
        List<GetMenuResponse> data = menuService.getMenus();

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonApiResponse.success(HttpStatus.OK,"메뉴 조회 성공",data));
    }
}
