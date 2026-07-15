// Assuming that `GetMenuResponse` has a `from(Menu menu)` method that maps Menu to GetMenuResponse
package com.example.ch6project.domain.menu.dto;

import com.example.ch6project.domain.menu.entity.Menu;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetMenuResponse {
    private Long id;
    private String name;
    private Long price;

    public static GetMenuResponse from(Menu menu) {
        GetMenuResponse response = new GetMenuResponse();
        response.id = menu.getId();
        response.name = menu.getName();
        response.price = menu.getPrice();
        return response;
    }
}