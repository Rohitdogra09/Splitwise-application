package com.splitwise.splitwise.backend.controller;


import com.splitwise.splitwise.backend.dto.UserBalanceDto;
import com.splitwise.splitwise.backend.entity.Expense;
import com.splitwise.splitwise.backend.entity.ExpenseSplit;
import com.splitwise.splitwise.backend.repository.ExpenseRepository;
import com.splitwise.splitwise.backend.repository.ExpenseSplitRepository;
import com.splitwise.splitwise.backend.repository.PaymentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/groups/{groupId}")
public class BalanceController {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final PaymentRepository paymentRepository;

    public BalanceController(ExpenseRepository expenseRepository,
                             ExpenseSplitRepository expenseSplitRepository,
                             PaymentRepository paymentRepository){
        this.expenseRepository=expenseRepository;
        this.expenseSplitRepository=expenseSplitRepository;
        this.paymentRepository=paymentRepository;
    }

    @GetMapping("/balances")
    public List<UserBalanceDto> getBalances(@PathVariable Long groupId){

        //userId -> paid total
        Map<Long, Double> paidMap = new HashMap<>();

        //userId-> share total
        Map<Long, Double> shareMap = new HashMap<>();

        //userId -> name
        Map<Long, String> nameMap = new HashMap<>();

        // 1. Load expenses and calculate paid amounts

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        for(Expense e: expenses){
            Long payerId = e.getPaidBy().getId();
            nameMap.put(payerId, e.getPaidBy().getName());
            paidMap.put(payerId, paidMap.getOrDefault(payerId, 0.0)+ e.getAmount());
        }
         //2. Load splits and calculate share amount
        List<ExpenseSplit> splits = expenseSplitRepository.findByExpenseGroupId(groupId);
        for(ExpenseSplit s: splits){
            Long uid=s.getUser().getId();
            nameMap.put(uid, s.getUser().getName());
            shareMap.put(uid, shareMap.getOrDefault(uid, 0.0)+s.getShareAmount());

        }

        var payments= paymentRepository.findByGroupId(groupId);
        for(var p : payments){
            Long fromId=p.getFromUser().getId();
            Long toId=p.getToUser().getId();

            nameMap.put(fromId, p.getFromUser().getName());
            nameMap.put(toId, p.getToUser().getName());

            // paying reduces what "from" owes _-> increses from's balance
            paidMap.put(fromId, paidMap.getOrDefault(fromId,0.0)+p.getAmount());

            //Receiving means they are owed less-> decreases to's balances
            paidMap.put(toId, paidMap.getOrDefault(toId,0.0)-p.getAmount());

        }

        //3. Merge all users and compute net balance =paid - share
        Set<Long> allUsers = new HashSet<>();
        allUsers.addAll(paidMap.keySet());
        allUsers.addAll(shareMap.keySet());

        List<UserBalanceDto> result = new ArrayList<>();
        for(Long uid: allUsers){
            double paid =paidMap.getOrDefault(uid, 0.0);
            double share = shareMap.getOrDefault(uid, 0.0);
            double balance = paid-share;

            //rount to 2 decimals for clean input

            balance=Math.round(balance *100.0)/100.0;

            result.add(new UserBalanceDto(uid, nameMap.get(uid), balance));
        }

        //sort by balance description( creditors first)
        result.sort((a,b)-> Double.compare(b.balance(), a.balance()));
        return result;

    }
    @GetMapping("/settlements")
    public java.util.List<com.splitwise.splitwise.backend.dto.SettlementDto> getSettlements(@PathVariable Long groupId){

        //reuse balances method logic
        List<UserBalanceDto> balances = getBalances(groupId);

        //creditors: balance>0, debtors: balance <0
        java.util.List<UserBalanceDto> creditors = balances.stream()
                .filter(b -> b.balance() > 0.0)
                .map(b -> new UserBalanceDto(b.userId(), b.name(),b.balance()))
                .toList();

        java.util.List<UserBalanceDto> debtors = balances.stream()
                .filter(b-> b.balance()<0.0)
                .map(b -> new UserBalanceDto(b.userId(), b.name(), -b.balance()))
                .toList();

        java.util.List<com.splitwise.splitwise.backend.dto.SettlementDto> result = new java.util.ArrayList<>();

        int i =0, j=0;

        while(i<debtors.size() && j< creditors.size()){
            UserBalanceDto debtor = debtors.get(i);
            UserBalanceDto creditor = creditors.get(j);

            double amount = Math.min(debtor.balance(), creditor.balance());
            amount= Math.round(amount*100)/100;

            result.add(new com.splitwise.splitwise.backend.dto.SettlementDto(
                    debtor.userId(), debtor.name(),
                    creditor.userId(), creditor.name(),
                    amount
            ));

            double debtorRemaining = debtor.balance()-amount;
            double creditorRemaining = creditor.balance()-amount;

            //move pointers

            if(debtorRemaining <=0.0001)
                i++;
            else debtors.set(i, new UserBalanceDto(debtor.userId(), debtor.name(), debtorRemaining));

            if(creditorRemaining<=0.0001) j++;
            else creditors.set(j, new UserBalanceDto(creditor.userId(), creditor.name(),creditorRemaining));
        }
        return result;
    }
}
