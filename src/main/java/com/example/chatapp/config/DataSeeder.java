package com.example.chatapp.config;

import com.example.chatapp.entity.*;
import com.example.chatapp.enums.ChannelType;
import com.example.chatapp.enums.MembershipRole;
import com.example.chatapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ServerRepository serverRepository;
    private final ChannelRepository channelRepository;
    private final MembershipRepository membershipRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("berk@test.com").isPresent()) {
            System.out.println("🚀 Test verileri zaten hazır, seeder atlanıyor.");
            return;
        }

        System.out.println("🧹 Eski veriler temizleniyor...");
        messageRepository.deleteAll();
        membershipRepository.deleteAll();
        channelRepository.deleteAll();
        serverRepository.deleteAll();
        userRepository.deleteAll();

        System.out.println("🌱 Veritabanı yeniden tohumlanıyor (Seeding)...");

        User admin = createUser("admin", "admin@test.com", "pass123");
        User user1 = createUser("berk", "berk@test.com", "pass123");
        User user2 = createUser("lucy", "lucy@test.com", "pass123");

        Server server = new Server();
        server.setName("Cyberpunk Edgerunners");
        server.setDescription("Night City Legend'ları için.");
        server.setOwner(admin);
        server.setSettings("{\"public\": true}");
        server = serverRepository.save(server);

 
        Channel textChannel = createChannel(server, "general-chat", ChannelType.TEXT); 
        Channel voiceChannel = createChannel(server, "voice-lobby", ChannelType.VOICE);

        addMember(server, admin, MembershipRole.OWNER);
        addMember(server, user1, MembershipRole.MEMBER);
        addMember(server, user2, MembershipRole.MEMBER);

        createMessage(textChannel, admin, "Welcome to Night City!");
        createMessage(textChannel, user1, "Preem server choom!");

        System.out.println("✅ Data Seeding completed.");
    }

    private User createUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setAvatarUrl("https://ui-avatars.com/api/?name=" + username);
        return userRepository.save(user);
    }

    private Channel createChannel(Server server, String name, ChannelType type) {
        Channel channel = new Channel();
        channel.setName(name);
        channel.setType(type);
        channel.setServer(server);
        channel.setSettings("{}");
        return channelRepository.save(channel);
    }

    private void addMember(Server server, User user, MembershipRole role) {
        Membership membership = new Membership();
        membership.setServer(server);
        membership.setUser(user);
        membership.setRole(role);
        membershipRepository.save(membership);
    }

    private void createMessage(Channel channel, User author, String content) {
        Message msg = new Message();
        msg.setChannel(channel);
        msg.setAuthor(author);
        msg.setContext(content);
        msg.setCreatedAt(LocalDateTime.now());
        messageRepository.save(msg);
    }
}