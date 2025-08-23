package com.example.demo.dto;

import java.util.List;

public record UpdateUserRolesRequest(
        List<String> set,    // αν δοθεί, αντικαθιστά ΟΛΟΥΣ τους ρόλους
        List<String> add,    // αλλιώς: πρόσθεσε αυτούς
        List<String> remove  // και/ή αφαίρεσε αυτούς
) {}