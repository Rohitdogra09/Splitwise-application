package com.splitwise.splitwise.backend.controller;

import com.splitwise.splitwise.backend.dto.CreatePaymentRequest;
import com.splitwise.splitwise.backend.entity.Group;
import com.splitwise.splitwise.backend.entity.Payment;
import com.splitwise.splitwise.backend.entity.User;
import com.splitwise.splitwise.backend.repository.GroupRepository;
import com.splitwise.splitwise.backend.repository.PaymentRepository;
import com.splitwise.splitwise.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/groups/{groupId}/payments")
public class PaymentController {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public PaymentController(GroupRepository groupRepository,
                             UserRepository userRepository,
                             PaymentRepository paymentRepository){
        this.groupRepository=groupRepository;
        this.userRepository=userRepository;
        this.paymentRepository=paymentRepository;
    }

    @PostMapping
    public Payment createPayemnt(@PathVariable Long groupId,
                                 @RequestBody CreatePaymentRequest request){
        if(request.amount()<=0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"amount must be greater than 0");
        }

        Group group=groupRepository.findById(groupId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        User from = userRepository.findById(request.fromUserId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"fromUser not found"));

        User to= userRepository.findById(request.toUserId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"toUser not found"));

        Payment payment=Payment.builder()
                .group(group)
                .fromUser(from)
                .toUser(to)
                .amount(request.amount())
                .createdAt(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);

    }
}
