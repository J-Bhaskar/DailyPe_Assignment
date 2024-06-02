package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.ManagerService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private ManagerService managerService;

    @PostMapping("/create_user")
    public String createUser(@RequestBody User user) {
        if (user.getFullName() == null || user.getFullName().isEmpty()) {
            return "Error: full_name must not be empty";
        }

        String mobNum = user.getMobNum();
        if (mobNum == null || !mobNum.matches("^(\\+91|0)?[6789]\\d{9}$")) {
            return "Error: Invalid mobile number";
        }
        user.setMobNum(mobNum.replaceAll("^(\\+91|0)", ""));

        String panNum = user.getPanNum();
        if (panNum == null || !Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}").matcher(panNum.toUpperCase()).matches()) {
            return "Error: Invalid PAN number";
        }
        user.setPanNum(panNum.toUpperCase());

        if (user.getManagerId() != null) {
            if (!managerService.getManagerById(user.getManagerId()).isPresent()) {
                return "Error: Invalid manager_id";
            }
        }

        userService.saveUser(user);
        return "User created successfully";
    }

    @PostMapping("/get_users")
    public List<User> getUsers(@RequestParam(required = false) UUID userId,
                               @RequestParam(required = false) String mobNum,
                               @RequestParam(required = false) UUID managerId) {
        if (userId != null) {
            return userService.getUserById(userId).map(List::of).orElse(List.of());
        } else if (mobNum != null) {
            return Optional.ofNullable(userService.getUserByMobNum(mobNum)).map(List::of).orElse(List.of());
        } else if (managerId != null) {
            return userService.getAllUsers().stream()
                    .filter(user -> managerId.equals(user.getManagerId()))
                    .toList();
        } else {
            return userService.getAllUsers();
        }
    }

    @PostMapping("/delete_user")
    public String deleteUser(@RequestParam(required = false) UUID userId,
                             @RequestParam(required = false) String mobNum) {
        if (userId != null) {
            return userService.getUserById(userId)
                    .map(user -> {
                        userService.deleteUser(user);
                        return "User deleted successfully";
                    })
                    .orElse("Error: user_id not found");
        } else if (mobNum != null) {
            return Optional.ofNullable(userService.getUserByMobNum(mobNum))
                    .map(user -> {
                        userService.deleteUser(user);
                        return "User deleted successfully";
                    })
                    .orElse("Error: mob_num not found");
        } else {
            return "Error: user_id or mob_num must be provided";
        }
    }

    @PostMapping("/update_user")
    public String updateUser(@RequestBody UserUpdateRequest request) {
        if (request.getUserIds().isEmpty() || request.getUpdateData().isEmpty()) {
            return "Error: user_ids and update_data must be provided";
        }

        List<User> users = request.getUserIds().stream()
                .map(userService::getUserById)
                .flatMap(Optional::stream)
                .toList();

        if (users.isEmpty()) {
            return "Error: No valid user_ids found";
        }

        boolean bulkUpdateManagerId = request.getUpdateData().size() == 1 && request.getUpdateData().containsKey("manager_id");
        if (bulkUpdateManagerId) {
            UUID newManagerId = UUID.fromString(request.getUpdateData().get("manager_id"));
            if (!managerService.getManagerById(newManagerId).isPresent()) {
                return "Error: Invalid manager_id";
            }
            users.forEach(user -> {
                user.setManagerId(newManagerId);
                userService.saveUser(user);
            });
            return "Users updated successfully";
        }

        if (request.getUserIds().size() > 1) {
            return "Error: Bulk update only allowed for manager_id";
        }

        User user = users.get(0);
        if (request.getUpdateData().containsKey("full_name")) {
            user.setFullName(request.getUpdateData().get("full_name"));
        }
        if (request.getUpdateData().containsKey("mob_num")) {
            user.setMobNum(request.getUpdateData().get("mob_num").replaceAll("^(\\+91|0)", ""));
        }
        if (request.getUpdateData().containsKey("pan_num")) {
            user.setPanNum(request.getUpdateData().get("pan_num").toUpperCase());
        }
        if (request.getUpdateData().containsKey("manager_id")) {
            UUID newManagerId = UUID.fromString(request.getUpdateData().get("manager_id"));
            if (!managerService.getManagerById(newManagerId).isPresent()) {
                return "Error: Invalid manager_id";
            }
            user.setManagerId(newManagerId);
        }

        userService.saveUser(user);
        return "User updated successfully";
    }
}
