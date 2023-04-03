package com.lookatme.server.config;

import com.lookatme.server.job.QuartzScheduler;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;
import java.util.Properties;

@RequiredArgsConstructor
@Configuration
public class QuartzConfig {
    private final JobLauncher jobLauncher;
    private final JobLocator jobLocator;
    private final JobRegistry jobRegistry;

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }

    @Bean
    public JobDetail simpleJobDetail() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobName", "simpleJob");
        jobDataMap.put("jobLauncher", jobLauncher);
        jobDataMap.put("jobLocator", jobLocator);

        /**
         * Job을 실행시킬때마다 JobInstance 생성
         * - JobInstance를 실행 시도할때마다 JobExecution이 생성
         * -> JobInstance가 실패할 경우 해당 JobInstance를 재실행 할 수 있음. (JobInstance는 고정 / JobExecution은 새로 생성)
         */
        return JobBuilder.newJob(QuartzScheduler.class)
                .withIdentity("simpleJob")
                .setJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    @Bean
    public JobDetail flowJobDetail() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobName", "flowJob");
        jobDataMap.put("jobLauncher", jobLauncher);
        jobDataMap.put("jobLocator", jobLocator);

        return JobBuilder.newJob(QuartzScheduler.class)
                .withIdentity("flowJob")
                .setJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    @Bean
    public JobDetail chunkJobDetail() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobName", "chunkJob");
        jobDataMap.put("jobLauncher", jobLauncher);
        jobDataMap.put("jobLocator", jobLocator);

        return JobBuilder.newJob(QuartzScheduler.class)
                .withIdentity("chunkJob")
                .setJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger simpleJobTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInSeconds(10)
                .repeatForever();

        return TriggerBuilder
                .newTrigger()
                .forJob(simpleJobDetail())
                .withIdentity("simpleJobTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }

    @Bean
    public Trigger flowJobTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInSeconds(30)
                .repeatForever();

        return TriggerBuilder
                .newTrigger()
                .forJob(flowJobDetail())
                .withIdentity("flowJobTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }

    @Bean
    public Trigger chunkJobTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInMinutes(30) // 30분 간격으로 실행
                .repeatForever();

        return TriggerBuilder
                .newTrigger()
                .forJob(chunkJobDetail())
                .withIdentity("chunkJobTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        scheduler.setTriggers(chunkJobTrigger());
        scheduler.setQuartzProperties(quartzProperties());
        scheduler.setJobDetails(chunkJobDetail());
        return scheduler;
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }
}
