package com.ddhva.ielts.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "\"learner\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class Learner extends User{
    private String phoneNumber;

    @OneToMany(mappedBy = "learner", fetch = FetchType.LAZY)
    private List<Library> libraries;

    @OneToMany(mappedBy = "learner", fetch = FetchType.LAZY)
    private List<Submissions> submissions;
}
