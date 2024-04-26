package com.example.fiatservice;

import com.shared.TransactionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@EnableKafka
public class FiatServiceKafkaConsumer
{

    private static final Logger LOG = LoggerFactory.getLogger(FiatServiceKafkaConsumer.class);

    @Autowired
    private FiatService fiatService;

    @KafkaListener(topics = "user_registered", groupId = "fiatServiceUserRegistrationGroup", containerFactory = "userRegistrationKafkaListenerContainerFactory")
    public void onUserRegistration(Long userId)
    {
        LOG.info("Initializing fiat balances for user: {}", userId);
        fiatService.initializeFiatBalancesForUser(userId);
    }

    @KafkaListener(topics = "transactions", groupId = "fiatServiceTransactionGroup", containerFactory = "transactionKafkaListenerContainerFactory")
    public void onTransaction(TransactionDto transaction) // ConsumerRecord<Long, TransactionDto> record
    {
        LOG.info("Received: {}", transaction);
        if(transaction.getStatus() == null) // this shouldn't be happening but it does
        {
            LOG.error("Transaction status is null");
            return;
        }
        if (transaction.getStatus().equals(TransactionDto.Status.NEW))
        {
            LOG.info("RESERVE FROM FIAT APP");
            fiatService.reserve(transaction);
        } else if (transaction.getStatus().equals(TransactionDto.Status.CONFIRMED))
        {
            LOG.info("CONFIRM FROM FIAT APP");
            fiatService.confirm(transaction);
        } else if (transaction.getStatus().equals(TransactionDto.Status.ROLLBACK) && !transaction.getSource().equals("fiat-service"))
        {
            fiatService.rollback(transaction);
            LOG.info("ROLLBACK FROM FIAT APP");
        }
    }
}
