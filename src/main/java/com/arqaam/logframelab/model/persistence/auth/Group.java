package com.arqaam.logframelab.model.persistence.auth;

import com.arqaam.logframelab.model.persistence.AuditableEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity(name = "Group")
@Table(name = "GROUPS")
@EqualsAndHashCode(callSuper = false)
public class Group extends AuditableEntity<Integer> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "GROUP_NAME")
  private String name;

  @JoinColumn(name = "GROUP_ID")
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<GroupAuthority> authorities = new HashSet<>();

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<GroupMember> members = new HashSet<>();
}
