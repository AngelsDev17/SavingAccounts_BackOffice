package com.saving.accounts.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saving.accounts.dto.AccountDto;
import com.saving.accounts.dto.SendMoneyDto;
import com.saving.accounts.enums.Type;
import com.saving.accounts.model.Account;
import com.saving.accounts.model.History;
import com.saving.accounts.model.User;
import com.saving.accounts.repository.iAccountRepository;
import com.saving.accounts.repository.iHistoryRepository;
import com.saving.accounts.repository.iUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/account")
@CrossOrigin
@AllArgsConstructor
public class AccountController {

    private final iUserRepository userRepository;
    private final iAccountRepository accountRepository;
    private final iHistoryRepository historyRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    @PostMapping("generate-user-and-account")
    public void generateAccount(@RequestBody AccountDto account) throws Exception {
        var existingAccount = userRepository.findOneByIdentification(account.getIdentification());

        if (!existingAccount.isEmpty()) throw new Exception("No fue posible generar la cuenta.");

        String accountNumber = generateAccountNumber();

        Account accountModel = this.modelMapper.map(account, Account.class);
        accountModel.setAccount(accountNumber);

        accountRepository.save(accountModel);

        User userModel = this.modelMapper.map(account, User.class);
        userModel.setPassword(new BCryptPasswordEncoder().encode(account.getPassword()));
        userModel.setAccount(accountNumber);

        userRepository.save(userModel);
    }

    @GetMapping("get-account-amount")
    public Integer getAccountAmount(HttpServletRequest request) throws Exception {
        String accountNumber = getFieldFromBearerToken(request, "account");

        var account = accountRepository.findOneByAccount(accountNumber)
                .orElseThrow(() -> new UsernameNotFoundException("La cuenta " + accountNumber + "no existe..."));

        return account.getAmount();
    }

    @GetMapping("get-account-history")
    public List<History> getAccountHistory(HttpServletRequest request) throws Exception {
        String accountNumber = getFieldFromBearerToken(request, "account");
        var history = historyRepository.findAllByAccount(accountNumber);

        if (history.isEmpty()) return null;

        return history.get();
    }

    @PostMapping("send-money")
    public void sendMoney(@RequestBody SendMoneyDto sendMoney, HttpServletRequest request) throws Exception {
        String accountNumber = getFieldFromBearerToken(request, "account");

        var account = accountRepository.findOneByAccount(accountNumber)
                .orElseThrow(() -> new UsernameNotFoundException("La cuenta " + accountNumber + "no existe..."));

        if (account.getAmount() < sendMoney.getAmount())
            throw new UsernameNotFoundException("No cuenta con fondos suficientes");

        var accountToSend = accountRepository.findOneByAccount(sendMoney.getAccount())
                .orElseThrow(() -> new UsernameNotFoundException("La cuenta " + accountNumber + "no existe..."));

        accountRepository.updateAmmountByAccount(
                accountToSend.getAmount() + sendMoney.getAmount(),
                sendMoney.getAccount());

        accountRepository.updateAmmountByAccount(
                account.getAmount() - sendMoney.getAmount(),
                accountNumber);

        History historyToSend = new History();

        historyToSend.setAccount(account.getAccount());
        historyToSend.setDestination(accountToSend.getAccount());
        historyToSend.setAmount(sendMoney.getAmount());
        historyToSend.setDate(new Date());
        historyToSend.setType(Type.SendMoney.name());

        historyRepository.save(historyToSend);

        History historyToReceive = new History();

        historyToReceive.setAccount(accountToSend.getAccount());
        historyToReceive.setDestination(account.getAccount());
        historyToReceive.setAmount(sendMoney.getAmount());
        historyToReceive.setDate(new Date());
        historyToReceive.setType(Type.ReceiveMoney.name());

        historyRepository.save(historyToReceive);
    }

    @PostMapping("withdraw-money")
    public void withdrawMoney(@RequestBody Integer amount, HttpServletRequest request) throws Exception {
        String accountNumber = getFieldFromBearerToken(request, "account");

        var account = accountRepository.findOneByAccount(accountNumber)
                .orElseThrow(() -> new UsernameNotFoundException("La cuenta " + accountNumber + "no existe..."));

        if (account.getAmount() < amount)
            throw new UsernameNotFoundException("No cuenta con fondos suficientes");

        accountRepository.updateAmmountByAccount(
                account.getAmount() - amount,
                accountNumber);

        History historyToSend = new History();

        historyToSend.setAccount(account.getAccount());
        historyToSend.setDestination("N/A");
        historyToSend.setAmount(amount);
        historyToSend.setDate(new Date());
        historyToSend.setType(Type.WithdrawMoney.name());

        historyRepository.save(historyToSend);
    }

    private String generateAccountNumber() {
        UUID uuid = UUID.randomUUID();

        String uuidStr = uuid.toString().replaceAll("-", "");
        String uuidNum = uuidStr.replaceAll("[^\\d]", "");
        return uuidNum.replaceAll("(.{8})(.{4})(.{4})(.{4})(.+)", "$1-$2-$3-$4-$5").substring(0, 18);
    }
    private String getFieldFromBearerToken(HttpServletRequest request, String field) throws JsonProcessingException {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        ObjectMapper objectMapper = new ObjectMapper();

        String bearerToken = request.getHeader("Authorization").replace("Bearer ", "");
        String tokenData = new String(decoder.decode(bearerToken.split("\\.")[1]));

        JsonNode rootNode = objectMapper.readTree(tokenData);
        return rootNode.get(field.toLowerCase()).asText();
    }
}
