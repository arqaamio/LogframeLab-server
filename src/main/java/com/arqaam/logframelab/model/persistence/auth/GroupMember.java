package com.arqaam.logframelab.model.persistence.auth;

import com.arqaam.logframelab.model.persistence.AuditableEntity;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity(name = "GroupMember")
@Table(name = "GROUP_MEMBERS")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class GroupMember extends AuditableEntity<GroupMemberId> {

  @EmbeddedId
  private GroupMemberId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("username")
  @JoinColumn(name = "USERNAME", insertable = false, updatable = false)
  private User user;

  @MapsId("groupId")
  @JoinColumn(referencedColumnName = "ID", name = "GROUP_ID", insertable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Group group;

  public GroupMember(User user, Group group) {
    this.user = user;
    this.group = group;
    this.id = new GroupMemberId(user.getUsername(), group.getId());
  }
}

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
class GroupMemberId implements Serializable {

  @Column(name = "USERNAME")
  private  String username;

  @Column(name = "GROUP_ID")
  private  Integer groupId;
}
