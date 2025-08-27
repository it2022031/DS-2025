package com.example.demo.dto;

import java.util.List;

public record UpdateUserRolesRequest(
        List<String> set,    // αντικαθιστά ΟΛΟΥΣ τους ρόλους
        List<String> add,    // προσθέτονται αυτοί
        List<String> remove  // αφαιρούνται αυτοί
) {}