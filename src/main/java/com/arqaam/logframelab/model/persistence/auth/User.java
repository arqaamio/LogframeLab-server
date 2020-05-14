package com.arqaam.logframelab.model.persistence.auth;

import com.arqaam.logframelab.model.persistence.AuditableEntity;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Entity(name = "User")
@Table(name = "USERS")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AuditableEntity<String> implements UserDetails {

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private final Set<GroupMember> groupMembership = new HashSet<>();

  @Id private String username;

  @Column(name = "PASSWORD")
  private String password;

  @Column(name = "ENABLED")
  private boolean enabled;

  public void addGroup(Group group) {
    GroupMember groupMember = new GroupMember(this, group);
    groupMembership.add(groupMember);
    group.getMembers().add(groupMember);
  }

  public void addGroups(Collection<Group> groups) {
    groups.forEach(this::addGroup);
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

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return groupMembership.stream()
        .flatMap(
            groupMember ->
                groupMember.getGroup().getAuthorities().stream()
                    .map(
                        groupAuthority ->
                            new SimpleGrantedAuthority(groupAuthority.getId().getAuthority())))
        .collect(Collectors.toSet());
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return this.isEnabled();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof User)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    User user = (User) o;
    return getUsername().equals(user.getUsername());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getUsername());
  }
}
