package com.ddhva.ielts.model;


import com.ddhva.ielts.enums.RoleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "\"roles\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    private String name;

    @Enumerated(EnumType.STRING)
    private RoleStatus status;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private List<User> users;
}