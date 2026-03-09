package com.chakray.users_api.service;

import com.chakray.users_api.model.User;
import com.chakray.users_api.model.Address;
import com.chakray.users_api.util.AESUtil;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UserService {

    private final List<User> users = new ArrayList<>();

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public UserService() {

        User user1 = new User(
                UUID.randomUUID(),
                "user1@mail.com",
                "user1",
                "+15555555555",
                AESUtil.encrypt("password1"),
                "AARR990101XXX",
                "01-01-2026 00:00",
                Arrays.asList(
                        new Address(1, "workaddress", "street No. 1", "UK"),
                        new Address(2, "homeaddress", "street No. 2", "AU")
                )
        );

        User user2 = new User(
                UUID.randomUUID(),
                "user2@mail.com",
                "monica",
                "+15555555556",
                AESUtil.encrypt("password2"),
                "BARR990101XXX",
                "01-01-2026 00:00",
                new ArrayList<>()
        );

        User user3 = new User(
                UUID.randomUUID(),
                "user3@mail.com",
                "rogelio",
                "+15555555557",
                AESUtil.encrypt("password3"),
                "CARR990101XXX",
                "01-01-2026 00:00",
                new ArrayList<>()
        );

        users.add(user1);
        users.add(user2);
        users.add(user3);
    }

    public List<User> getUsers(String sortedBy, String filter) {

        List<User> result = new ArrayList<>(users);

        // -------- FILTER --------
        if (filter != null && !filter.isEmpty()) {

            String[] parts = filter.split("\\+");

            if (parts.length == 3) {

                String field = parts[0];
                String operator = parts[1];
                String value = parts[2];

                result.removeIf(user -> {

                    String fieldValue = getFieldValue(user, field);

                    if (fieldValue == null) return true;

                    switch (operator) {

                        case "co":
                            return !fieldValue.contains(value);

                        case "eq":
                            return !fieldValue.equals(value);

                        case "sw":
                            return !fieldValue.startsWith(value);

                        case "ew":
                            return !fieldValue.endsWith(value);

                        default:
                            return true;
                    }
                });
            }
        }

        // -------- SORT --------
        if (sortedBy != null && !sortedBy.isEmpty()) {

            result.sort(Comparator.comparing(
                    user -> getFieldValue(user, sortedBy),
                    Comparator.nullsLast(String::compareTo)
            ));
        }

        return result;
    }

    private String getFieldValue(User user, String field) {

        switch (field) {

            case "email":
                return user.getEmail();

            case "name":
                return user.getName();

            case "phone":
                return user.getPhone();

            case "tax_id":
                return user.getTaxId();

            case "created_at":
                return user.getCreatedAt();

            case "id":
                return user.getId().toString();

            default:
                return null;
        }
    }

    public User createUser(User user) {

        if (user.getTaxId() == null || !isValidRFC(user.getTaxId()))
            throw new RuntimeException("Invalid RFC format");

        if (user.getPhone() == null || !isValidPhone(user.getPhone()))
            throw new RuntimeException("Invalid phone format");

        if (!isTaxIdUnique(user.getTaxId()))
            throw new RuntimeException("tax_id must be unique");

        user.setId(UUID.randomUUID());

        if (user.getPassword() != null)
            user.setPassword(AESUtil.encrypt(user.getPassword()));

        ZonedDateTime now = ZonedDateTime.now(
                ZoneId.of("Indian/Antananarivo")
        );

        user.setCreatedAt(now.format(formatter));

        users.add(user);

        return user;
    }

    public User updateUser(UUID id, User updatedUser) {

        for (User user : users) {

            if (user.getId().equals(id)) {

                if (updatedUser.getName() != null)
                    user.setName(updatedUser.getName());

                if (updatedUser.getEmail() != null)
                    user.setEmail(updatedUser.getEmail());

                if (updatedUser.getPhone() != null) {

                    if (!isValidPhone(updatedUser.getPhone()))
                        throw new RuntimeException("Invalid phone format");

                    user.setPhone(updatedUser.getPhone());
                }

                if (updatedUser.getTaxId() != null) {

                    if (!isValidRFC(updatedUser.getTaxId()))
                        throw new RuntimeException("Invalid RFC format");

                    if (!updatedUser.getTaxId().equals(user.getTaxId())
                            && !isTaxIdUnique(updatedUser.getTaxId()))
                        throw new RuntimeException("tax_id must be unique");

                    user.setTaxId(updatedUser.getTaxId());
                }

                if (updatedUser.getPassword() != null) {
                    user.setPassword(
                            AESUtil.encrypt(updatedUser.getPassword())
                    );
                }

                return user;
            }
        }

        throw new RuntimeException("User not found");
    }

    public void deleteUser(UUID id) {
        users.removeIf(user -> user.getId().equals(id));
    }

    private boolean isValidRFC(String rfc) {
        return rfc.matches("^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^\\+?[0-9]{10,13}$");
    }

    private boolean isTaxIdUnique(String taxId) {

        for (User user : users) {
            if (user.getTaxId().equals(taxId)) {
                return false;
            }
        }

        return true;
    }

    public User login(String taxId, String password) {

        for (User user : users) {

            if (user.getTaxId().equals(taxId)) {

                String encryptedPassword = AESUtil.encrypt(password);

                if (user.getPassword().equals(encryptedPassword)) {
                    return user;
                }

                throw new RuntimeException("Invalid password");
            }
        }

        throw new RuntimeException("User not found");
    }
}