package com.splitwise.splitwise.backend.controller;

import com.splitwise.splitwise.backend.dto.CreateExpenseRequest;
import com.splitwise.splitwise.backend.entity.Expense;
import com.splitwise.splitwise.backend.entity.ExpenseSplit;
import com.splitwise.splitwise.backend.entity.Group;
import com.splitwise.splitwise.backend.entity.User;
import com.splitwise.splitwise.backend.repository.ExpenseRepository;
import com.splitwise.splitwise.backend.repository.ExpenseSplitRepository;
import com.splitwise.splitwise.backend.repository.GroupRepository;
import com.splitwise.splitwise.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;

    public ExpenseController(GroupRepository groupRepository,
                             UserRepository userRepository,
                             ExpenseRepository expenseRepository,
                             ExpenseSplitRepository expenseSplitRepository){
        this.groupRepository=groupRepository;
        this.userRepository=userRepository;
        this.expenseRepository=expenseRepository;
        this.expenseSplitRepository=expenseSplitRepository;
    }

    @PostMapping
    public Expense createExpense(@PathVariable Long groupId,
                                 @RequestBody CreateExpenseRequest request){
        Group group = groupRepository.findById(groupId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Group not found"));

        User paidBy= userRepository.findById(request.paidByUserId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Payer not found"));

        double totalSplit=request.splits().values().stream().mapToDouble(Double::doubleValue).sum();
        if(Math.abs(totalSplit - request.amount())>0.01){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Split amount do not match total");

        }
        Expense expense = Expense.builder()
                .group(group)
                .title(request.title())
                .amount(request.amount())
                .paidBy(paidBy)
                .createdAt(LocalDateTime.now())
                .build();

        expenseRepository.save(expense);

        request.splits().forEach((userId, share)->{
            User user = userRepository.findById(userId)
                    .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));

            ExpenseSplit split= ExpenseSplit.builder()
                    .expense(expense)
                    .user(user)
                    .shareAmount(share)
                    .build();
            expenseSplitRepository.save(split);


        });
        return expense;
    }
}
