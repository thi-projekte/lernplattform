package de.thi.mynd.chat.entity;

import de.thi.mynd.common.entity.BaseEntityWithId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "chat_message")
public class ChatMessage extends BaseEntityWithId {

  @Column(nullable = false)
  public UUID topicId;

  @Column(nullable = false, columnDefinition = "TEXT")
  public String message;
}
