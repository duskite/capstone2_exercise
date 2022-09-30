package com.example.spring;

import com.example.spring.Repository.JpaMemberRepository;
import com.example.spring.Repository.MemberRepository;
import com.example.spring.Service.EmailService;
import com.example.spring.Service.EmailServiceImpl;
import com.example.spring.Service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

@Configuration
public class SpringConfig {

    private EntityManager em;

    @Autowired
    public SpringConfig(EntityManager em){
        this.em = em;
    }

    @Bean
    public MemberService memberService(){
        return new MemberService(memberRepository());
    }

    @Bean
    public MemberRepository memberRepository(){
        return new JpaMemberRepository(em);
    }

    @Bean
    public EmailService emailService(){ return new EmailServiceImpl(memberService());}
}
