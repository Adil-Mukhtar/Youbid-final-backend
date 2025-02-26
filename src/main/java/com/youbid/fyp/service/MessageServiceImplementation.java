package com.youbid.fyp.service;

import com.youbid.fyp.DTO.MessageDTO;
import com.youbid.fyp.DTO.UserDTO;
import com.youbid.fyp.model.Chat;
import com.youbid.fyp.model.Message;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImplementation implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Override
    public MessageDTO sendMessage(Chat chat, User sender, User receiver, String content) throws Exception {
        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);

        Message savedMessage = messageRepository.save(message);

        return new MessageDTO(
                savedMessage.getId(),
                new UserDTO(sender.getId(), sender.getFirstname(), sender.getLastname()),
                new UserDTO(receiver.getId(), receiver.getFirstname(), receiver.getLastname()),
                savedMessage.getContent(),
                savedMessage.getTimestamp(),
                savedMessage.getRead()
        );
    }



    @Override
    public List<MessageDTO> getMessagesByChat(Chat chat) {
        List<Message> messages = messageRepository.findByChatOrderByTimestampAsc(chat);

        return messages.stream().map(message -> new MessageDTO(
                message.getId(),
                new UserDTO(message.getSender().getId(), message.getSender().getFirstname(), message.getSender().getLastname()),
                new UserDTO(message.getReceiver().getId(), message.getReceiver().getFirstname(), message.getReceiver().getLastname()),
                message.getContent(),
                message.getTimestamp(),
                message.getRead()
        )).toList();
    }
}
