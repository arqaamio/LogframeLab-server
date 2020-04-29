package com.arqaam.logframelab.model.persistence.auth;

import com.arqaam.logframelab.model.persistence.AuditableEntity;
import lombok.*;

import javax.persistence.*;
import java.util.*;

@Data
@Entity(name = "User")
@Table(name = "USERS")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AuditableEntity<String> {

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<GroupMember> groupMembership = new ArrayList<>();

  @Id
  private String username;

  @Column(name = "PASSWORD")
  private String password;

  @Column(name = "ENABLED")
  private boolean enabled;

  public void addGroup(Group group) {
    GroupMember groupMember = new GroupMember(this, group);
    groupMembership.add(groupMember);
    group.getMembers().add(groupMember);
  }

  public void removeGroup(Group group) {
    for (Iterator<GroupMember> iterator = groupMembership.iterator(); iterator.hasNext(); ) {
      GroupMember membership = iterator.next();

      if (membership.getUser().equals(this) && membership.getGroup().equals(group)) {
        iterator.remove();
        membership.getGroup().getMembers().remove(membership);
        membership.setGroup(null);
        membership.setUser(null);
      }
    }
  }
}
